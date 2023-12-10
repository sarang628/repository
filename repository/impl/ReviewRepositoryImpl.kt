package com.sryang.torang_repository.di.repository.repository.impl

import android.content.Context
import com.sryang.torang_repository.api.ApiReview
import com.sryang.torang_repository.api.handle
import com.sryang.torang_repository.data.dao.FeedDao
import com.sryang.torang_repository.data.dao.LoggedInUserDao
import com.sryang.torang_repository.data.dao.PictureDao
import com.sryang.torang_repository.data.dao.RestaurantDao
import com.sryang.torang_repository.data.dao.ReviewDao
import com.sryang.torang_repository.data.entity.ReviewAndImageEntity
import com.sryang.torang_repository.data.entity.ReviewImageEntity
import com.sryang.torang_repository.data.remote.response.RemoteFeed
import com.sryang.torang_repository.data.remote.response.toReviewImage
import com.sryang.torang_repository.repository.ReviewRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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
    private val feedDao: FeedDao
) : ReviewRepository {
    override suspend fun getReviews(restaurantId: Int): List<RemoteFeed> {
        return apiReview.getReviews(restaurantId)
    }

    override suspend fun addReview(
        content: String,
        restaurantId: Int,
        rating: Float,
        files: List<String>
    ): RemoteFeed {
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
        } catch (e: retrofit2.HttpException) {
            throw Exception(e.handle())
        }
    }

    override suspend fun updateReview(
        reviewId: Int,
        contents: String,
        restaurantId: Int,
        rating: Float,
        files: List<String>,
        uploadedImage: List<Int>
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
        return feedDao.getFeed(reviewId)
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