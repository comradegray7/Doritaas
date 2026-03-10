package com.example.myapp.view.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.ShipmentItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.ShipmentViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomFloatingPointButton
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomTextField
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.TopBarActionsShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.ButtonIcon
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

/**
 * ShipmentManagementScreen - Shipping method configuration and management
 *
 * Manage shipping options, delivery methods, and pricing for customer orders.
 *
 * ## Features
 * - **Shipment List**: Display all shipping methods with pricing
 * - **Search**: Find shipment options by name or method
 * - **Add Shipment**: Create new shipping options
 * - **Edit Shipment**: Modify shipping details and pricing
 * - **Delete Shipment**: Remove shipping options with confirmation
 * - **Price Display**: Currency-formatted pricing
 * - **Floating Action Button**: Quick access to add new shipment
 *
 * ## Shipment Data
 * Each shipment includes:
 * - Name (e.g., "Standard Shipping", "Express Delivery")
 * - Delivery Method (e.g., "Ground", "Air", "Courier")
 * - Price (formatted as currency)
 *
 * ## User Workflow
 * 1. View list of all shipping options
 * 2. Use search bar to find specific methods
 * 3. Click FAB (+) to add new shipment option
 * 4. Enter shipment name, delivery method, and price
 * 5. Click shipment card to edit
 * 6. Click delete icon to remove option (with confirmation)
 * 7. Pull down to refresh shipment list
 *
 * ## Loading States
 * - Shows shimmer placeholders while loading
 * - Displays error state with retry button on failure
 * - Shows empty state when no shipments exist
 *
 * @param viewModel ViewModel for shipment operations
 * @param onNavigateBack Callback for back navigation
 *
 * @see ShipmentViewModel for shipment data operations
 * @see ShipmentCard for individual shipment display
 * @see AddShipmentDialog for creation dialog
 * @see EditShipmentDialog for editing dialog
 * @see DeleteShipmentDialog for delete confirmation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentManagementScreen(
    viewModel: ShipmentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val shipmentState by viewModel.shipmentState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val networkState = rememberNetworkState(networkManager)

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedShipment by remember { mutableStateOf<ShipmentItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    // Handle snack bar data
    LaunchedEffect(Unit) {
        viewModel.snackBarData.collect { snackBarData ->
            currentSnackBarData = snackBarData
            showSnackBar = true

            // Auto-dismiss after duration (unless indefinite)
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

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
                viewModel.loadShipments()

            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }
        },
        title = R.string.manage_shipments,
        snackBarHostState = snackBarHostState,
        showBottomBar = false,
        verticalArrangement = Arrangement.Top,
        onNavigateBack = { onNavigateBack() },
        floatingBtnContent = {
            CustomFloatingPointButton(
                onClick = {
                    showAddDialog = true
                }
            )
        },
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
                                viewModel.searchShipments(newQuery)
                            } else {
                                viewModel.loadShipments() // Load all when empty
                            }
                        },
                        onSearch = { query ->
                            viewModel.searchShipments(query)
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
                                stringResource(R.string.search_shipments),
                                style = windowSizeClass.bodyTextStyle
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                ButtonIconComposable(
                                    showBgColor = false,
                                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Search),
                                    onClick = {
                                        searchQuery = ""
                                        viewModel.loadShipments()
                                    },
                                    contentDescription = "Search"
                                )
                            }
                        }
                    )
                    CustomSpacer()

                    when {
                        shipmentState.isLoading -> {
                            // Loading state
                            CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))

                            CustomListCardShimmer()
                        }

                        shipmentState.error != null -> {
                            // Error state
                            CustomEmptyState(
                                btnLabel = R.string.retry,
                                subTitle = R.string.shipment_error,
                                onBtnClick = { viewModel.loadShipments() },
                                leadingIcon = Icons.Filled.Error,
                            )
                        }

                        shipmentState.shipments.isEmpty() -> {
                            CustomEmptyState(
                                titleStr = if (searchQuery.isEmpty()) "No shipment yet" else "No results found",
                                showBtn = false,
                                leadingIcon = Icons.Filled.SearchOff
                            )
                        }

                        else -> {
                            // Content state (no loading, no error)
                            CustomLazyColumn {

                                items(shipmentState.shipments) { shipment ->
                                    ShipmentCard(
                                        shipment = shipment,
                                        onEdit = {
                                            selectedShipment = shipment
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            selectedShipment = shipment
                                            showDeleteDialog = true
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
            // Dialogs (keep outside the main conditional)
            if (showAddDialog) {
                AddShipmentDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, method, price ->
                        viewModel.createShipment(name, method, price)
                        showAddDialog = false
                    }
                )
            }

            if (showEditDialog && selectedShipment != null) {
                EditShipmentDialog(
                    shipment = selectedShipment!!,
                    onDismiss = {
                        showEditDialog = false
                        selectedShipment = null
                    },
                    onConfirm = { name, method, price ->
                        viewModel.updateShipment(selectedShipment!!.id, name, method, price)
                        showEditDialog = false
                        selectedShipment = null
                    }
                )
            }

            if (showDeleteDialog && selectedShipment != null) {
                DeleteShipmentDialog(
                    shipment = selectedShipment!!,
                    onDismiss = {
                        showDeleteDialog = false
                        selectedShipment = null
                    },
                    onConfirm = {
                        viewModel.deleteShipment(selectedShipment!!.id, selectedShipment!!.name)
                        showDeleteDialog = false
                        selectedShipment = null
                    }
                )
            }
        },
        actions = {
            if (shipmentState.isLoading) {
                TopBarActionsShimmer()
            } else {
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                ButtonIconComposable(
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Refresh),
                    onClick = { viewModel.loadShipments() },
                    contentDescription = "Refresh"
                )
            }
        }
    )
}

/**
 * ShipmentCard - Individual shipment option display card
 *
 * Displays shipping method with name, delivery method, and pricing.
 *
 * @param shipment The shipment data to display
 * @param onEdit Callback when edit is clicked
 * @param onDelete Callback when delete is clicked
 */
