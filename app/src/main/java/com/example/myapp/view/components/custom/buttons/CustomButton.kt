package com.example.myapp.view.components.custom.buttons

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
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
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomCircularProgressIndicator
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CustomShape

/**
 * CustomButton - Composable function for creating adaptive, customizable buttons.
 *
 * This composable creates a Material Design 3 button with adaptive sizing,
 * optional icons, and flexible content. It supports both string resources
 * and custom content, making it suitable for various use cases throughout the app.
 *
 * @param modifier Optional modifier to apply to the button
 * @param iconModifier Optional modifier to apply to the icon
 * @param label String resource ID for the button text
 * @param icon Optional icon to display alongside the text
 * @param tintColor Color for the icon tint (defaults to outline color)
 * @param onClick Callback function for button clicks
 * @param contentDescription Accessibility description for the button
 * @param enabled Whether the button is enabled (default: true)
 * @param useSmallWidth Whether to use a small fixed width (default: false)
 * @param content Optional custom content to replace the default text+icon layout
 *
 * Usage:
 * ```
 * CustomButton(
 *     label = R.string.submit,
 *     onClick = { /* handle click */ }
 * )
 *
 * CustomButton(
 *     label = R.string.save,
 *     icon = ButtonIcon.Vector(Icons.Default.Save),
 *     onClick = { /* handle save */ }
 * )
 * ```
 */
@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    @StringRes label: Int? = null,
    icon: ButtonIcon? = null,
    tintColor: Color = colors.white, // Default to outline color for icons
    onClick: () -> Unit,
    contentDescription: String = "",
    enabled: Boolean = true,
    useSmallWidth: Boolean = false,
    strLabel: String = "",
    content: (@Composable RowScope.() -> Unit)? = null,
    isLoading: Boolean = false,
    useStringResourceLabel: Boolean = (label != 0 && strLabel.isBlank()), // Derived: true if label is valid and strLabel is not
    useDirectStringLabel: Boolean = strLabel.isNotBlank(),
    buttonColors: ButtonColors? = null,
    shape: Shape = CustomShape.mediumShape()
) {

    // Get the current window size constants for adaptive behavior
    val windowSizeConstant = LocalWindowSizeConstant.current

    Button(
        colors = buttonColors ?: ButtonDefaults.buttonColors (
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        enabled = enabled,
        onClick = onClick,
        shape = shape, // Use medium corner radius
        modifier = if (useSmallWidth)
            modifier.width(windowSizeConstant.smallButtonWidth) // Fixed small width
        else
            modifier.then(windowSizeConstant.adaptiveFormWidthModifier) // Adaptive width and height
    ) {

        if (isLoading) {
            CustomCircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
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
                                    modifier = iconModifier // Adaptive icon size
                                )
                            }

                            is ButtonIcon.Vector -> {
                                // Display vector icon
                                CustomIcon(
                                    icon = buttonIcon.imageVector,
                                    contentDescription = contentDescription,
                                    tint = tintColor,
                                    modifier = iconModifier // Adaptive icon size
                                )
                            }

                            is ButtonIcon.None -> {}
                        }
                        // Add spacing between icon and text
                        CustomSpacer(modifier = Modifier.width(windowSizeConstant.normalVerticalPadding))
                    }

                    when {
                        useDirectStringLabel -> { // If strLabel is provided and not blank
                            Text(
                                text = strLabel,
                                fontWeight = FontWeight.SemiBold,
                                style = windowSizeConstant.bodyTextStyle,
                                modifier = textModifier
                            )
                        }

                        useStringResourceLabel -> { // Else, if label is a valid resource ID
                            label?.let {
                                Text(
                                    text = stringResource(it), // This is now safe because label != 0 is checked by useStringResourceLabel
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = textModifier
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

