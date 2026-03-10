package com.example.myapp.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.customSpacing

/**
 * CustomLazyColumn - Composable function for creating adaptive vertical lazy lists.
 *
 * This composable creates a LazyColumn with adaptive alignment, spacing, and padding
 * based on the current window size. It provides consistent vertical scrolling behavior
 * throughout the app with proper spacing between items and bottom padding for navigation.
 *
 * @param content The composable content to be displayed in the vertical list
 *
 * Usage:
 * ```
 * CustomLazyColumn {
 *     items(products) { product ->
 *         ProductCard(product = product)
 *     }
 * }
 * ```
 */

@Composable
fun CustomLazyColumn(
    modifier: Modifier = Modifier, contentPadding: PaddingValues = PaddingValues(),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical? = Arrangement.spacedBy(customSpacing.custom16),
    content: LazyListScope.() -> Unit
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    val arrangement = Arrangement.spacedBy(windowSizeConstant.basePadding)

    LazyColumn(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement ?: arrangement, // Consistent vertical spacing
        contentPadding = contentPadding
    ) {
        content() // Display the provided content
    }
}

