package com.sarang.torang.di.repository

import android.util.Log
import com.gmail.bishoybasily.stomp.lib.Message
import com.google.gson.GsonBuilder
import com.sarang.torang.BuildConfig
import com.sarang.torang.api.ApiChat
import com.sarang.torang.core.database.dao.LoggedInUserDao
import com.sarang.torang.core.database.dao.UserDao
import com.sarang.torang.core.database.dao.chat.ChatMessageDao
import com.sarang.torang.core.database.dao.chat.ChatParticipantsDao
import com.sarang.torang.core.database.dao.chat.ChatRoomDao
import com.sarang.torang.core.database.model.chat.ChatImageEntity
import com.sarang.torang.core.database.model.chat.ChatMessageEntity
import com.sarang.torang.core.database.model.chat.ChatParticipantsEntity
import com.sarang.torang.core.database.model.chat.ChatRoomEntity
import com.sarang.torang.core.database.model.chat.embedded.ChatMessageUserImages
import com.sarang.torang.core.database.model.chat.embedded.ChatParticipantUser
import com.sarang.torang.core.database.model.chat.embedded.ChatRoomUser
import com.sarang.torang.core.database.model.user.UserEntity
import com.sarang.torang.data.ChatImage
import com.sarang.torang.data.ChatMessage
import com.sarang.torang.data.ChatRoom
import com.sarang.torang.data.User
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
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val apiChat             : ApiChat,
    private val chatMessageDao      : ChatMessageDao,
    private val chatRoomDao         : ChatRoomDao,
    private val chatParticipantsDao : ChatParticipantsDao,
    private val sessionService      : SessionService,
    private val userDao             : UserDao,
    private val loggedInUserDao     : LoggedInUserDao,
) :
    ChatRepository {
    private var webSocketClient = WebSocketClient()
    private val uploadingList: ArrayList<String> = ArrayList()

    init { connectWebSocket() }

    override suspend fun refreshAllChatRooms(): Result<Unit> {
        val token = sessionService.getToken() ?: return Result.failure(Exception("채팅방 로딩에 실패하였습니다. 로그인을 해주세요."))
        val user = loggedInUserDao.getLoggedInUser() ?: return Result.failure(Exception("채팅방 로딩에 실패하였습니다. 로그인 사용자 정보가 없습니다."))
        val chatRooms = try {
            apiChat.getChatRoom(token)
        }catch (e : Exception){
            return Result.failure(Exception("채팅방 정보를 가져오는데 실패하였습니다."))
        }

        chatRoomDao.deleteAll()
        chatParticipantsDao.deleteAll()
        chatRoomDao.addAll(chatRooms.chatRoomEntityList)
        chatParticipantsDao.addAll(chatRooms.chatParticipantsEntityList
            .filter { it.userId != user.userId } // 로그인 사용자는 제외하고 넣기. 나중에 채팅방 불러올 때 처리 까다로움.
        )
        userDao.insertOrUpdateUser(chatRooms.users)
        return Result.success(Unit)
    }

    override fun getAllChatRoomsFlow(): Flow<List<ChatRoom>> {
        val chatRoomsFlow : Flow<List<ChatRoomUser>> = chatRoomDao.findAllChatRoom(chatRoomDao, userDao)
        return chatRoomsFlow.map { chatRooms ->
            chatRooms.map { chatRoom ->
                ChatRoom(
                    chatParticipants = chatRoom.chatParticipants.map { it.user },
                    roomId = chatRoom.chatRoom.roomId,
                    createDate = chatRoom.chatRoom.createDate
                )
            }
        }
    }

    override suspend fun addChat(roomId: Int, message: String, uuid: String, ) {
        sessionService.getToken()?.let { auth ->
            loggedInUserDao.getLoggedInUser()?.userId?.let {
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
                chatMessageDao.add(chat)

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
        loggedInUserDao.getLoggedInUser()?.userId?.let {
            /*chatMessageDao.addImage(
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
            )*/
        }
    }

    override fun getChatsFlow(roomId: Int): Flow<List<ChatMessage>> {
        return chatMessageDao.findByRoomId(roomId = roomId).map { chatMessageUserImages ->
            chatMessageUserImages.map { it.chatMessage(roomId) }
        }
    }

    override suspend fun loadChats(roomId: Int) {
        val token = sessionService.getToken() ?: throw Exception("로그인을 해주세요.")

        val result : List<ChatApiModel> = apiChat.getContents(token, roomId)
        Log.d("__ChatRepositoryImpl", "loaded chat roomId : $roomId, chatSize: ${result.size}")

        chatMessageDao.addAll(result.chats)
    }

    override suspend fun removeAll() {
        chatRoomDao.deleteAll()
        chatMessageDao.deleteAll()
        chatParticipantsDao.deleteAll()
    }

    override suspend fun updateFailedUploadImage(roomId: Int) {
//        chatDao.updateFailedSendImages(uploadingList, roomId)
    }

    override suspend fun getUserOrCreateRoomByUserId(userId: Int): ChatRoom {
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

        return ChatRoom(
            roomId = 0,
            createDate = "",
            chatParticipants = listOf()
        )

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
