package com.example.myapp.view.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.example.myapp.ui.theme.LocalWindowSizeConstant

/**
 * CustomIcon - A flexible icon component supporting both [ImageVector] and [Painter].
 *
 * This component abstracts the selection between vector and painter-based icons while applying
 * consistent sizing based on the current window size class or a provided override.
 *
 * @param modifier The modifier to be applied to the icon
 * @param icon The [ImageVector] to display
 * @param painter The [Painter] to display (used if icon is null)
 * @param contentDescription The description for accessibility
 * @param useIcon Calculated property to determine if it should use the vector icon
 * @param usePainter Calculated property to determine if it should use the painter icon
 */
@Composable
fun CustomIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    painter: Painter? = null,
    contentDescription: String? = null,
    iconSize: Dp? = null,
    tint: Color? = LocalContentColor.current,
    useIcon: Boolean = (icon != null && painter == null),
    usePainter: Boolean = painter != null
) {

    val windowSizeConstant = LocalWindowSizeConstant.current
    val size = iconSize ?: windowSizeConstant.iconSize

    when {
        useIcon -> { // If icon is provided and not blank
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = contentDescription,
                    tint = tint ?: LocalContentColor.current,
                    modifier = modifier.then(
                        Modifier.size(
                            size
                        )
                    )
                )
            }
        }

        usePainter -> { // Else, if painter is a valid resource ID
            if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = contentDescription,
                    tint = tint ?: LocalContentColor.current,
                    modifier = modifier.then(
                        Modifier.size(
                            size
                        )
                    )
                )
            }
        }
    }

}