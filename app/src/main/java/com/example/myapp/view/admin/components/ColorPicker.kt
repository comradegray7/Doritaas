package com.example.myapp.view.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.R
import com.example.myapp.data.dataclass.ColorItem
import com.example.myapp.data.model.ColorViewModel
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyRow
import com.example.myapp.view.components.shimmerEffect


// ============================================
// REUSABLE ColorPicker Component (Single & Multi-Select)
// ============================================

/**
 * A reusable ColorPicker component that supports both single and multi-select modes.
 * It fetches available colors using [ColorViewModel] and displays them in a horizontal list.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param selectedColor The currently selected color name (for single select).
 * @param selectedColors The list of currently selected color names (for multi-select).
 * @param onColorSelected Callback invoked when a color is selected in single-select mode.
 * @param onColorsSelected Callback invoked when the selection changes in multi-select mode.
 * @param colorViewModel The ViewModel responsible for managing color data.
 * @param showLabel Whether to display a label above the color picker.
 * @param label The text to display as the label. Defaults to localized "Colors" or "Select Color".
 * @param multiSelect Whether to allow multiple colors to be selected.
 * @param defaultColors A list of default colors to show when [showDefaultOption] is true.
 * @param showDefaultOption Whether to show a card for resetting to default colors.
 */
@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    selectedColor: String? = null, // For single select
    selectedColors: List<String>? = null, // For multi-select
    onColorSelected: ((String) -> Unit)? = null, // For single select
    onColorsSelected: ((List<String>) -> Unit)? = null, // For multi-select
    colorViewModel: ColorViewModel = hiltViewModel(),
    showLabel: Boolean = true,
    label: String? = null,
    multiSelect: Boolean = false,
    defaultColors: List<String> = emptyList(),
    showDefaultOption: Boolean = false
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    val colorState by colorViewModel.colorState.collectAsState()
    val availableColors = colorState.colors

    LaunchedEffect(Unit) {
        if (availableColors.isEmpty()) {
            colorViewModel.loadColors()
        }
    }

    val currentSelectedColors = selectedColors ?: emptyList()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding)
    ) {
        // Optional label
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label ?: stringResource(
                        if (multiSelect) R.string.colors else R.string.select_color
                    ),
                    style = MaterialTheme.typography.labelMedium,
                )

                // Show selected info
                if (multiSelect && currentSelectedColors.isNotEmpty()) {
                    Text(
                        text = "${currentSelectedColors.size} selected",
                        style = windowSizeClass.labelTextStyle,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (!multiSelect && selectedColor != null) {
                    Text(
                        text = "1 selected",
                        style = windowSizeClass.labelTextStyle,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        when {
            colorState.isLoading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding)
                ) {
                    repeat(6) {
                        Box(
                            modifier = Modifier
                                .size(customSpacing.custom48)
                                .clip(CircleShape)
                                .shimmerEffect()
                        )
                    }
                }
            }

            colorState.error != null -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = colorState.error ?: "Failed to load colors",
                        style = windowSizeClass.bodyTextStyle,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(windowSizeClass.baseNormalVerticalPadding)
                    )
                }
            }

            availableColors.isEmpty() -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(windowSizeClass.baseNormalVerticalPadding),
                        horizontalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomIcon(
                            icon = Icons.Filled.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = stringResource(R.string.no_colors_options),
                            style = windowSizeClass.bodyTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding)
                ) {
                    // Default colors option (for both single and multi-select)
                    if (showDefaultOption && defaultColors.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (multiSelect) {
                                        onColorsSelected?.invoke(emptyList())
                                    } else {
                                        // For single select, we need a special value to indicate "default"
                                        // Or you could pass null to indicate no color selected
                                        onColorSelected?.invoke("") // or "default" or null
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (
                                    (multiSelect && currentSelectedColors.isEmpty()) ||
                                    (!multiSelect && selectedColor.isNullOrEmpty())
                                )
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(windowSizeClass.baseNormalVerticalPadding),
                                horizontalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (multiSelect) {
                                    Checkbox(
                                        checked = currentSelectedColors.isEmpty(),
                                        onCheckedChange = null
                                    )
                                } else {
                                    RadioButton(
                                        selected = selectedColor.isNullOrEmpty(),
                                        onClick = null
                                    )
                                }

                                Text(
                                    text = if (multiSelect)
                                        "Use default colors (${defaultColors.joinToString(", ")})"
                                    else
                                        "Use default color (${defaultColors.firstOrNull() ?: "default"})",
                                    style = windowSizeClass.bodyTextStyle
                                )
                            }
                        }
                    }

                    // Display colors
                    CustomLazyRow(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(
                            items = availableColors,
                            key = { it.id }
                        ) { colorItem ->
                            if (multiSelect) {
                                // Multi-select item
                                MultiSelectColorItem(
                                    colorItem = colorItem,
                                    isSelected = currentSelectedColors.contains(colorItem.name),
                                    onClick = {
                                        val newSelection =
                                            if (currentSelectedColors.contains(colorItem.name)) {
                                                currentSelectedColors - colorItem.name
                                            } else {
                                                currentSelectedColors + colorItem.name
                                            }
                                        onColorsSelected?.invoke(newSelection)
                                    }
                                )
                            } else {
                                // Single select item
                                SingleSelectColorItem(
                                    colorItem = colorItem,
                                    isSelected = selectedColor == colorItem.name,
                                    onClick = { onColorSelected?.invoke(colorItem.name) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// Single Select Color Item
// ============================================

/**
 * Represents an individual color item for single selection.
 * Displays a color circle with a checkmark if selected, and the color name.
 *
 * @param colorItem The data model for the color.
 * @param isSelected Whether this color is currently selected.
 * @param onClick Callback invoked when the item is clicked.
 */
@Composable
private fun SingleSelectColorItem(
    colorItem: ColorItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = windowSizeClass.smallVerticalPadding)
    ) {
        // Color circle
        Box(
            modifier = Modifier
                .size(customSpacing.custom48)
                .clip(CircleShape)
                .background(
                    try {
                        Color(colorItem.hexCode.toColorInt())
                    } catch (_: Exception) {
                        colors.gray
                    }
                )
                .border(
                    width = if (isSelected) windowSizeClass.borderSize else windowSizeClass.smallSizes,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Gray.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            // Checkmark for selected color
            if (isSelected) {
                CustomIcon(
                    icon = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = colors.white,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                        .padding(windowSizeClass.smallVerticalPadding)
                )
            }
        }

        // Color name
        Text(
            text = colorItem.name,
            style = windowSizeClass.labelTextStyle,
            modifier = Modifier.padding(top = windowSizeClass.smallVerticalPadding),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================
// Multi-Select Color Item
// ============================================

/**
 * Represents an individual color item for multiple selection.
 * Displays a color circle with an offset checkmark indicator if selected, and the color name.
 *
 * @param colorItem The data model for the color.
 * @param isSelected Whether this color is currently selected.
 * @param onClick Callback invoked when the item is clicked.
 */
@Composable
private fun MultiSelectColorItem(
    colorItem: ColorItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = windowSizeClass.smallVerticalPadding)
    ) {
        Box {
            // Color circle
            Box(
                modifier = Modifier
                    .size(customSpacing.custom48)
                    .clip(CircleShape)
                    .background(
                        try {
                            Color(colorItem.hexCode.toColorInt())
                        } catch (_: Exception) {
                            colors.gray
                        }
                    )
                    .border(
                        width = if (isSelected) windowSizeClass.borderSize else windowSizeClass.smallSizes,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            colors.gray.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            )

            // Checkbox indicator for multi-select
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(
                            x = windowSizeClass.smallVerticalPadding,
                            y = -windowSizeClass.smallVerticalPadding
                        )
                        .size(windowSizeClass.baseSize)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                        .border(
                            windowSizeClass.borderSize,
                            MaterialTheme.colorScheme.background,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CustomIcon(
                        icon = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Color name
        Text(
            text = colorItem.name,
            style = windowSizeClass.labelTextStyle,
            modifier = Modifier.padding(top = windowSizeClass.smallVerticalPadding),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

