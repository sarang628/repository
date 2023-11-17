package com.sryang.torang_repository.di.repository.api

import android.content.Context
import com.sryang.torang_repository.preference.TorangPreference
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface TorangOkhttpClient {
    fun getHttpClient(): OkHttpClient
    fun getUnsafeOkHttpClient(): OkHttpClient
}

@Singleton
class TorangOkHttpClientImpl @Inject constructor(@ApplicationContext val context: Context) :
    TorangOkhttpClient {

    override fun getHttpClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.HEADERS
        logger.level = HttpLoggingInterceptor.Level.BODY
        httpClient.addInterceptor(logger)
        httpClient.writeTimeout(10, TimeUnit.SECONDS)
        httpClient.connectTimeout(10, TimeUnit.SECONDS)
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

    override fun getUnsafeOkHttpClient(): OkHttpClient {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts: Array<TrustManager> = arrayOf<TrustManager>(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )

            // Install the all-trusting trust manager
            val sslContext: SSLContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
            val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
            builder.writeTimeout(100, TimeUnit.SECONDS)
            builder.connectTimeout(100, TimeUnit.SECONDS)
            builder.readTimeout(100, TimeUnit.SECONDS)
            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class OkHttpClientModule {
    @Binds
    abstract fun provideOkhttpClientModule(torangOkHttpClientImpl: TorangOkHttpClientImpl): TorangOkhttpClient
}