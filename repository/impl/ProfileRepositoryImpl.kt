package com.sarang.torang.di.repository.repository.impl

import com.sarang.torang.api.ApiProfile
import com.sarang.torang.api.handle
import com.sarang.torang.data.dao.FavoriteDao
import com.sarang.torang.data.dao.FeedDao
import com.sarang.torang.data.dao.LikeDao
import com.sarang.torang.data.entity.FavoriteEntity
import com.sarang.torang.data.entity.LikeEntity
import com.sarang.torang.data.entity.ReviewAndImageEntity
import com.sarang.torang.data.entity.ReviewImageEntity
import com.sarang.torang.data.remote.response.RemoteUser
import com.sarang.torang.repository.ProfileRepository
import com.sarang.torang.session.SessionClientService
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val apiProfile: ApiProfile,
    private val feedDao: FeedDao,
    private val likeDao: LikeDao,
    private val favoriteDao: FavoriteDao,
    private val sessionClientService: SessionClientService
) : ProfileRepository {

    override suspend fun loadProfile(userId: Int): RemoteUser {
        sessionClientService.getToken()?.let {
            try {
                return apiProfile.getProfileWithFollow(it, userId)
            } catch (e: HttpException) {
                throw Exception(e.handle())
            }
        }
        throw Exception("로그인 상태가 아닙니다.")
    }

    override suspend fun loadProfileByToken(): RemoteUser {
        sessionClientService.getToken()?.let {
            try {
                return apiProfile.getProfileByToken(it)
            } catch (e: HttpException) {
                throw Exception(e.handle())
            }
        }
        throw Exception("로그인 상태가 아닙니다.")
    }

    override fun getMyFeed(userId: Int): Flow<List<ReviewAndImageEntity>> {
        return feedDao.getMyFeed(userId)
    }

    override fun getMyFavorite(userId: Int): Flow<List<ReviewAndImageEntity>> {
        return favoriteDao.getMyFavorite(userId)
    }

    override suspend fun loadFeed() {

    }

    override suspend fun like(reviewId: Int) {
        //userDao.insertLike(reviewId)
    }

    override suspend fun favorite(reviewId: Int) {
        TODO("Not yet implemented")
    }

    override fun getLike(reviewId: Int): Flow<LikeEntity> {
        return likeDao.getLike(reviewId)
    }

    override fun getFavorite(reviewId: Int): Flow<FavoriteEntity> {
        return favoriteDao.getFavorite(reviewId)
    }

    override suspend fun isLogin(): Boolean {
        return true
    }

    override fun getReviewImages(reviewId: Int): Flow<List<ReviewImageEntity>> {
        return feedDao.getReviewImages(reviewId)
    }
}