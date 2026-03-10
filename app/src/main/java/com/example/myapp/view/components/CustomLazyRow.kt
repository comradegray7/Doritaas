package com.example.myapp.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapp.ui.theme.LocalWindowSizeConstant

/**
 * CustomLazyRow - Composable function for creating adaptive horizontal lazy lists.
 * 
 * This composable creates a LazyRow with adaptive spacing and padding based on
 * the current window size. It provides consistent horizontal scrolling behavior
 * throughout the app with proper spacing between items.
 *
 * @param modifier The modifier to be applied to the row
 * @param content The content DSL for the lazy list
 *
 * ## Usage:
 * ```kotlin
 * CustomLazyRow {
 *     items(products) { product ->
 *         ProductCard(product = product)
 *     }
 * }
 * ```
 */
@Composable
fun CustomLazyRow(modifier: Modifier = Modifier, content: LazyListScope.() -> Unit) {

    // Get the current window size constants for adaptive behavior
    val windowSizeConstant = LocalWindowSizeConstant.current

    LazyRow(
        modifier = Modifier.then(modifier),
        horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.baseNormalVerticalPadding), // Consistent spacing between items
        contentPadding = PaddingValues(horizontal = windowSizeConstant.contentPadding), // Adaptive horizontal padding
    ) {
        content() // Display the provided content
    }
}