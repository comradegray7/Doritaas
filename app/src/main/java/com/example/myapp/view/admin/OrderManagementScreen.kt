package com.example.myapp.view.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.Order
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.OrderViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomFilterChip
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.TopBarActionsShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.screens.product.order.OrderDetailsDialog
import com.example.myapp.view.screens.product.order.components.OrderCard
import com.example.myapp.view.screens.product.order.components.OrderStatusDialog
import com.example.myapp.view.utils.ButtonIcon
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * OrderManagementScreen - Order processing and tracking interface
 *
 * Manage customer orders from placement to fulfillment with comprehensive status tracking
 * and search capabilities.
 *
 * ## Features
 * - **Order List**: Display all orders with key information
 * - **Search**: Find orders by ID or customer details
 * - **Status Filtering**: Filter by order status (Pending, Processing, Shipped, Delivered, Cancelled)
 * - **Status Updates**: Change order status with visual feedback
 * - **Delete Orders**: Remove orders with confirmation dialog
 * - **Real-time Updates**: Automatic refresh on status changes
 *
 * ## Order Information Displayed
 * - Order ID (first 8 characters)
 * - Creation date and time
 * - Number of items
 * - Total amount
 * - User ID
 * - Current status with color-coded chip
 *
 * ## Order Statuses
 * - **Pending** (Orange): Awaiting confirmation
 * - **Processing** (Blue): Being prepared for shipment
 * - **Shipped** (Green): In transit to customer
 * - **Delivered** (Green): Successfully delivered
 * - **Cancelled** (Red): Cancelled by customer or admin
 *
 * ## User Workflow
 * 1. View list of all orders
 * 2. Use search bar to find specific orders
 * 3. Click filter chip to filter by status
 * 4. Click order card to view full details
 * 5. Use "Status" button to update order status
 * 6. Use "Delete" button to remove order (with confirmation)
 * 7. Pull down to refresh order list
 *
 * ## Loading States
 * - Shows shimmer placeholders while loading
 * - Displays error state with retry button on failure
 * - Shows empty state when no orders exist
 *
 * @param viewModel ViewModel for order operations and state management
 * @param onNavigateBack Callback invoked when navigating back from this screen
 *
 * @see OrderViewModel for order data operations
 * @see OrderCard for individual order display
 * @see OrderStatusDialog for status update dialog
 * @see DeleteOrderDialog for delete confirmation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    viewModel: OrderViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val orderState by viewModel.orderState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val networkState = rememberNetworkState(networkManager)

    var showStatusDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showOrderDetailsDialog by remember { mutableStateOf(false) }

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }
    val windowSizeClass = LocalWindowSizeConstant.current

    // Handle snack bar data
    LaunchedEffect(Unit) {
        viewModel.snackBarData.collect { snackBarData ->
            currentSnackBarData = snackBarData
            showSnackBar = true

            if (snackBarData.duration != SnackbarDuration.Indefinite) {
                delay(
                    when (snackBarData.duration) {
                        SnackbarDuration.Short -> 3000L
                        SnackbarDuration.Long -> 5000L
                        else -> 3000L
                    }
                )
                showSnackBar = false
            }
        }
    }

    LaunchedEffect(Unit) {
        // Initial load with default status ("all")
        viewModel.loadOrdersByStatus("all")
    }

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
                viewModel.refreshCurrentView()
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }
        },
        title = R.string.manage_orders,
        snackBarHostState = snackBarHostState,
        showBottomBar = false,
        verticalArrangement = Arrangement.Top,
        onNavigateBack = { onNavigateBack() },
        content = {
            // Network Status Banner
            if (!networkState.hasInternet) {

                CustomSpacer()

                PaddedSection(
                    alignment = Alignment.CenterHorizontally,
                    content = {
                        NetworkStatusBanner(
                            networkState = networkState,
                        )
                    }
                )

                CustomSpacer()
            }

            currentSnackBarData?.let { snackBarData ->
                PaddedSection(
                    alignment = Alignment.CenterHorizontally,
                    content = {
                        FloatingCustomSnackBar(
                            snackBarData = snackBarData,
                            visible = showSnackBar,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(top = windowSizeClass.baseSize),
                            onDismiss = {
                                showSnackBar = false
                                currentSnackBarData = null
                            }
                        )
                    }
                )
            }

            // Single condition check structure
            PaddedSection(
                alignment = Alignment.CenterHorizontally,
                content = {
                    // Search Bar
                    CustomSpacer()
                    CustomSearchBar(
                        query = searchQuery,
                        onQueryChange = { newQuery ->
                            searchQuery = newQuery
                            // Optional: Add debounce for real-time search
                            if (newQuery.isNotEmpty()) {
                                viewModel.searchOrders(newQuery)
                            } else {
                                viewModel.loadOrders() // Load all when empty
                            }
                        },
                        onSearch = { query ->
                            viewModel.searchOrders(query)
                        },
                        leadingIcon = {
                            CustomIcon(
                                icon = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        placeholder = {
                            Text(
                                stringResource(R.string.search_orders),
                                style = windowSizeClass.bodyTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                ButtonIconComposable(
                                    showBgColor = false,
                                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Search),
                                    onClick = {
                                        searchQuery = ""
                                        viewModel.loadOrders()
                                    },
                                    contentDescription = "Search"
                                )
                            }
                        }
                    )
                    CustomSpacer()

                    when {
                        orderState.isLoading -> {
                            // Loading State
                            CustomListCardShimmer()
                        }

                        orderState.error != null -> {
                            CustomEmptyState(
                                btnLabel = R.string.retry,
                                title = R.string.orders_error, // Fixed your 'color_error' typo too
                                onBtnClick = { viewModel.loadOrders() },
                                btnIcon = Icons.Filled.Error,
                            )
                        }

                        orderState.orders.isEmpty() -> {
                            // Empty State
                            CustomEmptyState(
                                titleStr = if (searchQuery.isEmpty()) "Complete a purchase and view your orders here" else "No results found",
                                showBtn = false,
                                leadingIcon = Icons.Filled.SearchOff
                            )
                        }

                        else -> {
                            // Orders List
                            CustomLazyColumn {

                                item {
                                    // Filter Button
                                    HeadlineWidget(
                                        leadingText = R.string.filter_orders,
                                        trailing = {
                                            CustomFilterChip(
                                                isSelected = orderState.selectedStatus != "all",
                                                onClick = { showStatusDialog = true },
                                                label = when (orderState.selectedStatus) {
                                                    "all" -> "All"
                                                    else -> orderState.selectedStatus.replaceFirstChar { it.uppercase() }
                                                },
                                                leadingIcon = {
                                                    CustomIcon(
                                                        icon = Icons.Filled.FilterList,
                                                        contentDescription = "Filter list",
                                                        tint = MaterialTheme.colorScheme.error,
                                                    )
                                                }
                                            )
                                        }
                                    )
                                }

                                items(orderState.orders) { order ->
                                    OrderCard(
                                        order = order,
                                        onOrderClick = {
                                            selectedOrder = order
                                            showOrderDetailsDialog = true
                                        },
                                        actions = {
                                            // Action Buttons
                                            Row {
                                                ButtonIconComposable(
                                                    showBgColor = false,
                                                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Edit),
                                                    onClick = {
                                                        selectedOrder = order
                                                        showStatusDialog = true
                                                    },
                                                    contentDescription = "Edit"
                                                )

                                                ButtonIconComposable(
                                                    showBgColor = false,
                                                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Delete),
                                                    onClick = {
                                                        selectedOrder = order
                                                        showDeleteDialog = true
                                                    },
                                                    contentDescription = "Delete",
                                                    tint = colors.red
                                                )
                                            }
                                        }
                                    )
                                }

                                item {
                                    CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))
                                }
                            }
                        }
                    }
                })

            if (showOrderDetailsDialog) {
                selectedOrder?.let {
                    OrderDetailsDialog(
                        order = it,
                        onDismiss = {
                            showOrderDetailsDialog = false
                            selectedOrder = null  // Clear selection on dismiss
                        }
                    )
                }
            }
            // Status Dialog
            if (showStatusDialog) {
                OrderStatusDialog(
                    currentOrder = selectedOrder,
                    currentStatus = selectedOrder?.status ?: orderState.selectedStatus,
                    onDismiss = {
                        showStatusDialog = false
                        selectedOrder = null
                    },
                    onStatusChange = { newStatus ->
                        selectedOrder?.let { order ->
                            viewModel.updateOrderStatus(order.id, newStatus)
                        } ?: run {
                            viewModel.loadOrdersByStatus(newStatus)
                        }
                        showStatusDialog = false
                        selectedOrder = null
                    }
                )
            }

            // Delete Dialog
            if (showDeleteDialog && selectedOrder != null) {
                DeleteOrderDialog(
                    order = selectedOrder!!,
                    onDismiss = {
                        showDeleteDialog = false
                        selectedOrder = null
                    },
                    onConfirm = {
                        viewModel.deleteOrder(selectedOrder!!.id)
                        showDeleteDialog = false
                        selectedOrder = null
                    }
                )
            }
        },
        actions = {
            if (orderState.isLoading) {
                TopBarActionsShimmer()
            } else {
                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Refresh),
                    onClick = { viewModel.loadOrders() },
                    contentDescription = "Refresh"
                )
            }
        }
    )
}

