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

// =============================================================================
// AuthGuard — requires any signed-in user
// =============================================================================

@Composable
fun AuthGuard(
    viewModel: AuthViewModel = hiltViewModel(),
    onUnauthenticated: () -> Unit,
    content: @Composable () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    // Wait for the role check to finish before deciding to redirect.
    // Using isInitialized as the gate prevents a flash-redirect while
    // Firebase Auth is still resolving the session.
    LaunchedEffect(authState.isInitialized, authState.isSignedIn) {
        if (authState.isInitialized && !authState.isSignedIn) {
            onUnauthenticated()
        }
    }

    when {
        // Still resolving session — show spinner
        !authState.isInitialized || authState.isLoading -> {
            Box(
                modifier          = Modifier.fillMaxSize(),
                contentAlignment  = Alignment.Center
            ) {
                CustomCircularProgressIndicator()
            }
        }

        // Session confirmed — show content
        authState.isSignedIn -> content()

        // Otherwise the LaunchedEffect above handles the redirect;
        // show nothing while navigation settles
    }
}

// =============================================================================
// AdminGuard — requires admin OR superAdmin
// =============================================================================

@Composable
fun AdminGuard(
    viewModel: AuthViewModel = hiltViewModel(),
    onUnauthorized: () -> Unit,
    content: @Composable () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val authState by viewModel.authState.collectAsState()

    // Derived: user qualifies if they are admin or superAdmin
    val isAuthorized = authState.isSignedIn && (authState.admin || authState.superAdmin)

    // Only redirect once isInitialized is true so we never jump while
    // the ViewModel is still fetching admin/superAdmin flags from Firestore.
    LaunchedEffect(authState.isInitialized, isAuthorized) {
        if (authState.isInitialized && !isAuthorized) {
            Log.d("AdminGuard", "Unauthorized — isSignedIn=${authState.isSignedIn} admin=${authState.admin} superAdmin=${authState.superAdmin}")
            onUnauthorized()
        }
    }

    when {
        // Still resolving session or role flags — show spinner
        !authState.isInitialized || authState.isLoading -> {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CustomCircularProgressIndicator()
                    Spacer(modifier = Modifier.height(windowSizeClass.basePadding))
                    Text(
                        text  = "Verifying access...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Authorized — show the admin content
        isAuthorized -> content()

        // Not authorized — show a clear access-denied screen while the
        // LaunchedEffect above triggers navigation away.
        else -> {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier            = Modifier.padding(customSpacing.custom16)
                ) {
                    CustomIcon(
                        icon               = Icons.Filled.Lock,
                        contentDescription = "Unauthorized",
                        iconSize           = customSpacing.custom64,
                        tint               = MaterialTheme.colorScheme.error
                    )
                    CustomSpacer()
                    Text(
                        text  = stringResource(R.string.access_denied),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    CustomSpacer()
                    Text(
                        text      = stringResource(R.string.admin_privileges),
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// =============================================================================
// GuestGuard — should only be seen when NOT signed in
// =============================================================================

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

    LaunchedEffect(authState.isInitialized, authState.isSignedIn) {
        if (!authState.isInitialized) return@LaunchedEffect
        if (!authState.isSignedIn) return@LaunchedEffect

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
                    message     = "You're already signed in",
                    actionLabel = "OK",
                    duration    = SnackbarDuration.Short
                )
                onAuthenticated?.invoke()
            }
            AuthenticatedAction.None -> { /* show content as-is */ }
        }
    }

    content()
}

// =============================================================================
// AuthenticatedAction
// =============================================================================

sealed class AuthenticatedAction {
    object None     : AuthenticatedAction()
    object Redirect : AuthenticatedAction()
    object Toast    : AuthenticatedAction()
    object Snackbar : AuthenticatedAction()
}
