package com.sarang.torang.di.repository.repository.impl

import android.util.Log
import com.sarang.torang.api.ApiComment
import com.sarang.torang.data.remote.response.RemoteComment
import com.sarang.torang.data.remote.response.CommentListApiModel
import com.sarang.torang.data.dao.CommentDao
import com.sarang.torang.data.dao.LoggedInUserDao
import com.sarang.torang.data.entity.CommentEntity
import com.sarang.torang.data.entity.toCommentEntity
import com.sarang.torang.data.entity.toCommentEntityList
import com.sarang.torang.repository.comment.CommentRepository
import com.sarang.torang.session.SessionClientService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    val apiComment: ApiComment,
    val commentDao: CommentDao,
    val sessionClientService: SessionClientService,
    val loggedInUserDao: LoggedInUserDao
) : CommentRepository {

    val TAG = "__CommentRepositoryImpl"

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCommentsFlow(reviewId: Int): Flow<List<CommentEntity>> {
        return commentDao.getComments(reviewId).flatMapMerge {
            requestFlow(it)
        }
    }

    private fun requestFlow(list: List<CommentEntity>): Flow<List<CommentEntity>> = flow {
        val temp = mutableListOf<CommentEntity>().apply {
            for (comment in list) {
                add(comment)
                addAll(commentDao.getReply(comment.commentId))
            }
        }
        emit(temp)
    }

    override suspend fun clear() {
        commentDao.clear()
    }

    override suspend fun loadMoreReply(commentId: Int) {
        val token = sessionClientService.getToken()
        if (token != null) {
            val result = apiComment.getSubComments(token, commentId)
            commentDao.insertComments(result.toCommentEntityList())
        } else {
            throw Exception("로그인을 해주세요")
        }
    }

    override suspend fun getComment(reviewId: Int): CommentListApiModel {
        val token = sessionClientService.getToken()
        if (token != null) {
            commentDao.clear()
            val result = apiComment.getComments(token, reviewId)
            commentDao.insertComments(result.list.toCommentEntityList())
            return result
        } else {
            throw Exception("로그인을 해주세요")
        }
    }

    override suspend fun getSubComment(parentCommentId: Int): List<RemoteComment> {
        val token = sessionClientService.getToken()
        if (token != null) {
            return apiComment.getSubComments(token, parentCommentId)
        } else {
            throw Exception("로그인을 해주세요")
        }
    }

    override suspend fun deleteComment(commentId: Int) {
        apiComment.deleteComment(commentId = commentId)
    }

    override suspend fun addComment(reviewId: Int, comment: String, onLocalUpdated: () -> Unit) {
        //로그인 토큰 가져오기
        sessionClientService.getToken().let {
            val user = loggedInUserDao.getLoggedInUser1()
            if (it == null || user == null) { // 토큰이 없거나 로그인한 사용자 정보가 없다면
                throw Exception("로그인 해주세요")
            }
            val commentEntity = CommentEntity(
                userName = user.userName,
                comment = comment,
                reviewId = reviewId,
                userId = user.userId,
                profilePicUrl = loggedInUserDao.getLoggedInUser1()?.profilePicUrl ?: "",
                parentCommentId = 0,
                isUploading = true
            )
            Log.d(
                "__CommentRepositoryImpl",
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())
            )
            commentDao.insertComment(commentEntity)
            onLocalUpdated.invoke()
            delay(1000)
            val result = apiComment.addComment(it, reviewId, comment)
            commentDao.update(updateId = commentEntity.commentId, result.toCommentEntity())
        }
    }

    override suspend fun addReply(
        reviewId: Int, comment: String, parentCommentId: Int, onLocalUpdated: () -> Unit
    ) {
        sessionClientService.getToken()?.let {
            val user = loggedInUserDao.getLoggedInUser1()
            if (it == null || user == null) { // 토큰이 없거나 로그인한 사용자 정보가 없다면
                throw Exception("로그인 해주세요")
            }

            val commentEntity = CommentEntity(
                userId = user.userId,
                comment = comment,
                profilePicUrl = user.profilePicUrl ?: "",
                reviewId = reviewId,
                userName = user.userName,
                parentCommentId = parentCommentId,
                isUploading = true
            )

            //DB insert
            commentDao.insertComment(commentEntity)
            onLocalUpdated.invoke()
            delay(1000)
            //API
            val result = apiComment.addComment(
                auth = it,
                review_id = reviewId,
                comment = comment,
                parentCommentId = parentCommentId
            )
            //DB update
            commentDao.update(
                updateId = commentEntity.commentId, result.toCommentEntity()
            )
        }
    }

    override suspend fun getCommentsWithOneReply(reviewId: Int): CommentListApiModel {
        val result =
            apiComment.getCommentsWithOneReply(sessionClientService.getToken() ?: "", reviewId)

        Log.d(
            TAG,
            "getCommentsWithOneReply. reviewId: ${reviewId}, result size: ${result.list.size}"
        )

        commentDao.insertComments(result.list.toCommentEntityList())
        return result
    }

    override suspend fun getSubComments(commentId: Int): List<RemoteComment> {
        val token = sessionClientService.getToken()
        if (token != null) {
            return apiComment.getSubComments(token, commentId)
        } else {
            throw Exception("로그인을 해주세요")
        }
    }
}