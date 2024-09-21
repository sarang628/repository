package com.sarang.torang.di.repository.repository.impl

import com.sarang.torang.api.ApiChat
import com.sarang.torang.data.dao.ChatDao
import com.sarang.torang.data.dao.LoggedInUserDao
import com.sarang.torang.data.dao.UserDao
import com.sarang.torang.data.entity.ChatEntity
import com.sarang.torang.data.entity.ChatEntityWithUser
import com.sarang.torang.data.entity.ChatParticipantsEntity
import com.sarang.torang.data.entity.ChatRoomEntity
import com.sarang.torang.data.entity.ChatRoomWithParticipantsAndUsers
import com.sarang.torang.data.entity.ChatRoomWithParticipantsEntity
import com.sarang.torang.data.entity.UserEntity
import com.sarang.torang.data.remote.response.ChatApiModel
import com.sarang.torang.data.remote.response.ChatRoomApiModel
import com.sarang.torang.data.remote.response.ChatUserApiModel
import com.sarang.torang.repository.ChatRepository
import com.sarang.torang.session.SessionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private suspend fun insertParticipants(
    chatDao: ChatDao,
    users: List<ChatUserApiModel>,
    roomId: Int,
) {
    chatDao.insertParticipats(
        users.map { user ->
            ChatParticipantsEntity(
                roomId = roomId,
                userId = user.userId
            )
        }
    )
}

private suspend fun insertOrUpdateUser(userDao: UserDao, users: List<ChatUserApiModel>) {
    users.forEach { user ->
        if (userDao.exists(user.userId) > 0) {
            userDao.updateByChatRoom(
                user.userId,
                user.userName,
                user.profilePicUrl
            )
        } else {
            userDao.insertUser(
                UserEntity(
                    userId = user.userId,
                    userName = user.userName,
                    email = "",
                    loginPlatform = "",
                    createDate = "",
                    accessToken = "",
                    profilePicUrl = user.profilePicUrl,
                    point = 0,
                    reviewCount = "",
                    followers = "",
                    following = ""
                )
            )
        }
    }
}

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val apiChat: ApiChat,
    private val chatDao: ChatDao,
    private val sessionService: SessionService,
    private val userDao: UserDao,
    private val loggedInUserDao: LoggedInUserDao,
) :
    ChatRepository {
    override suspend fun loadChatRoom() {
        sessionService.getToken()?.let {
            val result = apiChat.getChatRoom(it)
            chatDao.deleteAllChatRoom()
            chatDao.deleteAllParticipants()
            chatDao.addAll(result.map { it.toChatRoomEntity() })
            result.forEach { chatRoom ->
                insertOrUpdateUser(userDao, chatRoom.users)
                insertParticipants(chatDao, chatRoom.users, chatRoom.roomId)
            }
        }
    }

    override suspend fun loadContents(roomId: Int) {
        sessionService.getToken()?.let {
            val result = apiChat.getContents(it, roomId)
            chatDao.addAllChat(result.map { it.toChatEntity() })
        }
    }

    override fun getChatRoom(): Flow<List<ChatRoomWithParticipantsEntity>> {
        return chatDao.getChatRoom()
    }

    override suspend fun getUserOrCreateRoomByUserId(userId: Int): ChatRoomWithParticipantsAndUsers {
        var chatRoom = chatDao.getChatRoomByUserId(userId)

        if (chatRoom == null) {
            val result = apiChat.createChatRoom(sessionService.getToken() ?: "", userId)
            chatDao.addAll(listOf(result.toChatRoomEntity()))
            insertOrUpdateUser(userDao, result.users)
            insertParticipants(chatDao, result.users, result.roomId)
        }

        chatRoom = chatDao.getChatRoomByUserId(userId)

        if (chatRoom == null)
            throw throw Exception("채팅방 생성에 실패 하였습니다.")

        val participantsWithUserEntity =
            chatDao.getParticipantsWithUsers(chatRoom.chatRoomEntity.roomId)

        if (participantsWithUserEntity == null)
            throw throw Exception("참여자 정보를 가져오는데 실패했습니다.")

        return ChatRoomWithParticipantsAndUsers(
            chatRoomEntity = chatRoom.chatRoomEntity,
            participantsWithUsers = participantsWithUserEntity
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
                            participantsWithUsers = participantsWithUsers ?: listOf()
                        )
                    }
            }
            // 여러 Flow를 결합하여 결과를 반환
            combine(flows) { it.toList() }
        }
    }

    override suspend fun addChat(roomId: Int, message: String) {
        sessionService.getToken()?.let { auth ->
            loggedInUserDao.getLoggedInUser1()?.userId?.let {
                val chat = ChatEntity(
                    uuid = UUID.randomUUID().toString(),
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

                val result = apiChat.addChat(auth = auth, roomId = roomId, message = message)
                chatDao.delete(chat.uuid)
                chatDao.addChat(result.toChatEntity())
            }
        }
    }

    override suspend fun removeAll() {
        chatDao.deleteAllChatRoom()
        chatDao.deleteAllParticipants()
        chatDao.deleteAllChat()
    }

    override fun getContents(roomId: Int): Flow<List<ChatEntityWithUser>> {
        return chatDao.getContents(roomId)
    }
}

fun ChatRoomApiModel.toChatRoomEntity(): ChatRoomEntity = ChatRoomEntity(
    roomId = roomId,
    createDate = createDate,
)

fun ChatApiModel.toChatEntity(): ChatEntity = ChatEntity(
    roomId = roomId,
    createDate = createDate,
    message = message,
    userId = userId,
    uuid = uuid,
    sending = false // 전송 완료
)