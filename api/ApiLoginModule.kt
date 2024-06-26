package com.sarang.torang.di.repository.api

import com.sarang.torang.api.ApiLogin
import com.sarang.torang.data.Filter
import com.sarang.torang.data.Restaurant
import com.sarang.torang.data.User
import com.sarang.torang.data.remote.response.LoginResponse
import com.sarang.torang.data.remote.response.RemoteUser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiLoginModule {
    @Singleton
    @Provides
    fun provideApiLogin(
//        apiLogin: ProductApiLogin,
        apiLogin: LocalApiLogin
//        apiLogin: FakeApiLogin
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
    private var url = ApiUrl.local
    fun create(): ApiLogin {
        return retrofitModule.getRetrofit(torangOkhttpClient.getHttpClient(), url).create(
            ApiLogin::class.java
        )
    }
}

@Singleton
class FakeApiLogin @Inject constructor() {
    fun create(): ApiLogin {
        return object : ApiLogin {
            override suspend fun checkEmail(email: String, password: String): String {
                TODO("Not yet implemented")
            }

            override suspend fun confirmCode(
                token: String,
                confirmCode: String,
                name: String,
                email: String,
                password: String
            ): Boolean {
                TODO("Not yet implemented")
            }

            override suspend fun emailLogin(email: String, password: String): LoginResponse {
                return LoginResponse(
                    "123456", RemoteUser(
                        userId = 31,
                        userName = "name",
                        createDate = "",
                        email = "",
                        follow = 0,
                        follower = 0,
                        following = 0,
                        loginPlatform = "",
                        post = 0,
                        profilePicUrl = ""
                    )
                )
            }

            override suspend fun facebook_login(accessToken: String): Response<User> {
                TODO("Not yet implemented")
            }

            override suspend fun join(filter: Filter): Response<ArrayList<Restaurant>> {
                TODO("Not yet implemented")
            }

            override suspend fun sessionCheck(auth: String): Boolean {
                TODO("Not yet implemented")
            }
        }
    }
}