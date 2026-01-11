package com.sarang.torang.di.repository

import com.sarang.torang.data.Feed
import com.sarang.torang.datasource.FeedRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FeedRemoteDataSourceModule {
    @Singleton
    @Provides
    fun provideFeedRemoteDataSource(): FeedRemoteDataSource {
        return object : FeedRemoteDataSource{
            override suspend fun getFeeds(userId: Int): List<Feed> {
                TODO("Not yet implemented")
            }

            override suspend fun deleteFeed(reviewId: Int) {
                TODO("Not yet implemented")
            }
        }
    }
}