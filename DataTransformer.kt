package com.sarang.torang.di.repository

import com.google.gson.Gson
import com.sarang.torang.BuildConfig
import com.sarang.torang.data.Restaurant
import com.sarang.torang.core.database.model.chat.ChatRoomEntity
import com.sarang.torang.core.database.model.chat.embedded.ChatMessageUserImages
import com.sarang.torang.core.database.model.chat.embedded.ChatParticipantUser
import com.sarang.torang.core.database.model.feed.FeedEntity
import com.sarang.torang.core.database.model.restaurant.RestaurantEntity
import com.sarang.torang.core.database.model.image.ReviewImageEntity
import com.sarang.torang.core.database.model.restaurant.SearchedRestaurantEntity
import com.sarang.torang.core.database.model.user.LoggedInUserEntity
import com.sarang.torang.core.database.model.user.UserEntity
import com.sarang.torang.data.ChatImage
import com.sarang.torang.data.ChatMessage
import com.sarang.torang.data.User
import com.sarang.torang.data.remote.response.ChatRoomApiModel
import com.sarang.torang.data.remote.response.FeedApiModel
import com.sarang.torang.data.remote.response.RemotePicture
import com.sarang.torang.data.remote.response.RestaurantResponseDto
import com.sarang.torang.data.remote.response.UserApiModel

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

fun UserApiModel.toLoggedInUserEntity(): LoggedInUserEntity {
    return LoggedInUserEntity(
        userId = this.userId,
        userName = this.userName,
        email = this.email,
        loginPlatform = this.loginPlatform,
        createDate = this.createDate,
        profilePicUrl = profilePicUrl
    )
}

val UserEntity.user : User get() =
    User(
        userId = this.userId,
        userName = this.userName,
        email = this.email,
        loginPlatform = this.loginPlatform,
        createDate = this.createDate,
        profilePicUrl = this.profilePicUrl
    )

val ChatParticipantUser.user get() = User(
    userId = this.userEntity?.userId ?: -1,
    userName = this.userEntity?.userName ?: "사용자 정보 없음.",
    email = this.userEntity?.email ?: "",
    loginPlatform = this.userEntity?.loginPlatform ?: "",
    createDate = this.userEntity?.createDate ?: "",
    profilePicUrl = BuildConfig.PROFILE_IMAGE_SERVER_URL + (this.userEntity?.createDate ?: "")
)

fun  ChatMessageUserImages.chatMessage(roomId: Int) : ChatMessage {
    return ChatMessage(
        uuid = chatMessage.uuid,
        roomId = roomId,
        userId = user.userId,
        message = chatMessage.message,
        createDate = chatMessage.createDate,
        sending = chatMessage.sending,
        user = user.user,
        images = images.map {
            ChatImage(
                parentUuid = it.parentUuid,
                uuid = it.uuid,
                roomId = roomId,
                userId = it.userId,
                localUri = it.localUri,
                url = it.url,
                createDate = it.createDate,
                uploadedDate = it.uploadedDate,
                sending = it.sending,
                failed = it.failed
            )
        }
    )
}