@Composable
fun ShipmentCard(
    shipment: ShipmentItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Card(
        modifier = windowSizeConstant.adaptiveWidthModifier
            .clickable(onClick = onEdit)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeConstant.listCardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    CustomIcon(
                        icon = Icons.Filled.LocalShipping,
                        contentDescription = "Local shipping",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = windowSizeConstant.smallVerticalPadding)
                    )

                    CustomSpacer(modifier = Modifier.width(windowSizeConstant.baseNormalVerticalPadding))

                    Column {
                        Text(
                            text = shipment.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = windowSizeConstant.titleTextStyle,
                        )

                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomIcon(
                                icon = Icons.Filled.DeliveryDining,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            CustomSpacer(modifier = Modifier.width(windowSizeConstant.smallVerticalPadding))

                            Text(
                                text = shipment.deliveryMethod,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = windowSizeConstant.bodyTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

                        Text(
                            text = currencyFormatter.format(shipment.price),
                            style = windowSizeConstant.bodyTextStyle,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Action Buttons
                Row {
                    ButtonIconComposable(
                        showBgColor = false,
                        buttonIcon = ButtonIcon.Vector(Icons.Filled.Edit),
                        onClick = { onEdit() },
                        contentDescription = "Edit"
                    )

                    ButtonIconComposable(
                        showBgColor = false,
                        buttonIcon = ButtonIcon.Vector(Icons.Filled.Delete),
                        onClick = { onDelete() },
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }
}

/**
 * AddShipmentDialog - Dialog for creating new shipping methods
 *
 * Includes fields for shipment name, delivery method, and price with validation.
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when shipment is created, receives name, method, and price
 */
@Composable
fun AddShipmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double) -> Unit
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    var name by remember { mutableStateOf("") }
    var deliveryMethod by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var methodError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.LocalShipping,
                contentDescription = "Add shipping",
                iconSize = windowSizeConstant.largeIconSize
            )
        },
        title = {
            Text(
                stringResource(R.string.add_shipment_method),
                style = windowSizeConstant.titleTextStyle
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding)
            ) {
                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.shipment_name,
                    placeholder = R.string.shipment_label,
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    isError = nameError,
                    errorMessage = if (nameError) stringResource(R.string.shipment_empty_state) else ""
                )

                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.delivery_method,
                    placeholder = R.string.shipment_method_label,
                    value = deliveryMethod,
                    onValueChange = {
                        deliveryMethod = it
                        methodError = it.isBlank()
                    },
                    isError = methodError,
                    errorMessage = if (methodError) stringResource(R.string.delivery_method_state) else ""
                )

                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.price,
                    placeholder = R.string.placeholder_Price,
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        priceError = it.toDoubleOrNull() == null || it.toDouble() < 0
                    },
                    isError = priceError,
                    errorMessage = if (priceError) stringResource(R.string.price_state) else "",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIconContent = {
                        Text(
                            text = stringResource(R.string.price_tag),
                            style = windowSizeConstant.bodyTextStyle,
                            modifier = Modifier.padding(end = windowSizeConstant.baseNormalVerticalPadding)
                        )
                    }
                )
            }
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
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    when {
                        name.isBlank() -> nameError = true
                        deliveryMethod.isBlank() -> methodError = true
                        price == null || price < 0 -> priceError = true
                        else -> onConfirm(name, deliveryMethod, price)
                    }
                },
                label = R.string.add_shipment_method
            )
        }
    )
}

