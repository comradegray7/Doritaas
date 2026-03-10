package com.example.myapp.view.screens.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.myapp.view.components.CustomTextField
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.FormContainer
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.Logo
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.isValidEmail
import kotlinx.coroutines.delay

/**
 * ForgotPasswordScreen - A composable screen for password reset functionality
 *
 * This screen allows users to:
 * - Enter their email address for password reset
 * - Validate email format and presence
 * - Submit password reset request
 * - Navigate back to sign in screen
 *
 * The screen provides a simple form with email validation and clear user feedback.
 * It's part of the password recovery flow and serves as the entry point for
 * users who have forgotten their passwords.
 *
 * @param rememberPasswordClick Callback function to navigate back to sign in screen
 * @param resetPasswordClick Callback function to handle password reset submission
 *
 * Usage:
 * ```
 * ForgotPasswordScreen(
 *     rememberPasswordClick = { /* navigate back to sign in */ },
 *     resetPasswordClick = { /* handle password reset */ }
 * )
 * ```
 */

@Composable
fun ForgotPasswordScreen(
    rememberPasswordClick: () -> Unit,
    resetPasswordClick: () -> Unit,
    onEmailSent: () -> Unit,
    forgotPasswordViewModel: AuthViewModel = hiltViewModel(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    // Scroll state for the form container to handle scrolling when keyboard appears
    val scrollState = rememberScrollState()
    val authState by forgotPasswordViewModel.authState.collectAsState()
    val windowSizeClass = LocalWindowSizeConstant.current
    var isFormSubmitted by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }
    val networkState = rememberNetworkState(networkManager)

    // Handle snack bar messages
    val snackBarHostState = remember { SnackbarHostState() }

    // Handle snack bar data
    LaunchedEffect(Unit) {
        forgotPasswordViewModel.snackBarData.collect { snackBarData ->
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

    // Navigate when email is sent successfully
    LaunchedEffect(authState.isEmailSent) {
        if (authState.isEmailSent) {
            delay(3200L)
            onEmailSent()
        }
    }

    // Clear error when user starts typing
    LaunchedEffect(email) {
        if (authState.error != null) {
            forgotPasswordViewModel.clearError()
        }
    }

    /**
     * Validates the email input field
     *
     * Checks for:
     * - Empty email (required field validation)
     * - Valid email format using regex pattern
     *
     * @return Boolean indicating if email is valid
     */
    fun validateEmail(): Boolean {
        return when {
            // Only validate if form has been submitted
            isFormSubmitted && email.isBlank() -> {
                emailError = "Email is required"
                false
            }

            isFormSubmitted && !isValidEmail(email) -> {
                emailError = "Please enter a valid email address"
                false
            }

            else -> {
                emailError = ""
                true
            }
        }
    }

    /**
     * Validates the entire form before submission
     *
     * Currently validates:
     * - Email field
     *
     * @return Boolean indicating if all form fields are valid
     */
    fun validateForm(): Boolean {
        val isEmailValid = validateEmail()

        return isEmailValid
    }

    // Main scaffold container that provides the screen structure
    CustomScaffoldContainer(
        showTopBar = false, // No top app bar for this screen
        showBottomBar = false, // No bottom navigation bar
        verticalArrangement = Arrangement.Center,
        snackBarHostState = snackBarHostState, // Snack bar state
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
            FormContainer(
                scrollState = scrollState
            ) {
                // Logo section - displays app logo at the top
                Logo()

                // Headline section - displays main title and subtitle for password reset
                HeadlineWidget(
                    middleText = R.string.forgot_password,
                    subMiddleText = R.string.do_not_worry
                )

                // Show error from ViewModel if exists
                authState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = windowSizeClass.bodyTextStyle,
                        modifier = Modifier.padding(bottom = windowSizeClass.normalVerticalPadding)
                    )
                }

                CustomTextField(
                    label = R.string.email,
                    placeholder = R.string.enter_email,
                    value = email,
                    onValueChange = {
                        email = it
                        // Clear error when user starts typing (like in AddCategoryDialog)
                        if (emailError.isNotEmpty()) {
                            emailError = ""
                        }
                        if (authState.error != null) forgotPasswordViewModel.clearError()
                    },
                    isError = emailError.isNotEmpty(),
                    errorMessage = emailError,
                    enabled = !authState.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                // Reset password button - triggers form validation and submission
                CustomButton(
                    label = R.string.reset_password,
                    onClick = {
                        if (validateForm()) {
                            forgotPasswordViewModel.sendPasswordResetEmail(email)
                        }
                    },
                    isLoading = authState.isLoading
                )

                CustomRow(
                    leadingText = R.string.remember_password,
                    trailingText = R.string.sign_in,
                    onClick = { rememberPasswordClick() }
                )
            }
        }
    )
}
