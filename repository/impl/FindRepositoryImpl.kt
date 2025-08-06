package com.sarang.torang.di.repository.repository.impl

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.data.Distances
import com.sarang.torang.data.Prices
import com.sarang.torang.data.SearchType
import com.sarang.torang.data.remote.response.RestaurantApiModel
import com.sarang.torang.repository.FindRepository
import com.sarang.torang.repository.RequestLocationResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindRepositoryImpl @Inject constructor() : FindRepository {
    @Singleton
    class FindRepositoryImpl @Inject constructor(private val restaurantService: ApiRestaurant, @ApplicationContext private val context: Context) : FindRepository {
        private val isFirstRequestLocation = MutableStateFlow(false)
        private val isRequestingLocation = MutableStateFlow(false)
        private val currentPosition = MutableStateFlow(0)
        private val showRestaurantCardAndFilter = MutableStateFlow(true)
        private val hasGrantPermission: MutableStateFlow<Int> = MutableStateFlow<Int>(context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
        private val restaurants = MutableStateFlow<List<RestaurantApiModel>>(ArrayList())
        private val manager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        override suspend fun searchIfRestaurantEmpty() {
            if (restaurants.value.isEmpty()) { restaurants.emit(restaurantService.getAllRestaurant()) }
        }
        override fun getIsFirstRequestLocation(): Flow<Boolean> { return isFirstRequestLocation }
        override suspend fun notifyRequestLocation(): RequestLocationResult {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) { return RequestLocationResult.PERMISSION_DENIED }
            val statusOfGPS: Boolean = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!statusOfGPS) { return RequestLocationResult.GPS_OFF }
            return RequestLocationResult.SUCCESS
        }
        override fun isRequestingLocation(): Flow<Boolean> { return isRequestingLocation }
        override suspend fun notifyReceiveLocation() { isRequestingLocation.emit(false) }
        override suspend fun setCurrentPosition(position: Int) {
            currentPosition.emit(position)
            if (!showRestaurantCardAndFilter.value) showRestaurantCardAndFilter.emit(true) }
        override fun getCurrentPosition(): Flow<Int> { return currentPosition }
        override suspend fun requestLocationPermission(b: Boolean) {}
        override fun hasGrantPermission(): MutableStateFlow<Int> { return hasGrantPermission }
        override suspend fun permissionGranated() {}
        override fun showRestaurantCardAndFilter(): Flow<Boolean> { return showRestaurantCardAndFilter }
        override suspend fun clickMap() { showRestaurantCardAndFilter.emit(!showRestaurantCardAndFilter.value) }
        override fun getSearchedRestaurant(): List<RestaurantApiModel> { return restaurants.value }
        override suspend fun searchRestaurant(distances: Distances?, restaurantType: ArrayList<String>?, prices: Prices?, ratings: ArrayList<String>?, latitude: Double, longitude: Double, northEastLatitude: Double, northEastLongitude: Double, southWestLatitude: Double, southWestLongitude: Double, searchType: SearchType) {}
    }
    override suspend fun clickMap() {}
    override fun getCurrentPosition(): Flow<Int> { return MutableStateFlow(0) }
    override fun getIsFirstRequestLocation(): Flow<Boolean> { return MutableStateFlow(false) }
    override fun getSearchedRestaurant(): List<RestaurantApiModel> { return ArrayList() }
    override fun hasGrantPermission(): MutableStateFlow<Int> { return MutableStateFlow(0) }
    override fun isRequestingLocation(): Flow<Boolean> { return MutableStateFlow(false) }
    override suspend fun notifyReceiveLocation() {}
    override suspend fun notifyRequestLocation(): RequestLocationResult { return RequestLocationResult.SUCCESS }
    override suspend fun permissionGranated() {}
    override suspend fun requestLocationPermission(b: Boolean) {}
    override suspend fun searchIfRestaurantEmpty() {}
    override suspend fun searchRestaurant(distances: Distances?, restaurantType: ArrayList<String>?, prices: Prices?, ratings: ArrayList<String>?, latitude: Double, longitude: Double, northEastLatitude: Double, northEastLongitude: Double, southWestLatitude: Double, southWestLongitude: Double, searchType: SearchType) {}
    override suspend fun setCurrentPosition(position: Int) {}
    override fun showRestaurantCardAndFilter(): Flow<Boolean> { return MutableStateFlow(false) }
}