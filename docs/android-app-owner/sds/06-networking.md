# SDS 06 -- Networking Layer

**Parent:** [03_SDS.md](../03_SDS.md)

---

## 1. Networking Stack

| Component | Library | Role |
|-----------|---------|------|
| HTTP Client | OkHttp 4.12 | Connection pooling, interceptors, cert pinning |
| REST Client | Retrofit 2.11 | Type-safe API interface generation |
| Serialization | Kotlin Serialization 1.7 | JSON parsing (compile-time, no reflection) |
| Connectivity | ConnectivityManager | Network state monitoring |

---

## 2. Retrofit Service Interfaces

### AuthApiService

```kotlin
interface AuthApiService {
    @POST("api/auth/mobile-login.php")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST("api/auth/refresh.php")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): ApiResponse<RefreshTokenResponse>

    @POST("api/auth/logout.php")
    suspend fun logout(): ApiResponse<Unit>
}
```

### DashboardApiService

```kotlin
interface DashboardApiService {
    @GET("api/owners/dashboard-stats.php")
    suspend fun getDashboardStats(): ApiResponse<DashboardStatsDto>
}
```

### FranchiseApiService

```kotlin
interface FranchiseApiService {
    @GET("api/owners/franchises.php")
    suspend fun getFranchises(@Query("action") action: String = "list"): ApiResponse<List<FranchiseDto>>

    @POST("api/owners/franchises.php")
    suspend fun switchFranchise(@Body request: SwitchFranchiseRequest): ApiResponse<Unit>
}
```

### ReportApiService

One method per report endpoint; all share the same request/response shape:

```kotlin
interface ReportApiService {
    @POST("api/owners/daily-sales.php")
    suspend fun getDailySales(@Body req: ReportRequestDto): ApiResponse<ReportResponseDto>
    @POST("api/owners/sales-summary.php")
    suspend fun getSalesSummary(@Body req: ReportRequestDto): ApiResponse<ReportResponseDto>
    @POST("api/owners/sales-trends.php")
    suspend fun getSalesTrends(@Body req: ReportRequestDto): ApiResponse<ReportResponseDto>
    @POST("api/owners/sales-by-product.php")
    suspend fun getSalesByProduct(@Body req: ReportRequestDto): ApiResponse<ReportResponseDto>
    @POST("api/owners/top-sellers.php")
    suspend fun getTopSellers(@Body req: ReportRequestDto): ApiResponse<ReportResponseDto>
    @POST("api/owners/product-performance.php")
    suspend fun getProductPerformance(@Body req: ReportRequestDto): ApiResponse<ReportResponseDto>
    @POST("api/owners/commission-report.php")
    suspend fun getCommissionReport(@Body req: ReportRequestDto): ApiResponse<ReportResponseDto>
    @POST("api/owners/profit-loss.php")
    suspend fun getProfitLoss(@Body req: ReportRequestDto): ApiResponse<ReportResponseDto>
    @POST("api/owners/cash-flow.php")
    suspend fun getCashFlow(@Body req: ReportRequestDto): ApiResponse<ReportResponseDto>
    @POST("api/owners/balance-sheet.php")
    suspend fun getBalanceSheet(@Body req: ReportRequestDto): ApiResponse<ReportResponseDto>
    @POST("api/owners/expense-report.php")
    suspend fun getExpenseReport(@Body req: ReportRequestDto): ApiResponse<ReportResponseDto>
    @POST("api/owners/distributor-performance.php")
    suspend fun getDistributorPerformance(@Body req: ReportRequestDto): ApiResponse<ReportResponseDto>
    @POST("api/owners/inventory-valuation.php")
    suspend fun getInventoryValuation(@Body req: ReportRequestDto): ApiResponse<ReportResponseDto>
}
```

### ApprovalApiService

```kotlin
interface ApprovalApiService {
    @GET("api/owners/approvals/{type}.php")
    suspend fun getApprovals(@Path("type") type: String, @Query("status") status: String? = null): ApiResponse<List<ApprovalDto>>

    @POST("api/owners/approvals/{type}.php")
    suspend fun processApproval(@Path("type") type: String, @Body request: ApprovalActionRequest): ApiResponse<Unit>
}
```

### ProfileApiService

```kotlin
interface ProfileApiService {
    @GET("api/owners/profile.php")
    suspend fun getProfile(): ApiResponse<ProfileDto>

    @Multipart
    @POST("api/owners/profile.php")
    suspend fun updateProfile(
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part photo: MultipartBody.Part? = null
    ): ApiResponse<ProfileDto>
}
```

---

## 3. DTO Classes

### ApiResponse Wrapper

```kotlin
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val meta: MetaDto? = null
)

@Serializable
data class MetaDto(
    val page: Int? = null, val perPage: Int? = null,
    val total: Int? = null, val totalPages: Int? = null
)
```

### Auth DTOs

```kotlin
@Serializable
data class LoginRequest(
    val username: String, val password: String,
    @SerialName("device_name") val deviceName: String
)

@Serializable
data class LoginResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("owner_id") val ownerId: Long,
    @SerialName("franchise_id") val franchiseId: Long,
    @SerialName("owner_name") val ownerName: String
)

@Serializable
data class RefreshTokenRequest(@SerialName("refresh_token") val refreshToken: String)

@Serializable
data class RefreshTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long
)
```

