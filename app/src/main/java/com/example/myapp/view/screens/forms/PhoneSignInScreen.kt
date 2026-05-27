package com.example.myapp.view.screens.forms

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomTextField
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.FormContainer
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.Logo
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.joelkanyi.jcomposecountrycodepicker.component.KomposeCountryCodePicker
import com.joelkanyi.jcomposecountrycodepicker.component.rememberKomposeCountryCodePickerState
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
    val windowSizeClass = LocalWindowSizeConstant.current
    val context = LocalContext.current
    val activity = context as? ComponentActivity ?: return
    val networkState = rememberNetworkState(networkManager)

    val authState by viewModel.authState.collectAsState()

    // ---- Country code picker state ----
    val phonePickerState = rememberKomposeCountryCodePickerState(
        showCountryCode = true,
        showCountryFlag = true,
    )
    // Raw digits the user types — no prefix
    var phoneNumber by rememberSaveable { mutableStateOf("") }

    // ---- OTP flow state ----
    var otp by rememberSaveable { mutableStateOf("") }
    var showOTPField by rememberSaveable { mutableStateOf(false) }

    // ---- Validation errors ----
    var phoneError by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf("") }

    // ---- Snack bar ----
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }

    val scrollState = rememberScrollState()

    // ---- Navigate on successful sign-in (handles both OTP success paths) ----
    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn && authState.user != null) {
            onSignInSuccess()
        }
    }

    // ---- Snack bar event collection ----
    LaunchedEffect(Unit) {
        viewModel.snackBarData.collect { snackBarData ->
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

    CustomScaffoldContainer(
        showTopBar = false,
        showBottomBar = false,
        showBackArrow = false,
        snackBarHostState = snackBarHostState,
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

                HeadlineWidget(middleText = R.string.sign_in_with_otp)

                if (!showOTPField) {

                    // ---- Step 1: phone number entry ----
                    CustomTextField(
                        label = R.string.phone_number,
                        placeholder = R.string.phone_placeholder,
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            // Re-validate live after the user has already tried to proceed
                            if (phoneError.isNotEmpty()) {
                                phoneError = if (!phonePickerState.isPhoneNumberValid()) {
                                    "Please enter a valid phone number"
                                } else ""
                            }
                        },
                        leadingIcon = {
                            KomposeCountryCodePicker(
                                modifier = Modifier,
                                showOnlyCountryCodePicker = true,
                                text = phoneNumber,
                                state = phonePickerState,
                            )
                        },
                        isError = phoneError.isNotEmpty() || authState.error != null,
                        errorMessage = phoneError.ifEmpty { authState.error.orEmpty() },
                        enabled = !authState.isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    CustomSpacer()

                    CustomButton(
                        onClick = {
                            if (phonePickerState.isPhoneNumberValid()) {
                                phoneError = ""
                                // Send the full E.164 number, e.g. "+265991234567"
                                viewModel.sendOTP(
                                    phoneNumber = phonePickerState.getFullPhoneNumber(),
                                    activity = activity
                                )
                                showOTPField = true
                            } else {
                                phoneError = "Please enter a valid phone number"
                            }
                        },
                        label = R.string.send_otp,
                        isLoading = authState.isLoading,
                        enabled = !authState.isLoading && phoneNumber.isNotEmpty()
                    )

                } else {

                    // ---- Step 2: OTP verification ----
                    // Show the full formatted number so user can confirm it
                    Text(
                        text = "Enter the OTP sent to ${phonePickerState.getFullPhoneNumber()}",
                        style = windowSizeClass.bodyTextStyle,
                    )

                    CustomSpacer()

                    CustomTextField(
                        label = R.string.otp,
                        placeholder = R.string.otp_placeholder,
                        value = otp,
                        onValueChange = { newValue ->
                            otp = newValue.filter { it.isDigit() }.take(6)
                            otpError = ""
                            if (authState.error != null) viewModel.clearError()
                        },
                        isError = otpError.isNotEmpty() || authState.error != null,
                        errorMessage = otpError.ifEmpty { authState.error.orEmpty() },
                        enabled = !authState.isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )

                    CustomButton(
                        onClick = {
                            when {
                                otp.isEmpty() -> otpError = "OTP is required"
                                otp.length < 6 -> otpError = "OTP must be 6 digits"
                                else -> viewModel.verifyOTP(otp)
                            }
                        },
                        label = R.string.sign_in_with_otp,
                        isLoading = authState.isLoading,
                        enabled = !authState.isLoading && otp.length == 6
                    )

                    // Let the user go back and correct their number
                    CustomButton(
                        onClick = {
                            showOTPField = false
                            otp = ""
                            otpError = ""
                            viewModel.clearError()
                        },
                        enabled = !authState.isLoading,
                        label = R.string.change_number,
                        isLoading = authState.isLoading,
                    )

                }
            }
        }
    )
}