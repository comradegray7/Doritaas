package com.example.myapp.view.screens.product.product_rating_and_reviews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.constant
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomSpacer
import java.util.Locale

/**
 * ProductRating - Composable function for displaying star ratings
 *
 * This composable displays a row of stars (full, half, or empty) based on the rating value.
 * It also shows the numeric rating value next to the stars.
 *
 * @param modifier Modifier to be applied to the row
 * @param rating The rating value (e.g., 4.5)
 * @param maxRating The maximum number of stars (default: 5)
 * @param onRatingClick Callback when the rating component is clicked.
 */

@Composable
fun ProductRating(
    modifier: Modifier = Modifier,
    rating: Float = 0f,
    maxRating: Int = constant.five,
    onRatingClick: () -> Unit = {},
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(windowSizeClass.smallVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxRating) { index ->
            val starRating = when {
                index < rating.toInt() -> 1f // Full star
                index < rating && rating % 1 != 0f -> rating % 1 // Partial star
                else -> 0f // Empty star
            }

            CustomIcon(
                icon = when {
                    starRating >= 1f -> Icons.Filled.Star
                    starRating > 0f -> Icons.AutoMirrored.Filled.StarHalf
                    else -> Icons.Outlined.StarBorder
                },
                contentDescription = "rating icon",
                iconSize = windowSizeClass.basePadding,
                tint = if (starRating > 0f) colors.customColor6 else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable {

                    onRatingClick()
                }
            )
        }

        CustomSpacer(modifier = Modifier.width(windowSizeClass.smallVerticalPadding))

        Text(
            text = String.format(Locale.US, "%.1f", rating),
            style = windowSizeClass.labelTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable { onRatingClick() }
        )
    }
}