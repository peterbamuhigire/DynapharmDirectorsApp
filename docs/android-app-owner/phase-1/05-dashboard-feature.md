# Phase 1 -- Section 05: Dashboard Feature

**Parent:** [Phase 1 README](README.md) | Full vertical slice: DTO, Entity, Domain, DAO, Repo, UseCase, VM, Screen.

## 1. DashboardApiService.kt (`data/api/`)

```kotlin
package com.dynapharm.owner.data.api

import com.dynapharm.owner.data.dto.dashboard.DashboardStatsResponse
import retrofit2.http.GET

interface DashboardApiService {
    @GET("api/owners/dashboard-stats.php")
    suspend fun getStats(): DashboardStatsResponse
}
```

## 2. DashboardStatsDto.kt (`data/dto/dashboard/`) -- matches `api/owners/dashboard-stats.php`

```kotlin
package com.dynapharm.owner.data.dto.dashboard

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardStatsResponse(
    val success: Boolean,
    val data: DashboardStatsDto? = null,
    val meta: DashboardMetaDto? = null,
    val error: ErrorDto? = null
)

@Serializable
data class DashboardStatsDto(
    @SerialName("sales_mtd") val salesMtd: Double,
    @SerialName("sales_mtd_trend") val salesMtdTrend: Double? = null,
    @SerialName("cash_balance") val cashBalance: Double,
    @SerialName("cash_balance_trend") val cashBalanceTrend: Double? = null,
    @SerialName("inventory_value") val inventoryValue: Double,
    @SerialName("inventory_value_trend") val inventoryValueTrend: Double? = null,
    @SerialName("total_bv") val totalBv: Double,
    @SerialName("total_bv_trend") val totalBvTrend: Double? = null,
    @SerialName("pending_approvals") val pendingApprovals: Int,
    @SerialName("pending_by_type") val pendingByType: Map<String, Int>? = null,
    val currency: String,
    @SerialName("franchise_name") val franchiseName: String,
    val period: PeriodDto? = null
)

@Serializable
data class PeriodDto(val start: String, val end: String)

@Serializable
data class DashboardMetaDto(
    val timestamp: String? = null,
    @SerialName("franchise_id") val franchiseId: Long? = null,
    @SerialName("franchise_name") val franchiseName: String? = null,
    val currency: String? = null
)

@Serializable
data class ErrorDto(val code: String? = null, val message: String? = null)
```

## 3. DashboardStatsEntity.kt (`data/db/entity/`) -- one row per franchise

```kotlin
package com.dynapharm.owner.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dashboard_stats")
data class DashboardStatsEntity(
    @PrimaryKey val franchiseId: Long,
    val salesMtd: Double, val salesMtdTrend: Double?,
    val cashBalance: Double, val cashBalanceTrend: Double?,
    val inventoryValue: Double, val inventoryValueTrend: Double?,
    val totalBv: Double, val totalBvTrend: Double?,
    val pendingApprovals: Int, val currency: String,
    val franchiseName: String,
    val periodStart: String?, val periodEnd: String?,
    val cachedAt: Long, val expiresAt: Long
)
```

## 4. DashboardDao.kt (`data/db/dao/`)

```kotlin
package com.dynapharm.owner.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dynapharm.owner.data.db.entity.DashboardStatsEntity

@Dao
interface DashboardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: DashboardStatsEntity)

    @Query("SELECT * FROM dashboard_stats WHERE franchiseId = :fid AND expiresAt > :now LIMIT 1")
    suspend fun getLatest(fid: Long, now: Long): DashboardStatsEntity?

    @Query("SELECT * FROM dashboard_stats WHERE franchiseId = :fid LIMIT 1")
    suspend fun getStale(fid: Long): DashboardStatsEntity?

    @Query("DELETE FROM dashboard_stats WHERE expiresAt < :now")
    suspend fun deleteExpired(now: Long)

    @Query("DELETE FROM dashboard_stats WHERE franchiseId = :fid")
    suspend fun deleteByFranchise(fid: Long)

    @Query("DELETE FROM dashboard_stats")
    suspend fun deleteAll()
}
```

## 5. DashboardStats (`domain/model/`) -- pure Kotlin

```kotlin
package com.dynapharm.owner.domain.model

data class DashboardStats(
    val salesMtd: Double, val salesMtdTrend: Double?,
    val cashBalance: Double, val cashBalanceTrend: Double?,
    val inventoryValue: Double, val inventoryValueTrend: Double?,
    val totalBv: Double, val totalBvTrend: Double?,
    val pendingApprovals: Int, val currency: String,
    val franchiseName: String,
    val periodStart: String?, val periodEnd: String?,
    val cachedAt: Long? = null
)
```

