package com.example.myapp.view.screens.product

import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.AuthViewModel
import com.example.myapp.data.model.CartViewModel
import com.example.myapp.data.model.FavoriteViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.PrimeMembershipViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomBottomSection
import com.example.myapp.view.components.CustomBottomSectionShimmer
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
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.components.custom.buttons.FavoriteSplashButton
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CustomShape
import com.example.myapp.view.utils.primeUtils.isUserPrimeMember
import com.example.myapp.view.utils.toStripeCents
import kotlinx.coroutines.delay

/**
 * Main cart screen composable that displays the user's shopping cart.
 * Shows cart items, total price, savings, and checkout functionality.
 *
 * @param onSearchClick Callback when search icon is clicked.
 * @param onBackNavigation Callback for back navigation.
 * @param onCheckOutClick Callback to proceed to checkout with cart details.
 * @param sharedTransitionScope Scope for shared element transitions.
 * @param animatedContentScope Scope for animated content.
 * @param cartViewModel ViewModel for managing cart items and state.
 * @param favoriteViewModel ViewModel for managing favorite items.
 * @param navigateToShop Callback to navigate to the shop screen (empty state).
 * @param authViewModel ViewModel for authentication status.
 * @param onProductClick Callback when a product in the cart is clicked.
 * @param onRelatedProductClick Callback when a related product is clicked.
 * @param primeViewModel ViewModel for Prime membership status.
 * @param onSignInClick Callback when sign-in is required.
 * @param networkManager Manager for network connectivity status.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun CartScreen(
    onSearchClick: () -> Unit,
    onBackNavigation: () -> Unit,
    onCheckOutClick: (
        amountInCents: Int, cartItems: List<ProductItem>,
        customerEmail: String?, customerName: String?,
        isPrimeMember: Boolean,
        primeDiscount: Double,
    ) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    cartViewModel: CartViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    navigateToShop: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    onProductClick: (ProductItem) -> Unit,
    onRelatedProductClick: (ProductItem) -> Unit,
    primeViewModel: PrimeMembershipViewModel = hiltViewModel(),
    onSignInClick: () -> Unit,
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val cartState by cartViewModel.cartState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val primeState by primeViewModel.membershipState.collectAsState()
    val membershipStatus = primeState.membership?.status // This is your MembershipStatus enum
    val isPrimeMember = isUserPrimeMember(membershipStatus) // Returns true if ACTIVE
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()
    val user by authViewModel.authState.collectAsState()
    val scrollState = rememberScrollState()
    val networkState = rememberNetworkState(networkManager)

    val customerEmail = user.user?.email
    val customerName = user.user?.displayName
    val savings by remember(cartState.cartItems) {
        derivedStateOf {
            cartState.cartItems.sumOf {
                (it.oldPrice - it.price) * it.quantity
            }
        }
    }

    val totalPrimeDiscount = remember(cartState.cartItems, isPrimeMember) {
        if (isPrimeMember) {
            cartState.cartItems
                .filter { it.tags.contains("prime_eligible") }
                .sumOf { (it.price * it.quantity) * 0.20 }
        } else {
            0.0
        }
    }

    // Calculate totals from cartState.cartItems
    val total = remember(cartState.cartItems, totalPrimeDiscount, isPrimeMember) {
        val baseTotal = cartState.cartItems.sumOf { it.price * it.quantity }
        val shipping = if (isPrimeMember) 0.0 else 5.99
        baseTotal - totalPrimeDiscount + shipping
    }

    LaunchedEffect(authState.user) {
        if (authState.user == null) {
            // User logged out - clear immediately
            primeViewModel.clearPrimeStatus()
            cartViewModel.clearCart()
            Log.d("CartScreen", "User logged out - Prime status cleared")
        } else {
            // User logged in - load status
            primeViewModel.loadPrimeStatus()
            cartViewModel.loadCartItems()
            Log.d("CartScreen", "User logged in - Loading Prime status")
        }
    }

    // Handle snack bar data (same as SizeManagementScreen)
    LaunchedEffect(Unit) {
        cartViewModel.snackBarData.collect { snackBarData ->
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

    // Confirmation dialog for delete all
    if (showDeleteAllDialog) {
        CustomAlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text(
                    stringResource(R.string.clear_cart),
                    style = windowSizeClass.bodyTextStyle
                )
            },
            text = {
                Text(
                    "Are you sure you want to remove all ${cartState.cartItems.size} items from your Cart? This action cannot be undone.",
                    style = windowSizeClass.bodyTextStyle
                )
            },
            dismissButton = {
                CustomTextButton(
                    onClick = { showDeleteAllDialog = false },
                    label = R.string.cancel,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },

            confirmButton = {
                CustomTextButton(
                    label = R.string.delete_all,
                    color = MaterialTheme.colorScheme.error,
                    onClick = {
                        cartViewModel.clearCart()
                        showDeleteAllDialog = false
                    }
                )
            }
        )
    }

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
                cartViewModel.loadCartItems()
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
        snackBarHostState = snackBarHostState,
        title = R.string.shopping_cart_title,
        bottomBarContent = {
            // Check if loading and show shimmer
            if (cartState.isLoading) {
                CustomBottomSectionShimmer()
            } else if (cartState.cartItems.isEmpty()) {
                CustomBottomSection(
                    onClick = {
                        val amountInCents = total.toStripeCents
                        onCheckOutClick(
                            amountInCents,
                            cartState.cartItems,
                            customerEmail,
                            customerName,
                            isPrimeMember,
                            totalPrimeDiscount
                        )
                    },
                    enabled = false,
                    actionLabel = R.string.check_out,
                )
            } else {
                CustomBottomSection(
                    total = total,
                    onClick = {
                        val amountInCents = total.toStripeCents
                        onCheckOutClick(
                            amountInCents,
                            cartState.cartItems,
                            customerEmail,
                            customerName,
                            isPrimeMember,
                            totalPrimeDiscount
                        )
                    },
                    actionLabel = R.string.check_out,
                )
            }
        },
        content = {
            // Snack bar
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
                                .padding(top = windowSizeClass.baseSize),
                            onDismiss = {
                                showSnackBar = false
                                currentSnackBarData = null
                            }
                        )
                    }
                )
            }

            // Main content based on state (consistent with SizeManagementScreen)
            when {
                cartState.isLoading -> {
                    // Loading State
                    CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))
                    PaddedSection(
                        content = {
                            CustomListCardShimmer()
                        }
                    )
                }

                authState.user == null -> {
                    CustomEmptyState(
                        titleStr = "Sign in to view your cart",
                        btnLabel = R.string.sign_in, onBtnClick = { onSignInClick() },
                        leadingIcon = Icons.Filled.ShoppingCart,
                        scrollState = scrollState
                    )
                }

                cartState.error != null -> {
                    // Error State
                    CustomEmptyState(
                        btnLabel = R.string.retry,
                        title = R.string.cart_error,
                        onBtnClick = { cartViewModel.loadCartItems() },
                        btnIcon = Icons.Filled.Error,
                        scrollState = scrollState
                    )
                }

                cartState.cartItems.isEmpty() -> {
                    // Empty State
                    CustomEmptyState(
                        btnLabel = R.string.start_shopping,
                        title = R.string.your_Cart_is_empty,
                        onBtnClick = { navigateToShop() },
                        scrollState = scrollState,
                        leadingIcon = Icons.Filled.ShoppingCart,
                    )
                }

                else -> {
                    // Success State with item
                    CustomLazyColumn {
                        item {
                            PaddedSection(
                                content = {
                                    ProductSummaryCard(
                                        itemCount = cartState.cartItems.sumOf { it.quantity },
                                        savings = savings,
                                        trailingIcon = {
                                            CustomIcon(
                                                icon = Icons.Filled.ShoppingBasket,
                                                contentDescription = "Cart icon"
                                            )
                                        }
                                    )
                                })
                        }

                        items(cartState.cartItems, key = { it.id }) { product ->
                            val isFavorite by favoriteViewModel.getFavoriteStatus(product.id)
                                .collectAsState(initial = product.isFavorite)
                            PaddedSection(content = {
                                CartItemCard(
                                    isFavorite = isFavorite,
                                    product = product,
                                    onQuantityChange = { newQuantity ->
                                        cartViewModel.updateQuantity(product.id, newQuantity)
                                    },
                                    onRemove = {
                                        cartViewModel.removeFromCart(product)
                                    },
                                    onAddToFavoriteClick = {
                                        favoriteViewModel.toggleFavorite(product)
                                    },
                                    onProductClick = { onProductClick(product) },
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedContentScope = animatedContentScope,
                                    onSignInClick = onSignInClick
                                )
                            })
                        }

                        // In the "Success State with items" section, update the RelatedProductsSection:
                        if (cartState.cartItems.isNotEmpty()) {
                            item {
                                // Get the most common category from cart items
                                val categoryCounts = cartState.cartItems
                                    .groupBy { it.category }
                                    .mapValues { it.value.size }

                                val mostCommonCategory = categoryCounts
                                    .maxByOrNull { it.value }
                                    ?.key ?: ""

                                // Alternative: Get all categories and use the first non-empty one
                                val allCategories = cartState.cartItems
                                    .map { it.category }
                                    .filter { it.isNotBlank() }
                                    .distinct()

                                val firstCategory = allCategories.firstOrNull() ?: ""

                                // Use the most common category, fallback to first category
                                val selectedCategory = mostCommonCategory.ifBlank { firstCategory }

                                RelatedProductsSection(
                                    categoryName = selectedCategory,
                                    currentProductId = cartState.cartItems.joinToString(",") { it.id },
                                    onProductClick = { product ->
                                        onRelatedProductClick(product)
                                    },
                                    maxItems = 8,
                                    onSignInClick = onSignInClick
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
            // Show loading indicator in actions when refreshing
            if (cartState.isLoading) {
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

                if (cartState.cartItems.isNotEmpty()) {
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
 * Individual cart item card component.
 * Displays item details with quantity controls and remove button.
 *
 * @param product The [ProductItem] to display.
 * @param onQuantityChange Callback when quantity is adjusted.
 * @param onRemove Callback when the item is removed from cart.
 * @param onAddToFavoriteClick Callback when the item is added to favorites.
 * @param isFavorite Whether the item is currently a favorite.
 * @param sharedTransitionScope Scope for shared element transitions.
 * @param animatedContentScope Scope for animated content.
 * @param onProductClick Callback when the product card is clicked.
 * @param onSignInClick Callback when sign-in is required for an action.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CartItemCard(
    product: ProductItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    onAddToFavoriteClick: () -> Unit,
    isFavorite: Boolean = false,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onProductClick: () -> Unit,
    onSignInClick: () -> Unit
) {

    CustomItemCard(
        product = product,
        actions = { isAuthenticated, showSignInDialog ->
            FavoriteSplashButton(
                isFavorite = isFavorite,
                onToggle = {
                    if (isAuthenticated) {
                        onAddToFavoriteClick()
                    } else {
                        showSignInDialog() // Trigger the dialog
                    }
                }
            )

            // Delete button
            ButtonIconComposable(
                onClick = {
                    if (isAuthenticated) {
                        onRemove()
                    } else {
                        showSignInDialog() // Trigger the dialog
                    }
                },
                buttonIcon = ButtonIcon.Vector(Icons.Filled.Delete),
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error,
            )
        },
        onSignInClick = onSignInClick,
        bottomComponent = {
            // Quantity selector (only show if item is in stock)
            if (product.inStock) {
                QuantitySelector(
                    quantity = product.quantity,
                    onQuantityChange = onQuantityChange
                )
            }
        },
        onProductClick = { onProductClick() },
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
    )
}

/**
 * Quantity selector component with increase/decrease buttons.
 * Allows users to adjust the quantity of items in their cart.
 *
 * @param quantity The current quantity to display.
 * @param onQuantityChange Callback invoked with the new quantity value.
 */
