package com.sarang.torang.di.repository.repository.impl

import android.content.Context
import android.view.Menu
import androidx.compose.runtime.Composable
import com.sarang.torang.Picture
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.data.HoursOfOperation
import com.sarang.torang.data.RestaurantDetail
import com.sarang.torang.data.dao.PictureDao
import com.sarang.torang.data.dao.RestaurantDao
import com.sarang.torang.data.entity.ReviewImageEntity
import com.sarang.torang.data.remote.response.RemoteRestaurant
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
//    private val location = MutableStateFlow(Location(0.0, 0.0))

    override suspend fun loadRestaurant(restaurantId: Int): RemoteRestaurant {
        return apiRestaurant.getRestaurantById(restaurantId)
    }

    override suspend fun loadMenus(restaurantId: Int): ArrayList<Menu> {
        return apiRestaurant.getMenus(HashMap<String, String>().apply {
            put("restaurant_id", restaurantId.toString())
        })
    }

    override suspend fun loadHours(restaurantId: Int): ArrayList<HoursOfOperation> {
        return apiRestaurant.getHoursOfOperation(HashMap<String, String>().apply {
            put("restaurant_id", restaurantId.toString())
        })
    }

    override suspend fun loadRestaurantDetail(restaurantId: Int): RestaurantDetail {
        val result = apiRestaurant.getRestaurantDetail(restaurantId)
        pictureDao.insertPictures(result.pictures.toReviewImageList())
        return result
    }
}

fun List<Picture>.toReviewImageList(): List<ReviewImageEntity> {
    return this.map { it.toReviewImageEntity() }
}

fun Picture.toReviewImageEntity(): ReviewImageEntity {
    return ReviewImageEntity(
        pictureId = picture_id,
        restaurantId = restaurant_id,
        reviewId = restaurant_id,
        pictureUrl = picture_url,
        createDate = create_date,
        userId = user_id,
        menuId = menu_id,
        menu = 0
    )
}