## 6. DashboardMapper.kt (`data/mapper/`)

```kotlin
package com.dynapharm.owner.data.mapper

import com.dynapharm.owner.data.db.entity.DashboardStatsEntity
import com.dynapharm.owner.data.dto.dashboard.DashboardStatsDto
import com.dynapharm.owner.domain.model.DashboardStats

object DashboardMapper {
    fun dtoToDomain(d: DashboardStatsDto) = DashboardStats(
        d.salesMtd, d.salesMtdTrend, d.cashBalance, d.cashBalanceTrend,
        d.inventoryValue, d.inventoryValueTrend, d.totalBv, d.totalBvTrend,
        d.pendingApprovals, d.currency, d.franchiseName,
        d.period?.start, d.period?.end
    )

    fun dtoToEntity(d: DashboardStatsDto, fid: Long, ttlMs: Long): DashboardStatsEntity {
        val now = System.currentTimeMillis()
        return DashboardStatsEntity(fid, d.salesMtd, d.salesMtdTrend,
            d.cashBalance, d.cashBalanceTrend, d.inventoryValue, d.inventoryValueTrend,
            d.totalBv, d.totalBvTrend, d.pendingApprovals, d.currency, d.franchiseName,
            d.period?.start, d.period?.end, now, now + ttlMs)
    }

    fun entityToDomain(e: DashboardStatsEntity) = DashboardStats(
        e.salesMtd, e.salesMtdTrend, e.cashBalance, e.cashBalanceTrend,
        e.inventoryValue, e.inventoryValueTrend, e.totalBv, e.totalBvTrend,
        e.pendingApprovals, e.currency, e.franchiseName,
        e.periodStart, e.periodEnd, e.cachedAt
    )
}
```

## 7. DashboardRepository (`domain/repository/`)

```kotlin
package com.dynapharm.owner.domain.repository

import com.dynapharm.owner.domain.model.DashboardStats
import com.dynapharm.owner.util.Resource
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getStats(franchiseId: Long, forceRefresh: Boolean = false): Flow<Resource<DashboardStats>>
}
```

`data/repository/DashboardRepositoryImpl.kt` -- offline-first, 30-min TTL:

```kotlin
package com.dynapharm.owner.data.repository

import com.dynapharm.owner.data.api.DashboardApiService
import com.dynapharm.owner.data.db.dao.DashboardDao
import com.dynapharm.owner.data.mapper.DashboardMapper
import com.dynapharm.owner.domain.model.DashboardStats
import com.dynapharm.owner.domain.repository.DashboardRepository
import com.dynapharm.owner.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val api: DashboardApiService,
    private val dao: DashboardDao
) : DashboardRepository {

    private companion object { const val TTL = 30L * 60_000L }

    override fun getStats(franchiseId: Long, forceRefresh: Boolean): Flow<Resource<DashboardStats>> = flow {
        emit(Resource.Loading())
        if (!forceRefresh) {
            dao.getLatest(franchiseId, System.currentTimeMillis())?.let {
                emit(Resource.Success(DashboardMapper.entityToDomain(it))); return@flow
            }
        }
        val stale = dao.getStale(franchiseId)
        if (stale != null) emit(Resource.Loading(DashboardMapper.entityToDomain(stale)))
        try {
            val resp = api.getStats()
            if (resp.success && resp.data != null) {
                dao.insert(DashboardMapper.dtoToEntity(resp.data, franchiseId, TTL))
                emit(Resource.Success(DashboardMapper.dtoToDomain(resp.data)))
            } else {
                fallback(stale, resp.error?.message ?: "Failed to load dashboard")
            }
        } catch (e: Exception) {
            Timber.e(e, "Dashboard fetch failed"); fallback(stale, e.message ?: "Network error")
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<Resource<DashboardStats>>.fallback(
        stale: com.dynapharm.owner.data.db.entity.DashboardStatsEntity?, msg: String
    ) {
        if (stale != null) emit(Resource.Success(DashboardMapper.entityToDomain(stale), isStale = true))
        else emit(Resource.Error(msg))
    }
}
```

## 8. GetDashboardStatsUseCase (`domain/usecase/dashboard/`)

