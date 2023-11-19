package com.sryang.torang_repository.di.repository.api

import com.sryang.torang_repository.api.ApiComment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class ApiCommentModule {
    @Singleton
    @Provides
    fun provideRemoteFeedService(
        apiComment: ProductApiComment,
//        apiComment: LocalApiComment
    ): ApiComment {
        return apiComment.create()
    }
}

/**
 * 피드 서비스 Product
 */
@Singleton
class ProductApiComment @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://sarang628.iptime.org:8081/"
    fun create(): ApiComment {
        return retrofitModule
//            .getRetrofit(torangOkHttpClientImpl.getUnsafeOkHttpClient(), url)
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), url)
            .create(ApiComment::class.java)
    }
}

/**
 * 로컬 서버 피드 서비스
 */
@Singleton
class LocalApiComment @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://172.20.10.2:8081/"
    fun create(): ApiComment {
        return retrofitModule.getRetrofit(torangOkHttpClientImpl.getHttpClient(), url).create(
            ApiComment::class.java
        )
    }
}

