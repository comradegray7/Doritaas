package com.example.myapp.view.components.custom.buttons

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import com.example.myapp.view.components.CustomCircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CustomShape

/**
 * CustomElevatedButton - A customizable elevated button composable.
 *
 * Wraps [ElevatedButton] with consistent styling, adaptive sizing, loading state support,
 * and optional icon integration.
 *
 * @param modifier Modifier to be applied to the button.
 * @param iconModifier Modifier to be applied to the icon.
 * @param label String resource ID for the button label.
 * @param size Optional override for icon size.
 * @param icon Optional icon to display.
 * @param tintColor Color to tint the icon.
 * @param onClick Callback invoked when the button is clicked.
 * @param contentDescription Accessibility description.
 * @param enabled Whether the button is enabled.
 * @param width Optional specific width for the button.
 * @param content Optional custom content (overrides default label/icon).
 * @param isLoading Whether to show a loading spinner.
 * @param buttonColor Optional override for the button's background color.
 */
@Composable
fun CustomElevatedButton(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    @StringRes label: Int,
    size: Dp? = null,
    icon: ButtonIcon? = null,
    tintColor: Color = MaterialTheme.colorScheme.scrim,
    onClick: () -> Unit,
    contentDescription: String = "",
    enabled: Boolean = true,
    width: Dp? = null,
    shape: Shape = CustomShape.mediumShape(),
    content: (@Composable RowScope.() -> Unit)? = null,
    isLoading: Boolean = false,
    buttonColor: Color? = null
) {

    // Get the current window size constants for adaptive behavior
    val windowSizeConstant = LocalWindowSizeConstant.current

    width?.let {
        ElevatedButton(
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor ?: MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledContentColor = MaterialTheme.colorScheme.onSurface
            ), // Use default Material Design 3 button colors
            enabled = enabled,
            onClick = onClick,
            shape = shape, // Use medium corner radius
            modifier = modifier.width(width = it) // Adaptive width and height
        ) {

            if (isLoading) {
                CustomCircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = windowSizeConstant.cardElevationPadding,
                    modifier = Modifier
                        .size(windowSizeConstant.baseSize),
                    trackColor = Color.Transparent
                )
            } else {
                if (content != null) {
                    // Use custom content if provided
                    content()
                } else {
                    // Default layout with icon and text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Display icon if provided
                        icon?.let { buttonIcon ->
                            when (buttonIcon) {
                                is ButtonIcon.Resource -> {
                                    // Display drawable resource icon
                                    CustomIcon(
                                        painter = painterResource(id = buttonIcon.drawableRes),
                                        contentDescription = contentDescription,
                                        tint = tintColor,
                                        modifier = iconModifier,
                                        iconSize = size
                                            ?: windowSizeConstant.iconSize // Adaptive icon size
                                    )
                                }

                                is ButtonIcon.Vector -> {
                                    // Display vector icon
                                    CustomIcon(
                                        icon = buttonIcon.imageVector,
                                        contentDescription = contentDescription,
                                        tint = tintColor,
                                        modifier = iconModifier,
                                        iconSize = size
                                            ?: windowSizeConstant.iconSize  // Adaptive icon size
                                    )
                                }

                                is ButtonIcon.None -> {}

                            }
                            // Add spacing between icon and text
                            CustomSpacer(modifier = Modifier.width(windowSizeConstant.baseVerticalPadding))
                        }

                        // Display button text
                        Text(
                            text = stringResource(label),
                            fontWeight = FontWeight.SemiBold // Semi-bold text weight
                        )
                    }
                }
            }
        }
    }
}
