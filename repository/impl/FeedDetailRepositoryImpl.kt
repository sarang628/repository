package com.sryang.torang_repository.di.repository.repository.impl

import android.content.Context
import com.sryang.torang_repository.api.ApiComment
import com.sryang.torang_repository.api.ApiRestaurant
import com.sryang.torang_repository.data.RemoteComment
import com.sryang.torang_repository.data.Restaurant
import com.sryang.torang_repository.data.dao.CommentDao
import com.sryang.torang_repository.data.dao.LoggedInUserDao
import com.sryang.torang_repository.data.dao.RestaurantDao
import com.sryang.torang_repository.data.dao.ReviewDao
import com.sryang.torang_repository.data.entity.CommentEntity
import com.sryang.torang_repository.data.entity.FeedEntity
import com.sryang.torang_repository.repository.FeedDetailRepository
import com.sryang.torang_repository.preference.TorangPreference
import com.sryang.torang_repository.session.SessionService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    private val loggedInUserDao: LoggedInUserDao, override val isLogin: Flow<Boolean>,
    private val sessionService: SessionService
) :
    FeedDetailRepository {

    override suspend fun getComments(reviewId: Int): List<RemoteComment> {
        var list: List<RemoteComment> = ArrayList();
        sessionService.getToken()?.let {
            list = apiComment.getComments(it, reviewId).list
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

@Singleton
class TimeLineDetailRepositoryTestImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val restaurantDao: RestaurantDao,
    private val restaurantService: ApiRestaurant,
    private val apiComment: ApiComment,
    private val reviewDao: ReviewDao,
    private val loggedInUserDao: LoggedInUserDao, override val isLogin: Flow<Boolean>,
    private val torangPreference: TorangPreference
) :
    FeedDetailRepository {

    override suspend fun getComments(reviewId: Int): List<RemoteComment> {
        return ArrayList()
    }

    override fun getCommentsFlow(reviewId: Int): Flow<List<CommentEntity>> {
        TODO("Not yet implemented")
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

    fun userId(): Int {
        return torangPreference.getUserId()
    }

    override suspend fun addComment(reviewId: Int, value: String): RemoteComment {
        TODO("not yet implemented")
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeLineDetailRepositoryModule {
    @Binds
    abstract fun bindTimeLineDetailRepository(
        timeLineDetailRepositoryImpl: FeedDetailRepositoryImpl
    ): FeedDetailRepository
}
