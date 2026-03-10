package com.example.myapp.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.example.myapp.ui.theme.LocalWindowSizeConstant

/**
 * PaddedSection - Composable function for creating sections with adaptive padding and alignment.
 * 
 * This composable creates a Column container with adaptive horizontal padding and alignment
 * based on the current window size. It's useful for creating consistent, responsive
 * content sections throughout the app.
 * 
 * @param content The composable content to be displayed within the padded section
 *
 * ## Usage:
 * ```kotlin
 * PaddedSection {
 *     Text("Content goes here")
 *     Button("Click me") { }
 * }
 * ```
 */
@Composable
fun PaddedSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
    alignment: Alignment.Horizontal? = Alignment.Start,
    contentPadding: Dp? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center
) {

    // Get the current window size constants for adaptive behavior
    val windowSizeConstant = LocalWindowSizeConstant.current

    if (alignment != null) {
        Column(
            modifier = modifier.then(
                Modifier
                    .fillMaxWidth() // Take full available width
                    .padding(horizontal = contentPadding ?: windowSizeConstant.contentPadding)
            ), // Apply adaptive horizontal padding
            horizontalAlignment = alignment, // Use adaptive horizontal alignment
            verticalArrangement = verticalArrangement,
            content = content // Display the provided content
        )
    }
}
