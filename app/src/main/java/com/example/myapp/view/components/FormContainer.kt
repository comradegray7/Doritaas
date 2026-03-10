package com.example.myapp.view.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapp.ui.theme.LocalWindowSizeConstant

/**
 * FormContainer - Composable function for creating scrollable form containers.
 * 
 * This composable creates a vertically scrollable column container specifically
 * designed for forms. It provides consistent spacing, padding, and scrolling
 * behavior for form content across the app.
 * 
 * @param scrollState Optional ScrollState for controlling scroll behavior
 * @param content The composable content to be displayed within the form container
 *
 * ## Usage:
 * ```kotlin
 * FormContainer {
 *     Text("Form content goes here")
 *     CustomTextField(...)
 *     CustomButton(...)
 * }
 * ```
 */
@Composable
fun FormContainer(
    modifier: Modifier = Modifier,
    scrollState: ScrollState? = null, // Default scroll state
    content: @Composable () -> Unit
) {
    // Get the current window size constants for adaptive behavior
    val windowSizeConstant = LocalWindowSizeConstant.current
    val scrollState = scrollState ?: rememberScrollState()
    Column(
        modifier = modifier.then(
            Modifier
                .verticalScroll(scrollState) // Enable vertical scrolling
                .padding(horizontal = windowSizeConstant.contentPadding)
        ), // Apply adaptive horizontal padding
        horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
        verticalArrangement = Arrangement.spacedBy(windowSizeConstant.basePadding) // Consistent medium spacing between items
    ) {
        content() // Display the provided form content
        CustomSpacer() // Add adaptive spacing at the bottom
    }
}