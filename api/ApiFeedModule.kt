package com.sarang.torang.di.repository.api

import com.sarang.torang.api.feed.ApiFeed
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
        apiFeed: LocalApiFeed,
    ): ApiFeed {
        return apiFeed.create()
    }
}

@Singleton
class LocalApiFeed @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule,
) {
    private var url = ApiUrl.feed
    fun create(): ApiFeed {
        return retrofitModule.getRetrofit(torangOkHttpClientImpl.getHttpClient(), url).create(
            ApiFeed::class.java
        )
    }
}