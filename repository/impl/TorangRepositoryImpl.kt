package com.sarang.torang.di.repository.repository.impl

import android.content.Context
import android.view.Menu
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.data.HoursOfOperation
import com.sarang.torang.data.dao.RestaurantDao
import com.sarang.torang.data.remote.response.RemoteRestaurant
import com.sarang.torang.repository.RestaurantRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Deprecated("기능별로 나눠진 저장소로 사용하세요.")
@Singleton
class TorangRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val restaurantService: ApiRestaurant,
    private val restaurantDao: RestaurantDao
) :
    RestaurantRepository {

    private val mapClick = MutableStateFlow<Boolean>(false)
//    private val location = MutableStateFlow(Location(0.0, 0.0))

    override suspend fun loadRestaurant(restaurantId: Int): RemoteRestaurant {
        return restaurantService.getRestaurantById(restaurantId)
    }

    override suspend fun loadMenus(restaurantId: Int): ArrayList<Menu> {
        return restaurantService.getMenus(HashMap<String, String>().apply {
            put("restaurant_id", restaurantId.toString())
        })
    }

    override suspend fun loadHours(restaurantId: Int): ArrayList<HoursOfOperation> {
        return restaurantService.getHoursOfOperation(HashMap<String, String>().apply {
            put("restaurant_id", restaurantId.toString())
        })
    }
}
