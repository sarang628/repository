package com.sryang.torang_repository.di.repository.repository.impl

import android.content.Context
import com.sryang.torang_repository.api.ApiReview
import com.sryang.torang_repository.data.MyReview
import com.sryang.torang_repository.data.dao.FeedDao
import com.sryang.torang_repository.data.dao.LoggedInUserDao
import com.sryang.torang_repository.data.dao.MyReviewDao
import com.sryang.torang_repository.data.dao.PictureDao
import com.sryang.torang_repository.data.entity.FeedEntity
import com.sryang.torang_repository.data.entity.ReviewAndImageEntity
import com.sryang.torang_repository.data.entity.ReviewImageEntity
import com.sryang.torang_repository.datasource.MyReviewsLocalDataSource
import com.sryang.torang_repository.datasource.MyReviewsRemoteDataSource
import com.sryang.torang_repository.repository.MyReviewsRepository
import com.sryang.torang_repository.preference.TorangPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyReviewsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiReview: ApiReview,
    private val feedDao: FeedDao,
    private val pictureDao: PictureDao,
    @Deprecated("MyReviewsLocalDataSource 이동") private val myReviewDao: MyReviewDao,
    @Deprecated("MyReviewsLocalDataSource 이동") private val loggedInUserDao: LoggedInUserDao,
    private val myReviewsLocalDataSource: MyReviewsLocalDataSource,
    private val myReviewsRemoteDataSource: MyReviewsRemoteDataSource,
    private val torangPreference: TorangPreference
) :
    MyReviewsRepository {

    override suspend fun getMyReviews(restaurantId: Int): List<ReviewAndImageEntity> {
        val list = apiReview.getMyReviews(HashMap<String, String>().apply {
            put("restaurant_id", restaurantId.toString())
            put("user_id", torangPreference.getUserId().toString())
        })

        val list1 = ArrayList<ReviewAndImageEntity>()
        for (review in list) {
            //list1.add(ReviewAndImageEntity.parse(review))
        }

        //피드 추가하기
        val feeds = ArrayList<FeedEntity>()
        val images = ArrayList<ReviewImageEntity>()
        for (reviewAndInage in list1) {
            /*reviewAndInage.toFeedEntity()?.let {
                feeds.add(it)
            }*/

            reviewAndInage.images.let {
                images.addAll(it)
            }

        }
        feedDao.insertAll(feeds)
        pictureDao.insertPictures(images)
        return list1
    }

    fun userId(): Int {
        return torangPreference.getUserId()
    }

    suspend fun userId1() : Int?{
        return 0
    }

    override fun getMyReviews1(restaurantId: Int): Flow<List<FeedEntity>> {
//        Logger.d("${userId()}, $restaurantId")
        return myReviewDao.getMyReviews(userId(), restaurantId)
    }

    override suspend fun getMyReviews3(restaurantId: Int): List<MyReview> {
        val list = apiReview.getMyReviews(HashMap<String, String>().apply {
            put("user_id", "" + userId1())
            put("restaurant_id", "" + restaurantId)
        })
        val list1 = ArrayList<MyReview>()
        for (review in list) {
            TODO()
        }
        return list1
    }
}