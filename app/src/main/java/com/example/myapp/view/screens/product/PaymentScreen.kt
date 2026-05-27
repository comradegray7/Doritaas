
@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapp.view.screens.product

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.BenefitType
import com.example.myapp.data.dataclass.CheckoutSummary
import com.example.myapp.data.dataclass.DeliveryAddress
import com.example.myapp.data.dataclass.DeliveryAddressState
import com.example.myapp.data.dataclass.Order
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.ShipmentItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.DeliveryAddressViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.PaymentState
import com.example.myapp.data.model.PaymentViewModel
import com.example.myapp.data.model.PrimeMembershipViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.data.model.ShipmentViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.admin.components.ProductColorSelection
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomCircularProgressIndicator
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomFilterChip
import com.example.myapp.view.components.CustomHorizontalDivider
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomImageContainer
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomLazyRow
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.screens.AddAddressDialog
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CloudinaryHelper
import com.example.myapp.view.utils.CustomShape
import com.example.myapp.view.utils.formatPrice
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheet.Builder
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.delay

// ============================================================
// Validation helpers
// ============================================================

data class CheckoutValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val itemFieldErrors: Map<Int, Set<String>> = emptyMap()
)

fun validateCheckoutItems(
    items: List<ProductItem>,
    address: DeliveryAddress?
): CheckoutValidationResult {
    val errors = mutableListOf<String>()
    val itemFieldErrors = mutableMapOf<Int, MutableSet<String>>()

    if (address == null) {
        errors.add("Please select a delivery address")
    }

    items.forEachIndexed { index, item ->
        val itemErrors = mutableSetOf<String>()
        val label = if (items.size > 1) "Item ${index + 1} (${item.productName})" else item.productName

        if (item.sizes.isNotEmpty() && item.selectedSize.isEmpty()) {
            errors.add("$label: Select a size")
            itemErrors.add("size")
        }
        if (item.colors.isNotEmpty() && item.selectedColor.isEmpty()) {
            errors.add("$label: Select a color")
            itemErrors.add("color")
        }
        if (item.selectedShipment.isEmpty()) {
            errors.add("$label: Select a shipping option")
            itemErrors.add("shipping")
        }

        if (itemErrors.isNotEmpty()) itemFieldErrors[index] = itemErrors
    }

    return CheckoutValidationResult(
        isValid = errors.isEmpty(),
        errors = errors,
        itemFieldErrors = itemFieldErrors
    )
}

// ============================================================
// PaymentScreen
// ============================================================

