package com.sarang.torang.di.repository.api

import com.sarang.torang.api.ApiJoin
import com.sarang.torang.api.ApiLogin
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiJoinModule {
    @Singleton
    @Provides
    fun provideApiLogin(
        apiLogin: ProductApiJoin,
//        apiLogin: FakeApiJoin
    ): ApiJoin {
        return apiLogin.create()
    }
}


@Singleton
class ProductApiJoin @Inject constructor(
    private val torangOkhttpClient: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule,
) {
    private var url = ApiUrl.join
    fun create(): ApiJoin {
        return retrofitModule.getRetrofit(torangOkhttpClient.getHttpClient(), url).create(
            ApiJoin::class.java
        )
    }
}


@Singleton
class FakeApiJoin @Inject constructor() {
    fun create(): ApiJoin {
        return object : ApiJoin {
            override suspend fun checkEmail(email: String, password: String): String {
                TODO("Not yet implemented")
            }

            override suspend fun confirmCode(
                token: String,
                confirmCode: String,
                name: String,
                email: String,
                password: String,
            ): Boolean {
                TODO("Not yet implemented")
            }

        }
    }
}