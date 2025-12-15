package com.sarang.torang.di.repository

import com.sarang.torang.api.feed.ApiFeed
import com.sarang.torang.core.database.dao.FavoriteDao
import com.sarang.torang.core.database.dao.FeedDao
import com.sarang.torang.core.database.model.favorite.FavoriteEntity
import com.sarang.torang.data.Favorite
import com.sarang.torang.di.torang_database_di.toFavoriteEntity
import com.sarang.torang.repository.FavoriteRepository
import com.sarang.torang.session.SessionClientService
import com.sarang.torang.session.SessionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
            favoriteDao.add(result.toFavoriteEntity())
        } else {
            throw Exception("로그인을 해주세요.")
        }
    }

    override suspend fun deleteFavorite(reviewId: Int) {
        val token = sessionClientService.getToken()
        if (token != null) {
            val favorite = favoriteDao.findByReviewId(reviewId = reviewId)
            val remoteFavorite = apiFeed.deleteFavorite(favorite.favoriteId)
            favoriteDao.delete(
                favoriteId = remoteFavorite.favorite_id
            )
        } else {
            throw Exception("로그인을 해주세요.")
        }
    }

    override fun findByReviewIdFlow(reviewId: Int): Flow<Favorite> {
        return favoriteDao.findByReviewIdFlow(reviewId)
                          .map { Favorite.from(it)!! }
    }
}