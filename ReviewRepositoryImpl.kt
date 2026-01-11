package com.sarang.torang.di.repository

import android.content.Context
import com.sarang.torang.api.ApiReview
import com.sarang.torang.api.ApiReviewV1
import com.sarang.torang.api.handle
import com.sarang.torang.core.database.dao.FeedDao
import com.sarang.torang.core.database.dao.LoggedInUserDao
import com.sarang.torang.core.database.dao.PictureDao
import com.sarang.torang.core.database.dao.RestaurantDao
import com.sarang.torang.core.database.dao.ReviewDao
import com.sarang.torang.data.Feed
import com.sarang.torang.data.ReviewAndImage
import com.sarang.torang.di.torang_database_di.toFeedEntity
import com.sarang.torang.di.torang_database_di.toReviewImage
import com.sarang.torang.repository.review.ReviewRepository
import com.sarang.torang.session.SessionService
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
    private val apiReviewV1: ApiReviewV1,
    private val restaurantDao: RestaurantDao,
    private val loggedInUserDao: LoggedInUserDao,
    private val reviewDao: ReviewDao,
    private val pictureDao: PictureDao,
    private val feedDao: FeedDao,
    private val sessionService: SessionService,
) : ReviewRepository {
    override suspend fun getReviews(restaurantId: Int): List<Feed> {

        return apiReviewV1.getReviewsByRestaurantId(
            auth = sessionService.getToken() ?: "",
            restaurantId = restaurantId
        ).map {
            Feed.fromApiModel(it)
        }
    }

    override suspend fun addReview(
        content: String,
        restaurantId: Int?,
        rating: Float,
        files: List<String>,
    ): Feed {
        val user = loggedInUserDao.getLoggedInUser() ?: throw Exception("로그인을 해주세요.")
        try {
            val review = apiReview.addReview(
                user_id = user.userId,
                contents = content.toRequestBody(),
                torang_id = restaurantId,
                rating = rating,
                file = filesToMultipart(files)
            )

            pictureDao.addAll(
                review.pictures.map {
                    it.toReviewImage()
                }
            )
            reviewDao.insert(review.toFeedEntity())

            return Feed.fromApiModel(review)
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
        val userId = loggedInUserDao.getLoggedInUser()?.userId
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

        pictureDao.delete(reviewId)
        pictureDao.addAll(
            review.pictures.map {
                it.toReviewImage()
            }
        )
        reviewDao.insert(review.toFeedEntity())
    }

    override suspend fun getReview(reviewId: Int): ReviewAndImage {
        val reviewImageEntity = feedDao.find(reviewId) ?: throw Exception("리뷰를 찾을 수 없습니다.")
        return ReviewAndImage.from(reviewImageEntity)
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