# SDS 03 -- Hilt Dependency Injection Modules

**Parent:** [03_SDS.md](../03_SDS.md)

---

## 1. Module Overview

| Module | Scope | Provides |
|--------|-------|----------|
| `AppModule` | Singleton | Application context, shared utilities |
| `NetworkModule` | Singleton | OkHttpClient, Retrofit, all API services |
| `DatabaseModule` | Singleton | Room database, all DAOs |
| `RepositoryModule` | Singleton | Binds repository interfaces to implementations |
| `SecurityModule` | Singleton | EncryptedSharedPreferences, TokenManager, BiometricManager |

---

## 2. AppModule

```kotlin
package com.dynapharm.ownerhub.di

import android.content.Context
import com.dynapharm.ownerhub.util.CurrencyFormatter
import com.dynapharm.ownerhub.util.DateFormatter
import com.dynapharm.ownerhub.util.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides @Singleton
    fun provideCurrencyFormatter(): CurrencyFormatter = CurrencyFormatter()

    @Provides @Singleton
    fun provideDateFormatter(): DateFormatter = DateFormatter()

    @Provides @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor =
        NetworkMonitor(context)
}
```

---

## 3. NetworkModule

```kotlin
package com.dynapharm.ownerhub.di

import com.dynapharm.ownerhub.BuildConfig
import com.dynapharm.ownerhub.data.api.*
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

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        isLenient = true
    }

    @Provides @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.ENABLE_LOGGING)
                HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

    @Provides @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        val builder = CertificatePinner.Builder()
        val pins = BuildConfig.CERTIFICATE_PINS
        if (pins.isNotBlank()) {
            pins.split(";").forEach { builder.add("app.dynapharm-dms.com", it.trim()) }
        }
        return builder.build()
    }

    @Provides @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tenantInterceptor: TenantInterceptor,
        tokenRefreshAuthenticator: TokenRefreshAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor,
        certificatePinner: CertificatePinner
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(tenantInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenRefreshAuthenticator)
        .certificatePinner(certificatePinner)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    // --- API Service Providers ---
    @Provides @Singleton
    fun provideAuthApi(r: Retrofit): AuthApiService = r.create(AuthApiService::class.java)
    @Provides @Singleton
    fun provideDashboardApi(r: Retrofit): DashboardApiService = r.create(DashboardApiService::class.java)
    @Provides @Singleton
    fun provideFranchiseApi(r: Retrofit): FranchiseApiService = r.create(FranchiseApiService::class.java)
    @Provides @Singleton
    fun provideReportApi(r: Retrofit): ReportApiService = r.create(ReportApiService::class.java)
    @Provides @Singleton
    fun provideApprovalApi(r: Retrofit): ApprovalApiService = r.create(ApprovalApiService::class.java)
    @Provides @Singleton
    fun provideProfileApi(r: Retrofit): ProfileApiService = r.create(ProfileApiService::class.java)
}
```

---

## 4. DatabaseModule

```kotlin
package com.dynapharm.ownerhub.di

import android.content.Context
import androidx.room.Room
import com.dynapharm.ownerhub.data.db.OwnerHubDatabase
import com.dynapharm.ownerhub.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OwnerHubDatabase =
        Room.databaseBuilder(context, OwnerHubDatabase::class.java, "ownerhub_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideFranchiseDao(db: OwnerHubDatabase): FranchiseDao = db.franchiseDao()
    @Provides fun provideDashboardKpiDao(db: OwnerHubDatabase): DashboardKpiDao = db.dashboardKpiDao()
    @Provides fun provideReportCacheDao(db: OwnerHubDatabase): ReportCacheDao = db.reportCacheDao()
    @Provides fun provideApprovalDao(db: OwnerHubDatabase): ApprovalDao = db.approvalDao()
    @Provides fun provideSyncQueueDao(db: OwnerHubDatabase): SyncQueueDao = db.syncQueueDao()
}
```

---

