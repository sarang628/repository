package com.sarang.torang.di.repository.repository.impl

import com.sarang.torang.core.database.model.favorite.FavoriteEntity
import com.sarang.torang.core.database.model.feed.FeedEntity
import com.sarang.torang.core.database.model.like.LikeEntity
import com.sarang.torang.data.remote.response.FavoriteApiModel
import com.sarang.torang.data.remote.response.FeedApiModel
import com.sarang.torang.data.remote.response.LikeApiModel

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

fun FeedApiModel.toFeedEntity(): FeedEntity {
    return FeedEntity(
        reviewId = this.reviewId,
        userId = this.user.userId,
        rating = this.rating,
        userName = this.user.userName,
        profilePicUrl = this.user.profilePicUrl,
        likeAmount = this.like_amount,
        commentAmount = this.comment_amount,
        restaurantName = this.restaurant.restaurantName,
        restaurantId = this.restaurant.restaurantId,
        contents = this.contents,
        createDate = this.create_date
    )
}
