package com.sarang.torang.di.repository.api

import com.sarang.torang.api.ApiLike
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class ApiLikeModule {
    @Singleton
    @Provides
    fun provideRemoteFeedService(
//        apiLike: ProductApiLike,
        apiLike: LocalApiLike
    ): ApiLike {
        return apiLike.create()
    }
}

@Singleton
class ProductApiLike @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule,
) {
    private var url = ApiUrl.prod
    fun create(): ApiLike {
        return retrofitModule
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), url)
            .create(ApiLike::class.java)
    }
}

@Singleton
class LocalApiLike @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule,
) {
    private var url = ApiUrl.local
    fun create(): ApiLike {
        return retrofitModule.getRetrofit(torangOkHttpClientImpl.getHttpClient(), url).create(
            ApiLike::class.java
        )
    }
}