package com.sarang.torang.di.repository.repository

import com.sarang.torang.di.repository.repository.impl.ChatRepositoryImpl
import com.sarang.torang.di.repository.repository.impl.CommentRepositoryImpl
import com.sarang.torang.di.repository.repository.impl.EditProfileRepositoryImpl
import com.sarang.torang.di.repository.repository.impl.FeedRepositoryImpl
import com.sarang.torang.di.repository.repository.impl.FollowRepositoryImpl
import com.sarang.torang.di.repository.repository.impl.LikeRepositoryImpl
import com.sarang.torang.di.repository.repository.impl.LoginRepositoryImpl
import com.sarang.torang.di.repository.repository.impl.MapRepositoryImpl
import com.sarang.torang.di.repository.repository.impl.ProfileRepositoryImpl
import com.sarang.torang.di.repository.repository.impl.RestaurantRepositoryImpl
import com.sarang.torang.di.repository.repository.impl.ReviewRepositoryImpl
import com.sarang.torang.repository.ChatRepository
import com.sarang.torang.repository.EditProfileRepository
import com.sarang.torang.repository.FeedRepository
import com.sarang.torang.repository.FollowRepository
import com.sarang.torang.repository.LikeRepository
import com.sarang.torang.repository.LoginRepository
import com.sarang.torang.repository.MapRepository
import com.sarang.torang.repository.ProfileRepository
import com.sarang.torang.repository.RestaurantRepository
import com.sarang.torang.repository.comment.CommentRepository
import com.sarang.torang.repository.review.ReviewRepository
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
    abstract fun provideInfoRepository(infoRepositoryImpl: RestaurantRepositoryImpl): RestaurantRepository

    @Binds
    abstract fun provideReviewRepository(reviewRepositoryImpl: ReviewRepositoryImpl): ReviewRepository

    @Binds
    abstract fun provideMapRepository(mapRepositoryImpl: MapRepositoryImpl): MapRepository

    @Binds
    abstract fun provideLoginRepository(loginRepositoryImpl: LoginRepositoryImpl): LoginRepository

    @Binds
    abstract fun provideFeedRepository(feedRepositoryImpl: FeedRepositoryImpl): FeedRepository

    @Binds
    abstract fun provideFollowRepository(followRepository: FollowRepositoryImpl): FollowRepository

    @Binds
    abstract fun provideCommentRepository(commentRepository: CommentRepositoryImpl): CommentRepository

    @Binds
    abstract fun provideChatRepository(chatRepository: ChatRepositoryImpl): ChatRepository

    @Binds
    abstract fun provideLikeRepository(likeRepository: LikeRepositoryImpl): LikeRepository
}