package com.example.myapp.view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * PullToRefreshComponent - Composable function for implementing pull-to-refresh functionality.
 * 
 * This composable wraps content with Material Design 3's pull-to-refresh functionality,
 * allowing users to refresh content by pulling down on the screen. It provides a
 * customizable refresh indicator and handles the refresh state automatically.
 * 
 * @param modifier Optional modifier to apply to the pull-to-refresh container
 * @param isRefreshing Whether the content is currently being refreshed
 * @param onRefresh Callback function to execute when refresh is triggered
 * @param content The composable content to be wrapped with pull-to-refresh functionality
 * 
 * ## Usage:
 * ```kotlin
 * PullToRefreshComponent(
 *     isRefreshing = isLoading,
 *     onRefresh = { /* refresh logic */ }
 * ) {
 *     // Your content here
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshComponent(
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    // Create pull-to-refresh state for managing the refresh gesture
    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing, // Current refresh state
        onRefresh = onRefresh, // Callback when refresh is triggered
        modifier = modifier, // Apply custom modifier
        state = state, // Use the pull-to-refresh state
        indicator = {
            // Custom refresh indicator with Material Design 3 styling
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter), // Position at top center
                isRefreshing = isRefreshing, // Pass refresh state to indicator
                containerColor = MaterialTheme.colorScheme.primaryContainer, // Use theme container color
                color = MaterialTheme.colorScheme.onPrimaryContainer, // Use theme on-container color
                state = state // Pass state to indicator
            )
        },
    ) {
        // Content container
        Box {
            content() // Display the wrapped content
        }
    }
}