package com.sryang.torang_repository.di.repository.api

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sryang.torang_repository.api.ApiRestaurant
import com.sryang.torang_repository.data.Filter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class ApiRestaurantModule {
    @Singleton
    @Provides
    fun provideRemoteFeedService(
        apiRestaurant: ProductApiRestaurant
//        apiRestaurant: LocalApiRestaurant
    ): ApiRestaurant {
        return apiRestaurant.create()
    }
}

@Singleton
class ProductApiRestaurant @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://sarang628.iptime.org:8081/"
    fun create(): ApiRestaurant {
        return retrofitModule
//            .getRetrofit(torangOkHttpClientImpl.getUnsafeOkHttpClient(), url)
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), url)
            .create(ApiRestaurant::class.java)
    }
}

@Singleton
class LocalApiRestaurant @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://169.254.145.170:8081/"
    fun create(): ApiRestaurant {
        return retrofitModule
//            .getRetrofit(torangOkHttpClientImpl.getUnsafeOkHttpClient(), url)
            .getRetrofit(torangOkHttpClientImpl.getHttpClient(), url)
            .create(ApiRestaurant::class.java)
    }
}

@Composable
fun ApiRestaurantTest(apiRestaurant: ApiRestaurant) {
    val coroutine = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    Column {
        Button(onClick = {
            coroutine.launch {
                try {
                    val result = apiRestaurant.getRestaurantDetail(1)
                    text = result.toString()
                } catch (e: HttpException) {
                    val responseBody = e.response()?.errorBody()
                    text = responseBody?.string() ?: "알 수 없는 오류가 발생했습니다."
                    Log.e("TestApiRestaurant", text)

                } catch (e: Exception) {
                    Log.e("TestApiRestaurant", e.toString())
                }
            }
        }) {

        }
        Button(onClick = {
            coroutine.launch {
                val result = apiRestaurant.getFilterRestaurant(
                    Filter()
                )
                text = result.toString()
            }
        }) {

        }
        Text(text = text, Modifier.verticalScroll(rememberScrollState()))
    }

}