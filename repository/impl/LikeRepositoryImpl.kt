package com.sarang.torang.di.repository.repository.impl

import com.sarang.torang.api.ApiLike
import com.sarang.torang.data.remote.response.FollowerApiModel
import com.sarang.torang.exception.NotLoggedInException
import com.sarang.torang.repository.LikeRepository
import com.sarang.torang.session.SessionService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LikeRepositoryImpl @Inject constructor(
    val apiLike: ApiLike,
    val session: SessionService
) : LikeRepository {
    override suspend fun getLikeUserFromReview(reviewId: Int): List<FollowerApiModel> {
        val token = session.getToken()
        if (token != null) {
            return apiLike.getLikeUserByReviewId(token, reviewId.toString())
        } else {
            // 예외 처리 또는 fallback
            throw NotLoggedInException("로그인 정보가 없습니다.")
        }
    }
}