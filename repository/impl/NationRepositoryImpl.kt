package com.sryang.torang_repository.di.repository.repository.impl

import com.sryang.torang_repository.data.Restaurant
import com.sryang.torang_repository.data.NationItem
import com.sryang.torang_repository.data.dao.RestaurantDao
import com.sryang.torang_repository.api.ApiRestaurant
import com.sryang.torang_repository.repository.NationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NationRepositoryImpl @Inject constructor(
    private val restaurantDao: RestaurantDao,
    private val restaurantService: ApiRestaurant
) : NationRepository {

    private val selectNationItem: MutableStateFlow<NationItem> = MutableStateFlow(NationItem(0))

    override suspend fun getNationItems(): List<NationItem> {
        TODO("Not yet implemented")
    }

    override suspend fun findRestaurant(): List<Restaurant> {
        TODO("Not yet implemented")
    }

    override fun getSelectNationItem(): Flow<NationItem> {
        return selectNationItem
    }

    override suspend fun selectNationItem(nationItem: NationItem) {
        selectNationItem.emit(nationItem)
    }

}

