package com.example.myapp.view.screens.bottom_bar

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
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
import com.example.myapp.data.dataclass.CategoryItem
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.AuthViewModel
import com.example.myapp.data.model.CartViewModel
import com.example.myapp.data.model.FavoriteViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.PrimeMembershipViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.ClickableSearchBarShimmer
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.ProductShimmer
import com.example.myapp.view.components.TopBarActionsShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.ShoppingCartBadge
import com.example.myapp.view.screens.product.ProductSection
import com.example.myapp.view.screens.product.categories.Categories
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.primeUtils.isUserPrimeMember

/**
 * AllProductsScreen - Comprehensive product browsing screen.
 *
 * This screen provides an alternative view to browse products, emphasizing categories
 * and curated lists (Trending, Featured). It is similar to [ShopScreen] but may serve
 * as a "Catalog" view.
 *
 * ## Features
 * - **Category Browser**: Horizontal list of product categories.
 * - **Curated Lists**: Displays "Trending" and "Featured" products.
 * - **Navigation**: Access to Search and Cart.
 *
 * ## User Workflow
 * 1. User selects "All Products" or similar entry point.
 * 2. Can filter by category (via [Categories] component).
 * 3. Scrolls through product lists.
 * 4. Taps product for details or add to cart.
 *
 * @param onSearchClick Navigation callback for search.
 * @param onCartClick Navigation callback for cart.
 * @param onCategoryClick Callback when a category is selected.
 * @param onProductClick Callback for product details.
 * @param onAllProductsClick Callback for "See All" navigation (recursive/refresh).
 * @param viewModel [ProductCrudViewModel] for data.
 * @param favoriteViewModel [FavoriteViewModel] for favorites.
 * @param cartViewModel [CartViewModel] for cart.
 * @param onSignInClick Auth callback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllProductsScreen(
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onCategoryClick: (CategoryItem) -> Unit = {},
    onProductClick: (ProductItem) -> Unit,
    onAllProductsClick: () -> Unit,
    viewModel: ProductCrudViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    onSignInClick: () -> Unit,
    primeViewModel: PrimeMembershipViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val uiState by viewModel.productState.collectAsState()
    val imageLoader = viewModel.getImageLoader()
    val authState by authViewModel.authState.collectAsState()
    val primeState by primeViewModel.membershipState.collectAsState()
    val taggedProducts by viewModel.taggedProducts.collectAsState()
    val networkState = rememberNetworkState(networkManager)
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    LaunchedEffect(authState.user) {
        if (authState.user == null) {
            primeViewModel.clearPrimeStatus()
            Log.d("ShopScreen", "User logged out - Prime status cleared")
        } else {
            primeViewModel.loadPrimeStatus()
            Log.d("ShopScreen", "User logged in - Loading Prime status")
        }
    }

    val isPrimeMember = remember(authState.user, primeState.membership) {
        if (authState.user == null) {
            false
        } else {
            isUserPrimeMember(primeState.membership?.status)
        }
    }

    val scrollState = rememberScrollState()

    // Use pre-loaded tagged products from ViewModel state
    val trendingProducts = uiState.trendingProducts

    val featuredProducts = uiState.featuredProducts

    val flashDealProducts = taggedProducts["flash_deal"] ?: emptyList()

    val primeEligibleProducts = if (isPrimeMember) {
        taggedProducts["prime_eligible"] ?: emptyList()
    } else {
        emptyList()
    }

    val freeShippingProducts = uiState.products.filter { it.shipmentCost == 0.0 }.take(10).ifEmpty {
        uiState.products.take(10)
    }

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
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
        showBackArrow = false,
        showBottomBar = false,
        title = R.string.all_products_title,
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
                                .padding(top = windowSizeClass.baseSize),
                            onDismiss = {
                                showSnackBar = false
                                currentSnackBarData = null
                            }
                        )
                    }
                )
            }

            when {
                uiState.isLoading -> {
                    // Your existing shimmer loading states
                    PaddedSection(
                        content = {
                            ClickableSearchBarShimmer()
                        }
                    )
                    CustomSpacer()
                    ProductShimmer()
                }

                uiState.error != null -> {
                    // Error state
                    CustomEmptyState(
                        btnLabel = R.string.retry,
                        title = R.string.error_loading_products,
                        onBtnClick = { viewModel.refreshProducts() },
                        scrollState = scrollState,
                        leadingIcon = Icons.Filled.Error,
                    )

                }

                uiState.products.isEmpty() -> {
                    CustomEmptyState(
                        title = R.string.no_products_found,
                        subTitle = R.string.check_back_soon,
                        showBtn = false,
                        leadingIcon = Icons.Filled.Inventory2,
                    )

                }

                else -> {
                    CustomLazyColumn {
                        item {
                            Categories(
                                onCategoryClick = { category ->
                                    onCategoryClick(category)
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

                        // Prime Eligible Products (only show for Prime members)
                        if (primeEligibleProducts.isNotEmpty()) {
                            item {
                                ProductSection(
                                    titleRes = R.string.prime_eligible,
                                    products = primeEligibleProducts,
                                    favoriteViewModel = favoriteViewModel,
                                    cartViewModel = cartViewModel,
                                    imageLoader = imageLoader,
                                    onProductClick = onProductClick,
                                    onSignInClick = onSignInClick,
                                    onSeeAllClick = onAllProductsClick
                                )
                            }
                        }

                        if (freeShippingProducts.isNotEmpty()) {
                            item {
                                ProductSection(
                                    titleRes = R.string.free_products,
                                    products = freeShippingProducts,
                                    favoriteViewModel = favoriteViewModel,
                                    cartViewModel = cartViewModel,
                                    imageLoader = imageLoader,
                                    onProductClick = onProductClick,
                                    onSignInClick = onSignInClick,
                                    onSeeAllClick = onAllProductsClick
                                )
                            }
                        }

                        item {
                            CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerMedium))
                        }
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
                    onClick = { onSearchClick() },
                    contentDescription = "Search"
                )

                CustomSpacer(modifier = Modifier.width(windowSizeClass.basePadding))

                val badgeNumber = cartViewModel.cartItems

                ShoppingCartBadge(badgeNumber, onCartClick)
            }
        }
    )
}

