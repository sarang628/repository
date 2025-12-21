package com.sarang.torang.di.repository

import android.util.Log
import androidx.room.Transaction
import com.google.gson.Gson
import com.sarang.torang.api.ApiReview
import com.sarang.torang.api.feed.ApiFeed
import com.sarang.torang.api.feed.ApiFeedV1
import com.sarang.torang.api.handle
import com.sarang.torang.core.database.dao.FavoriteDao
import com.sarang.torang.core.database.dao.FeedDao
import com.sarang.torang.core.database.dao.FeedGridDao
import com.sarang.torang.core.database.dao.LikeDao
import com.sarang.torang.core.database.dao.LoggedInUserDao
import com.sarang.torang.core.database.dao.MainFeedDao
import com.sarang.torang.core.database.dao.MyFeedDao
import com.sarang.torang.core.database.dao.PictureDao
import com.sarang.torang.core.database.dao.ReviewImageDao
import com.sarang.torang.core.database.dao.UserDao
import com.sarang.torang.core.database.model.favorite.FavoriteAndImageEntity
import com.sarang.torang.core.database.model.favorite.FavoriteEntity
import com.sarang.torang.core.database.model.feed.FeedGridEntity
import com.sarang.torang.core.database.model.feed.MainFeedEntity
import com.sarang.torang.core.database.model.feed.ReviewAndImageEntity
import com.sarang.torang.core.database.model.image.ReviewImageEntity
import com.sarang.torang.core.database.model.like.LikeAndImageEntity
import com.sarang.torang.core.database.model.like.LikeEntity
import com.sarang.torang.data.FavoriteAndImage
import com.sarang.torang.data.LikeAndImage
import com.sarang.torang.data.ReviewAndImage
import com.sarang.torang.data.ReviewImage
import com.sarang.torang.data.remote.response.FavoriteFeedApiModel
import com.sarang.torang.data.remote.response.FeedApiModel
import com.sarang.torang.di.torang_database_di.toFavoriteEntity
import com.sarang.torang.di.torang_database_di.toFeedEntity
import com.sarang.torang.di.torang_database_di.toLikeEntity
import com.sarang.torang.di.torang_database_di.toReviewImage
import com.sarang.torang.di.torang_database_di.toUserEntity
import com.sarang.torang.repository.feed.FeedRepository
import com.sarang.torang.session.SessionClientService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val apiFeed                 : ApiFeed,
    private val feedDao                 : FeedDao,
    private val userDao                 : UserDao,
    private val likeDao                 : LikeDao,
    private val pictureDao              : PictureDao,
    private val favoriteDao             : FavoriteDao,
    private val sessionClientService    : SessionClientService,
    private val feedGridDao             : FeedGridDao
) : FeedRepository {
    private val tag: String = "__FeedRepositoryImpl"
    override suspend    fun findById(reviewId: Int): ReviewAndImage {
        try {
            val result: FeedApiModel =
                apiFeed.getFeedByReviewId(sessionClientService.getToken(), reviewId)
            insertFeed(listOf(result))
        } catch (e: UnknownHostException) {
            Log.e(tag, e.message.toString())
            throw Exception("서버에 접속할 수 없습니다.")
        } catch (e: Exception) {
            throw Exception(e.handle())
        }

        val reviewAndImageEntity = feedDao.find(reviewId) ?: throw Exception("리뷰를 찾을 수 없습니다.")

        return ReviewAndImage.from(reviewAndImageEntity)
    }
    override suspend    fun findAllUserFeedById(reviewId: Int) {
        val feedList = apiFeed.loadUserAllFeedsByReviewId(sessionClientService.getToken(), reviewId)
        try {
            insertFeed(feedList)
        } catch (e: Exception) {
            Log.e("__FeedRepositoryImpl", e.toString())
            Log.e(
                "__FeedRepositoryImpl",
                Gson().newBuilder().setPrettyPrinting().create().toJson(feedList)
            )
            throw Exception("피드를 가져오는데 실패하였습니다.")
        }
    }
    override suspend    fun findByPictureId(pictureId: Int) {
        TODO("Not yet implemented")
    }
    override suspend    fun deleteById(reviewId: Int) {
        //원격 저장소 요청
        apiFeed.deleteReview(reviewId)
        //로컬 저장소에서 삭제
        feedDao.deleteByReviewId(reviewId)
    }
    private  suspend    fun insertFeed(feedList: List<FeedApiModel>) {
        feedDao.insertAllFeed(userDao         = userDao,
                              likeDao         = likeDao,
                              pictureDao      = pictureDao,
                              favoriteDao     = favoriteDao,
                              feedList        = feedList.map { it.toFeedEntity() },
                              reviewImages    = feedList.map { it.pictures }.flatMap { it }.map { it.toReviewImage() },
                              userList        = feedList.map { it.toUserEntity() },
                              likeList        = feedList.filter { it.like != null }.map { it.like!!.toLikeEntity() },
                              favorites       = feedList.filter { it.favorite != null }.map { it.favorite!!.toFavoriteEntity() })
    }

    @Transaction
    override suspend fun deleteAll() {
        feedDao.deleteAll()
        likeDao.deleteAll()
        favoriteDao.deleteAll()
        pictureDao.deleteAll()
        feedGridDao.deleteAll()
    }
}