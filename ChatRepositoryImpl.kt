package com.sarang.torang.di.repository

import android.util.Log
import com.gmail.bishoybasily.stomp.lib.Message
import com.google.gson.GsonBuilder
import com.sarang.torang.api.ApiChat
import com.sarang.torang.core.database.dao.LoggedInUserDao
import com.sarang.torang.core.database.dao.UserDao
import com.sarang.torang.core.database.dao.chat.ChatMessageDao
import com.sarang.torang.core.database.dao.chat.ChatParticipantsDao
import com.sarang.torang.core.database.dao.chat.ChatRoomDao
import com.sarang.torang.core.database.model.chat.ChatMessageEntity
import com.sarang.torang.core.database.model.chat.ChatRoomEntity
import com.sarang.torang.core.database.model.chat.embedded.ChatMessageUserImages
import com.sarang.torang.core.database.model.chat.embedded.ChatParticipantUser
import com.sarang.torang.core.database.model.chat.embedded.ChatRoomParticipants
import com.sarang.torang.data.remote.response.ChatApiModel
import com.sarang.torang.di.torang_database_di.chatParticipantsEntityList
import com.sarang.torang.di.torang_database_di.chatRoomEntityList
import com.sarang.torang.di.torang_database_di.chats
import com.sarang.torang.di.torang_database_di.users
import com.sarang.torang.repository.ChatRepository
import com.sarang.torang.session.SessionService
import com.sarang.torang.util.WebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val apiChat                     : ApiChat,
    private val chatMessageDao              : ChatMessageDao,
    private val chatRoomDao                 : ChatRoomDao,
    private val chatParticipantsDao         : ChatParticipantsDao,
    private val sessionService              : SessionService,
    private val userDao                     : UserDao,
    private val loggedInUserDao             : LoggedInUserDao,
) :
    ChatRepository {
    private var webSocketClient = WebSocketClient()
    private val uploadingList: ArrayList<String> = ArrayList()

    init { connectWebSocket() }

    override suspend fun refreshAllChatRooms() {
        val token = sessionService.getToken() ?: throw Exception("채팅방 로딩에 실패하였습니다. 로그인을 해주세요.")

        val chatRooms = apiChat.getChatRoom(token)

        chatRoomDao.deleteAll()
        chatRoomDao.addAll(chatRooms.chatRoomEntityList)
        chatParticipantsDao.deleteAll()
        chatParticipantsDao.addAll(chatRooms.chatParticipantsEntityList)
        userDao.insertOrUpdateUser(chatRooms.users)

    }

    override fun getAllChatRoomsFlow(): Flow<List<ChatRoomParticipants>> {
        // 첫 번째 Flow: ChatRoomEntity 목록 가져오기
        val chatRoomFlow = chatRoomDao.findAllFlow()

        // 두 번째 Flow: 각 ChatRoomEntity에 대응하는 ParticipantsWithUserEntity 목록 가져오기
        return chatRoomFlow.flatMapLatest { chatRooms ->
            // 각 채팅방에 대한 Participants 정보를 가져와서 결합
            val flows = chatRooms.map { chatRoom ->
                chatParticipantsDao.findByRoomIdFlow(chatRoom.roomId)
                    .map { participantsUsers ->
                        ChatRoomParticipants(
                            chatRoom = chatRoom,
                            chatParticipants = participantsUsers?.map {
                                ChatParticipantUser(
                                    participantsEntity = it.participantsEntity,
                                    userEntity = it.userEntity
                                )
                            } ?: emptyList()
                        )
                    }
            }
            // 여러 Flow를 결합하여 결과를 반환
            combine(flows) { it.toList() }
        }
    }

    override suspend fun addChat(roomId: Int, message: String, uuid: String, ) {
        sessionService.getToken()?.let { auth ->
            loggedInUserDao.getLoggedInUser1()?.userId?.let {
                val chat = ChatMessageEntity(
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
                //chatDao.addChat(chat)

                try {
                    webSocketClient.sendMessage(auth, chat.uuid, roomId, chat.message)
                } catch (e: Exception) {
                    Log.e("__ChatRepositoryImpl", "Error sending message: ${e.message}")
                    throw Exception("메시지 전송에 실패하였습니다.")
                }
            }
        }
    }

    override suspend fun addImageChat(roomId: Int, message: List<String>, uuid: String) {
        Log.d("__ChatRepositoryImpl", "request add image : $message")

        uploadingList.addAll(message)

        //uuid가 외래키가 걸려있어 ChatEntity에 먼저 추가해줘야 함.
        addChat(roomId, "", uuid)

        //로컬 DB에 추가하기
        /*loggedInUserDao.getLoggedInUser1()?.userId?.let {
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
        }*/
    }

    override fun getChatsFlow(roomId: Int): Flow<List<ChatMessageUserImages>> {
        return chatMessageDao.findByRoomId(roomId = roomId)
    }

    override suspend fun loadChats(roomId: Int) {
        val token = sessionService.getToken() ?: throw Exception("로그인을 해주세요.")

        val result : List<ChatApiModel> = apiChat.getContents(token, roomId)
        Log.d("__ChatRepositoryImpl", "loaded chat roomId : $roomId, chatSize: ${result.size}")

        chatMessageDao.addAll(result.chats)
    }

    override suspend fun removeAll() {
//        chatDao.deleteAllChatRoom()
//        chatDao.deleteAllParticipants()
//        chatDao.deleteAllChat()
    }

    override suspend fun updateFailedUploadImage(roomId: Int) {
//        chatDao.updateFailedSendImages(uploadingList, roomId)
    }

    override suspend fun getUserOrCreateRoomByUserId(userId: Int): ChatRoomParticipants {
        //var chatRoom = chatDao.getChatRoomByUserId(userId)

        /*if (chatRoom == null) {
            val result = apiChat.createChatRoom(sessionService.getToken() ?: "", userId)
            chatDao.addAll(listOf(result.toChatRoomEntity()))
            //TODO::데이터 변환하기
            userDao.insertOrUpdateUser(*//*result.users*//*)
            //TODO::데이터 변환하기
            chatDao.insertParticipants(*//*listOf(result)*//*)
        }*/

        //chatRoom = chatDao.getChatRoomByUserId(userId)

        /*if (chatRoom == null)
            throw throw Exception("채팅방 생성에 실패 하였습니다.")*/

        /*val participantsWithUserEntity =
            chatDao.getParticipantsWithUsers(chatRoom.chatRoomEntity.roomId)
                ?: throw throw Exception("참여자 정보를 가져오는데 실패했습니다.")*/

        return ChatRoomParticipants(
            chatRoom = ChatRoomEntity(roomId = 0, createDate = ""), listOf())

        /*return ChatRoomWithParticipantsAndUsers(
            chatRoomEntity = chatRoom.chatRoomEntity,
            participantsWithUsers = participantsWithUserEntity.map {
                ParticipantsWithUser(
                    roomId = it.participantsEntity.roomId,
                    userId = it.userEntity.userId,
                    userName = it.userEntity.userName,
                    profilePicUrl = it.userEntity.profilePicUrl,
                )
            }
        )*/
    }

    private fun connectWebSocket(){
        try {
            webSocketClient.connect()
        } catch (e: Exception) {
            Log.e("__ChatRepositoryImpl", "Error connecting to WebSocket: ${e.message}")
        }
    }

    override fun event(coroutineScope: CoroutineScope): Flow<Message> {
        webSocketClient.subScribeEvent(coroutineScope)
        return callbackFlow {
            webSocketClient.getFlow().collect {
                trySend(it)
                if (it.command == "MESSAGE") {
                    it.payload?.let {
                        val chat = GsonBuilder().create().fromJson(it, ChatApiModel::class.java)
//                        chatDao.delete(chat.uuid)
//                        chatDao.addChat(chat.toChatEntity())
                    }
                }
            }
            awaitClose()
        }
    }

    override suspend fun subscribe(roomId: Int) {
        webSocketClient.subScribe(roomId)
    }

    override fun unSubscribe(topic: Int) {
        webSocketClient.unSubscribe(topic)
    }
}

/*fun ChatApiModel.toChatEntity(): ChatRoomEntity = ChatRoomEntity(
    roomId = roomId,
    createDate = createDate,
    message = message,
    userId = userId,
    uuid = uuid,
    sending = false // 전송 완료
)*/

/*
fun ParticipantsWithUserEntity.toParticipantsWithUser(): ParticipantsWithUser = ParticipantsWithUser(
    roomId = this.participantsEntity.roomId,
    userId = this.userEntity.userId,
    userName = this.userEntity.userName,
    profilePicUrl = this.userEntity.profilePicUrl,
)*/
