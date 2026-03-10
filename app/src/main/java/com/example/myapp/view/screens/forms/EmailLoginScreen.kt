package com.example.myapp.view.screens.forms

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneIphone
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.example.myapp.view.components.OrDivider
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.isValidEmail
import com.example.myapp.view.components.isValidPassword
import com.example.myapp.view.utils.ButtonIcon
import kotlinx.coroutines.delay

/**
 * EmailLoginScreen - Composable function for the email/password login screen.
 *
 * This screen allows users to log in using their email and password.
 * It includes validation, error handling, and links to password reset and sign up.
 * The screen provides multiple authentication options:
 * - Google Sign-In
 * - Apple Sign-In
 * - Email/Password login
 * - Forgot password navigation
 * - Sign up navigation
 *
 * @param onForgetPasswordClick Callback for when the "Forgot Password" text is clicked.
 * @param onNavigateToShopScreen Callback for navigation after sign-in.
 * @param onNavigateToSignUpScreen Callback for navigation to sign-up screen.
 * @param viewModel AuthViewModel instance for authentication.
 * @param onContinueWithPhoneClick Callback for phone sign-in.
 * @param networkManager NetworkManager instance for network state.
 *
 * Usage:
 * ```
 * EmailLoginScreen(
 *     onForgetPasswordClick = { /* navigate to forgot password */ },
 *     onSignClick = { /* navigate to sign up */ }
 * )
 * ```
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun EmailLoginScreen(
    onForgetPasswordClick: () -> Unit,
    onNavigateToShopScreen: () -> Unit = {},
    onNavigateToSignUpScreen: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel(),
    onContinueWithPhoneClick: () -> Unit,
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val context = LocalContext.current
    val windowSizeConstant = LocalWindowSizeConstant.current
    val authState by viewModel.authState.collectAsState()
    val scrollState = rememberScrollState()
    val networkState = rememberNetworkState(networkManager)

    // Form state variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Validation state variables
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var isFormSubmitted by remember { mutableStateOf(false) }

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }

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

    // Handle successful authentication
    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn) {
            delay(3000L)
            onNavigateToShopScreen()
        }
    }

    /**
     * validateEmail
     *
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
     * validatePassword
     *
     */
    fun validatePassword(): Boolean {
        return when {
            isFormSubmitted && password.isBlank() -> {
                passwordError = "Password is required"
                false
            }

            isFormSubmitted && !isValidPassword(password) -> {
                passwordError = "Password must be at least 8 characters with letters and numbers"
                false
            }
            else -> {
                passwordError = ""
                true
            }
        }
    }

    /**
     * validateForm
     *
     */
    fun validateForm(): Boolean {
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        return isEmailValid && isPasswordValid
    }

    // Validate on each recomposition when form is submitted
    LaunchedEffect(email, password, isFormSubmitted) {
        if (isFormSubmitted) {
            validateEmail()
            validatePassword()
        }
    }

    CustomScaffoldContainer(
        showTopBar = false,
        showBottomBar = false,
        verticalArrangement = Arrangement.Center,
        snackBarHostState = snackBarHostState,
        content = {
            // Network Status Banner
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
                                .padding(top = windowSizeConstant.baseSize),
                            onDismiss = {
                                showSnackBar = false
                                currentSnackBarData = null
                            }
                        )
                    }
                )
            }

            FormContainer(scrollState = scrollState) {
                Logo()

                HeadlineWidget(
                    middleText = R.string.sign_in_to_your_account,
                )

                // Show error message if any
                authState.error?.let { error ->
                    Text(
                        text = error,
                        style = windowSizeConstant.bodyTextStyle,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = windowSizeConstant.normalVerticalPadding)
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
                        if (authState.error != null) viewModel.clearError()
                    },
                    isError = emailError.isNotEmpty(),
                    errorMessage = emailError,
                    enabled = !authState.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                CustomTextField(
                    label = R.string.password,
                    placeholder = R.string.enter_password,
                    value = password,
                    onValueChange = {
                        password = it
                        // Clear error when user starts typing (like in AddCategoryDialog)
                        if (passwordError.isNotEmpty()) {
                            passwordError = ""
                        }
                        if (authState.error != null) viewModel.clearError()
                    },
                    isError = passwordError.isNotEmpty(),
                    errorMessage = passwordError,
                    isPassword = true,
                    enabled = !authState.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                CustomButton(
                    label = R.string.sign_in,
                    onClick = {
                        isFormSubmitted = true // Mark form as submitted
                        if (validateForm()) {
                            viewModel.signInWithEmail(email, password)
                        }
                    },
                    enabled = !authState.isLoading,
                    isLoading = authState.isLoading
                )

                OrDivider()

                CustomButton(
                    label = R.string.sign_in_with_google,
                    tintColor = MaterialTheme.colorScheme.scrim,
                    icon = ButtonIcon.Resource(R.drawable.google_icon),
                    onClick = {
                        (context as? ComponentActivity)?.let { activity ->
                            viewModel.signInWithGoogle(activity)
                        } ?: run {
                            // Handle error - show message to user
                            println("Context is not a ComponentActivity")
                        }
                    },
                    contentDescription = "Google Icon",
                    enabled = !authState.isLoading
                )

                // Phone Sign In Button - secondary social login option
                CustomButton(
                    label = R.string.sign_in_with_phone,
                    icon = ButtonIcon.Vector(Icons.Filled.PhoneIphone),
                    onClick = {
                        onContinueWithPhoneClick()
                    },
                    contentDescription = "phone",
                    enabled = !authState.isLoading
                )

                Text(
                    text = stringResource(R.string.forgot_password),
                    style = windowSizeConstant.bodyTextStyle,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.clickable(enabled = !authState.isLoading) {
                        onForgetPasswordClick()
                    }
                )

                CustomRow(
                    leadingText = R.string.do_not_have_account,
                    trailingText = R.string.sign_up,
                    onClick = { onNavigateToSignUpScreen() },
                )
            }
        }
    )
}
