package com.example.myapp.view.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.myapp.R
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.formatPrice

/**
 * CustomBottomSection - Composable function for creating bottom sections with total and action button.
 * 
 * This composable creates a bottom section typically used in shopping carts or checkout screens.
 * It displays a total amount and an action button (like "Checkout" or "Add to Cart").
 * The layout is responsive and uses adaptive styling based on window size.
 * 
 * @param actionLabel String resource ID for the action button text
 * @param icon Optional icon for the action button
 * @param total Optional total amount to display (defaults to 0.0)
 * @param onClick Callback function for the action button click
 * @param enabled Whether the action button is clickable
 *
 * ## Usage:
 * ```kotlin
 * CustomBottomSection(
 *     actionLabel = R.string.checkout,
 *     total = 99.99,
 *     onClick = { /* handle checkout */ }
 * )
 * ```
 */
@Composable
fun CustomBottomSection(
    modifier: Modifier = Modifier,
    @StringRes actionLabel: Int,
    icon: ButtonIcon? = null,
    total: Double? = 0.0,
    onClick: () -> Unit,
    enabled: Boolean = true
) {

    val windowSizeClass = LocalWindowSizeConstant.current

    // Row displaying "Total" label and total amount
    Row(
        modifier = modifier.then(Modifier.fillMaxWidth()), // Take full available width
        horizontalArrangement = Arrangement.SpaceBetween, // Space items apart
        verticalAlignment = Alignment.CenterVertically // Center items vertically
    ) {
        // "Total" label
        Text(
            text = stringResource(R.string.total),
            style = windowSizeClass.bodyTextStyle,// Medium title style
            color = MaterialTheme.colorScheme.onSurfaceVariant // Surface variant color
        )
        
        // Total amount with currency formatting
        total?.let {
            Text(
                text = formatPrice(it), // Format as USD currency
                style = windowSizeClass.bodyTextStyle,// Medium title style
                fontWeight = FontWeight.Bold, // Bold weight for emphasis
                color = MaterialTheme.colorScheme.primary // Primary color for emphasis
            )
        }
    }

    // adaptive spacing
    CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

    // Action button (e.g., "Checkout", "Add to Cart")
    CustomButton(
        label = actionLabel,
        icon = icon,
        enabled = enabled,
        onClick = { onClick() } // Handle button click
    )
}
