package com.sryang.torang_repository.di.repository.datasource

import com.sryang.torang_repository.datasource.FeedRemoteDataSource
import com.sryang.torang_repository.datasource.impl.FeedRemoteDataSourceImpl
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