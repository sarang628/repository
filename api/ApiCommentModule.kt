package com.sarang.torang.di.repository.api

import com.sarang.torang.api.ApiComment
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
    private var url = ApiUrl.comment
    fun create(): ApiComment {
        return retrofitModule
//            .getRetrofit(torangOkHttpClientImpl.getUnsafeOkHttpClient(), url)
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), url)
            .create(ApiComment::class.java)
    }
}