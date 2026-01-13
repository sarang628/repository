package com.sarang.torang.di.repository

import com.sarang.torang.BuildConfig
import com.sarang.torang.Follower
import com.sarang.torang.core.database.model.chat.ChatMessageEntity
import com.sarang.torang.core.database.model.chat.ChatParticipantsEntity
import com.sarang.torang.core.database.model.chat.ChatRoomEntity
import com.sarang.torang.core.database.model.chat.embedded.ChatMessageUserImages
import com.sarang.torang.core.database.model.chat.embedded.ChatParticipantUser
import com.sarang.torang.core.database.model.comment.CommentEntity
import com.sarang.torang.core.database.model.favorite.FavoriteAndImageEntity
import com.sarang.torang.core.database.model.favorite.FavoriteEntity
import com.sarang.torang.core.database.model.feed.FeedEntity
import com.sarang.torang.core.database.model.feed.FeedGridEntity
import com.sarang.torang.core.database.model.feed.MyFeedEntity
import com.sarang.torang.core.database.model.feed.ReviewAndImageEntity
import com.sarang.torang.core.database.model.image.ReviewImageEntity
import com.sarang.torang.core.database.model.like.LikeAndImageEntity
import com.sarang.torang.core.database.model.like.LikeEntity
import com.sarang.torang.core.database.model.restaurant.RestaurantEntity
import com.sarang.torang.core.database.model.search.SearchEntity
import com.sarang.torang.core.database.model.user.LoggedInUserEntity
import com.sarang.torang.core.database.model.user.UserEntity
import com.sarang.torang.data.Alarm
import com.sarang.torang.data.ChatImage
import com.sarang.torang.data.ChatMessage
import com.sarang.torang.data.Comment
import com.sarang.torang.data.CommentList
import com.sarang.torang.data.Favorite
import com.sarang.torang.data.FavoriteAndImage
import com.sarang.torang.data.Feed
import com.sarang.torang.data.Filter
import com.sarang.torang.data.Like
import com.sarang.torang.data.LikeAndImage
import com.sarang.torang.data.Restaurant
import com.sarang.torang.data.RestaurantWithFiveImages
import com.sarang.torang.data.ReviewAndImage
import com.sarang.torang.data.ReviewImage
import com.sarang.torang.data.Search
import com.sarang.torang.data.User
import com.sarang.torang.data.remote.response.AlarmAlarmModel
import com.sarang.torang.data.remote.response.ChatApiModel
import com.sarang.torang.data.remote.response.ChatRoomApiModel
import com.sarang.torang.data.remote.response.ChatUserApiModel
import com.sarang.torang.data.remote.response.CommentListApiModel
import com.sarang.torang.data.remote.response.FeedApiModel
import com.sarang.torang.data.remote.response.FilterApiModel
import com.sarang.torang.data.remote.response.FollowerApiModel
import com.sarang.torang.data.remote.response.RatingApiModel
import com.sarang.torang.data.remote.response.RemoteComment
import com.sarang.torang.data.remote.response.RestaurantResponseDto
import com.sarang.torang.data.remote.response.RestaurantV1WithFiveImagesResponseModel
import com.sarang.torang.data.remote.response.UserApiModel
import com.sarang.torang.data.repository.FeedGrid


