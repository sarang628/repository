package com.sarang.torang.di.repository.api

import com.sarang.torang.api.ApiCommentLike
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class ApiCommentLikeModule {
    @Singleton
    @Provides
    fun provideRemoteFeedService(
        apiCommentLike: ProductApiCommentLike,
        //apiFeed: LocalApiAlarm
    ): ApiCommentLike {
        return apiCommentLike.create()
    }
}

@Singleton
class ProductApiCommentLike @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://sarang628.iptime.org:8081/"
    fun create(): ApiCommentLike {
        return retrofitModule
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), url)
            .create(ApiCommentLike::class.java)
    }
}

@Singleton
class LocalApiCommentLike @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://192.168.0.14:8081/"
    fun create(): ApiCommentLike {
        return retrofitModule.getRetrofit(torangOkHttpClientImpl.getHttpClient(), url).create(
            ApiCommentLike::class.java
        )
    }
}