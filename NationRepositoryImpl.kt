package com.sarang.torang.di.repository

import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.core.database.dao.RestaurantDao
import com.sarang.torang.data.NationItem
import com.sarang.torang.data.Restaurant
import com.sarang.torang.repository.NationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NationRepositoryImpl @Inject constructor(
    private val restaurantDao: RestaurantDao,
    private val restaurantService: ApiRestaurant
) : NationRepository {

    private val selectNationItem: MutableStateFlow<NationItem> = MutableStateFlow(NationItem(0))

    override suspend fun getNationItems(): List<NationItem> {
        return listOf()
    }

    override suspend fun findRestaurant(): List<Restaurant> {
        return listOf()
    }

    override fun getSelectNationItem(): Flow<NationItem> {
        return selectNationItem
    }

    override suspend fun selectNationItem(nationItem: NationItem) {
        selectNationItem.emit(nationItem)
    }

}