```kotlin
package com.dynapharm.owner.domain.usecase.dashboard

import com.dynapharm.owner.domain.repository.DashboardRepository
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(private val repo: DashboardRepository) {
    operator fun invoke(franchiseId: Long, forceRefresh: Boolean = false) =
        repo.getStats(franchiseId, forceRefresh)
}
```

## 9. DashboardViewModel (`presentation/dashboard/`) -- loading/success/error, pull-to-refresh, 60s auto-refresh

```kotlin
package com.dynapharm.owner.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynapharm.owner.domain.model.DashboardStats
import com.dynapharm.owner.domain.usecase.dashboard.GetDashboardStatsUseCase
import com.dynapharm.owner.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true, val isRefreshing: Boolean = false,
    val stats: DashboardStats? = null, val error: String? = null,
    val isStale: Boolean = false, val ownerFirstName: String = ""
)

sealed class DashboardEvent { data object NavigateToApprovals : DashboardEvent() }

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardStats: GetDashboardStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<DashboardEvent>()
    val events = _events.asSharedFlow()
    private var autoRefreshJob: Job? = null
    private var fid: Long = 0L

    fun loadDashboard(franchiseId: Long, ownerName: String = "") {
        fid = franchiseId
        if (ownerName.isNotBlank()) _uiState.update { it.copy(ownerFirstName = ownerName) }
        fetch(false); startAutoRefresh()
    }

    fun onPullToRefresh() { _uiState.update { it.copy(isRefreshing = true) }; fetch(true) }
    fun onPendingApprovalsTapped() { viewModelScope.launch { _events.emit(DashboardEvent.NavigateToApprovals) } }

    private fun fetch(force: Boolean) { viewModelScope.launch {
        getDashboardStats(fid, force).collect { r -> when (r) {
            is Resource.Loading -> _uiState.update { it.copy(isLoading = r.data == null, stats = r.data ?: it.stats, error = null) }
            is Resource.Success -> _uiState.update { it.copy(isLoading = false, isRefreshing = false, stats = r.data, error = null, isStale = r.isStale) }
            is Resource.Error -> _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = r.message, stats = r.data ?: it.stats) }
        }}
    }}

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch { while (true) { delay(60_000L); fetch(true) } }
    }

    override fun onCleared() { super.onCleared(); autoRefreshJob?.cancel() }
}
```

## 10. DashboardScreen.kt (`presentation/dashboard/`) -- greeting, badge, 5 KPI cards (2+2+1), offline banner, shimmer

