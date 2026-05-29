package com.example.myapp.data.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.dataclass.Order
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * OrderState - UI State for Order Management
 *
 * Tracks the state of order lists, details, and modification operations.
 *
 * @property error Error message if operations fail
 */
data class OrderState(
    val isLoading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val currentOrder: Order? = null,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val selectedStatus: String = "all"
)

/**
 * OrderViewModel - ViewModel for Order History and Management
 *
 * Handles fetching, filtering, updating, and deleting orders.
 * Used by both user-facing order history and admin order management screens.
 *
 * ## Dependencies
 * - OrderRepository: Data source for order operations
 */
@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    companion object {
        private const val TAG = "OrderViewModel"
    }

    private val _orderState = MutableStateFlow(OrderState())
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder.asStateFlow()

    /**
     * Load all orders.
     *
     * Resets selectedStatus to "all" and fetches the complete order list.
     * Use [refreshCurrentView] instead if you want to preserve the active filter.
     */
    fun loadOrders() {
        viewModelScope.launch {
            _orderState.update {
                it.copy(isLoading = true, error = null, selectedStatus = "all")
            }

            orderRepository.getOrders().fold(
                onSuccess = { orders ->
                    Log.d(TAG, "Orders loaded: ${orders.size}")
                    _orderState.update {
                        it.copy(isLoading = false, orders = orders, error = null)
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to load orders: ${exception.message}")
                    _orderState.update {
                        it.copy(isLoading = false, error = exception.message)
                    }
                    _snackBarData.emit(
                        SnackBarData(exception.message ?: "Failed to load orders", "Error")
                    )
                }
            )
        }
    }

    /**
     * Refresh current view, respecting the active status filter.
     *
     * Call this instead of [loadOrders] whenever you want to reload data
     * without losing the filter the user has selected.
     */
    fun refreshCurrentView() {
        loadOrdersByStatus(_orderState.value.selectedStatus)
    }

    /**
     * Filter orders by status.
     *
     * list stays visible during the fetch, preventing a flash of empty state.
     *
     * @param status The status string to filter by (e.g., "pending", "shipped").
     *               Use "all" to clear filters.
     */
    fun loadOrdersByStatus(status: String) {
        viewModelScope.launch {
            _orderState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    selectedStatus = status
                    // Clearing it causes a flash of the empty-state UI on every filter
                    // change while the new data is in-flight. The previous list stays
                    // visible behind the shimmer until the real results arrive.
                )
            }

            val result = if (status == "all") {
                orderRepository.getOrders()
            } else {
                orderRepository.getOrdersByStatus(status)
            }

            result.fold(
                onSuccess = { orders ->
                    _orderState.update {
                        it.copy(isLoading = false, orders = orders, error = null)
                    }
                },
                onFailure = { exception ->
                    _orderState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "An unknown error occurred"
                        )
                    }
                    _snackBarData.emit(
                        SnackBarData(exception.message ?: "Failed to load orders", "Error")
                    )
                }
            )
        }
    }

    /**
     * Get details for a specific order.
     *
     * @param orderId Unique identifier of the order
     */
    fun getOrderById(orderId: String) {
        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true, error = null) }

            orderRepository.getOrderById(orderId).fold(
                onSuccess = { order ->
                    if (order != null) {
                        _currentOrder.value = order
                        _orderState.update {
                            it.copy(isLoading = false, currentOrder = order, error = null)
                        }
                    } else {
                        _orderState.update { it.copy(isLoading = false, error = "Order not found") }
                        _snackBarData.emit(SnackBarData("Order not found", "Error"))
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to fetch order: ${exception.message}")
                    _orderState.update {
                        it.copy(isLoading = false, error = exception.message)
                    }
                    _snackBarData.emit(
                        SnackBarData(exception.message ?: "Failed to fetch order", "Error")
                    )
                }
            )
        }
    }

    /**
     * Update the status of an order.
     *
     * Used by admins to progress an order through its lifecycle.
     *
     */
    /**
     * Update the status of an order with Optimistic UI updates.
     */
    fun updateOrderStatus(orderId: String, status: String) {
        // 1. Capture the previous state in case we need to roll back
        val previousOrders = _orderState.value.orders

        // 2. Optimistically update the local list immediately
        _orderState.update { currentState ->
            currentState.copy(
                orders = currentState.orders.map {
                    if (it.id == orderId) it.copy(status = status) else it
                },
                // DO NOT set isLoading = true here, it prevents the "jumpy" UI
                isSuccess = false
            )
        }

        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status).fold(
                onSuccess = { updatedOrder ->
                    // Update specific order details if the server returned extra info
                    _orderState.update { currentState ->
                        currentState.copy(
                            isSuccess = true,
                            orders = currentState.orders.map {
                                if (it.id == orderId) updatedOrder else it
                            }
                        )
                    }
                    _snackBarData.emit(SnackBarData("Order status updated to $status"))
                },
                onFailure = { exception ->
                    // 3. Rollback on failure
                    _orderState.update { it.copy(orders = previousOrders) }
                    Log.e(TAG, "Failed to update order status: ${exception.message}")
                    _snackBarData.emit(
                        SnackBarData(exception.message ?: "Update failed. Please try again.", "Error")
                    )
                }
            )
        }
    }

    /**
     * Delete an order with Optimistic UI updates.
     */
    fun deleteOrder(orderId: String) {
        val previousOrders = _orderState.value.orders

        // Optimistically remove from list immediately
        _orderState.update { currentState ->
            currentState.copy(
                orders = currentState.orders.filter { it.id != orderId }
            )
        }

        viewModelScope.launch {
            orderRepository.deleteOrder(orderId).fold(
                onSuccess = {
                    _snackBarData.emit(SnackBarData("Order deleted successfully"))
                },
                onFailure = { exception ->
                    // Rollback if delete fails
                    _orderState.update { it.copy(orders = previousOrders) }
                    _snackBarData.emit(
                        SnackBarData(exception.message ?: "Failed to delete order", "Error")
                    )
                }
            )
        }
    }

    /**
     * Load orders for a specific user.
     *
     * @param userId The user whose orders to load
     */
    fun loadOrdersByUser(userId: String) {
        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true, error = null) }

            orderRepository.getOrdersByUser(userId).fold(
                onSuccess = { orders ->
                    _orderState.update {
                        it.copy(isLoading = false, orders = orders, error = null)
                    }
                },
                onFailure = { exception ->
                    _orderState.update {
                        it.copy(isLoading = false, error = exception.message)
                    }
                    _snackBarData.emit(
                        SnackBarData(exception.message ?: "Failed to load orders", "Error")
                    )
                }
            )
        }
    }

    /**
     * Search orders by customer details or order ID.
     *
     * FIX: Replaced `loadOrders()` with `refreshCurrentView()` on blank query so
     * the active status filter is preserved when the search is cleared.
     * Also removed the duplicate `_orderState` update that was setting isLoading twice.
     *
     * @param query Search term (order ID, customer name, etc.)
     */
    fun searchOrders(query: String) {
        if (query.isBlank()) {
            // FIX: Was loadOrders() — that reset selectedStatus to "all", wiping the
            // active filter every time the search bar was cleared.
            refreshCurrentView()
            return
        }

        viewModelScope.launch {
            // FIX: Single atomic update (was duplicated with both _orderState.update
            // and _orderState.value = ... in the original code).
            _orderState.update { it.copy(isLoading = true, error = null) }

            orderRepository.searchOrders(query).fold(
                onSuccess = { orders ->
                    _orderState.update {
                        it.copy(isLoading = false, orders = orders, error = null)
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to search orders: ${exception.message}")
                    _orderState.update {
                        it.copy(isLoading = false, error = exception.message)
                    }
                }
            )
        }
    }

    /**
     * Search orders by ID scoped to a specific user.
     *
     * @param query Search term (order ID, etc.)
     * @param userId The user to scope the search to
     */
    fun searchOrdersById(query: String, userId: String) {
        if (query.isBlank()) {
            refreshCurrentView()
            return
        }

        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true, error = null) }

            orderRepository.searchOrdersById(query, userId).fold(
                onSuccess = { orders ->
                    _orderState.update {
                        it.copy(isLoading = false, orders = orders, error = null)
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to search orders: ${exception.message}")
                    _orderState.update {
                        it.copy(isLoading = false, error = exception.message)
                    }
                }
            )
        }
    }
}