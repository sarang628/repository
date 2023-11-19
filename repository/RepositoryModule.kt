package com.sryang.torang_repository.di.repository.repository

import com.sryang.torang_repository.di.repository.repository.impl.EditProfileRepositoryImpl
import com.sryang.torang_repository.di.repository.repository.impl.FeedRepositoryImpl
import com.sryang.torang_repository.di.repository.repository.impl.FollowRepositoryImpl
import com.sryang.torang_repository.di.repository.repository.impl.InfoRepositoryImpl
import com.sryang.torang_repository.di.repository.repository.impl.LoginRepositoryImpl
import com.sryang.torang_repository.di.repository.repository.impl.MapRepositoryImpl
import com.sryang.torang_repository.di.repository.repository.impl.MenuRepositoryImpl
import com.sryang.torang_repository.di.repository.repository.impl.ProfileRepositoryImpl
import com.sryang.torang_repository.di.repository.repository.impl.ReviewRepositoryImpl
import com.sryang.torang_repository.repository.EditProfileRepository
import com.sryang.torang_repository.repository.FeedRepository
import com.sryang.torang_repository.repository.FollowRepository
import com.sryang.torang_repository.repository.LoginRepository
import com.sryang.torang_repository.repository.MapRepository
import com.sryang.torang_repository.repository.MenuRepository
import com.sryang.torang_repository.repository.ProfileRepository
import com.sryang.torang_repository.repository.RestaurantRepository
import com.sryang.torang_repository.repository.ReviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun provideProfileRepository(profileRepository: ProfileRepositoryImpl): ProfileRepository

    @Binds
    abstract fun provideEditProfileRepository(profileRepository: EditProfileRepositoryImpl): EditProfileRepository

    @Binds
    abstract fun provideInfoRepository(infoRepositoryImpl: InfoRepositoryImpl): RestaurantRepository

    @Binds
    abstract fun provideReviewRepository(reviewRepositoryImpl: ReviewRepositoryImpl): ReviewRepository

    @Binds
    abstract fun provideMenuRepository(menuRepositoryImpl: MenuRepositoryImpl): MenuRepository

    @Binds
    abstract fun provideMapRepository(mapRepositoryImpl: MapRepositoryImpl): MapRepository

    @Binds
    abstract fun provideLoginRepository(loginRepositoryImpl: LoginRepositoryImpl): LoginRepository

    @Binds
    abstract fun provideFeedRepository(feedRepositoryImpl: FeedRepositoryImpl): FeedRepository

    @Binds
    abstract fun provideFollowRepository(followRepository: FollowRepositoryImpl): FollowRepository
}