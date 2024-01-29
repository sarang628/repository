package com.sarang.torang.di.repository.repository.impl

import com.sarang.torang.api.ApiComment
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.data.RemoteComment
import com.sarang.torang.data.Restaurant
import com.sarang.torang.data.dao.CommentDao
import com.sarang.torang.data.dao.LoggedInUserDao
import com.sarang.torang.data.dao.RestaurantDao
import com.sarang.torang.data.dao.ReviewDao
import com.sarang.torang.data.entity.CommentEntity
import com.sarang.torang.data.entity.FeedEntity
import com.sarang.torang.repository.FeedDetailRepository
import com.sarang.torang.session.SessionService
import com.sarang.torang.util.DateConverter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedDetailRepositoryImpl @Inject constructor(
    private val restaurantService: ApiRestaurant,
    private val apiComment: ApiComment,
    private val commentDao: CommentDao,
    private val restaurantDao: RestaurantDao,
    private val reviewDao: ReviewDao,
    private val loggedInUserDao: LoggedInUserDao,
    val isLogin: Flow<Boolean>,
    private val sessionService: SessionService
) :
    FeedDetailRepository {

    override suspend fun getComments(reviewId: Int): List<RemoteComment> {
        var list: List<RemoteComment> = ArrayList();
        sessionService.getToken()?.let {
            list = apiComment.getComments(it, reviewId).list.map {
                it.copy(
                    create_date = DateConverter.formattedDate(it.create_date)
                )
            }
        }
        //commentDao.insertComments(CommentEntity.parse(list))
        return list
    }

    override fun getCommentsFlow(reviewId: Int): Flow<List<CommentEntity>> {
        return commentDao.getComments(reviewId)
    }

    override fun getReview(): Flow<FeedEntity> {
        TODO("Not yet implemented")
    }

    override fun getRestaurant(reviewId: Int): Flow<Restaurant> {
        //return restaurantDao.getRestaurantByReviewId(reviewId)
        TODO()
    }

    override fun getFeed(reviewId: Int): Flow<FeedEntity> {
        //return reviewDao.getFeedbyReviewId(reviewId)
        TODO()
    }

    override suspend fun addComment(reviewId: Int, value: String): RemoteComment {

        /*val comment = restaurantService.addComment(Comment().apply {
            user = User().apply { userId = loggedInUserDao.getLoggedInUserEntity1()!!.userId!! }
            comment = value
            review_id = reviewId
        })

        commentDao.insertComment(Comment.parse(comment))

        return comment*/
        TODO()
    }
}