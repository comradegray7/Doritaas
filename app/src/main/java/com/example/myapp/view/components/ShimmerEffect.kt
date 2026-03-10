package com.example.myapp.view.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.utils.CustomShape

/**
 * ShimmerEffect - Extension function that adds a shimmer loading animation to any composable.
 *
 * This modifier creates a moving gradient effect that simulates content loading.
 * It uses an infinite animation with a linear gradient brush that moves across the surface.
 *
 * Usage:
 * ```
 * Box(
 *     modifier = Modifier
 *         .size(customSpacing.custom100)
 *         .shimmerEffect()
 * )
 * ```
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    // Define shimmer colors with varying opacity for gradient effect
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Main shimmer color
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), // Transparent middle
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Main shimmer color
    )

    // Create infinite animation transition for continuous shimmer effect
    val transition = rememberInfiniteTransition(label = "shimmer")

    val translateAnim by transition.animateFloat(
        initialValue = 0f, // Start position
        targetValue = 1000f, // End position
        animationSpec = infiniteRepeatable(
            animation = tween(
                1200,
                easing = LinearEasing
            ), // 1.2 second duration with linear easing
            repeatMode = RepeatMode.Restart // Restart animation when it completes
        ),
        label = "shimmerAnimation"
    )

    // Create linear gradient brush that moves across the surface
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero, // Start from top-left
        end = Offset(x = translateAnim, y = translateAnim) // End at animated position
    )

    // Apply the shimmer background
    background(brush = brush)
}

/**
 * ProductCarouselShimmer - Composable for displaying a shimmer loading state for product carousels.
 *
 * This composable creates a horizontal list of shimmer placeholders that mimic
 * the layout of a product carousel. It uses adaptive sizing based on window size.
 *
 * Usage:
 * ```
 * ProductCarouselShimmer()
 * ```
 */
@Composable
fun ProductCarouselShimmer() {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Column(
        modifier = Modifier.fillMaxWidth(), // Take full width
        horizontalAlignment = Alignment.CenterHorizontally // Center content
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = windowSizeConstant.contentPadding), // Adaptive horizontal padding
            horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.carouselPageSpacing), // Adaptive spacing
            modifier = Modifier
                .fillMaxWidth()
                .height(windowSizeConstant.carouselImageHeight) // Adaptive height
        ) {
            items(10) { // Display 10 shimmer items
                Box(
                    modifier = Modifier
                        .width(windowSizeConstant.carouselImageWidth) // Adaptive width
                        .height(windowSizeConstant.carouselImageHeight) // Adaptive height
                        .clip(CustomShape.extraLargeShape()) // Rounded corners
                        .shimmerEffect() // Apply shimmer animation
                )
            }
        }
    }
}

/**
 * CustomItemCardShimmer - Composable for displaying a shimmer loading state for item cards.
 *
 * This composable creates a card-shaped shimmer placeholder that mimics the layout
 * of a typical item card with image, text content, and action buttons.
 *
 * @param modifier Optional modifier to apply to the shimmer card
 *
 * Usage:
 * ```
 * CustomItemCardShimmer()
 * ```
 */
@Composable
fun CustomItemCardShimmer(
    modifier: Modifier = Modifier
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    customSpacing

    PaddedSection(
        content = {
            Card(
                modifier = modifier.then(windowSizeConstant.adaptiveWidthModifier),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = windowSizeConstant.smallVerticalPadding
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(windowSizeConstant.listCardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shimmer placeholder for item image
                    Box(
                        modifier =  Modifier
                            .size(windowSizeConstant.listImagePadding)
                            .clip(CustomShape.mediumShape())
                            .shimmerEffect()
                    )

                    // Shimmer placeholders for text content
                    Column(
                        modifier = Modifier
                            .padding(horizontal = windowSizeConstant.listRightPadding)
                            .weight(1f, fill = false)
                    ) {
                        // Main title shimmer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(windowSizeConstant.basePadding) // 16.dp -> medium
                                .clip(CustomShape.mediumShape())
                                .shimmerEffect()
                        )

                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding)) // 4.dp

                        // Subtitle shimmer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(windowSizeConstant.baseNormalVerticalPadding)
                                .clip(CustomShape.mediumShape())
                                .shimmerEffect()
                        )

                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding)) // 8.dp

                        // Price shimmer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.3f)
                                .height(windowSizeConstant.basePadding) // 16.dp
                                .clip(CustomShape.mediumShape())
                                .shimmerEffect()
                        )
                    }

                    // Shimmer placeholders for action buttons
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(windowSizeConstant.baseNormalVerticalPadding) // 12.dp
                    ) {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .size(windowSizeConstant.baseSize) // 24.dp
                                    .clip(CustomShape.extraLargeShape())
                                    .shimmerEffect()
                            )
                        }
                    }
                }
            }
        })
}

