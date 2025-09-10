package com.sarang.torang.di.repository

import com.sarang.torang.datasource.FeedRemoteDataSource
import com.sarang.torang.datasource.impl.FeedRemoteDataSourceImpl
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
    fun provideFeedRemoteDataSource(feedRemoteDataSourceImpl: FeedRemoteDataSourceImpl): FeedRemoteDataSource {
        return feedRemoteDataSourceImpl
    }
}