@Composable
fun PaymentScreen(
    cartItems: List<ProductItem> = emptyList(),
    customerEmail: String? = null,
    customerName: String? = null,
    viewModel: PaymentViewModel = hiltViewModel(),
    addressViewModel: DeliveryAddressViewModel = hiltViewModel(),
    productViewModel: ProductCrudViewModel = hiltViewModel(),
    shipmentViewModel: ShipmentViewModel = hiltViewModel(),
    onBackNavigation: () -> Unit = {},
    productItems: List<ProductItem> = emptyList(),
    onPaymentSuccess: (Order) -> Unit = {},
    primeViewModel: PrimeMembershipViewModel = hiltViewModel(),
    cloudinaryHelper: CloudinaryHelper = CloudinaryHelper(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val sheetConfig by viewModel.sheetConfig.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()
    val currentAmount by viewModel.currentAmount.collectAsState()
    val addressState by addressViewModel.state.collectAsState()
    val orderCreated by viewModel.orderCreated.collectAsState()
    val checkoutSummary by viewModel.checkoutSummary.collectAsState()
    val productUiState by productViewModel.productState.collectAsState()
    val networkState = rememberNetworkState(networkManager)

    val context = LocalContext.current
    val windowSizeConstant = LocalWindowSizeConstant.current

    val initialItems = remember(cartItems, productItems) {
        val sourceItems = productItems.ifEmpty { cartItems }
        sourceItems.map { item ->
            item.copy(
                selectedColor = item.selectedColor,
                selectedSize = item.selectedSize.ifEmpty { item.sizes.firstOrNull() ?: "" }
            )
        }
    }

    var editableCartItems by remember { mutableStateOf(initialItems) }
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    var showCustomerDetailsDialog by remember { mutableStateOf(false) }
    var showAddressSelectionDialog by remember { mutableStateOf(false) }
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var showShipmentDialog by remember { mutableStateOf(false) }
    var showSizeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showQuantityDialog by remember { mutableStateOf(false) }
    var currentEditingItemIndex by remember { mutableIntStateOf(-1) }
    val defaultColor = remember { listOf("Black") }

    var isPaymentTriggered by remember { mutableStateOf(false) }
    var lockedCheckoutItems by remember { mutableStateOf<List<ProductItem>>(emptyList()) }
    var lockedDeliveryAddress by remember { mutableStateOf<DeliveryAddress?>(null) }

    var selectedAddress by remember { mutableStateOf<DeliveryAddress?>(null) }
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }

    val totalAmount = remember(editableCartItems) {
        editableCartItems.sumOf { (it.price * it.quantity) + it.shipmentCost }
    }

    val paymentItems = lockedCheckoutItems.ifEmpty { editableCartItems }
    val paymentAddress = lockedDeliveryAddress ?: selectedAddress

    // Derived validation — updates automatically whenever items or address change
    val validation = remember(editableCartItems, selectedAddress) {
        validateCheckoutItems(editableCartItems, selectedAddress)
    }

    val paymentResultCallback = { paymentResult: PaymentSheetResult ->
        viewModel.updatePaymentState(paymentResult)
        if (paymentResult is PaymentSheetResult.Canceled || paymentResult is PaymentSheetResult.Failed) {
            isPaymentTriggered = false
            lockedCheckoutItems = emptyList()
            lockedDeliveryAddress = null
        }
    }
    val paymentSheet = remember(paymentResultCallback) { Builder(paymentResultCallback) }.build()
    val isLoading = paymentState is PaymentState.FetchConfig

    LaunchedEffect(Unit) {
        primeViewModel.loadPrimeStatus()
        addressViewModel.loadUserAddresses()
        shipmentViewModel.loadShipments()
    }

    LaunchedEffect(Unit) {
        cartItems.forEachIndexed { index, item -> println("Cart item $index: ${item.productName}") }
        productItems.forEachIndexed { index, item -> println("Product item $index: ${item.productName}") }
    }

    LaunchedEffect(editableCartItems) {
        if (!isPaymentTriggered && editableCartItems.isNotEmpty()) {
            viewModel.updateCheckoutSummary(editableCartItems)
        }
    }

    LaunchedEffect(paymentState, sheetConfig) {
        if (isPaymentTriggered && paymentState is PaymentState.Ready && sheetConfig != null) {
            PaymentConfiguration.init(context, sheetConfig!!.publishableKey)
            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret = sheetConfig!!.paymentIntent,
                configuration = PaymentSheet.Configuration(
                    merchantDisplayName = "Doritaas",
                    customer = PaymentSheet.CustomerConfiguration(
                        id = sheetConfig!!.customer,
                        ephemeralKeySecret = sheetConfig!!.ephemeralKey
                    )
                )
            )
        }
    }

    LaunchedEffect(paymentState) {
        if (paymentState is PaymentState.Error) {
            isPaymentTriggered = false
            lockedCheckoutItems = emptyList()
            lockedDeliveryAddress = null
        }
    }

    LaunchedEffect(orderCreated) {
        orderCreated?.let { order ->
            Log.d("PaymentScreen", "Order created successfully: ${order.id}")
            delay(1500L)
            onPaymentSuccess(order)
        }
    }

    LaunchedEffect(addressState.addresses) {
        if (selectedAddress == null && addressState.addresses.isNotEmpty()) {
            selectedAddress = addressState.addresses.find { it.isDefault }
                ?: addressState.addresses.firstOrNull()
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        CustomScaffoldContainer(
            onRefresh = {
                if (networkState.hasInternet) {
                    if (isPaymentTriggered) {
                        currentSnackBarData = SnackBarData(
                            message = "Payment is locked. Cancel payment to change your order.",
                            duration = SnackbarDuration.Short
                        )
                        showSnackBar = true
                    } else if (editableCartItems.isNotEmpty()) {
                        viewModel.updateCheckoutSummary(editableCartItems)
                    }
                } else {
                    currentSnackBarData = SnackBarData(
                        message = "Cannot refresh - No internet connection",
                        isError = true,
                        duration = SnackbarDuration.Short
                    )
                    showSnackBar = true
                }
            },
            onNavigateBack = {
                if (isPaymentTriggered) {
                    currentSnackBarData = SnackBarData(
                        message = "Payment is locked. Cancel payment to change your order.",
                        duration = SnackbarDuration.Short
                    )
                    showSnackBar = true
                } else {
                    onBackNavigation()
                }
            },
            verticalArrangement = Arrangement.Top,
            title = R.string.order_summary_title,
            showBottomBar = false,
            snackBarHostState = snackBarHostState,
            content = {
                if (!networkState.hasInternet) {
                    CustomSpacer()
                    NetworkIndicator(networkState = networkState)
                    CustomSpacer()
                    PaddedSection(
                        alignment = Alignment.CenterHorizontally,
                        content = { NetworkStatusBanner(networkState = networkState) }
                    )
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
                                    .padding(top = windowSizeConstant.baseSize),
                                onDismiss = {
                                    showSnackBar = false
                                    currentSnackBarData = null
                                }
                            )
                        }
                    )
                }

                if (paymentState is PaymentState.FetchConfig && !isPaymentTriggered) {
                    PaddedSection(
                        alignment = Alignment.CenterHorizontally,
                        content = { CustomListCardShimmer() }
                    )
                } else if (paymentState is PaymentState.Error) {
                    PaddedSection(
                        alignment = Alignment.CenterHorizontally,
                        content = {
                            CustomEmptyState(
                                btnLabel = R.string.retry,
                                titleStr = (paymentState as PaymentState.Error).message,
                                onBtnClick = { viewModel.refreshPaymentState() },
                                leadingIcon = Icons.Filled.Error,
                            )
                        }
                    )
                } else {
                    if (editableCartItems.isEmpty()) {
                        CustomEmptyState(
                            title = R.string.no_results,
                            showBtn = false,
                            btnIcon = Icons.Filled.ShoppingCart,
                        )
                    } else {
                        PaddedSection(content = {
                            CustomLazyColumn {

                                // Header
                                item {
                                    Text(
                                        text = if (isPaymentTriggered)
                                            "Payment locked (${paymentItems.size} items)"
                                        else
                                            "Review items (${editableCartItems.size})",
                                        style = windowSizeConstant.titleTextStyle,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = windowSizeConstant.normalVerticalPadding)
                                    )
                                }

                                // Locked banner
                                if (isPaymentTriggered) {
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = windowSizeConstant.baseVerticalPadding),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(windowSizeConstant.basePadding),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CustomIcon(
                                                    icon = Icons.Filled.Lock,
                                                    contentDescription = "Payment locked",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                CustomSpacer(modifier = Modifier.width(windowSizeConstant.baseVerticalPadding))
                                                Text(
                                                    text = "Order details are locked while secure payment is being prepared.",
                                                    style = windowSizeConstant.bodyTextStyle,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }

                                // Image carousel — error border on items with missing fields
                                item {
                                    CustomLazyRow {
                                        items(paymentItems.size) { index ->
                                            val item = paymentItems[index]
                                            val isSelected = selectedItemIndex == index
                                            val hasErrors = validation.itemFieldErrors.containsKey(index)

                                            Card(
                                                modifier = Modifier
                                                    .height(windowSizeConstant.customImageHeight)
                                                    .width(windowSizeConstant.carouselImageWidth)
                                                    .clickable { selectedItemIndex = index },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected)
                                                        MaterialTheme.colorScheme.primaryContainer
                                                    else
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                                border = when {
                                                    hasErrors && !isPaymentTriggered -> BorderStroke(
                                                        windowSizeConstant.borderSize,
                                                        MaterialTheme.colorScheme.error
                                                    )
                                                    isSelected -> BorderStroke(
                                                        windowSizeConstant.borderSize,
                                                        MaterialTheme.colorScheme.primary
                                                    )
                                                    else -> null
                                                }
                                            ) {
                                                CustomImageContainer(
                                                    data = cloudinaryHelper.getImageUrl(item.imageUrl),
                                                    contentDescription = item.productName,
                                                    modifier = Modifier
                                                        .height(windowSizeConstant.customImageHeight)
                                                        .width(windowSizeConstant.carouselImageWidth)
                                                        .clip(CustomShape.mediumShape())
                                                )
                                            }
                                        }
                                    }
                                }

                                // Selected product details
                                item {
                                    if (selectedItemIndex in paymentItems.indices) {
                                        val selectedItem = paymentItems[selectedItemIndex]
                                        val selectedItemFieldErrors =
                                            validation.itemFieldErrors[selectedItemIndex] ?: emptySet()

                                        CustomSpacer()

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(windowSizeConstant.basePadding),
                                                verticalArrangement = Arrangement.spacedBy(
                                                    windowSizeConstant.baseVerticalPadding
                                                )
                                            ) {
                                                DetailRow(label = "Product name", value = selectedItem.productName)
                                                DetailRow(label = "Brand", value = selectedItem.brand)

                                                // Size
                                                if (selectedItem.sizes.isNotEmpty()) {
                                                    EditableDetailRow(
                                                        label = "Size",
                                                        value = selectedItem.selectedSize.ifEmpty { "Not selected" },
                                                        isError = "size" in selectedItemFieldErrors,
                                                        onEditClick = {
                                                            if (!isPaymentTriggered) {
                                                                currentEditingItemIndex = selectedItemIndex
                                                                showSizeDialog = true
                                                            }
                                                        },
                                                        enabled = !isPaymentTriggered
                                                    )
                                                }

                                                // Color
                                                if (selectedItem.colors.isNotEmpty()) {
                                                    EditableDetailRow(
                                                        label = "Color",
                                                        value = selectedItem.selectedColor.ifEmpty { "Not selected" },
                                                        isError = "color" in selectedItemFieldErrors,
                                                        onEditClick = {
                                                            if (!isPaymentTriggered) {
                                                                currentEditingItemIndex = selectedItemIndex
                                                                showColorDialog = true
                                                            }
                                                        },
                                                        enabled = !isPaymentTriggered
                                                    )
                                                }

                                                // Quantity
                                                EditableDetailRow(
                                                    label = "Quantity",
                                                    value = "${selectedItem.quantity}",
                                                    onEditClick = {
                                                        if (!isPaymentTriggered) {
                                                            currentEditingItemIndex = selectedItemIndex
                                                            showQuantityDialog = true
                                                        }
                                                    },
                                                    enabled = !isPaymentTriggered
                                                )

                                                // Shipping
                                                EditableDetailRow(
                                                    label = "Shipping",
                                                    value = selectedItem.selectedShipment.ifEmpty { "Not selected" },
                                                    isError = "shipping" in selectedItemFieldErrors,
                                                    valueColor = when {
                                                        "shipping" in selectedItemFieldErrors ->
                                                            MaterialTheme.colorScheme.error
                                                        selectedItem.shipmentCost == 0.0 -> colors.green
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    },
                                                    onEditClick = {
                                                        if (!isPaymentTriggered) {
                                                            currentEditingItemIndex = selectedItemIndex
                                                            showShipmentDialog = true
                                                        }
                                                    },
                                                    enabled = !isPaymentTriggered
                                                )

                                                // Delivery address (fixed: smart add vs edit routing)
                                                EditableDetailRow(
                                                    label = "Delivery address",
                                                    value = if (paymentAddress != null)
                                                        "${paymentAddress.addressLine1}, ${paymentAddress.city}"
                                                    else
                                                        "Not selected",
                                                    isError = paymentAddress == null,
                                                    valueColor = if (paymentAddress == null)
                                                        MaterialTheme.colorScheme.error
                                                    else
                                                        MaterialTheme.colorScheme.onSurface,
                                                    onEditClick = {
                                                        if (!isPaymentTriggered) {
                                                            if (addressState.addresses.isNotEmpty()) {
                                                                showAddressSelectionDialog = true
                                                            } else {
                                                                showAddAddressDialog = true
                                                            }
                                                        }
                                                    },
                                                    enabled = !isPaymentTriggered
                                                )

                                                DetailRow(
                                                    label = "Item Price",
                                                    value = formatPrice(selectedItem.price)
                                                )

                                                DetailRow(
                                                    label = "Subtotal",
                                                    value = formatPrice(selectedItem.price * selectedItem.quantity),
                                                    labelStyle = windowSizeConstant.titleTextStyle,
                                                    valueStyle = windowSizeConstant.titleTextStyle,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                // Payment / success section
                                if (paymentState is PaymentState.Success) {
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = windowSizeConstant.basePadding),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            CustomIcon(
                                                icon = Icons.Filled.CheckCircle,
                                                contentDescription = "Success",
                                                tint = colors.customColor5,
                                            )
                                            CustomSpacer()
                                            Text(
                                                text = stringResource(R.string.payment_successful),
                                                style = windowSizeConstant.titleTextStyle,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.customColor5
                                            )
                                            CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseVerticalPadding))

                                            if (orderCreated != null) {
                                                Text(
                                                    text = "Order #${orderCreated?.id?.take(8)}... created",
                                                    style = windowSizeConstant.bodyTextStyle,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseVerticalPadding))
                                                Text(
                                                    text = stringResource(R.string.order_details),
                                                    style = windowSizeConstant.labelTextStyle,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            } else {
                                                CustomCircularProgressIndicator(
                                                    modifier = Modifier.size(customSpacing.custom24),
                                                    strokeWidth = customSpacing.custom2,
                                                )
                                                CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseVerticalPadding))
                                                Text(
                                                    text = stringResource(R.string.creating_order),
                                                    style = windowSizeConstant.bodyTextStyle,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    item { CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerMedium)) }
                                } else {
                                    // Prime order summary (single merged card)
                                    checkoutSummary?.let { summary ->
                                        if (summary.isPrimeOrder) {
                                            item {
                                                PrimeOrderSummaryCard(
                                                    summary = summary,
                                                    modifier = Modifier.padding(vertical = windowSizeConstant.baseVerticalPadding)
                                                )
                                            }
                                        }
                                    }

                                    // Validation summary + pay button
                                    item {
                                        PaddedSection(
                                            alignment = Alignment.CenterHorizontally,
                                            content = {
                                                if (!validation.isValid) {
                                                    // Error summary card
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                                        )
                                                    ) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(windowSizeConstant.basePadding),
                                                            verticalArrangement = Arrangement.spacedBy(
                                                                windowSizeConstant.smallVerticalPadding
                                                            )
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                CustomIcon(
                                                                    icon = Icons.Filled.Warning,
                                                                    contentDescription = "Validation errors",
                                                                    tint = MaterialTheme.colorScheme.error
                                                                )
                                                                CustomSpacer(modifier = Modifier.width(windowSizeConstant.baseVerticalPadding))
                                                                Text(
                                                                    text = "Complete the following before paying:",
                                                                    style = windowSizeConstant.bodyTextStyle,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                                )
                                                            }
                                                            validation.errors.forEach { error ->
                                                                Row(
                                                                    verticalAlignment = Alignment.Top,
                                                                    modifier = Modifier.padding(start = windowSizeConstant.basePadding)
                                                                ) {
                                                                    Text(
                                                                        text = "• ",
                                                                        style = windowSizeConstant.bodyTextStyle,
                                                                        color = MaterialTheme.colorScheme.error
                                                                    )
                                                                    Text(
                                                                        text = error,
                                                                        style = windowSizeConstant.bodyTextStyle,
                                                                        color = MaterialTheme.colorScheme.onErrorContainer
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    CustomSpacer()

                                                    // Visually disabled pay button
                                                    CustomButton(
                                                        onClick = { /* blocked — validation failed */ },
                                                        icon = ButtonIcon.Vector(imageVector = Icons.Filled.Lock),
                                                        strLabel = "Proceed to Pay ${formatPrice(checkoutSummary?.total ?: totalAmount)}",
                                                        contentDescription = "payment",
                                                        enabled = false
                                                    )
                                                } else {
                                                    // All fields valid — show loading or button
                                                    if (isPaymentTriggered &&
                                                        (paymentState is PaymentState.FetchConfig || paymentState is PaymentState.Loading)
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center,
                                                            modifier = Modifier.padding(vertical = windowSizeConstant.baseVerticalPadding)
                                                        ) {
                                                            CustomCircularProgressIndicator()
                                                            CustomSpacer()
                                                            Text(
                                                                text = "Securing payment session...",
                                                                style = windowSizeConstant.labelTextStyle,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    } else {
                                                        CustomButton(
                                                            onClick = {
                                                                if (!isPaymentTriggered) {
                                                                    isPaymentTriggered = true
                                                                    lockedCheckoutItems = editableCartItems
                                                                    lockedDeliveryAddress = selectedAddress
                                                                    if (editableCartItems.isNotEmpty()) {
                                                                        viewModel.fetchConfiguration(
                                                                            customerEmail = customerEmail,
                                                                            customerName = customerName,
                                                                            productItems = editableCartItems,
                                                                            deliveryAddress = selectedAddress
                                                                        )
                                                                    } else {
                                                                        viewModel.fetchConfigurationWithAmount(currentAmount)
                                                                    }
                                                                }
                                                            },
                                                            icon = ButtonIcon.Vector(imageVector = Icons.Filled.Lock),
                                                            strLabel = "Proceed to Pay ${formatPrice(checkoutSummary?.total ?: totalAmount)}",
                                                            contentDescription = "payment",
                                                            enabled = !isPaymentTriggered
                                                        )
                                                    }
                                                }

                                                CustomSpacer()

                                                Row(
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    CustomIcon(
                                                        icon = Icons.Filled.Security,
                                                        contentDescription = "Secure payment",
                                                        iconSize = windowSizeConstant.basePadding,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    CustomSpacer(modifier = Modifier.width(windowSizeConstant.baseVerticalPadding))
                                                    Text(
                                                        text = stringResource(R.string.secure_payment_powered_by_stripe),
                                                        style = windowSizeConstant.labelTextStyle,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                CustomSpacer()
                                            }
                                        )
                                    }

                                    item { CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall)) }
                                }
                            }
                        })
                    }
                }
            }
        )

        // Full-screen loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(windowSizeConstant.baseSize)
                        .wrapContentSize(),
                    elevation = CardDefaults.cardElevation(defaultElevation = windowSizeConstant.normalVerticalPadding)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(windowSizeConstant.baseSize)
                    ) {
                        CustomCircularProgressIndicator(
                            strokeWidth = windowSizeConstant.smallVerticalPadding,
                            color = MaterialTheme.colorScheme.primary
                        )
                        CustomSpacer()
                        Text(
                            text = stringResource(R.string.processing_stripe_payment),
                            style = windowSizeConstant.titleTextStyle,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))
                        Text(
                            text = stringResource(R.string.setting_up_secure_payment),
                            style = windowSizeConstant.titleTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Dialogs
        if (showCustomerDetailsDialog) {
            CustomerDetailsDialog(
                customerName = customerName,
                customerEmail = customerEmail,
                selectedAddress = selectedAddress,
                addressState = addressState,
                onEditAddress = {
                    showCustomerDetailsDialog = false
                    showAddressSelectionDialog = true
                },
                onAddNewAddress = {
                    showCustomerDetailsDialog = false
                    showAddAddressDialog = true
                },
                onDismiss = { showCustomerDetailsDialog = false }
            )
        }

        if (showAddressSelectionDialog) {
            AddressSelectionDialog(
                addresses = addressState.addresses,
                selectedAddress = selectedAddress,
                onAddressSelected = { address ->
                    selectedAddress = address
                    showAddressSelectionDialog = false
                },
                onAddNewAddress = {
                    showAddressSelectionDialog = false
                    showAddAddressDialog = true
                },
                onDismiss = { showAddressSelectionDialog = false }
            )
        }

        if (showAddAddressDialog) {
            AddAddressDialog(
                onDismiss = { showAddAddressDialog = false },
                onConfirm = { address ->
                    addressViewModel.createAddress(address)
                    showAddAddressDialog = false
                }
            )
        }

        if (showShipmentDialog && currentEditingItemIndex in editableCartItems.indices) {
            val currentItem = editableCartItems[currentEditingItemIndex]
            ShipmentSelectionDialog(
                shipmentOptions = productUiState.shipmentItem,
                selectedShipment = currentItem.selectedShipment,
                onShipmentSelected = { shipment ->
                    editableCartItems = editableCartItems.toMutableList().apply {
                        this[currentEditingItemIndex] = currentItem.copy(
                            selectedShipment = shipment.name,
                            shipmentCost = shipment.price
                        )
                    }
                    showShipmentDialog = false
                    currentEditingItemIndex = -1
                },
                onDismiss = { showShipmentDialog = false; currentEditingItemIndex = -1 }
            )
        }

        if (showSizeDialog && currentEditingItemIndex in editableCartItems.indices) {
            val currentItem = editableCartItems[currentEditingItemIndex]
            SizeSelectionDialog(
                sizes = currentItem.sizes,
                selectedSize = currentItem.selectedSize,
                onSizeSelected = { size ->
                    editableCartItems = editableCartItems.toMutableList().apply {
                        this[currentEditingItemIndex] = currentItem.copy(selectedSize = size)
                    }
                    showSizeDialog = false
                    currentEditingItemIndex = -1
                },
                onDismiss = { showSizeDialog = false; currentEditingItemIndex = -1 }
            )
        }

        if (showColorDialog && currentEditingItemIndex in editableCartItems.indices) {
            val currentItem = editableCartItems[currentEditingItemIndex]
            ColorSelectionDialog(
                defaultColor = currentItem.colors.ifEmpty { defaultColor },
                selectedColor = currentItem.selectedColor,
                onColorChanged = { color ->
                    editableCartItems = editableCartItems.toMutableList().apply {
                        this[currentEditingItemIndex] = currentItem.copy(selectedColor = color)
                    }
                    showColorDialog = false
                    currentEditingItemIndex = -1
                },
                onDismiss = { showColorDialog = false; currentEditingItemIndex = -1 }
            )
        }

        if (showQuantityDialog && currentEditingItemIndex in editableCartItems.indices) {
            val currentItem = editableCartItems[currentEditingItemIndex]
            QuantitySelectionDialog(
                selectedQuantity = currentItem.quantity,
                onQuantitySelected = { quantity ->
                    editableCartItems = editableCartItems.toMutableList().apply {
                        this[currentEditingItemIndex] = currentItem.copy(quantity = quantity)
                    }
                    showQuantityDialog = false
                    currentEditingItemIndex = -1
                },
                onDismiss = { showQuantityDialog = false; currentEditingItemIndex = -1 }
            )
        }
    }
}

