package com.sryang.torang_repository.di.repository.api

import com.sryang.torang_repository.api.ApiFeed
import com.sryang.torang_repository.api.ApiFilter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class ApiFilterModule {
    @Singleton
    @Provides
    fun provideRemoteFeedService(
        service: ProductApiFilter,
//        service: LocalApiFilter
    ): ApiFilter {
        return service.create()
    }
}

/**
 * 피드 서비스 Product
 */
@Singleton
class ProductApiFilter @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = ApiUrl.prod
    fun create(): ApiFilter {
        return retrofitModule
//            .getRetrofit(torangOkHttpClientImpl.getUnsafeOkHttpClient(), url)
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), url)
            .create(ApiFilter::class.java)
    }
}

/**
 * 로컬 서버 피드 서비스
 */
@Singleton
class LocalApiFilter @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = ApiUrl.local
    fun create(): ApiFilter {
        return retrofitModule.getRetrofit(torangOkHttpClientImpl.getHttpClient(), url).create(
            ApiFilter::class.java
        )
    }
}