package com.sarang.torang.di.repository.api

import com.sarang.torang.api.ApiAlarm
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class ApiAlarmModule {
    @Singleton
    @Provides
    fun provideRemoteFeedService(
        apiAlarm: ProductApiAlarm,
        //apiFeed: LocalApiAlarm
    ): ApiAlarm {
        return apiAlarm.create()
    }
}

@Singleton
class ProductApiAlarm @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://sarang628.iptime.org:8081/"
    fun create(): ApiAlarm {
        return retrofitModule
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), url)
            .create(ApiAlarm::class.java)
    }
}

@Singleton
class LocalApiAlarm @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://192.168.0.14:8081/"
    fun create(): ApiAlarm {
        return retrofitModule.getRetrofit(torangOkHttpClientImpl.getHttpClient(), url).create(
            ApiAlarm::class.java
        )
    }
}