package com.sarang.torang.di.repository.repository.impl

import android.content.Context
import android.view.Menu
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.data.dao.RestaurantDao
import com.sarang.torang.repository.MenuRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val restaurantService: ApiRestaurant,
    private val restaurantDao: RestaurantDao
) : MenuRepository {
    private val mapClick = MutableStateFlow<Boolean>(false)

    override suspend fun getMenus(restaurantId: Int): ArrayList<Menu> {
        return restaurantService.getMenus(HashMap<String, String>().apply {
            put("restaurant_id", restaurantId.toString())
        })
    }
}