// ============================================================
// HELPER COMPOSABLES
// ============================================================

@Composable
fun EditableDetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isError: Boolean = false,
    onEditClick: () -> Unit,
    enabled: Boolean = true
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val resolvedValueColor = if (isError) MaterialTheme.colorScheme.error else valueColor
    // Use a consistent icon size that is clearly visible — not basePadding which is too small
    val actionIconSize = windowSizeClass.iconSize

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable { onEditClick() } else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label + required error badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = windowSizeClass.bodyTextStyle,
                color = if (isError)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isError) {
                CustomSpacer(modifier = Modifier.width(windowSizeClass.smallVerticalPadding))
                CustomIcon(
                    icon = Icons.Filled.Error,
                    contentDescription = "Required",
                    iconSize = actionIconSize,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(windowSizeClass.smallVerticalPadding)
        ) {
            Text(
                text = value,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = windowSizeClass.bodyTextStyle,
                color = resolvedValueColor,
                fontWeight = FontWeight.Medium
            )

            if (enabled) {
                CustomIcon(
                    // Add icon when nothing is selected, Edit icon when value exists
                    icon = if (isError) Icons.Filled.Add else Icons.Filled.Edit,
                    contentDescription = if (isError) "Add $label" else "Edit $label",
                    iconSize = actionIconSize,
                    tint = if (isError)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            } else {
                CustomIcon(
                    icon = Icons.Filled.Lock,
                    contentDescription = "$label locked",
                    iconSize = actionIconSize,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    labelStyle: TextStyle? = null,
    valueStyle: TextStyle? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val labelTextStyle = labelStyle ?: windowSizeConstant.bodyTextStyle
    val valueTextStyle = valueStyle ?: windowSizeConstant.bodyTextStyle

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = labelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = valueTextStyle, color = valueColor, fontWeight = fontWeight)
    }
}

@Composable
fun ShipmentSelectionDialog(
    shipmentOptions: List<ShipmentItem>,
    selectedShipment: String,
    onShipmentSelected: (ShipmentItem) -> Unit,
    onDismiss: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomAlertDialog(
        scrollable = false,
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(icon = Icons.Filled.LocalShipping, contentDescription = "Shipping", iconSize = windowSizeClass.largeIconSize)
        },
        title = {
            Text(text = stringResource(R.string.shipping_option), style = windowSizeClass.titleTextStyle, fontWeight = FontWeight.Bold)
        },
        text = {
            CustomLazyColumn {
                items(shipmentOptions.size) { index ->
                    val option = shipmentOptions[index]
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onShipmentSelected(option) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedShipment == option.name)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (selectedShipment == option.name)
                            BorderStroke(customSpacing.customHalf, MaterialTheme.colorScheme.primary)
                        else null
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(customSpacing.custom16),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = option.name, fontWeight = FontWeight.Medium, style = windowSizeClass.bodyTextStyle)
                                Text(text = option.deliveryMethod, style = windowSizeClass.bodyTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = if (option.price == 0.0) "FREE" else formatPrice(option.price),
                                style = windowSizeClass.bodyTextStyle,
                                fontWeight = FontWeight.Bold,
                                color = if (option.price == 0.0) colors.green else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            CustomTextButton(onClick = onDismiss, label = R.string.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    )
}

@Composable
fun SizeSelectionDialog(
    sizes: List<String>,
    selectedSize: String,
    onSizeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomAlertDialog(
        scrollable = false,
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(icon = Icons.Filled.FormatSize, contentDescription = "Size", iconSize = windowSizeClass.largeIconSize)
        },
        title = {
            Text(text = stringResource(R.string.select_size), style = windowSizeClass.titleTextStyle, fontWeight = FontWeight.Bold)
        },
        text = {
            CustomLazyRow {
                items(sizes.size) { index ->
                    val size = sizes[index]
                    CustomFilterChip(label = size, isSelected = selectedSize == size, onClick = { onSizeSelected(size) })
                }
            }
        },
        confirmButton = {
            CustomTextButton(onClick = onDismiss, label = R.string.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    )
}

@Composable
fun ColorSelectionDialog(
    onDismiss: () -> Unit,
    selectedColor: String,
    onColorChanged: (String) -> Unit,
    defaultColor: List<String> = emptyList(),
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.select_color, windowSizeClass.titleTextStyle), fontWeight = FontWeight.Bold)
        },
        icon = {
            CustomIcon(icon = Icons.Filled.ColorLens, contentDescription = "Change colors", iconSize = windowSizeClass.largeIconSize, tint = colors.orange)
        },
        text = {
            ProductColorSelection(
                multiSelect = false,
                selectedColor = selectedColor,
                onColorSelected = { color -> color?.let { onColorChanged(it) } },
                defaultColors = defaultColor
            )
        },
        confirmButton = {
            CustomTextButton(onClick = onDismiss, label = R.string.confirm, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    )
}

// ============================================================
// ADDRESS DIALOGS
// ============================================================

@Composable
fun CustomerDetailsDialog(
    customerName: String?,
    customerEmail: String?,
    selectedAddress: DeliveryAddress?,
    addressState: DeliveryAddressState,
    onEditAddress: () -> Unit,
    onAddNewAddress: () -> Unit,
    onDismiss: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(icon = Icons.Filled.PersonAdd, contentDescription = "Person", iconSize = windowSizeClass.largeIconSize)
        },
        title = {
            Text(stringResource(R.string.customer_order_title), style = windowSizeClass.titleTextStyle)
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.customer_information),
                    style = windowSizeClass.bodyTextStyle,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = windowSizeClass.baseVerticalPadding)
                )
                CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

                customerName?.let {
                    Text(
                        text = "Name: $it",
                        style = windowSizeClass.bodyTextStyle,
                        modifier = Modifier.padding(bottom = windowSizeClass.smallVerticalPadding)
                    )
                }
                customerEmail?.let {
                    Text(
                        text = "Email: $it",
                        style = windowSizeClass.bodyTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = windowSizeClass.basePadding)
                    )
                }

                Text(
                    text = stringResource(R.string.delivery_address),
                    style = windowSizeClass.bodyTextStyle,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = windowSizeClass.baseVerticalPadding)
                )

                if (selectedAddress != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = windowSizeClass.normalVerticalPadding),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = windowSizeClass.smallSizes)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(windowSizeClass.normalVerticalPadding)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = selectedAddress.fullName, style = windowSizeClass.bodyTextStyle, fontWeight = FontWeight.Medium)
                                    Text(text = selectedAddress.phoneNumber, style = windowSizeClass.bodyTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = windowSizeClass.smallVerticalPadding))
                                    Text(text = "${selectedAddress.addressLine1}${if (selectedAddress.addressLine2.isNotEmpty()) ", ${selectedAddress.addressLine2}" else ""}", style = windowSizeClass.bodyTextStyle, modifier = Modifier.padding(top = windowSizeClass.smallVerticalPadding))
                                    Text(text = "${selectedAddress.city}, ${selectedAddress.state} ${selectedAddress.zipCode}", style = windowSizeClass.bodyTextStyle, modifier = Modifier.padding(top = windowSizeClass.smallVerticalPadding))
                                    Text(text = selectedAddress.country, style = windowSizeClass.bodyTextStyle, modifier = Modifier.padding(top = windowSizeClass.smallVerticalPadding))
                                }
                                ButtonIconComposable(showBgColor = false, buttonIcon = ButtonIcon.Vector(Icons.Filled.Edit), onClick = onEditAddress, contentDescription = "Edit address")
                            }

                            if (selectedAddress.isDefault) {
                                CustomSpacer(Modifier.height(windowSizeClass.baseVerticalPadding))
                                Surface(shape = CustomShape.mediumShape(), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                                    Text(
                                        text = stringResource(R.string.default_address),
                                        style = windowSizeClass.labelTextStyle,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = windowSizeClass.baseVerticalPadding, vertical = windowSizeClass.smallVerticalPadding)
                                    )
                                }
                            }
                        }
                    }
                } else if (addressState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = windowSizeClass.basePadding), contentAlignment = Alignment.Center) {
                        CustomCircularProgressIndicator(modifier = Modifier.size(windowSizeClass.basePadding), strokeWidth = windowSizeClass.borderSize)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = windowSizeClass.basePadding)) {
                        CustomIcon(icon = Icons.Filled.LocationOff, contentDescription = "Location off", tint = MaterialTheme.colorScheme.onSurfaceVariant, iconSize = windowSizeClass.largeIconSize)
                        CustomSpacer()
                        Text(text = stringResource(R.string.no_address_selected), style = windowSizeClass.bodyTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }
        },
        dismissButton = {
            CustomTextButton(onClick = onDismiss, label = R.string.dismiss, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        confirmButton = {
            CustomTextButton(onClick = onAddNewAddress, label = R.string.add_address)
        }
    )
}

