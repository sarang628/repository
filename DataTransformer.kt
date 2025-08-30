package com.sarang.torang.di.repository

import com.google.gson.Gson
import com.sarang.torang.data.Restaurant
import com.sarang.torang.core.database.model.chat.ChatRoomEntity
import com.sarang.torang.core.database.model.feed.FeedEntity
import com.sarang.torang.core.database.model.restaurant.RestaurantEntity
import com.sarang.torang.core.database.model.image.ReviewImageEntity
import com.sarang.torang.core.database.model.restaurant.SearchedRestaurantEntity
import com.sarang.torang.data.remote.response.ChatRoomApiModel
import com.sarang.torang.data.remote.response.FeedApiModel
import com.sarang.torang.data.remote.response.RemotePicture
import com.sarang.torang.data.remote.response.RestaurantResponseDto

fun RestaurantEntity.toEntity() : Restaurant{
    return Restaurant(restaurantId = restaurantId, restaurantName = restaurantName, address = address, lat = lat, lon = lon, rating = rating, tel = tel, prices = prices, restaurantType = restaurantType, regionCode = regionCode, reviewCount = reviewCount, site = site, website = website, imgUrl1 = imgUrl1)
}

/*
fun SearchedRestaurantEntity.Companion.fromRestaurantEntity(restaurantEntity: RestaurantEntity): SearchedRestaurantEntity {
    val gson = Gson()
    val json = gson.toJson(restaurantEntity)
    return gson.fromJson(json, SearchedRestaurantEntity::class.java)
}
*/

/*fun SearchedRestaurantEntity.Companion.fromRestaurantEntity(models: List<RestaurantEntity>): List<SearchedRestaurantEntity> {
    return models.map { it ->
        SearchedRestaurantEntity.fromRestaurantEntity(it)
    }
}*/

/*fun SearchedRestaurantEntity.Companion.fromRestaurantApiModel(restaurantEntity: RestaurantResponseDto): SearchedRestaurantEntity {
    val gson = Gson()
    val json = gson.toJson(restaurantEntity)
    val result = gson.fromJson(json, SearchedRestaurantEntity::class.java)
    return result.copy(restaurantName = restaurantEntity.restaurantName ?: "null", restaurantId = restaurantEntity.restaurantId ?: -1, imgUrl1 = restaurantEntity.imgUrl1 ?: "null", regionCode = restaurantEntity.regionCode.toString(), restaurantType = restaurantEntity.restaurantType ?: "null", reviewCount = restaurantEntity.reviewCount.toString(), website = restaurantEntity.website ?: "null")
}*/

/*fun SearchedRestaurantEntity.Companion.fromRestaurantApiModel(models: List<RestaurantResponseDto>): List<SearchedRestaurantEntity> {
    return models
        .map { it ->
            SearchedRestaurantEntity.fromRestaurantApiModel(it)
        }
}*/

fun ChatRoomApiModel.toChatRoomEntity(): ChatRoomEntity = ChatRoomEntity(
    roomId = roomId,
    createDate = createDate,
)

fun FeedApiModel.toFeedEntity(): FeedEntity {
    return FeedEntity(
        reviewId = reviewId,
        userId = user.userId,
        contents = contents,
        rating = rating,
        userName = user.userName,
        likeAmount = like_amount,
        commentAmount = comment_amount,
        restaurantName = restaurant.restaurantName,
        restaurantId = restaurant.restaurantId,
        createDate = this.create_date,
        profilePicUrl = this.user.profilePicUrl
    )
}

fun RemotePicture.toReviewImage(): ReviewImageEntity {
    return ReviewImageEntity(
        pictureId = this.picture_id,
        restaurantId = this.restaurant_id,
        userId = this.user_id,
        reviewId = this.review_id,
        pictureUrl = this.picture_url,
        createDate = this.create_date ?: "",
        menuId = this.menu_id,
        menu = 1,
        width = this.width,
        height = this.height
    )
}