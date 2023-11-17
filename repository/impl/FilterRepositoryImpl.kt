package com.sryang.torang_repository.di.repository.repository.impl

import com.sryang.torang_repository.data.Distances
import com.sryang.torang_repository.data.Filter
import com.sryang.torang_repository.data.Prices
import com.sryang.torang_repository.data.SearchType
import com.sryang.torang_repository.repository.FilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterRepositoryImpl @Inject constructor() : FilterRepository {

    var filter = MutableStateFlow(
        Filter(
            SearchType.AROUND,
            ratings = ArrayList(),
            restaurantTypes = ArrayList()
        )
    )

    override fun getCurrentFilter(): Flow<Filter> {
        return filter
    }

    override suspend fun selectRestaurantType(food: String) {
    }

    override suspend fun selectPrice(price: Prices) {
//        filter.update {
//            if (it.prices == price)
//                it.copy(prices = Prices.NONE)
//            else
//                it.copy(prices = price)
//        }
    }

    override suspend fun selectRatings(ratings: String) {
//        filter.update {
//            val list = ArrayList<Ratings>()
//            list.addAll(it.ratings)
//            if (list.contains(ratings))
//                list.remove(ratings)
//            else
//                list.add(ratings)
//            Logger.d("filter copy")
//            it.copy(ratings = list)
//        }
    }

    override suspend fun selectDistance(distances: Distances) {
//        filter.update {
//            if (it.distances == distances) {
//                it.copy(
//                    distances = Distances.NONE
//                )
//            } else {
//                it.copy(
//                    distances = distances
//                )
//            }
//        }
    }

    override suspend fun getFilter(): Filter {
        return filter.value
    }
}