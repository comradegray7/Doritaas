package com.example.myapp.view.screens.product.order

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
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
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.Order
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.OrderViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.admin.CustomSearchBar
import com.example.myapp.view.admin.DeleteOrderDialog
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.TopBarActionsShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.screens.product.order.components.OrderCard
import com.example.myapp.view.screens.product.order.components.OrderStatusDialog
import com.example.myapp.view.utils.ButtonIcon
import kotlinx.coroutines.delay

/**
 * ViewOrdersScreen - Screen for viewing and managing user orders.
 *
 * Displays a list of orders placed by the user. Supports:
 * - Listing orders with different states (loading, error, empty, success).
 * - Searching orders.
 * - Refreshing the order list.
 * - Viewing order details in a dialog.
 * - Updating order status (if applicable/allowed).
 * - Deleting orders.
 *
 * @param userId The ID of the user whose orders are to be displayed.
 * @param viewModel The [OrderViewModel] used for state management and data operations.
 * @param onNavigateBack Callback to navigate back to the previous screen.
 * @param networkManager Manager for checking network connectivity.
 */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ViewOrdersScreen(
    userId: String,
    viewModel: OrderViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager,
) {
    val windowClass = LocalWindowSizeConstant.current

    val orderState by viewModel.orderState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val networkState = rememberNetworkState(networkManager)
    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOrderDetailsDialog by remember { mutableStateOf(false) }

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

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

    // Load orders when authenticated
    LaunchedEffect(userId) {
        viewModel.loadOrdersByUser(userId)
    }

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
                viewModel.loadOrdersByUser(userId)
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
                                .padding(top = windowClass.baseSize),
                            onDismiss = {
                                showSnackBar = false
                                currentSnackBarData = null
                            }
                        )
                    }
                )
            }

            PaddedSection(
                alignment = Alignment.CenterHorizontally,
                content = {
                    CustomSpacer()
                    CustomSearchBar(
                        query = searchQuery,
                        onQueryChange = { newQuery ->
                            searchQuery = newQuery
                            if (newQuery.isNotEmpty()) {
                                viewModel.searchOrdersById(
                                    newQuery,
                                    userId
                                )
                            } else {
                                viewModel.loadOrdersByUser(userId)
                            }
                        },
                        onSearch = { query ->
                            viewModel.searchOrdersById(query, userId)
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
                                style = windowClass.bodyTextStyle,
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
                                        viewModel.loadOrdersByUser(userId)
                                    },
                                    contentDescription = "Search"
                                )
                            }
                        }
                    )
                    CustomSpacer()

                    // Single condition check structure
                    when {
                        orderState.isLoading -> {
                            // Loading State
                            PaddedSection(
                                content = {
                                    CustomListCardShimmer()
                                }
                            )
                        }

                        orderState.error != null -> {
                            // Error State
                            PaddedSection(
                                content = {
                                    CustomEmptyState(
                                        btnLabel = R.string.retry,
                                        title = R.string.orders_error,
                                        onBtnClick = { viewModel.loadOrdersByUser(userId) },
                                        btnIcon = Icons.Filled.Error,
                                    )
                                })
                        }

                        orderState.orders.isEmpty() -> {
                            // Empty State
                            CustomEmptyState(
                                titleStr = if (searchQuery.isEmpty()) "Complete a purchase to view your orders" else "No results found",
                                showBtn = false,
                                leadingIcon = Icons.Filled.SearchOff
                            )
                        }

                        else -> {
                            // Orders List
                            CustomLazyColumn {

                                items(orderState.orders) { order ->
                                    OrderCard(
                                        order = order,
                                        actions = {},
                                        onOrderClick = {
                                            selectedOrder = order
                                            showOrderDetailsDialog = true
                                        },
                                    )
                                }

                                item {
                                    CustomSpacer(modifier = Modifier.height(windowClass.customSpacerSmall))
                                }
                            }
                        }
                    }
                })

            if (showOrderDetailsDialog) {
                selectedOrder?.let {  // Use selectedOrder instead of order
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
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Refresh),
                    onClick = { viewModel.loadOrdersByUser(userId) },
                    contentDescription = "Refresh"
                )
            }
        }
    )
}

