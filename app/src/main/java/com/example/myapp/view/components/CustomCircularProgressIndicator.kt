package com.example.myapp.view.components

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.example.myapp.ui.theme.customSpacing

/**
 * CustomCircularProgressIndicator - A wrapper around Material3's CircularProgressIndicator.
 *
 * This component provides a consistent loading indicator styling for the application.
 * It supports both determinate (progress value provided) and indeterminate (spinning) modes.
 *
 * @param modifier Modifier to be applied to the indicator.
 * @param progress The progress of this indicator (0.0 to 1.0). If null, the indicator is indeterminate.
 */
@Composable
fun CustomCircularProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    strokeWidth: Dp = customSpacing.custom4,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    if (progress != null) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = modifier,
            color = color,
            strokeWidth = strokeWidth,
            trackColor = trackColor,
        )
    } else {
        CircularProgressIndicator(
            modifier = modifier,
            color = color,
            strokeWidth = strokeWidth,
            trackColor = trackColor,
        )
    }
}