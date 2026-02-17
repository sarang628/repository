package com.sarang.torang.di.repository

import com.sarang.torang.core.database.dao.FeedDao
import com.sarang.torang.core.database.dao.MyFeedDao
import com.sarang.torang.core.database.dao.ReviewAndImageDao
import com.sarang.torang.core.database.dao.ReviewImageDao
import com.sarang.torang.data.FavoriteAndImage
import com.sarang.torang.data.LikeAndImage
import com.sarang.torang.data.ReviewAndImage
import com.sarang.torang.data.ReviewImage
import com.sarang.torang.data.repository.FeedGrid
import com.sarang.torang.repository.feed.FeedFlowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedFlowRepositoryImpl @Inject constructor(
    private val feedDao                 : FeedDao,
    private val reviewAndImageDao       : ReviewAndImageDao,
    private val myFeedDao               : MyFeedDao,
    private val reviewImageDao          : ReviewImageDao,
) : FeedFlowRepository {
    private val tag: String = "__FeedRepositoryImpl"
    override fun findRestaurantFeedsFlow(restaurantId: Int) : Flow<List<ReviewAndImage>> {
        return reviewAndImageDao.findAllByRestaurantIdFlow(restaurantId).map { it.map { ReviewAndImage.from(it) } }
    }
    override fun findByIdFlow(reviewId: Int)                : Flow<ReviewAndImage?> {
        return reviewAndImageDao.findByReviewIdFlow(reviewId = reviewId).map { if(it == null) null else ReviewAndImage.from(it) }
    }
    override fun findMyFeedById(reviewId: Int)              : Flow<List<ReviewAndImage>> {
        return myFeedDao.findUserFeedsByReviewId(reviewId)
                        .map { it.map { ReviewAndImage.from(it) } }
    }
    override fun findByUserIdFlow(userId: Int)              : Flow<List<ReviewAndImage>> {
        return myFeedDao.findByUserId(userId)
                        .map { it.map { ReviewAndImage.from(it) } }
    }
    override fun findByFavoriteFlow()                       : Flow<List<FavoriteAndImage>> {
        return reviewAndImageDao.findAllByFavoriteFlow().map { it.map { FavoriteAndImage.from(it) } }
    }
    override fun findByLikeFlow()                           : Flow<List<LikeAndImage>> {
        return reviewAndImageDao.findAllByLikeFlow().map { it.map { LikeAndImage.from(it) } }
    }
    override fun findByPictureIdFlow(pictureId: Int)        : Flow<ReviewAndImage?> {
        return reviewAndImageDao.findByPictureIdFlow(pictureId).map {
            it?.let { ReviewAndImage.from(it) }
        }
    }
    override fun findReviewImagesFlow(reviewId: Int)        : Flow<List<ReviewImage>> {
        return reviewImageDao.getReviewImages(reviewId).map { it.map { ReviewImage.from(it) } }
    }
    override fun findFeedGridFlow()                         : Flow<List<FeedGrid>> {
        return reviewAndImageDao.findAllByFeedGrid().map {
            it.map {
                FeedGrid(reviewId   = it.reviewId,
                         pictureId  = it.pictureId,
                         pictureUrl = it.pictureUrl,
                         width      = it.width,
                         height     = it.height)
            }
        }
    }
}