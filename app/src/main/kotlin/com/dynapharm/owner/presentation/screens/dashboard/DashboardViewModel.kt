package com.dynapharm.owner.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynapharm.owner.domain.model.Result
import com.dynapharm.owner.domain.usecase.dashboard.GetDashboardStatsUseCase
import com.dynapharm.owner.domain.usecase.dashboard.RefreshDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Dashboard screen.
 * Manages dashboard statistics data and UI state.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val refreshDashboardUseCase: RefreshDashboardUseCase
) : ViewModel() {

    companion object {
        // Cache is considered stale after 5 minutes
        private const val CACHE_STALE_THRESHOLD_MILLIS = 5 * 60 * 1000L
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardStats()
    }

    /**
     * Loads dashboard statistics.
     * Emits cached data first if available, then fetches fresh data.
     */
    fun loadDashboardStats() {
        viewModelScope.launch {
            getDashboardStatsUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        // Only show loading if we don't have data yet
                        if (_uiState.value.stats == null) {
                            _uiState.update { it.copy(isLoading = true, error = null) }
                        }
                    }

                    is Result.Success -> {
                        val stats = result.data
                        val isStale = isDataStale(stats.lastUpdated)

                        _uiState.update {
                            it.copy(
                                stats = stats,
                                isLoading = false,
                                isRefreshing = false,
                                error = null,
                                isStale = isStale,
                                lastUpdatedTimestamp = stats.lastUpdated
                            )
                        }
                    }

                    is Result.Error -> {
                        // Only show error if we don't have cached data
                        if (_uiState.value.stats == null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    error = result.message ?: result.exception.message
                                        ?: "Failed to load dashboard data"
                                )
                            }
                        } else {
                            // We have cached data, just mark as stale and stop refreshing
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    isStale = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Refreshes dashboard statistics.
     * Forces a fresh fetch from the API.
     * Used by pull-to-refresh.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }

            when (val result = refreshDashboardUseCase()) {
                is Result.Success -> {
                    val stats = result.data
                    _uiState.update {
                        it.copy(
                            stats = stats,
                            isRefreshing = false,
                            isStale = false,
                            error = null,
                            lastUpdatedTimestamp = stats.lastUpdated
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            // Don't overwrite existing data on refresh error
                            error = if (it.stats == null) {
                                result.message ?: "Failed to refresh dashboard data"
                            } else {
                                null // Keep showing current data
                            }
                        )
                    }
                }

                is Result.Loading -> {
                    // Shouldn't happen with suspend function, but handle it
                    _uiState.update { it.copy(isRefreshing = true) }
                }
            }
        }
    }

    /**
     * Dismisses the error state.
     * Allows user to manually dismiss error messages.
     */
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Checks if data is stale based on timestamp.
     */
    private fun isDataStale(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp > CACHE_STALE_THRESHOLD_MILLIS
    }
}
