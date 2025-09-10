package com.sarang.torang.di.repository

import android.content.Context
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.api.ApiReview
import com.sarang.torang.data.ModifyFeedData
import com.sarang.torang.core.database.dao.FeedDao
import com.sarang.torang.core.database.dao.LoggedInUserDao
import com.sarang.torang.core.database.dao.PictureDao
import com.sarang.torang.core.database.dao.RestaurantDao
import com.sarang.torang.core.database.dao.ReviewDao
import com.sarang.torang.core.database.model.feed.FeedEntity
import com.sarang.torang.core.database.model.restaurant.RestaurantEntity
import com.sarang.torang.core.database.model.feed.ReviewAndImageEntity
import com.sarang.torang.core.database.model.image.ReviewImageEntity
import com.sarang.torang.repository.MyReviewRepository
import com.sarang.torang.preference.TorangPreference
import com.sarang.torang.util.CountingFileRequestBody
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyReviewRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reviewDao: ReviewDao,
    private val restaurantService: ApiRestaurant,
    private val apiReview: ApiReview,
    private val feedDao: FeedDao,
    private val pictureDao: PictureDao,
    private val restaurantDao: RestaurantDao,
    private val loggedInUserDao: LoggedInUserDao,
    private val torangPreference: TorangPreference
) :
    MyReviewRepository {
    override fun getMyReview(reviewId: Int): Flow<FeedEntity?> {
        return reviewDao.getFeedFlowbyReviewId(reviewId)
    }

    override fun getUploadedPicture(reviewId: Int): Flow<List<ReviewImageEntity>> {
        return MutableStateFlow<List<ReviewImageEntity>>(ArrayList())
    }


    override suspend fun uploadReview(review: ReviewAndImageEntity) {

        if (review.review.userId == -1)
            throw IllegalArgumentException("사용자 정보가 없습니다.")

        val fileList = ArrayList<File>()

        review.images.let {
            for (image in it) {
                fileList.add(File(image.pictureUrl))
            }
        }

        val pictureList = ArrayList<MultipartBody.Part>()

        for (i in fileList.indices) {
            val file: File = fileList[i]
            val requestFile = CountingFileRequestBody(file, "image/*", object :
                CountingFileRequestBody.ProgressListener {
                override fun transferred(num: Long) {

                }
            })

            pictureList.add(MultipartBody.Part.createFormData("file", file.name, requestFile))
        }

        val result = restaurantService.fileUpload(review.toMap(), pictureList)

        //피드 추가하기
        val list1 = ArrayList<ReviewAndImageEntity>()
        //list1.add(ReviewAndImageEntity.parse(result))

        //피드 추가하기
        val feeds = ArrayList<FeedEntity>()
        val images = ArrayList<ReviewImageEntity>()
        for (reviewAndInage in list1) {
            /*reviewAndInage.toFeedEntity().let {
                feeds.add(it)
            }*/
            reviewAndInage.images.let {
                images.addAll(it)
            }

        }
        feedDao.insertAll(feeds)
        pictureDao.insertPictures(images)
    }

    override suspend fun modifyReview(review: ReviewAndImageEntity) {

        if (review.review.userId == -1)
            throw IllegalArgumentException("사용자 정보가 없습니다.")

        val fileList = ArrayList<File>()

        review.images.let {
            for (image in it) {
                fileList.add(File(image.pictureUrl))
            }
        }

        val pictureList = ArrayList<MultipartBody.Part>()

        for (i in fileList.indices) {
            val file: File = fileList[i]
            val requestFile = CountingFileRequestBody(file, "image/*", object :
                CountingFileRequestBody.ProgressListener {
                override fun transferred(num: Long) {

                }
            })

            pictureList.add(MultipartBody.Part.createFormData("file", file.name, requestFile))
        }

        val result = apiReview.updateReview(review.toMap(), pictureList)

        //피드 추가기
        val list1 = ArrayList<ReviewAndImageEntity>()
        //list1.add(ReviewAndImageEntity.parse(result))

        //피드 추가하기
        val feeds = ArrayList<FeedEntity>()
        val images = ArrayList<ReviewImageEntity>()
        for (reviewAndInage in list1) {
            /*reviewAndInage.toFeedEntity().let {
                feeds.add(it)
            }*/
            reviewAndInage.images.let {
                images.addAll(it)
            }

        }
        feedDao.insertAll(feeds)
        pictureDao.insertPictures(images)
    }

    override suspend fun modifyReview(review: ModifyFeedData) {

        /*if (review.reviewAndImage.review == null)
            throw IllegalArgumentException("리뷰 데이터가 없습니다.")*/

        /*if (review.reviewAndImage.user == null || review.reviewAndImage.user?.userId == -1)
            throw IllegalArgumentException("사용자 정보가 없습니다.")*/

        val fileList = ArrayList<File>()

        /*review.reviewAndImage.images?.let {
            for (image in it) {
                fileList.add(File(image.picture_url))
            }
        }*/

        val pictureList = ArrayList<MultipartBody.Part>()

        for (i in fileList.indices) {
            val file: File = fileList[i]
            val requestFile = CountingFileRequestBody(file, "image/*", object :
                CountingFileRequestBody.ProgressListener {
                override fun transferred(num: Long) {

                }
            })

            pictureList.add(MultipartBody.Part.createFormData("file", file.name, requestFile))
        }

        try {
//            val result = restaurantService.updateReview(review.toMap(), pictureList)

            //피드 추가기
            val list1 = ArrayList<ReviewAndImageEntity>()
            //list1.add(ReviewAndImageEntity.parse(result))

            //피드 추가하기
            val feeds = ArrayList<FeedEntity>()
            val images = ArrayList<ReviewImageEntity>()
            for (reviewAndInage in list1) {
                /*reviewAndInage.toFeedEntity()?.let {
                    feeds.add(it)
                }*/
                reviewAndInage.images.let {
                    //images.addAll(it)
                }

            }
            feedDao.insertAll(feeds)
            //userDao.deletePicturesByReviewId(review.reviewAndImage.review!!.review_id)
            pictureDao.insertPictures(images)
        } catch (e: Exception) {
//            Logger.e(e.toString())
        }
    }

    override suspend fun getRestaurant(restaurantId: Int): RestaurantEntity? {
        return restaurantDao.getRestaurantByRestaurantId(restaurantId)
    }

    override fun userId(): Int {
        return torangPreference.getUserId()
    }

    override suspend fun userId1(): Int {
        var userId = -1

        return userId
    }
}


fun ReviewAndImageEntity.toMap(): HashMap<String, RequestBody> {
    val params: HashMap<String, RequestBody> = HashMap()
    params["review_id"] =
        RequestBody.create("text/plain".toMediaTypeOrNull(), "" + review.reviewId)
    params["torang_id"] =
        RequestBody.create("text/plain".toMediaTypeOrNull(), "" + review.restaurantId)
    params["contents"] =
        RequestBody.create("text/plain".toMediaTypeOrNull(), "" + review.contents)
    params["rating"] = RequestBody.create("text/plain".toMediaTypeOrNull(), "" + review.rating)
    return params
}