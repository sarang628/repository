package com.sarang.torang.di.repository.repository.impl

import com.sarang.torang.api.ApiLike
import com.sarang.torang.api.feed.ApiFeed
import com.sarang.torang.core.database.dao.FavoriteDao
import com.sarang.torang.core.database.dao.FeedDao
import com.sarang.torang.core.database.dao.LikeDao
import com.sarang.torang.data.remote.response.FollowerApiModel
import com.sarang.torang.exception.NotLoggedInException
import com.sarang.torang.repository.FavoriteRepository
import com.sarang.torang.repository.LikeRepository
import com.sarang.torang.session.SessionClientService
import com.sarang.torang.session.SessionService
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    val session: SessionService,
    val feedDao: FeedDao,
    val apiFeed : ApiFeed,
    val favoriteDao: FavoriteDao,
    val sessionClientService: SessionClientService
) : FavoriteRepository {
    override suspend fun addFavorite(reviewId: Int) {
        val token = sessionClientService.getToken()
        if (token != null) {
            val result = apiFeed.addFavorite(token, reviewId)
            favoriteDao.insertFavorite(result.toFavoriteEntity())
        } else {
            throw Exception("로그인을 해주세요.")
        }
    }

    override suspend fun deleteFavorite(reviewId: Int) {
        val token = sessionClientService.getToken()
        if (token != null) {
            val favorite = favoriteDao.getFavorite1(reviewId = reviewId)
            val remoteFavorite = apiFeed.deleteFavorite(favorite.favoriteId)
            favoriteDao.deleteFavorite(
                remoteFavorite.toFavoriteEntity()
            )
        } else {
            throw Exception("로그인을 해주세요.")
        }
    }
}