## 5. RepositoryModule

```kotlin
package com.dynapharm.ownerhub.di

import com.dynapharm.ownerhub.data.repository.*
import com.dynapharm.ownerhub.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository
    @Binds @Singleton abstract fun bindFranchiseRepository(impl: FranchiseRepositoryImpl): FranchiseRepository
    @Binds @Singleton abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository
    @Binds @Singleton abstract fun bindApprovalRepository(impl: ApprovalRepositoryImpl): ApprovalRepository
    @Binds @Singleton abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
```

---

## 6. SecurityModule

```kotlin
package com.dynapharm.ownerhub.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.dynapharm.ownerhub.util.BiometricManager
import com.dynapharm.ownerhub.util.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides @Singleton
    fun provideMasterKey(@ApplicationContext context: Context): MasterKey =
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    @Provides @Singleton
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context,
        masterKey: MasterKey
    ): SharedPreferences = EncryptedSharedPreferences.create(
        context, "ownerhub_secure_prefs", masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    @Provides @Singleton
    fun provideTokenManager(prefs: SharedPreferences): TokenManager = TokenManager(prefs)

    @Provides @Singleton
    fun provideBiometricManager(@ApplicationContext ctx: Context): BiometricManager =
        BiometricManager(ctx)
}
```

---

## 7. Interceptors (Constructor Injection)

### AuthInterceptor

```kotlin
package com.dynapharm.ownerhub.di

import com.dynapharm.ownerhub.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .apply {
                tokenManager.getAccessToken()?.let {
                    addHeader("Authorization", "Bearer $it")
                }
            }
            .build()
        return chain.proceed(request)
    }
}
```

### TenantInterceptor

```kotlin
package com.dynapharm.ownerhub.di

import com.dynapharm.ownerhub.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TenantInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = tokenManager.getActiveFranchiseId()?.let { id ->
            chain.request().newBuilder()
                .addHeader("X-Franchise-ID", id.toString())
                .build()
        } ?: chain.request()
        return chain.proceed(request)
    }
}
```

### TokenRefreshAuthenticator

```kotlin
package com.dynapharm.ownerhub.di

import com.dynapharm.ownerhub.data.api.AuthApiService
import com.dynapharm.ownerhub.data.dto.auth.RefreshTokenRequest
import com.dynapharm.ownerhub.util.TokenManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiService: dagger.Lazy<AuthApiService>
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("X-Retry-Auth") != null) return null

        return runBlocking {
            mutex.withLock {
                val currentToken = tokenManager.getAccessToken()
                val requestToken = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")

                // Another thread already refreshed
                if (currentToken != null && currentToken != requestToken) {
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .header("X-Retry-Auth", "true")
                        .build()
                }

                val refreshToken = tokenManager.getRefreshToken()
                    ?: return@runBlocking null

                try {
                    val result = authApiService.get()
                        .refreshToken(RefreshTokenRequest(refreshToken))
                    if (result.success && result.data != null) {
                        tokenManager.saveTokens(result.data.accessToken, result.data.refreshToken)
                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", "Bearer ${result.data.accessToken}")
                            .header("X-Retry-Auth", "true")
                            .build()
                    }
                } catch (_: Exception) { }

                tokenManager.clearTokens()
                null
            }
        }
    }
}
```

---

## 8. Hilt Application Class

```kotlin
package com.dynapharm.ownerhub

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class OwnerHubApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.ENABLE_LOGGING) Timber.plant(Timber.DebugTree())
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

---

## 9. Cross-References

| Topic | Document |
|-------|----------|
| Architecture layers | [01-architecture.md](01-architecture.md) |
| Gradle dependency versions | [02-gradle-config.md](02-gradle-config.md) |
| Security details | [05-security.md](05-security.md) |
| Networking layer | [06-networking.md](06-networking.md) |
| Offline sync (WorkManager) | [04-offline-sync.md](04-offline-sync.md) |
