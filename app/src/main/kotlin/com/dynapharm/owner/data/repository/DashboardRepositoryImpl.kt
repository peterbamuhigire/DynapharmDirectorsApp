package com.dynapharm.owner.data.repository

import com.dynapharm.owner.data.local.db.dao.DashboardDao
import com.dynapharm.owner.data.remote.api.DashboardApiService
import com.dynapharm.owner.di.IoDispatcher
import com.dynapharm.owner.domain.model.DashboardStats
import com.dynapharm.owner.domain.model.Result
import com.dynapharm.owner.domain.model.toDomainModel
import com.dynapharm.owner.domain.model.toEntity
import com.dynapharm.owner.domain.repository.DashboardRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of DashboardRepository.
 * Handles dashboard data fetching, caching, and synchronization.
 * Uses cache-first strategy with 5-minute TTL.
 */
class DashboardRepositoryImpl @Inject constructor(
    private val dashboardDao: DashboardDao,
    private val dashboardApiService: DashboardApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : DashboardRepository {

    companion object {
        // Cache validity duration: 5 minutes
        private const val CACHE_VALIDITY_MILLIS = 5 * 60 * 1000L
    }

    override fun observeDashboardStats(): Flow<Result<DashboardStats>> = flow {
        emit(Result.Loading)

        try {
            // Step 1: Check cache and emit if valid
            val cached = dashboardDao.getDashboardStats()
            if (cached != null) {
                val isStale = !isCacheValid(cached.cachedAt)
                emit(Result.Success(cached.toDomainModel()))

                // If cache is fresh, we're done
                if (!isStale) {
                    return@flow
                }
            }

            // Step 2: Fetch fresh data from API (either no cache or stale cache)
            val response = dashboardApiService.getDashboardStats()
            if (response.success && response.data != null) {
                val entity = response.data.toEntity()
                dashboardDao.insertDashboardStats(entity)
                emit(Result.Success(entity.toDomainModel()))
            } else {
                // If API fails but we have cached data, keep showing it
                if (cached != null) {
                    // Already emitted cached data above
                } else {
                    emit(Result.Error(
                        Exception(response.error?.message ?: "Failed to fetch dashboard data"),
                        response.error?.details
                    ))
                }
            }
        } catch (e: Exception) {
            // If network error and we have cached data, keep showing it
            val cached = dashboardDao.getDashboardStats()
            if (cached == null) {
                emit(Result.Error(e, "Failed to load dashboard data: ${e.message}"))
            }
            // Otherwise, cached data was already emitted above
        }
    }.flowOn(ioDispatcher)

    override suspend fun getDashboardStats(forceRefresh: Boolean): Result<DashboardStats> =
        withContext(ioDispatcher) {
            try {
                if (!forceRefresh) {
                    // Check cache first
                    val cached = dashboardDao.getDashboardStats()
                    if (cached != null && isCacheValid(cached.cachedAt)) {
                        return@withContext Result.Success(cached.toDomainModel())
                    }
                }

                // Fetch from API
                val response = dashboardApiService.getDashboardStats()
                if (response.success && response.data != null) {
                    val entity = response.data.toEntity()
                    dashboardDao.insertDashboardStats(entity)
                    Result.Success(entity.toDomainModel())
                } else {
                    // Try to return stale cache if available
                    val cached = dashboardDao.getDashboardStats()
                    if (cached != null) {
                        Result.Success(cached.toDomainModel())
                    } else {
                        Result.Error(
                            Exception(response.error?.message ?: "Failed to fetch dashboard data"),
                            response.error?.details
                        )
                    }
                }
            } catch (e: Exception) {
                // Try to return stale cache if available
                val cached = dashboardDao.getDashboardStats()
                if (cached != null) {
                    Result.Success(cached.toDomainModel())
                } else {
                    Result.Error(e, "Failed to load dashboard data: ${e.message}")
                }
            }
        }

    override suspend fun refreshDashboardStats(): Result<DashboardStats> =
        getDashboardStats(forceRefresh = true)

    override suspend fun clearCache() = withContext(ioDispatcher) {
        dashboardDao.clearDashboardStats()
    }

    /**
     * Checks if cached data is still valid based on TTL.
     */
    private fun isCacheValid(cachedAt: Long): Boolean {
        return System.currentTimeMillis() - cachedAt < CACHE_VALIDITY_MILLIS
    }
}
