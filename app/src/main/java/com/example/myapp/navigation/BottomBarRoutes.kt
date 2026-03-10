package com.example.myapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person2
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Destination entries for the main bottom navigation bar.
 *
 * Each enum value describes a bottom bar tab, including:
 * - `route`: Navigation route used by the `NavController`
 * - `label`: Text label displayed under the icon
 * - `icon`: [ImageVector] shown in the tab
 * - `contentDescription`: Accessibility description for screen readers
 */
enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    SHOP(route = AppRoutes.SHOP, label = "Shop", icon = Icons.Filled.Store, contentDescription = "Shop"),
    ALL_PRODUCTS(
        route = AppRoutes.ALL_PRODUCTS,
        label = "All Products",
        icon = Icons.Filled.Widgets,
        contentDescription = "All Products"
    ),
    FAVORITE(
        route = AppRoutes.FAVORITE,
        label = "Favorites",
        icon = Icons.Filled.Favorite,
        contentDescription = "Favorite"
    ),
    PROFILE(route = AppRoutes.PROFILE, label = "Profile", icon = Icons.Filled.Person2, contentDescription = "Profile"),
}