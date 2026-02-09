package com.dynapharm.owner.di

import com.dynapharm.owner.BuildConfig
import com.dynapharm.owner.data.remote.interceptor.AuthInterceptor
import com.dynapharm.owner.data.remote.interceptor.FranchiseContextInterceptor
import com.dynapharm.owner.data.remote.interceptor.TokenRefreshAuthenticator
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module providing network-related dependencies.
 * Provides OkHttpClient, Retrofit, and JSON serialization.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides JSON configuration for kotlinx.serialization.
     * Configured to be lenient and ignore unknown keys.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    /**
     * Provides HTTP logging interceptor.
     * Logging is enabled based on BuildConfig.ENABLE_LOGGING.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.ENABLE_LOGGING) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    /**
     * Provides certificate pinner for SSL pinning.
     * Pins are loaded from BuildConfig.CERTIFICATE_PINS.
     */
    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        val builder = CertificatePinner.Builder()
        val pins = BuildConfig.CERTIFICATE_PINS

        if (pins.isNotBlank()) {
            // Extract hostname from API_BASE_URL
            val hostname = BuildConfig.API_BASE_URL
                .removePrefix("https://")
                .removePrefix("http://")
                .trimEnd('/')
                .split("/")
                .first()

            // Add each pin (separated by semicolon)
            pins.split(";").forEach { pin ->
                if (pin.isNotBlank()) {
                    builder.add(hostname, pin.trim())
                }
            }
        }

        return builder.build()
    }

    /**
     * Provides configured OkHttpClient.
     * Includes authentication, franchise context, token refresh, and logging.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        franchiseContextInterceptor: FranchiseContextInterceptor,
        tokenRefreshAuthenticator: TokenRefreshAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor,
        certificatePinner: CertificatePinner
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(franchiseContextInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenRefreshAuthenticator)
        .certificatePinner(certificatePinner)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Provides configured Retrofit instance.
     * Uses kotlinx.serialization for JSON conversion.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    /**
     * Provides AuthApiService instance.
     */
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): com.dynapharm.owner.data.remote.api.AuthApiService =
        retrofit.create(com.dynapharm.owner.data.remote.api.AuthApiService::class.java)

    /**
     * Provides DashboardApiService instance.
     */
    @Provides
    @Singleton
    fun provideDashboardApiService(retrofit: Retrofit): com.dynapharm.owner.data.remote.api.DashboardApiService =
        retrofit.create(com.dynapharm.owner.data.remote.api.DashboardApiService::class.java)
}
