package com.sryang.torang_repository.di.repository.api

import com.sryang.torang_repository.api.ApiAlarm
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
        apiAlarm: com.sryang.torang_repository.di.repository.api.ProductApiAlarm,
        //apiFeed: LocalApiAlarm
    ): ApiAlarm {
        return apiAlarm.create()
    }
}

@Singleton
class ProductApiAlarm @Inject constructor(
    private val torangOkHttpClientImpl: com.sryang.torang_repository.di.repository.api.TorangOkhttpClient,
    private val retrofitModule: com.sryang.torang_repository.di.repository.api.RetrofitModule
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
    private val torangOkHttpClientImpl: com.sryang.torang_repository.di.repository.api.TorangOkhttpClient,
    private val retrofitModule: com.sryang.torang_repository.di.repository.api.RetrofitModule
) {
    private var url = "http://192.168.0.14:8081/"
    fun create(): ApiAlarm {
        return retrofitModule.getRetrofit(torangOkHttpClientImpl.getHttpClient(), url).create(
            ApiAlarm::class.java
        )
    }
}