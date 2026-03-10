package com.example.myapp.view.admin.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapp.ui.theme.LocalWindowSizeConstant

/**
 * A wrapper component for [ColorPicker] specifically designed for product color selection.
 * It provides both single and multi-selection modes and displays information about the current selection.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param selectedColor The currently selected color name (for single select).
 * @param onColorSelected Callback invoked when a color is selected in single-select mode.
 * @param selectedColors The list of currently selected color names (for multi-select).
 * @param onColorsChanged Callback invoked when the selection changes in multi-select mode.
 * @param defaultColors A list of default colors to show when [showDefaultOption] is true.
 * @param multiSelect Whether to allow multiple colors to be selected.
 * @param showDefaultOption Whether to show a card for resetting to default colors in the nested [ColorPicker].
 * @param showLabel Whether to display a label in the nested [ColorPicker].
 */
@Composable
fun ProductColorSelection(
    modifier: Modifier = Modifier,
    // Single selection parameters
    selectedColor: String? = null,
    onColorSelected: (String?) -> Unit = {},
    // Multi-selection parameters
    selectedColors: List<String> = emptyList(),
    onColorsChanged: (List<String>) -> Unit = {},
    // Common parameters
    defaultColors: List<String> = listOf("Black", "White"),
    multiSelect: Boolean = false, // Default to single select
    showDefaultOption: Boolean = true,
    showLabel: Boolean = true,
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Column {
        if (multiSelect) {
            // Multi-select ColorPicker with default option
            ColorPicker(
                selectedColors = selectedColors,
                onColorsSelected = onColorsChanged,
                showLabel = showLabel,
                multiSelect = true,
                defaultColors = defaultColors,
                showDefaultOption = showDefaultOption,
                modifier = modifier.then(Modifier.fillMaxWidth())
            )

            // Display selected colors info for multi-select
            if (selectedColors.isEmpty() && showDefaultOption) {
                Text(
                    text = "Using default colors: ${defaultColors.joinToString(", ")}",
                    style = windowSizeClass.bodyTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = windowSizeClass.normalVerticalPadding)
                )
            } else if (selectedColors.isNotEmpty()) {
                Text(
                    text = "Selected: ${selectedColors.joinToString(", ")}",
                    style = windowSizeClass.bodyTextStyle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = windowSizeClass.normalVerticalPadding)
                )
            }
        } else {
            // Single select ColorPicker with default option
            ColorPicker(
                selectedColor = selectedColor,
                onColorSelected = onColorSelected,
                showLabel = showLabel,
                multiSelect = false,
                defaultColors = defaultColors,
                showDefaultOption = showDefaultOption,
                modifier = modifier.then(Modifier.fillMaxWidth())
            )

            // Display selected color info for single select
            if (selectedColor.isNullOrEmpty() && showDefaultOption) {
                Text(
                    text = "Using default color: ${defaultColors.firstOrNull() ?: "Default"}",
                    style = windowSizeClass.bodyTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = windowSizeClass.normalVerticalPadding)
                )
            } else if (!selectedColor.isNullOrEmpty()) {
                Text(
                    text = "Selected: $selectedColor",
                    style = windowSizeClass.bodyTextStyle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = windowSizeClass.normalVerticalPadding)
                )
            }
        }
    }
}