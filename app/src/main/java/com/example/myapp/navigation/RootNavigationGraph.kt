package com.example.myapp.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapp.data.AppStateManager
import com.example.myapp.data.model.AuthState
import com.example.myapp.data.model.CartViewModel
import com.example.myapp.data.model.FavoriteViewModel
import com.example.myapp.navigation.navigation_helper.NavigationAnimations
import com.example.myapp.view.screens.bottom_bar.MainAppScreen
import com.example.myapp.view.screens.onboarding.OnboardingScreen
import kotlinx.coroutines.delay

/**
 * RootNavigationGraph - High-level navigation controller.
 *
 * Handles the initial entry point of the application, managing:
 * - Onboarding flow vs Main App flow content switching.
 * - Navigation state persistence (save/restore last route).
 * - Deep linking validation (not crashing on bad state).
 * - Clearing app state on logout.
 *
 * @param startDestination Initial route to display.
 * @param navController The [NavHostController] to manage navigation.
 * @param appStateManager Manager for saving/restoring navigation history.
 * @param onRouteChanged Callback triggered on every route change.
 * @param authState Current authentication state.
 * @param cartViewModel ViewModel for managing cart (cleared on logout).
 * @param favoriteViewModel ViewModel for managing favorites (cleared on logout).
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun RootNavigationGraph(
    startDestination: String,
    navController: NavHostController = rememberNavController(),
    appStateManager: AppStateManager,
    onRouteChanged: (String) -> Unit = {},
    authState: AuthState,
    cartViewModel: CartViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {

    //  Track current route
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(currentBackStackEntry) {
        currentBackStackEntry?.destination?.route?.let { route ->
            onRouteChanged(route)
        }
    }

    LaunchedEffect(currentBackStackEntry) {
        currentBackStackEntry?.destination?.route?.let { route ->
            onRouteChanged(route)
            // Save immediately on route change
            appStateManager.saveNavigationState(route)
        }
    }

    //   Restore last route after splash screen
    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn) {
            val savedRoute = appStateManager.restoreNavigationState()

            if (savedRoute != null &&
//                savedRoute != AppRoutes.ON_BOARDING &&
                savedRoute != AppRoutes.MAIN_FLOW &&
                savedRoute != AppRoutes.SIGN_IN &&
                savedRoute != AppRoutes.SIGN_UP
            ) {
                delay(100)
                navController.navigate(savedRoute) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        } else {
            //User logged out - clear both favorites and cart
            favoriteViewModel.clearFavorites()
            cartViewModel.clearCart()
            appStateManager.clearNavigationState()
        }
    }



    val startDestination = when {
        !authState.isSignedIn -> AppRoutes.ON_BOARDING
        else ->  AppRoutes.MAIN_FLOW
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { NavigationAnimations.slideInFromLeft() }, // Slide in from bottom
        exitTransition = { NavigationAnimations.slideOutToLeft() }, // Slide out to bottom
        popEnterTransition = { NavigationAnimations.slideInFromLeft() }, // Back navigation
        popExitTransition = { NavigationAnimations.slideOutToRight() } // Back navigation
    ) {
        // Onboarding flow - no bottom bar
        composable(
            AppRoutes.ON_BOARDING,
            enterTransition = { NavigationAnimations.slideInFromLeft() }, // Slide in from bottom
            exitTransition = { NavigationAnimations.slideOutToLeft() }, // Slide out to bottom
            popEnterTransition = { NavigationAnimations.slideInFromLeft() }, // Back navigation
            popExitTransition = { NavigationAnimations.slideOutToRight() } // Back navigation
            ) {
            OnboardingScreen(
                onFinished = {
                    // Navigate to main flow and clear onboarding from back stack
                    navController.navigate(AppRoutes.MAIN_FLOW) {
                        popUpTo(AppRoutes.ON_BOARDING) { inclusive = true }
                    }
                }
            )
        }

        // Main app flow - with bottom bar
        composable(
            AppRoutes.MAIN_FLOW,
            enterTransition = { NavigationAnimations.slideInFromLeft() }, // Slide in from bottom
            exitTransition = { NavigationAnimations.slideOutToLeft() }, // Slide out to bottom
            popEnterTransition = { NavigationAnimations.slideInFromLeft() }, // Back navigation
            popExitTransition = { NavigationAnimations.slideOutToRight() } // Back navigation
        ) {
            MainAppScreen()
        }
    }
}