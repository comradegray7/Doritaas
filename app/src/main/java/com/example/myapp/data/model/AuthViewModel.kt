package com.example.myapp.data.model

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.ForgotPasswordUseCase
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.dataclass.UserProfile
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Coordinates authentication UI state, sign-in/sign-up workflows, and admin user management.
 *
 * The ViewModel wraps [AuthRepository] operations in lifecycle-aware coroutines and exposes
 * immutable [authState] and one-shot [snackBarData] streams for Compose screens. It also owns
 * client-side guards for user-management actions so the UI can fail fast before repository calls.
 *
 * @property authRepository Data source for Firebase Auth and user profile operations.
 * @property forgotPasswordUseCase Use case for password reset email requests.
 * @property primeMembershipRepository Cache/source for Prime membership state cleared on sign-out.
 * @property favoritesRepository Favorites cache cleared on sign-out.
 * @property cartRepository Cart cache cleared on sign-out.
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

    // =========================================================================
    // Auth state initialisation
    // =========================================================================

    /**
     * Called once on ViewModel creation. Reads the current Firebase Auth session
     * and resolves admin / superAdmin status from Firestore in a single coroutine
     * so there is no intermediate state where isLoading flips more than once.
     *
     * Does NOT pre-load the user list — screens that need it call loadAllUsers()
     * themselves, preventing unnecessary Firestore reads on every app launch.
     */
    private fun checkAuthState() {
        val currentUser = authRepository.getCurrentUser()

        if (currentUser == null) {
            _authState.update {
                AuthState(
                    isLoading     = false,
                    isSignedIn    = false,
                    isInitialized = true
                )
            }
            return
        }

        // User is present — mark as loading until role check completes
        _authState.update {
            AuthState(
                isLoading     = true,
                user          = currentUser,
                isSignedIn    = true,
                isInitialized = false
            )
        }

        viewModelScope.launch {
            try {
                val admin      = authRepository.isUserAdmin(currentUser.uid)
                val superAdmin = authRepository.isUserSuperAdmin(currentUser.uid)

                _authState.update {
                    it.copy(
                        isLoading     = false,
                        admin         = admin,
                        superAdmin    = superAdmin,
                        isInitialized = true
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Role check failed: ${e.message}", e)
                _authState.update {
                    it.copy(
                        isLoading     = false,
                        admin         = false,
                        superAdmin    = false,
                        isInitialized = true
                    )
                }
            }
        }
    }

    // =========================================================================
    // User list management (admin / superAdmin screens only)
    // =========================================================================

    /**
     * Fetches all users. Safe to call multiple times — the admin guard runs
     * both here (fast client-side check) and in the repository (server-side).
     */
    fun loadAllUsers() {
        if (!_authState.value.admin && !_authState.value.superAdmin) {
            viewModelScope.launch {
                _snackBarData.emit(
                    SnackBarData("Permission denied: admin access required", isError = true)
                )
            }
            return
        }

        viewModelScope.launch {
//            _authState.update { it.copy(isLoading = true, error = null) }

            authRepository.getAllUsers()
                .onSuccess { users ->
                    _authState.update { it.copy(isLoading = false, users = users, error = null) }
                }
                .onFailure { e ->
                    _authState.update { it.copy(isLoading = false, error = e.message) }
                    _snackBarData.emit(
                        SnackBarData(e.message ?: "Failed to load users", isError = true)
                    )
                }
        }
    }

    /**
     * Client-side search over the already-loaded user list — no extra Firestore
     * reads. The repository's searchUsers() does a full collection scan each
     * call which is expensive; filter locally instead since we already have
     * the list in state.
     *
     * Falls back to loadAllUsers() when the query is cleared.
     */
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            loadAllUsers()
            return
        }
        if (!_authState.value.admin && !_authState.value.superAdmin) return

        val q = query.trim()
        val filtered = _authState.value.users.filter { user ->
            user?.fullName?.contains(q, ignoreCase = true)    == true ||
            user?.email?.contains(q, ignoreCase = true)       == true ||
            user?.displayName?.contains(q, ignoreCase = true) == true
        }
        _authState.update { it.copy(users = filtered, error = null) }
    }

    /**
     * Promotes or demotes a user's admin role.
     *
     * Client-side guards mirror the server-side guards in the repository so
     * the UI can show a meaningful error before a round-trip to Firestore.
     */
    fun toggleAdminStatus(userId: String, makeAdmin: Boolean) {
        val currentState = _authState.value

        // Self-demotion
        if (userId == currentState.user?.uid) {
            viewModelScope.launch {
                _snackBarData.emit(
                    SnackBarData("You cannot change your own admin role", isError = true)
                )
            }
            return
        }

        // Targeting a superAdmin
        val targetUser = currentState.users.find { it?.id == userId }
        if (targetUser?.superAdmin == true) {
            viewModelScope.launch {
                _snackBarData.emit(
                    SnackBarData("SuperAdmin accounts cannot be modified", isError = true)
                )
            }
            return
        }

        // Regular admin trying to demote another admin
        if (targetUser?.admin == true && !currentState.superAdmin) {
            viewModelScope.launch {
                _snackBarData.emit(
                    SnackBarData("Only a SuperAdmin can modify another admin's role", isError = true)
                )
            }
            return
        }

        viewModelScope.launch {
            authRepository.toggleAdminStatus(userId, makeAdmin)
                .onSuccess {
                    loadAllUsers()
                    _snackBarData.emit(
                        SnackBarData(
                            if (makeAdmin) "User promoted to Admin"
                            else "Admin privileges revoked"
                        )
                    )
                }
                .onFailure { e ->
                    _snackBarData.emit(
                        SnackBarData(e.message ?: "Failed to update admin status", isError = true)
                    )
                }
        }
    }

    /**
     * Deletes a user's Firestore document (superAdmin only).
     *
     * IMPORTANT: This removes the Firestore profile document but does NOT
     * delete the Firebase Auth account. Full account deletion requires a
     * Cloud Function with the Admin SDK. Wire one up if you need hard deletes.
     */
    fun deleteUser(userId: String) {
        val currentState = _authState.value

        if (!currentState.superAdmin) {
            viewModelScope.launch {
                _snackBarData.emit(
                    SnackBarData("Only a SuperAdmin can delete users", isError = true)
                )
            }
            return
        }
        if (userId == currentState.user?.uid) {
            viewModelScope.launch {
                _snackBarData.emit(
                    SnackBarData("You cannot delete your own account", isError = true)
                )
            }
            return
        }

        val targetUser = currentState.users.find { it?.id == userId }
        if (targetUser?.superAdmin == true) {
            viewModelScope.launch {
                _snackBarData.emit(
                    SnackBarData("SuperAdmin accounts cannot be deleted", isError = true)
                )
            }
            return
        }

        val userName = targetUser?.fullName ?: "User"

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }

            authRepository.deleteUser(userId)
                .onSuccess {
                    loadAllUsers()
                    _snackBarData.emit(SnackBarData("$userName has been deleted"))
                }
                .onFailure { e ->
                    _authState.update { it.copy(isLoading = false) }
                    _snackBarData.emit(
                        SnackBarData(e.message ?: "Failed to delete user", isError = true)
                    )
                }
        }
    }

    // =========================================================================
    // NOTE — addUser() is intentionally omitted
    // =========================================================================
    //
    // Firebase's `createUserWithEmailAndPassword()` on the client SDK immediately
    // signs OUT the currently signed-in admin and signs IN as the new account.
    // There is no safe way to create users from the client while staying signed in.
    //
    // Solution: implement a Firebase Cloud Function using the Admin SDK:
    //
    //   exports.createUser = functions.https.onCall(async (data, context) => {
    //     if (!context.auth?.token?.superAdmin) throw new functions.https.HttpsError(...)
    //     const user = await admin.auth().createUser({ email, password })
    //     await admin.firestore().collection('users').doc(user.uid).set({ ... })
    //     return { uid: user.uid }
    //   })
    //
    // Then call it from the ViewModel with Firebase Functions SDK.

    // =========================================================================
    // Authentication
    // =========================================================================

    /**
     * Signs in with an email/password credential and refreshes the caller's role flags.
     *
     * @param email Email address entered by the user.
     * @param password Password entered by the user.
     */
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.signInWithEmail(email, password)) {
                is AuthResponse.Success -> {
                    val uid        = result.user?.uid
                    val admin      = uid?.let { authRepository.isUserAdmin(it) } ?: false
                    val superAdmin = uid?.let { authRepository.isUserSuperAdmin(it) } ?: false

                    _authState.update {
                        it.copy(
                            isLoading  = false,
                            user       = result.user,
                            isSignedIn = true,
                            admin      = admin,
                            superAdmin = superAdmin,
                            error      = null
                        )
                    }
                    _snackBarData.emit(SnackBarData("Welcome back!"))
                }
                is AuthResponse.Error -> {
                    _authState.update { it.copy(isLoading = false, error = result.message) }
                    _snackBarData.emit(SnackBarData(result.message, isError = true))
                }
            }
        }
    }

    /**
     * Creates a standard customer account with email/password credentials.
     *
     * New accounts are explicitly treated as non-admin users until a privileged user promotes
     * them through the user-management flow.
     *
     * @param email Email address for the new Firebase Auth account.
     * @param password Password for the new Firebase Auth account.
     */
    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.signUpWithEmail(email, password)) {
                is AuthResponse.Success -> {
                    // New sign-ups are never admins or superAdmins
                    _authState.update {
                        it.copy(
                            isLoading  = false,
                            user       = result.user,
                            isSignedIn = true,
                            admin      = false,
                            superAdmin = false,
                            error      = null
                        )
                    }
                }
                is AuthResponse.Error -> {
                    _authState.update { it.copy(isLoading = false, error = result.message) }
                    _snackBarData.emit(SnackBarData(result.message, isError = true))
                }
            }
        }
    }

    /**
     * Starts Google sign-in through the repository and resolves admin role flags on success.
     *
     * @param activity Host activity required by Android Credential Manager.
     */
    fun signInWithGoogle(activity: ComponentActivity) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.signInWithGoogle(activity)) {
                is AuthResponse.Success -> {
                    val uid        = result.user?.uid
                    val admin      = uid?.let { authRepository.isUserAdmin(it) } ?: false
                    val superAdmin = uid?.let { authRepository.isUserSuperAdmin(it) } ?: false

                    _authState.update {
                        it.copy(
                            isLoading  = false,
                            user       = result.user,
                            isSignedIn = true,
                            admin      = admin,
                            superAdmin = superAdmin,
                            error      = null
                        )
                    }
                    _snackBarData.emit(SnackBarData("Signed in with Google"))
                }
                is AuthResponse.Error -> {
                    _authState.update { it.copy(isLoading = false, error = result.message) }
                    _snackBarData.emit(SnackBarData(result.message, isError = true))
                }
            }
        }
    }

    /**
     * Sends a password reset email and updates [AuthState.isEmailSent] on success.
     *
     * @param email Account email that should receive the reset link.
     */
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null, isEmailSent = false) }

            forgotPasswordUseCase(email)
                .onSuccess {
                    _authState.update { it.copy(isLoading = false, isEmailSent = true, error = null) }
                    _snackBarData.emit(
                        SnackBarData("Password reset email sent to $email")
                    )
                }
                .onFailure { e ->
                    _authState.update { it.copy(isLoading = false, error = e.message) }
                    _snackBarData.emit(
                        SnackBarData(e.message ?: "Failed to send reset email", isError = true)
                    )
                }
        }
    }

    /**
     * Signs out and clears all local caches.
     * Non-suspend — launches its own coroutine for async cleanup.
     */
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            cartRepository.clearCart()
            favoritesRepository.clearAllFavorites()
            primeMembershipRepository.clearPrimeMembership()
            _authState.value = AuthState(isLoading = false, isInitialized = true)
            _snackBarData.emit(SnackBarData("You have been signed out"))
        }
    }

    /**
     * Clears the current auth error without changing any session or loading state.
     */
    fun clearError() {
        _authState.update { it.copy(error = null) }
    }
}

