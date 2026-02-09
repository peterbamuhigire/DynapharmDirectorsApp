# Phase 1 -- Section 03: Core Infrastructure

**[Back to Phase 1 README](./README.md)** | **Package:** `com.dynapharm.owner`

---

## 1. Hilt DI Modules

### 1.1 AppModule

```kotlin
package com.dynapharm.owner.di

import android.content.Context
import com.dynapharm.owner.util.NetworkMonitor
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
    fun provideNetworkMonitor(@ApplicationContext ctx: Context): NetworkMonitor =
        NetworkMonitor(ctx)
}
```

### 1.2 NetworkModule

```kotlin
package com.dynapharm.owner.di

import com.dynapharm.owner.BuildConfig
import com.dynapharm.owner.data.api.*
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
        ignoreUnknownKeys = true; coerceInputValues = true
        encodeDefaults = true; isLenient = true
    }

    @Provides @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenRefreshAuthenticator: TokenRefreshAuthenticator,
        json: Json
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.ENABLE_LOGGING) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        val pinBuilder = CertificatePinner.Builder()
        val pins = BuildConfig.CERTIFICATE_PINS
        if (pins.isNotBlank()) {
            pins.split(";").forEach { pinBuilder.add("app.dynapharm-dms.com", it.trim()) }
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .authenticator(tokenRefreshAuthenticator)
            .certificatePinner(pinBuilder.build())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides @Singleton fun provideAuthApi(r: Retrofit): AuthApiService = r.create(AuthApiService::class.java)
    @Provides @Singleton fun provideDashboardApi(r: Retrofit): DashboardApiService = r.create(DashboardApiService::class.java)
    @Provides @Singleton fun provideFranchiseApi(r: Retrofit): FranchiseApiService = r.create(FranchiseApiService::class.java)
    @Provides @Singleton fun provideReportApi(r: Retrofit): ReportApiService = r.create(ReportApiService::class.java)
    @Provides @Singleton fun provideApprovalApi(r: Retrofit): ApprovalApiService = r.create(ApprovalApiService::class.java)
    @Provides @Singleton fun provideProfileApi(r: Retrofit): ProfileApiService = r.create(ProfileApiService::class.java)
}
```

### 1.3 DatabaseModule

```kotlin
package com.dynapharm.owner.di

import android.content.Context
import androidx.room.Room
import com.dynapharm.owner.data.db.DynapharmOwnerDatabase
import com.dynapharm.owner.data.db.dao.*
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
    fun provideDatabase(@ApplicationContext ctx: Context): DynapharmOwnerDatabase =
        Room.databaseBuilder(ctx, DynapharmOwnerDatabase::class.java, "dynapharm_owner.db")
            .fallbackToDestructiveMigration().build()

    @Provides fun franchiseDao(db: DynapharmOwnerDatabase): FranchiseDao = db.franchiseDao()
    @Provides fun dashboardKpiDao(db: DynapharmOwnerDatabase): DashboardKpiDao = db.dashboardKpiDao()
    @Provides fun reportCacheDao(db: DynapharmOwnerDatabase): ReportCacheDao = db.reportCacheDao()
    @Provides fun approvalDao(db: DynapharmOwnerDatabase): ApprovalDao = db.approvalDao()
    @Provides fun syncQueueDao(db: DynapharmOwnerDatabase): SyncQueueDao = db.syncQueueDao()
}
```

### 1.4 RepositoryModule

```kotlin
package com.dynapharm.owner.di

import com.dynapharm.owner.data.repository.*
import com.dynapharm.owner.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuthRepo(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindDashboardRepo(impl: DashboardRepositoryImpl): DashboardRepository
    @Binds @Singleton abstract fun bindFranchiseRepo(impl: FranchiseRepositoryImpl): FranchiseRepository
    @Binds @Singleton abstract fun bindReportRepo(impl: ReportRepositoryImpl): ReportRepository
    @Binds @Singleton abstract fun bindApprovalRepo(impl: ApprovalRepositoryImpl): ApprovalRepository
    @Binds @Singleton abstract fun bindProfileRepo(impl: ProfileRepositoryImpl): ProfileRepository
}
```

---

## 2. AuthInterceptor

Attaches Bearer token, Accept header, and X-Franchise-ID to every request.

```kotlin
package com.dynapharm.owner.di

import com.dynapharm.owner.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
        tokenManager.getAccessToken()?.let {
            builder.addHeader("Authorization", "Bearer $it")
        }
        tokenManager.getActiveFranchiseId()?.let {
            builder.addHeader("X-Franchise-ID", it.toString())
        }
        return chain.proceed(builder.build())
    }
}
```

---

## 3. TokenRefreshAuthenticator

Handles 401 responses: refreshes the access token and retries. Uses a mutex so concurrent 401s trigger only one refresh.

