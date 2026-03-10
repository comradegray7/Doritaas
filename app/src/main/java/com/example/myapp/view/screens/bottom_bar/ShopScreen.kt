package com.example.myapp.view.screens.bottom_bar

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.Stars
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.MembershipStatus
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.AuthViewModel
import com.example.myapp.data.model.CarouselViewModel
import com.example.myapp.data.model.CartViewModel
import com.example.myapp.data.model.FavoriteViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.PrimeMembershipViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
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
import com.example.myapp.view.screens.ClickableSearchBar
import com.example.myapp.view.screens.product.CarouselPlaceholder
import com.example.myapp.view.screens.product.ProductCarousel
import com.example.myapp.view.screens.product.ProductSection
import com.example.myapp.view.screens.product.product_rating_and_reviews.ProductRatingDialog
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CloudinaryHelper
import com.example.myapp.view.utils.primeUtils.isUserPrimeMember
import kotlinx.coroutines.delay

/**
 * ShopScreen - Main dashboard for product discovery.
 *
 * This screen serves as the primary storefront, featuring:
 * - Search functionality
 * - Promotional carousel
 * - Horizontal scrolling lists for "Trending", "Featured", and "Free Shipping" products
 * - Access to Cart and Delivery Location
 * - Product interaction (Click, Favorite, Add to Cart)
 *
 * ## Features
 * - **Dynamic Content**: Displays products fetched from [ProductCrudViewModel].
 * - **Interactive Elements**: Search bar, carousels, and product cards are fully interactive.
 * - **State Handling**: Manages loading, error, and success states for data fetching.
 * - **User Reviews**: Integrated product rating dialog.
 *
 * ## User Workflow
 * 1. User lands on Shop Screen.
 * 2. Can search for products or browse categories/lists.
 * 3. Can tap a product to view details.
 * 4. Can add items to cart or toggle favorites directly.
 *
 * @param onSearchClick Navigation callback for search screen.
 * @param onLocationClick Navigation callback for location management.
 * @param onProductClick Navigation callback for product details.
 * @param onCarouselClick Navigation callback for carousel items.
 * @param onCartClick Navigation callback for cart screen.
 * @param onAllProductsClick Navigation callback for "See All" lists.
 * @param viewModel [ProductCrudViewModel] for data.
 * @param favoriteViewModel [FavoriteViewModel] for wishlist management.
 * @param cartViewModel [CartViewModel] for cart management.
 * @param onSignInClick Callback for auth requirements.
 */
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ShopScreen(
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit,
    onProductClick: (ProductItem) -> Unit,
    onCarouselClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onAllProductsClick: () -> Unit,
    viewModel: ProductCrudViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    primeViewModel: PrimeMembershipViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onSignInClick: () -> Unit,
    onNavigateToPrime: () -> Unit,
    onNavigateToPrimeDetails: () -> Unit,
    carouselViewModel: CarouselViewModel = hiltViewModel(),
    cloudinaryHelper: CloudinaryHelper = CloudinaryHelper(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val uiState by viewModel.productState.collectAsState()
    val imageLoader = viewModel.getImageLoader()
    val authState by authViewModel.authState.collectAsState()
    val primeState by primeViewModel.membershipState.collectAsState()
    val taggedProducts by viewModel.taggedProducts.collectAsState()
    val networkState = rememberNetworkState(networkManager)

    val snackBarHostState = remember { SnackbarHostState() }
    val currentUserReview by viewModel.currentUserReview.collectAsState()
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedProductForRating by remember { mutableStateOf<ProductItem?>(null) }

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


    LaunchedEffect(uiState.tags) {
        viewModel.loadAllTaggedProducts()
    }

    LaunchedEffect(selectedProductForRating, currentUserReview) {
        if (selectedProductForRating != null) {
            Log.d("ShopScreen", "Current user review: ${currentUserReview?.rating}")
            delay(100)
            showRatingDialog = true
        }
    }

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
                carouselViewModel.loadCarousels()
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }
        },
        snackBarHostState = snackBarHostState,
        showBottomBar = false,
        showBackArrow = false,
        verticalArrangement = Arrangement.Top,
        showTitle = false,
        topBarComposable = { Text(
                modifier = Modifier,
                text = stringResource(R.string.logo),
                maxLines = 1,
                style = TextStyle(
                    fontSize = 22.sp,
                    shadow = Shadow()
                ),
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            ) },
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

            if (showRatingDialog && selectedProductForRating != null) {
                ProductRatingDialog(
                    productName = selectedProductForRating!!.productName,
                    currentUserRating = currentUserReview?.rating ?: 0f,
                    currentReviewText = currentUserReview?.review ?: "",
                    onDismiss = {
                        showRatingDialog = false
                        selectedProductForRating = null
                        viewModel.clearCurrentUserReview()
                    },
                    onSubmitRating = { rating, review ->
                        viewModel.submitProductRating(
                            selectedProductForRating!!.id,
                            rating,
                            review
                        )
                        showRatingDialog = false
                        selectedProductForRating = null
                    }
                )
            }

            when {
                uiState.isLoading -> {
                    PaddedSection(
                        alignment = Alignment.CenterHorizontally,
                        content = {
                        ClickableSearchBarShimmer()
                    }
                    )
                    CustomSpacer()
                    ProductShimmer()
                }

                uiState.error != null -> {
                    CustomEmptyState(
                        btnLabel = R.string.retry,
                        titleStr = uiState.error ?: "Unknown error",
                        onBtnClick = { viewModel.refreshProducts() },
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
                            PaddedSection(
                                alignment = Alignment.CenterHorizontally,
                                content = {
                                ClickableSearchBar(
                                    onClick = { onSearchClick() }
                                )
                            }
                            )
                        }

                        if (uiState.carousel.isNotEmpty()) {
                            item {
                                ProductCarousel(
                                    onCarouselClick = onCarouselClick,
                                    cloudinaryHelper = cloudinaryHelper,
                                    carouselItems = uiState.carousel
                                )
                            }
                        } else {
                            // Show carousel placeholder
                            item {
                                CarouselPlaceholder()
                            }
                        }

                        if (flashDealProducts.isNotEmpty()) {
                            item {
                                ProductSection(
                                    showLeadingComposable = false,
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
                                    showLeadingComposable = false,
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
                    buttonIcon = ButtonIcon.Vector(
                        if (isPrimeMember) Icons.Filled.Stars else Icons.Outlined.Stars
                    ),
                    onClick = {
                        when {
                            !authState.isSignedIn -> onSignInClick()
                            primeState.membership?.status == MembershipStatus.ACTIVE ->
                                onNavigateToPrimeDetails() // Goes to management
                            else ->
                                onNavigateToPrime() // Goes to join
                        }
                    },
                    tint = if (isPrimeMember) colors.customColor16 else MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = if (isPrimeMember) "prime active" else "prime membership"
                )

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.LocationOn),
                    onClick = { onLocationClick() },
                    contentDescription = "delivery location"
                )

                ShoppingCartBadge(
                    badgeNumber = cartViewModel.cartItems,
                    onCartClick = onCartClick
                )
            }
        }
    )
}