/**
 * ProductCardShimmer - Displays a shimmer placeholder for a product card.
 *
 * @param modifier Modifier to apply to the card.
 * @param isSelected Whether the card is in a selected state (affects border color).
 */
@Composable
fun ProductCardShimmer(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val spacing = customSpacing

    Card(
        modifier = modifier
            .width(spacing.custom180) // 180.dp
            .height(windowSizeConstant.cardHeight)
            .border(
                width = if (isSelected) spacing.customHalf else spacing.customZero2,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                shape = CustomShape.mediumShape()
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(CustomShape.mediumShape())
                    .shimmerEffect()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = windowSizeConstant.baseNormalVerticalPadding,
                        vertical = windowSizeConstant.normalVerticalPadding
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(
                    modifier = Modifier
                        .height(windowSizeConstant.baseNormalVerticalPadding)
                        .fillMaxWidth(0.8f)
                        .clip(CustomShape.mediumShape())
                        .shimmerEffect()
                )

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .height(windowSizeConstant.basePadding)
                            .width(spacing.custom40) // 40.dp
                            .clip(CustomShape.mediumShape())
                            .shimmerEffect()
                    )

                    Box(
                        modifier = Modifier
                            .height(windowSizeConstant.normalVerticalPadding)
                            .width(spacing.custom30) // 30.dp
                            .clip(CustomShape.mediumShape())
                            .shimmerEffect()
                    )

                    Box(
                        modifier = Modifier
                            .size(windowSizeConstant.basePadding)
                            .clip(CustomShape.extraLargeShape())
                            .shimmerEffect()
                    )
                }

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

                ProductRatingShimmer()

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))
            }
        }
    }
}

/**
 * ProductRatingShimmer - Displays a shimmer placeholder for a product rating row.
 *
 * @param modifier Modifier to apply to the row.
 * @param maxRating Number of rating icons to display.
 */
@Composable
fun ProductRatingShimmer(
    modifier: Modifier = Modifier,
    maxRating: Int = 10
) {
    customSpacing
    val windowSizeConstant = LocalWindowSizeConstant.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxRating) {
            Box(
                modifier = Modifier
                    .size(windowSizeConstant.basePadding)
                    .clip(CustomShape.extraLargeShape())
                    .shimmerEffect()
            )
        }

        CustomSpacer(modifier = Modifier.width(windowSizeConstant.normalVerticalPadding))

        Box(
            modifier = Modifier
                .height(windowSizeConstant.baseNormalVerticalPadding)
                .width(windowSizeConstant.baseSize) // 24.dp
                .clip(CustomShape.mediumShape())
                .shimmerEffect()
        )
    }
}

/**
 * CustomBottomSectionShimmer - A loading placeholder for the bottom action section. */
@Composable
fun CustomBottomSectionShimmer(
    modifier: Modifier = Modifier
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Shimmer for the Total Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for "Total" label
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(20.dp)
                    .clip(CustomShape.extraLargeShape())
                    .shimmerEffect() // Applying your shimmer animation
            )

            // Placeholder for Amount
            Box(
                modifier = Modifier
                    .width(windowSizeClass.customSpacerMedium)
                    .height(windowSizeClass.baseSize)
                    .clip(CustomShape.extraLargeShape())
                    .shimmerEffect()
            )
        }

        // Adaptive spacing
        CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

        // Shimmer for the Action Button
        Box(modifier = Modifier
                .fillMaxWidth()
                .height(windowSizeClass.customButtonPadding) // Match your theme's button height
                .clip(CustomShape.extraLargeShape()) // Match your theme's radius
                .shimmerEffect()
        )
    }
}


/**
 * ClickableSearchBarShimmer - Shimmer placeholder for a clickable search bar.
 *
 */
