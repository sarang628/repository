package com.sarang.torang.di.repository.api

import com.sarang.torang.api.ApiFeed
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class ApiFeedModule {
    @Singleton
    @Provides
    fun provideRemoteFeedService(
//        apiFeed: ProductApiFeed,
        apiFeed: LocalApiFeed
    ): ApiFeed {
        return apiFeed.create()
    }
}

/**
 * 피드 서비스 Product
 */
@Singleton
class ProductApiFeed @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://sarang628.iptime.org:8081/"
    fun create(): ApiFeed {
        return retrofitModule
//            .getRetrofit(torangOkHttpClientImpl.getUnsafeOkHttpClient(), url)
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), url)
            .create(ApiFeed::class.java)
    }
}

/**
 * 로컬 서버 피드 서비스
 */
@Singleton
class LocalApiFeed @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = ApiUrl.prod
    fun create(): ApiFeed {
        return retrofitModule.getRetrofit(torangOkHttpClientImpl.getHttpClient(), url).create(
            ApiFeed::class.java
        )
    }
}