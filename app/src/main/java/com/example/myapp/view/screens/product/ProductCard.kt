package com.example.myapp.view.screens.product

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import com.example.myapp.R
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.model.AuthViewModel
import com.example.myapp.data.model.PrimeMembershipViewModel
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomImageContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomSurfaceContainer
import com.example.myapp.view.components.SignInRequiredDialog
import com.example.myapp.view.components.custom.buttons.CustomCartButton
import com.example.myapp.view.components.custom.buttons.FavoriteSplashButton
import com.example.myapp.view.screens.product.product_rating_and_reviews.ProductRating
import com.example.myapp.view.utils.CloudinaryHelper
import com.example.myapp.view.utils.CustomShape
import com.example.myapp.view.utils.calculateDiscountedPrice
import com.example.myapp.view.utils.formatPrice
import com.example.myapp.view.utils.primeUtils.isUserPrimeMember
import kotlinx.coroutines.delay

/**
 * ProductCard - Composable function for displaying product information in a card format.
 *
 * This composable creates a visually appealing product card with the following features:
 * - Product image with favorite/wishlist functionality.
 * - Product name with text overflow handling.
 * - Price display with optional old price (for discounts).
 * - Star rating system with partial star support.
 * - Interactive animations (scale and elevation on press).
 * - Optional selection state with border highlighting.
 * - Cart interaction with loading and animation states.
 * - Prime member benefits visualization.
 *
 * The card uses a two-section layout:
 * - Top section: Product image with favorite icon overlay and badges.
 * - Bottom section: Product details, pricing, rating, and actions.
 *
 * @param modifier Modifier to be applied to the root composable.
 * @param product The [ProductItem] data to display.
 * @param onAddToCart Callback invoked when the add-to-cart button is clicked.
 * @param onFavoriteClick Callback invoked when the favorite/heart icon is clicked.
 * @param isInCart Whether the product is currently in the shopping cart.
 * @param onProductClick Callback invoked when the card itself is clicked.
 * @param onSignInClick Callback invoked when an action requires authentication.
 * @param isFavorite Whether the product is currently marked as a favorite.
 * @param authViewModel ViewModel for observing authentication state.
 * @param primeViewModel ViewModel for observing Prime membership status.
 * @param imageLoader [ImageLoader] for handling image loading.
 * @param imageRes Optional local drawable resource ID for the product image (fallback).
 * @param isLoading Whether the card (specifically cart action) is in a loading state.
 * @param cloudinaryHelper Helper for processing Cloudinary image URLs.
 */

