package com.example.myapp.view.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.CategoryItem
import com.example.myapp.data.dataclass.DiscountType
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.ProductTag
import com.example.myapp.data.dataclass.PromotionsData
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.PromotionViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomFloatingPointButton
import com.example.myapp.view.components.CustomHorizontalDivider
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomImageContainer
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomRadioButton
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomTextField
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.TopBarActionsShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CloudinaryHelper
import com.example.myapp.view.utils.CustomShape
import com.example.myapp.view.utils.formatDate
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * PromotionManagementScreen - Promotional campaign management
 *
 * Create and manage promotional offers and special deals for products.
 *
 * ## Features
 * - **Promotion List**: Display all active and past promotions
 * - **Search**: Find promotions by title
 * - **Add Promotion**: Create new promotional campaigns
 * - **Edit Promotion**: Modify promotion details
 * - **Delete Promotion**: Remove promotions with confirmation
 * - **Floating Action Button**: Quick access to add new promotion
 *
 * ## Promotion Data
 * Each promotion includes:
 * - Title (e.g., "Summer Sale", "Black Friday Deal")
 * - Description (detailed information about the promotion)
 *
 * ## User Workflow
 * 1. View list of all promotions
 * 2. Use search bar to find specific promotions
 * 3. Click FAB (+) to add new promotion
 * 4. Enter promotion title and description
 * 5. Click promotion card to edit
 * 6. Click delete icon to remove promotion (with confirmation)
 * 7. Pull down to refresh promotion list
 *
 * ## Loading States
 * - Shows shimmer placeholders while loading
 * - Displays error state with retry button on failure
 * - Shows empty state when no promotions exist
 *
 * @param viewModel ViewModel for promotion operations
 * @param onNavigateBack Callback for back navigation
 *
 * @see PromotionViewModel for promotion data operations
 * @see AddPromotionDialog for creation dialog
 * @see EditPromotionDialog for editing dialog
 * @see DeletePromotionDialog for delete confirmation
 */
// ============================================================================
// MAIN PROMOTION MANAGEMENT SCREEN
// ============================================================================

