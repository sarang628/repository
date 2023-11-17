package com.sryang.torang_repository.di.repository.service

import android.content.Context
import com.sryang.torang_repository.di.repository.api.RetrofitModule
import com.sryang.torang_repository.di.repository.api.TorangOkhttpClient
import com.sryang.torang_repository.preference.TorangPreference
import com.sryang.torang_repository.api.ApiReport
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 실서버 신고 서비스
 */
@Singleton
class ProductReportService @Inject constructor(
    private val torangOkHttpClientImpl: TorangOkhttpClient,
    private val retrofitModule: RetrofitModule
) {
    private var url = "http://sarang628.iptime.org:8080/"

    //    private var url = "https://www.vrscoo.com:8080/"
    fun create(): ApiReport {
        return retrofitModule.getRetrofit(torangOkHttpClientImpl.getHttpClient(), url).create(
            ApiReport::class.java
        )
    }
}

/**
 * 로컬 서버 신고 서비스
 */
class LocalReportService {

    var API_URL = "http://10.0.2.2:8080/"

    private fun getHttpClient(context: Context): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.HEADERS
        logger.level = HttpLoggingInterceptor.Level.BODY
        httpClient.addInterceptor(logger)
        httpClient.writeTimeout(10, TimeUnit.SECONDS)
        httpClient.connectTimeout(10, TimeUnit.SECONDS)
        httpClient.writeTimeout(10, TimeUnit.SECONDS)
        httpClient.readTimeout(10, TimeUnit.SECONDS)

        httpClient.addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("User-Agent", "android")
                .header("accessToken", "")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }

        return httpClient.build()
    }

    private fun getRetrofit(httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(httpClient)
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun create(context: Context): ApiReport {
        return getRetrofit(getHttpClient(context = context)).create(ApiReport::class.java)
    }
}

@InstallIn(SingletonComponent::class)
@Module
class ReportServiceModule {

    @Singleton
    @Provides
    fun provideReportService(productReportService: ProductReportService): ApiReport {
        return productReportService.create()
    }
}
