package com.example.myapp.view.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.example.myapp.ui.theme.LocalWindowSizeConstant

/**
 * CustomHorizontalDivider - Reusable horizontal divider with consistent spacing
 *
 * Provides a standardized horizontal divider with vertical padding for visual separation
 * between content sections throughout the app.
 *
 * ## Features
 * - Consistent vertical padding (16dp)
 * - Material Design 3 styling
 * - Reusable across all screens
 *
 * @see HorizontalDivider for base Material 3 component
 */
@Composable
fun CustomHorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {

    val windowSizeConstant = LocalWindowSizeConstant.current

    HorizontalDivider(
        modifier = modifier.then(Modifier.padding(vertical = windowSizeConstant.basePadding)),
        thickness = thickness, color = color,
    )
}