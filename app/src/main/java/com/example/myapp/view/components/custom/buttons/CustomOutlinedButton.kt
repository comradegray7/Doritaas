package com.example.myapp.view.components.custom.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CustomShape

/**
 * CustomOutlinedButton - A customizable outlined button composable.
 *
 * Wraps [OutlinedButton] with support for optional icons, adaptive sizing,
 * and consistent styling.
 *
 * @param modifier Modifier to be applied to the button.
 * @param useSmallWidth If true, applies a fixed small width.
 * @param label String resource ID for the button label.
 * @param enabled Whether the button is enabled.
 * @param onClick Callback invoked when the button is clicked.
 * @param shape Shape of the button (defaults to rounded corners).
 * @param icon Optional icon to display.
 * @param contentDescription Accessibility description.
 * @param tintColor Color to tint the icon.
 * @param iconModifier Modifier to be applied to the icon.
 */
@Composable
fun CustomOutlinedButton(
    modifier: Modifier = Modifier,
    useSmallWidth: Boolean = false,
    @DrawableRes label: Int? = null,
    labelStr: String? = "",
    enabled: Boolean = true,
    onClick: () -> Unit,
    shape: Shape = CustomShape.mediumShape(),
    icon: ButtonIcon? = null,
    contentDescription: String = "",
    tintColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconModifier: Modifier = Modifier,
    useStringResourceLabel: Boolean = (label != 0 && labelStr?.isBlank() == true), // Derived: true if label is valid and strLabel is not
    useDirectStringLabel: Boolean = labelStr?.isNotBlank() == true,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors()
) {

    val windowSizeConstant = LocalWindowSizeConstant.current

    OutlinedButton(
        onClick, shape = shape, enabled = enabled,
        modifier = if (useSmallWidth) Modifier.width(windowSizeConstant.smallButtonWidth) else modifier,
        colors = colors
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Show icon if provided
            icon?.let { buttonIcon ->
                when (buttonIcon) {
                    is ButtonIcon.Resource -> {
                        CustomIcon(
                            painter = painterResource(id = buttonIcon.drawableRes),
                            contentDescription = contentDescription,
                            tint = tintColor,
                            modifier = iconModifier.size(windowSizeConstant.iconSize)
                        )
                    }

                    is ButtonIcon.Vector -> {
                        CustomIcon(
                            icon = buttonIcon.imageVector,
                            contentDescription = contentDescription,
                            tint = tintColor,
                            modifier = iconModifier.size(windowSizeConstant.iconSize)
                        )
                    }

                    is ButtonIcon.None -> {}

                }
                CustomSpacer(modifier = Modifier.width(windowSizeConstant.normalVerticalPadding))
            }

            when {
                useDirectStringLabel -> { // If strLabel is provided and not blank
                    if (labelStr != null) {
                        Text(
                            text = labelStr,
                            style = windowSizeConstant.bodyTextStyle
                        )
                    }
                }

                useStringResourceLabel -> { // Else, if label is a valid resource ID
                    label?.let {
                        Text(
                            text = stringResource(it), // This is now safe because label != 0 is checked by useStringResourceLabel
                            style = windowSizeConstant.bodyTextStyle
                        )
                    }
                }
            }
        }
    }
}
