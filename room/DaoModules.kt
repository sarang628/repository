package com.sryang.torang_repository.di.repository.room

import com.sryang.torang_repository.data.AppDatabase
import com.sryang.torang_repository.data.dao.FavoriteDao
import com.sryang.torang_repository.data.dao.FeedDao
import com.sryang.torang_repository.data.dao.LikeDao
import com.sryang.torang_repository.data.dao.LoggedInUserDao
import com.sryang.torang_repository.data.dao.PictureDao
import com.sryang.torang_repository.data.dao.UserDao
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
}