@Composable
fun ClickableSearchBarShimmer(
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val spacing = customSpacing

    Box(
        modifier = windowSizeConstant.adaptiveWidthModifier
            .clip(MaterialTheme.shapes.medium)
            .height(windowSizeConstant.adaptiveHeight)
            .shimmerEffect()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = windowSizeConstant.baseNormalVerticalPadding), // 12.dp
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(windowSizeConstant.baseSize) // 24.dp
                        .clip(CustomShape.extraLargeShape())
                        .shimmerEffect()
                )
                CustomSpacer(modifier = Modifier.width(windowSizeConstant.normalVerticalPadding))
                Box(
                    modifier = Modifier
                        .height(windowSizeConstant.basePadding) // 16.dp
                        .width(windowSizeConstant.customSpacerLarge) // 100.dp
                        .clip(CustomShape.mediumShape())
                        .shimmerEffect()
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(windowSizeConstant.iconSize)
                        .clip(CustomShape.extraLargeShape())
                        .shimmerEffect()
                )

                Box(
                    modifier = Modifier
                        .width(spacing.customHalf) // 1.5.dp
                        .height(windowSizeConstant.baseSize) // 24.dp
                        .shimmerEffect()
                )

                Box(
                    modifier = Modifier
                        .size(windowSizeConstant.iconSize)
                        .clip(CustomShape.extraLargeShape())
                        .shimmerEffect()
                )
            }
        }
    }
}

/**
 * SmallProductImageShimmer - Shimmer placeholders for a row of small product images.
 *
 * @param count Number of shimmer images to display.
 */
@Composable
fun SmallProductImageShimmer(
    count: Int = 10
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    CustomLazyRow {
        items(count) {
            Box(
                modifier = Modifier
                    .padding(horizontal = windowSizeConstant.smallVerticalPadding)
                    .size(windowSizeConstant.customSpacerMedium)
                    .clip(CustomShape.mediumShape())
                    .shimmerEffect()
            )
        }
    }
}

/**
 * ButtonIconShimmer - Shimmer placeholder for a circular button icon.
 *
 * @param modifier Modifier to apply to the icon.
 */
@Composable
fun ButtonIconShimmer(
    modifier: Modifier = Modifier
) {
    val spacing = customSpacing

    Box(
        modifier = modifier
            .size(spacing.custom36)
            .clip(CustomShape.extraLargeShape())
            .shimmerEffect()
    )
}

/**
 * TopBarActionsShimmer - Shimmer placeholders for top bar action icons.
 */
@Composable
fun TopBarActionsShimmer() {
    customSpacing
    val windowSizeConstant = LocalWindowSizeConstant.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
    ) {
        ButtonIconShimmer()
        ButtonIconShimmer()
    }
}

/**
 * ProductSummaryCardShimmer - Shimmer placeholder for a product summary card.
 *
 * @param modifier Modifier to apply to the card.
 */
@Composable
fun ProductSummaryCardShimmer(
    modifier: Modifier = Modifier
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val spacing = customSpacing

    val radialGradient = Brush.radialGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        radius = 800f
    )

    Card(
        modifier = modifier.then(windowSizeConstant.adaptiveWidthModifier),
        elevation = CardDefaults.cardElevation(defaultElevation = windowSizeConstant.normalVerticalPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(radialGradient)
                .padding(windowSizeConstant.listCardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(
                        width = spacing.custom60,
                        height = windowSizeConstant.baseSize
                    )
                    .clip(MaterialTheme.shapes.small)
                    .shimmerEffect()
            )

            Box(
                modifier = Modifier
                    .size(windowSizeConstant.productSummaryImagePadding)
                    .clip(CustomShape.mediumShape())
                    .shimmerEffect()
            )

            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = windowSizeConstant.listRightPadding)
                    .height(windowSizeConstant.baseSize)
                    .clip(CustomShape.mediumShape())
                    .shimmerEffect()
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(windowSizeConstant.baseVerticalPadding)
            ) {
                Box(
                    modifier = Modifier
                        .size(
                            width = spacing.custom50,
                            height = windowSizeConstant.basePadding
                        )
                        .clip(CustomShape.mediumShape())
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .size(
                            width = spacing.custom60,
                            height = windowSizeConstant.baseSize
                        )
                        .clip(CustomShape.mediumShape())
                        .shimmerEffect()
                )
            }

            Box(
                modifier = Modifier
                    .size(windowSizeConstant.baseSize)
                    .clip(CustomShape.extraLargeShape())
                    .shimmerEffect()
            )
        }
    }
}

/**
 * SearchListShimmer - Shimmer placeholders for a list of search results.
 */
