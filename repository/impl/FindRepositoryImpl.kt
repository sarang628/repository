package com.sarang.torang.di.repository.repository.impl

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.data.Filter
import com.sarang.torang.data.remote.response.RestaurantApiModel
import com.sarang.torang.repository.FindRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindRepositoryImpl @Inject constructor(
    val apiRestaurant: ApiRestaurant
) : FindRepository {
    private var _restaurants : MutableStateFlow<List<RestaurantApiModel>> = MutableStateFlow(listOf())
    override var restaurants : StateFlow<List<RestaurantApiModel>> = _restaurants

    override suspend fun search(filter: Filter) {
        try {
            _restaurants.emit(apiRestaurant.getFilterRestaurant(filter))
        }catch (e : Exception){
            Log.e("__FindRepositoryImpl", e.toString())
        }
    }
}