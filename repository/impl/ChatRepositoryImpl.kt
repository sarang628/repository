package com.sarang.torang.di.repository.repository.impl

import android.content.Context
import com.sarang.torang.api.ApiChat
import com.sarang.torang.data.dao.ChatDao
import com.sarang.torang.data.entity.ChatEntity
import com.sarang.torang.data.entity.ChatRoomEntity
import com.sarang.torang.data.remote.response.ChatApiModel
import com.sarang.torang.data.remote.response.ChatRoomApiModel
import com.sarang.torang.repository.ChatRepository
import com.sarang.torang.session.SessionService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val apiChat: ApiChat,
    private val chatDao: ChatDao,
    private val sessionService: SessionService,
) :
    ChatRepository {
    override suspend fun loadChatRoom() {
        sessionService.getToken()?.let {
            val result = apiChat.getChatRoom(it)
            chatDao.addAll(result.map { it.toChatRoomEntity() })
        }
    }

    override suspend fun loadContents(roomId: Int) {
        sessionService.getToken()?.let {
            val result = apiChat.getContents(it, roomId)
            chatDao.addAllChat(result.map { it.toChatEntity() })
        }
    }

    override fun getChatRoom(): Flow<List<ChatRoomEntity>> {
        return chatDao.getChatRoom()
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