fun Restaurant.Companion.from(entity : RestaurantEntity) : Restaurant{
    return Restaurant(restaurantId      = entity.restaurantId,
                      restaurantName    = entity.restaurantName,
                      address           = entity.address,
                      lat               = entity.lat,
                      lon               = entity.lon,
                      rating            = entity.rating,
                      tel               = entity.tel,
                      prices            = entity.prices,
                      restaurantType    = entity.restaurantType,
                      regionCode        = entity.regionCode,
                      reviewCount       = entity.reviewCount,
                      site              = entity.site,
                      website           = entity.website,
                      imgUrl1           = entity.imgUrl1)
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
        userId          = this.userId,
        userName        = this.userName,
        email           = this.email,
        loginPlatform   = this.loginPlatform,
        createDate      = this.createDate,
        profilePicUrl   = this.profilePicUrl,
        following       = this.following.toInt(),
        follower        = this.followers.toInt()
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

fun Comment.Companion.from(entity : CommentEntity) : Comment{
    return Comment(commentId       = entity.commentId,
                   userId          = entity.userId,
                   profilePicUrl   = entity.profilePicUrl,
                   userName        = entity.userName,
                   comment         = entity.comment,
                   reviewId        = entity.reviewId)
}

fun Favorite.Companion.from(entity : FavoriteEntity?) : Favorite?{
    return entity?.let {
        Favorite(favoriteId  = entity.favoriteId,
                 reviewId    = entity.reviewId,
                 createDate  = entity.createDate)
    }
}

fun ReviewAndImage.Companion.from(entity : ReviewAndImageEntity) : ReviewAndImage {
    return ReviewAndImage(
        review = Feed.from(entity.review),
        images = entity.images.map { ReviewImage.from(it) },
        like = Like.from(entity.like),
        favorite = Favorite.from(entity.favorite)
    )
}

fun Feed.Companion.from(entity : FeedEntity) : Feed{
    return Feed(reviewId        = entity.reviewId,
                userId          = entity.userId,
                restaurantId    = entity.restaurantId,
                userName        = entity.userName,
                restaurantName  = entity.restaurantName,
                profilePicUrl   = entity.profilePicUrl,
                contents        = entity.contents,
                rating          = entity.rating,
                likeAmount      = entity.likeAmount,
                commentAmount   = entity.commentAmount,
                createDate      = entity.createDate)
}

fun ReviewImage.Companion.from(entity : ReviewImageEntity) : ReviewImage{
    return ReviewImage(pictureId = entity.pictureId,
                       restaurantId = entity.restaurantId,
                       userId = entity.userId,
                       reviewId = entity.reviewId,
                       pictureUrl = entity.pictureUrl,
                       createDate = entity.createDate,
                       menuId = entity.menuId,
                       menu = entity.menu,
                       width = entity.width,
                       height = entity.height)
}

fun Like.Companion.from(entity : LikeEntity?) : Like? {
    return entity?.let {
        Like(likeId      = entity.likeId,
            reviewId    = entity.reviewId,
            createDate  = entity.createDate)
    }
}

fun FavoriteAndImage.Companion.from(entity : FavoriteAndImageEntity) : FavoriteAndImage {
    return FavoriteAndImage(favoriteId  = entity.favoriteId,
                            reviewId    = entity.reviewId,
                            createDate  = entity.createDate,
                            pictureUrl  = entity.pictureUrl,
                            pictureId   = entity.pictureId,
                            width       = entity.width,
                            height      = entity.height)
}

fun LikeAndImage.Companion.from(entity : LikeAndImageEntity) : LikeAndImage {
    return LikeAndImage(likeId      = entity.likeId,
                        reviewId    = entity.reviewId,
                        createDate  = entity.createDate,
                        pictureId   = entity.pictureId,
                        pictureUrl  = entity.pictureUrl,
                        width       = entity.width,
                        height      = entity.height)
}

fun Search.Companion.from(entity : SearchEntity) : Search{
    return Search(key           = entity.key,
                  keyword       = entity.keyword,
                  createDate    = entity.createDate)
}

val Search.entity : SearchEntity get() = SearchEntity(key           = this.key,
                                                      keyword       = this.keyword,
                                                      createDate    = this.createDate)

fun FeedGrid.Companion.from(entity : FeedGridEntity) : FeedGrid{
    return FeedGrid(reviewId = entity.reviewId)
}

fun CommentList.Companion.fromApiModel(apiModel: CommentListApiModel) : CommentList{
    return CommentList(
        profilePicUrl = apiModel.profilePicUrl,
        list = apiModel.list.map { Comment.fromApiModel(it) }
    )
}

fun Comment.Companion.fromApiModel(apiModel : RemoteComment): Comment{
    return Comment(
        commentId = apiModel.comment_id,
        userId = apiModel.user.userId,
        profilePicUrl = apiModel.user.profilePicUrl,
        userName = apiModel.user.userName,
        comment = apiModel.comment,
        reviewId = apiModel.review_id
    )
}

fun Feed.Companion.fromApiModel(apiModel: FeedApiModel) : Feed{
    return Feed(
        reviewId = apiModel.reviewId,
        userId = apiModel.user.userId,
        restaurantId = apiModel.restaurant.restaurantId,
        userName = apiModel.user.userName,
        restaurantName = apiModel.restaurant.restaurantName,
        profilePicUrl = apiModel.user.profilePicUrl,
        contents = apiModel.contents,
        rating = apiModel.rating,
        likeAmount = apiModel.like_amount,
        commentAmount = apiModel.comment_amount,
        createDate = apiModel.create_date
    )
}

fun Alarm.Companion.fromApiModel(apiModel : AlarmAlarmModel) : Alarm{
    return Alarm(
        alarmId = apiModel.alarmId,
        userId = apiModel.user.userId,
        otherUserId = apiModel.otherUserId,
        contents = apiModel.contents,
        alarmType = apiModel.alarmType,
        reviewId = apiModel.reviewId,
        createDate = apiModel.createDate,
        user = User.fromApiModel(apiModel.user),
        otherUser = User.fromApiModel(apiModel.otherUser),
        pictureUrl = apiModel.pictureUrl
    )
}

fun User.Companion.fromApiModel(apiModel : UserApiModel) : User{
    return User(
        userId          = apiModel.userId,
        userName        = apiModel.userName,
        email           = apiModel.email,
        loginPlatform   = apiModel.loginPlatform,
        createDate      = apiModel.createDate,
        profilePicUrl   = apiModel.profilePicUrl,
        post            = apiModel.post,
        follower        = apiModel.follower,
        following       = apiModel.following
    )
}

fun RestaurantWithFiveImages.Companion.from(restaurant: RestaurantV1WithFiveImagesResponseModel) : RestaurantWithFiveImages {
    return RestaurantWithFiveImages(
        restaurant = Restaurant(
            restaurantId = restaurant.restaurant?.restaurantId ?: 0,
            restaurantName = restaurant.restaurant?.restaurantName ?: "",
            address = restaurant.restaurant?.address ?: "",
            lat = restaurant.restaurant?.latitude ?: 0.0,
            lon = restaurant.restaurant?.longitude ?: 0.0,
            rating = restaurant.restaurant?.rating ?: 0f,
            tel = restaurant.restaurant?.tel ?: "",
            prices = restaurant.restaurant?.prices ?: "",
            restaurantType = restaurant.restaurant?.restaurantType ?: "",
            regionCode = restaurant.restaurant?.regionCode ?: 0,
            reviewCount = restaurant.restaurant?.reviewCount ?: 0,
            site = restaurant.restaurant?.website ?: "",
            imgUrl1 = restaurant.restaurant?.image ?: "",
            restaurantTypeCd = restaurant.restaurant?.restaurantTypeCd ?: "",
        ),
        images = restaurant.images ?: listOf()
    )
}

fun Follower.Companion.fromApiModel(apiModel : FollowerApiModel) : Follower{
    return Follower(
        followerId = apiModel.followerId,
        userName = apiModel.userName,
        profilePicUrl = apiModel.profilePicUrl,
        isFollow = apiModel.isFollow
    )
}

fun Restaurant.Companion.fromApiModel(restaurant: RestaurantResponseDto) : Restaurant{
    return Restaurant(
        restaurantId = restaurant.restaurantId ?: -1,
        restaurantName = restaurant.restaurantName ?: "",
        address = restaurant.address ?: "",
        lat = restaurant.lat ?: 0.0,
        lon = restaurant.lon ?: 0.0,
        rating = restaurant.rating ?: 0f,
        tel = restaurant.tel ?: "",
        prices = restaurant.prices ?: "",
        restaurantType = restaurant.restaurantType ?: "",
        regionCode = restaurant.regionCode ?: 0,
        reviewCount = restaurant.reviewCount ?: 0,
        site = restaurant.site ?: "",
        website = restaurant.website ?: "",
        imgUrl1 = restaurant.imgUrl1 ?: "",
        restaurantTypeCd = restaurant.restaurantTypeCd ?: ""
    )
}

fun RestaurantResponseDto.toEntity() : Restaurant {
    return Restaurant(
        restaurantId = restaurantId ?: -1,
        restaurantName = restaurantName ?: "null",
        address = address ?: "null",
        lat = lat ?: 0.0,
        lon = lon ?: 0.0,
        rating = rating ?: 0f,
        tel = tel ?: "null",
        prices = prices ?: "null",
        restaurantType = restaurantType ?: "null",
        regionCode = regionCode ?: 0,
        reviewCount = reviewCount ?: 0,
        site = site ?: "null",
        website = website ?: "null",
        imgUrl1 = imgUrl1 ?: "null",
        restaurantTypeCd = restaurantTypeCd ?: "null"
    )
}

fun Filter.toApiModel() : FilterApiModel{
    return FilterApiModel(
        searchType = this.searchType.name,
        keyword = this.keyword,
        distances = this.distances,
        prices = this.prices,
        restaurantTypes = this.restaurantTypes,
        ratings = this.ratings?.map { it.toRatingApiModel() },
        latitude = this.lat,
        longitude = this.lon,
        northEastLat = this.northEastLat,
        northEastLon = this.northEastLon,
        southWestLat = this.southWestLat,
        southWestLon = this.southWestLon,
    )
}

fun String.toRatingApiModel() : RatingApiModel{
    return when(this){
        "*" -> RatingApiModel.ONE
        "**" -> RatingApiModel.TWO
        "***" -> RatingApiModel.THREE
        "****" -> RatingApiModel.FOUR
        "*****" -> RatingApiModel.FIVE
        else -> {
            RatingApiModel.ONE
        }
    }
}