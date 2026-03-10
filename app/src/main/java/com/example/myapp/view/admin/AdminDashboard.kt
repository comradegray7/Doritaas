package com.example.myapp.view.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.AnalyticsViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.AnalyticsShimmerGrid
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomDropDownMenuItem
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.TopBarActionsShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.ButtonIcon
import kotlinx.coroutines.delay

/**
 * AdminDashboardScreen - Central hub for administrative operations
 *
 * This screen serves as the main entry point for administrators, providing an overview
 * of key metrics and quick navigation to all management screens.
 *
 * ## Features
 * - **Analytics Overview**: Real-time counts for all major entities
 * - **Quick Navigation**: One-tap access to management screens
 * - **Refresh Capability**: Pull-to-refresh and manual refresh button
 * - **Responsive Grid**: Adaptive layout for different screen sizes
 * - **Error Handling**: Graceful error states with retry options
 *
 * ## Analytics Cards
 * The dashboard displays analytics for:
 * - **Products**: Total product count with navigation to product management
 * - **Sizes**: Available size options
 * - **Colors**: Available color options
 * - **Promotions**: Active and expired promotions
 * - **Brands**: Registered brands
 * - **Shipments**: Shipping methods and tracking
 * - **Categories**: Product categories
 * - **Orders**: Customer orders
 *
 * ## Navigation Menu
 * Top-right dropdown menu provides access to:
 * - Manage Categories
 * - Manage Orders
 * - Manage Products
 * - Manage Sizes
 * - Manage Brands
 * - Manage Shipments
 * - Manage Promotions
 * - Manage Colors
 *
 * ## User Workflow
 * 1. View analytics overview on dashboard
 * 2. Click any analytics card to navigate to that management screen
 * 3. Use dropdown menu for additional navigation options
 * 4. Pull down or click refresh to update analytics
 *
 * ## Loading States
 * - Shows shimmer placeholders while loading analytics
 * - Displays error state with retry button on failure
 * - Prevents interaction during data refresh
 *
 * @param viewModel ViewModel for product CRUD operations
 * @param onProductClick Callback to navigate to product management
 * @param onCategoryClick Callback to navigate to category management
 * @param onSizeClick Callback to navigate to size management
 * @param onBrandClick Callback to navigate to brand management
 * @param onNavigateBack Callback to navigate back from dashboard
 * @param onShipmentClick Callback to navigate to shipment management
 * @param onPromotionClick Callback to navigate to promotion management
 * @param onColorClick Callback to navigate to color management
 * @param onOrderClick Callback to navigate to order management
 * @param analyticsViewModel ViewModel providing analytics data
 *
 * @see AnalyticsViewModel for analytics data management
 * @see MiniAnalyticsCard for individual metric cards
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: ProductCrudViewModel = hiltViewModel(),
    onProductClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onSizeClick: () -> Unit,
    onBrandClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onShipmentClick: () -> Unit,
    onPromotionClick: () -> Unit,
    onColorClick: () -> Unit,
    onOrderClick: () -> Unit,
    onPrimeClick: () -> Unit,
    onTagClick: () -> Unit,
    onCarouselClick: () -> Unit,
    analyticsViewModel: AnalyticsViewModel = hiltViewModel(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeAppConstants = LocalWindowSizeConstant.current

    val analyticsState by analyticsViewModel.analyticsState.collectAsState()

    LaunchedEffect(Unit) {
        analyticsViewModel.loadAnalytics()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductItem?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    val networkState = rememberNetworkState(networkManager)

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
            analyticsViewModel.loadAnalytics()
        },
        title = R.string.admin_dashboard,
        snackBarHostState = snackBarHostState,
        showBottomBar = false,
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
                        CustomSpacer()
                        FloatingCustomSnackBar(
                            snackBarData = snackBarData,
                            visible = showSnackBar,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(top = windowSizeAppConstants.baseSize),
                            onDismiss = {
                                showSnackBar = false
                                currentSnackBarData = null
                            }
                        )
                    }
                )
            }

            if (analyticsState.isLoading) {
                PaddedSection(
                    content = {
                        CustomSpacer()
                        AnalyticsShimmerGrid()
                    }
                )

            } else if (analyticsState.error != null) {
                CustomEmptyState(
                    btnLabel = R.string.retry,
                    subTitle = R.string.analytics_error,
                    onBtnClick = { analyticsViewModel.loadAnalytics() },
                    leadingIcon = Icons.Filled.Error,
                )
            } else {
                CustomLazyColumn {
                    item {
                        CustomSpacer()
                    }
                    // Grid Row 1
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = windowSizeAppConstants.baseSize),
                            horizontalArrangement = Arrangement.spacedBy(windowSizeAppConstants.basePadding)
                        ) {
                            // Products Analytics
                            Box(modifier = Modifier.weight(1f)) {
                                MiniAnalyticsCard(
                                    title = "Products",
                                    icon = Icons.Filled.Shop,
                                    count = analyticsState.productsCount,
                                    onClick = onProductClick,
                                )
                            }

                            // Sizes Analytics
                            Box(modifier = Modifier.weight(1f)) {
                                MiniAnalyticsCard(
                                    title = "Sizes",
                                    icon = Icons.Filled.FormatSize,
                                    count = analyticsState.sizesCount,
                                    onClick = onSizeClick,
                                )
                            }
                        }
                    }

                    // Grid Row 2
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = windowSizeAppConstants.baseSize),
                            horizontalArrangement = Arrangement.spacedBy(windowSizeAppConstants.basePadding)
                        ) {
                            // Colors Analytics
                            Box(modifier = Modifier.weight(1f)) {
                                MiniAnalyticsCard(
                                    title = "Colors",
                                    icon = Icons.Filled.ColorLens,
                                    count = analyticsState.colorsCount,
                                    onClick = onColorClick,
                                )
                            }

                            // Promotions Analytics
                            Box(modifier = Modifier.weight(1f)) {
                                MiniAnalyticsCard(
                                    title = "Promotions",
                                    icon = Icons.Filled.Campaign,
                                    count = analyticsState.promotionsCount,
                                    onClick = onPromotionClick,
                                )
                            }
                        }
                    }

                    // Grid Row 3
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = windowSizeAppConstants.baseSize),
                            horizontalArrangement = Arrangement.spacedBy(windowSizeAppConstants.basePadding)
                        ) {
                            // Brands Analytics
                            Box(modifier = Modifier.weight(1f)) {
                                MiniAnalyticsCard(
                                    title = "Brands",
                                    icon = Icons.AutoMirrored.Filled.BrandingWatermark,
                                    count = analyticsState.brandsCount,
                                    onClick = onBrandClick,
                                )
                            }

                            // Promotions Analytics
                            Box(modifier = Modifier.weight(1f)) {
                                MiniAnalyticsCard(
                                    title = "Shipments",
                                    icon = Icons.Filled.DeliveryDining,
                                    count = analyticsState.shipmentsCount,
                                    onClick = onShipmentClick,
                                )
                            }
                        }
                    }
                    // Grid Row 4
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = windowSizeAppConstants.baseSize),
                            horizontalArrangement = Arrangement.spacedBy(windowSizeAppConstants.basePadding)
                        ) {
                            // Brands Analytics
                            Box(modifier = Modifier.weight(1f)) {
                                MiniAnalyticsCard(
                                    title = "Categories",
                                    icon = Icons.Filled.Category,
                                    count = analyticsState.categoriesCount,
                                    onClick = onCategoryClick,
                                )
                            }

                            // Orders Analytics
                            Box(modifier = Modifier.weight(1f)) {
                                MiniAnalyticsCard(
                                    title = "Orders",
                                    icon = Icons.Filled.ShoppingBasket,
                                    count = analyticsState.ordersCount,
                                    onClick = onOrderClick,
                                )
                            }
                        }
                    }

                    // Grid Row 5
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = windowSizeAppConstants.baseSize),
                            horizontalArrangement = Arrangement.spacedBy(windowSizeAppConstants.basePadding)
                        ) {
                            // Tags Analytics
                            Box(modifier = Modifier.weight(1f)) {
                                MiniAnalyticsCard(
                                    title = "Product Tags",
                                    icon = Icons.Filled.Tag,
                                    count = analyticsState.tagCount,
                                    onClick = onTagClick,
                                )
                            }

                            //Carousel Analytics
                            Box(modifier = Modifier.weight(1f)) {
                                MiniAnalyticsCard(
                                    title = "Product Carousel",
                                    icon = Icons.Filled.ViewCarousel,
                                    count = analyticsState.carouselCount,
                                    onClick = onCarouselClick,
                                )
                            }
                        }
                    }

                    item {
                        CustomSpacer(modifier = Modifier.height(windowSizeAppConstants.customSpacerSmall))
                    }
                }
            }
            // Delete Confirmation Dialog
            if (showDeleteDialog && productToDelete != null) {
                CustomAlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        productToDelete = null
                    },
                    icon = {
                        CustomIcon(
                            icon = Icons.Filled.Warning,
                            contentDescription = "Warning",
                            tint = colors.orange,
                            iconSize = windowSizeAppConstants.largeIconSize
                        )
                    },
                    title = {
                        Text(
                            stringResource(R.string.delete_product),
                            style = windowSizeAppConstants.titleTextStyle
                        )
                    },
                    text = {
                        Text(
                            "Are you sure you want to delete '${productToDelete?.productName}'?",
                            style = windowSizeAppConstants.bodyTextStyle
                        )
                    },
                    confirmButton = {
                        CustomTextButton(
                            onClick = {
                                productToDelete?.let { product ->
                                    viewModel.deleteProduct(product.id, product.productName)
                                }
                                showDeleteDialog = false
                                productToDelete = null
                            },
                            label = R.string.delete,
                            color = MaterialTheme.colorScheme.error
                        )
                    }, dismissButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            productToDelete = null
                        }) {
                            Text(
                                stringResource(R.string.cancel),
                                style = windowSizeAppConstants.bodyTextStyle
                            )
                        }
                    })
            }

        },
        actions = {
            if (analyticsState.isLoading) {
                TopBarActionsShimmer()
            } else {
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                ButtonIconComposable(
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.MoreVert),
                    onClick = { expanded = true },
                    contentDescription = "Menu"
                )

                Box {
                    DropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false }) {
                        CustomDropDownMenuItem(text = {
                            Row {
                                CustomIcon(
                                    icon = Icons.Default.Category,
                                    contentDescription = "Category"
                                )

                                CustomSpacer(modifier = Modifier.width(windowSizeAppConstants.normalVerticalPadding))

                                Text(
                                    stringResource(R.string.manage_categories),
                                    style = windowSizeAppConstants.bodyTextStyle
                                )
                            }
                        }, onClick = {
                            expanded = false
                            onCategoryClick()
                        })

                        CustomDropDownMenuItem(text = {
                            Row {
                                CustomIcon(
                                    icon = Icons.Default.Category,
                                    contentDescription = "Category"
                                )

                                CustomSpacer(modifier = Modifier.width(windowSizeAppConstants.normalVerticalPadding))

                                Text(
                                    stringResource(R.string.manage_orders),
                                    style = windowSizeAppConstants.bodyTextStyle
                                )
                            }
                        }, onClick = {
                            expanded = false
                            onOrderClick()
                        })

                        CustomDropDownMenuItem(text = {
                            Row {
                                CustomIcon(icon = Icons.Filled.Shop, contentDescription = "Shop")

                                CustomSpacer(modifier = Modifier.width(windowSizeAppConstants.normalVerticalPadding))

                                Text(
                                    stringResource(R.string.manage_products),
                                    style = windowSizeAppConstants.bodyTextStyle
                                )
                            }
                        }, onClick = {
                            expanded = false
                            onProductClick()
                        })

                        CustomDropDownMenuItem(text = {
                            Row {
                                CustomIcon(icon = Icons.Filled.Stars, contentDescription = null)
                                CustomSpacer(modifier = Modifier.width(windowSizeAppConstants.normalVerticalPadding))
                                Text(
                                    stringResource(R.string.manage_prime),
                                    style = windowSizeAppConstants.bodyTextStyle
                                )
                            }
                        }, onClick = {
                            expanded = false
                            onPrimeClick()
                        })

                        CustomDropDownMenuItem(text = {
                            Row {
                                CustomIcon(
                                    icon = Icons.Filled.FormatSize,
                                    contentDescription = null
                                )
                                CustomSpacer(modifier = Modifier.width(windowSizeAppConstants.normalVerticalPadding))
                                Text(
                                    stringResource(R.string.manage_sizes),
                                    style = windowSizeAppConstants.bodyTextStyle
                                )
                            }
                        }, onClick = {
                            expanded = false
                            onSizeClick()
                        })

                        CustomDropDownMenuItem(text = {
                            Row {
                                CustomIcon(
                                    icon = Icons.AutoMirrored.Filled.BrandingWatermark,
                                    contentDescription = "Brand watermark"
                                )

                                CustomSpacer(modifier = Modifier.width(windowSizeAppConstants.normalVerticalPadding))

                                Text(
                                    stringResource(R.string.manage_brand),
                                    style = windowSizeAppConstants.bodyTextStyle
                                )
                            }
                        }, onClick = {
                            expanded = false
                            onBrandClick()
                        })

                        CustomDropDownMenuItem(text = {
                            Row {
                                CustomIcon(
                                    icon = Icons.Filled.DeliveryDining,
                                    contentDescription = "Delivery"
                                )
                                CustomSpacer(modifier = Modifier.width(windowSizeAppConstants.normalVerticalPadding))
                                Text(
                                    stringResource(R.string.manage_shipments),
                                    style = windowSizeAppConstants.bodyTextStyle
                                )
                            }
                        }, onClick = {
                            expanded = false
                            onShipmentClick()
                        })

                        CustomDropDownMenuItem(text = {
                            Row {
                                CustomIcon(
                                    icon = Icons.Filled.Campaign,
                                    contentDescription = "Campaign"
                                )
                                CustomSpacer(modifier = Modifier.width(windowSizeAppConstants.normalVerticalPadding))
                                Text(
                                    stringResource(R.string.manage_promotions),
                                    style = windowSizeAppConstants.bodyTextStyle
                                )
                            }
                        }, onClick = {
                            expanded = false
                            onPromotionClick()
                        })

                        DropdownMenuItem(text = {
                            Row {
                                CustomIcon(icon = Icons.Filled.ColorLens, contentDescription = null)
                                CustomSpacer(modifier = Modifier.width(windowSizeAppConstants.normalVerticalPadding))
                                Text(
                                    stringResource(R.string.manage_colors),
                                    style = windowSizeAppConstants.bodyTextStyle
                                )
                            }
                        }, onClick = {
                            expanded = false
                            onColorClick()
                        }
                        )
                        CustomDropDownMenuItem(text = {
                            Row {
                                CustomIcon(icon = Icons.Filled.Tag, contentDescription = "Tag")

                                CustomSpacer(modifier = Modifier.width(windowSizeAppConstants.normalVerticalPadding))

                                Text(
                                    stringResource(R.string.manage_tags),
                                    style = windowSizeAppConstants.bodyTextStyle
                                )
                            }
                        }, onClick = {
                            expanded = false
                            onTagClick()
                        }
                        )

                        CustomDropDownMenuItem(text = {
                            Row {
                                CustomIcon(
                                    icon = Icons.Filled.ViewCarousel,
                                    contentDescription = "View carousel"
                                )

                                CustomSpacer(modifier = Modifier.width(windowSizeAppConstants.normalVerticalPadding))

                                Text(
                                    stringResource(R.string.manage_carousel),
                                    style = windowSizeAppConstants.bodyTextStyle
                                )
                            }
                        }, onClick = {
                            expanded = false
                            onCarouselClick()
                        }
                        )
                    }
                }
            }
        }
    )
}

/**
 * MiniAnalyticsCard - Compact analytics display card
 *
 * Displays a single metric with an icon, count, and title in a clickable card format.
 * Used in the admin dashboard to show key statistics and provide navigation.
 *
 * ## Visual Structure
 * ```
 * ┌─────────────┐
 * │    Icon     │
 * │    Count    │
 * │    Title    │
 * └─────────────┘
 * ```
 *
 * ## Features
 * - Icon representation of the metric type
 * - Large, bold count display
 * - Descriptive title
 * - Clickable to navigate to detailed view
 * - Consistent styling with Material Design 3
 *
 * @param title The label describing the metric (e.g., "Products", "Orders")
 * @param icon The icon representing the metric type
 * @param count The numeric value to display
 * @param onClick Callback invoked when card is clicked
 * @param color Optional icon tint color (defaults to onSurface)
 */
@Composable
fun MiniAnalyticsCard(
    title: String,
    icon: ImageVector,
    count: Int,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val windowSizeAppConstants = LocalWindowSizeConstant.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = windowSizeAppConstants.cardElevationPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeAppConstants.baseVerticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CustomIcon(
                icon = icon,
                contentDescription = null,
                tint = color,
                iconSize = windowSizeAppConstants.largeIconSize
            )

            CustomSpacer(modifier = Modifier.height(windowSizeAppConstants.normalVerticalPadding))

            Text(
                text = "$count",
                style = windowSizeAppConstants.titleTextStyle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CustomSpacer(modifier = Modifier.height(windowSizeAppConstants.smallVerticalPadding))

            Text(
                text = title,
                style = windowSizeAppConstants.bodyTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
