package com.sarang.torang.di.repository

import android.content.Context
import com.google.gson.Gson
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.data.HoursOfOperation
import com.sarang.torang.data.Menu
import com.sarang.torang.data.RestaurantDetail
import com.sarang.torang.core.database.dao.PictureDao
import com.sarang.torang.core.database.dao.RestaurantDao
import com.sarang.torang.core.database.model.image.ReviewImageEntity
import com.sarang.torang.data.Restaurant
import com.sarang.torang.data.remote.response.PictureApiModel
import com.sarang.torang.data.remote.response.RestaurantDetailApiModel
import com.sarang.torang.data.remote.response.RestaurantResponseDto
import com.sarang.torang.repository.RestaurantRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val apiRestaurant: ApiRestaurant,
    private val restaurantDao: RestaurantDao,
    private val pictureDao: PictureDao,
) :
    RestaurantRepository {

    private val mapClick = MutableStateFlow<Boolean>(false)

    override suspend fun loadRestaurant(restaurantId: Int): Restaurant {
        return Restaurant.fromApiModel(apiRestaurant.getRestaurantById(restaurantId))
    }


    override suspend fun loadMenus(restaurantId: Int): List<Menu> {
        return apiRestaurant.getMenus(HashMap<String, String>().apply {
            put("restaurant_id", restaurantId.toString())
        }).map {
            Gson().fromJson<Menu>(Gson().toJson(it), Menu::class.java)
        }
    }

    override suspend fun loadHours(restaurantId: Int): List<HoursOfOperation> {
        return apiRestaurant.getHoursOfOperation(HashMap<String, String>().apply {
            put("restaurant_id", restaurantId.toString())
        }).map {
            Gson().fromJson<HoursOfOperation>(Gson().toJson(it), HoursOfOperation::class.java)
        }
    }

    override suspend fun loadRestaurantDetail(restaurantId: Int): RestaurantDetail {
        val result : RestaurantDetailApiModel = apiRestaurant.getRestaurantDetail(restaurantId)
        pictureDao.addAll(result.pictures.toReviewImageList())
        return Gson().fromJson<RestaurantDetail>(Gson().toJson(result), RestaurantDetail::class.java)
    }
}

fun List<PictureApiModel>.toReviewImageList(): List<ReviewImageEntity> {
    return this.map { it.toReviewImageEntity() }
}

fun PictureApiModel.toReviewImageEntity(): ReviewImageEntity {
    return ReviewImageEntity(
        pictureId = picture_id,
        restaurantId = restaurant_id,
        reviewId = restaurant_id,
        pictureUrl = picture_url ?: "",
        createDate = create_date ?: "",
        userId = user_id,
        menuId = menu_id,
        menu = 0,
        width = width,
        height = height
    )
}