```kotlin
package com.dynapharm.owner.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dynapharm.owner.R
import com.dynapharm.owner.domain.model.DashboardStats
import java.text.NumberFormat
import java.time.LocalTime
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    franchiseId: Long, ownerFirstName: String,
    onNavigateToApprovals: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(franchiseId) { viewModel.loadDashboard(franchiseId, ownerFirstName) }
    LaunchedEffect(Unit) { viewModel.events.collect { when (it) {
        is DashboardEvent.NavigateToApprovals -> onNavigateToApprovals()
    }}}

    PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = { viewModel.onPullToRefresh() },
        modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Greeting(state.ownerFirstName)
            Spacer(Modifier.height(8.dp))
            state.stats?.franchiseName?.let { FranchiseBadge(it); Spacer(Modifier.height(16.dp)) }
            AnimatedVisibility(state.isStale) { StaleBanner(state.stats?.cachedAt); Spacer(Modifier.height(12.dp)) }
            when {
                state.isLoading && state.stats == null -> ShimmerGrid()
                state.error != null && state.stats == null -> ErrorCard(state.error!!) { viewModel.onPullToRefresh() }
                else -> state.stats?.let { KpiGrid(it) { viewModel.onPendingApprovalsTapped() } }
            }
        }
    }
}

@Composable private fun Greeting(name: String) {
    val g = when (LocalTime.now().hour) { in 0..11 -> stringResource(R.string.good_morning)
        in 12..16 -> stringResource(R.string.good_afternoon); else -> stringResource(R.string.good_evening) }
    Text("$g${if (name.isNotBlank()) ", $name!" else "!"}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
}

@Composable private fun FranchiseBadge(name: String) {
    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 14.dp, vertical = 6.dp)) {
        Text(name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
    }
}

@Composable private fun StaleBanner(cachedAt: Long?) {
    val mins = cachedAt?.let { ((System.currentTimeMillis() - it) / 60_000).toInt() }
    val t = when { mins == null -> "unknown"; mins < 1 -> "just now"; else -> "$mins min ago" }
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.tertiaryContainer).padding(12.dp)) {
        Text("Showing cached data (last updated $t)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
    }
}

@Composable private fun KpiGrid(s: DashboardStats, onApprovalsTap: () -> Unit) {
    val c = s.currency
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(Modifier.weight(1f), stringResource(R.string.kpi_sales_mtd), fmtCur(s.salesMtd, c), s.salesMtdTrend, Icons.Filled.AttachMoney, MaterialTheme.colorScheme.primary)
        StatCard(Modifier.weight(1f), stringResource(R.string.kpi_cash_balance), fmtCur(s.cashBalance, c), s.cashBalanceTrend, Icons.Filled.AccountBalance, MaterialTheme.colorScheme.secondary)
    }
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(Modifier.weight(1f), stringResource(R.string.kpi_inventory_value), fmtCur(s.inventoryValue, c), s.inventoryValueTrend, Icons.Filled.Inventory, MaterialTheme.colorScheme.tertiary)
        StatCard(Modifier.weight(1f), stringResource(R.string.kpi_total_bv), fmtBv(s.totalBv), s.totalBvTrend, Icons.Filled.ShowChart, MaterialTheme.colorScheme.primary)
    }
    Spacer(Modifier.height(12.dp))
    StatCard(Modifier.fillMaxWidth().clickable { onApprovalsTap() }, stringResource(R.string.kpi_pending_approvals), s.pendingApprovals.toString(), null,
        Icons.Filled.Pending, if (s.pendingApprovals > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline, tapHint = stringResource(R.string.tap_to_view))
}

@Composable private fun StatCard(
    modifier: Modifier, title: String, value: String, trend: Double?,
    icon: ImageVector, tint: Color, tapHint: String? = null
) {
    Card(modifier, RoundedCornerShape(16.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surface), CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(24.dp), tint); Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            trend?.let { Spacer(Modifier.height(4.dp)); TrendBadge(it) }
            tapHint?.let { Spacer(Modifier.height(4.dp)); Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable private fun TrendBadge(t: Double) {
    val up = t >= 0.0; val c = if (up) Color(0xFF2E7D32) else Color(0xFFC62828)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(if (up) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown, null, Modifier.size(16.dp), c)
        Spacer(Modifier.width(4.dp))
        Text("${if (up) "+" else ""}${"%.1f".format(t)}%", style = MaterialTheme.typography.bodySmall, color = c, fontWeight = FontWeight.Medium)
    }
}

@Composable private fun ShimmerGrid() { repeat(2) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { repeat(2) {
        Card(Modifier.weight(1f).height(120.dp), RoundedCornerShape(16.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) { Box(Modifier.fillMaxSize()) }
    }}; Spacer(Modifier.height(12.dp))
}; Card(Modifier.fillMaxWidth().height(120.dp), RoundedCornerShape(16.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) { Box(Modifier.fillMaxSize()) } }

@Composable private fun ErrorCard(msg: String, onRetry: () -> Unit) {
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), CardDefaults.cardColors(MaterialTheme.colorScheme.errorContainer)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(msg, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.tap_to_retry), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onRetry() })
        }
    }
}

private fun fmtCur(a: Double, code: String): String = try {
    NumberFormat.getCurrencyInstance(Locale.getDefault()).apply { currency = Currency.getInstance(code); maximumFractionDigits = 0 }.format(a)
} catch (_: Exception) { "$code ${NumberFormat.getNumberInstance().format(a)}" }

private fun fmtBv(v: Double) = NumberFormat.getNumberInstance().apply { maximumFractionDigits = 0 }.format(v)
```

## 11. Wiring and Resources

**String resources** (`res/values/strings.xml`): `good_morning`, `good_afternoon`, `good_evening`, `kpi_sales_mtd`, `kpi_cash_balance`, `kpi_inventory_value`, `kpi_total_bv`, `kpi_pending_approvals`, `tap_to_view`, `tap_to_retry`.

**Hilt** -- add to `RepositoryModule`: `@Binds abstract fun bindDashboardRepo(impl: DashboardRepositoryImpl): DashboardRepository`. Add to `NetworkModule`: `@Provides @Singleton fun provideDashboardApi(retrofit: Retrofit) = retrofit.create(DashboardApiService::class.java)`.

**Resource wrapper** (`util/Resource.kt`):

```kotlin
package com.dynapharm.owner.util
sealed class Resource<T>(val data: T? = null, val message: String? = null, val isStale: Boolean = false) {
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Success<T>(data: T, isStale: Boolean = false) : Resource<T>(data, isStale = isStale)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}
```

---

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-02-08 | Initial dashboard feature vertical slice |
