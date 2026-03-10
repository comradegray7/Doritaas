package com.example.myapp.view.admin

import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.myapp.ui.theme.LocalWindowSizeConstant

/**
 * CustomSearchBar - Reusable search input component for admin screens
 *
 * Provides consistent search functionality across all admin management screens
 * with adaptive sizing based on window size class and Material Design 3 styling.
 *
 * ## Features
 * - **Adaptive Sizing**: Width and height adjust based on screen size
 * - **Customizable Icons**: Leading and trailing icons can be customized
 * - **Placeholder Support**: Custom placeholder text
 * - **Real-time Search**: Callback on query changes
 * - **Material Design 3**: Uses MD3 SearchBar component
 * - **Surface Variant**: Uses surface variant color for container
 *
 * ## Usage Example
 * ```kotlin
 * CustomSearchBar(
 *     query = searchQuery,
 *     onQueryChange = { newQuery ->
 *         searchQuery = newQuery
 *         viewModel.search(newQuery)
 *     },
 *     onSearch = { query -> viewModel.performSearch(query) },
 *     leadingIcon = { Icon(Icons.Filled.Search, "Search") },
 *     trailingIcon = {
 *         if (searchQuery.isNotEmpty()) {
 *             IconButton(onClick = { searchQuery = "" }) {
 *                 Icon(Icons.Filled.Clear, "Clear")
 *             }
 *         }
 *     },
 *     placeholder = { Text("Search products...") }
 * )
 * ```
 *
 * @param query Current search query text
 * @param onQueryChange Callback invoked when query text changes
 * @param onSearch Callback invoked when search is submitted (e.g., pressing enter)
 * @param trailingIcon Composable for trailing icon (typically clear button when query is not empty)
 * @param leadingIcon Composable for leading icon (typically search icon)
 * @param placeholder Composable for placeholder text shown when query is empty
 *
 * @see ProductManagementScreen for usage example
 * @see OrderManagementScreen for usage example
 * @see CategoryManagementScreen for usage example
 * @see SizeManagementScreen for usage example
 * @see ColorManagementScreen for usage example
 * @see BrandManagementScreen for usage example
 * @see ShipmentManagementScreen for usage example
 * @see PromotionManagementScreen for usage example
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchBar(
    query: String = "",
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit),
    leadingIcon: @Composable (() -> Unit),
    placeholder: @Composable (() -> Unit)
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    SearchBar(
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = windowSizeConstant.adaptiveWidthModifier
            .height(windowSizeConstant.adaptiveHeight),
        inputField = {
            CompositionLocalProvider(LocalTextStyle provides windowSizeConstant.bodyTextStyle) {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { newQuery -> onQueryChange(newQuery) }, // FIX: Call the lambda
                    onSearch = { searchQuery -> onSearch(searchQuery) }, // FIX: Call the lambda
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = { placeholder() },
                    leadingIcon = { leadingIcon() },
                    trailingIcon = { trailingIcon() }
                )
            }
        },
        expanded = false,
        onExpandedChange = { }
    ) {}
}