@Composable
fun PromotionManagementScreen(
    viewModel: PromotionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToPromotionDetails: (String) -> Unit = {},
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val promotionState by viewModel.promotionState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val networkState = rememberNetworkState(networkManager)

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPromotion by remember { mutableStateOf<PromotionsData?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val windowSizeClass = LocalWindowSizeConstant.current

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

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

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
                viewModel.loadPromotions()
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }
        },
        title = R.string.manage_promotions,
        snackBarHostState = snackBarHostState,
        showBottomBar = false,
        verticalArrangement = Arrangement.Top,
        onNavigateBack = { onNavigateBack() },
        floatingBtnContent = {
            CustomFloatingPointButton(
                onClick = { showAddDialog = true }
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
                            if (newQuery.isNotEmpty()) {
                                viewModel.searchPromotions(newQuery)
                            } else {
                                viewModel.loadPromotions()
                            }
                        },
                        onSearch = { query ->
                            viewModel.searchPromotions(query)
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
                                stringResource(R.string.search_promotions),
                                style = windowSizeClass.bodyTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                ButtonIconComposable(
                                    showBgColor = false,
                                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Close),
                                    onClick = {
                                        searchQuery = ""
                                        viewModel.loadPromotions()
                                    },
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    )
                    CustomSpacer()

                    when {
                        promotionState.isLoading -> {
                            CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))

                            CustomListCardShimmer()
                        }

                        promotionState.error != null -> {
                            CustomEmptyState(
                                btnLabel = R.string.retry,
                                subTitle = R.string.promotions_error,
                                onBtnClick = { viewModel.loadPromotions() },
                                leadingIcon = Icons.Filled.Error,
                            )
                        }

                        promotionState.promotions.isEmpty() -> {
                            CustomEmptyState(
                                titleStr = if (searchQuery.isEmpty()) "No Promotions yet" else "No results found",
                                showBtn = false,
                                leadingIcon = Icons.Filled.SearchOff,
                            )
                        }

                        else -> {
                            CustomLazyColumn {

                                items(
                                    items = promotionState.promotions,
                                    key = { promotion -> promotion.id }
                                ) { promotion ->
                                    EnhancedPromotionCard(
                                        promotion = promotion,
                                        onEdit = {
                                            selectedPromotion = promotion
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            selectedPromotion = promotion
                                            showDeleteDialog = true
                                        },
                                        onManageProducts = {
                                            onNavigateToPromotionDetails(promotion.id)
                                        }
                                    )
                                }
                                item {
                                    CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))
                                }
                            }
                        }
                    }
                }
            )

            // Dialogs
            if (showAddDialog) {
                AddPromotionDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { promotionData ->
                        viewModel.createPromotion(promotionData)
                        showAddDialog = false
                    }
                )
            }

            if (showEditDialog && selectedPromotion != null) {
                EditPromotionDialog(
                    promotion = selectedPromotion!!,
                    onDismiss = {
                        showEditDialog = false
                        selectedPromotion = null
                    },
                    onConfirm = { updatedPromotion ->
                        viewModel.updatePromotion(updatedPromotion)
                        showEditDialog = false
                        selectedPromotion = null
                    }
                )
            }

            if (showDeleteDialog && selectedPromotion != null) {
                DeletePromotionDialog(
                    promotion = selectedPromotion!!,
                    onDismiss = {
                        showDeleteDialog = false
                        selectedPromotion = null
                    },
                    onConfirm = {
                        viewModel.deletePromotion(selectedPromotion!!.id, selectedPromotion!!.title)
                        showDeleteDialog = false
                        selectedPromotion = null
                    }
                )
            }
        },
        actions = {
            if (promotionState.isLoading) {
                TopBarActionsShimmer()
            } else {
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Refresh),
                    onClick = { viewModel.loadPromotions() },
                    contentDescription = "Refresh"
                )
            }
        }
    )
}

// ============================================================================
// ENHANCED PROMOTION CARD (with product count)
// ============================================================================

/**
 * EnhancedPromotionCard - Visual display for promotional campaigns
 *
 * Shows promotion status (Active/Expired), discount details, period, and description.
 *
 * @param promotion The promotion data to display
 * @param onEdit Callback when edit icon is clicked
 * @param onDelete Callback when delete icon is clicked
 * @param onManageProducts Callback when card is clicked to manage associated products
 */
