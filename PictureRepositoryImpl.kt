package com.sarang.torang.di.repository

import com.google.gson.Gson
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.core.database.dao.PictureDao
import com.sarang.torang.data.Picture
import com.sarang.torang.data.ReviewImage
import com.sarang.torang.repository.PicturesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PicturesRepositoryImpl @Inject constructor(
    private val restaurantService: ApiRestaurant,
    private val pictureDao: PictureDao,
) :
    PicturesRepository {
    override suspend fun getPictures(restaurantId: Int): List<Picture> {
        return restaurantService.getPictures(HashMap<String, String>().apply {
            put("restaurant_id", restaurantId.toString())
        }).map {
            Gson().fromJson<Picture>(Gson().toJson(this), Picture::class.java)
        }.toList()
    }

    override fun getFeedPictureFlow(reviewId: Int): Flow<List<ReviewImage>> {
        return pictureDao.findByIdFlow(reviewId).map { it.map { ReviewImage.from(it) } }
    }

    override suspend fun getFeedPicture(reviewId: Int): List<ReviewImage> {
        return pictureDao.findAllRestaurantById(reviewId).map { ReviewImage.from(it) }
    }

    override suspend fun getImagesByRestaurantId(restaurantId: Int): List<ReviewImage> {
        return pictureDao.findByRestaurantId(restaurantId).map { ReviewImage.from(it) }
    }

    override suspend fun getImagesByImageId(imageId: Int): List<ReviewImage> {
        return pictureDao.findById(imageId).map { ReviewImage.from(it) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PictureRepositoryModule {
    @Binds
    abstract fun bindPictureRepositoryModule(pictureRepositoryImpl: PicturesRepositoryImpl)
            : PicturesRepository
}