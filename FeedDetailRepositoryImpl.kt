package com.sarang.torang.di.repository

import android.content.Context
import com.sarang.torang.api.ApiComment
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.core.database.dao.CommentDao
import com.sarang.torang.core.database.dao.LoggedInUserDao
import com.sarang.torang.core.database.dao.RestaurantDao
import com.sarang.torang.core.database.dao.ReviewDao
import com.sarang.torang.data.Comment
import com.sarang.torang.data.Feed
import com.sarang.torang.data.Restaurant
import com.sarang.torang.data.remote.response.RemoteComment
import com.sarang.torang.preference.TorangPreference
import com.sarang.torang.repository.feed.FeedDetailRepository
import com.sarang.torang.session.SessionService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    override suspend fun getComments(reviewId: Int): List<Comment> {
        var list: List<RemoteComment> = ArrayList();
        sessionService.getToken()?.let {
            list = apiComment.getComments(it, reviewId).list
        }
        //commentDao.insertComments(CommentEntity.parse(list))
        return list.map {
            Comment.fromApiModel(it)
        }
    }

    override fun getCommentsFlow(reviewId: Int): Flow<List<Comment>> {
        return commentDao.getComments(reviewId)
                         .map { it.map { Comment.from(it) } }
    }

    override fun getReview(): Flow<Feed> {
        throw Exception("")
    }

    override fun getRestaurant(reviewId: Int): Flow<Restaurant> {
        throw Exception("")
    }

    override fun getFeed(reviewId: Int): Flow<Feed> {
        throw Exception("")
    }

    override suspend fun addComment(reviewId: Int, value: String): Comment {
        throw Exception("")
    }
}

@Singleton
class TimeLineDetailRepositoryTestImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val restaurantDao: RestaurantDao,
    private val restaurantService: ApiRestaurant,
    private val apiComment: ApiComment,
    private val reviewDao: ReviewDao,
    private val loggedInUserDao: LoggedInUserDao,
    val isLogin: Flow<Boolean>,
    private val torangPreference: TorangPreference
) :
    FeedDetailRepository {

    override suspend fun getComments(reviewId: Int): List<Comment> {
        return ArrayList()
    }

    override fun getCommentsFlow(reviewId: Int): Flow<List<Comment>> {
        throw Exception("")
    }

    override fun getReview(): Flow<Feed> {
        throw Exception("")
    }

    override fun getRestaurant(reviewId: Int): Flow<Restaurant> {
        throw Exception("")
    }

    override fun getFeed(reviewId: Int): Flow<Feed> {
        throw Exception("")
    }

    fun userId(): Int {
        return torangPreference.getUserId()
    }

    override suspend fun addComment(reviewId: Int, value: String): Comment {
        throw Exception("")
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
