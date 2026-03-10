package com.example.myapp.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.myapp.R
import com.example.myapp.ui.theme.LocalWindowSizeConstant

/**
 * OrDivider - Composable function for creating a visual divider with "OR" text.
 * 
 * This composable creates a horizontal divider with "OR" text in the center,
 * commonly used to separate different authentication options or sections in forms.
 * The divider uses adaptive padding based on the current window size.
 *
 * ## Usage:
 * ```kotlin
 * OrDivider()
 * ```
 */
@Composable
fun OrDivider() {
    // Get the current window size constants for adaptive behavior
    val windowSizeConstant = LocalWindowSizeConstant.current

    Row(
        modifier = Modifier.fillMaxWidth(), // Take full available width
        horizontalArrangement = Arrangement.Center, // Center the divider horizontally
        verticalAlignment = Alignment.CenterVertically, // Center items vertically
    ) {
        // Left horizontal divider line
        CustomHorizontalDivider(
            modifier = Modifier.weight(1f), // Adaptive width based on window size
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) // Semi-transparent outline color
        )
        
        // "OR" text in the center
        Text(
            text = stringResource(R.string.or), // "OR" text from string resources
            Modifier.padding(horizontal = windowSizeConstant.basePadding), // Medium horizontal padding
            style = windowSizeConstant.bodyTextStyle, // Medium body text style
            color = MaterialTheme.colorScheme.onSurfaceVariant // Surface variant color for subtle appearance
        )
        
        // Right horizontal divider line
        CustomHorizontalDivider(
            modifier = Modifier.weight(1f), // Adaptive width based on window size
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) // Semi-transparent outline color
        )
    }
}