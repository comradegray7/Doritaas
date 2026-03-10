package com.example.myapp.view.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.myapp.R
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.utils.formatPrice

/**
 * ProductSummaryCard - Displays a summary of product information with customizable content.
 *
 * This composable creates a card that shows product summary information including:
 * - Item count
 * - Leading icon (optional)
 * - Middle content (optional)
 * - Savings amount (if applicable)
 * - Trailing icon (optional)
 *
 * The card features a radial gradient background and adaptive sizing based on
 * the current window size. It's commonly used in shopping carts and product
 * summary sections to display key information at a glance.
 *
 * @param itemCount Number of items to display
 * @param leadingIcon Composable for the leading icon (left side)
 * @param trailingIcon Composable for the trailing icon (right side)
 * @param middleComposable Composable for middle content area
 * @param showLeading Whether to show the leading icon
 * @param showItems Whether to show the item count text
 * @param showMiddle Whether to show the middle content area
 * @param showTrailingIcon Whether to show the trailing icon
 * @param savings Amount of money saved (displayed if greater than 0)
 */
@Composable
fun ProductSummaryCard(
    itemCount: Int,
    leadingIcon: @Composable (() -> Unit) = {},
    trailingIcon: @Composable (() -> Unit) = {},
    middleComposable: @Composable (() -> Unit) = {},
    showLeading: Boolean = false,
    showItems: Boolean = true,
    showMiddle: Boolean = false,
    showTrailingIcon: Boolean = true,
    savings: Double
) {

    val windowSizeConstant = LocalWindowSizeConstant.current

    // Create radial gradient background for visual appeal
    val radialGradient = Brush.radialGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        radius = 800f // Gradient radius for smooth color transition
    )
    
    // Main card container with adaptive width and elevation
    Card(
        modifier = windowSizeConstant.adaptiveWidthModifier,
        elevation = CardDefaults.cardElevation(defaultElevation = windowSizeConstant.normalVerticalPadding)
    ){
        // Content row with gradient background and proper spacing
        Row(
            modifier = Modifier 
                .fillMaxWidth()
                .background(radialGradient) // Apply radial gradient background
                .padding(windowSizeConstant.listCardPadding), // Adaptive padding
            horizontalArrangement = Arrangement.SpaceBetween, // Space content evenly
            verticalAlignment = Alignment.CenterVertically // Center content vertically
        ) {
            // Item count display (shown by default)
            if (showItems) {
                Text(
                    text = "$itemCount items",
                    style = windowSizeConstant.titleTextStyle,
                )
            }
            
            // Leading icon section (optional)
            if (showLeading) {
                Column(modifier = Modifier.size(windowSizeConstant.productSummaryImagePadding)){
                    leadingIcon() // Render the leading icon composable
                }
            }
            
            // Middle content section (optional)
            if (showMiddle) {
               Column(modifier =
                   Modifier.weight(1f, fill = false) // Take remaining space
                   .padding(horizontal = windowSizeConstant.listRightPadding) // Right padding
               ){
                   middleComposable() // Render the middle content composable
               }
            }
            
            // Savings display (only shown if savings > 0)
            if (savings > 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(windowSizeConstant.baseVerticalPadding)){
                // Savings label
                Text(
                    text = stringResource(R.string.savings),
                    style = windowSizeConstant.bodyTextStyle,
                 )
                    // Savings amount with currency formatting
                    Text(
                    text = formatPrice(savings), // Format as currency with 2 decimal places
                    style = windowSizeConstant.bodyTextStyle,
                        fontWeight = FontWeight.Bold, // Bold text for emphasis
                    color = MaterialTheme.colorScheme.onPrimaryContainer // Highlight color
                 )
                }
            }
            
            // Trailing icon section (shown by default)
            if (showTrailingIcon) {
                trailingIcon() // Render the trailing icon composable
            }
        }
    }
}