@Composable
fun ProductCard(
    modifier: Modifier = Modifier,
    product: ProductItem,
    onAddToCart: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    isInCart: Boolean = false,
    onProductClick: () -> Unit,
    onSignInClick: () -> Unit,
    isFavorite: Boolean = false,
    authViewModel: AuthViewModel = hiltViewModel(),
    primeViewModel: PrimeMembershipViewModel = hiltViewModel(),
    imageLoader: ImageLoader? = null,
    @DrawableRes imageRes: Int? = null,
    isLoading: Boolean = false,
    cloudinaryHelper: CloudinaryHelper = CloudinaryHelper(),
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val authState by authViewModel.authState.collectAsState()
    val primeState by primeViewModel.membershipState.collectAsState()

    var showAuthDialog by remember { mutableStateOf(false) }

    val membershipStatus = primeState.membership?.status
    val isPrimeMember = isUserPrimeMember(membershipStatus)

    val isPrimeEligible = product.tags.contains("prime_eligible")
    val isFlashDeal = product.tags.contains("flash_deal")  
    val isBestSeller = product.tags.contains("best_seller")  

    val showPrimeBadge = isPrimeEligible && isPrimeMember

    // calculation (this is based on price, not tags)
    val hasDiscount =
        product.oldPrice > 0 && product.oldPrice > product.price

    val discount = if (hasDiscount) {
        calculateDiscountedPrice(
            product.price,
            product.oldPrice
        )
    } else {
        0.0
    }

    // Animation states
    var isCartAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(authState.isSignedIn) {
        if (!authState.isSignedIn) {
            primeViewModel.clearPrimeStatus()
        }
    }

    LaunchedEffect(isInCart) {
        isCartAnimating = true
        delay(300)
        isCartAnimating = false
    }

    val cartButtonScale by animateFloatAsState(
        targetValue = if (isCartAnimating) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "cart_button_scale"
    )

    val cartButtonColor by animateColorAsState(
        targetValue = if (isInCart)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(durationMillis = 400, easing = EaseInOutCubic),
        label = "cart_button_color"
    )

    val cartButtonContentColor by animateColorAsState(
        targetValue = if (isInCart)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 400, easing = EaseInOutCubic),
        label = "cart_button_content_color"
    )

    Card(
        modifier = modifier.then(Modifier.size(windowSizeConstant.productCardPaddings)
            .wrapContentHeight()
            .clickable(onClick = onProductClick))
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(customSpacing.custom150)
            ) {
                // Product Image
                if (product.imageUrl.isNotEmpty()) {
                    CustomImageContainer(
                        data = cloudinaryHelper.getImageUrl(product.imageUrl),
                        contentDescription = product.productName,
                        imageLoader = imageLoader,
                        placeholder = painterResource(R.drawable.image_placeholder),
                        error = painterResource(R.drawable.network_error),
                        shape = CustomShape.mediumShape(),
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (imageRes != null) {
                    CustomImageContainer(
                        data = imageRes,
                        contentDescription = product.productName,
                        shape = CustomShape.mediumShape())

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(
                                CustomShape.mediumShape()
                            )
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CustomIcon(
                            icon = Icons.Filled.Image,
                            contentDescription = "No image",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            iconSize = windowSizeConstant.largeIconSize
                        )
                    }
                }

                // BADGES OVERLAY - based on tags
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .zIndex(1f)
                            .offset(x = -windowSizeConstant.baseNormalVerticalPadding, y = -windowSizeConstant.contentVerticalPadding)
                            .padding(
                                horizontal = windowSizeConstant.normalVerticalPadding,
                                vertical = windowSizeConstant.baseVerticalPadding
                            )
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding)
                        ) {
                            // Discount Badge (still calculated from price)
                            if (hasDiscount && discount > 0) {
                                CustomSurfaceContainer(
                                    color = colors.white,
                                    textStr = "${discount.toInt()}% OFF",
                                    contentDescription = "discount",
                                    textColor = colors.customColor6
                                )
                            }

                            // Flash Deal Badge (from tags)
                            if (isFlashDeal) {
                                CustomSurfaceContainer(
                                    color = colors.customColor6,
                                    icon = Icons.Filled.FlashOn,
                                    text = R.string.flash,
                                    contentDescription = "flash deal"
                                )
                            }

                            // Best Seller Badge (from tags)
                            if (isBestSeller) {
                                CustomSurfaceContainer(
                                    color = colors.customColor5,
                                    icon = Icons.Filled.VerifiedUser,
                                    text = R.string.best_seller,
                                    contentDescription = "best seller"
                                )
                            }
                        }
                    }

                    // Favorite button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = customSpacing.custom1, y = -customSpacing.custom2)
                            .zIndex(1f)
                    ) {
                        FavoriteSplashButton(
                            isFavorite = isFavorite,
                            onToggle = {
                                if (authState.isSignedIn) {
                                    onFavoriteClick()
                                } else {
                                    showAuthDialog = true
                                }
                            }
                        )
                    }
                }

            }

            // Product Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(windowSizeConstant.adaptiveProductCardHeight)
                    .padding(windowSizeConstant.smallVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding)
            ) {
                // Product Name
                Text(
                    text = product.productName,
                    style = windowSizeConstant.bodyTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Price with Prime preview
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding)
                ) {
                    Text(
                        text = formatPrice(product.price),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = windowSizeConstant.bodyTextStyle,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Old Price
                    if (product.oldPrice > 0 && hasDiscount) {
                        Text(
                            text = formatPrice(product.oldPrice),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = windowSizeConstant.labelTextStyle,
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                // Rating
                ProductRating(
                    rating = product.rating,
                    maxRating = 5
                )

                // Prime Savings Info
                if (showPrimeBadge) {
                    val primeSavings = product.price * 0.20
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding)
                    ) {
                        CustomIcon(
                            icon = Icons.Filled.Savings,
                            contentDescription = "savings",
                            iconSize = windowSizeConstant.baseIconSize,
                            tint = colors.customColor16
                        )

                        Text(
                            text = "Save ${formatPrice(primeSavings)} with Prime",
                            style = windowSizeConstant.labelTextStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = colors.customColor16,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.contentVerticalPadding))
                }

                // Add to Cart Button
                Row{
                    CustomCartButton(
                        isInCart = isInCart,
                        isLoading = isLoading,
                        onAddToCart = {
                            if (authState.isSignedIn) {
                                isCartAnimating = true
                                onAddToCart()
                            } else {
                                showAuthDialog = true
                            }
                        },
                        cartButtonColor = cartButtonColor,
                        cartButtonContentColor = cartButtonContentColor,
                        cartButtonScale = cartButtonScale
                    )
                }
            }
        }
    }

    if (showAuthDialog) {
        SignInRequiredDialog(
            onDismiss = { showAuthDialog = false },
            onSignInClick = {
                showAuthDialog = false
                onSignInClick()
            }
        )
    }
}



