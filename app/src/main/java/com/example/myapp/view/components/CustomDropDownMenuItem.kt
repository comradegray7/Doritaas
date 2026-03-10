package com.example.myapp.view.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * CustomDropDownMenuItem - A styled Material 3 DropdownMenuItem with custom text color.
 *
 * Provides a standardized way to define menu items within dropdown menus (like ExposedDropdownMenu)
 * with support for icons and custom styling.
 *
 * @param modifier The modifier to be applied to the menu item
 * @param text The content of the menu item (typically a Text composable)
 * @param onClick Callback called when the menu item is clicked
 * @param leadingIcon Optional composable to be displayed before the text
 * @param trailingIcon Optional composable to be displayed after the text
 * @param textColor The color for the text content. Defaults to [Color.Unspecified].
 */
@Composable
fun CustomDropDownMenuItem(
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    DropdownMenuItem(
        modifier = modifier,
        text = text,
        onClick = onClick,
        colors = MenuDefaults.itemColors(
            textColor = textColor
        ),
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon
    )
}