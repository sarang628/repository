package com.sarang.torang.di.repository.api

import com.sarang.torang.api.ApiAlarm
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class ApiAlarmModule {
    @Singleton
    @Provides
    fun provideRemoteFeedService(
        torangOkHttpClientImpl: TorangOkhttpClient,
        retrofitModule: RetrofitModule,
    ): ApiAlarm {
        return retrofitModule
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), ApiUrl.alarm)
            .create(ApiAlarm::class.java)
    }
}