package com.example.myapp.view.screens.product.product_rating_and_reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import coil3.ImageLoader
import com.example.myapp.R
import com.example.myapp.data.dataclass.Review
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomImageContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.utils.getTimeAgo

/**
 * UI for a single product review item.
 *
 * Displays the reviewer's avatar (or placeholder), name, verification badge, timestamp,
 * star rating, review text and a "Helpful" button. The composable manages local UI
 * state for whether the current user has marked the review as helpful and disables
 * the button once marked.
 *
 * @param review the [Review] model containing review data to display.
 * @param onMarkHelpful callback invoked when the "Helpful" button is pressed (only once).
 * @param modifier optional [Modifier] to be applied to the root Card.
 * @param imageLoader optional [ImageLoader] used by [CustomImageContainer] to load images.
 */
@Composable
fun ReviewItem(
    review: Review,
    onMarkHelpful: () -> Unit,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader? = null,
) {
    var hasMarkedHelpful by remember { mutableStateOf(false) }
    val windowSizeConstant = LocalWindowSizeConstant.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeConstant.basePadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
                ) {
                    // User profile image
                    if (review.userProfileImage.isNotEmpty()) {
                        CustomImageContainer(
                            data = review.userProfileImage,
                            contentDescription = "user profile image",
                            imageLoader = imageLoader,
                            placeholder = painterResource(R.drawable.image_placeholder),
                            error = painterResource(R.drawable.network_error),
                            shape = CircleShape,
                            modifier = Modifier.size(windowSizeConstant.customSpacerSmall),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        CustomIcon(
                             icon = Icons.Filled.AccountCircle,
                             contentDescription = "Account circle",
                             iconSize = windowSizeConstant.largeIconSize,
                             tint = MaterialTheme.colorScheme.onSurfaceVariant
                         )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                review.userName,
                                style = windowSizeConstant.bodyTextStyle,
                                fontWeight = FontWeight.Bold
                            )
                            if (review.verified) {
                                CustomSpacer(modifier = Modifier.width(windowSizeConstant.smallVerticalPadding))
                               CustomIcon(
                                    icon = Icons.Filled.CheckCircle,
                                    contentDescription = "Verified Purchase",
                                    iconSize = windowSizeConstant.basePadding,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        review.timestamp?.let { timestamp ->
                            Text(
                                getTimeAgo(timestamp),
                                style = windowSizeConstant.labelTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Star rating
                ProductRating(
                    rating = review.rating,
                    maxRating = 5
                )
            }

            if (review.review.isNotEmpty()) {
                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                Text(
                    review.review,
                    style = windowSizeConstant.bodyTextStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

            // Helpful button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding)
            ) {

                TextButton(
                    onClick = {
                        if (!hasMarkedHelpful) {
                            hasMarkedHelpful = true
                            onMarkHelpful()
                        }
                    },
                    enabled = !hasMarkedHelpful
                ) {
                    CustomIcon(
                        icon = if (hasMarkedHelpful) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = null,
                        tint = if (hasMarkedHelpful)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )

                    CustomSpacer(modifier = Modifier.width(windowSizeConstant.smallVerticalPadding))

                    Text(
                        if (hasMarkedHelpful) "Marked helpful (${review.helpful + 1})"
                        else "Helpful (${review.helpful})",
                        style = windowSizeConstant.bodyTextStyle
                    )
                }
            }
        }
    }
}