/**
 * Snapshot of authentication and user-management state consumed by Compose screens.
 *
 * @property isLoading Whether an auth or user-management request is currently running.
 * @property isInitialized Whether the initial Firebase session/role lookup has completed.
 * @property isSignedIn Whether a Firebase user is currently signed in.
 * @property isEmailSent Whether a password reset email was sent successfully.
 * @property user Currently signed-in Firebase user, if any.
 * @property users Loaded Firestore user profiles for admin management screens.
 * @property error Last user-facing error message, if any.
 * @property admin Whether the current signed-in user has admin privileges.
 * @property superAdmin Whether the current signed-in user has superAdmin privileges.
 */
data class AuthState(
    val isLoading: Boolean = true,
    val isInitialized: Boolean = false,
    val isSignedIn: Boolean = false,
    val isEmailSent: Boolean = false,
    val user: FirebaseUser? = null,
    val users: List<UserProfile?> = emptyList(),
    val error: String? = null,
    // Role flags for the currently signed-in user
    val admin: Boolean = false,
    val superAdmin: Boolean = false
) {
    /**
     * Convenience alias used by user-management UI to check superAdmin privileges.
     */
    val currentUserIsSuperAdmin: Boolean get() = superAdmin
}

/**
 * Result wrapper returned by repository authentication operations.
 */
sealed class AuthResponse {
    /**
     * Successful authentication result.
     *
     * @property user Firebase user returned by the auth provider. This can be null for flows
     * that complete an intermediate step, such as sending a phone OTP.
     */
    data class Success(val user: FirebaseUser?) : AuthResponse()

    /**
     * Failed authentication result.
     *
     * @property message User-facing error message.
     */
    data class Error(val message: String) : AuthResponse()
}
