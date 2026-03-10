package com.example.myapp.data.model

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.ForgotPasswordUseCase
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.AuthRepository
import com.example.myapp.data.repository.FirebaseCartRepository
import com.example.myapp.data.repository.FirebaseFavoritesRepository
import com.example.myapp.data.repository.PrimeMembershipRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AuthState - UI State for Authentication screens
 *
 * Represents the current authentication status of the user and UI state.
 *
 * @property isLoading Loading indicator for auth operations
 * @property user The currently authenticated Firebase user (null if signed out)
 * @property error Error message for failed operations
 * @property isEmailSent Flag indicating if a reset/verification email was sent
 * @property isSignedIn Boolean flag helpful for UI navigation logic
 * @property isAdmin Boolean flag indicating if the current user has admin privileges
 * @property isInitialized Whether the auth state has been initial checked
 */
data class AuthState(
    val isLoading: Boolean = true,
    val user: FirebaseUser? = null,
    val error: String? = null,
    val isEmailSent: Boolean = false,
    val isSignedIn: Boolean = false,
    val isAdmin: Boolean = false,
    val isInitialized: Boolean = false
)

/**
 * AuthResult - Wrapper for authentication operation results
 *
 * Used to pass success/failure state from Repository to ViewModel.
 */
sealed class AuthResult {
    /**
     * Operation successful
     * @property user The authenticated user object
     */
    data class Success(val user: FirebaseUser?) : AuthResult()

    /**
     * Operation failed
     * @property message Error description
     */
    data class Error(val message: String) : AuthResult()
}

/**
 * AuthViewModel - ViewModel for Authentication management
 *
 * Handles all user authentication operations including sign-in, sign-up,
 * password reset, Google auth, and phone OTP verification.
 * Also manages admin status verification.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val primeMembershipRepository: PrimeMembershipRepository,
    private val favoritesRepository: FirebaseFavoritesRepository,
    private val cartRepository: FirebaseCartRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    init {
        checkAuthState()
    }

    /**
     * Check current authentication state
     *
     * Verifies if a user is currently logged in via Firebase Auth.
     * Updates [authState] with the user object and signed-in status.
     * Also triggers an admin check if a user is found.
     */

    private fun checkAuthState() {
        val currentUser = authRepository.getCurrentUser()

        // Set initial state with loading
        _authState.value = AuthState(
            isLoading = currentUser != null, // Only loading if we need to check admin
            user = currentUser,
            isSignedIn = currentUser != null,
            isAdmin = false
        )

        // Check if user is admin
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val isAdmin = authRepository.isUserAdmin(currentUser.uid)
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isSignedIn = true,
                        isAdmin = isAdmin
                    )
                    Log.d("AuthViewModel", "User ${currentUser.email} isAdmin: $isAdmin")
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error checking admin status: ${e.message}", e)
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAdmin = false
                    )
                }
            }
        } else {
            _authState.value = _authState.value.copy(
                isLoading = false,
                isSignedIn = false,
                isAdmin = false
            )
        }
    }

    /**
     * Sign in with email and password
     *
     * @param email User's email address
     * @param password User's password
     */
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.signInWithEmail(email, password)) {
                is AuthResult.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        user = result.user,
                        isSignedIn = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Signed in with Email"))
                }
                is AuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _snackBarData.emit(SnackBarData(result.message ,"failed to sign in"))
                }
            }
        }
    }

    /**
     * signUpWithEmail - Register a new user with email and password
     *
     * @param email User's email address
     * @param password User's chosen password
     */
    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.signUpWithEmail(email, password)) {
                is AuthResult.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        user = result.user,
                        isSignedIn = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Signed up with Email"))
                }
                is AuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _snackBarData.emit(SnackBarData(result.message ,"failed to sign up"))
                }
            }
        }
    }

    /**
     * signInWithGoogle - Authenticate user using Google Sign-In
     *
     * @param activity The component activity required for Google Auth client
     */
    fun signInWithGoogle(activity: ComponentActivity) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.signInWithGoogle(activity)) {
                is AuthResult.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        user = result.user,
                        isSignedIn = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Signed in with Google"))
                }

                is AuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _snackBarData.emit(SnackBarData(result.message ,"failed to sign in with Google"))
                }
            }
        }
    }

    /**
     * Sign out the current user
     *
     * Clears local auth state and invokes repository sign out.
     */
    suspend fun signOut() {
        authRepository.signOut()
        cartRepository.clearCart()
        favoritesRepository.clearAllFavorites()
        primeMembershipRepository.clearPrimeMembership()
        _authState.value = AuthState()

        viewModelScope.launch {
            _snackBarData.emit(SnackBarData("You are Signed out"))
        }
    }

    /**
     * clearError - Resets the error state in the UI
     */
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    /**
     * sendOTP - Triggers SMS verification code request
     *
     * @param phoneNumber The user's phone number in E.164 format
     * @param activity The component activity for phone verification
     */
    fun sendOTP(phoneNumber: String, activity: ComponentActivity) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.sendOTP(phoneNumber, activity)) {
                is AuthResult.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("OTP sent to $phoneNumber"))
                }
                is AuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )

                    _snackBarData.emit(SnackBarData(result.message ,"failed to send OTP"))
                }
            }
        }
    }

    /**
     * Verify OTP code
     *
     * Verifies the One-Time Password entered by the user.
     *
     * @param otp The 6-digit code received via SMS
     */
    fun verifyOTP(otp: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.verifyOTP(otp)) {
                is AuthResult.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        user = result.user,
                        isSignedIn = true,
                        error = null
                    )
                }
                is AuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    /**
     * Send password reset email
     *
     * Triggers the forgot password flow via email.
     *
     * @param email The registered email address
     */
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                error = null,
                isEmailSent = false // 💡 Reset state when starting
            )
            forgotPasswordUseCase(email)
                .onSuccess {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isEmailSent = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Password reset email sent to $email Check your inbox. "))
                }
                .onFailure { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(SnackBarData(exception.message ?: "Failed to send reset email"))
                }
        }
    }
}