@Composable
fun SearchListShimmer() {
    val windowSizeConstant = LocalWindowSizeConstant.current
    customSpacing

    PaddedSection(
        content = {
            CustomLazyColumn {
                items(10) {
                    Card(
                        modifier = windowSizeConstant.adaptiveWidthModifier,
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = windowSizeConstant.cardElevationPadding
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(windowSizeConstant.listCardPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(windowSizeConstant.listImagePadding)
                                    .clip(CustomShape.mediumShape())
                                    .shimmerEffect()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(windowSizeConstant.baseSize)
                                    .clip(CustomShape.mediumShape())
                                    .shimmerEffect()
                            )

                            Box(
                                modifier = Modifier
                                    .size(windowSizeConstant.baseSize)
                                    .clip(CustomShape.extraLargeShape())
                                    .shimmerEffect()
                            )
                        }
                    }
                }
            }
        })
}

/**
 * ProfileCardShimmer - Shimmer placeholders for a profile card and related items.
 */
@Composable
fun ProfileCardShimmer() {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val spacing = customSpacing

    CustomLazyColumn(verticalArrangement = Arrangement.Center) {
        item {
            PaddedSection(
                alignment = Alignment.CenterHorizontally,
                content = {
                    Box(
                        modifier = windowSizeConstant.productImageSize.then(
                            Modifier
                                .height(windowSizeConstant.customImageHeight)
                                .clip(CustomShape.mediumShape())
                                .shimmerEffect()
                        )
                    )
                })
        }

        items(1) {
            CustomSpacer()
            PaddedSection(
                alignment = Alignment.CenterHorizontally,
                content = {
                    CustomListCardShimmer(count = 1)
                })
            CustomSpacer()
        }

        items(
            1
        ) {
            Column(
                modifier = windowSizeConstant.adaptiveWidthModifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(spacing.custom28)
                        .fillMaxWidth(0.8f)
                        .clip(CustomShape.mediumShape())
                        .shimmerEffect()
                )
            }
        }
    }
}

/**
 * ProductDescriptionShimmer - Shimmer placeholders for a product description screen.
 */
@Composable
fun ProductDescriptionShimmer() {
    val spacing = customSpacing
    val windowSizeConstant = LocalWindowSizeConstant.current

    CustomLazyColumn {
        item {
            PaddedSection(
                content = {
                    Box(
                        modifier = windowSizeConstant.productImageSize.then(
                            Modifier
                                .clip(CustomShape.mediumShape())
                                .shimmerEffect()
                        )
                    )
                })
        }

        item {
            SmallProductImageShimmer(count = 10)
        }

        item {
            PaddedSection(
                content = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
                    ) {
                        repeat(5) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(windowSizeConstant.baseSize)
                                    .clip(CustomShape.mediumShape())
                                    .shimmerEffect()
                            )
                        }
                    }
                })
        }

        item {
            PaddedSection(
                content = {
                    Row(horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.baseNormalVerticalPadding)) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .size(windowSizeConstant.customSpacerSmall)
                                    .clip(CustomShape.mediumShape())
                                    .shimmerEffect()
                            )
                        }
                    }
                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseNormalVerticalPadding))
                    Row(horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.baseNormalVerticalPadding)) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .width(windowSizeConstant.customSpacerMedium)
                                    .height(spacing.custom70)
                                    .clip(CustomShape.mediumShape())
                                    .shimmerEffect()
                            )
                        }
                    }
                })
        }

        item {
            PaddedSection(
                content = {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(spacing.custom65)
                                .clip(CustomShape.mediumShape())
                                .shimmerEffect()
                        )
                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseNormalVerticalPadding))
                    }
                })
        }

        item {
            PaddedSection(
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CustomShape.mediumShape())
                            .shimmerEffect()
                            .padding(windowSizeConstant.basePadding)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(windowSizeConstant.basePadding)
                                    .clip(CustomShape.mediumShape())
                                    .shimmerEffect()
                            )
                            Spacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))
                        }
                    }
                })
        }

        item {
            PaddedSection(
                content = {
                    Row(horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.baseNormalVerticalPadding)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(spacing.custom36)
                                    .clip(CustomShape.mediumShape())
                                    .shimmerEffect()
                            )
                        }
                    }
                })
        }

        item {
            PaddedSection(
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(spacing.custom48)
                            .clip(CustomShape.mediumShape())
                            .shimmerEffect()
                    )
                })
        }
    }
}

/**
 * ProductShimmerList - Shimmer placeholders for a product summary and a list of item cards.
 */
