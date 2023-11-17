package com.sryang.torang_repository.di.repository.api

import com.sryang.torang_repository.api.ApiFeed
import com.sryang.torang_repository.api.ApiLogin
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiLoginModule {
    @Singleton
    @Provides
    fun provideApiLogin(
        apiLogin: ProductApiLogin,
//        apiLogin: LocalApiLogin
    ): ApiLogin {
        return apiLogin.create()
    }
}


@Singleton
class ProductApiLogin @Inject constructor(
    private val torangOkhttpClient: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://sarang628.iptime.org:8081/"
    fun create(): ApiLogin {
        return retrofitModule.getRetrofit(torangOkhttpClient.getHttpClient(), url).create(
            ApiLogin::class.java
        )
    }
}

@Singleton
class LocalApiLogin @Inject constructor(
    private val torangOkhttpClient: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://192.168.0.14:8081/"
    fun create(): ApiLogin {
        return retrofitModule.getRetrofit(torangOkhttpClient.getHttpClient(), url).create(
            ApiLogin::class.java
        )
    }
}