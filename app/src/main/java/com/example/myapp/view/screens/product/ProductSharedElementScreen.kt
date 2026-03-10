package com.example.myapp.view.screens.product

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomImageContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FormContainer
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.screens.product.product_rating_and_reviews.ProductRating
import com.example.myapp.view.utils.CloudinaryHelper

/**
 * ProductSharedElementScreen - Product detail screen with shared element transitions.
 *
 * This composable creates a product detail view that supports smooth shared element
 * transitions from the product list. It displays the product image and details with
 * animated transitions that create a seamless user experience when navigating
 * between product list and detail views.
 *
 * Features:
 * - Shared element transitions for product images
 * - Shared element transitions for product text content
 * - Smooth animations with custom easing curves
 * - Responsive layout with proper spacing
 *
 * @param id Unique identifier for the shared element transition
 * @param item Product data to display
 * @param sharedTransitionScope Scope for managing shared element transitions
 * @param animatedContentScope Scope for managing content animations
 * @param onBackPressed Callback for back navigation (currently commented out)
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProductSharedElementScreen(
    id: String,
    item: ProductItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBackPressed: () -> Unit,
    oldPrice: Double? = 0.0,
    cloudinaryHelper: CloudinaryHelper = CloudinaryHelper(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val scrollState = rememberScrollState()
    val networkState = rememberNetworkState(networkManager)

    // Use shared transition scope for coordinated animations
    with(sharedTransitionScope) {
        // Main content column with centered alignment and consistent spacing
        FormContainer(scrollState = scrollState) {
            // Top spacing for proper layout
            // Network Status Banner
            // Network Indicator in top bar

            if (!networkState.hasInternet) {
                CustomSpacer()
                NetworkIndicator(networkState = networkState)

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

            CustomSpacer()

            // Main content section with padding
            PaddedSection(
                alignment = Alignment.CenterHorizontally,
                content = {
                    // Product image with shared element transition
                    CustomImageContainer(
                        data = cloudinaryHelper.getImageUrl(item.imageUrl),
                        contentDescription = item.category,
                        modifier = windowSizeConstant.productImageSize.then(
                            Modifier
                                .height(windowSizeConstant.customImageHeight)
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "image-$id"),
                                    animatedVisibilityScope = animatedContentScope,
                                    exit = fadeOut(),
                                    boundsTransform = { _, _ ->
                                        tween(
                                            durationMillis = 500, // Animation duration for smooth transition
                                            easing = FastOutSlowInEasing // Smooth easing curve for natural movement
                                        )
                                    }
                                )
                        )
                    )

                    CustomSpacer()

                    // Product headline widget with shared element transition for text content
                    HeadlineWidget(
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "text-$id"),
                            animatedVisibilityScope = animatedContentScope
                        ),
                        middleTextStr = item.productName, // Product name
                        subMiddleTextStr = item.description // Product description
                    )

                    CustomSpacer()

                    // Brand information
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Brand: ${item.brand}",
                            style = windowSizeConstant.bodyTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CustomSpacer()

                        // Price section with optional old price and more info button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.baseNormalVerticalPadding)
                        ) {
                            // Current price display
                            Text(
                                text = "$${item.price}",
                                style = windowSizeConstant.bodyTextStyle,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Old price display (optional) - shows strikethrough for discounts
                            if (oldPrice != null) {
                                Text(
                                    text = "$${item.oldPrice}",
                                    style = windowSizeConstant.labelTextStyle,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }

                        CustomSpacer()

                        ProductRating(
                            rating = item.rating,
                            maxRating = 5
                        )
                    }
                }
            )
        }
    }
}
