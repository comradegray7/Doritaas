package com.example.myapp.data.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.dataclass.DashboardStats
import com.example.myapp.data.dataclass.DashboardUiState
import com.example.myapp.data.dataclass.ProductStatusData
import com.example.myapp.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * DashboardViewModel - ViewModel for the Admin Dashboard
 *
 * Aggregates data from various repositories to provide a comprehensive
 * overview of the application's status for administrators.
 *
 * Fetches stats for products, orders, sales, and inventory.
 *
 * ## Dependencies
 * - DashboardRepository: Centralized source for dashboard statistics
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())

    init {
        loadDashboardData()
    }

    /**
     * Load all dashboard data.
     *
     * Fetches statistics for all modules in parallel to minimize wait time.
     * Detailed stats include:
     * - Overall counts (revenue, orders, products)
     * - Product status (stock levels)
     * - Distribution stats (categories, brands, colors, sizes)
     * - Order status breakdown
     * - Monthly sales data
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load all dashboard data in parallel for better performance
                val statsDeferred = async { dashboardRepository.getDashboardStats() }
                val productStatusDeferred = async { dashboardRepository.getProductStatusData() }
                val categoryStatsDeferred = async { dashboardRepository.getCategoryStats() }
                val brandStatsDeferred = async { dashboardRepository.getBrandStats() }
                val colorStatsDeferred = async { dashboardRepository.getColorStats() }
                val sizeStatsDeferred = async { dashboardRepository.getSizeStats() }
                val orderStatusDeferred = async { dashboardRepository.getOrderStatusData() }
                val monthlySalesDeferred = async { dashboardRepository.getMonthlySales() }

                // Await all results
                val stats = statsDeferred.await()
                val productStatus = productStatusDeferred.await()
                val categoryStats = categoryStatsDeferred.await()
                val brandStats = brandStatsDeferred.await()
                val colorStats = colorStatsDeferred.await()
                val sizeStats = sizeStatsDeferred.await()
                val orderStatus = orderStatusDeferred.await()
                val monthlySales = monthlySalesDeferred.await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dashboardStats = stats.getOrNull() ?: DashboardStats(),
                    productStatus = productStatus.getOrNull() ?: ProductStatusData(),
                    categoryStats = categoryStats.getOrNull() ?: emptyList(),
                    brandStats = brandStats.getOrNull() ?: emptyList(),
                    colorStats = colorStats.getOrNull() ?: emptyList(),
                    sizeStats = sizeStats.getOrNull() ?: emptyList(),
                    orderStatus = orderStatus.getOrNull() ?: emptyList(),
                    monthlySales = monthlySales.getOrNull() ?: emptyList(),
                    error = null
                )

                Log.d("DashboardViewModel", "Dashboard data loaded successfully")

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error loading dashboard data: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard data"
                )
            }
        }
    }

}
