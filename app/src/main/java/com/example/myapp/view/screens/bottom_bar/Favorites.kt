package com.example.myapp.view.screens.bottom_bar

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.CartViewModel
import com.example.myapp.data.model.FavoriteViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomItemCard
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.TopBarActionsShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomCartButton
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.screens.product.ProductSummaryCard
import com.example.myapp.view.screens.product.RelatedProductsSection
import com.example.myapp.view.utils.ButtonIcon
import kotlinx.coroutines.delay

/**
 * FavoritesScreen - Displays user's saved wishlist items.
 *
 * This screen facilitates:
 * - Viewing saved products.
 * - Moving items to Cart.
 * - Removing items from Wishlist (individually or all at once).
 * - Viewing related products based on wishlist content.
 *
 * ## Features
 * - **Calculated Savings**: Shows total savings from saved items.
 * - **Bulk Actions**: "Delete All" functionality with confirmation.
 * - **Empty State**: Encourages shopping when wishlist is empty.
 * - **Transitions**: Shared element transitions for product images.
 *
 * ## User Workflow
 * 1. User views wishlist.
 * 2. Can tap "Add to Cart" to move an item.
 * 3. Can tap "Delete" icon to remove item.
 * 4. Can tap "Delete All" in top bar to clear list.
 *
 * @param onSearchClick Navigation callback for search.
 * @param navigateToShop Navigation callback for empty state.
 * @param onProductClick Navigation callback for details.
 * @param sharedTransitionScope Scope for shared element animations.
 * @param animatedContentScope Scope for traversal animations.
 * @param favoriteViewModel [FavoriteViewModel] for data.
 * @param cartViewModel [CartViewModel] for cart status.
 * @param onSignInClick Auth callback.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun FavoritesScreen(
    onSearchClick: () -> Unit,
    navigateToShop: () -> Unit,
    onProductClick: (ProductItem) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    onSignInClick: () -> Unit,
    onRelatedProductClick: (ProductItem) -> Unit,
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    val favoriteState by favoriteViewModel.favoriteState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val networkState = rememberNetworkState(networkManager)

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    // Calculate savings
    val savings by remember(favoriteState.favoriteItems) {
        derivedStateOf {
            favoriteState.favoriteItems.sumOf { (it.oldPrice - it.price).coerceAtLeast(0.0) * it.quantity }
        }
    }

    // Handle snack bar data (consistent with SizeManagementScreen)
    LaunchedEffect(Unit) {
        favoriteViewModel.snackBarData.collect { snackBarData ->
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
                favoriteViewModel.loadFavorites()
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
        verticalArrangement = Arrangement.Top,
        snackBarHostState = snackBarHostState,
        showBottomBar = false,
        title = R.string.favorites_title,
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

            // Confirmation dialog for delete all
            if (showDeleteAllDialog) {
                CustomAlertDialog(
                    onDismissRequest = { showDeleteAllDialog = false },
                    title = {
                        Text(
                            stringResource(R.string.clear_wishlist),
                            style = windowSizeConstant.bodyTextStyle
                        )
                    },
                    text = {
                        Text(
                            "Are you sure you want to remove all ${favoriteState.favoriteItems.size} items from your wishlist? This action cannot be undone.",
                            style = windowSizeConstant.bodyTextStyle
                        )
                    },
                    dismissButton = {
                        CustomTextButton(
                            onClick = { showDeleteAllDialog = false },
                            label = R.string.cancel
                        )
                    },
                    confirmButton = {
                        CustomTextButton(
                            label = R.string.delete_all,
                            onClick = {
                                favoriteViewModel.deleteAllFromFavorites()
                                showDeleteAllDialog = false
                            },
                            color = MaterialTheme.colorScheme.error
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
            // Main content based on state (consistent pattern)
            when {
                favoriteState.isLoading -> {
                    // Loading State
                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall))
                    PaddedSection(
                        content = {
                            CustomListCardShimmer()
                        }
                    )
                }

                favoriteState.error != null -> {
                    // Error State
                    CustomEmptyState(
                        btnLabel = R.string.retry,
                        title = R.string.favorites_error,
                        onBtnClick = { favoriteViewModel.loadFavorites() },
                        btnIcon = Icons.Filled.Error,
                        scrollState = scrollState
                    )
                }

                favoriteState.favoriteItems.isEmpty() -> {
                    // Empty State
                    CustomEmptyState(
                        btnLabel = R.string.start_shopping,
                        subTitle = R.string.empty_wishlist_subtitle,
                        onBtnClick = { navigateToShop() },
                        scrollState = scrollState,
                        leadingIcon = Icons.Filled.Favorite,
                    )
                }

                else -> {
                    // Success State with items
                    CustomLazyColumn {
                        item {
                            CustomSpacer()

                            PaddedSection(
                                content = {
                                    ProductSummaryCard(
                                        itemCount = favoriteState.favoriteItems.size,
                                        savings = savings,
                                        trailingIcon = {
                                            CustomIcon(
                                                icon = Icons.Filled.Favorite,
                                                contentDescription = "Trailing icon"
                                            )
                                        }
                                    )
                                }
                            )
                        }

                        items(favoriteState.favoriteItems) { product ->
                            val isInCart by cartViewModel.getCartStatus(product.id)
                                .collectAsState(initial = false)

                            PaddedSection(content = {
                                FavoriteItemCard(
                                    product = product,
                                    onAddToCart = {
                                        if (isInCart) {
                                            cartViewModel.removeFromCart(product)
                                        } else {
                                            cartViewModel.addToCart(product)
                                        }
                                    },
                                    onProductClick = { onProductClick(product) },
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedContentScope = animatedContentScope,
                                    onRemoveFromWishlist = {
                                        favoriteViewModel.removeFromFavorites(product)
                                    },
                                    isInCart = isInCart,
                                    onSignInClick = onSignInClick
                                )
                            })

                        }

                        if (favoriteState.favoriteItems.isNotEmpty()) {
                            item {
                                val favoriteCategories = favoriteState.favoriteItems
                                    .map { it.category }
                                    .distinct()
                                    .firstOrNull() ?: ""

                                RelatedProductsSection(
                                    categoryName = favoriteCategories,
                                    currentProductId = favoriteState.favoriteItems.joinToString(",") { it.id },
                                    onProductClick = { product ->
                                        onRelatedProductClick(product)
                                    },
                                    maxItems = 8,
                                    onSignInClick = onSignInClick
                                )
                            }
                        }

                        item {
                            CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerMedium))
                        }
                    }
                }
            }
        },
        actions = {
            // Show loading indicator in actions
            if (favoriteState.isLoading) {
                TopBarActionsShimmer()
            } else {
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Search),
                    onClick = onSearchClick,
                    contentDescription = "Search"
                )

                if (favoriteState.favoriteItems.isNotEmpty()) {
                    ButtonIconComposable(
                        showBgColor = false,
                        buttonIcon = ButtonIcon.Vector(Icons.Filled.Delete),
                        onClick = { showDeleteAllDialog = true },
                        contentDescription = "Delete All"
                    )
                }
            }
        }
    )
}

/**
 * FavoriteItemCard - Individual card content for a favorite item.
 *
 * Wraps [CustomItemCard] to provide specific actions for the Favorites screen:
 * - Add/Remove from Cart
 * - Remove from Favorites
 *
 * @param modifier Card modifier.
 * @param product Product data.
 * @param onAddToCart Action when add to cart is clicked.
 * @param onRemoveFromWishlist Action to remove this specific item.
 * @param sharedTransitionScope Shared element scope.
 * @param animatedContentScope Animation scope.
 * @param isInCart Current cart status of this item.
 * @param onProductClick Action when product image/body is clicked.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FavoriteItemCard(
    modifier: Modifier = Modifier,
    product: ProductItem,
    onAddToCart: () -> Unit,
    onRemoveFromWishlist: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    isInCart: Boolean,
    onProductClick: () -> Unit = {},
    onSignInClick: () -> Unit
) {

    CustomItemCard(
        modifier = modifier,
        onProductClick = onProductClick,
        product = product,
        onSignInClick = onSignInClick,
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
        actions = { isAuthenticated, showSignInDialog ->
            // Add to Cart button
            CustomCartButton(
                isInCart = isInCart,
                onAddToCart = {
                    if (isAuthenticated) {
                        onAddToCart()
                    } else {
                        showSignInDialog() // Trigger the dialog
                    }
                },
                useRoundedButton = true
            )

            // Remove from favorites button
            ButtonIconComposable(
                onClick = {
                    if (isAuthenticated) {
                        onRemoveFromWishlist()
                    } else {
                        showSignInDialog() // Trigger the dialog
                    }
                },
                buttonIcon = ButtonIcon.Vector(Icons.Filled.Delete),
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    )
}



