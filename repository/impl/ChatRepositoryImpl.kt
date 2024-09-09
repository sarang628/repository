package com.sarang.torang.di.repository.repository.impl

import com.sarang.torang.api.ApiChat
import com.sarang.torang.data.dao.ChatDao
import com.sarang.torang.data.dao.UserDao
import com.sarang.torang.data.entity.ChatEntity
import com.sarang.torang.data.entity.ChatParticipantsEntity
import com.sarang.torang.data.entity.ChatRoomEntity
import com.sarang.torang.data.entity.ChatRoomWithParticipantsAndUsers
import com.sarang.torang.data.entity.ChatRoomWithParticipantsEntity
import com.sarang.torang.data.entity.UserEntity
import com.sarang.torang.data.remote.response.ChatApiModel
import com.sarang.torang.data.remote.response.ChatRoomApiModel
import com.sarang.torang.repository.ChatRepository
import com.sarang.torang.session.SessionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val apiChat: ApiChat,
    private val chatDao: ChatDao,
    private val sessionService: SessionService,
    private val userDao: UserDao,
) :
    ChatRepository {
    override suspend fun loadChatRoom() {
        sessionService.getToken()?.let {
            val result = apiChat.getChatRoom(it)
            chatDao.addAll(result.map { it.toChatRoomEntity() })
            result.forEach { chatRoom ->

                chatRoom.users.forEach { user ->
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

                chatDao.insertParticipats(
                    chatRoom.users.map { user ->
                        ChatParticipantsEntity(
                            roomId = chatRoom.roomId,
                            userId = user.userId
                        )
                    }
                )
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

    override fun getChatRoomsWithParticipantsAndUsers(): Flow<List<ChatRoomWithParticipantsAndUsers>> {
        // 첫 번째 Flow: ChatRoomEntity 목록 가져오기
        val chatRoomFlow = chatDao.getChatRoom()

        // 두 번째 Flow: 각 ChatRoomEntity에 대응하는 ParticipantsWithUserEntity 목록 가져오기
        return chatRoomFlow.flatMapLatest { chatRooms ->
            // 각 채팅방에 대한 Participants 정보를 가져와서 결합
            val flows = chatRooms.map { chatRoom ->
                chatDao.getParticipantsWithUsers(chatRoom.chatRoomEntity.roomId)
                    .map { participantsWithUsers ->
                        ChatRoomWithParticipantsAndUsers(
                            chatRoomEntity = chatRoom.chatRoomEntity,
                            participantsWithUsers = participantsWithUsers
                        )
                    }
            }
            // 여러 Flow를 결합하여 결과를 반환
            combine(flows) { it.toList() }
        }
    }

    override fun getContents(roomId: Int): Flow<List<ChatEntity>> {
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
)