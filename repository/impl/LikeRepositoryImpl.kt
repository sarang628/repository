package com.sarang.torang.di.repository.repository.impl

import com.sarang.torang.api.ApiLike
import com.sarang.torang.api.feed.ApiFeed
import com.sarang.torang.core.database.dao.FeedDao
import com.sarang.torang.core.database.dao.LikeDao
import com.sarang.torang.data.remote.response.FollowerApiModel
import com.sarang.torang.exception.NotLoggedInException
import com.sarang.torang.repository.LikeRepository
import com.sarang.torang.session.SessionClientService
import com.sarang.torang.session.SessionService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LikeRepositoryImpl @Inject constructor(
    val apiLike: ApiLike,
    val session: SessionService,
    val likeDao: LikeDao,
    val feedDao: FeedDao,
    val apiFeed : ApiFeed,
    val sessionClientService: SessionClientService
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

    override suspend fun addLike(reviewId: Int) {
        val token = sessionClientService.getToken()
        if (token != null) {
            val result = apiLike.addLike(token, reviewId)
            likeDao.insertLike(result.toLikeEntity())
            feedDao.addLikeCount(reviewId)
        } else {
            throw java.lang.Exception("로그인을 해주세요.")
        }
    }

    override suspend fun deleteLike(reviewId: Int) {
        val like = likeDao.getLike1(reviewId = reviewId)
        val remoteLike = apiLike.deleteLike(like.likeId)
        likeDao.deleteLike(
            remoteLike.toLikeEntity()
        )
        feedDao.subTractLikeCount(reviewId)
    }
}