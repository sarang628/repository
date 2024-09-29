package com.sarang.torang.di.repository.api

import com.sarang.torang.api.ApiLogin
import com.sarang.torang.data.bottomsheet.User
import com.sarang.torang.data.remote.response.LoginApiModel
import com.sarang.torang.data.remote.response.UserApiModel
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
        apiLogin: ProductApiLogin,
//        apiLogin: FakeApiLogin
    ): ApiLogin {
        return apiLogin.create()
    }
}


@Singleton
class ProductApiLogin @Inject constructor(
    private val torangOkhttpClient: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule,
) {
    private var url = ApiUrl.login
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
            override suspend fun emailLogin(email: String, password: String): LoginApiModel {
                return LoginApiModel(
                    "123456", UserApiModel(
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


            override suspend fun sessionCheck(auth: String): Boolean {
                TODO("Not yet implemented")
            }
        }
    }
}