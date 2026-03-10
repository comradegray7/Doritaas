package com.example.myapp.view.screens.bottom_bar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapp.navigation.AppNavigationGraph
import com.example.myapp.navigation.AppRoutes
import com.example.myapp.navigation.Destination
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomIcon

/**
 * MainAppScreen - The main entry point for the app's bottom bar navigation.
 *
 * This composable initializes the [NavHostController] and determines whether the bottom navigation
 * bar should be visible based on the current destination. It delegates the UI rendering to
 * [BottomNavigationLayout].
 *
 * ## Features
 * - Initializes `rememberNavController`.
 * - Tracks current back stack entry to update UI state.
 * - Hides bottom navigation on screens not listed in [Destination].
 *
 * @see BottomNavigationLayout
 * @see Destination
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navigationScreens = Destination.entries.map { it.route }
    val shouldShowNavigation = currentDestination?.route in navigationScreens

    BottomNavigationLayout(
        navController = navController,
        currentDestination = currentDestination,
        shouldShowNavigation = shouldShowNavigation,
    )
}

/**
 * BottomNavigationLayout - Layout structure for screens with bottom navigation.
 *
 * Wraps the content in a [Scaffold] and conditionally displays the [NavigationBar].
 * Handles navigation item clicks and state restoration.
 *
 * ## Features
 * - Displays Material 3 [NavigationBar] with icons and labels.
 * - Highlights current active destination.
 * - Hosts the [AppNavigationGraph].
 *
 * @param navController The navigation controller for the app.
 * @param currentDestination The current navigation destination to determine selected state.
 * @param shouldShowNavigation Whether to display the bottom navigation bar (true for main tabs).
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)

@Composable
private fun BottomNavigationLayout(
    navController: NavHostController,
    currentDestination: NavDestination?,
    shouldShowNavigation: Boolean = true,
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Scaffold(
        bottomBar = {
            if (shouldShowNavigation) {
                Column { // Wrap in Column to place the border on top
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant, // Subtle gray/border color
                        thickness = customSpacing.customZero8
                    )
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        windowInsets = NavigationBarDefaults.windowInsets
                    ) {
                        Destination.entries.forEach { destination ->
                            NavigationBarItem(
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.outline,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedTextColor = MaterialTheme.colorScheme.outline,
                                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                icon = {
                                    CustomIcon(
                                        icon = destination.icon,
                                        contentDescription = destination.contentDescription,
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.label,
                                        style = windowSizeConstant.bottomBarLabelStyles,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                selected = currentDestination?.hierarchy?.any {
                                    it.route == destination.route
                                } == true,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        content = { padding ->
            val padding = PaddingValues.Zero

            AppNavigationGraph(
                modifier = Modifier
                    .consumeWindowInsets(padding)
                    .padding(padding)
                    .fillMaxSize(),
                navController = navController,
                startDestination = AppRoutes.SHOP
            )
        }
    )
}
