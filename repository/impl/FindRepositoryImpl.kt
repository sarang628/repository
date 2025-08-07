package com.sarang.torang.di.repository.repository.impl

import android.util.Log
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.data.Filter
import com.sarang.torang.data.remote.response.RestaurantApiModel
import com.sarang.torang.repository.FindRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindRepositoryImpl @Inject constructor(
    val apiRestaurant: ApiRestaurant
) : FindRepository {
    private var _restaurants : MutableStateFlow<List<RestaurantApiModel>> = MutableStateFlow(listOf())
    override var restaurants : StateFlow<List<RestaurantApiModel>> = _restaurants
    private var foodType: List<String> = arrayListOf()
    private var price: List<String> = arrayListOf()
    private var rating: List<String> = arrayListOf()
    private var distance: String = ""
    private var keyword: String = ""

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

    fun setFoodtye(foodType : List<String>){ this.foodType = foodType }
    fun setPrice(price : List<String>){ this.price = price }
    fun setRating(rating : List<String>){ this.rating = rating }
    fun setDistance(distance : String){ this.distance = distance }
    fun setKeyword(keyword : String){ this.keyword = keyword }


    suspend fun findThisArea() {
        val filter = Filter()
        filter.prices = price
        filter.ratings = rating
        filter.distances = distance
        filter.keyword = keyword
        filter.restaurantTypes = foodType
        search(filter)
    }

    suspend fun findFilter() {
        val filter = Filter()
        filter.prices = price
        filter.ratings = rating
        filter.distances = distance
        filter.keyword = keyword
        filter.restaurantTypes = foodType
        search(filter)
    }


}