package com.example.myapp.view.screens.forms

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.AuthViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomRow
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.FormContainer
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.Logo
import com.example.myapp.view.components.OrDivider
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.utils.ButtonIcon
import kotlinx.coroutines.delay

/**
 * LoginScreen - Composable function for the user login screen.
 * 
 * This screen provides multiple authentication options including:
 * - Google Sign-In
 * - Apple Sign-In
 * - Email Sign-In
 * - Sign Up navigation
 * 
 * The screen uses a custom scaffold container with pull-to-refresh functionality
 * and displays the app logo prominently at the top. This is the main entry point
 * for user authentication, offering various login methods to accommodate different
 * user preferences and requirements.
 * 
 * @param onContinueWithEmailClick Callback for Email Sign-In button
 * @param onSignUpClick Callback for Sign Up navigation
 * 
 * Usage:
 * ```
 * LoginScreen(
 *     onContinueWithGoogleClick = { /* handle Google sign in */ },
 *     onContinueWithAppleClick = { /* handle Apple sign in */ },
 *     onContinueWithEmailClick = { /* navigate to email login */ },
 *     onSignUpClick = { /* navigate to sign up */ }
 * )
 * ```
 */
@Composable
fun LoginScreen(
    onContinueWithPhoneClick: () -> Unit,
    onContinueWithEmailClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onSignInSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager

) {
    // Remember scroll state for the form container to handle scrolling when keyboard appears
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val networkState = rememberNetworkState(networkManager)
    val windowSizeClass = LocalWindowSizeConstant.current
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }

    // Navigate to main screen when signed in successfully
    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn && authState.user != null) {
            onSignInSuccess()
        }
    }

    // Handle snack bar data
    LaunchedEffect(Unit) {
        viewModel.snackBarData.collect { snackBarData ->
            currentSnackBarData = snackBarData
            showSnackBar = true

            // Auto-dismiss after duration (unless indefinite)
            if (snackBarData.duration != SnackbarDuration.Indefinite) {
                delay(
                    when (snackBarData.duration) {
                        SnackbarDuration.Short -> 3000L
                        SnackbarDuration.Long -> 5000L
                        else -> 3000L
                    }
                )
                showSnackBar = false
            }
        }
    }

    // Main scaffold container that provides the screen structure
    CustomScaffoldContainer(
        showTopBar = false, // No top bar for login screen
        showBottomBar = false, // No bottom bar for login screen
        showBackArrow = false, //no back arrow for login screen
        snackBarHostState = snackBarHostState,
        verticalArrangement = Arrangement.Center,
        content = {
            // Network Indicator in top bar
            if (!networkState.hasInternet) {

                CustomSpacer()
                NetworkIndicator(networkState = networkState)
                CustomSpacer()

                PaddedSection(
                    alignment = Alignment.CenterHorizontally,
                    content = {
                        NetworkStatusBanner(
                            networkState = networkState,
                        )
                    }
                )

                CustomSpacer()
            }

            currentSnackBarData?.let { snackBarData ->
                PaddedSection(
                    alignment = Alignment.CenterHorizontally,
                    content = {
                        FloatingCustomSnackBar(
                            snackBarData = snackBarData,
                            visible = showSnackBar,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(top = windowSizeClass.baseSize),
                            onDismiss = {
                                showSnackBar = false
                                currentSnackBarData = null
                            }
                        )
                    }
                )
            }

            // Form container that handles scrolling and layout
            FormContainer(scrollState = scrollState) {
                // Logo section - Display app logo prominently at the top
                Logo()

                // Main headline for the login screen - provides context and instructions
                HeadlineWidget(
                    middleText = R.string.sign_in_to_your_account
                )

                // Google Sign-In Button - primary social login option
                // Google Sign-In Button - primary social login option
                CustomButton(
                    label = R.string.sign_in_with_google,
                    tintColor = MaterialTheme.colorScheme.scrim,
                    icon = ButtonIcon.Resource(R.drawable.google_icon),
                    isLoading = authState.isLoading,
                    enabled = !authState.isLoading,
                    onClick = {
                        // Clear any existing errors before attempting sign-in
                        viewModel.clearError()

                        (context as? ComponentActivity)?.let { activity ->
                            viewModel.signInWithGoogle(activity)
                        } ?: run {
                            // Handle error - show message to user
                            println("Context is not a ComponentActivity")
                        }
                    },
                    contentDescription = "Google Icon",
                )

                // Visual separator between social and email login options
                OrDivider()

                // Email Sign-In Button - traditional login method
                CustomButton(
                    label = R.string.sign_in_with_Email,
                    icon = ButtonIcon.Resource(R.drawable.mail_icon),
                    onClick = {
                        onContinueWithEmailClick()
                    },
                    enabled = !authState.isLoading,
                    contentDescription = "email icon",
                )

                // Phone Sign In Button - secondary social login option
                CustomButton(
                    label = R.string.sign_in_with_phone,
                    icon = ButtonIcon.Vector(Icons.Filled.PhoneIphone),
                    onClick = {
                        onContinueWithPhoneClick()
                    },
                    enabled = !authState.isLoading,
                    contentDescription = "phone",
                )

                // Sign Up navigation row - allows users to go to sign-up screen
                CustomRow(
                    leadingText = R.string.do_not_have_account,
                    trailingText = R.string.sign_up,
                    onClick = { onSignUpClick() }
                )
            }
        }
    )
}
