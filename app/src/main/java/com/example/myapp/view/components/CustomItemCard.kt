package com.example.myapp.view.components

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
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
import com.example.myapp.view.screens.product.product_rating_and_reviews.ProductRating
import com.example.myapp.view.utils.CloudinaryHelper
import com.example.myapp.view.utils.CustomShape
import com.example.myapp.view.utils.formatPrice
import com.example.myapp.view.utils.primeUtils.isUserPrimeMember

/**
 * CustomItemCard - Product list item card with shared element transitions
 *
 * Displays product information in a horizontal card layout with image, details,
 * and action buttons. Supports shared element transitions for smooth navigation animations.
 *
 * ## Features
 * - **Shared Element Transitions**: Animated image transitions between screens
 * - **Product Image**: Displays product image with discount badge and stock overlay
 * - **Discount Badge**: Shows percentage off when old price exists
 * - **Stock Status**: Visual indicator for in-stock/out-of-stock
 * - **Product Details**: Name, price, stock status, rating
 * - **Rating Display**: Star rating visualization
 * - **Custom Actions**: Slot for action buttons (favorite, cart, etc.)
 * - **Bottom Component**: Slot for additional content below details
 * - **Adaptive Sizing**: Adjusts to different screen sizes
 *
 * ## Displayed Information
 * - Product image with discount badge (if applicable)
 * - Product name (truncated to 1 line)
 * - Current price (highlighted in primary color)
 * - Stock status (in stock/out of stock)
 * - Star rating
 * - Out of stock overlay (if not in stock)
 *
 * ## Layout Structure
 * - Left: Product image (100dp) with badges/overlays
 * - Center: Product details (name, price, stock, rating)
 * - Right: Action buttons (favorites, cart, etc.)
 *
 * @param modifier Modifier for the card
 * @param product Product data to display
 * @param bottomComponent Composable slot for content below product details
 * @param actions Composable slot for action buttons (favorite, cart, etc.)
 * @param sharedTransitionScope Scope for shared element transitions
 * @param animatedContentScope Scope for animated content
 * @param onProductClick Callback when card is clicked
 *
 * @see ProductItem for product data structure
 */
@OptIn(ExperimentalSharedTransitionApi::class)

@Composable
fun CustomItemCard(
    modifier: Modifier = Modifier,
    product: ProductItem,
    bottomComponent: @Composable () -> Unit = {},
    actions: @Composable (isAuthenticated: Boolean, showSignInDialog: () -> Unit) -> Unit = { _, _ -> },
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onProductClick: () -> Unit,
    onSignInClick: () -> Unit,
    imageLoader: ImageLoader? = null,
    cloudinaryHelper: CloudinaryHelper = CloudinaryHelper(),
    authViewModel: AuthViewModel = hiltViewModel(),
    primeViewModel: PrimeMembershipViewModel = hiltViewModel(), 
) {

    val windowSizeConstant = LocalWindowSizeConstant.current
    val primeState by primeViewModel.membershipState.collectAsState()
    val membershipStatus = primeState.membership?.status // This is your MembershipStatus enum
    val authState by authViewModel.authState.collectAsState()

    val isPrimeMember = isUserPrimeMember(membershipStatus) // Returns true if ACTIVE

    var showAuthDialog by remember { mutableStateOf(false) }

    // Discount Badge and Out of Stock Overlay
    val hasDiscount = product.oldPrice > 0 && product.oldPrice > product.price

    val discount = if (hasDiscount) {
        ((product.oldPrice - product.price) / product.oldPrice) * 100.0
    } else {
        0.0
    }

    // Check if Prime eligible and user is Prime member
    val isPrimeEligible = product.tags.contains("prime_eligible")
    val showPrimeBadge = isPrimeEligible && isPrimeMember
    val isAuthenticated = authState.isSignedIn

    Card(
        modifier = modifier
            .then(windowSizeConstant.adaptiveListCardWidthModifier)
            .clickable { onProductClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = customSpacing.custom4
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeConstant.normalVerticalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //image container
            with(sharedTransitionScope) {
                Box(
                    modifier = Modifier
                        .size(windowSizeConstant.listImagePadding)
                        .clip(CustomShape.mediumShape())
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    CustomImageContainer(
                        data = cloudinaryHelper.getImageUrl(product.imageUrl),
                        imageLoader = imageLoader,
                        contentDescription = product.productName,
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "image-${product.id}"),
                            animatedVisibilityScope = animatedContentScope,
                            boundsTransform = { _, _ ->
                                tween(
                                    durationMillis = 500,
                                    easing = FastOutSlowInEasing
                                )
                            }
                        ),
                    )

                    // Badges Container (Top-Left)
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .zIndex(1f)
                            .offset(x = customSpacing.custom1, y = -customSpacing.custom14),
                        verticalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding)
                    ) {
                        // Manual Discount Badge
                        if (discount > 0) {
                            CustomSurfaceContainer(
                                color = colors.white,
                                textStr = "${discount.toInt()}% OFF",
                                contentDescription = "discount",
                                textColor = colors.customColor6
                            )
                        }
                    }

                    if (!product.inStock) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    CustomShape.mediumShape()
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.out_of_stock),
                                fontSize = 10.sp,
                                style = windowSizeConstant.bodyTextStyle,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            CustomSpacer(modifier = Modifier.width(windowSizeConstant.basePadding))

            // Product Details with sharedBounds for text
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(6f)
                ) {
                    Text(
                        text = product.productName,
                        style = windowSizeConstant.bodyTextStyle,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

                    // Price Row with Prime savings
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding)
                    ) {
                        Text(
                            text = formatPrice(product.price),
                            style = windowSizeConstant.bodyTextStyle,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.primary
                        )

                        //  Show Prime savings preview
                        if (showPrimeBadge) {
                            val primePrice = product.price * 0.8 // 20% off
                            Text(
                                text = formatPrice(primePrice),
                                style = windowSizeConstant.labelTextStyle,
                                color = colors.customColor16,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (product.oldPrice > 0) {
                        Text(
                            text = formatPrice(product.oldPrice),
                            style = windowSizeConstant.labelTextStyle,
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

                    if (product.inStock) {
                        Text(
                            text = stringResource(R.string.in_stock),
                            style = windowSizeConstant.labelTextStyle,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

                    // Brand & Category
                    if (product.category.isNotEmpty()) {
                        CustomAssistChip(
                            onClick = { /*DO NOTHING */ },
                            label = product.category,
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.height(windowSizeConstant.baseSize)
                        )
                    }

                    // Rating section (optional) - displays star rating with partial stars
                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

                    ProductRating(
                        rating = product.rating,
                        maxRating = 5
                    )

                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseVerticalPadding))

                    bottomComponent()
                }

                CustomSpacer(modifier = Modifier.width(windowSizeConstant.baseVerticalPadding))

                Column(
                    modifier = Modifier
                        .weight(2f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(windowSizeConstant.baseNormalVerticalPadding)
                ) {
                    actions(isAuthenticated) {
                        showAuthDialog = true
                    }
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