@Composable
fun EnhancedPromotionCard(
    promotion: PromotionsData,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onManageProducts: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val currentTime = System.currentTimeMillis()

    Card(
        modifier = windowSizeClass.adaptiveWidthModifier
            .clickable(onClick = onManageProducts),
        colors = CardDefaults.cardColors(
            containerColor = if (promotion.expired)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (promotion.expired)
            BorderStroke(windowSizeClass.smallSizes, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
        else
            BorderStroke(
                windowSizeClass.smallSizes,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )

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
                // Left side
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(windowSizeClass.baseVerticalPadding)
                    ) {
                        CustomIcon(
                            icon = Icons.Filled.LocalOffer,
                            contentDescription = null,
                            tint = if (promotion.expired)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                        )

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (promotion.expired)
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = CustomShape.mediumShape()
                                )
                                .padding(
                                    horizontal = windowSizeClass.baseVerticalPadding,
                                    vertical = windowSizeClass.borderSize
                                )
                        ) {
                            Text(
                                text = if (promotion.expired) "EXPIRED" else "ACTIVE",
                                style = windowSizeClass.labelTextStyle,
                                color = if (promotion.expired)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary,
                            )
                        }

                        //  Discount Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                    shape = CustomShape.mediumShape()
                                )
                                .padding(
                                    horizontal = windowSizeClass.baseVerticalPadding,
                                    vertical = windowSizeClass.borderSize
                                )
                        ) {
                            Text(
                                text = when (promotion.discountType) {
                                    DiscountType.PERCENTAGE -> "${promotion.discountValue.toInt()}% OFF"
                                    DiscountType.FIXED_AMOUNT -> "$${promotion.discountValue.toInt()} OFF"
                                    DiscountType.BUY_X_GET_Y -> "BUY ${promotion.discountValue.toInt()} GET 1"
                                    DiscountType.FREE_SHIPPING -> "FREE SHIP"
                                },
                                style = windowSizeClass.labelTextStyle,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    CustomSpacer(modifier = Modifier.height(windowSizeClass.baseVerticalPadding))

                    Text(
                        text = promotion.title,
                        style = windowSizeClass.titleTextStyle,
                        fontWeight = FontWeight.Bold
                    )

                    CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

                    Text(
                        text = promotion.description,
                        style = windowSizeClass.bodyTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    CustomSpacer(modifier = Modifier.height(windowSizeClass.basePadding))

                    // Date info
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(windowSizeClass.basePadding)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(windowSizeClass.smallVerticalPadding)
                        ) {
                            CustomIcon(
                                icon = Icons.Outlined.CalendarToday,
                                contentDescription = "Start Date",
                                iconSize = windowSizeClass.basePadding,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = formatDate(promotion.startAt),
                                style = windowSizeClass.labelTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(windowSizeClass.smallVerticalPadding)
                        ) {
                            CustomIcon(
                                icon = Icons.Outlined.EventAvailable,
                                contentDescription = "End Date",
                                iconSize = windowSizeClass.basePadding,
                                tint = if (promotion.expired)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

                            Text(
                                text = formatDate(promotion.endAt),
                                style = windowSizeClass.labelTextStyle,
                                color = if (promotion.expired)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

                    // Days remaining
                    val daysDifference =
                        ((promotion.endAt - currentTime) / (1000 * 60 * 60 * 24)).toInt()
                    if (!promotion.expired && daysDifference >= 0) {
                        Text(
                            text = when (daysDifference) {
                                0 -> "⏰ Expires today"
                                1 -> "⏰ Expires in 1 day"
                                else -> "⏰ Expires in $daysDifference days"
                            },
                            style = windowSizeClass.labelTextStyle,
                            color = if (daysDifference <= 3)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    CustomSpacer(modifier = Modifier.height(windowSizeClass.baseVerticalPadding))

                }

                // Right side actions
                Column {
                    ButtonIconComposable(
                        showBgColor = false,
                        buttonIcon = ButtonIcon.Vector(Icons.Filled.Edit),
                        onClick = onEdit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    CustomSpacer(modifier = Modifier.height(windowSizeClass.baseVerticalPadding))

                    ButtonIconComposable(
                        showBgColor = false,
                        buttonIcon = ButtonIcon.Vector(Icons.Filled.Delete),
                        onClick = onDelete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// ============================================================================
// ADD PRODUCT TO PROMOTION DIALOG
// ============================================================================

/**
 * AddProductToPromotionDialog - Selection tool for adding products to a promotion
 *
 * Provides a searchable list of available products with checkbox selection.
 *
 * @param availableProducts List of products that can be added
 * @param onDismiss Callback to close dialog
 * @param onConfirm Callback with selected product IDs
 * @param viewModel ViewModel for data operations
 */
@Composable
fun AddProductToPromotionDialog(
    availableProducts: List<ProductItem>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit,
    viewModel: PromotionViewModel = hiltViewModel(),
    cloudinaryHelper: CloudinaryHelper = CloudinaryHelper()
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    var selectedProductIds by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(searchQuery, availableProducts) {
        if (searchQuery.isBlank()) {
            availableProducts
        } else {
            availableProducts.filter {
                it.productName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Add,
                contentDescription = "Add products",
                iconSize = windowSizeConstant.largeIconSize
            )
        },
        title = {
            Text(
                "Add Products",
                style = windowSizeConstant.titleTextStyle
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = customSpacing.custom400)
            ) {
                CustomSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {},
                    leadingIcon = {
                        CustomIcon(
                            icon = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    placeholder = {
                        Text(
                            stringResource(R.string.search_promotions),
                            style = windowSizeConstant.bodyTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            ButtonIconComposable(
                                showBgColor = false,
                                buttonIcon = ButtonIcon.Vector(Icons.Filled.Close),
                                onClick = {
                                    searchQuery = ""
                                    viewModel.loadPromotions()
                                },
                                contentDescription = "Clear"
                            )
                        }
                    }
                )

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                Text(
                    text = "${selectedProductIds.size} selected",
                    style = windowSizeConstant.bodyTextStyle,
                    color = MaterialTheme.colorScheme.primary
                )

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                CustomLazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        val isSelected = selectedProductIds.contains(product.id)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedProductIds = if (isSelected) {
                                        selectedProductIds - product.id
                                    } else {
                                        selectedProductIds + product.id
                                    }
                                }
                                .padding(windowSizeConstant.normalVerticalPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )

                            if(product.imageUrl.isNotEmpty()){
                            CustomImageContainer(
                                data = cloudinaryHelper.getImageUrl(product.imageUrl),
                                contentDescription = product.productName,
                                shape = CustomShape.mediumShape(),
                                modifier = Modifier.size(windowSizeConstant.customSpacerSmall)
                            )}
                            else {
                                CustomIcon(
                                    icon = Icons.Filled.Image,
                                    iconSize = windowSizeConstant.customSpacerSmall,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.productName,
                                    style = windowSizeConstant.bodyTextStyle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "$${product.price}",
                                    style = windowSizeConstant.bodyTextStyle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.cancel,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            CustomTextButton(
                onClick = { onConfirm(selectedProductIds.toList()) },
                label = R.string.add_products,
                enabled = selectedProductIds.isNotEmpty()
            )
        }
    )
}

// ============================================================================
// ADD/EDIT PROMOTION DIALOGS (with discount configuration)
// ============================================================================

/**
 * AddPromotionDialog - Form for creating new promotional campaigns
 *
 * Configure title, description, discount type, value, and validity period.
 *
 * @param onDismiss Callback to close dialog
 * @param onConfirm Callback with new promotion data
 */
@Composable
fun AddPromotionDialog(
    onDismiss: () -> Unit,
    onConfirm: (PromotionsData) -> Unit
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf(DiscountType.PERCENTAGE) }
    var discountValue by remember { mutableStateOf("") }
    var minPurchase by remember { mutableStateOf("") }
    var maxDiscount by remember { mutableStateOf("") }

    val startDateCalendar = remember { Calendar.getInstance() }
    val endDateCalendar = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
    }

    var titleError by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf("") }
    var discountError by remember { mutableStateOf("") }
    var endDateError by remember { mutableStateOf("") }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Campaign,
                contentDescription = "Add campaign",
                iconSize = windowSizeConstant.largeIconSize
            )
        },
        title = {
            Text(
                "Create New Promotion",
                style = windowSizeConstant.titleTextStyle
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(windowSizeConstant.baseNormalVerticalPadding)
            ) {
                // Title
                CustomTextField(
                    label = R.string.title,
                    placeholder = R.string.promotions_label,
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = if (it.isBlank()) "Title required" else ""
                    },
                    isError = titleError.isNotEmpty(),
                    errorMessage = titleError
                )

                // Description
                CustomTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        descriptionError = if (it.isBlank()) "Description required" else ""
                    },
                    label = R.string.description,
                    placeholder = R.string.promotion_details,
                    isError = descriptionError.isNotEmpty(),
                    supportingText = { if (descriptionError.isNotEmpty()) Text(descriptionError) },
                    minLines = 2,
                    maxLines = 4
                )

                CustomHorizontalDivider()

                // Discount Type
                Text(
                    text = stringResource(R.string.discount_type),
                    style = windowSizeConstant.bodyTextStyle,
                )

                Column(verticalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding)) {
                    DiscountType.entries.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { discountType = type }
                                .padding(vertical = windowSizeConstant.smallVerticalPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomRadioButton(
                                selected = discountType == type,
                                onClick = { discountType = type }
                            )

                            Text(
                                text = when (type) {
                                    DiscountType.PERCENTAGE -> "Percentage Off (e.g., 20% off)"
                                    DiscountType.FIXED_AMOUNT -> "Fixed Amount (e.g., $20 off)"
                                    DiscountType.BUY_X_GET_Y -> "Buy X Get Y Free"
                                    DiscountType.FREE_SHIPPING -> "Free Shipping"
                                },
                                style = windowSizeConstant.bodyTextStyle,
                                modifier = Modifier.padding(start = windowSizeConstant.normalVerticalPadding)
                            )
                        }
                    }
                }

                // Discount Value
                if (discountType != DiscountType.FREE_SHIPPING) {
                    CustomTextField(
                        value = discountValue,
                        onValueChange = {
                            discountValue = it
                            discountError = when {
                                it.isBlank() -> "Discount value required"
                                it.toDoubleOrNull() == null -> "Invalid number"
                                discountType == DiscountType.PERCENTAGE && (it.toDouble() < 0 || it.toDouble() > 100) ->
                                    "Percentage must be 0-100"
                                else -> ""
                            }
                        },
                        labelStr = when (discountType) {
                            DiscountType.PERCENTAGE -> "Percentage (%)"
                            DiscountType.FIXED_AMOUNT -> "Amount ($)"
                            DiscountType.BUY_X_GET_Y -> "Quantity to Buy"
                            else -> "Value"
                        },
                        placeholderUnit = {  // Use placeholderUnit for dynamic content
                            Text(
                                text = when (discountType) {
                                    DiscountType.PERCENTAGE -> "e.g., 20"
                                    DiscountType.FIXED_AMOUNT -> "e.g., 50"
                                    DiscountType.BUY_X_GET_Y -> "e.g., 2"
                                    else -> ""
                                },
                                style = windowSizeConstant.bodyTextStyle
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = discountError.isNotEmpty(),
                    )
                }

                // Min Purchase (optional)
                CustomTextField(
                    value = minPurchase,
                    onValueChange = { minPurchase = it },
                    label = R.string.minimum_purchase,
                    placeholder = R.string.no_minimum,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Max Discount (for percentage only)
                if (discountType == DiscountType.PERCENTAGE) {
                    CustomTextField(
                        value = maxDiscount,
                        onValueChange = { maxDiscount = it },
                        label = R.string.max_discount,
                        placeholder = R.string.no_cap,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                CustomHorizontalDivider(modifier = Modifier.padding(vertical = windowSizeConstant.normalVerticalPadding))

                // Dates
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
                ) {
                    Text(
                        text = stringResource(R.string.start),
                        style = windowSizeConstant.bodyTextStyle,
                        modifier = Modifier.weight(1f)
                    )

                    DatePickerButton(
                        selectedDate = startDateCalendar,
                        onDateSelected = { newDate ->
                            startDateCalendar.timeInMillis = newDate.timeInMillis
                            if (endDateCalendar.timeInMillis <= startDateCalendar.timeInMillis) {
                                endDateCalendar.timeInMillis = startDateCalendar.timeInMillis
                                endDateCalendar.add(Calendar.DAY_OF_YEAR, 1)
                            }
                        },
                        label = formatDate(startDateCalendar.timeInMillis)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
                ) {
                    Text(
                        text = stringResource(R.string.end),
                        style = windowSizeConstant.bodyTextStyle,
                        modifier = Modifier.weight(1f)
                    )
                    DatePickerButton(
                        selectedDate = endDateCalendar,
                        onDateSelected = { newDate ->
                            endDateCalendar.timeInMillis = newDate.timeInMillis
                        },
                        label = formatDate(endDateCalendar.timeInMillis)
                    )
                }

                // Validation
                if (endDateCalendar.timeInMillis <= startDateCalendar.timeInMillis) {
                    endDateError = "End date must be after start date"
                    Text(
                        text = endDateError,
                        style = windowSizeConstant.bodyTextStyle,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    endDateError = ""
                }
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
                label = R.string.add_new_promotion,
                onClick = {
                    // Validate
                    titleError = if (title.isBlank()) "Title required" else ""
                    descriptionError = if (description.isBlank()) "Description required" else ""

                    if (discountType != DiscountType.FREE_SHIPPING) {
                        discountError = when {
                            discountValue.isBlank() -> "Discount value required"
                            discountValue.toDoubleOrNull() == null -> "Invalid number"
                            else -> ""
                        }
                    }

                    if (titleError.isEmpty() && descriptionError.isEmpty() &&
                        discountError.isEmpty() && endDateError.isEmpty()
                    ) {

                        val promotion = PromotionsData(
                            title = title,
                            description = description,
                            discountType = discountType,
                            discountValue = if (discountType == DiscountType.FREE_SHIPPING) 0.0
                            else discountValue.toDoubleOrNull() ?: 0.0,
                            minPurchaseAmount = minPurchase.toDoubleOrNull() ?: 0.0,
                            maxDiscountAmount = maxDiscount.toDoubleOrNull() ?: 0.0,
                            startAt = startDateCalendar.timeInMillis,
                            endAt = endDateCalendar.timeInMillis,
                            expired = endDateCalendar.timeInMillis < System.currentTimeMillis(),
                            isActive = true
                        )
                        onConfirm(promotion)
                    }
                }
            )
        }
    )
}

/**
 * EditPromotionDialog - Form for modifying existing promotions
 *
 * Updates promotional details including status, title, description, and end date.
 *
 * @param promotion The promotion to edit
 * @param onDismiss Callback to close dialog
 * @param onConfirm Callback with updated promotion data
 */
@Composable
fun EditPromotionDialog(
    promotion: PromotionsData,
    onDismiss: () -> Unit,
    onConfirm: (PromotionsData) -> Unit
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    var title by remember { mutableStateOf(promotion.title) }
    var description by remember { mutableStateOf(promotion.description) }
    var discountType by remember { mutableStateOf(promotion.discountType) }
    var discountValue by remember { mutableStateOf(promotion.discountValue.toString()) }
    var minPurchase by remember { mutableStateOf(promotion.minPurchaseAmount.toString()) }
    var maxDiscount by remember { mutableStateOf(promotion.maxDiscountAmount.toString()) }
    var isActive by remember { mutableStateOf(promotion.isActive) }

    val startDateCalendar = remember(promotion.startAt) {
        Calendar.getInstance().apply { timeInMillis = promotion.startAt }
    }
    val endDateCalendar = remember(promotion.endAt) {
        Calendar.getInstance().apply { timeInMillis = promotion.endAt }
    }

    var titleError by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf("") }
    var discountError by remember { mutableStateOf("") }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Campaign,
                contentDescription = "Edit promotion",
                iconSize = windowSizeConstant.largeIconSize
            )
        },
        title = {
            Text(
                stringResource(R.string.edit_promotion),
                style = windowSizeConstant.titleTextStyle
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(windowSizeConstant.baseNormalVerticalPadding)
            ) {
                // Active Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Active",
                        style = windowSizeConstant.bodyTextStyle,
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }

                // Title
                CustomTextField(
                    label = R.string.title,
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = if (it.isBlank()) "Title required" else ""
                    },
                    isError = titleError.isNotEmpty(),
                    errorMessage = titleError
                )

                // Description
                CustomTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        descriptionError = if (it.isBlank()) "Description required" else ""
                    },
                    label = R.string.description,
                    isError = descriptionError.isNotEmpty(),
                    supportingText = { if (descriptionError.isNotEmpty()) Text(descriptionError) },
                    minLines = 2,
                    maxLines = 4
                )

                CustomHorizontalDivider()

                // Discount Type (read-only in edit)
                Text(
                    text = "Discount Type: ${discountType.name}",
                    style = windowSizeConstant.bodyTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Discount Value
                if (discountType != DiscountType.FREE_SHIPPING) {
                    CustomTextField(
                        value = discountValue,
                        onValueChange = {
                            discountValue = it
                            discountError =
                                if (it.toDoubleOrNull() == null) "Invalid number" else ""
                        },
                        label = R.string.discount_value,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = discountError.isNotEmpty(),
                        errorMessage = discountError
                    )
                }

                // Dates (Start is read-only)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
                ) {
                Text(
                    text =  stringResource(R.string.start),
                    style = windowSizeConstant.bodyTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                    DatePickerButton(
                        selectedDate = startDateCalendar,
                        onDateSelected = { newDate ->
                            startDateCalendar.timeInMillis = newDate.timeInMillis
                        },
                        label = formatDate(startDateCalendar.timeInMillis)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
                ) {
                    Text(
                        text = stringResource(R.string.end),
                        style = windowSizeConstant.bodyTextStyle,
                        modifier = Modifier.weight(1f)
                    )

                    DatePickerButton(
                        selectedDate = endDateCalendar,
                        onDateSelected = { newDate ->
                            endDateCalendar.timeInMillis = newDate.timeInMillis
                        },
                        label = formatDate(endDateCalendar.timeInMillis)
                    )
                }
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
                label = R.string.save,
                onClick = {
                    if (titleError.isEmpty() && descriptionError.isEmpty() && discountError.isEmpty()) {
                        val updated = promotion.copy(
                            title = title,
                            description = description,
                            discountValue = discountValue.toDoubleOrNull()
                                ?: promotion.discountValue,
                            minPurchaseAmount = minPurchase.toDoubleOrNull()
                                ?: promotion.minPurchaseAmount,
                            maxDiscountAmount = maxDiscount.toDoubleOrNull()
                                ?: promotion.maxDiscountAmount,
                            endAt = endDateCalendar.timeInMillis,
                            expired = endDateCalendar.timeInMillis < System.currentTimeMillis(),
                            isActive = isActive
                        )
                        onConfirm(updated)
                    }
                }
            )
        }
    )
}

/**
 * DeletePromotionDialog - Confirmation for deleting a promotion
 *
 * @param promotion The promotion to be deleted
 * @param onDismiss Callback when cancelled
 * @param onConfirm Callback when confirmed
 */
@Composable
fun DeletePromotionDialog(
    promotion: PromotionsData,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Warning,
                contentDescription = "Warning",
                tint = colors.orange,
                iconSize = windowSizeConstant.largeIconSize
            )
        },
        title = {
            Text(
                stringResource(R.string.delete_promotion),
                style = windowSizeConstant.titleTextStyle
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)) {
                Text(
                    stringResource(R.string.delete_promotion_warning),
                    style = windowSizeConstant.bodyTextStyle
                )

                Text(
                    text = promotion.title,
                    style = windowSizeConstant.bodyTextStyle,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.promotion_caution),
                    color = MaterialTheme.colorScheme.error,
                    style = windowSizeConstant.labelTextStyle
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
                label = R.string.delete,
                onClick = onConfirm,
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}


