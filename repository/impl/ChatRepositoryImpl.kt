package com.sarang.torang.di.repository.repository.impl

import android.util.Log
import com.gmail.bishoybasily.stomp.lib.Message
import com.google.gson.GsonBuilder
import com.sarang.torang.api.ApiChat
import com.sarang.torang.core.database.dao.ChatDao
import com.sarang.torang.core.database.dao.LoggedInUserDao
import com.sarang.torang.core.database.dao.UserDao
import com.sarang.torang.core.database.model.chat.ChatEntity
import com.sarang.torang.core.database.model.chat.ChatEntityWithUser
import com.sarang.torang.core.database.model.chat.ChatRoomEntity
import com.sarang.torang.core.database.model.chat.ChatRoomWithParticipantsAndUsers
import com.sarang.torang.core.database.model.chat.ChatRoomWithParticipantsEntity
import com.sarang.torang.core.database.model.chat.ParticipantsWithUser
import com.sarang.torang.core.database.model.chat.ParticipantsWithUserEntity
import com.sarang.torang.data.remote.response.ChatApiModel
import com.sarang.torang.di.repository.toChatRoomEntity
import com.sarang.torang.repository.ChatRepository
import com.sarang.torang.session.SessionService
import com.sarang.torang.util.WebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val apiChat: ApiChat,
    private val chatDao: ChatDao,
    private val sessionService: SessionService,
    private val userDao: UserDao,
    private val loggedInUserDao: LoggedInUserDao,
) :
    ChatRepository {
    private var webSocketClient = WebSocketClient()

    init {
        try {
            webSocketClient.connect()
        } catch (e: Exception) {
            Log.e("__ChatRepositoryImpl", "Error connecting to WebSocket: ${e.message}")
        }
    }

    private val uploadingList: ArrayList<String> = ArrayList()

    override suspend fun loadChatRoom() {
        Log.d("__ChatRepositoryImpl", "loadChatRoom")
        val token = sessionService.getToken() ?: throw Exception("채팅방 로딩에 실패하였습니다. 로그인을 해주세요.")
        //TODO::데이터 변환하기
        chatDao.loadChatRoom(userDao/*, apiChat.getChatRoom(token)*/)
    }

    override suspend fun loadContents(roomId: Int) {
        val token = sessionService.getToken() ?: throw Exception("로그인을 해주세요.")

        val result = apiChat.getContents(token, roomId)
        Log.d("__ChatRepositoryImpl", "loaded chat roomId : $roomId, chatSize: ${result.size}")

        chatDao.addAllChat(
            result.map { it.toChatEntity() }
        )
    }

    override fun getChatRoom(): Flow<List<ChatRoomWithParticipantsEntity>> {
        return chatDao.getChatRoom()
    }

    override fun getChatRoom1(): Flow<List<ChatRoomEntity>> {
        return chatDao.getChatRoom1()
    }

    override suspend fun getUserOrCreateRoomByUserId(userId: Int): ChatRoomWithParticipantsAndUsers {
        var chatRoom = chatDao.getChatRoomByUserId(userId)

        if (chatRoom == null) {
            val result = apiChat.createChatRoom(sessionService.getToken() ?: "", userId)
            chatDao.addAll(listOf(result.toChatRoomEntity()))
            //TODO::데이터 변환하기
            userDao.insertOrUpdateUser(/*result.users*/)
            //TODO::데이터 변환하기
            chatDao.insertParticipants(/*listOf(result)*/)
        }

        chatRoom = chatDao.getChatRoomByUserId(userId)

        if (chatRoom == null)
            throw throw Exception("채팅방 생성에 실패 하였습니다.")

        val participantsWithUserEntity =
            chatDao.getParticipantsWithUsers(chatRoom.chatRoomEntity.roomId)
                ?: throw throw Exception("참여자 정보를 가져오는데 실패했습니다.")

        return ChatRoomWithParticipantsAndUsers(
            chatRoomEntity = chatRoom.chatRoomEntity,
            participantsWithUsers = participantsWithUserEntity.map {
                ParticipantsWithUser(
                    roomId = it.participantsEntity.roomId,
                    userId = it.userEntity.userId,
                    userName = it.userEntity.userName,
                    profilePicUrl = it.userEntity.profilePicUrl,
                )
            }
        )
    }

    override fun getChatRoomsWithParticipantsAndUsers(): Flow<List<ChatRoomWithParticipantsAndUsers>> {
        // 첫 번째 Flow: ChatRoomEntity 목록 가져오기
        val chatRoomFlow = chatDao.getChatRoom()

        // 두 번째 Flow: 각 ChatRoomEntity에 대응하는 ParticipantsWithUserEntity 목록 가져오기
        return chatRoomFlow.flatMapLatest { chatRooms ->
            // 각 채팅방에 대한 Participants 정보를 가져와서 결합
            val flows = chatRooms.map { chatRoom ->
                chatDao.getParticipantsWithUsersFlow(chatRoom.chatRoomEntity.roomId)
                    .map { participantsWithUsers ->
                        ChatRoomWithParticipantsAndUsers(
                            chatRoomEntity = chatRoom.chatRoomEntity,
                            participantsWithUsers = participantsWithUsers?.map { it.toParticipantsWithUser() }
                                ?: listOf()
                        )
                    }
            }
            // 여러 Flow를 결합하여 결과를 반환
            combine(flows) { it.toList() }
        }
    }

    override suspend fun addChat(
        roomId: Int,
        message: String,
        uuid: String,
    ) {
        sessionService.getToken()?.let { auth ->
            loggedInUserDao.getLoggedInUser1()?.userId?.let {
                val chat = ChatEntity(
                    uuid = uuid,
                    roomId = roomId,
                    userId = it,
                    message = message,
                    createDate = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.KOREA
                    ).format(System.currentTimeMillis()),
                    sending = true
                )
                //로컬 DB에 우선 추가
                chatDao.addChat(chat)

                try {
                    webSocketClient.sendMessage(auth, chat.uuid, roomId, chat.message)
                } catch (e: Exception) {
                    Log.e("__ChatRepositoryImpl", "Error sending message: ${e.message}")
                    throw Exception("메시지 전송에 실패하였습니다.")
                }
            }
        }
    }

    override suspend fun addImage(roomId: Int, message: List<String>, uuid: String) {
        Log.d("__ChatRepositoryImpl", "request add image : $message")

        uploadingList.addAll(message)

        //uuid가 외래키가 걸려있어 ChatEntity에 먼저 추가해줘야 함.
        addChat(roomId, "", uuid)

        //로컬 DB에 추가하기
        loggedInUserDao.getLoggedInUser1()?.userId?.let {
            chatDao.addImage1(
                parentUuid = uuid,
                roomId = roomId,
                userId = it,
                createDate = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.KOREA
                ).format(System.currentTimeMillis()),
                uploadedDate = "",
                sending = true,
                message = message,
            )
        }
    }


    override suspend fun removeAll() {
        chatDao.deleteAllChatRoom()
        chatDao.deleteAllParticipants()
        chatDao.deleteAllChat()
    }

    override suspend fun subscribe(roomId: Int) {
        webSocketClient.subScribe(roomId)
    }

    override fun event(coroutineScope: CoroutineScope): Flow<Message> {
        webSocketClient.subScribeEvent(coroutineScope)
        return callbackFlow {
            webSocketClient.getFlow().collect {
                trySend(it)
                if (it.command == "MESSAGE") {
                    it.payload?.let {
                        val chat = GsonBuilder().create().fromJson(it, ChatApiModel::class.java)
                        chatDao.delete(chat.uuid)
                        chatDao.addChat(chat.toChatEntity())
                    }
                }
            }
            awaitClose()
        }
    }

    override fun unSubscribe(topic: Int) {
        webSocketClient.unSubscribe(topic)
    }

    override suspend fun updateFailedUploadImage(roomId: Int) {
        chatDao.updateFailedSendImages(uploadingList, roomId)
    }

    override fun getContents(roomId: Int): Flow<List<ChatEntityWithUser>> {
        return chatDao.getContents(roomId)
    }
}

fun ChatApiModel.toChatEntity(): ChatEntity = ChatEntity(
    roomId = roomId,
    createDate = createDate,
    message = message,
    userId = userId,
    uuid = uuid,
    sending = false // 전송 완료
)

fun ParticipantsWithUserEntity.toParticipantsWithUser(): ParticipantsWithUser = ParticipantsWithUser(
    roomId = this.participantsEntity.roomId,
    userId = this.userEntity.userId,
    userName = this.userEntity.userName,
    profilePicUrl = this.userEntity.profilePicUrl,
)