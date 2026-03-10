package com.example.myapp.view.screens.product.promotions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.SearchOff
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
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.Offer
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.CartViewModel
import com.example.myapp.data.model.FavoriteViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.data.model.PromotionViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomFilterChip
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomLazyRow
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.HeroCard
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.ShoppingCartBadge
import com.example.myapp.view.screens.product.ProductCard

/**
 * FlashDealsScreen - Screen displaying limited-time flash deals.
 *
 * Shows a list of products with significant discounts (e.g., >30%).
 * Features:
 * - Filtering deals by discount percentage (50%+, 70%+).
 * - Hero banner with countdown timer for the active flash sale.
 * - Grid view of discounted products.
 * - Real-time network status monitoring.
 *
 * @param onBackNavigation Callback to navigate back.
 * @param viewModel [ProductCrudViewModel] for product data.
 * @param favoriteViewModel [FavoriteViewModel] for managing favorites.
 * @param cartViewModel [CartViewModel] for cart operations.
 * @param promotionsViewModel [PromotionViewModel] for fetching promotion details.
 * @param onProductClick Callback when a product is clicked.
 * @param onSignInClick Callback to initiate sign-in.
 * @param onCartClick Callback to navigate to cart.
 * @param networkManager Manager for network connectivity.
 */