@Composable
fun AddressSelectionDialog(
    addresses: List<DeliveryAddress>,
    selectedAddress: DeliveryAddress?,
    onAddressSelected: (DeliveryAddress) -> Unit,
    onAddNewAddress: () -> Unit,
    onDismiss: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomAlertDialog(
        scrollable = false,
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(icon = Icons.Filled.AddLocationAlt, contentDescription = "Select address", iconSize = windowSizeClass.largeIconSize)
        },
        title = {
            Text(stringResource(R.string.select_delivery_address), style = windowSizeClass.titleTextStyle)
        },
        confirmButton = {
            CustomTextButton(onClick = onAddNewAddress, label = R.string.add_delivery_address)
        },
        dismissButton = {
            CustomTextButton(onClick = onDismiss, label = R.string.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = customSpacing.custom400)) {
                if (addresses.isEmpty()) {
                    CustomEmptyState(
                        title = R.string.no_saved_address,
                        subTitle = R.string.select_delivery_address_to_proceed,
                        showBtn = false,
                        leadingIcon = Icons.Filled.LocationOff,
                    )
                } else {
                    CustomLazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(addresses) { address ->
                            AddressSelectionItem(
                                address = address,
                                isSelected = selectedAddress?.id == address.id,
                                onSelected = { onAddressSelected(address) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = windowSizeClass.baseVerticalPadding)
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun AddressSelectionItem(
    address: DeliveryAddress,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            BorderStroke(windowSizeClass.borderSize, MaterialTheme.colorScheme.primary)
        else
            BorderStroke(windowSizeClass.smallSizes, MaterialTheme.colorScheme.outline),
        onClick = onSelected
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(windowSizeClass.basePadding)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = address.fullName, style = windowSizeClass.bodyTextStyle, fontWeight = FontWeight.Medium)
                    CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))
                    Text(text = address.phoneNumber, style = windowSizeClass.bodyTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))
                    Text(text = "${address.addressLine1}${if (address.addressLine2.isNotEmpty()) ", ${address.addressLine2}" else ""}", style = windowSizeClass.bodyTextStyle)
                    CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))
                    Text(text = "${address.city}, ${address.state} ${address.zipCode}", style = windowSizeClass.bodyTextStyle)
                    CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))
                    Text(text = address.country, style = windowSizeClass.bodyTextStyle)
                }
                if (isSelected) {
                    CustomIcon(icon = Icons.Filled.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                }
            }

            if (address.isDefault) {
                CustomSpacer(Modifier.height(windowSizeClass.baseVerticalPadding))
                Surface(shape = CustomShape.mediumShape(), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                    Text(
                        text = stringResource(R.string.default_address),
                        style = windowSizeClass.labelTextStyle,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = windowSizeClass.normalVerticalPadding, vertical = windowSizeClass.smallVerticalPadding)
                    )
                }
            }
        }
    }
}