/**
 * EditShipmentDialog - Dialog for editing existing shipping methods
 *
 * Pre-filled with current shipment data.
 *
 * @param shipment The shipment to edit
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when shipment is updated, receives new name, method, and price
 */
@Composable
fun EditShipmentDialog(
    shipment: ShipmentItem,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double) -> Unit
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    var name by remember { mutableStateOf(shipment.name) }
    var deliveryMethod by remember { mutableStateOf(shipment.deliveryMethod) }
    var priceText by remember { mutableStateOf(shipment.price.toString()) }
    var nameError by remember { mutableStateOf(false) }
    var methodError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.LocalShipping,
                contentDescription = "Edit shipping",
                iconSize = windowSizeConstant.largeIconSize
            )
        },
        title = {
            Text(
                stringResource(R.string.edit_shipment),
                style = windowSizeConstant.titleTextStyle
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(windowSizeConstant.baseNormalVerticalPadding)
            ) {
                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.shipment_name,
                    placeholder = R.string.shipment_label,
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    isError = nameError,
                    errorMessage = if (nameError) stringResource(R.string.shipment_empty_state) else ""
                )

                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.delivery_method,
                    placeholder = R.string.shipment_method_label,
                    value = deliveryMethod,
                    onValueChange = {
                        deliveryMethod = it
                        methodError = it.isBlank()
                    },
                    isError = methodError,
                    errorMessage = if (methodError) stringResource(R.string.delivery_method_state) else ""
                )

                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.price,
                    placeholder = R.string.placeholder_Price,
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        priceError = it.toDoubleOrNull() == null || it.toDouble() < 0
                    },
                    isError = priceError,
                    errorMessage = if (priceError) stringResource(R.string.price_state) else "",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIconContent = {
                        Text(
                            text = stringResource(R.string.price_tag),
                            style = windowSizeConstant.bodyTextStyle,
                            modifier = Modifier.padding(end = windowSizeConstant.baseVerticalPadding)
                        )
                    }
                )
            }
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
                label = R.string.edit_shipment,
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    when {
                        name.isBlank() -> nameError = true
                        deliveryMethod.isBlank() -> methodError = true
                        price == null || price < 0 -> priceError = true
                        else -> onConfirm(name, deliveryMethod, price)
                    }
                }
            )
        }
    )
}

/**
 * DeleteShipmentDialog - Shipment deletion confirmation dialog
 *
 * @param shipment The shipment to delete
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when deletion is confirmed
 */
@Composable
fun DeleteShipmentDialog(
    shipment: ShipmentItem,
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
                stringResource(R.string.delete_shipment),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Text(
                "Are you sure you want to delete '${shipment.name}'? This action cannot be undone.",
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
                label = R.string.delete_shipment,
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}
