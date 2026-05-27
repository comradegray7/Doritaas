package com.example.myapp.view.screens.forms

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.myapp.data.dataclass.UserProfile
import com.example.myapp.data.model.AuthViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.ProfileViewModel
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
import com.example.myapp.view.components.TermsOfServiceAndUse
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.components.isValidEmail
import com.example.myapp.view.components.isValidFullName
import com.example.myapp.view.components.isValidPassword
import com.joelkanyi.jcomposecountrycodepicker.component.KomposeCountryCodePicker
import com.joelkanyi.jcomposecountrycodepicker.component.rememberKomposeCountryCodePickerState
import kotlinx.coroutines.delay

/**
 * SignUpScreen - Composable function for user registration
 *
 * This screen provides a comprehensive sign-up form with the following features:
 * - Full name, email, phone, location and password input fields with real-time validation
 * - Terms of service agreement checkbox
 * - Social login options (Google and Apple)
 * - Navigation to sign-in screen
 * - Form validation with error messages
 *
 * The screen uses a custom scaffold container with pull-to-refresh functionality
 * and displays the app logo prominently at the top.
 *
 * @param onSignInClick Callback function for navigating to sign-in screen
 *
 * Usage:
 * ```
 * SignUpScreen(
 *     onSignInClick = { /* navigate to sign in */ }
 * )
 * ```
 */

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun SignUpScreen(
    onSignInClick: () -> Unit,
    onNavigateToShop: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val networkState = rememberNetworkState(networkManager)

    val authState by authViewModel.authState.collectAsState()
    val isProfileLoading by profileViewModel.isLoading.collectAsState()

    // ---- Country code picker state ----
    val phonePickerState = rememberKomposeCountryCodePickerState(
        showCountryCode = true,
        showCountryFlag = true,
    )

    // Raw digits the user types (no country prefix)
    var phoneNumber by rememberSaveable { mutableStateOf("") }

    // ---- Form fields — all rememberSaveable so they survive config changes ----
    var isAgreed by rememberSaveable { mutableStateOf(false) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    // ---- Validation error messages ----
    var fullNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var termsError by remember { mutableStateOf("") }

    // ---- Snack bar ----
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    // ---- Navigate after successful sign-up + profile creation ----
    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn && authState.user != null) {
            val userProfile = UserProfile(
                fullName = fullName,
                displayName = fullName.split(" ").firstOrNull() ?: fullName,
                email = email,
                // Use the full E.164 number (e.g. "+265991234567")
                phone = phonePickerState.getFullPhoneNumber(),
                admin = false
            )
            profileViewModel.createProfile(authState.user!!.uid, userProfile)
            delay(3200L)
            onNavigateToShop()
        }
    }

    // ---- Collect snack bar events ----
    LaunchedEffect(Unit) {
        authViewModel.snackBarData.collect { snackBarData ->
            currentSnackBarData = snackBarData
            showSnackBar = true
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

    // ---- Validation functions ----

    fun validateFullName(): Boolean {
        return when {
            fullName.isEmpty() -> {
                fullNameError = "Full name is required"; false
            }

            !isValidFullName(fullName) -> {
                fullNameError = "Please enter first and last name"; false
            }

            else -> {
                fullNameError = ""; true
            }
        }
    }

    fun validateEmail(): Boolean {
        return when {
            email.isEmpty() -> {
                emailError = "Email is required"; false
            }

            !isValidEmail(email) -> {
                emailError = "Please enter a valid email address"; false
            }

            else -> {
                emailError = ""; true
            }
        }
    }

    fun validatePassword(): Boolean {
        return when {
            password.isEmpty() -> {
                passwordError = "Password is required"; false
            }

            !isValidPassword(password) -> {
                passwordError =
                    "Password must be at least 8 characters with letters and numbers"; false
            }

            else -> {
                passwordError = ""; true
            }
        }
    }

    fun validatePhone(): Boolean {
        // Delegate to the library's own validator — it checks country-specific rules
        return if (!phonePickerState.isPhoneNumberValid()) {
            phoneError = "Please enter a valid phone number"
            false
        } else {
            phoneError = ""
            true
        }
    }

    fun validateTerms(): Boolean {
        return if (!isAgreed) {
            termsError = "You must agree to the terms and conditions"
            false
        } else {
            termsError = ""
            true
        }
    }

    fun validateForm(): Boolean {
        // Evaluate ALL fields so every error shows at once
        val a = validateFullName()
        val b = validateEmail()
        val c = validatePassword()
        val d = validatePhone()
        val e = validateTerms()

        if (authState.error != null) authViewModel.clearError()

        return a && b && c && d && e
    }

    // ---- UI ----

    val scrollState = rememberScrollState()

    CustomScaffoldContainer(
        showTopBar = false,
        showBottomBar = false,
        showBackArrow = false,
        verticalArrangement = Arrangement.Center,
        content = {

            // Network banner
            if (!networkState.hasInternet) {
                CustomSpacer()
                NetworkIndicator(networkState = networkState)
                CustomSpacer()
                PaddedSection(alignment = Alignment.CenterHorizontally, content = {
                    NetworkStatusBanner(networkState = networkState)
                })
                CustomSpacer()
            }

            // Floating snack bar
            currentSnackBarData?.let { snackBarData ->
                PaddedSection(alignment = Alignment.CenterHorizontally, content = {
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
                })
            }

            FormContainer(scrollState = scrollState) {

                Logo()

                HeadlineWidget(middleText = R.string.create_account)

                // Auth error card
                authState.error?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = windowSizeClass.basePadding),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(windowSizeClass.basePadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error,
                                style = windowSizeClass.bodyTextStyle,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            CustomTextButton(
                                onClick = { authViewModel.clearError() },
                                label = R.string.dismiss
                            )
                        }
                    }
                }

                // Full name
                CustomTextField(
                    label = R.string.full_name,
                    placeholder = R.string.enter_full_name,
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        if (fullNameError.isNotEmpty()) validateFullName()
                        if (authState.error != null) authViewModel.clearError()
                    },
                    isError = fullNameError.isNotEmpty(),
                    errorMessage = fullNameError,
                    enabled = !authState.isLoading && !isProfileLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                // Email
                CustomTextField(
                    label = R.string.email,
                    placeholder = R.string.enter_email,
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError.isNotEmpty()) validateEmail()
                        if (authState.error != null) authViewModel.clearError()
                    },
                    isError = emailError.isNotEmpty(),
                    errorMessage = emailError,
                    enabled = !authState.isLoading && !isProfileLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                // Phone + country code picker
                CustomTextField(
                    label = R.string.phone,
                    placeholder = R.string.enter_phone,
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        // Re-validate live once the user has already tried submitting
                        if (phoneError.isNotEmpty()) validatePhone()
                    },
                    leadingIcon = {
                        KomposeCountryCodePicker(
                            modifier = Modifier,
                            showOnlyCountryCodePicker = true,
                            text = phoneNumber,
                            state = phonePickerState,
                        )
                    },
                    isError = phoneError.isNotEmpty(),
                    errorMessage = phoneError,          // rendered by CustomTextField — no duplicate Text needed
                    enabled = !authState.isLoading && !isProfileLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                // Password
                CustomTextField(
                    label = R.string.password,
                    placeholder = R.string.enter_password,
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError.isNotEmpty()) validatePassword()
                        if (authState.error != null) authViewModel.clearError()
                    },
                    isError = passwordError.isNotEmpty(),
                    errorMessage = passwordError,
                    isPassword = true,
                    enabled = !authState.isLoading && !isProfileLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                // Terms of service
                PaddedSection(alignment = Alignment.CenterHorizontally, content = {
                    TermsOfServiceAndUse(
                        isChecked = isAgreed,
                        onCheckedChange = {
                            isAgreed = it
                            if (termsError.isNotEmpty()) validateTerms()
                        },
                        termsUrl = "https://example.com/terms-of-service",
                        privacyUrl = "https://example.com/privacy-policy",
                        errorMessage = termsError,
                        isError = termsError.isNotEmpty(),
                        enabled = !authState.isLoading && !isProfileLoading
                    )
                })

                // Sign up
                CustomButton(
                    label = R.string.sign_up,
                    onClick = {
                        if (validateForm()) {
                            authViewModel.signUpWithEmail(email, password)
                        }
                    },
                    enabled = !authState.isLoading && !isProfileLoading,
                    isLoading = authState.isLoading || isProfileLoading
                )

                // Already have an account?
                CustomRow(
                    leadingText = R.string.already_have_account,
                    trailingText = R.string.sign_in,
                    onClick = { onSignInClick() }
                )
            }
        }
    )
}