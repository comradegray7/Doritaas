package com.example.myapp.view.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.utils.CustomShape

/**
 * CustomSurfaceContainer - A versatile container for badges, labels, or small interactive elements.
 *
 * This component provides a styled surface with support for icons, text (string or resource),
 * and custom borders. It's often used for discount badges, status indicators, or category tags.
 *
 * @param modifier The modifier to be applied to the inner layout
 * @param color The background color of the surface
 * @param borderColor Optional color for the border
 * @param icon Optional icon to display before the text
 * @param contentDescription Accessibility description for the icon
 * @param text Optional string resource ID for the label
 * @param textStr Optional direct string value for the label
 * @param width Thickness of the border (required if borderColor is set)
 * @param fontWeight Font weight for the label text. Defaults to [FontWeight.Bold].
 * @param onClick Callback called when the surface is clicked
 * @param iconSize Size of the leading icon. Defaults to 10dp.
 * @param textStyle Optional custom text style override for the label
 * @param shape The shape of the surface. Defaults to rounded corners (6dp).
 */
@Composable
fun CustomSurfaceContainer(
    modifier: Modifier = Modifier,
    color: Color,
    borderColor: Color? = null,
    icon: ImageVector? = null,
    tint: Color? = MaterialTheme.colorScheme.onPrimary,
    contentDescription: String? = null,
    @StringRes text: Int? = null,
    textStr: String? = null,
    textColor: Color? = MaterialTheme.colorScheme.onPrimary,
    width: Dp? = null,
    fontWeight: FontWeight? = FontWeight.Bold,
    onClick: () -> Unit = {},
    iconSize: Dp? = null,
    textStyle: TextStyle? = null,
    shape: Shape? = null
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val size = iconSize ?: windowSizeConstant.baseIconSize
    val cardShape = shape ?: CustomShape.mediumShape()

    shape?.let { it ->
        Surface(
            onClick = onClick,
            color = color,
            shape = cardShape,
            border = if (borderColor != null && width != null) {
                BorderStroke(width = width, color = borderColor)
            } else {
                null
            }
        ) {
            Row(
                modifier = modifier.then(
                    Modifier.padding(
                        horizontal = windowSizeConstant.baseVerticalPadding,
                        vertical = windowSizeConstant.smallVerticalPadding
                    )
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding)
            ) {
                icon?.let { it ->
                    CustomIcon(
                        icon = it,
                        contentDescription = contentDescription,
                        iconSize = size,
                        tint = tint ?: MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                (textStr ?: text?.let { stringResource(it) })?.let { textContent ->
                    Text(
                        text = textContent,
                        style = textStyle ?: windowSizeConstant.labelTextStyle,
                        color = textColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = fontWeight ?: FontWeight.Bold

                    )
                }
            }
        }
    }
}
