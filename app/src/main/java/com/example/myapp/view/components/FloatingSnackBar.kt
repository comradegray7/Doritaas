package com.example.myapp.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.utils.ButtonIcon

/**
 * FloatingCustomSnackBar - Animated floating snack bar for user feedback
 *
 * Displays temporary messages with animations, icons, and optional actions.
 * Supports both success and error states with appropriate styling.
 *
 * ## Features
 * - **Animated Entry/Exit**: Smooth slide and fade animations
 * - **State-Based Styling**: Different colors for success/error
 * - **Contextual Icons**: CheckCircle for success, Error for errors
 * - **Optional Action**: Can include action button with callback
 * - **Dismissible**: Close button to manually dismiss
 * - **Auto-Dismiss**: Automatically hides after duration
 *
 * ## Visual States
 * - **Success**: Primary container color with checkmark icon
 * - **Error**: Error container color with error icon
 *
 * ## Usage Example
 * ```kotlin
 * FloatingCustomSnackBar(
 *     snackBarData = SnackBarData(
 *         message = "Product added to cart",
 *         isError = false,
 *         actionLabel = "View Cart",
 *         onActionClick = { navigateToCart() }
 *     ),
 *     visible = showSnackBar,
 *     onDismiss = { showSnackBar = false }
 * )
 * ```
 *
 * @param modifier Modifier for positioning (typically with navigationBarsPadding)
 * @param snackBarData Data object containing message, error state, and optional action
 * @param visible Whether the snackbar should be visible
 * @param onDismiss Callback when snackbar is dismissed
 *
 * @see SnackBarData for data structure
 */

@Composable
fun FloatingCustomSnackBar(
    modifier: Modifier = Modifier,
    snackBarData: SnackBarData,
    visible: Boolean,
    onDismiss: () -> Unit = {},
) {
    val windowSizeAppConstants = LocalWindowSizeConstant.current

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Card(
            modifier = modifier
                .padding(windowSizeAppConstants.basePadding)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = windowSizeAppConstants.normalVerticalPadding),
            colors = CardDefaults.cardColors(
                containerColor = if (snackBarData.isError)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(windowSizeAppConstants.basePadding),
            ) {
                // First Row: Icon and Message
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CustomIcon(
                        icon = if (snackBarData.isError) Icons.Filled.Error else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (snackBarData.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    // Use padding for spacing instead of Spacer
                    Text(
                        text = snackBarData.message,
                        style = windowSizeAppConstants.labelTextStyle,
                        color = if (snackBarData.isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = windowSizeAppConstants.basePadding)
                    )
                }

                // Only add the second row if action or close is present
                if (snackBarData.actionLabel != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = windowSizeAppConstants.basePadding),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = snackBarData.actionLabel.uppercase(),
                            modifier = Modifier
                                .clickable {
                                    snackBarData.onActionClick?.invoke()
                                    onDismiss()
                                }
                                .padding(start = windowSizeAppConstants.basePadding),
                            style = windowSizeAppConstants.labelTextStyle,
                            fontWeight = FontWeight.Bold,
                            color = if (snackBarData.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )

                        ButtonIconComposable(
                            onClick = onDismiss,
                            buttonIcon = ButtonIcon.Vector(Icons.Filled.Close),
                            contentDescription = "Dismiss",
                            showBgColor = false,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
