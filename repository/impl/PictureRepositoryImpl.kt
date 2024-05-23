package com.sarang.torang.di.repository.repository.impl

import com.sarang.torang.Picture
import com.sarang.torang.data.dao.PictureDao
import com.sarang.torang.data.entity.ReviewImageEntity
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.repository.PicturesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PicturesRepositoryImpl @Inject constructor(
    private val restaurantService: ApiRestaurant,
    private val pictureDao: PictureDao,
) :
    PicturesRepository {
    override suspend fun getPictures(restaurantId: Int): ArrayList<Picture> {
        return restaurantService.getPictures(HashMap<String, String>().apply {
            put("restaurant_id", restaurantId.toString())
        })
    }

    override fun getFeedPictureFlow(reviewId: Int): Flow<List<ReviewImageEntity>> {
        return pictureDao.getFeedImageFlow(reviewId)
    }

    override suspend fun getFeedPicture(reviewId: Int): List<ReviewImageEntity> {
        return pictureDao.getFeedImage(reviewId)
    }

    override suspend fun getImagesByRestaurantId(restaurantId: Int): List<ReviewImageEntity> {
        return pictureDao.getFeedImageByRestaurantId(restaurantId)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PictureRepositoryModule {
    @Binds
    abstract fun bindPictureRepositoryModule(pictureRepositoryImpl: PicturesRepositoryImpl)
            : PicturesRepository
}