// ============================================================
// PRIME COMPOSABLES
// ============================================================

/**
 * PrimeOrderSummaryCard — single card replacing the previous two-card setup.
 *
 * How Prime benefit math works (all values come pre-calculated from the ViewModel):
 *
 *  subtotal          = sum of (price × quantity) for every item — full price, no discounts yet
 *  primeDiscountAmount = subtotal × member discount rate (e.g. 10 %) — set in PrimeMembershipViewModel
 *  tax               = (subtotal − primeDiscountAmount) × tax rate — tax is on the discounted price
 *  shippingCost      = 0.0 when FREE_SHIPPING benefit is active, else the normal shipment price
 *  total             = subtotal − primeDiscountAmount + shippingCost + tax
 *  primeTotalSavings = primeDiscountAmount + (normalShippingCost − shippingCost)
 *                      i.e. money saved on discount PLUS money saved on shipping
 *
 * appliedBenefits is a list of BenefitType entries that were actually triggered:
 *   FREE_SHIPPING     → shippingCost set to 0.0
 *   EXCLUSIVE_DISCOUNT→ primeDiscountAmount > 0
 *   PRIME_REWARDS     → bonus reward points added (savingsAmount = point value)
 */

@Composable
fun PrimeOrderSummaryCard(summary: CheckoutSummary, modifier: Modifier = Modifier) {
    val windowSizeClass = LocalWindowSizeConstant.current
    if (!summary.isPrimeOrder) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(windowSizeClass.smallSizes, colors.customColor9.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeClass.basePadding),
            verticalArrangement = Arrangement.spacedBy(windowSizeClass.baseVerticalPadding)
        ) {
            // ── Prime header
            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomIcon(icon = Icons.Filled.Stars, contentDescription = null, tint = colors.customColor9)
                CustomSpacer(modifier = Modifier.width(windowSizeClass.baseVerticalPadding))
                Column {
                    Text(
                        stringResource(R.string.prime_member_benefits_applied),
                        style = windowSizeClass.titleTextStyle,
                        fontWeight = FontWeight.Bold,
                        color = colors.customColor9
                    )
                    // primeTotalSavings = discount saved + shipping saved
                    Text(
                        "You're saving ${formatPrice(summary.primeTotalSavings)} on this order",
                        style = windowSizeClass.bodyTextStyle,
                        color = colors.customColor5,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Active benefits list (FREE_SHIPPING / EXCLUSIVE_DISCOUNT / PRIME_REWARDS)
            if (summary.appliedBenefits.isNotEmpty()) {
                CustomHorizontalDivider(color = colors.customColor9.copy(alpha = 0.2f))
                summary.appliedBenefits.forEach { benefit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            CustomIcon(
                                icon = when (benefit.benefitType) {
                                    BenefitType.FREE_SHIPPING -> Icons.Filled.LocalShipping
                                    BenefitType.EXCLUSIVE_DISCOUNT -> Icons.Filled.Percent
                                    BenefitType.PRIME_REWARDS -> Icons.Filled.CardGiftcard
                                },
                                contentDescription = null,
                                tint = colors.customColor9,
                                iconSize = windowSizeClass.baseIconSize
                            )
                            CustomSpacer(modifier = Modifier.width(windowSizeClass.baseVerticalPadding))
                            Text(benefit.description, style = windowSizeClass.bodyTextStyle)
                        }
                        // savingsAmount > 0 means this benefit has a monetary value to display
                        if (benefit.savingsAmount > 0) {
                            Text(
                                "-${formatPrice(benefit.savingsAmount)}",
                                style = windowSizeClass.bodyTextStyle,
                                fontWeight = FontWeight.Bold,
                                color = colors.customColor5
                            )
                        }
                    }
                }
            }

            // ── Price breakdown
            CustomHorizontalDivider()

            // subtotal = raw items total (price × qty, no discounts)
            DetailRow(label = stringResource(R.string.sub_total), value = formatPrice(summary.subtotal))

            // primeDiscountAmount = subtotal × member discount rate
            if (summary.primeDiscountAmount > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CustomIcon(icon = Icons.Filled.Stars, contentDescription = null, tint = colors.customColor9, iconSize = windowSizeClass.baseIconSize)
                        CustomSpacer(modifier = Modifier.width(windowSizeClass.smallVerticalPadding))
                        Text(stringResource(R.string.prime_discount), style = windowSizeClass.bodyTextStyle, color = colors.customColor9)
                    }
                    Text("-${formatPrice(summary.primeDiscountAmount)}", style = windowSizeClass.bodyTextStyle, color = colors.customColor5, fontWeight = FontWeight.Bold)
                }
            }

            // tax = (subtotal − primeDiscountAmount) × tax rate
            if (summary.tax > 0) {
                DetailRow(label = stringResource(R.string.tax), value = formatPrice(summary.tax))
            }

            // shippingCost = 0.0 when FREE_SHIPPING benefit applies, otherwise normal rate
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.shipping), style = windowSizeClass.bodyTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (summary.shippingCost == 0.0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.free), style = windowSizeClass.bodyTextStyle, color = colors.customColor9, fontWeight = FontWeight.Bold)
                        CustomSpacer(modifier = Modifier.width(windowSizeClass.smallVerticalPadding))
                        CustomIcon(icon = Icons.Filled.Stars, contentDescription = null, tint = colors.customColor9, iconSize = windowSizeClass.baseIconSize)
                    }
                } else {
                    Text(formatPrice(summary.shippingCost), style = windowSizeClass.bodyTextStyle)
                }
            }

            CustomHorizontalDivider()

            // total = subtotal − primeDiscountAmount + shippingCost + tax
            DetailRow(
                label = stringResource(R.string.total),
                value = formatPrice(summary.total),
                labelStyle = windowSizeClass.titleTextStyle,
                valueStyle = windowSizeClass.titleTextStyle,
                fontWeight = FontWeight.Bold
            )

            // Savings badge — only when there is something to show
            if (summary.primeTotalSavings > 0) {
                Surface(
                    color = colors.customColor9.copy(alpha = 0.1f),
                    shape = CustomShape.mediumShape()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(windowSizeClass.baseVerticalPadding),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomIcon(icon = Icons.Filled.Savings, contentDescription = null, tint = colors.customColor9, iconSize = windowSizeClass.baseIconSize)
                        CustomSpacer(modifier = Modifier.width(windowSizeClass.baseVerticalPadding))
                        Text(
                            "You saved ${formatPrice(summary.primeTotalSavings)} with Prime!",
                            style = windowSizeClass.bodyTextStyle,
                            fontWeight = FontWeight.Bold,
                            color = colors.customColor9
                        )
                    }
                }
            }

            // Estimated delivery (faster for Prime members)
            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomIcon(icon = Icons.Filled.LocalShipping, contentDescription = null, tint = colors.customColor5)
                CustomSpacer(modifier = Modifier.width(windowSizeClass.baseVerticalPadding))
                Text("Estimated delivery: ${summary.estimatedDelivery}", style = windowSizeClass.bodyTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun QuantitySelectionDialog(
    selectedQuantity: Int,
    onQuantitySelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val quantities = (1..10).toList()

    CustomAlertDialog(
        scrollable = false,
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(icon = Icons.Filled.ShoppingCart, contentDescription = "Quantity", iconSize = windowSizeClass.largeIconSize)
        },
        title = {
            Text(text = "Select Quantity", style = windowSizeClass.titleTextStyle, fontWeight = FontWeight.Bold)
        },
        text = {
            CustomLazyRow {
                items(quantities.size) { index ->
                    val quantity = quantities[index]
                    CustomFilterChip(label = "$quantity", isSelected = selectedQuantity == quantity, onClick = { onQuantitySelected(quantity) })
                }
            }
        },
        confirmButton = {
            CustomTextButton(onClick = onDismiss, label = R.string.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    )
}