/**
 * DeleteOrderDialog - Order deletion confirmation dialog
 *
 * Displays a warning dialog to confirm order deletion, preventing accidental removals.
 *
 * ## Features
 * - Warning icon to indicate destructive action
 * - Order ID displayed for confirmation
 * - Warning about irreversibility
 * - Confirm and cancel buttons
 *
 * @param order The order to be deleted
 * @param onDismiss Callback when dialog is dismissed without deleting
 * @param onConfirm Callback when user confirms deletion
 */
@Composable
fun DeleteOrderDialog(
    order: Order,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Warning,
                contentDescription = "Warning",
                tint = colors.orange,
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Text(
                stringResource(R.string.delete_order),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Text(
                "Are you sure you want to delete order #${order.id.take(8)}? This action cannot be undone.",
                style = windowSizeClass.bodyTextStyle
            )
        },
        dismissButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.cancel,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            CustomTextButton(
                onClick = onConfirm,
                label = R.string.delete_order,
                color = MaterialTheme.colorScheme.error
            )
        },
    )
}

/**
 * Extension function for date formatting
 *
 * Formats a Date object to a human-readable string in the format:
 * "MMM dd, yyyy at hh:mm a" (e.g., "Dec 12, 2025 at 01:30 PM")
 *
 * @return Formatted date string
 */
fun Date.formatToString(): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return formatter.format(this)
}