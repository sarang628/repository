package com.sryang.torang_repository.di.repository.repository.impl

import android.content.Context
import com.sryang.torang_repository.api.ApiReview
import com.sryang.torang_repository.data.dao.RestaurantDao
import com.sryang.torang_repository.data.remote.response.RemoteFeed
import com.sryang.torang_repository.repository.ReviewRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val apiReview: ApiReview,
    private val restaurantDao: RestaurantDao
) : ReviewRepository {

    private val mapClick = MutableStateFlow<Boolean>(false)

    override suspend fun getReviews(restaurantId: Int): List<RemoteFeed> {
        return apiReview.getReviews(restaurantId)
    }
}