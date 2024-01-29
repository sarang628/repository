package com.sarang.torang.di.repository.repository.impl

import com.sarang.torang.api.ApiComment
import com.sarang.torang.data.RemoteComment
import com.sarang.torang.data.RemoteCommentList
import com.sarang.torang.repository.CommentRepository
import com.sarang.torang.session.SessionClientService
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    val apiComment: ApiComment,
    val sessionClientService: SessionClientService
) : CommentRepository {
    override suspend fun getComment(reviewId: Int): RemoteCommentList {
        return apiComment.getComments(sessionClientService.getToken()!!, reviewId)
    }

    override suspend fun deleteComment(commentId: Int) {
        apiComment.deleteComment(commentId = commentId)
    }

    override suspend fun addComment(reviewId: Int, comment: String): RemoteComment {
        sessionClientService.getToken()?.let {
            return apiComment.addComment(it, reviewId, comment)
        }
        throw Exception("token is empty")
    }

}