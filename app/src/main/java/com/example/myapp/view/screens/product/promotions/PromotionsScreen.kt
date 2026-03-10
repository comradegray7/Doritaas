package com.example.myapp.view.screens.product.promotions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.MembershipStatus
import com.example.myapp.data.dataclass.Offer
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.AuthViewModel
import com.example.myapp.data.model.CartViewModel
import com.example.myapp.data.model.FavoriteViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.PrimeMembershipViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.data.model.PromotionViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.ClickableSearchBarShimmer
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomLazyRow
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.HeroSection
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.ProductShimmer
import com.example.myapp.view.components.TopBarActionsShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.ShoppingCartBadge
import com.example.myapp.view.screens.product.ProductSection
import com.example.myapp.view.utils.ButtonIcon

/**
 * PromotionsScreen - The central hub for all product promotions.
 *
 * Aggregates various types of offers:
 * - Special offers (Flash Deals, Prime Exclusive, Daily Essentials).
 * - Active promotional campaigns with specific products.
 * - Trending, Featured, and Flash Deal product lists.
 * - Search and Cart access.
 *
 * @param onBackNavigation Callback to navigate back.
 * @param viewModel [ProductCrudViewModel] for product data.
 * @param favoriteViewModel [FavoriteViewModel] for favorites.
 * @param cartViewModel [CartViewModel] for cart.
 * @param promotionsViewModel [PromotionViewModel] for promotion logic.
 * @param onSearchClick Callback to open search.
 * @param onCartClick Callback to open cart.
 * @param onAllProductsClick Callback to view all products.
 * @param onProductClick Callback when a product is clicked.
 * @param onSignInClick Callback to initiate sign-in.
 * @param onLightningDealsClick Callback to view lightning/flash deals.
 * @param onNavigateToPrime Callback to navigate to Prime signup.
 * @param onNavigateToPrimeDetails Callback to navigate to Prime details.
 * @param onDailyEssentialsClick Callback to view daily essentials.
 * @param primeViewModel [PrimeMembershipViewModel] for membership status.
 * @param authViewModel [AuthViewModel] for authentication status.
 * @param networkManager Manager for network connectivity.
 */
