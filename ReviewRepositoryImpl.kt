package com.sarang.torang.di.repository

import android.content.Context
import com.sarang.torang.api.ApiReview
import com.sarang.torang.api.handle
import com.sarang.torang.core.database.dao.FeedDao
import com.sarang.torang.core.database.dao.LoggedInUserDao
import com.sarang.torang.core.database.dao.PictureDao
import com.sarang.torang.core.database.dao.RestaurantDao
import com.sarang.torang.core.database.dao.ReviewDao
import com.sarang.torang.core.database.model.feed.ReviewAndImageEntity
import com.sarang.torang.data.remote.response.FeedApiModel
import com.sarang.torang.di.torang_database_di.toFeedEntity
import com.sarang.torang.repository.review.ReviewRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val apiReview: ApiReview,
    private val restaurantDao: RestaurantDao,
    private val loggedInUserDao: LoggedInUserDao,
    private val reviewDao: ReviewDao,
    private val pictureDao: PictureDao,
    private val feedDao: FeedDao,
) : ReviewRepository {
    override suspend fun getReviews(restaurantId: Int): List<FeedApiModel> {
        return apiReview.getReviews(restaurantId)
    }

    override suspend fun addReview(
        content: String,
        restaurantId: Int?,
        rating: Float,
        files: List<String>,
    ): FeedApiModel {
        val user = loggedInUserDao.getLoggedInUser1() ?: throw Exception("로그인을 해주세요.")
        try {
            val review = apiReview.addReview(
                user_id = user.userId,
                contents = content.toRequestBody(),
                torang_id = restaurantId,
                rating = rating,
                file = filesToMultipart(files)
            )

            pictureDao.insertPictures(
                review.pictures.map {
                    it.toReviewImage()
                }
            )
            reviewDao.insert(review.toFeedEntity())

            return review
        } catch (e: HttpException) {
            throw Exception(e.handle())
        }
    }

    override suspend fun updateReview(
        reviewId: Int,
        contents: String,
        restaurantId: Int?,
        rating: Float,
        files: List<String>,
        uploadedImage: List<Int>,
    ) {
        val userId = loggedInUserDao.getLoggedInUser1()?.userId
        userId ?: throw java.lang.Exception("로그인을 해주세요.")
        val review = apiReview.addReview(
            review_id = reviewId,
            user_id = userId,
            contents = contents.toRequestBody(),
            torang_id = restaurantId,
            rating = rating,
            file = filesToMultipart(files),
            uploadedImage = uploadedImage
        )

        pictureDao.removePicture(reviewId)
        pictureDao.insertPictures(
            review.pictures.map {
                it.toReviewImage()
            }
        )
        reviewDao.insert(review.toFeedEntity())
    }

    override suspend fun getReview(reviewId: Int): ReviewAndImageEntity {
        return feedDao.getFeed(reviewId) ?: throw Exception("리뷰를 찾을 수 없습니다.")
    }
}

fun filesToMultipart(file: List<String>): ArrayList<MultipartBody.Part> {
    val list = ArrayList<MultipartBody.Part>()
        .apply {
            addAll(
                file.map {
                    val file = File(it)
                    MultipartBody.Part.createFormData(
                        name = "file",
                        filename = file.name,
                        body = file.asRequestBody()
                    )
                }
            )
        }
    return list
}