@Composable
/**
 * ProductShimmerList
 *
 */
fun ProductShimmerList() {

    PaddedSection(
        content = {
            ProductSummaryCardShimmer()
        })

    CustomSpacer()

    CustomLazyColumn {
        items(10) {
            CustomItemCardShimmer()
        }
    }
}

/**
 * ProductShimmer - Shimmer placeholders for a product carousel and a list of product cards.
 */
@Composable
/**
 * ProductShimmer
 *
 */
fun ProductShimmer() {
    CustomLazyColumn {
        item {
            ProductCarouselShimmer()
        }
        items(10) {
            ProductCardShimmerRow()
        }
    }
}

/**
 * ProductCardShimmerRow - Displays a row of shimmer placeholders for loading state
 *
 * @param itemCount Number of shimmer cards to display
 */
@Composable
fun ProductCardShimmerRow(
    itemCount: Int = 10
) {
    CustomLazyRow {
        items(itemCount) {
            ProductCardShimmer(
                modifier = Modifier,
                isSelected = false
            )
        }
    }
}

/**
 * ProductCardShimmerRow - Displays a row of shimmer placeholders for loading state
 *
 */
@Composable
/**
 * ListCardShimmer
 *
 */
fun ListCardShimmer() {
    val windowSizeConstant = LocalWindowSizeConstant.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = windowSizeConstant.basePadding,
                vertical = windowSizeConstant.normalVerticalPadding
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = windowSizeConstant.cardElevationPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeConstant.basePadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side content (icon and text)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon placeholder
                Box(
                    modifier = Modifier
                        .size(windowSizeConstant.baseSize)
                        .clip(CustomShape.extraLargeShape())
                        .shimmerEffect()
                )

                CustomSpacer(modifier = Modifier.width(windowSizeConstant.baseNormalVerticalPadding))

                // Text placeholder
                Column(
                    verticalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(windowSizeConstant.basePadding)
                            .clip(CustomShape.mediumShape())
                            .shimmerEffect()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(windowSizeConstant.baseNormalVerticalPadding)
                            .clip(CustomShape.mediumShape())
                            .shimmerEffect()
                    )
                }
            }

            // Right side actions (edit and delete buttons)
            Row {
                // Edit button placeholder
                Box(
                    modifier = Modifier
                        .size(windowSizeConstant.baseSize)
                        .clip(CustomShape.extraLargeShape())
                        .shimmerEffect()
                )

                CustomSpacer(modifier = Modifier.width(windowSizeConstant.normalVerticalPadding))

                // Delete button placeholder
                Box(
                    modifier = Modifier
                        .size(windowSizeConstant.baseSize)
                        .clip(CustomShape.extraLargeShape())
                        .shimmerEffect()
                )
            }
        }
    }
}

@Composable
/**
 * CustomListCardShimmer
 *
 * @param count The count parameter
 */
fun CustomListCardShimmer(count: Int = 10) {
        repeat(count) {
            ListCardShimmer()
        }
}

/**
 * MiniAnalyticsCardShimmer - Shimmer placeholder for a mini analytics card.
 *
 * @param modifier Modifier to apply to the card.
 */
@Composable
fun MiniAnalyticsCardShimmer(
    modifier: Modifier = Modifier
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = false, onClick = {}),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = windowSizeClass.cardElevationPadding)
    ) {
        Column(
            modifier = Modifier.padding(windowSizeClass.basePadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon placeholder
            Box(
                modifier = Modifier
                    .size(customSpacing.custom50)
                    .clip(CustomShape.extraLargeShape())
                    .shimmerEffect()
            )

            CustomSpacer()

            // Count number placeholder
            Box(
                modifier = Modifier
                    .width(customSpacing.custom60) // ~60.dp
                    .height(customSpacing.custom50)
                    .clip(CustomShape.mediumShape())
                    .shimmerEffect()
            )

            CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

            // Title placeholder
            Box(
                modifier = Modifier
                    .width(windowSizeClass.customSpacerMedium)
                    .height(windowSizeClass.baseNormalVerticalPadding) // ~12.dp
                    .clip(CustomShape.mediumShape())
                    .shimmerEffect()
            )
        }
    }
}

/**
 * AnalyticsCardShimmer - Shimmer placeholder for a full analytics card with chart.
 *
 * @param modifier Modifier to apply to the card.
 */
