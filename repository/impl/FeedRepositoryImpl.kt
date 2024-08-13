package com.sarang.torang.di.repository.repository.impl

import android.util.Log
import androidx.room.Transaction
import com.google.gson.Gson
import com.sarang.torang.api.ApiComment
import com.sarang.torang.api.ApiFeed
import com.sarang.torang.data.remote.response.LikeApiModel
import com.sarang.torang.data.dao.FavoriteDao
import com.sarang.torang.data.dao.FeedDao
import com.sarang.torang.data.dao.LikeDao
import com.sarang.torang.data.dao.MyFeedDao
import com.sarang.torang.data.dao.PictureDao
import com.sarang.torang.data.dao.UserDao
import com.sarang.torang.data.entity.FavoriteEntity
import com.sarang.torang.data.entity.FeedEntity
import com.sarang.torang.data.entity.LikeEntity
import com.sarang.torang.data.entity.ReviewAndImageEntity
import com.sarang.torang.data.entity.UserEntity
import com.sarang.torang.data.remote.response.FavoriteApiModel
import com.sarang.torang.data.remote.response.FeedApiModel
import com.sarang.torang.data.remote.response.toReviewImage
import com.sarang.torang.repository.FeedRepository
import com.sarang.torang.session.SessionClientService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val apiFeed: ApiFeed,
    private val feedDao: FeedDao,
    private val myFeedDao: MyFeedDao,
    private val pictureDao: PictureDao,
    private val userDao: UserDao,
    private val likeDao: LikeDao,
    private val favoriteDao: FavoriteDao,
    private val apiComment: ApiComment,
    private val sessionClientService: SessionClientService
) : FeedRepository {
    override val feeds: Flow<List<ReviewAndImageEntity>> = feedDao.getAllFeedWithUser()
    override fun getMyFeed(reviewId: Int): Flow<List<ReviewAndImageEntity>> {
        return myFeedDao.getMyFeedByReviewId(reviewId)
    }

    override suspend fun getFeedByReviewId(reviewId : Int) : ReviewAndImageEntity{
        return feedDao.getFeed(reviewId)
    }

    override fun getFeedByRestaurantId(restaurantId: Int): Flow<List<ReviewAndImageEntity>> {
        return feedDao.getFeedByRestaurantId(restaurantId)
    }

    override suspend fun deleteFeed(reviewId: Int) {
        //원격 저장소 요청
        apiFeed.deleteReview(reviewId)
        //로컬 저장소에서 삭제
        feedDao.deleteFeed(reviewId)
    }

    @Transaction
    override suspend fun deleteFeedAll() {
        feedDao.deleteAll()
        likeDao.deleteAll()
        favoriteDao.deleteAll()
        pictureDao.deleteAll()
    }

    override suspend fun loadFeed() {
        val feedList = apiFeed.getFeeds(sessionClientService.getToken())
        try {
            deleteFeedAll()
            feedDao.insertAll(feedList.map { it.toFeedEntity() })

            val list = feedList
                .map { it.pictures }
                .flatMap { it }
                .map { it.toReviewImage() }

            feedDao.insertAllFeed(
                feedList = feedList.map { it.toFeedEntity() },
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
            Log.e("__FeedRepositoryImpl", e.toString())
            Log.e(
                "__FeedRepositoryImpl",
                Gson().newBuilder().setPrettyPrinting().create().toJson(feedList)
            )
            throw Exception("피드를 가져오는데 실패하였습니다.")
        }
    }

    override suspend fun loadFeedWithPage(page: Int) {
        val feedList = apiFeed.getFeedsWithPage(sessionClientService.getToken(), page)
        try {
            if(page == 0)
                deleteFeedAll()

            feedDao.insertAll(feedList.map { it.toFeedEntity() })

            val list = feedList
                .map { it.pictures }
                .flatMap { it }
                .map { it.toReviewImage() }

            feedDao.insertAllFeed(
                feedList = feedList.map { it.toFeedEntity() },
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
            Log.e("__FeedRepositoryImpl", e.toString())
            Log.e(
                "__FeedRepositoryImpl",
                Gson().newBuilder().setPrettyPrinting().create().toJson(feedList)
            )
            throw Exception("피드를 가져오는데 실패하였습니다.")
        }
    }

    override suspend fun addLike(reviewId: Int) {
        val token = sessionClientService.getToken()
        if (token != null) {
            val result = apiFeed.addLike(token, reviewId)
//            val result = apiFeed.addLike(it, reviewId)
            likeDao.insertLike(result.toLikeEntity())
        } else {
            throw java.lang.Exception("로그인을 해주세요.")
        }
    }

    override suspend fun deleteLike(reviewId: Int) {
        val like = likeDao.getLike1(reviewId = reviewId)
        val remoteLike = apiFeed.deleteLike(like.likeId)
        likeDao.deleteLike(
            remoteLike.toLikeEntity()
        )
    }

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

fun FeedApiModel.toUserEntity(): UserEntity {
    return UserEntity(
        userId = this.user.userId,
        email = this.user.email ?: "",
        loginPlatform = this.user.loginPlatform ?: "",
        createDate = this.user.createDate ?: "",
        accessToken = "",
        profilePicUrl = this.user.profilePicUrl,
        point = 0,
        reviewCount = "0",
        following = "0",
        followers = "0",
        userName = this.user.userName
    )
}

fun FeedApiModel.toFeedEntity(): FeedEntity {
    return FeedEntity(
        reviewId = this.reviewId,
        userId = this.user.userId,
        rating = this.rating,
        userName = this.user.userName,
        profilePicUrl = this.user.profilePicUrl,
        likeAmount = this.like_amount,
        commentAmount = this.comment_amount,
        restaurantName = this.restaurant.restaurantName,
        restaurantId = this.restaurant.restaurantId,
        contents = this.contents,
        createDate = this.create_date
    )
}

fun FavoriteApiModel.toFavoriteEntity(): FavoriteEntity {
    return FavoriteEntity(
        reviewId = this.review_id,
        favoriteId = this.favorite_id,
        userId = this.user_id,
        createDate = this.create_date
    )
}

fun LikeApiModel.toLikeEntity(): LikeEntity {
    return LikeEntity(
        likeId = likeId,
        userId = userId,
        createDate = createDate,
        reviewId = reviewId
    )
}