### Report DTOs

```kotlin
@Serializable
data class ReportRequestDto(
    @SerialName("date_from") val dateFrom: String,
    @SerialName("date_to") val dateTo: String,
    @SerialName("branch_id") val branchId: Long? = null,
    val filters: Map<String, String>? = null
)

@Serializable
data class ReportResponseDto(
    @SerialName("report_type") val reportType: String,
    val title: String, val columns: List<ColumnDto>,
    val rows: List<Map<String, String>>,
    val summary: Map<String, String>? = null,
    @SerialName("generated_at") val generatedAt: String
)

@Serializable
data class ColumnDto(
    val key: String, val label: String,
    val type: String = "text", val align: String = "left"
)
```

### Approval DTOs

```kotlin
@Serializable
data class ApprovalDto(
    val id: Long,
    @SerialName("approval_type") val approvalType: String,
    val title: String, val description: String,
    val amount: Double? = null, val currency: String? = null,
    @SerialName("requested_by") val requestedBy: String,
    @SerialName("requested_at") val requestedAt: String,
    val status: String,
    val attachments: List<String>? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class ApprovalActionRequest(
    val id: Long, val action: String, val comments: String? = null
)
```

---

## 4. Resource Sealed Class

```kotlin
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String, val data: T? = null) : Resource<T>()
    class Loading<T>(val data: T? = null) : Resource<T>()
}
```

---

## 5. Safe API Call Wrapper

```kotlin
suspend fun <T> safeApiCall(apiCall: suspend () -> ApiResponse<T>): Resource<T> {
    return withContext(Dispatchers.IO) {
        try {
            val response = apiCall()
            if (response.success && response.data != null) Resource.Success(response.data)
            else Resource.Error(response.message ?: "Unknown server error")
        } catch (e: SocketTimeoutException) {
            Resource.Error("Request timed out. Please try again.")
        } catch (e: IOException) {
            Resource.Error("Network error. Check your connection.")
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                401 -> "Session expired. Please log in again."
                403 -> "Access denied."
                404 -> "Resource not found."
                500 -> "Server error. Please try again later."
                else -> "HTTP error ${e.code()}"
            }
            Resource.Error(msg)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }
    }
}
```

---

## 6. NetworkMonitor

```kotlin
@Singleton
class NetworkMonitor @Inject constructor(private val context: Context) {

    val isOnline: Flow<Boolean> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                )
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        cm.registerNetworkCallback(request, callback)

        // Emit initial state
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        trySend(caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))

        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    fun isCurrentlyOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
```

---

## 7. Repository Pattern with Caching

Example: DashboardRepositoryImpl showing the cache-then-network pattern.

```kotlin
@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: DashboardApiService,
    private val kpiDao: DashboardKpiDao,
    private val mapper: DashboardMapper
) : DashboardRepository {

    companion object { private const val KPI_TTL_MS = 10 * 60 * 1000L }

    override fun getDashboardKpis(
        franchiseId: Long, forceRefresh: Boolean
    ): Flow<Resource<DashboardKpi>> = flow {
        val now = System.currentTimeMillis()

        // 1. Return fresh cache if available
        if (!forceRefresh) {
            kpiDao.getCachedKpi(franchiseId, now)?.let {
                emit(Resource.Success(mapper.entityToDomain(it))); return@flow
            }
        }

        // 2. Emit stale cache as loading state
        val stale = kpiDao.getStaleKpi(franchiseId)
        emit(Resource.Loading(stale?.let { mapper.entityToDomain(it) }))

        // 3. Fetch from API
        when (val result = safeApiCall { apiService.getDashboardStats() }) {
            is Resource.Success -> {
                val entity = mapper.dtoToEntity(result.data, franchiseId, KPI_TTL_MS)
                kpiDao.cacheKpi(entity)
                emit(Resource.Success(mapper.dtoToDomain(result.data)))
            }
            is Resource.Error -> {
                val data = stale?.let { mapper.entityToDomain(it) }
                emit(Resource.Error(result.message, data))
            }
            is Resource.Loading -> { /* not expected */ }
        }
    }
}
```

---

## 8. Timeout and Retry Configuration

| Setting | Value | Rationale |
|---------|-------|-----------|
| Connect timeout | 30 seconds | Accommodates slow mobile networks |
| Read timeout | 30 seconds | Reports can be large |
| Write timeout | 30 seconds | Profile photo uploads |
| Token refresh retry | 1 attempt | Fail fast, redirect to login |
| Sync queue max retries | 3 | Approval actions are important |
| Sync backoff | Exponential (30s base) | Avoid overwhelming server |

**Logging:** BODY-level logging in debug builds only. Release builds use `Level.NONE` to prevent token and data leakage into logcat.

---

## 9. Cross-References

| Topic | Document |
|-------|----------|
| OkHttp/Retrofit Hilt setup | [03-hilt-modules.md](03-hilt-modules.md) |
| Certificate pinning | [05-security.md](05-security.md) |
| Auth interceptors | [03-hilt-modules.md](03-hilt-modules.md) |
| Cache TTLs and Room DAOs | [04-offline-sync.md](04-offline-sync.md) |
| API contract (full endpoints) | [../api-contract/](../api-contract/) |
| Architecture overview | [01-architecture.md](01-architecture.md) |
