package com.example.myapp.view.components.custom.buttons

import androidx.compose.foundation.background
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CustomShape

/**
 * ButtonIconComposable - A composable that renders an icon button with a circular background.
 *
 * This component supports different icon types (Vector, Resource, None) via [ButtonIcon]
 * and provides a consistent look for icon-only buttons throughout the app.
 *
 * @param modifier Modifier to be applied to the IconButton.
 * @param buttonIcon The icon to display (Vector or Resource).
 * @param onClick Callback invoked when the button is clicked.
 * @param contentDescription Accessibility description for the icon.
 * @param tint Color to tint the icon.
 * @param enabled Whether the button is enabled.
 * @param backgroundColor Background color of the circular container.
 */

@Composable
fun ButtonIconComposable(
    modifier: Modifier = Modifier,
    buttonIcon: ButtonIcon,
    onClick: () -> Unit = {},
    contentDescription: String?,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showBgColor: Boolean = true,
    iconSize: Dp? = null
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    val modifier = modifier.background(
        color = if (showBgColor) backgroundColor else colors.transparent,
        shape = CustomShape.circleShape()
    )

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {

        when (buttonIcon) {
            is ButtonIcon.Vector -> {
                CustomIcon(
                    icon = buttonIcon.imageVector,
                    contentDescription = contentDescription,
                    tint = tint,
                    iconSize = iconSize ?: windowSizeClass.iconSize
                )
            }

            is ButtonIcon.Resource -> {
                CustomIcon(
                    painter = painterResource(id = buttonIcon.drawableRes),
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = modifier,
                    iconSize = iconSize ?: windowSizeClass.iconSize
                )
            }

            is ButtonIcon.None -> {}
        }
    }
}