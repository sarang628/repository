package com.sarang.torang.di.repository.repository.impl

import android.util.Log
import androidx.room.Transaction
import com.google.gson.Gson
import com.sarang.torang.api.ApiFeed
import com.sarang.torang.api.ApiProfile
import com.sarang.torang.api.ApiReview
import com.sarang.torang.api.handle
import com.sarang.torang.data.dao.FavoriteDao
import com.sarang.torang.data.dao.LikeDao
import com.sarang.torang.data.dao.MyFeedDao
import com.sarang.torang.data.dao.PictureDao
import com.sarang.torang.data.dao.UserDao
import com.sarang.torang.data.entity.FavoriteEntity
import com.sarang.torang.data.entity.ReviewAndImageEntity
import com.sarang.torang.data.entity.ReviewImageEntity
import com.sarang.torang.data.entity.toMyFeedEntity
import com.sarang.torang.data.remote.response.UserApiModel
import com.sarang.torang.data.remote.response.toReviewImage
import com.sarang.torang.repository.ProfileRepository
import com.sarang.torang.session.SessionClientService
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val apiProfile: ApiProfile,
    private val apiFeed: ApiFeed,
    private val apiReview: ApiReview,
    private val myFeedDao: MyFeedDao,
    private val pictureDao: PictureDao,
    private val likeDao: LikeDao,
    private val userDao: UserDao,
    private val favoriteDao: FavoriteDao,
    private val sessionClientService: SessionClientService
) : ProfileRepository {

    override suspend fun loadProfile(userId: Int): UserApiModel {
        sessionClientService.getToken()?.let {
            try {
                return apiProfile.getProfileWithFollow(it, userId)
            } catch (e: HttpException) {
                throw Exception(e.handle())
            }
        }
        throw Exception("로그인 상태가 아닙니다.")
    }

    override suspend fun loadProfileByToken(): UserApiModel {
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
        return myFeedDao.getMyFeed(userId)
    }

    override fun getMyFavorite(userId: Int): Flow<List<ReviewAndImageEntity>> {
        return favoriteDao.getMyFavorite(userId)
    }

    @Transaction
    suspend fun deleteFeedAll() {
    }

    override suspend fun loadMyFeed(userId: Int) {
        val feedList = apiReview.getMyReviewsByUserId(userId)
        try {
            deleteFeedAll()
            myFeedDao.insertAll(feedList.map { it.toMyFeedEntity() })

            val list = feedList
                .map { it.pictures }
                .flatMap { it }
                .map { it.toReviewImage() }

            myFeedDao.insertAllFeed(
                feedList = feedList.map { it.toMyFeedEntity() },
                userDao = userDao,
                pictureDao = pictureDao,
                reviewImages = list,
                userList = feedList.map { it.toUserEntity() },
                likeDao = likeDao,
                likeList = feedList.filter { it.like != null }.map { it.like!!.toLikeEntity() },
                favoriteDao = favoriteDao,
                favorites = feedList.filter { it.favorite != null }
                    .map { it.favorite!!.toFavoriteEntity() }
            )
        } catch (e: Exception) {
            Log.e("FeedRepositoryImpl", e.toString())
            Log.e(
                "FeedRepositoryImpl",
                Gson().newBuilder().setPrettyPrinting().create().toJson(feedList)
            )
            throw Exception("피드를 가져오는데 실패하였습니다.")
        }
    }

    override fun getFavorite(reviewId: Int): Flow<FavoriteEntity> {
        return favoriteDao.getFavorite(reviewId)
    }

    override fun getReviewImages(reviewId: Int): Flow<List<ReviewImageEntity>> {
        return myFeedDao.getReviewImages(reviewId)
    }
}