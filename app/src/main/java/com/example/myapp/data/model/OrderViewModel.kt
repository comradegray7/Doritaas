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
/**
 * OrderViewModel
 *
 */
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
     * Load all orders
     *
     * Fetches the complete list of orders from the repository.
     * Updates [orderState] with the result.
     */
    fun loadOrders() {
        viewModelScope.launch {
            _orderState.value = _orderState.value.copy(
                isLoading = true,
                error = null,
                selectedStatus = "all"
            )

            orderRepository.getOrders().fold(
                onSuccess = { orders ->
                    Log.d(TAG, "Orders loaded: ${orders.size}")
                    _orderState.value = _orderState.value.copy(
                        isLoading = false,
                        orders = orders,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to load orders: ${exception.message}")
                    _orderState.value = _orderState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )

                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to load orders",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Refresh current view (respects current filter)
     */
    fun refreshCurrentView() {
        viewModelScope.launch {
            val currentStatus = _orderState.value.selectedStatus
            loadOrdersByStatus(currentStatus)
        }
    }

    /**
     * Filter orders by status
     *
     * @param status The status string to filter by (e.g., "pending", "shipped").
     *               Use "all" to clear filters.
     */
    fun loadOrdersByStatus(status: String) {
        viewModelScope.launch {
            // Atomic update to start loading and CLEAR previous errors
            _orderState.update {
                it.copy(
                    isLoading = true,
                    error = null, // Clear error immediately
                    selectedStatus = status,
                    orders = emptyList() // Optional: clear list to show shimmer clearly
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
                        it.copy(
                            isLoading = false,
                            orders = orders,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _orderState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "An unknown error occurred"
                        )
                    }
                }
            )
        }
    }

    /**
     * Get details for a specific order
     *
     * @param orderId Unique identifier of the order
     */
    fun getOrderById(orderId: String) {
        viewModelScope.launch {
            _orderState.value = _orderState.value.copy(
                isLoading = true,
                error = null
            )

            orderRepository.getOrderById(orderId).fold(
                onSuccess = { order ->
                    if (order != null) {
                        _currentOrder.value = order
                        _orderState.value = _orderState.value.copy(
                            isLoading = false,
                            currentOrder = order,
                            error = null
                        )
                    } else {
                        _orderState.value = _orderState.value.copy(
                            isLoading = false,
                            error = "Order not found"
                        )
                        _snackBarData.emit(SnackBarData("Order not found", "Error"))
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to fetch order: ${exception.message}")
                    _orderState.value = _orderState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to fetch order",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Update the status of an order
     *
     * Used by admins to progress an order through its lifecycle.
     *
     * @param orderId ID of the order to update
     * @param status New status value (e.g., "shipped")
     */
    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            _orderState.value = _orderState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            orderRepository.updateOrderStatus(orderId, status).fold(
                onSuccess = { order ->
                    _orderState.value = _orderState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        currentOrder = order,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Order status updated to $status"))
                    refreshCurrentView() // Refresh to show updated status
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to update order status: ${exception.message}")
                    _orderState.value = _orderState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to update order status",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    // Delete order
    /**
     * deleteOrder
     *
     *
     * @param orderId The orderId parameter
     */
    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            _orderState.value = _orderState.value.copy(
                isLoading = true,
                error = null
            )

            orderRepository.deleteOrder(orderId).fold(
                onSuccess = {
                    _orderState.value = _orderState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Order deleted successfully"))
                    refreshCurrentView()  // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to delete order: ${exception.message}")
                    _orderState.value = _orderState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to delete order",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * loadOrdersByUser
     *
     *
     * @param userId The userId parameter
     */
    fun loadOrdersByUser(userId: String) {
        viewModelScope.launch {
            _orderState.value = _orderState.value.copy(
                isLoading = true,
                error = null
            )

            orderRepository.getOrdersByUser(userId).fold(
                onSuccess = { orders ->
                    _orderState.value = _orderState.value.copy(
                        isLoading = false,
                        orders = orders,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _orderState.value = _orderState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to load orders",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Search orders customer details
     *
     * @param query Search term (OrderID, Customer Name, etc)
     */
    fun searchOrders(query: String) {
        if (query.isBlank()) {
            loadOrders()
            return
        }

        viewModelScope.launch {
            if (query.isEmpty()) {
                refreshCurrentView()
                return@launch
            }

            _orderState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            _orderState.value = _orderState.value.copy(
                isLoading = true,
                error = null
            )

            orderRepository.searchOrders(query).fold(
                onSuccess = { orders ->
                    _orderState.value = _orderState.value.copy(
                        isLoading = false,
                        orders = orders,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to search orders: ${exception.message}")
                    _orderState.value = _orderState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    /**
     * Search orders by ID or customer details
     *
     * @param query Search term (OrderID, Customer Name, etc)
     */
    fun searchOrdersById(query: String, userId: String) {
        if (query.isBlank()) {
            loadOrders()
            return
        }

        viewModelScope.launch {
            _orderState.value = _orderState.value.copy(
                isLoading = true,
                error = null
            )

            orderRepository.searchOrdersById(query, userId).fold(
                onSuccess = { orders ->
                    _orderState.value = _orderState.value.copy(
                        isLoading = false,
                        orders = orders,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to search orders: ${exception.message}")
                    _orderState.value = _orderState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }
}