@Composable
fun PromotionsScreen(
    onBackNavigation: () -> Unit,
    viewModel: ProductCrudViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    promotionsViewModel: PromotionViewModel = hiltViewModel(),
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onAllProductsClick: () -> Unit = {},
    onProductClick: (ProductItem) -> Unit,
    onSignInClick: () -> Unit,
    onLightningDealsClick: () -> Unit = {},
    onNavigateToPrime: () -> Unit,
    onNavigateToPrimeDetails: () -> Unit,
    onDailyEssentialsClick: () -> Unit = {},
    primeViewModel: PrimeMembershipViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val snackBarHostState = remember { SnackbarHostState() }
    val promotionState by promotionsViewModel.promotionState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val primeState by primeViewModel.membershipState.collectAsState()
    val networkState = rememberNetworkState(networkManager)
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }
    val taggedProducts by viewModel.taggedProducts.collectAsState()

    val uiState by viewModel.productState.collectAsState()

    val badgeNumber = cartViewModel.cartItems

    // Load promotions and their products
    LaunchedEffect(Unit) {
        promotionsViewModel.loadPromotions()
        viewModel.loadProducts()
    }

    // Load Prime status once for the entire screen
    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn) {
            primeViewModel.loadPrimeStatus()
        }
    }

    // Load products for each active promotion
    LaunchedEffect(promotionState.promotions) {
        promotionState.promotions
            .filter { !it.expired }
            .forEach { promotion ->
                promotionsViewModel.loadProductsForPromotion(promotion.id)
            }
    }

    val imageLoader = viewModel.getImageLoader()

    val trendingProducts = uiState.trendingProducts

    val featuredProducts = uiState.featuredProducts

    val flashDealProducts = taggedProducts["flash_deal"] ?: emptyList()

    // Special offers
    val specialOffers = listOf(
        Offer(
            id = "1", title = "Flash Deals", buttonText = "Shop Now",
            gradient = listOf(colors.customColor6, colors.customColor7),
            leadingIcon = Icons.Filled.FlashOn,
            onClick = onLightningDealsClick
        ),
        Offer(
            id = "2", title = "Prime Exclusive", buttonText = "Join Prime",
            gradient = listOf(colors.customColor9, colors.customColor10),
            leadingIcon = Icons.Filled.Stars,
            onClick = {
                when {
                    !authState.isSignedIn -> onSignInClick()
                    primeState.membership?.status == MembershipStatus.ACTIVE ->
                        onNavigateToPrimeDetails()

                    else -> onNavigateToPrime()
                }
            }
        ),
        Offer(
            id = "3", title = "Daily Essentials", buttonText = "Explore",
            gradient = listOf(colors.customColor11, colors.customColor12),
            leadingIcon = Icons.Filled.ShoppingBasket,
            onClick = onDailyEssentialsClick
        )
    )

    // Active Promotions Section - One section per promotion
    val activePromotions = promotionState.promotions.filter { !it.expired }

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
                promotionsViewModel.loadPromotions()
                viewModel.refreshProducts()
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
        title = R.string.product_promotions_title,
        showBottomBar = false,
        verticalArrangement = Arrangement.Top,
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
                                .padding(top = windowSizeConstant.baseSize),
                            onDismiss = {
                                showSnackBar = false
                                currentSnackBarData = null
                            }
                        )
                    }
                )
            }

            if (uiState.isLoading) {
                PaddedSection(
                    alignment = Alignment.CenterHorizontally,
                    content = { ClickableSearchBarShimmer() })
                CustomSpacer()
                ProductShimmer()
            } else if (uiState.error != null) {
                CustomEmptyState(
                    btnLabel = R.string.retry,
                    title = R.string.promotions_error,
                    onBtnClick = { viewModel.refreshProducts() },
                    scrollState = rememberScrollState(),
                    leadingIcon = Icons.Filled.Error,
                )
            } else {

                CustomLazyColumn {
                    // Special Offers Section
                    item {
                        CustomSpacer()
                        PaddedSection(content = {
                            HeadlineWidget(
                                leadingText = R.string.special_offers_title,
                                trailing = {
                                    ButtonIconComposable(
                                        buttonIcon = ButtonIcon.Vector(Icons.Filled.Stars),
                                        onClick = {},
                                        contentDescription = "special offer"
                                    )
                                }
                            )
                        })
                    }

                    item {
                        CustomLazyRow {
                            items(items = specialOffers, key = { it.id }) { offer ->
                                HeroSection(offer = offer)
                            }
                        }
                    }

                    items(activePromotions) { promotion ->
                        val promotionProducts =
                            promotionState.promotionProductsMap[promotion.id] ?: emptyList()

                        ProductSection(
                            middleText = promotion.title,
                            subMiddleText = promotion.description,
                            products = promotionProducts,
                            leadingComposable = {
                                CustomIcon(
                                    icon = Icons.Filled.Campaign,
                                    tint = MaterialTheme.colorScheme.primary,
                                    contentDescription = "promotion",
                                    iconSize = windowSizeConstant.largeIconSize,
                                )
                            },
                            favoriteViewModel = favoriteViewModel,
                            cartViewModel = cartViewModel,
                            imageLoader = imageLoader,
                            onProductClick = onProductClick,
                            onSignInClick = onSignInClick,
                            onSeeAllClick = onAllProductsClick,
                            trailingComposable = {
                                PromotionCountdownTimer(
                                    endAt = promotion.endAt,
                                    textColor = colors.customColor6
                                )
                            }
                        )
                    }

                    if (flashDealProducts.isNotEmpty()) {
                        item {
                            ProductSection(
                                titleRes = R.string.flash_deals,
                                products = flashDealProducts,
                                favoriteViewModel = favoriteViewModel,
                                cartViewModel = cartViewModel,
                                imageLoader = imageLoader,
                                onProductClick = onProductClick,
                                onSignInClick = onSignInClick,
                                onSeeAllClick = onAllProductsClick
                            )
                        }
                    }

                    // Trending products
                    if (trendingProducts.isNotEmpty()) {
                        item {
                            ProductSection(
                                titleRes = R.string.trending_products,
                                products = trendingProducts,
                                favoriteViewModel = favoriteViewModel,
                                cartViewModel = cartViewModel,
                                imageLoader = imageLoader,
                                onProductClick = onProductClick,
                                onSignInClick = onSignInClick,
                                onSeeAllClick = onAllProductsClick
                            )
                        }
                    }

                    // Featured products
                    if (featuredProducts.isNotEmpty()) {
                        item {
                            ProductSection(
                                titleRes = R.string.featured_products,
                                products = featuredProducts,
                                favoriteViewModel = favoriteViewModel,
                                cartViewModel = cartViewModel,
                                imageLoader = imageLoader,
                                onProductClick = onProductClick,
                                onSignInClick = onSignInClick,
                                onSeeAllClick = onAllProductsClick
                            )
                        }
                    }
                   item{
                       CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall))
                   }
                }
            }
        },
        actions = {
            if (uiState.isLoading) {
                TopBarActionsShimmer()
            } else {
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Search),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { onSearchClick() },
                    contentDescription = "Search"
                )

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseVerticalPadding))

                ShoppingCartBadge(badgeNumber, onCartClick)
            }
        }
    )
}