```kotlin
package com.dynapharm.owner.di

import com.dynapharm.owner.data.api.AuthApiService
import com.dynapharm.owner.data.dto.auth.RefreshTokenRequest
import com.dynapharm.owner.util.TokenManager
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
                        .header("X-Retry-Auth", "true").build()
                }
                val refreshToken = tokenManager.getRefreshToken() ?: run {
                    tokenManager.clearAll(); return@runBlocking null
                }
                try {
                    val result = authApiService.get()
                        .refreshToken(RefreshTokenRequest(refreshToken))
                    if (result.success && result.data != null) {
                        tokenManager.saveTokens(
                            result.data.accessToken,
                            result.data.refreshToken ?: refreshToken
                        )
                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", "Bearer ${result.data.accessToken}")
                            .header("X-Retry-Auth", "true").build()
                    }
                } catch (_: Exception) { }
                tokenManager.clearAll(); null
            }
        }
    }
}
```

---

## 4. NetworkMonitor

Observes connectivity via `ConnectivityManager`. Exposes `StateFlow<Boolean>` for Compose and a synchronous `isCurrentlyOnline()`.

```kotlin
package com.dynapharm.owner.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(context: Context) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _isOnline = MutableStateFlow(check())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    val connectivityFlow: Flow<Boolean> = callbackFlow {
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(n: Network) { trySend(true); _isOnline.value = true }
            override fun onLost(n: Network) { trySend(false); _isOnline.value = false }
            override fun onCapabilitiesChanged(n: Network, caps: NetworkCapabilities) {
                val ok = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(ok); _isOnline.value = ok
            }
        }
        val req = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        cm.registerNetworkCallback(req, cb)
        trySend(check())
        awaitClose { cm.unregisterNetworkCallback(cb) }
    }.distinctUntilChanged()

    fun isCurrentlyOnline(): Boolean = check()
    private fun check(): Boolean {
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
```

---

## 5. Result Sealed Class

Unified result type for repositories and ViewModels.

```kotlin
package com.dynapharm.owner.util

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val message: String, val data: T? = null, val code: String? = null) : Result<T>()
    data class Loading<T>(val data: T? = null) : Result<T>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    fun getOrNull(): T? = when (this) {
        is Success -> data; is Error -> data; is Loading -> data
    }
}
```

---

## 6. ApiResponse Wrapper

Matches PHP `{success, data, message, error, meta}` envelope.

```kotlin
package com.dynapharm.owner.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: ApiError? = null,
    val meta: MetaDto? = null
)

@Serializable
data class ApiError(
    val code: String? = null,
    val message: String? = null,
    val details: kotlinx.serialization.json.JsonElement? = null
)

@Serializable
data class MetaDto(
    val page: Int? = null,
    @SerialName("per_page") val perPage: Int? = null,
    val total: Int? = null,
    @SerialName("total_pages") val totalPages: Int? = null,
    val timestamp: String? = null
)
```

---

## 7. Safe API Call Helper

```kotlin
package com.dynapharm.owner.util

import com.dynapharm.owner.data.dto.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

suspend fun <T> safeApiCall(apiCall: suspend () -> ApiResponse<T>): Result<T> =
    withContext(Dispatchers.IO) {
        try {
            val resp = apiCall()
            if (resp.success && resp.data != null) Result.Success(resp.data)
            else Result.Error(resp.error?.message ?: resp.message ?: "Unknown error", code = resp.error?.code)
        } catch (e: SocketTimeoutException) { Result.Error("Request timed out.")
        } catch (e: IOException) { Result.Error("Network error. Check your connection.")
        } catch (e: HttpException) {
            Result.Error(when (e.code()) {
                401 -> "Session expired."; 403 -> "Access denied."; 404 -> "Not found."
                422 -> "Validation error."; 423 -> "Account locked."; 429 -> "Too many requests."
                in 500..599 -> "Server error."; else -> "HTTP ${e.code()}"
            }, code = e.code().toString())
        } catch (e: Exception) { Result.Error(e.localizedMessage ?: "Unexpected error") }
    }
```

---

## 8. Build Config Summary

| Field | Debug | Staging | Release |
|-------|-------|---------|---------|
| `API_BASE_URL` | `http://10.0.2.2/DMS_web/` | `https://staging.dynapharm-dms.com/` | `https://app.dynapharm-dms.com/` |
| `ENABLE_LOGGING` | `true` | `true` | `false` |
| `CERTIFICATE_PINS` | `""` | staging pin | production pins |

---

## 9. Cross-References

| Topic | Document |
|-------|----------|
| Hilt modules (design spec) | [../sds/03-hilt-modules.md](../sds/03-hilt-modules.md) |
| Security architecture | [../sds/05-security.md](../sds/05-security.md) |
| Networking (design spec) | [../sds/06-networking.md](../sds/06-networking.md) |
| Authentication feature | [04-authentication-feature.md](04-authentication-feature.md) |
| API contract: Auth | [../api-contract/02-endpoints-auth.md](../api-contract/02-endpoints-auth.md) |
