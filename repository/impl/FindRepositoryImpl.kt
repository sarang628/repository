package com.sarang.torang.di.repository.repository.impl

import android.util.Log
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.data.Restaurant
import com.sarang.torang.data.remote.response.FilterApiModel
import com.sarang.torang.data.remote.response.RestaurantResponseDto
import com.sarang.torang.repository.FindRepository
import com.sarang.torang.repository.MapRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindRepositoryImpl @Inject constructor(
    val apiRestaurant: ApiRestaurant,
    val mapRepository: MapRepository
) : FindRepository {
    private var _restaurants : MutableStateFlow<List<Restaurant>> = MutableStateFlow(listOf())
    private var _foodType: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    private var _price: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    private var _rating: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    private var _distance: MutableStateFlow<String> = MutableStateFlow("")
    private var _keyword: MutableStateFlow<String> = MutableStateFlow("")
    override var restaurants : StateFlow<List<Restaurant>> = _restaurants
    private var _selectedRestaurant : MutableStateFlow<Restaurant> = MutableStateFlow(Restaurant())
    var selectedRestaurant : StateFlow<Restaurant> = _selectedRestaurant
    private val foodType: StateFlow<List<String>> = _foodType
    private val price: StateFlow<List<String>> = _price
    private val rating: StateFlow<List<String>> = _rating
    private val distance: StateFlow<String> = _distance
    private val keyword: StateFlow<String> = _keyword

    suspend fun setDistance(distance : String){ if(distance == this.distance.value) this._distance.emit("") else this._distance.emit(distance)}
    suspend fun setKeyword(keyword : String){ this._keyword.emit(keyword) }
    fun getFoodType(): StateFlow<List<String>> { return foodType }
    fun getPrices(): StateFlow<List<String>> { return price }
    fun getRatings(): StateFlow<List<String>> { return rating }
    fun getDistances(): StateFlow<String> { return distance }

    /**
     * 맵에서 포인트 클릭 시 카드 스와이프 발생하여 중복 선택
     * 중복 선택은 문제가 안되는데
     * 카드가 가려저 있는 상태에서 클릭하면
     * 카드가 한 장씩 이벤트 발생해서맵 포인터가 다른 음식점으로 계속 이동 함.
     */
    var blockCardSwipeEvent = false

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
        val filter = FilterApiModel()
        filter.searchType = "BOUND"
        filter.prices = price.value
        filter.ratings = rating.value
        filter.distances = distance.value
        if(distance.value == "") filter.distances = null
        filter.keyword = keyword.value
        filter.restaurantTypes = foodType.value
        filter.north = mapRepository.getNElon()
        filter.east = mapRepository.getNElat()
        filter.south = mapRepository.getSWlon()
        filter.west = mapRepository.getSWlat()
        search(filter)
    }

    override suspend fun findFilter() {
        val filter = FilterApiModel()
        filter.prices = price.value
        filter.ratings = rating.value
        filter.distances = distance.value
        if(distance.value == "") filter.distances = null
        filter.keyword = keyword.value
        filter.restaurantTypes = foodType.value
        search(filter)
    }

    override suspend fun search(filter: FilterApiModel) {
        try {
            Log.d("__FindRepositoryImpl", "restaurant filter search: $filter")
            _restaurants.emit(apiRestaurant.getFilterRestaurant(filter).map {
                it.toEntity()
            })
        }
        catch (e : HttpException){
            Log.e("__FindRepositoryImpl", e.response()?.errorBody()?.string().toString())
        }
        catch (e : Exception){
            Log.e("__FindRepositoryImpl", e.toString())
        }
    }

    suspend fun selectRestaurantFromMarker(restaurantId: Int) {
        Log.d("__FindRepositoryImpl", "selectRestaurantFromMarker: ${restaurantId}")
        _restaurants.value.firstOrNull { it.restaurantId == restaurantId }?.let {
            _selectedRestaurant.emit(it)
        }
        blockCardSwipeEvent = true
        delay(1000)
        blockCardSwipeEvent = false
    }

    suspend fun selectRestaurantFromSwipe(restaurantId: Int) {
        if(blockCardSwipeEvent){
            Log.w("__FindRepositoryImpl", "block card swipe event restaurantId : ${restaurantId}")
            return
        }
        Log.d("__FindRepositoryImpl", "selectRestaurantFromSwipe: ${restaurantId}")
        _restaurants.value.firstOrNull { it.restaurantId == restaurantId }?.let {
            _selectedRestaurant.emit(it)
        }
    }


    suspend fun selectRestaurant(restaurantId: Int) {
        Log.d("__FindRepositoryImpl", "selectRestaurant: ${restaurantId}")
        _restaurants.value.firstOrNull { it.restaurantId == restaurantId }?.let {
            _selectedRestaurant.emit(it)
        }
    }

    fun RestaurantResponseDto.toEntity() : Restaurant{
        return Restaurant(restaurantId = restaurantId ?: -1, restaurantName = restaurantName ?: "null", address = address ?: "null", lat = lat ?: 0.0, lon = lon ?: 0.0, rating = rating ?: 0f, tel = tel ?:"null", prices = prices ?: "null", restaurantType = restaurantType ?: "null", regionCode = regionCode ?: 0, reviewCount = reviewCount ?: 0, site = site ?: "null", website = website ?: "null", imgUrl1 = imgUrl1 ?: "null", restaurantTypeCd = restaurantTypeCd ?: "null")
    }
}