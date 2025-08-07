package com.sarang.torang.di.repository.room

import com.sarang.torang.data.AppDatabase
import com.sarang.torang.data.dao.ChatDao
import com.sarang.torang.data.dao.CommentDao
import com.sarang.torang.data.dao.FavoriteDao
import com.sarang.torang.data.dao.FeedDao
import com.sarang.torang.data.dao.LikeDao
import com.sarang.torang.data.dao.LoggedInUserDao
import com.sarang.torang.data.dao.MyFeedDao
import com.sarang.torang.data.dao.PictureDao
import com.sarang.torang.data.dao.RestaurantDao
import com.sarang.torang.data.dao.ReviewDao
import com.sarang.torang.data.dao.SearchedRestaurantDao
import com.sarang.torang.data.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class DaoModules {
    @Provides
    fun provideLoggedInUserDao(appDatabase: AppDatabase): LoggedInUserDao {
        return appDatabase.LoggedInUserDao()
    }

    @Provides
    fun proviadeFeedDao(appDatabase: AppDatabase): FeedDao {
        return appDatabase.feedDao()
    }

    @Provides
    fun providePictureDao(appDatabase: AppDatabase): PictureDao {
        return appDatabase.pictureDao()
    }

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    fun provideLikeDao(appDatabase: AppDatabase): LikeDao {
        return appDatabase.likeDao()
    }

    @Provides
    fun provideFavoriteDao(appDatabase: AppDatabase): FavoriteDao {
        return appDatabase.favoriteDao()
    }

    @Provides
    fun provideReviewDao(appDatabase: AppDatabase): ReviewDao {
        return appDatabase.reviewDao()
    }

    @Provides
    fun provideRestaurantDao(appDatabase: AppDatabase): RestaurantDao {
        return appDatabase.restaurantDao()
    }

    @Provides
    fun provideCommentDao(appDatabase: AppDatabase): CommentDao {
        return appDatabase.commentDao()
    }

    @Provides
    fun provideMyFeedDao(appDatabase: AppDatabase): MyFeedDao {
        return appDatabase.myFeedDao()
    }

    @Provides
    fun provideChatDao(appDatabase: AppDatabase): ChatDao {
        return appDatabase.chatDao()
    }

    @Provides
    fun searchedRestaurantDao(appDatabase: AppDatabase): SearchedRestaurantDao {
        return appDatabase.searchedRestaurantDao()
    }
}