@Composable
fun AnalyticsCardShimmer(
    modifier: Modifier = Modifier
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = customSpacing.custom32,
                vertical = windowSizeClass.normalVerticalPadding
            )
            .clickable(enabled = false, onClick = {}),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = windowSizeClass.smallVerticalPadding)
    ) {
        Column(
            modifier = Modifier.padding(windowSizeClass.basePadding)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding)
                ) {
                    // Icon placeholder
                    Box(
                        modifier = Modifier
                            .size(customSpacing.custom50)
                            .clip(CustomShape.extraLargeShape())
                            .shimmerEffect()
                    )

                    // Title placeholder
                    Box(
                        modifier = Modifier
                            .width(windowSizeClass.customSpacerMedium) // ~80.dp
                            .height(customSpacing.custom50) //50.dp
                            .clip(CustomShape.mediumShape())
                            .shimmerEffect()
                    )
                }

                // Chevron placeholder
                Box(
                    modifier = Modifier
                        .size(windowSizeClass.basePadding)
                        .clip(CustomShape.extraLargeShape())
                        .shimmerEffect()
                )
            }

            CustomSpacer()

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // Main count placeholder
                    Box(
                        modifier = Modifier
                            .width(windowSizeClass.customSpacerLarge) // ~100.dp
                            .height(customSpacing.custom50) //50.dp
                            .clip(CustomShape.mediumShape())
                            .shimmerEffect()
                    )

                    CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

                    // "Total" label placeholder
                    Box(
                        modifier = Modifier
                            .width(windowSizeClass.customSpacerMedium) // ~80.dp
                            .height(windowSizeClass.baseNormalVerticalPadding)
                            .clip(CustomShape.mediumShape())
                            .shimmerEffect()
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // Trend row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(windowSizeClass.smallVerticalPadding)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(windowSizeClass.basePadding)
                                .clip(CustomShape.extraLargeShape())
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier
                                .width(windowSizeClass.customSpacerMedium) // ~80.dp
                                .height(windowSizeClass.normalVerticalPadding)
                                .clip(CustomShape.mediumShape())
                                .shimmerEffect()
                        )
                    }

                    CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

                    // Trend text placeholder
                    Box(
                        modifier = Modifier
                            .width(customSpacing.custom60) // ~60.dp
                            .height(windowSizeClass.baseNormalVerticalPadding)
                            .clip(CustomShape.mediumShape())
                            .shimmerEffect()
                    )
                }
            }

            CustomSpacer()

            // Chart placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(windowSizeClass.customSpacerMedium)
                    .clip(CustomShape.mediumShape())
                    .shimmerEffect()
            )
        }
    }
}

/**
 * AnalyticsShimmerGrid - Comprehensive shimmer loading state for the analytics dashboard.
 * Uses a 2x2 grid layout for mini cards and includes summary row.
 */
@Composable
/**
 * AnalyticsShimmerGrid
 *
 */
fun AnalyticsShimmerGrid() {
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomLazyColumn {

        // Grid Row 1 shimmer
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = customSpacing.custom32),
                horizontalArrangement = Arrangement.spacedBy(windowSizeClass.basePadding)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MiniAnalyticsCardShimmer()
                }

                Box(modifier = Modifier.weight(1f)) {
                    MiniAnalyticsCardShimmer()
                }
            }
        }

        // Grid Row 2 shimmer
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = customSpacing.custom32),
                horizontalArrangement = Arrangement.spacedBy(windowSizeClass.basePadding)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MiniAnalyticsCardShimmer()
                }

                Box(modifier = Modifier.weight(1f)) {
                    MiniAnalyticsCardShimmer()
                }
            }
        }

        // Grid Row 3 shimmer
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = customSpacing.custom32),
                horizontalArrangement = Arrangement.spacedBy(windowSizeClass.basePadding)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MiniAnalyticsCardShimmer()
                }

                Box(modifier = Modifier.weight(1f)) {
                    MiniAnalyticsCardShimmer()
                }
            }
        }

        // Grid Row 4 shimmer
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = customSpacing.custom32),
                horizontalArrangement = Arrangement.spacedBy(windowSizeClass.basePadding)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MiniAnalyticsCardShimmer()
                }

                Box(modifier = Modifier.weight(1f)) {
                    MiniAnalyticsCardShimmer()
                }
            }
        }

        // Full analytics card shimmer (optional - for larger cards)
        item {
            AnalyticsCardShimmer()
        }

        item {
            CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerLarge))
        }
    }
}

