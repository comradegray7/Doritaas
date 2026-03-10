package com.example.myapp.data.authentication

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.R
import com.example.myapp.data.model.AuthViewModel
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomCircularProgressIndicator
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomSpacer

// ============================================
// 1. AUTH GUARD COMPOSABLE
// ============================================

/**
 * AuthGuard - Protects routes requiring authentication
 *
 * Composable wrapper that checks if user is authenticated before showing content.
 * Redirects to sign-in if user is not authenticated. Monitors auth state in real-time.
 *
 * ## Features
 * - Real-time auth state monitoring via Flow
 * - Automatic redirect on unauthenticated access
 * - Loading state while checking authentication
 * - Seamless user experience with smooth transitions
 *
 * ## Usage
 * ```kotlin
 * AuthGuard(
 *     onUnauthenticated = { navController.navigate(AppRoutes.SIGN_IN) }
 * ) {
 *     // Protected content (e.g., CartScreen, ProfileScreen)
 *     CartScreen()
 * }
 * ```
 *
 * ## Protected Routes
 * Typically used for:
 * - Cart screen
 * - Favorites screen
 * - Profile screen
 * - Orders screen
 * - Payment screen
 *
 * @param viewModel AuthViewModel for auth state management
 * @param onUnauthenticated Callback invoked when user is not authenticated
 * @param content Protected content to display when user is authenticated
 *
 * @see AuthViewModel for auth state management
 * @see AdminGuard for admin-level protection
 */

@Composable
fun AuthGuard(
    viewModel: AuthViewModel = hiltViewModel(),
    onUnauthenticated: () -> Unit,
    content: @Composable () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState.isLoading, authState.isSignedIn) {
        // Only redirect once the auth check has actually completed
        if (!authState.isLoading && !authState.isSignedIn) {
            onUnauthenticated()
        }
    }

    when {
        authState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CustomCircularProgressIndicator()
            }
        }

        authState.isSignedIn -> content()
    }
}

/**
 * Guards routes that require admin privileges
 * Redirects to shop if user is not admin
 */
@Composable
fun AdminGuard(
    viewModel: AuthViewModel = hiltViewModel(),
    onUnauthorized: () -> Unit,
    content: @Composable () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val authState by viewModel.authState.collectAsState()
    val hasCheckedAdmin = remember { mutableStateOf(false) }
    val hasRedirected = remember { mutableStateOf(false) }

    // Wait for auth to be ready and admin status to be determined
    LaunchedEffect(authState.isLoading, authState.isSignedIn, authState.isAdmin) {
        // Skip if still loading or already redirected
        if (authState.isLoading || hasRedirected.value) return@LaunchedEffect

        when {
            !authState.isSignedIn -> {
                Log.d("AdminGuard", "Unauthorized: User not signed in")
                hasRedirected.value = true
                onUnauthorized()
            }

            !authState.isAdmin -> {
                Log.d("AdminGuard", "Unauthorized: User not admin")
                hasRedirected.value = true
                onUnauthorized()
            }

            else -> {
                // Admin check complete and user is admin
                if (!hasCheckedAdmin.value) {
                    Log.d("AdminGuard", "Authorized: User is admin")
                    hasCheckedAdmin.value = true
                }
            }
        }
    }

    // Handle UI states
    when {
        // Show loading while initializing or checking admin status
        authState.isLoading || (!authState.isAdmin && authState.isSignedIn && !hasCheckedAdmin.value) -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CustomCircularProgressIndicator()
                    Spacer(modifier = Modifier.height(windowSizeClass.basePadding))
                    Text(
                        text = if (authState.isLoading)
                            "Loading..."
                        else
                            "Verifying admin access...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Show content if authenticated and admin
        authState.isSignedIn && authState.isAdmin -> {
            // Reset redirect flag when content is shown
            LaunchedEffect(Unit) {
                hasRedirected.value = false
            }
            content()
        }

        // Show unauthorized screen
        else -> {
            // Reset states when showing unauthorized
            LaunchedEffect(Unit) {
                hasCheckedAdmin.value = false
                hasRedirected.value = false
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(customSpacing.custom16)
                ) {
                    CustomIcon(
                        icon = Icons.Filled.Lock,
                        contentDescription = "Unauthorized",
                        iconSize = customSpacing.custom64,
                        tint = MaterialTheme.colorScheme.error
                    )

                    CustomSpacer()

                    Text(
                        text = stringResource(R.string.access_denied),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    CustomSpacer()

                    Text(
                        text = stringResource(R.string.admin_privileges),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    CustomSpacer()

                    Text(
                        text = "SignedIn=${authState.isSignedIn}, isAdmin=${authState.isAdmin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


/**
 * Guards routes that should only be accessible when NOT authenticated.
 * Provides multiple options for handling authenticated users.
 */

@Composable
fun GuestGuard(
    viewModel: AuthViewModel = hiltViewModel(),
    authenticatedAction: AuthenticatedAction = AuthenticatedAction.None,
    snackBarHostState: SnackbarHostState? = null,
    onAuthenticated: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn) {
            when (authenticatedAction) {
                AuthenticatedAction.Redirect -> {
                    onAuthenticated?.invoke()
                }

                AuthenticatedAction.Toast -> {
                    Toast.makeText(
                        context,
                        "Please sign out to access this page",
                        Toast.LENGTH_LONG
                    ).show()
                    onAuthenticated?.invoke()
                }

                AuthenticatedAction.Snackbar -> {
                    snackBarHostState?.showSnackbar(
                        message = "You're already signed in",
                        actionLabel = "OK",
                        duration = SnackbarDuration.Short
                    )
                    onAuthenticated?.invoke()
                }

                AuthenticatedAction.None -> {
                    // Do nothing, just show the content
                }
            }
        }
    }

    // Always show content, but handle authenticated state appropriately
    content()
}

/**
 * AuthenticatedAction - Actions for handling authenticated users in guest routes
 *
 * Sealed class defining different ways to handle authenticated users
 * attempting to access guest-only routes.
 *
 * ## Actions
 * - **None**: No action, just show content (default)
 * - **Redirect**: Navigate away immediately without message
 * - **Toast**: Show toast message then redirect
 * - **Snackbar**: Show snackbar message then redirect
 *
 * @see GuestGuard for usage
 */
sealed class AuthenticatedAction {
    /**
     * None
     *
     * Singleton object for [TODO: Add description]
     */
    object None : AuthenticatedAction()

    /**
     * Redirect
     *
     * Singleton object for [TODO: Add description]
     */
    object Redirect : AuthenticatedAction()

    /**
     * Toast
     *
     * Singleton object for [TODO: Add description]
     */
    object Toast : AuthenticatedAction()

    /**
     * Snackbar
     *
     * Singleton object for [TODO: Add description]
     */
    object Snackbar : AuthenticatedAction()
}