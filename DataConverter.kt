package com.sarang.torang.di.repository

import com.sarang.torang.core.database.model.favorite.FavoriteEntity
import com.sarang.torang.core.database.model.feed.FeedEntity
import com.sarang.torang.core.database.model.like.LikeEntity
import com.sarang.torang.core.database.model.user.UserEntity
import com.sarang.torang.data.remote.response.FavoriteApiModel
import com.sarang.torang.data.remote.response.FeedApiModel
import com.sarang.torang.data.remote.response.LikeApiModel

fun FeedApiModel.toUserEntity(): UserEntity {
    return UserEntity(
        userId = this.user.userId,
        email = this.user.email ?: "",
        loginPlatform = this.user.loginPlatform ?: "",
        createDate = this.user.createDate ?: "",
        accessToken = "",
        profilePicUrl = this.user.profilePicUrl,
        point = 0,
        reviewCount = "0",
        following = "0",
        followers = "0",
        userName = this.user.userName
    )
}

fun FavoriteApiModel.toFavoriteEntity(): FavoriteEntity {
    return FavoriteEntity(
        reviewId = this.review_id,
        favoriteId = this.favorite_id,
        userId = this.user_id,
        createDate = this.create_date
    )
}

fun LikeApiModel.toLikeEntity(): LikeEntity {
    return LikeEntity(
        likeId = likeId,
        userId = userId,
        createDate = createDate,
        reviewId = reviewId
    )
}