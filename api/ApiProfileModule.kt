package com.sarang.torang.di.repository.api

import com.sarang.torang.api.ApiProfile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class ApiProfileModule {
    @Singleton
    @Provides
    fun provideRemoteFeedService(
        apiProfile: ProductApiProfile
//        apiProfile: LocalApiProfile
    ): ApiProfile {
        return apiProfile.create()
    }
}

@Singleton
class ProductApiProfile @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://sarang628.iptime.org:8081/"
    fun create(): ApiProfile {
        return retrofitModule
//            .getRetrofit(torangOkHttpClientImpl.getUnsafeOkHttpClient(), url)
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), url)
            .create(ApiProfile::class.java)
    }
}

@Singleton
class LocalApiProfile @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://192.168.1.216:8081/"
    fun create(): ApiProfile {
        return retrofitModule
//            .getRetrofit(torangOkHttpClientImpl.getUnsafeOkHttpClient(), url)
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), url)
            .create(ApiProfile::class.java)
    }
}