@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    // Cache the callbacks to prevent recreation
    val onDecrease = remember(quantity) {
        { if (quantity > 1) onQuantityChange(quantity - 1) }
    }

    val onIncrease = remember(quantity) {
        { onQuantityChange(quantity + 1) }
    }

    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                CustomShape.mediumShape()
            )
            .padding(
                horizontal = windowSizeConstant.normalVerticalPadding,
                vertical = windowSizeConstant.smallVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Decrease quantity button (disabled when quantity is 1)
        Box(
            modifier = Modifier
                .size(windowSizeConstant.baseSize)
                .clickable(
                    enabled = quantity > 1,
                    onClick = onDecrease
                ),
            contentAlignment = Alignment.Center
        ) {
            CustomIcon(
                icon = Icons.Filled.Remove,
                contentDescription = "Decrease quantity",
                tint = if (quantity > 1) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        }

        // Current quantity display
        Text(
            text = quantity.toString(),
            style = windowSizeConstant.bodyTextStyle,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.widthIn(min = windowSizeConstant.baseSize),
            textAlign = TextAlign.Center
        )

        // Increase quantity button
        Box(
            modifier = Modifier
                .size(windowSizeConstant.baseSize)
                .clickable(onClick = onIncrease),
            contentAlignment = Alignment.Center
        ) {
            CustomIcon(
                icon = Icons.Filled.Add,
                contentDescription = "Increase quantity",
                iconSize = windowSizeConstant.basePadding,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

