package com.sarang.torang.di.repository

import android.text.TextUtils
import android.util.Log
import com.sarang.torang.api.ApiFilter
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.data.Filter
import com.sarang.torang.data.RestaurantWithFiveImages
import com.sarang.torang.data.SearchType
import com.sarang.torang.data.remote.response.FilterApiModel
import com.sarang.torang.data.remote.response.RatingApiModel
import com.sarang.torang.di.repository.data.Distances
import com.sarang.torang.repository.FindRepository
import com.sarang.torang.repository.MapRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindRepositoryImpl @Inject constructor(
    val apiRestaurant: ApiRestaurant,
    val mapRepository: MapRepository,
    val apiFilter: ApiFilter
) : FindRepository {
    val tag = "__FindRepositoryImpl"
    private var _restaurants        : MutableStateFlow<List<RestaurantWithFiveImages>> = MutableStateFlow(listOf())
    private var _foodType           : MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    private var _price              : MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    private var _rating             : MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    private var _distance           : MutableStateFlow<Distances> = MutableStateFlow(Distances.NONE)
    private var _keyword            : MutableStateFlow<String> = MutableStateFlow("")
    private var _selectedRestaurant : MutableStateFlow<RestaurantWithFiveImages> = MutableStateFlow(RestaurantWithFiveImages())

    var selectedRestaurant          : StateFlow<RestaurantWithFiveImages> = _selectedRestaurant
    private val foodType            : StateFlow<List<String>> = _foodType
    private val price               : StateFlow<List<String>> = _price
    private val rating              : StateFlow<List<String>> = _rating
    private val distance            : StateFlow<Distances> = _distance
    private val keyword             : StateFlow<String> = _keyword

    override var restaurants        : Flow<List<RestaurantWithFiveImages>>
        = combine(_restaurants,
        _foodType,
            _price,
            _rating,
            _distance)
    { restaurants, foodType, price, rating, distance ->
        var restaurantList = restaurants
        if(foodType.isNotEmpty()) {
            restaurantList = restaurants.filter {
                foodType.map { it.lowercase() }
                        .contains(it.restaurant.restaurantType.lowercase())
            }
        }

        if(price.isNotEmpty()){
            restaurantList = restaurants.filter {
                price.contains(it.restaurant.prices)
            }
        }

        if(rating.isNotEmpty()){
            restaurantList = restaurants.filter { restaurant->
                rating.any { restaurant.restaurant.rating > it.length.toFloat()
                             && restaurant.restaurant.rating < (it.length.toFloat() + 0.9) }
            }
        }

        restaurantList
    }

    suspend fun setDistance(distance : String) {
        if(_distance.value.value == distance){
            _distance.emit(Distances.NONE)
        }
        else _distance.emit(value = Distances.findByString(distance))
    }
    override suspend fun setKeyword(keyword : String){ this._keyword.emit(keyword) }
    override fun getFoodType(): StateFlow<List<String>> { return foodType }
    override fun getPrices(): StateFlow<List<String>> { return price }
    override fun getRatings(): StateFlow<List<String>> { return rating }
    fun getDistances(): StateFlow<Distances> { return distance }

    /**
     * 맵에서 포인트 클릭 시 카드 스와이프 발생하여 중복 선택
     * 중복 선택은 문제가 안되는데
     * 카드가 가려저 있는 상태에서 클릭하면
     * 카드가 한 장씩 이벤트 발생해서맵 포인터가 다른 음식점으로 계속 이동 함.
     */
    var blockCardSwipeEvent = false

    override suspend fun setFoodType(foodType : String){
        if(this.foodType.value.contains(foodType)) {
            this._foodType.emit(this.foodType.value.filter { it != foodType })
        }
        else {
            this._foodType.emit(this.foodType.value + foodType )
        }
    }
    override suspend fun setPrice(price : String){
        if(this.price.value.contains(price)) {
            this._price.emit(this.price.value.filter { it != price })
        }
        else {
            this._price.emit(this.price.value + price )
        }
    }
    override suspend fun setRating(rating : String){
        if(this.rating.value.contains(rating)) {
            this._rating.emit(this.rating.value.filter { it != rating })
        }
        else {
            this._rating.emit(this.rating.value + rating )
        }
    }


    override suspend fun findThisArea() {
        val filter = Filter(
            searchType = SearchType.BOUND,
            prices = price.value,
            ratings = rating.value,
            distances = TextUtils.join(",", listOf(Distances.NONE)),
            keyword = keyword.value,
            restaurantTypes = foodType.value,
            northEastLon = mapRepository.getNElon(),
            northEastLat = mapRepository.getNElat(),
            southWestLon = mapRepository.getSWlon(),
            southWestLat = mapRepository.getSWlat(),
        )
        search(filter)
    }

    override suspend fun findFilter() {
        val filter = Filter(
            prices = price.value,
            ratings = rating.value,
            distances = distance.value.name,
            keyword = keyword.value,
            restaurantTypes = foodType.value,
        )
        search(filter)
    }

    override suspend fun search(filter: Filter) {
        try {
            Log.d(tag, "restaurant filter search: $filter")
            val result = if(filter.searchType == SearchType.BOUND) apiFilter.boundRestaurant(filter.toApiModel())
                         else apiFilter.aroundRestaurant(filter.toApiModel())

            val list = result.restaurants.map {
                if (it != null) RestaurantWithFiveImages.from(it)
                else RestaurantWithFiveImages()
            }
            _restaurants.emit(list)
        }
        catch (e : HttpException)   { Log.e(tag, e.response()?.errorBody()?.string().toString()) }
        catch (e : Exception)       { Log.e(tag, e.toString()) }
    }

    fun Filter.toApiModel() : FilterApiModel{
        return FilterApiModel(
            searchType = this.searchType.name,
            keyword = this.keyword,
            distances = this.distances,
            prices = this.prices,
            restaurantTypes = this.restaurantTypes,
            ratings = this.ratings?.map { it.toRatingApiModel() },
            latitude = this.lat,
            longitude = this.lon,
            northEastLat = this.northEastLat,
            northEastLon = this.northEastLon,
            southWestLat = this.southWestLat,
            southWestLon = this.southWestLon,
        )
    }

    fun String.toRatingApiModel() : RatingApiModel{
        return when(this){
            "*" -> RatingApiModel.ONE
            "**" -> RatingApiModel.TWO
            "***" -> RatingApiModel.THREE
            "****" -> RatingApiModel.FOUR
            "*****" -> RatingApiModel.FIVE
            else -> {
                RatingApiModel.ONE
            }
        }
    }

    override suspend fun selectRestaurantFromMarker(restaurantId: Int) {
        _restaurants.value.firstOrNull { it.restaurant.restaurantId == restaurantId }?.let {
            Log.d(tag, "selectRestaurantFromMarker: $restaurantId")
            _selectedRestaurant.emit(it)
        } ?: run {
            Log.e(tag, "failed select restaurant from select marker restaurantId : $restaurantId")
        }
        blockCardSwipeEvent = true
        delay(1000)
        blockCardSwipeEvent = false
    }

    override suspend fun selectRestaurantFromSwipe(restaurantId: Int) {
        if(blockCardSwipeEvent){ Log.w(tag, "block card swipe event restaurantId : ${restaurantId}"); return }
        Log.d(tag, "selectRestaurantFromSwipe: $restaurantId")
        _restaurants.value.firstOrNull { it.restaurant.restaurantId == restaurantId }?.let { _selectedRestaurant.emit(it) }
    }


    override suspend fun selectRestaurant(restaurantId: Int) {
        Log.d(tag, "selectRestaurant: $restaurantId")
        _restaurants.value.firstOrNull { it.restaurant.restaurantId == restaurantId }?.let { _selectedRestaurant.emit(it) }
    }
}