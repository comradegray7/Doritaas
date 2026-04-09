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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
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
import com.example.myapp.view.components.TopBarActionsShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.custom.buttons.CustomOutlinedButton
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

// ============================================
// Enhanced PaymentScreen with Editable Options
// ============================================

/**
 * PaymentScreen - Stripe-integrated payment and checkout screen
 *
 * Comprehensive payment processing screen with Stripe integration, allowing users to
 * review order details, edit product options, select delivery address, and complete payment.
 *
 * ## Features
 * - **Stripe Payment Integration**: Secure payment processing via Stripe Payment Sheet
 * - **Editable Product Options**: Modify size, color, and shipping method before payment
 * - **Address Selection**: Choose or add delivery addresses
 * - **Real-time Price Calculation**: Updates total based on selections
 * - **Order Summary**: Detailed breakdown of items, shipping, and totals
 * - **Payment States**: Loading, ready, success, and error handling
 * - **Security Indicators**: Shows secure payment badges
 *
 * ## User Workflow
 * 1. Review cart items with product details
 * 2. Edit size, color, or shipping options if needed
 * 3. Select or add delivery address
 * 4. Review total amount and breakdown
 * 5. Click "Proceed to Pay" to open Stripe payment sheet
 * 6. Complete payment securely
 * 7. Receive order confirmation
 *
 * ## Payment States
 * - **FetchConfig**: Loading payment configuration from Stripe
 * - **Ready**: Payment sheet ready to present
 * - **Success**: Payment completed successfully
 * - **Error**: Payment failed, retry available
 *
 * @param cartItems List of products from cart
 * @param customerEmail Customer's email for Stripe
 * @param customerName Customer's name for Stripe
 * @param viewModel ViewModel for payment operations
 * @param addressViewModel ViewModel for delivery address operations
 * @param productViewModel ViewModel for product data
 * @param shipmentViewModel ViewModel for shipping options
 * @param onBackNavigation Callback for back navigation
 * @param productItems Alternative product list (overrides cartItems)
 * @param onPaymentSuccess Callback when payment succeeds with created order
 *
 * @see PaymentViewModel for payment processing
 * @see com.example.myapp.view.screens.product.order.OrderConfirmationScreen for post-payment screen
 * @see DeliveryAddressViewModel for address management
 */

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
    val productUiState by productViewModel.productState.collectAsState()
    val orderCreated by viewModel.orderCreated.collectAsState()
    val checkoutSummary by viewModel.checkoutSummary.collectAsState()
    val networkState = rememberNetworkState(networkManager)

    val context = LocalContext.current
    val windowSizeConstant = LocalWindowSizeConstant.current

    val initialItems = remember(cartItems, productItems) {
        val sourceItems = productItems.ifEmpty { cartItems }
        sourceItems.map { item ->
            item.copy(
                selectedColor = item.selectedColor,
                selectedSize = item.selectedSize.ifEmpty {
                    item.sizes.firstOrNull() ?: ""
                }
            )
        }
    }

    var editableCartItems by remember { mutableStateOf(initialItems) }
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    // Dialog states
    var showCustomerDetailsDialog by remember { mutableStateOf(false) }
    var showAddressSelectionDialog by remember { mutableStateOf(false) }
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var showShipmentDialog by remember { mutableStateOf(false) }
    var showSizeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var currentEditingItemIndex by remember { mutableIntStateOf(-1) }

    var selectedAddress by remember { mutableStateOf<DeliveryAddress?>(null) }
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    val defaultColor = remember { listOf("Black") }

    val totalAmount = remember(editableCartItems) {
        editableCartItems.sumOf {
            (it.price * it.quantity) + it.shipmentCost
        }
    }

    val paymentResultCallback = { paymentResult: PaymentSheetResult ->
        viewModel.updatePaymentState(paymentResult)
    }

    // One-time setup only - no config fetching here
    LaunchedEffect(Unit) {
        primeViewModel.loadPrimeStatus()
        addressViewModel.loadUserAddresses()
        shipmentViewModel.loadShipments()
    }

    //  Debug logging kept from original
    LaunchedEffect(Unit) {
        cartItems.forEachIndexed { index, item ->
            println("Cart item $index: ${item.productName}")
        }
        productItems.forEachIndexed { index, item ->
            println("Product item $index: ${item.productName}")
        }
    }

    //  Stripe init moved here so it fires only when config actually arrives
    LaunchedEffect(sheetConfig) {
        sheetConfig?.let {
            PaymentConfiguration.init(context, it.publishableKey)
            Log.d("Stripe", "Stripe initialized with key: ${it.publishableKey}")
        }
    }

    // fetchConfigurationWithAmount fallback is preserved
    LaunchedEffect(editableCartItems) {
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

    //  Refetch only when address actually changes, null guard preserved
    LaunchedEffect(selectedAddress) {
        val address = selectedAddress ?: return@LaunchedEffect
        if (editableCartItems.isNotEmpty()) {
            viewModel.fetchConfiguration(
                customerEmail = customerEmail,
                customerName = customerName,
                productItems = editableCartItems,
                deliveryAddress = address
            )
        }
    }

    // Watch for order creation
    LaunchedEffect(orderCreated) {
        orderCreated?.let { order ->
            Log.d("PaymentScreen", "Order created successfully: ${order.id}")
            delay(1500L)
            onPaymentSuccess(order)
        }
    }

    // Watch for address state - set default address
    LaunchedEffect(addressState.addresses) {
        if (selectedAddress == null && addressState.addresses.isNotEmpty()) {
            selectedAddress = addressState.addresses.find { it.isDefault }
                ?: addressState.addresses.firstOrNull()
        }
    }

    // Collect snack bar data
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

    val paymentSheet = remember(paymentResultCallback) { Builder(paymentResultCallback) }.build()
    val isLoading = paymentState is PaymentState.FetchConfig

    Box(modifier = Modifier.fillMaxSize()) {
        CustomScaffoldContainer(
            onRefresh = {
                if (networkState.hasInternet) {
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
                } else {
                    currentSnackBarData = SnackBarData(
                        message = "Cannot refresh - No internet connection",
                        isError = true,
                        duration = SnackbarDuration.Short
                    )
                    showSnackBar = true
                }
            },
            onNavigateBack = { onBackNavigation() },
            verticalArrangement = Arrangement.Top,
            title = R.string.order_summary_title,
            showBottomBar = false,
            floatingBtnContent = {
                CustomOutlinedButton(
                    label = R.string.create_address,
                    icon = ButtonIcon.Vector(Icons.Filled.Add),
                    onClick = {
                        showAddAddressDialog = true
                    },
                    contentDescription = "Delivery address"
                )
            },
            snackBarHostState = snackBarHostState,
            content = {
                if (!networkState.hasInternet) {
                    // Network Indicator in top bar
                    CustomSpacer()

                    NetworkIndicator(networkState = networkState)

                    CustomSpacer()

                    PaddedSection(
                        alignment = Alignment.CenterHorizontally,
                        content = {
                            NetworkStatusBanner(
                                networkState = networkState,
                            )
                        }
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

                when (paymentState) {
                    is PaymentState.FetchConfig -> {
                        PaddedSection(
                            alignment = Alignment.CenterHorizontally,
                            content = {
                                CustomListCardShimmer()
                            })
                    }

                    is PaymentState.Error -> {
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
                    }

                    else -> {
                        if (editableCartItems.isEmpty()) {
                            CustomEmptyState(
                                title = R.string.no_results,
                                showBtn = false,
                                btnIcon = Icons.Filled.ShoppingCart,
                            )
                        } else {
                            PaddedSection(content = {
                                CustomLazyColumn {
                                    item {
                                        Text(
                                            text = "Items (${editableCartItems.size})",
                                            style = windowSizeConstant.titleTextStyle,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(bottom = windowSizeConstant.normalVerticalPadding)
                                        )
                                    }

                                    item {
                                        CustomLazyRow {
                                            items(editableCartItems.size) { index ->
                                                val item = editableCartItems[index]
                                                val isSelected = selectedItemIndex == index

                                                Card(
                                                    modifier = Modifier
                                                        .width(customSpacing.custom180)
                                                        .height(windowSizeConstant.customImageHeight + windowSizeConstant.baseVerticalPadding * 4)
                                                     .clickable { selectedItemIndex = index },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isSelected)
                                                            MaterialTheme.colorScheme.primaryContainer
                                                        else
                                                            MaterialTheme.colorScheme.surfaceVariant
                                                    ),
                                                    border = if (isSelected)
                                                        BorderStroke(
                                                            windowSizeConstant.borderSize,
                                                            MaterialTheme.colorScheme.primary
                                                        )
                                                    else null
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(windowSizeConstant.normalVerticalPadding),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        // Constrain image to a square area to preserve aspect ratio
                                                        CustomImageContainer(
                                                            data = cloudinaryHelper.getImageUrl(item.imageUrl),
                                                            contentDescription = item.productName,
                                                            modifier = Modifier
                                                                .height(windowSizeConstant.customImageHeight)
                                                                .width(customSpacing.custom120)
                                                                .clip(CustomShape.mediumShape())
                                                        )

                                                        CustomSpacer(
                                                            modifier = Modifier.height(
                                                                windowSizeConstant.baseVerticalPadding
                                                            )
                                                        )

                                                        Text(
                                                            text = item.productName,
                                                            style = windowSizeConstant.bodyTextStyle,
                                                            fontWeight = FontWeight.Medium,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Selected Product Details
                                    item {
                                        if (selectedItemIndex in editableCartItems.indices) {
                                            val selectedItem = editableCartItems[selectedItemIndex]

                                            CustomSpacer()

                                            // EDITABLE PRODUCT DETAILS CARD
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                                                    DetailRow(
                                                        label = "Brand",
                                                        value = selectedItem.brand
                                                    )

                                                    if (selectedItem.sizes.isNotEmpty()) {
                                                        EditableDetailRow(
                                                            label = "Size",
                                                            value = selectedItem.selectedSize.ifEmpty { "Not selected" },
                                                            onEditClick = {
                                                                currentEditingItemIndex =
                                                                    selectedItemIndex
                                                                showSizeDialog = true
                                                            }
                                                        )
                                                    }

                                                    if (selectedItem.colors.isNotEmpty()) {
                                                        EditableDetailRow(
                                                            label = "Color",
                                                            value = selectedItem.selectedColor.ifEmpty { "Not selected" },
                                                            onEditClick = {
                                                                currentEditingItemIndex =
                                                                    selectedItemIndex
                                                                showColorDialog = true
                                                            }
                                                        )
                                                    }

                                                    DetailRow(
                                                        label = "Quantity",
                                                        value = "${selectedItem.quantity}"
                                                    )

                                                    EditableDetailRow(
                                                        label = "Shipping",
                                                        value = selectedItem.selectedShipment.ifEmpty { "Not selected" },
                                                        valueColor = if (selectedItem.shipmentCost == 0.0)
                                                            colors.green
                                                        else
                                                            MaterialTheme.colorScheme.onSurface,
                                                        onEditClick = {
                                                            currentEditingItemIndex =
                                                                selectedItemIndex
                                                            showShipmentDialog = true
                                                        }
                                                    )

                                                    DetailRow(
                                                        label = "Shipping Cost",
                                                        value = if (selectedItem.shipmentCost == 0.0)
                                                            "FREE"
                                                        else
                                                            formatPrice(selectedItem.shipmentCost),
                                                        valueColor = if (selectedItem.shipmentCost == 0.0)
                                                            colors.green
                                                        else
                                                            MaterialTheme.colorScheme.onSurface
                                                    )

                                                    DetailRow(
                                                        label = "Item Price",
                                                        value = formatPrice(selectedItem.price)
                                                    )

                                                    DetailRow(
                                                        label = "Subtotal",
                                                        value = formatPrice(selectedItem.price * selectedItem.quantity),
                                                        labelStyle = MaterialTheme.typography.titleMedium,
                                                        valueStyle = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Grand Total Card
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = windowSizeConstant.baseVerticalPadding),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(windowSizeConstant.basePadding)
                                            ) {
                                                val itemsTotal =
                                                    editableCartItems.sumOf { it.price * it.quantity }
                                                val shippingTotal =
                                                    editableCartItems.sumOf { it.shipmentCost }
                                                val grandTotal = itemsTotal + shippingTotal

                                                DetailRow(
                                                    label = stringResource(R.string.items_total),
                                                    value = formatPrice(itemsTotal)
                                                )

                                                CustomSpacer()

                                                DetailRow(
                                                    label = stringResource(R.string.shipping_total),
                                                    value = if (shippingTotal == 0.0) "Normal Shipping" else formatPrice(
                                                        shippingTotal
                                                    ),
                                                    valueColor = if (shippingTotal == 0.0) colors.green else MaterialTheme.colorScheme.onSurface
                                                )

                                                CustomSpacer(
                                                    modifier = Modifier.height(
                                                        windowSizeConstant.baseVerticalPadding
                                                    )
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.total_amount),
                                                        style = windowSizeConstant.titleTextStyle,
                                                        fontWeight = FontWeight.Bold
                                                    )

                                                    CustomSpacer(
                                                        modifier = Modifier.height(
                                                            windowSizeConstant.baseVerticalPadding
                                                        )
                                                    )

                                                    Text(
                                                        text = formatPrice(grandTotal),
                                                        style = windowSizeConstant.titleTextStyle,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        CustomSpacer()
                                    }

                                    // Payment Button Section
                                    when (paymentState) {
                                        is PaymentState.Ready -> {
                                            checkoutSummary?.let { summary ->
                                                if (summary.isPrimeOrder) {
                                                    item {
                                                        PrimeSavingsSummaryCard(
                                                            summary = summary,
                                                            modifier = Modifier.padding(vertical = windowSizeConstant.baseVerticalPadding)
                                                        )

                                                        OrderSummaryWithPrime(
                                                            summary = summary,
                                                            modifier = Modifier.padding(vertical = windowSizeConstant.baseVerticalPadding)
                                                        )
                                                    }
                                                }
                                            }

                                            item {
                                                PaddedSection(
                                                    alignment = Alignment.CenterHorizontally,
                                                    content = {
                                                        if (selectedAddress == null) {
                                                            Card(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                colors = CardDefaults.cardColors(
                                                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                                                )
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(windowSizeConstant.basePadding),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    CustomIcon(
                                                                        icon = Icons.Filled.Warning,
                                                                        contentDescription = "Warning",
                                                                        tint = colors.orange
                                                                    )

                                                                    CustomSpacer(
                                                                        modifier = Modifier.width(
                                                                            windowSizeConstant.baseVerticalPadding
                                                                        )
                                                                    )

                                                                    Text(
                                                                        text = stringResource(R.string.select_delivery_address_to_proceed),
                                                                        style = windowSizeConstant.bodyTextStyle,
                                                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                                                    )
                                                                }
                                                            }
                                                        } else if (sheetConfig != null) {
                                                            CustomButton(
                                                                onClick = {
                                                                    sheetConfig?.let { config ->
                                                                        paymentSheet.presentWithPaymentIntent(
                                                                            paymentIntentClientSecret = config.paymentIntent,
                                                                            configuration = PaymentSheet.Configuration(
                                                                                merchantDisplayName = "Doritaas",
                                                                                customer = PaymentSheet.CustomerConfiguration(
                                                                                    id = config.customer,
                                                                                    ephemeralKeySecret = config.ephemeralKey
                                                                                )
                                                                            )
                                                                        )
                                                                    }
                                                                },
                                                                icon = ButtonIcon.Vector(imageVector = Icons.Filled.Lock),
                                                                strLabel = "Proceed to Pay ${
                                                                    formatPrice(
                                                                        checkoutSummary?.total ?: totalAmount
                                                                    )
                                                                }",
                                                                contentDescription = "payment",
                                                                enabled = selectedAddress != null
                                                            )

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

                                                                CustomSpacer(
                                                                    modifier = Modifier.width(
                                                                        windowSizeConstant.baseVerticalPadding
                                                                    )
                                                                )

                                                                Text(
                                                                    text = stringResource(R.string.secure_payment_powered_by_stripe),
                                                                    style = windowSizeConstant.labelTextStyle,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                            CustomSpacer()

                                                        } else {
                                                            // sheetConfig still loading, show a small loader
                                                            CustomCircularProgressIndicator()
                                                        }
                                                    }
                                                )
                                            }

                                            item {
                                                CustomSpacer(
                                                    modifier = Modifier.height(
                                                        windowSizeConstant.customSpacerMedium
                                                    )
                                                )
                                            }
                                        }

                                        is PaymentState.Success -> {
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

                                                    CustomSpacer(
                                                        modifier = Modifier.height(
                                                            windowSizeConstant.baseVerticalPadding
                                                        )
                                                    )

                                                    if (orderCreated != null) {
                                                        Text(
                                                            text = "Order #${
                                                                orderCreated?.id?.take(
                                                                    8
                                                                )
                                                            }... created",
                                                            style = windowSizeConstant.bodyTextStyle,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )

                                                        CustomSpacer(
                                                            modifier = Modifier.height(
                                                                windowSizeConstant.baseVerticalPadding
                                                            )
                                                        )

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

                                                        CustomSpacer(
                                                            modifier = Modifier.height(
                                                                windowSizeConstant.baseVerticalPadding
                                                            )
                                                        )

                                                        Text(
                                                            text = stringResource(R.string.creating_order),
                                                            style = windowSizeConstant.bodyTextStyle,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }

                                            item {
                                                CustomSpacer(
                                                    modifier = Modifier.height(
                                                        windowSizeConstant.customSpacerMedium
                                                    )
                                                )
                                            }
                                        }

                                        else -> {
                                            item {
                                                CustomEmptyState(
                                                    btnLabel = R.string.retry,
                                                    titleStr = "An error occurred during payment. Please try again.",
                                                    onBtnClick = {
                                                        viewModel.fetchConfiguration(
                                                            customerEmail = customerEmail,
                                                            customerName = customerName,
                                                            productItems = editableCartItems,
                                                            deliveryAddress = selectedAddress
                                                        )
                                                    },
                                                    leadingIcon = Icons.Filled.Error,
                                                    enableScroll = false
                                                )
                                            }
                                            item {
                                                CustomSpacer(
                                                    modifier = Modifier.height(
                                                        windowSizeConstant.customSpacerMedium
                                                    )
                                                )

                                            }
                                        }
                                    }
                                }
                            }
                            )
                        }
                    }
                }
            },
            actions = {
                when (paymentState) {
                    is PaymentState.FetchConfig -> TopBarActionsShimmer()
                    else -> {
                        NetworkIndicator(networkState = networkState)
                        ButtonIconComposable(
                            tint = MaterialTheme.colorScheme.primary,
                            buttonIcon = ButtonIcon.Vector(Icons.Filled.LocationOn),
                            onClick = { showCustomerDetailsDialog = true },
                            contentDescription = "Delivery address"
                        )
                    }
                }
            }
        )

        // Loading overlay
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

        // All dialog code remains the same
        if (showShipmentDialog && currentEditingItemIndex >= 0) {
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
                onDismiss = {
                    showShipmentDialog = false
                    currentEditingItemIndex = -1
                }
            )
        }

        if (showSizeDialog && currentEditingItemIndex >= 0) {
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
                onDismiss = {
                    showSizeDialog = false
                    currentEditingItemIndex = -1
                }
            )
        }

        if (showColorDialog && currentEditingItemIndex >= 0) {
            val currentItem = editableCartItems[currentEditingItemIndex]

            ColorSelectionDialog(
                defaultColor = defaultColor,
                selectedColor = currentItem.selectedColor,
                onColorChanged = { selectedColor ->
                    editableCartItems = editableCartItems.toMutableList().apply {
                        this[currentEditingItemIndex] = currentItem.copy(
                            selectedColor = selectedColor
                        )
                    }
                },
                onDismiss = {
                    showColorDialog = false
                    currentEditingItemIndex = -1
                }
            )
        }

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
    }
}

// ============================================
// HELPER COMPOSABLE
// ============================================

@Composable
fun EditableDetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    onEditClick: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = windowSizeClass.bodyTextStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(windowSizeClass.smallVerticalPadding)
        ) {
            Text(
                text = value,
                style = windowSizeClass.bodyTextStyle,
                color = valueColor,
                fontWeight = FontWeight.Medium
            )

            CustomIcon(
                icon = Icons.Filled.Edit,
                contentDescription = "Edit $label",
                iconSize = windowSizeClass.basePadding,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    labelStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    valueStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = labelStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            fontWeight = fontWeight
        )
    }
}

// ============================================
// SELECTION DIALOGS
// ============================================

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
            CustomIcon(
                icon = Icons.Filled.LocalShipping,
                contentDescription = "Shipping",
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Text(
                text = stringResource(R.string.shipping_option),
                style = windowSizeClass.titleTextStyle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            CustomLazyColumn {
                items(shipmentOptions.size) { index ->
                    val option = shipmentOptions[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onShipmentSelected(option) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedShipment == option.name)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        ),
                        border = if (selectedShipment == option.name)
                            BorderStroke(
                                customSpacing.customHalf,
                                MaterialTheme.colorScheme.primary
                            )
                        else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(customSpacing.custom16),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = option.name,
                                    fontWeight = FontWeight.Medium,
                                    style = windowSizeClass.bodyTextStyle
                                )

                                Text(
                                    text = option.deliveryMethod,
                                    style = windowSizeClass.bodyTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = if (option.price == 0.0) "FREE" else "$${option.price}",
                                style = windowSizeClass.bodyTextStyle,
                                fontWeight = FontWeight.Bold,
                                color = if (option.price == 0.0)
                                    colors.green
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.cancel,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            CustomIcon(
                icon = Icons.Filled.FormatSize,
                contentDescription = "Size",
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Text(
                text = stringResource(R.string.select_size),
                style = windowSizeClass.titleTextStyle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            CustomLazyRow {
                items(sizes.size) { index ->
                    val size = sizes[index]
                    CustomFilterChip(
                        label = size,
                        isSelected = selectedSize == size,
                        onClick = { onSizeSelected(size) }
                    )


                }
            }
        },
        confirmButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.cancel,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        icon = {
            CustomIcon(
                icon = Icons.Filled.Warning,
                contentDescription = "Warning",
                iconSize = windowSizeClass.largeIconSize,
                tint = colors.orange
            )
        },
        text = {
            ProductColorSelection(
                multiSelect = false,
                selectedColor = selectedColor,
                onColorSelected = { color ->
                    color?.let { onColorChanged(it) }
                },
                defaultColors = defaultColor
            )

        },
        confirmButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.confirm,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

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
            CustomIcon(
                icon = Icons.Filled.PersonAdd,
                contentDescription = "Person",
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomIcon(
                    icon = Icons.Filled.Person,
                    contentDescription = "Person",
                    tint = MaterialTheme.colorScheme.primary,
                )

                CustomSpacer(modifier = Modifier.width(windowSizeClass.basePadding))

                Text(
                    stringResource(R.string.customer_order_title),
                    style = windowSizeClass.titleTextStyle
                )
            }
        },
        text = {
            Column {
                // Customer Information Section
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

                // Delivery Address Section
                Text(
                    text = stringResource(R.string.delivery_address),
                    style = windowSizeClass.bodyTextStyle,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = windowSizeClass.baseVerticalPadding)
                )

                if (selectedAddress != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = windowSizeClass.normalVerticalPadding),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = windowSizeClass.smallSizes)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(windowSizeClass.normalVerticalPadding)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedAddress.fullName,
                                        style = windowSizeClass.bodyTextStyle,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = selectedAddress.phoneNumber,
                                        style = windowSizeClass.bodyTextStyle,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = windowSizeClass.smallVerticalPadding)
                                    )

                                    Text(
                                        text = "${selectedAddress.addressLine1}${if (selectedAddress.addressLine2.isNotEmpty()) ", ${selectedAddress.addressLine2}" else ""}",
                                        style = windowSizeClass.bodyTextStyle,
                                        modifier = Modifier.padding(top = windowSizeClass.smallVerticalPadding)
                                    )

                                    Text(
                                        text = "${selectedAddress.city}, ${selectedAddress.state} ${selectedAddress.zipCode}",
                                        style = windowSizeClass.bodyTextStyle,
                                        modifier = Modifier.padding(top = windowSizeClass.smallVerticalPadding)
                                    )

                                    Text(
                                        text = selectedAddress.country,
                                        style = windowSizeClass.bodyTextStyle,
                                        modifier = Modifier.padding(top = windowSizeClass.smallVerticalPadding)
                                    )
                                }

                                ButtonIconComposable(
                                    showBgColor = false,
                                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Edit),
                                    onClick = onEditAddress,
                                    contentDescription = "Edit address"
                                )
                            }

                            if (selectedAddress.isDefault) {
                                CustomSpacer(Modifier.height(windowSizeClass.baseVerticalPadding))
                                Surface(
                                    shape = CustomShape.mediumShape(),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = stringResource(R.string.default_address),
                                        style = windowSizeClass.labelTextStyle,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(
                                            horizontal = windowSizeClass.baseVerticalPadding,
                                            vertical = windowSizeClass.smallVerticalPadding
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else if (addressState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = windowSizeClass.basePadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CustomCircularProgressIndicator(
                            modifier = Modifier.size(windowSizeClass.basePadding),
                            strokeWidth = windowSizeClass.borderSize
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = windowSizeClass.basePadding)
                    ) {
                        CustomIcon(
                            icon = Icons.Filled.LocationOff,
                            contentDescription = "Location off",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            iconSize = windowSizeClass.largeIconSize
                        )

                        CustomSpacer()

                        Text(
                            text = stringResource(R.string.no_address_selected),
                            style = windowSizeClass.bodyTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                    }
                }
            }
        },
        dismissButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.dismiss,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            CustomTextButton(
                onClick = onAddNewAddress,
                label = R.string.add_address,
            )
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
            CustomIcon(
                icon = Icons.Filled.AddLocationAlt,
                contentDescription = "Person",
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Text(
                stringResource(R.string.select_delivery_address),
                style = windowSizeClass.titleTextStyle
            )
        },
        confirmButton = {
            CustomTextButton(
                onClick = onAddNewAddress,
                label = R.string.add_delivery_address,
            )
        },
        dismissButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.cancel,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = customSpacing.custom400)
            ) {
                if (addresses.isEmpty()) {
                    CustomEmptyState(
                        title = R.string.no_saved_address,
                        subTitle = R.string.select_delivery_address_to_proceed,
                        showBtn = false,
                        leadingIcon = Icons.Filled.LocationOff,
                    )
                } else {
                    CustomLazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(addresses) { address ->
                            AddressSelectionItem(
                                address = address,
                                isSelected = selectedAddress?.id == address.id,
                                onSelected = { onAddressSelected(address) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = windowSizeClass.baseVerticalPadding)
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
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(windowSizeClass.borderSize, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(windowSizeClass.smallSizes, MaterialTheme.colorScheme.outline)
        },
        onClick = onSelected
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeClass.basePadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = address.fullName,
                        style = windowSizeClass.bodyTextStyle,
                        fontWeight = FontWeight.Medium
                    )
                    CustomSpacer(modifier = Modifier.width(windowSizeClass.baseVerticalPadding))

                    Text(
                        text = address.phoneNumber,
                        style = windowSizeClass.bodyTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    CustomSpacer(modifier = Modifier.width(windowSizeClass.baseVerticalPadding))

                    Text(
                        text = "${address.addressLine1}${if (address.addressLine2.isNotEmpty()) ", ${address.addressLine2}" else ""}",
                        style = windowSizeClass.bodyTextStyle
                    )

                    CustomSpacer(modifier = Modifier.width(windowSizeClass.baseVerticalPadding))

                    Text(
                        text = "${address.city}, ${address.state} ${address.zipCode}",
                        style = windowSizeClass.bodyTextStyle
                    )

                    CustomSpacer(modifier = Modifier.width(windowSizeClass.baseVerticalPadding))

                    Text(
                        text = address.country,
                        style = windowSizeClass.bodyTextStyle
                    )

                    CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))
                }

                if (isSelected) {
                    CustomIcon(
                        icon = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (address.isDefault) {
                CustomSpacer(Modifier.height(windowSizeClass.baseVerticalPadding))
                Surface(
                    shape = CustomShape.mediumShape(),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = stringResource(R.string.default_address),
                        style = windowSizeClass.labelTextStyle,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            horizontal = windowSizeClass.normalVerticalPadding,
                            vertical = windowSizeClass.smallVerticalPadding
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PrimeSavingsSummaryCard(
    summary: CheckoutSummary,
    modifier: Modifier = Modifier
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    if (!summary.isPrimeOrder) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.customColor9.copy(alpha = 0.1f)
        ),
        border = BorderStroke(windowSizeClass.smallSizes, colors.customColor9.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeClass.basePadding)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomIcon(
                    icon = Icons.Filled.Stars,
                    contentDescription = null,
                    tint = colors.customColor9,
                )

                CustomSpacer(modifier = Modifier.width(windowSizeClass.baseVerticalPadding))

                Column {
                    Text(
                        stringResource(R.string.prime_member_benefits_applied),
                        style = windowSizeClass.titleTextStyle,
                        fontWeight = FontWeight.Bold,
                        color = colors.customColor9
                    )

                    CustomSpacer(
                        modifier = Modifier.height(windowSizeClass.baseVerticalPadding)
                    )

                    Text(
                        "You're saving ${
                            formatPrice(summary.primeTotalSavings)
                        }",
                        style = windowSizeClass.bodyTextStyle,
                        color = colors.customColor5,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (summary.appliedBenefits.isNotEmpty()) {
                CustomSpacer()
                CustomHorizontalDivider(
                    color = colors.customColor9.copy(alpha = 0.2f)
                )

                // Benefits list
                summary.appliedBenefits.forEach { benefit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = windowSizeClass.smallVerticalPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            CustomIcon(
                                icon = when (benefit.benefitType) {
                                    BenefitType.FREE_SHIPPING -> Icons.Filled.LocalShipping
                                    BenefitType.EXCLUSIVE_DISCOUNT -> Icons.Filled.Percent
                                    BenefitType.PRIME_REWARDS -> Icons.Filled.CardGiftcard
                                },
                                contentDescription = null,
                                tint = colors.customColor9,
                                modifier = Modifier.size(windowSizeClass.basePadding)
                            )

                            CustomSpacer(modifier = Modifier.width(windowSizeClass.baseVerticalPadding))

                            Text(
                                benefit.description,
                                style = windowSizeClass.bodyTextStyle
                            )
                        }

                        if (benefit.savingsAmount > 0) {
                            Text(
                                "-$${formatPrice(benefit.savingsAmount)}",
                                style = windowSizeClass.bodyTextStyle,
                                fontWeight = FontWeight.Bold,
                                color = colors.customColor5
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSummaryWithPrime(
    summary: CheckoutSummary,
    modifier: Modifier = Modifier
) {

    val windowSizeClass = LocalWindowSizeConstant.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeClass.basePadding)
        ) {
            Text(
                stringResource(R.string.order_summary),
                style = windowSizeClass.titleTextStyle
            )

            CustomSpacer()

            // Subtotal
            DetailRow(
                valueStyle = windowSizeClass.bodyTextStyle,
                label = stringResource(R.string.sub_total),
                value = formatPrice(summary.subtotal)
            )

            CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

            // Prime Discount
            if (summary.isPrimeOrder && summary.primeDiscountAmount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CustomIcon(
                            icon = Icons.Filled.Stars,
                            contentDescription = "Stars",
                            tint = colors.customColor9,
                            iconSize = windowSizeClass.basePadding
                        )

                        CustomSpacer(modifier = Modifier.width(windowSizeClass.smallVerticalPadding))

                        Text(
                            stringResource(R.string.prime_discount),
                            style = windowSizeClass.bodyTextStyle
                        )
                    }
                    Text(
                        "-$${formatPrice(summary.primeDiscountAmount)}",
                        style = windowSizeClass.bodyTextStyle,
                        color = colors.customColor5,
                        fontWeight = FontWeight.Bold
                    )
                }
                CustomSpacer(modifier = Modifier.height(windowSizeClass.baseVerticalPadding))
            }

            // Tax
            DetailRow(
                valueStyle = windowSizeClass.bodyTextStyle,
                label = stringResource(R.string.tax),
                value = formatPrice(summary.tax)
            )

            CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

            // Shipping
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.shipping),
                    style = windowSizeClass.bodyTextStyle
                )

                if (summary.isPrimeOrder && summary.shippingCost == 0.0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.free),
                            style = windowSizeClass.bodyTextStyle,
                            color = colors.customColor9,
                        )

                        CustomSpacer(modifier = Modifier.width(windowSizeClass.smallVerticalPadding))

                        CustomIcon(
                            icon = Icons.Filled.Stars,
                            contentDescription = "Stars",
                            tint = colors.customColor9,
                            iconSize = windowSizeClass.basePadding
                        )
                    }
                } else {
                    Text(
                        formatPrice(summary.shippingCost),
                        style = windowSizeClass.bodyTextStyle,
                        color = colors.customColor9,
                    )
                }
            }

            CustomHorizontalDivider(
                modifier = Modifier.padding(vertical = windowSizeClass.normalVerticalPadding)
            )

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.total),
                    style = windowSizeClass.titleTextStyle
                )

                Text(
                    formatPrice(summary.total),
                    style = windowSizeClass.bodyTextStyle
                )
            }

            // Prime savings highlight
            if (summary.isPrimeOrder && summary.primeTotalSavings > 0) {
                Surface(
                    color = colors.customColor9.copy(alpha = 0.1f),
                    shape = CustomShape.mediumShape()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(windowSizeClass.baseVerticalPadding),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomIcon(
                            icon = Icons.Filled.Savings,
                            contentDescription = "Savings",
                            tint = colors.customColor9,
                            iconSize = windowSizeClass.basePadding
                        )

                        CustomSpacer(modifier = Modifier.width(windowSizeClass.basePadding))

                        Text(
                            "You saved ${formatPrice(summary.primeTotalSavings)} with Prime!",
                            style = windowSizeClass.bodyTextStyle,
                            fontWeight = FontWeight.Bold,
                            color = colors.customColor9
                        )
                    }
                }
            }

            // Estimated delivery
            CustomSpacer()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomIcon(
                    icon = Icons.Filled.LocalShipping,
                    contentDescription = "Local shipping",
                    tint = if (summary.isPrimeOrder) colors.customColor5 else MaterialTheme.colorScheme.onSurfaceVariant,
                )

                CustomSpacer(modifier = Modifier.width(windowSizeClass.baseVerticalPadding))

                Text(
                    "Estimated delivery: ${summary.estimatedDelivery}",
                    style = windowSizeClass.bodyTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