@Composable
fun FlashDealsScreen(
    onBackNavigation: () -> Unit,
    viewModel: ProductCrudViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    promotionsViewModel: PromotionViewModel = hiltViewModel(),
    onProductClick: (ProductItem) -> Unit,
    onSignInClick: () -> Unit,
    onCartClick: () -> Unit,
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val promotionState by promotionsViewModel.promotionState.collectAsState()
    val badgeNumber = cartViewModel.cartItems
    val imageLoader = viewModel.getImageLoader()
    val uiState by viewModel.productState.collectAsState()
    val windowSizeConstant = LocalWindowSizeConstant.current
    val networkState = rememberNetworkState(networkManager)
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    var selectedDiscountFilter by remember { mutableStateOf("all") }

    val products = uiState.products

    // Get flash deal products from promotions
    val flashDeals = remember(products) {
        products.filter { product ->
            val discount = if (product.oldPrice > 0) {
                ((product.oldPrice - product.price) / product.oldPrice * 100).toInt()
            } else 0
            discount >= 30
        }
    }

    // Apply discount filters
    val filteredDeals = remember(flashDeals, selectedDiscountFilter) {
        when (selectedDiscountFilter) {
            "50+" -> flashDeals.filter {
                val discount = if (it.oldPrice > 0) {
                    ((it.oldPrice - it.price) / it.oldPrice * 100).toInt()
                } else 0
                discount >= 50
            }

            "70+" -> flashDeals.filter {
                val discount = if (it.oldPrice > 0) {
                    ((it.oldPrice - it.price) / it.oldPrice * 100).toInt()
                } else 0
                discount >= 70
            }

            else -> flashDeals
        }
    }

    LaunchedEffect(Unit) {
        promotionsViewModel.loadPromotions()
        promotionsViewModel.loadAllPromotionProducts()
    }

    val flashOffer = Offer(
        id = "1",
        title = "Flash Deals",
        description = "Up to 70% off limited time",
        gradient = listOf(colors.customColor6, colors.customColor7),
        leadingIcon = Icons.Filled.FlashOn,
        composableFunction = {
            val activePromotion = promotionState.promotions.firstOrNull {
                !it.expired && it.isActive
            }

            activePromotion?.let { promotion ->
                PromotionCountdownTimer(
                    textColor = colors.red,
                    color = colors.white,
                    endAt = promotion.endAt
                )
            }
        }
    )

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
                promotionsViewModel.loadPromotions()
                promotionsViewModel.loadAllPromotionProducts()
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }
        },
        onNavigateBack = onBackNavigation,
        snackBarHostState = snackBarHostState,
        title = R.string.flash_deals,
        showBottomBar = false,
        verticalArrangement = Arrangement.Top,
        content = {
            // Network Status Banner
            if (!networkState.hasInternet) {
                // Network Indicator in top bar

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
                                .padding(top = windowSizeConstant.baseSize),
                            onDismiss = {
                                showSnackBar = false
                                currentSnackBarData = null
                            }
                        )
                    }
                )
            }

            when {
                promotionState.isLoading -> {
                    PaddedSection(
                        content = {
                            CustomListCardShimmer()
                        }
                    )
                }

                promotionState.error != null -> {
                    CustomEmptyState(
                        btnLabel = R.string.retry,
                        title = R.string.promotions_error,
                        onBtnClick = { promotionsViewModel.loadPromotions() },
                        leadingIcon = Icons.Filled.Error,
                        enableScroll = false  // Disable internal scrolling
                    )
                }

                else -> {
                    CustomLazyColumn {
                        // Hero Card
                        item {
                            CustomSpacer()
                            PaddedSection(
                                alignment = Alignment.CenterHorizontally,
                                content = {
                                    HeroCard(
                                        offer = flashOffer
                                    )
                                })
                        }

                        // Filter Chips
                        item {
                            PaddedSection(
                                content = {
                                    HeadlineWidget(
                                        leadingText = R.string.filter_by_discount
                                    )

                                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                                    CustomLazyRow {
                                        items(
                                            listOf(
                                                "all" to "All Deals",
                                                "50+" to "50% & Above",
                                                "70+" to "70% & Above"
                                            )
                                        ) { (filter, label) ->
                                            CustomFilterChip(
                                                isSelected = selectedDiscountFilter == filter,
                                                onClick = { selectedDiscountFilter = filter },
                                                label = label,
                                                leadingIcon = if (selectedDiscountFilter == filter) {
                                                    {
                                                        CustomIcon(
                                                            icon = Icons.Filled.Check,
                                                            contentDescription = null,
                                                            iconSize = windowSizeConstant.basePadding
                                                        )
                                                    }
                                                } else null
                                            )
                                        }
                                    }
                                })
                        }

                        // Deals Count
                        item {
                            PaddedSection(
                                content = {
                                    Text(
                                        "${filteredDeals.size} deals available",
                                        style = windowSizeConstant.bodyTextStyle,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant

                                    )
                                })
                        }

                        // Products Grid
                        if (filteredDeals.isEmpty()) {
                            item {
                                CustomEmptyState(
                                    btnLabel = R.string.browse_products,
                                    title = R.string.no_deals_found,
                                    onBtnClick = onBackNavigation,
                                    leadingIcon = Icons.Filled.SearchOff,
                                    enableScroll = false
                                )
                            }
                        } else {
                            item {
                                CustomLazyRow {
                                    items(
                                        items = flashDeals,
                                        key = { it.id }
                                    ) { product ->
                                        val isFavorite by favoriteViewModel.getFavoriteStatus(
                                            product.id
                                        )
                                            .collectAsState(initial = product.isFavorite)

                                        val isInCart by cartViewModel
                                            .isInCart(product.id)
                                            .collectAsState(initial = false)

                                        ProductCard(
                                            product = product,
                                            isFavorite = isFavorite,
                                            isInCart = isInCart,
                                            imageLoader = imageLoader,
                                            onProductClick = { onProductClick(product) },
                                            onAddToCart = {
                                                if (isInCart) {
                                                    cartViewModel.removeFromCart(product)
                                                } else {
                                                    cartViewModel.addToCart(product)
                                                }
                                            },
                                            onFavoriteClick = {
                                                favoriteViewModel.toggleFavorite(product)
                                            },
                                            onSignInClick = onSignInClick
                                        )
                                    }
                                }
                            }

                            item {
                                CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall))
                            }
                        }
                    }
                }
            }
        },
        actions = {
            // Network Indicator in top bar
            NetworkIndicator(networkState = networkState)

            if (!promotionState.isLoading) {
                ShoppingCartBadge(badgeNumber, onCartClick)
            }
        }
    )
}






