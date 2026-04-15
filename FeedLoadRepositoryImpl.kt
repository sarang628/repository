package com.sarang.torang.di.repository

import android.util.Log
import androidx.room.Transaction
import com.google.gson.Gson
import com.sarang.torang.api.ApiFeed
import com.sarang.torang.api.ApiFeedV1
import com.sarang.torang.api.ApiReview
import com.sarang.torang.api.handle
import com.sarang.torang.core.database.dao.FavoriteDao
import com.sarang.torang.core.database.dao.FeedDao
import com.sarang.torang.core.database.dao.FeedGridDao
import com.sarang.torang.core.database.dao.FeedInsertDao
import com.sarang.torang.core.database.dao.LikeDao
import com.sarang.torang.core.database.dao.LoggedInUserDao
import com.sarang.torang.core.database.dao.MainFeedDao
import com.sarang.torang.core.database.dao.MyFeedDao
import com.sarang.torang.core.database.dao.PictureDao
import com.sarang.torang.core.database.dao.UserDao
import com.sarang.torang.core.database.model.favorite.FavoriteEntity
import com.sarang.torang.core.database.model.feed.FeedGridEntity
import com.sarang.torang.core.database.model.feed.MainFeedEntity
import com.sarang.torang.core.database.model.image.ReviewImageEntity
import com.sarang.torang.core.database.model.like.LikeEntity
import com.sarang.torang.data.ReviewAndImage
import com.sarang.torang.data.remote.response.FavoriteFeedApiModel
import com.sarang.torang.data.remote.response.FeedApiModel
import com.sarang.torang.di.torang_database_di.toFavoriteEntity
import com.sarang.torang.di.torang_database_di.toFeedEntity
import com.sarang.torang.di.torang_database_di.toLikeEntity
import com.sarang.torang.di.torang_database_di.toReviewImage
import com.sarang.torang.di.torang_database_di.toUserEntity
import com.sarang.torang.repository.feed.FeedLoadRepository
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

private const val tag = "__FeedLoadRepositoryImpl"
@Singleton
class FeedLoadRepositoryImpl @Inject constructor(
    private val apiFeed                 : ApiFeed,
    private val apiFeedV1               : ApiFeedV1,
    private val apiReview               : ApiReview,
    private val feedDao                 : FeedDao,
    private val feedInsertDao           : FeedInsertDao,
    private val feedGridDao             : FeedGridDao,
    private val userDao                 : UserDao,
    private val likeDao                 : LikeDao,
    private val myFeedDao               : MyFeedDao,
    private val pictureDao              : PictureDao,
    private val mainFeedDao             : MainFeedDao,
    private val favoriteDao             : FavoriteDao,
    private val loggedInUserDao         : LoggedInUserDao,
    private val sessionClientService    : SessionClientService
) : FeedLoadRepository {
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
    override suspend    fun loadById(reviewId: Int) {
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
                reviewImages = list.mapNotNull { it },
                userList     = feedList.map { it.toUserEntity() },
                likeList     = feedList.filter { it.like != null }
                                       .map { it.like!!.toLikeEntity() },
                favorites    = feedList.filter { it.favorite != null }
                                       .map { it.favorite!!.toFavoriteEntity() }
            )
        } catch (e: Exception) {
            Log.e(tag, e.toString())
            Log.e(tag, Gson().newBuilder().setPrettyPrinting().create().toJson(feedList))
            throw Exception("피드를 가져오는데 실패하였습니다.")
        }
    }
    private  suspend    fun insertFeed(feedList: List<FeedApiModel>) {
        feedInsertDao.insertAllFeed(userDao         = userDao,
                                    likeDao         = likeDao,
                                    pictureDao      = pictureDao,
                                    favoriteDao     = favoriteDao,
                                    feedList        = feedList.map { it.toFeedEntity() },
                                    reviewImages    = feedList.map { it.pictures }
                                                              .flatMap { it }
                                                              .mapNotNull { it.toReviewImage() },
                                    userList        = feedList.map { it.toUserEntity() },
                                    likeList        = feedList.filter { it.like != null }.map { it.like!!.toLikeEntity() },
                                    favorites       = feedList.filter { it.favorite != null }.map { it.favorite!!.toFavoriteEntity() },
                                    feedDao         = feedDao)
    }
    @Transaction
             suspend    fun deleteAll() {
        feedDao.deleteAll()
        likeDao.deleteAll()
        favoriteDao.deleteAll()
        pictureDao.deleteAll()
        feedGridDao.deleteAll()
    }
    override suspend    fun loadFeedGird(reviewId: Int) {
        val result = apiFeedV1.findByFeedGrid(reviewId = reviewId,
                                              offset = 30)

        val count  = feedGridDao.findAll().size

        val reviewImages = emptyList<ReviewImageEntity>()
        try {
            val reviewImages = result.filter {
                var result = true
                val errorMessage = "findByFeedGrid API 응답 변환 오류"
                if(it.reviewId == null){
                    Log.w(tag, "$errorMessage reviewId 없음.")
                    result = false
                } else if(it.picture == null){
                    Log.w(tag, "$errorMessage picture 없음.")
                    result = false
                } else if(it.picture?.pictureId == null){
                    Log.w(tag, "$errorMessage pictureId 없음.")
                    result = false
                } else if(it.picture?.pictureUrl == null){
                    Log.w(tag, "$errorMessage pictureUrl 없음.")
                    result = false
                } else if(it.picture?.width == null){
                    Log.w(tag, "$errorMessage width 없음.")
                    result = false
                } else if(it.picture?.height == null){
                    Log.w(tag, "$errorMessage height 없음.")
                    result = false
                }
                result
            }.mapNotNull { it ->
                val pic = it.picture
                val rid = it.reviewId

                // rid와 pic이 모두 null이 아닐 때만 run 내부 실행
                if (rid != null && pic != null) {
                    // 1. 검사할 값들을 모두 '불변 지역 변수(val)'로 선언합니다.
                    val pid = pic.pictureId
                    val url = pic.pictureUrl
                    val w = pic.width
                    val h = pic.height

                    // 2. 이 지역 변수들을 체크하면 하단에서는 자동으로 non-null로 취급됩니다.
                    if (pid != null && url != null && w != null && h != null) {
                        ReviewImageEntity(
                            pictureId = pid,   // !! 필요 없음 (Smart Cast)
                            pictureUrl = url,  // !! 필요 없음
                            width = w,
                            height = h,
                            reviewId = rid
                        )
                    } else {
                        Log.w(tag, "findByFeedGrid API 응답 변환 오류: 필수 데이터 누락")
                        null
                    }
                } else {
                    Log.w(tag, "API 응답 변환 오류: 필수 객체(reviewId/picture) 누락")
                    null
                }
            }
            pictureDao.addAll(reviewImages)
        }catch (e : Exception){
            throw Exception("피드 그리드 API 응답 데이터 변환 실패: ${e.message}")
        }

        pictureDao.addAll(reviewImages = reviewImages)

        feedGridDao.addAll(
            result.mapIndexedNotNull { index, model ->
                val reviewId = model.reviewId
                reviewId?.let {
                    FeedGridEntity(reviewId = it,
                        order    = count+index)
                }
            }
        )
    }

    override suspend fun setLoadTrigger(boolean: Boolean) {
        loadTrigger.emit(boolean)
    }
}