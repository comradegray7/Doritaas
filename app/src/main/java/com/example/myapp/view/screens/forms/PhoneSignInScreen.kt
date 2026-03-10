package com.example.myapp.view.screens.forms

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomTextField
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.FormContainer
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.Logo
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.getPhoneNumberErrorMessage
import kotlinx.coroutines.delay

/**
 * PhoneAuthScreen - Phone number authentication with OTP verification
 *
 * Two-step authentication screen using Firebase Phone Authentication. Users enter their
 * phone number, receive an OTP via SMS, and verify to sign in.
 *
 * ## Features
 * - **Two-Step Process**: Phone number entry → OTP verification
 * - **Firebase Integration**: Uses Firebase Phone Authentication
 * - **Input Validation**: Phone number and OTP validation
 * - **Error Handling**: Displays validation and authentication errors
 * - **Loading States**: Shows loading indicator during verification
 * - **Auto-Navigation**: Redirects to main screen on successful sign-in
 * - **Change Number**: Allows user to go back and change phone number
 *
 * ## User Workflow
 * 1. User enters phone number
 * 2. Clicks "Send OTP" button
 * 3. Receives SMS with 6-digit OTP code
 * 4. Enters OTP code
 * 5. Clicks "Verify OTP" button
 * 6. System verifies OTP with Firebase
 * 7. On success, navigates to main screen
 * 8. Can click "Change Number" to start over
 *
 * ## Validation Rules
 * ### Phone Number
 * - Required field
 * - Must be valid phone format
 * - Includes country code (e.g., +1234567890)
 *
 * ### OTP
 * - Required field
 * - Must be exactly 6 digits
 * - Only numeric characters allowed
 *
 * ## States
 * - **Phone Entry**: Initial state, user enters phone number
 * - **OTP Entry**: After sending OTP, user enters verification code
 * - **Loading**: During OTP send or verification
 * - **Error**: When validation or authentication fails
 * - **Success**: When OTP verified, auto-navigates
 *
 * @param viewModel AuthViewModel for authentication operations
 * @param onSignInSuccess Callback when sign-in is successful
 *
 * @see AuthViewModel for authentication logic
 * @see LoginScreen for alternative email/password authentication
 * @see SignUpScreen for account creation
 */
@Composable
fun PhoneAuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onSignInSuccess: () -> Unit,
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeClass  = LocalWindowSizeConstant.current
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var showOTPField by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val activity = context as? ComponentActivity ?: return
    val networkState = rememberNetworkState(networkManager)

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

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
                    middleText = R.string.sign_in_with_otp
                )

                if (!showOTPField) {
                    // Phone number input
                    CustomTextField(
                        label = R.string.phone_number, // Use your string resource
                        placeholder = R.string.phone_placeholder, // "+1234567890"
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            phoneError = ""
                        },
                        isError = phoneError.isNotEmpty() || authState.error != null,
                        errorMessage = phoneError.ifEmpty { authState.error.orEmpty() },
                        enabled = !authState.isLoading
                    )
                    CustomSpacer()
                    CustomButton(
                        onClick = {
                            val error = getPhoneNumberErrorMessage(phoneNumber)
                            if (error.isEmpty()) {
                                viewModel.sendOTP(phoneNumber, activity)
                                showOTPField = true
                            } else {
                                phoneError = error
                            }
                        },
                        label = R.string.send_otp, // "Send OTP",
                        isLoading = authState.isLoading,
                        enabled = !authState.isLoading && phoneNumber.isNotEmpty()
                    )
                } else {
                    // OTP input header
                    Text(
                        "Enter the OTP sent to $phoneNumber",
                        style = windowSizeClass.bodyTextStyle,
                    )

                    CustomSpacer()

                    // OTP input field
                    CustomTextField(
                        label = R.string.otp, // "OTP"
                        placeholder = R.string.otp_placeholder, // "000000"
                        value = otp,
                        onValueChange = { newValue ->
                            // Only allow digits and limit to 6
                            otp = newValue.filter { it.isDigit() }.take(6)
                            otpError = ""
                        },
                        isError = otpError.isNotEmpty() || authState.error != null,
                        errorMessage = otpError.ifEmpty { authState.error.orEmpty() },
                        enabled = !authState.isLoading
                    )

                    CustomButton(
                        onClick = {
                            when {
                                otp.isEmpty() -> otpError = "OTP is required"
                                otp.length < 6 -> otpError = "OTP must be 6 digits"
                                else -> viewModel.verifyOTP(otp)
                            }
                        },
                        label = R.string.sign_in_with_otp, // "Verify OTP"
                        isLoading = authState.isLoading,
                        enabled = !authState.isLoading && otp.isNotEmpty()
                    )

                    CustomButton(
                        onClick = {
                            showOTPField = false
                            otp = ""
                            otpError = ""
                        },
                        enabled = !authState.isLoading,
                        label = R.string.change_number,
                        isLoading = authState.isLoading,
                    )

                    if (authState.isSignedIn) {
                        LaunchedEffect(Unit) {
                            delay(2000)
                            onSignInSuccess()
                        }
                    }
                }
            }
        }
    )
}