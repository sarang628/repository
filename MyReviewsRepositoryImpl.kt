package com.sarang.torang.di.repository

import android.content.Context
import com.sarang.torang.api.ApiReview
import com.sarang.torang.core.database.dao.FeedDao
import com.sarang.torang.core.database.dao.LoggedInUserDao
import com.sarang.torang.core.database.dao.MyReviewDao
import com.sarang.torang.core.database.dao.PictureDao
import com.sarang.torang.core.database.model.feed.FeedEntity
import com.sarang.torang.core.database.model.feed.ReviewAndImageEntity
import com.sarang.torang.core.database.model.image.ReviewImageEntity
import com.sarang.torang.data.MyReview
import com.sarang.torang.datasource.MyReviewsLocalDataSource
import com.sarang.torang.datasource.MyReviewsRemoteDataSource
import com.sarang.torang.preference.TorangPreference
import com.sarang.torang.repository.MyReviewsRepository
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
        feedDao.addAll(feeds)
        pictureDao.addAll(images)
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

        }
        return list1
    }
}