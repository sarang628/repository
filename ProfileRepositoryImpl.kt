package com.sarang.torang.di.repository

import android.util.Log
import androidx.room.Transaction
import com.google.gson.Gson
import com.sarang.torang.api.feed.ApiFeed
import com.sarang.torang.api.ApiProfile
import com.sarang.torang.api.ApiReview
import com.sarang.torang.api.handle
import com.sarang.torang.core.database.dao.FavoriteDao
import com.sarang.torang.core.database.dao.LikeDao
import com.sarang.torang.core.database.dao.MyFeedDao
import com.sarang.torang.core.database.dao.PictureDao
import com.sarang.torang.core.database.dao.UserDao
import com.sarang.torang.core.database.model.favorite.FavoriteEntity
import com.sarang.torang.core.database.model.feed.MyFeedEntity
import com.sarang.torang.core.database.model.feed.ReviewAndImageEntity
import com.sarang.torang.core.database.model.image.ReviewImageEntity
import com.sarang.torang.data.remote.response.FeedApiModel
import com.sarang.torang.data.remote.response.UserApiModel
import com.sarang.torang.di.torang_database_di.toFavoriteEntity
import com.sarang.torang.di.torang_database_di.toLikeEntity
import com.sarang.torang.di.torang_database_di.toUserEntity
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
    private val sessionClientService: SessionClientService,
) : ProfileRepository {

    override suspend fun loadProfile(userId: Int): UserApiModel {

        if (sessionClientService.getToken() != null) {
            try {
                return apiProfile.getProfileWithFollow(sessionClientService.getToken()!!, userId)
            } catch (e: HttpException) {
                throw Exception(e.handle())
            }
        } else {
            return apiProfile.getProfile("$userId")
        }
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


fun FeedApiModel.toMyFeedEntity(): MyFeedEntity {
    return MyFeedEntity(
        reviewId = reviewId,
        userId = user.userId,
        contents = contents,
        rating = rating,
        userName = user.userName,
        likeAmount = like_amount,
        commentAmount = comment_amount,
        restaurantName = restaurant.restaurantName,
        restaurantId = restaurant.restaurantId,
        createDate = this.create_date,
        profilePicUrl = this.user.profilePicUrl
    )
}
