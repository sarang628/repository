package com.sryang.torang_repository.di.repository.api

import com.sryang.torang_repository.api.ApiReview
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class ApiReviewModule {
    @Singleton
    @Provides
    fun provideRemoveFeedService(
        reviewServiceProduct: ReviewServiceProductImpl
    ): ApiReview {
        return reviewServiceProduct.create()
    }
}


/**
 * 리뷰 서비스 Product
 */
@Singleton
class ReviewServiceLocalImpl @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://192.168.1.18:8081/"
    fun create(): ApiReview {
        return retrofitModule
//            .getRetrofit(torangOkHttpClientImpl.getUnsafeOkHttpClient(), url)
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), url)
            .create(ApiReview::class.java)
    }
}

@Singleton
class ReviewServiceProductImpl @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://sarang628.iptime.org:8081/"
    fun create(): ApiReview {
        return retrofitModule
//            .getRetrofit(torangOkHttpClientImpl.getUnsafeOkHttpClient(), url)
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), url)
            .create(ApiReview::class.java)
    }
}