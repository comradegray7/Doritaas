package com.example.myapp.view.components

import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.utils.ButtonIcon

/**
 * CustomEmptyState - Reusable empty state component
 *
 * Displays when a list or collection is empty, with optional icon, title, subtitle,
 * and action button. Provides consistent empty state UX across the app.
 *
 * ## Features
 * - **Customizable Icon**: Optional leading icon with configurable size
 * - **Title & Subtitle**: Supports both string and string resource
 * - **Optional Action Button**: Can include button with custom label and icon
 * - **Centered Layout**: Vertically and horizontally centered content
 * - **Scrollable**: Supports scrolling for small screens
 *
 * ## Common Use Cases
 * - Empty cart: "Your cart is empty" with "Start Shopping" button
 * - No favorites: "No favorites yet" with "Browse Products" button
 * - No search results: "No results found" without button
 * - No orders: "No orders yet" with "Shop Now" button
 * - Error states: "Something went wrong" with "Retry" button
 *
 * ## Usage Example
 * ```kotlin
 * CustomEmptyState(
 *     leadingIcon = Icons.Filled.ShoppingCart,
 *     titleStr = "Your cart is empty",
 *     subTitle = R.string.start_shopping_message,
 *     showBtn = true,
 *     btnLabel = R.string.start_shopping,
 *     btnIcon = Icons.Filled.ShoppingBag,
 *     onBtnClick = { navigateToShop() }
 * )
 * ```
 *
 * @param showBtn Whether to show the action button (default: true)
 * @param iconSize Size of the leading icon (default: 48dp)
 * @param btnLabel String resource for button label (required if showBtn is true)
 * @param titleStr String title text (alternative to title resource)
 * @param title String resource for title
 * @param subTitle String resource for subtitle
 * @param leadingIcon Icon to display above title
 * @param btnIcon Icon for the action button
 * @param onBtnClick Callback when action button is clicked
 * @param scrollState Scroll state for the container
 *
 * @see HeadlineWidget for title/subtitle display
 * @see CustomButton for the action button
 */
@Composable
fun CustomEmptyState(
    modifier: Modifier = Modifier,
    showBtn: Boolean = true,
    iconSize: Dp? = null,
    @StringRes btnLabel: Int? = null,
    titleStr: String? = "",
    @StringRes title: Int? = null,
    @StringRes subTitle: Int? = null,
    leadingIcon: ImageVector? = null,
    btnIcon: ImageVector? = null,
    onBtnClick: () -> Unit = {},
    scrollState: ScrollState = rememberScrollState(),
    enableScroll: Boolean = true
) {
    val windowSizeAppConstant = LocalWindowSizeConstant.current
    val size = iconSize ?: windowSizeAppConstant.largeIconSize

    PaddedSection(content = {
        Column(
            modifier = modifier.then(
                Modifier
                    .fillMaxSize()
                    .then(if (enableScroll) Modifier.verticalScroll(scrollState) else Modifier)
                    .padding(windowSizeAppConstant.contentVerticalPadding)
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Empty cart icon
            if (leadingIcon != null) {
                CustomIcon(
                    icon = leadingIcon,
                    contentDescription = null,
                    iconSize = size,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            CustomSpacer(modifier = Modifier.height(windowSizeAppConstant.baseVerticalPadding))

            HeadlineWidget(
                middleTextStr = titleStr,
                middleText = title,
                subMiddleText = subTitle
            )

            CustomSpacer(modifier = Modifier.height(windowSizeAppConstant.baseVerticalPadding))

            // action state button - ONLY show if 'showBtn' is true AND a label is provided
            if (showBtn && btnLabel != null) {
                CustomButton(
                    onClick = { onBtnClick() },
                    label = btnLabel,
                    // The btnIcon is passed here. If btnIcon is null, CustomButton should handle it internally.
                    icon = if (btnIcon != null) {
                        // If btnIcon is provided, use ButtonIcon.Vector(ImageVector)
                        ButtonIcon.Vector(btnIcon)
                    } else {
                        // If btnIcon is null, use the empty state
                        ButtonIcon.None
                    }
                )
            }
        }
    })

}


