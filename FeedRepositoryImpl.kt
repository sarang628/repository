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
import com.sarang.torang.core.database.dao.LikeDao
import com.sarang.torang.core.database.dao.LoggedInUserDao
import com.sarang.torang.core.database.dao.MainFeedDao
import com.sarang.torang.core.database.dao.MyFeedDao
import com.sarang.torang.core.database.dao.PictureDao
import com.sarang.torang.core.database.dao.ReviewImageDao
import com.sarang.torang.core.database.dao.UserDao
import com.sarang.torang.core.database.model.favorite.FavoriteAndImageEntity
import com.sarang.torang.core.database.model.favorite.FavoriteEntity
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
import com.sarang.torang.repository.FeedRepository
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
    private val apiFeedV1               : ApiFeedV1,
    private val apiReview               : ApiReview,
    private val feedDao                 : FeedDao,
    private val userDao                 : UserDao,
    private val likeDao                 : LikeDao,
    private val myFeedDao               : MyFeedDao,
    private val pictureDao              : PictureDao,
    private val mainFeedDao             : MainFeedDao,
    private val reviewImageDao          : ReviewImageDao,
    private val favoriteDao             : FavoriteDao,
    private val loggedInUserDao         : LoggedInUserDao,
    private val sessionClientService    : SessionClientService
) : FeedRepository {
    private val tag: String = "__FeedRepositoryImpl"
    private val loadTrigger = MutableStateFlow(false)
    @OptIn(ExperimentalCoroutinesApi::class)
    override val feeds: Flow<List<ReviewAndImage>?> = loadTrigger.flatMapLatest { shouldLoad ->
            if (shouldLoad) {
                mainFeedDao.findAllFlow().map { it.map { ReviewAndImage.from(it) } }
                //feedDao.findAllFlow()
            } else {
                flowOf(null)
            }
        }
    override            fun findRestaurantFeedsFlow(restaurantId: Int): Flow<List<ReviewAndImage>> {
        return feedDao.findAllByRestaurantIdFlow(restaurantId).map { it.map { ReviewAndImage.from(it) } }
    }
             suspend    fun initLoaded(){
        if (!loadTrigger.value)
            loadTrigger.emit(true)
    }
    override suspend    fun loadById(reviewId: Int, count: Int) {
        val feedList = apiFeed.getNextReviewsByReviewId(
            auth = sessionClientService.getToken(),
            reviewId = reviewId,
            count = count
        )
        try {
            insertFeed(feedList)
        } catch (e: Exception) {
            Log.e(
                "__FeedRepositoryImpl",
                Gson().newBuilder().setPrettyPrinting().create().toJson(feedList)
            )
            throw Exception("피드를 가져오는데 실패하였습니다.")
        }
    }
    override suspend    fun loadByPage(page: Int) {
        try {
            val feedList = apiFeed.getFeedsWithPage(sessionClientService.getToken(), page)
            // delete feed after succeed to call api
            if (page == 0) {
                deleteAll()
                mainFeedDao.deleteAll()
            }
            insertFeed(feedList)
            mainFeedDao.addAll(feedList.mapIndexed { index, model -> MainFeedEntity(reviewId = model.reviewId,
                order    = page * 20 + index)})
            initLoaded()
        }
        catch (e : ConnectException){
            throw ConnectException("피드를 가져오는데 실패하였습니다. 서버 접속에 실패 하였습니다.")
        }
        catch (e: Exception) {
            Log.e("__FeedRepositoryImpl", e.toString())
            throw Exception("피드를 가져오는데 실패하였습니다.")
        }
    }
    override suspend    fun loadByFavorite() {
        val token = sessionClientService.getToken() ?: throw Exception("로그인을 해주세요.")

        val result : List<FavoriteFeedApiModel> = apiFeedV1.findByFavorite(token)

        pictureDao.addAll(
            result.map {
                ReviewImageEntity(
                    pictureId = it.picture.pictureId,
                    pictureUrl = it.picture.pictureUrl,
                    width = it.picture.width,
                    height = it.picture.height,
                    reviewId = it.reviewId
                )
            }
        )
        favoriteDao.deleteAll()
        val favoriteEntities = result.map {
            FavoriteEntity(reviewId = it.reviewId,
                favoriteId = it.favoriteId,
                createDate = it.createDate)
        }
        favoriteDao.addAll(favoriteEntities)
    }
    override suspend    fun loadByLike() {
        val token = sessionClientService.getToken() ?: throw Exception("로그인을 해주세요.")
        val result = apiFeedV1.findByLike(auth = token)

        pictureDao.addAll(
            result.map {
                ReviewImageEntity(
                    pictureId = it.picture.pictureId,
                    pictureUrl = it.picture.pictureUrl,
                    width = it.picture.width,
                    height = it.picture.height,
                    reviewId = it.reviewId
                )
            }
        )
        likeDao.deleteAll()
        val likeEntities = result.map {
            LikeEntity(reviewId = it.reviewId,
                likeId = it.likeId,
                createDate = it.createDate)
        }
        likeDao.addAll(likeEntities)
    }
    override suspend    fun loadByRestaurantId(restaurantId: Int) {
        val result = apiFeedV1.findByRestaurantId(sessionClientService.getToken(), restaurantId)
        insertFeed(result)
    }
             suspend    fun loadFeedByRestaurantId(restaurantId: Int) {
        val feedList = apiFeedV1.findByUserAndRestaurantId(
            auth = sessionClientService.getToken()?:"",
            userId = loggedInUserDao.getLoggedInUser()?.userId ?: 0,
            restaurantId = restaurantId
        )
        insertFeed(feedList)
    }
    override suspend    fun loadByUserId(userId: Int) {
        val feedList = apiReview.getMyReviewsByUserId(userId)
        try {
            myFeedDao.addAll(feedList.map { it.toMyFeedEntity() })

            val list = feedList.map { it.pictures }
                .flatMap { it }
                .map { it.toReviewImage() }

            myFeedDao.addAll(feedList.map { it.toMyFeedEntity() })

            myFeedDao.insertAllFeed(feedList     = feedList.map { it.toMyFeedEntity() },
                userDao      = userDao,
                pictureDao   = pictureDao,
                likeDao      = likeDao,
                favoriteDao  = favoriteDao,
                reviewImages = list,
                userList     = feedList.map { it.toUserEntity() },
                likeList     = feedList.filter { it.like != null }
                    .map { it.like!!.toLikeEntity() },
                favorites    = feedList.filter { it.favorite != null }
                    .map { it.favorite!!.toFavoriteEntity() }
            )
        } catch (e: Exception) {
            Log.e("FeedRepositoryImpl", e.toString())
            Log.e("FeedRepositoryImpl",
                Gson().newBuilder().setPrettyPrinting().create().toJson(feedList))
            throw Exception("피드를 가져오는데 실패하였습니다.")
        }
    }
    override suspend    fun findById(reviewId: Int): ReviewAndImage {
        try {
            val result: FeedApiModel =
                apiFeed.getFeedByReviewId(sessionClientService.getToken(), reviewId)
            insertFeed(listOf(result))
            initLoaded()
        } catch (e: UnknownHostException) {
            Log.e(tag, e.message.toString())
            throw Exception("서버에 접속할 수 없습니다.")
        } catch (e: Exception) {
            throw Exception(e.handle())
        }

        val reviewAndImageEntity = feedDao.find(reviewId) ?: throw Exception("리뷰를 찾을 수 없습니다.")

        return ReviewAndImage.from(reviewAndImageEntity)
    }
    override            fun findMyFeedById(reviewId: Int): Flow<List<ReviewAndImage>> {
        return myFeedDao.findUserFeedsByReviewId(reviewId)
                        .map { it.map { ReviewAndImage.from(it) } }
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
    override            fun findByUserIdFlow(userId: Int): Flow<List<ReviewAndImage>> {
        return myFeedDao.findByUserId(userId)
                        .map { it.map { ReviewAndImage.from(it) } }
    }
    override            fun findByFavoriteFlow(): Flow<List<FavoriteAndImage>> {
        return feedDao.findAllByFavoriteFlow().map { it.map { FavoriteAndImage.from(it) } }
    }
    override            fun findByLikeFlow(): Flow<List<LikeAndImage>> {
        return feedDao.findAllByLikeFlow().map { it.map { LikeAndImage.from(it) } }
    }
    override suspend    fun findByPictureId(pictureId: Int) {
        TODO("Not yet implemented")
    }
    override            fun findByPictureIdFlow(pictureId: Int): Flow<ReviewAndImage?> {
        return feedDao.findByPictureIdFlow(pictureId).map {
            it?.let { ReviewAndImage.from(it) }
        }
    }
    override            fun findReviewImagesFlow(reviewId: Int): Flow<List<ReviewImage>> {
        return reviewImageDao.getReviewImages(reviewId).map { it.map { ReviewImage.from(it) } }
    }
    override suspend    fun deleteById(reviewId: Int) {
        //원격 저장소 요청
        apiFeed.deleteReview(reviewId)
        //로컬 저장소에서 삭제
        feedDao.deleteByReviewId(reviewId)
    }
    @Transaction
    override suspend    fun deleteAll() {
        feedDao.deleteAll()
        likeDao.deleteAll()
        favoriteDao.deleteAll()
        pictureDao.deleteAll()
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

}