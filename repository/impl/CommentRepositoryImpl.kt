package com.sarang.torang.di.repository.repository.impl

import com.sarang.torang.api.ApiComment
import com.sarang.torang.data.RemoteComment
import com.sarang.torang.data.RemoteCommentList
import com.sarang.torang.data.dao.CommentDao
import com.sarang.torang.data.entity.CommentEntity
import com.sarang.torang.data.entity.toCommentEntityList
import com.sarang.torang.repository.CommentRepository
import com.sarang.torang.session.SessionClientService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    val apiComment: ApiComment,
    val commentDao: CommentDao,
    val sessionClientService: SessionClientService
) : CommentRepository {

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

    override suspend fun getComment(reviewId: Int): RemoteCommentList {
        val token = sessionClientService.getToken()
        if (token != null) {
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

    override suspend fun addComment(reviewId: Int, comment: String): RemoteComment {
        sessionClientService.getToken()?.let {
            return apiComment.addComment(it, reviewId, comment)
        }
        throw Exception("로그인을 해주세요")
    }

    override suspend fun addReply(
        reviewId: Int,
        comment: String,
        parentCommentId: Int
    ): RemoteComment {
        sessionClientService.getToken()?.let {
            return apiComment.addComment(
                auth = it,
                review_id = reviewId,
                comment = comment,
                parentCommentId = parentCommentId
            )
        }
        throw Exception("로그인을 해주세요")
    }

    override suspend fun getCommentsWithOneReply(reviewId: Int): RemoteCommentList {
        val token = sessionClientService.getToken()
        if (token != null) {
            val result = apiComment.getCommentsWithOneReply(token, reviewId)
            commentDao.insertComments(result.list.toCommentEntityList())
            return result
        } else {
            throw Exception("로그인을 해주세요")
        }
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