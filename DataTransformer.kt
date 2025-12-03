package com.sarang.torang.di.repository

import com.sarang.torang.BuildConfig
import com.sarang.torang.core.database.model.chat.ChatMessageEntity
import com.sarang.torang.core.database.model.chat.ChatParticipantsEntity
import com.sarang.torang.core.database.model.chat.ChatRoomEntity
import com.sarang.torang.core.database.model.chat.embedded.ChatMessageUserImages
import com.sarang.torang.core.database.model.chat.embedded.ChatParticipantUser
import com.sarang.torang.core.database.model.feed.MyFeedEntity
import com.sarang.torang.core.database.model.restaurant.RestaurantEntity
import com.sarang.torang.core.database.model.user.LoggedInUserEntity
import com.sarang.torang.core.database.model.user.UserEntity
import com.sarang.torang.data.ChatImage
import com.sarang.torang.data.ChatMessage
import com.sarang.torang.data.Restaurant
import com.sarang.torang.data.User
import com.sarang.torang.data.remote.response.ChatApiModel
import com.sarang.torang.data.remote.response.ChatRoomApiModel
import com.sarang.torang.data.remote.response.ChatUserApiModel
import com.sarang.torang.data.remote.response.FeedApiModel
import com.sarang.torang.data.remote.response.UserApiModel

fun RestaurantEntity.toEntity() : Restaurant{
    return Restaurant(restaurantId = restaurantId, restaurantName = restaurantName, address = address, lat = lat, lon = lon, rating = rating, tel = tel, prices = prices, restaurantType = restaurantType, regionCode = regionCode, reviewCount = reviewCount, site = site, website = website, imgUrl1 = imgUrl1)
}

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
    userId = this.userEntity.userId,
    userName = this.userEntity.userName,
    email = this.userEntity.email,
    loginPlatform = this.userEntity.loginPlatform,
    createDate = this.userEntity.createDate,
    profilePicUrl = BuildConfig.PROFILE_IMAGE_SERVER_URL + (this.userEntity.createDate)
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

val ChatUserApiModel.user : UserEntity get() = UserEntity(
    userId = userId,
    userName = userName,
    profilePicUrl = profilePicUrl,
    email = "",
    loginPlatform = "",
    createDate = "",
    accessToken = "",
    point = 0,
    reviewCount = "",
    followers = "",
    following = ""
)

val ChatRoomApiModel.participants : List<ChatParticipantsEntity> get() = participants.map {
    ChatParticipantsEntity(
        roomId = roomId,
        userId = it.userId
    )
}

val ChatApiModel.chat : ChatMessageEntity get() = ChatMessageEntity(
    userId = userId,
    uuid = uuid,
    roomId = roomId,
    message = message,
    createDate = createDate,
    sending = false
)

fun FeedApiModel.toMyFeedEntity(): MyFeedEntity {
    return MyFeedEntity(
        reviewId        = reviewId,
        userId          = user.userId,
        contents        = contents,
        rating          = rating,
        userName        = user.userName,
        likeAmount      = like_amount,
        commentAmount   = comment_amount,
        restaurantName  = restaurant.restaurantName,
        restaurantId    = restaurant.restaurantId,
        createDate      = this.create_date,
        profilePicUrl   = this.user.profilePicUrl
    )
}
