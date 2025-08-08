package com.sarang.torang.di.repository.repository.impl

import android.util.Log
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.data.Filter
import com.sarang.torang.data.Restaurant
import com.sarang.torang.data.remote.response.RestaurantResponseDto
import com.sarang.torang.data.remote.response.toEntity
import com.sarang.torang.repository.FindRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.contains

@Singleton
class FindRepositoryImpl @Inject constructor(
    val apiRestaurant: ApiRestaurant
) : FindRepository {
    private var _restaurants : MutableStateFlow<List<RestaurantResponseDto>> = MutableStateFlow(listOf())
    private var _foodType: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    private var _price: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    private var _rating: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    private var _distance: MutableStateFlow<String> = MutableStateFlow("")
    private var _keyword: MutableStateFlow<String> = MutableStateFlow("")
    override var restaurants : StateFlow<List<RestaurantResponseDto>> = _restaurants
    private var _selectedRestaurant : MutableStateFlow<Restaurant> = MutableStateFlow(Restaurant())
    var selectedRestaurant : StateFlow<Restaurant> = _selectedRestaurant
    private val foodType: StateFlow<List<String>> = _foodType
    private val price: StateFlow<List<String>> = _price
    private val rating: StateFlow<List<String>> = _rating
    private val distance: StateFlow<String> = _distance
    private val keyword: StateFlow<String> = _keyword

    suspend fun setDistance(distance : String){ this._distance.emit(distance)}
    suspend fun setKeyword(keyword : String){ this._keyword.emit(keyword) }
    fun getFoodType(): StateFlow<List<String>> { return foodType }
    fun getPrices(): StateFlow<List<String>> { return price }
    fun getRatings(): StateFlow<List<String>> { return rating }
    fun getDistances(): StateFlow<String> { return distance }

    suspend fun setFoodType(foodType : String){
            if(this.foodType.value.contains(foodType)) {
                this._foodType.emit(this.foodType.value.filter { it != foodType }.toList())
            }
            else {
                this._foodType.emit(ArrayList(this.foodType.value).apply { add(foodType) })
            }
    }
    suspend fun setPrice(price : String){
        if(this.price.value.contains(price)) {
            this._price.emit(this.price.value.filter { it != price }.toList())
        }
        else {
            this._price.emit(ArrayList(this.price.value).apply { add(price) })
        }
    }
    suspend fun setRating(rating : String){
        if(this.rating.value.contains(rating)) {
            this._rating.emit(this.rating.value.filter { it != rating }.toList())
        }
        else {
            this._rating.emit(ArrayList(this.rating.value).apply { add(rating) })
        }
    }


    override suspend fun findThisArea() {
        val filter = Filter()
        filter.prices = price.value
        filter.ratings = rating.value
        filter.distances = distance.value
        if(distance.value == "") filter.distances = null
        filter.keyword = keyword.value
        filter.restaurantTypes = foodType.value
        search(filter)
    }

    override suspend fun findFilter() {
        val filter = Filter()
        filter.prices = price.value
        filter.ratings = rating.value
        filter.distances = distance.value
        if(distance.value == "") filter.distances = null
        filter.keyword = keyword.value
        filter.restaurantTypes = foodType.value
        search(filter)
    }

    override suspend fun search(filter: Filter) {
        try {
            Log.d("__FindRepositoryImpl", filter.toString())
            _restaurants.emit(apiRestaurant.getFilterRestaurant(filter))
        }
        catch (e : HttpException){
            Log.e("__FindRepositoryImpl", e.response()?.errorBody()?.string().toString())
        }
        catch (e : Exception){
            Log.e("__FindRepositoryImpl", e.toString())
        }
    }


    suspend fun selectRestaurant(restaurantId: Int) {
        _restaurants.value.firstOrNull { it.restaurantId == restaurantId }?.let {
            _selectedRestaurant.emit(it.toEntity())
        }
    }

}