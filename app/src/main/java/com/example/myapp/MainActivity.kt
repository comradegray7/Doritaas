package com.example.myapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.data.AppStateManager
import com.example.myapp.data.model.AuthViewModel
import com.example.myapp.navigation.AppRoutes
import com.example.myapp.navigation.RootNavigationGraph
import com.example.myapp.ui.theme.MyAppTheme
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.utils.LocalWindowSizeClass
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

/**
 * MainActivity - The main entry point of the application.
 *
 * This activity serves as the root of the app and handles the initial setup,
 * including splash screen, edge-to-edge display, and theme configuration.
 * It also manages the window size class for responsive design.
 *
 * Key responsibilities:
 * - Optionally manages the system-level splash screen (see commented code).
 * - Enables edge-to-edge content drawing for a modern, immersive UI.
/**
 * to
 *
*/
 * - Provides the window size class to the Compose hierarchy for adaptive layouts.
 * - Applies the app's theme and renders the main navigation or splash screen.
 */

@AndroidEntryPoint
/**
 * MainActivity
 *
 */
class MainActivity : ComponentActivity() {

    // Inject AppStateManager
    @Inject
    lateinit var appStateManager: AppStateManager

    private var currentRoute: String? = null

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        setContent {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()

            val windowSizeClass = calculateWindowSizeClass(this)

            // Determine start destination
            val startDestination = when {
                !authState.isSignedIn -> AppRoutes.ON_BOARDING
                authState.admin -> AppRoutes.ADMIN_DASHBOARD
                else -> AppRoutes.SHOP
            }

            CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
                MyAppTheme {
                    Surface(tonalElevation = customSpacing.customZero2) {

                        RootNavigationGraph(
                            startDestination = startDestination,
                            appStateManager = appStateManager,
                            onRouteChanged = { route -> currentRoute = route },
                            authState = authState
                        )
                    }
                }
            }
        }

        PaymentConfiguration.init(
            this,
            "pk_test_51L8W9ZFSOt52vVKn3eQ0ytKnk65HMXU6WWoJFMlJwPTFFIPBa0bRhxOnZs7RWMIA839EbLzlw8kFbDyqWyC6KnC400Q6ASpuTb"
        )
    }

    //Save state when app goes to background
    override fun onPause() {
        super.onPause()
        currentRoute?.let { route ->
            appStateManager.saveNavigationState(route)
        }
    }

    //Save state when app is stopped
    override fun onStop() {
        super.onStop()
        currentRoute?.let { route ->
            appStateManager.saveNavigationState(route)
        }
    }
}