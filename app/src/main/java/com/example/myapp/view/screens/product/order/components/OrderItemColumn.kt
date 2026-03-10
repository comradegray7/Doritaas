package com.example.myapp.view.screens.product.order.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomIcon

/**
 * DetailRow - Displays a labeled value with an icon.
 *
 * A reusable row component that shows an icon, a label, and a value.
 * Used for displaying structured data like order details.
 *
 * @param icon The icon to display at the start of the row.
 * @param label The label text describing the value.
 * @param value The main value text to display.
 * @param valueStyle The text style for the value. Defaults to bodyMedium.
 * @param valueColor The color of the value text. Defaults to onSurface.
 * @param fontWeight The font weight of the value text. Defaults to Normal.
 */
@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueStyle: TextStyle? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
        ) {
            CustomIcon(
                icon = icon,
                contentDescription = null,
                iconSize = windowSizeConstant.basePadding,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = label,
                style = windowSizeConstant.bodyTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = value,
            style = valueStyle ?: windowSizeConstant.bodyTextStyle,
            color = valueColor,
            fontWeight = fontWeight
        )
    }
}