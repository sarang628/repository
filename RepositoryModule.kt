package com.sarang.torang.di.repository

import com.sarang.torang.repository.ChatRepository
import com.sarang.torang.repository.EditProfileRepository
import com.sarang.torang.repository.FavoriteRepository
import com.sarang.torang.repository.feed.FeedRepository
import com.sarang.torang.repository.FindRepository
import com.sarang.torang.repository.FollowRepository
import com.sarang.torang.repository.LikeRepository
import com.sarang.torang.repository.LoginRepository
import com.sarang.torang.repository.MapRepository
import com.sarang.torang.repository.UserRepository
import com.sarang.torang.repository.RestaurantRepository
import com.sarang.torang.repository.comment.CommentRepository
import com.sarang.torang.repository.feed.FeedFlowRepository
import com.sarang.torang.repository.feed.FeedLoadRepository
import com.sarang.torang.repository.review.ReviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// @formatter:off
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun provideProfileRepository    (profileRepository: UserRepositoryImpl)         : UserRepository
    @Binds abstract fun provideEditProfileRepository(profileRepository: EditProfileRepositoryImpl)  : EditProfileRepository
    @Binds abstract fun provideInfoRepository       (infoRepositoryImpl: RestaurantRepositoryImpl)  : RestaurantRepository
    @Binds abstract fun provideReviewRepository     (reviewRepositoryImpl: ReviewRepositoryImpl)    : ReviewRepository
    @Binds abstract fun provideMapRepository        (mapRepositoryImpl: MapRepositoryImpl)          : MapRepository
    @Binds abstract fun provideLoginRepository      (loginRepositoryImpl: LoginRepositoryImpl)      : LoginRepository
    @Binds abstract fun provideFeedRepository       (feedRepositoryImpl: FeedRepositoryImpl)        : FeedRepository
    @Binds abstract fun provideFollowRepository     (followRepository: FollowRepositoryImpl)        : FollowRepository
    @Binds abstract fun provideCommentRepository    (commentRepository: CommentRepositoryImpl)      : CommentRepository
    @Binds abstract fun provideChatRepository       (chatRepository: ChatRepositoryImpl)            : ChatRepository
    @Binds abstract fun provideLikeRepository       (likeRepository: LikeRepositoryImpl)            : LikeRepository
    @Binds abstract fun provideFindRepository       (findRepository: FindRepositoryImpl)            : FindRepository
    @Binds abstract fun provideFavoriteRepository   (favoriteRepository: FavoriteRepositoryImpl)    : FavoriteRepository
    @Binds abstract fun provideFeedLoadRepository   (feedLoadRepository: FeedLoadRepositoryImpl)    : FeedLoadRepository
    @Binds abstract fun provideFeedFlowRepository   (feedFlowRepository: FeedFlowRepositoryImpl)    : FeedFlowRepository
}