// ============================================================================
// BULK ADD PRODUCTS DIALOG
// ============================================================================

/**
 * BulkAddProductsDialog - Filter-based product selection tool
 *
 * Allows adding products to a promotion based on their category or associated tags.
 *
 * @param categories List of available product categories
 * @param tags List of available product tags
 * @param onDismiss Callback to close dialog
 * @param onAddByTag Callback when adding by tag, receives tag name
 * @param onAddByCategory Callback when adding by category, receives category ID
 */
@Composable
fun BulkAddProductsDialog(
    categories: List<CategoryItem>,
    tags: List<ProductTag>,
    onDismiss: () -> Unit,
    onAddByTag: (String) -> Unit,
    onAddByCategory: (String) -> Unit
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.AddBox,
                contentDescription = "Bulk add",
                iconSize = windowSizeConstant.largeIconSize
            )
        },
        title = {
            Text(
                "Bulk Add Products",
                style = windowSizeConstant.titleTextStyle
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = customSpacing.custom400)
            ) {
                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "By Tag",
                                style = windowSizeConstant.bodyTextStyle
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "By Category",
                                style = windowSizeConstant.bodyTextStyle
                            )
                        }
                    )
                }

                CustomSpacer()

                when (selectedTab) {
                    0 -> {
                        // Tag selection
                        CustomLazyColumn(modifier = Modifier.weight(1f)) {
                            items(tags, key = { it.id }) { tag ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTag = tag.name }
                                        .padding(windowSizeConstant.normalVerticalPadding),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CustomRadioButton(
                                        selected = selectedTag == tag.name,
                                        onClick = { selectedTag = tag.name }
                                    )

                                    Text(
                                        text = tag.displayName,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = windowSizeConstant.bodyTextStyle,
                                        modifier = Modifier.padding(start = windowSizeConstant.normalVerticalPadding)
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // Category selection
                        CustomLazyColumn(modifier = Modifier.weight(1f)) {
                            items(categories, key = { it.id }) { category ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCategory = category.id }
                                        .padding(windowSizeConstant.normalVerticalPadding),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CustomRadioButton(
                                        selected = selectedCategory == category.id,
                                        onClick = { selectedCategory = category.id }
                                    )

                                    Text(
                                        text = category.categoryName,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = windowSizeConstant.bodyTextStyle,
                                        modifier = Modifier.padding(start = windowSizeConstant.normalVerticalPadding)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.cancel,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            CustomTextButton(
                onClick = {
                    when (selectedTab) {
                        0 -> selectedTag?.let { onAddByTag(it) }
                        1 -> selectedCategory?.let { onAddByCategory(it) }
                    }
                },
                label = R.string.add_products,
                enabled = (selectedTab == 0 && selectedTag != null) ||
                        (selectedTab == 1 && selectedCategory != null)
            )
        }
    )
}

/**
 * DatePickerButton - Button that triggers a date selection dialog
 *
 * @param selectedDate The currently selected date
 * @param onDateSelected Callback when a new date is chosen
 * @param label Optional button text (defaults to formatted date)
 * @param enabled Whether the button is interactive
 */
@Composable
fun DatePickerButton(
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    label: String = "",
    enabled: Boolean = true
) {
    var showDatePicker by remember { mutableStateOf(false) }

    // Display the button
    CustomButton(
        modifier = Modifier.width(customSpacing.custom200),
        onClick = { showDatePicker = true },
        strLabel = label.ifEmpty { formatDate(selectedDate.timeInMillis) },
        enabled = enabled
    )

    // Show date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            title = "Select Date",
            initialDate = selectedDate,
            onDateSelected = { newDate ->
                onDateSelected(newDate)
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * DatePickerDialog - Material Design 3 date selection dialog
 *
 * @param title Dialog header text
 * @param initialDate Initially selected date
 * @param onDateSelected Callback with the chosen date
 * @param onDismiss Callback when cancelled
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    title: String = "Select Date",
    initialDate: Calendar = Calendar.getInstance(),
    onDateSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.timeInMillis,
        yearRange = IntRange(2020, 2030)
    )

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.DateRange,
                contentDescription = "Date picker",
                iconSize = windowSizeConstant.largeIconSize
            )
        },
        title = {
            Text(
                text = title,
                style = windowSizeConstant.titleTextStyle,
            )
        },
        text = {
            DatePicker(state = datePickerState)
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
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = millis
                        }
                        onDateSelected(calendar)
                    }
                    onDismiss()
                },
                label = R.string.select_date
            )
        }
    )
}