package com.example.myapp.data.repository

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.myapp.R
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.model.AuthResult
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/**
 * AuthRepository
 *
 * Interface defining the contract for authentication operations including email/password,
 * Google Sign-In, and Phone (OTP) authentication.
 */
interface AuthRepository {
    /**
     * Sign in using an email/password pair.
     *
     * Wraps `FirebaseAuth.signInWithEmailAndPassword` and, on success,
     * ensures the user document is upserted in Firestore.
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResult

    /**
     * Register a new account using email/password.
     *
     * Creates the user in Firebase Auth, then persists a basic `User`
     * document in Firestore.
     */
    suspend fun signUpWithEmail(email: String, password: String): AuthResult

    /**
     * Launch the Google Sign‑In flow for the given `activity`.
     *
     * Uses Credential Manager + `GoogleIdTokenCredential` and signs
     * the resulting account into Firebase.
     */
    suspend fun signInWithGoogle(activity: ComponentActivity): AuthResult

    /**
     * Send a password‑reset email for the given address.
     *
     * Returns `Result.success(Unit)` on success or a failure
     * with a user‑friendly error message on common Firebase errors.
     */
    suspend fun sendPasswordResetEmail(email: String) : Result<Unit>

    /**
     * The currently signed‑in Firebase user, or `null` if not authenticated.
     */
    fun getCurrentUser(): FirebaseUser?

    /**
     * Sign the current user out of Firebase Auth.
     *
     * Does not clear any Firestore data; only the auth session.
     */
    fun signOut()

    /**
     * Send an SMS one‑time‑password (OTP) to the given phone number.
     *
     * Uses `PhoneAuthProvider` under the hood and returns:
     * - `Success(null)` when the code is sent and we await user input
     * - `Success(user)` when instant verification/auto‑retrieval succeeds
     * - `Error` on failure.
     */
    suspend fun sendOTP(phoneNumber: String, activity: ComponentActivity): AuthResult

    /**
     * Verify a user‑entered OTP against the previously sent verification ID.
     *
     * On success, signs the user into Firebase and returns `Success(user)`.
     */
    suspend fun verifyOTP(otp: String): AuthResult

    /**
     * Convenience accessor for the current user UID, or `null` if signed out.
     */
    fun getCurrentUserId(): String?

    /**
     * Check whether the given user has admin privileges.
     *
     * Looks up the user document and reads the `isAdmin` flag, returning
     * `false` on any error.
     */
    suspend fun isUserAdmin(userId: String): Boolean

    /**
     * Fetch all user documents from the users collection.
     *
     * Intended for admin views; returns a `Result` so callers can surface
     * errors in the UI.
     */
    suspend fun getAllUsers(): Result<List<User>>
}

/**
 * User - Basic profile snapshot stored in Firestore.
 *
 * Mirrors key fields from `FirebaseUser` plus simple metadata
 * such as provider, creation time, and last login.
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val provider: String = "google",
    val createdAt: Timestamp = Timestamp.now(),
    val lastLogin: Timestamp = Timestamp.now()
)


/**
 * AuthRepositoryImpl
 *
 * Implementation of [AuthRepository] using Firebase Auth and Firestore.
 */
class AuthRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = firestore.collection(FirestoreCollections.USERS)

    private suspend fun saveUserToFirestore(user: FirebaseUser) {
        try {
            // Check if user already exists
            val document = usersCollection.document(user.uid).get().await()

            if (document.exists()) {
                // Update last login time for existing user
                usersCollection.document(user.uid).update("lastLogin", Timestamp.now())
                println("User already exists, updated last login: ${user.uid}")
            } else {
                // Create new user document
                val userData = User(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "",
                    lastLogin = Timestamp.now()
                )

                usersCollection.document(user.uid).set(userData)
                    .addOnSuccessListener {
                        println("New user successfully saved to Firestore: ${user.uid}")
                    }
                    .addOnFailureListener { e ->
                        println("Error saving user to Firestore: ${e.message}")
                    }
            }
        } catch (e: Exception) {
            println("Exception saving user to Firestore: ${e.message}")
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                saveUserToFirestore(user) // This will await the Firestore operation
            }
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign in failed")
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                saveUserToFirestore(user) // This will await the Firestore operation
            }
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign up failed")
        }
    }


    override suspend fun signInWithGoogle(activity: ComponentActivity): AuthResult {
        return try {
            val result = signInWithGoogleCredentialManager(activity)

            // Save user to Firestore after successful sign-in
            if (result is AuthResult.Success && result.user != null) {
                saveUserToFirestore(result.user)
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Google sign in failed: ${e.message}", e)
            AuthResult.Error(e.message ?: "Google sign in failed")
        }
    }

    private suspend fun signInWithGoogleCredentialManager(activity: ComponentActivity): AuthResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                val credentialManager = CredentialManager.create(activity)

                // Get the Web Client ID from string resources
                val webClientId = activity.getString(R.string.web_client_id) // Make sure this exists in your strings.xml

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true) // Auto-select if only one account
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                activity.lifecycleScope.launch {
                    try {
                        val credentialResponse = credentialManager.getCredential(
                            request = request,
                            context = activity
                        )

                        when (val credential = credentialResponse.credential) {
                            is GoogleIdTokenCredential -> {
                                // Direct GoogleIdTokenCredential
                                val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                                signInToFirebase(firebaseCredential, continuation)
                            }

                            is CustomCredential -> {
                                // GoogleIdTokenCredential can come wrapped as a CustomCredential
                                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    try {
                                        val googleIdTokenCredential =
                                            GoogleIdTokenCredential.createFrom(credential.data)
                                        val firebaseCredential = GoogleAuthProvider.getCredential(
                                            googleIdTokenCredential.idToken,
                                            null
                                        )
                                        signInToFirebase(firebaseCredential, continuation)
                                    } catch (e: GoogleIdTokenParsingException) {
                                        Log.e(TAG, "Invalid Google ID token: ${e.message}", e)
                                        if (continuation.isActive) {
                                            continuation.resume(AuthResult.Error("Invalid Google ID token"))
                                        }
                                    }
                                } else {
                                    val errorMsg =
                                        "Unexpected custom credential type: ${credential.type}"
                                    Log.e(TAG, errorMsg)
                                    if (continuation.isActive) {
                                        continuation.resume(AuthResult.Error(errorMsg))
                                    }
                                }
                            }
                            else -> {
                                val errorMsg =
                                    "Unsupported credential type: ${credential::class.java.simpleName}"
                                Log.e(TAG, errorMsg)
                                if (continuation.isActive) {
                                    continuation.resume(AuthResult.Error(errorMsg))
                                }
                            }
                        }

                    } catch (e: GetCredentialException) {
                        val errorMsg = when (e) {
                            is GetCredentialCancellationException -> {
                                "Google Sign-In was cancelled"
                            }
                            is NoCredentialException -> {
                                "No Google accounts available on this device"
                            }
                            is GetCredentialProviderConfigurationException -> {
                                "Google Sign-In is not properly configured"
                            }
                            is GetCredentialUnsupportedException -> {
                                "Google Sign-In is not supported on this device"
                            }
                            else -> {
                                "Google Sign-In failed: ${e.message}"
                            }
                        }
                        Log.e(TAG, "Credential exception: $errorMsg", e)
                        if (continuation.isActive) {
                            continuation.resume(AuthResult.Error(errorMsg))
                        }
                    } catch (e: Exception) {
                        val errorMsg = "Unexpected error during Google Sign-In: ${e.message}"
                        Log.e(TAG, errorMsg, e)
                        if (continuation.isActive) {
                            continuation.resume(AuthResult.Error(errorMsg))
                        }
                    }
                }

                // Handle cancellation
                continuation.invokeOnCancellation {
                    Log.d(TAG, "Google Sign-In coroutine was cancelled")
                }

            } catch (e: Exception) {
                val errorMsg = "Failed to initialize Google Sign-In: ${e.message}"
                Log.e(TAG, errorMsg, e)
                continuation.resume(AuthResult.Error(errorMsg))
            }
        }
    }

    private fun signInToFirebase(
        firebaseCredential: AuthCredential,
        continuation: CancellableContinuation<AuthResult>
    ) {
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Firebase authentication successful")
                    val user = auth.currentUser
                    if (continuation.isActive) {
                        continuation.resume(AuthResult.Success(user))
                    }
                } else {
                    val errorMsg = task.exception?.message ?: "Firebase authentication failed"
                    Log.e(TAG, "Firebase auth failed: $errorMsg")
                    if (continuation.isActive) {
                        continuation.resume(AuthResult.Error(errorMsg))
                    }
                }
            }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {

        return try {
            Log.d(TAG, "Sending password reset email to: $email")

            auth.sendPasswordResetEmail(email).await()

            Log.d(TAG, "Password reset email sent successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error sending password reset email: ${e.message}", e)

            // Handle specific Firebase exceptions
            val errorMessage = when (e) {
                is FirebaseAuthInvalidUserException -> "No account found with this email address"
                is FirebaseAuthInvalidCredentialsException -> "Invalid email address format"
                is FirebaseNetworkException -> "Network error. Please check your connection"
                else -> e.message ?: "Failed to send reset email"
            }

            Result.failure(Exception(errorMessage))
        }
    }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override fun signOut() {
        auth.signOut()
    }

    override fun getCurrentUserId(): String? {
                return auth.currentUser?.uid
    }

    // Store verification ID temporarily
    private var verificationId: String? = null
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null

    override suspend fun sendOTP(phoneNumber: String, activity: ComponentActivity): AuthResult {
        return suspendCancellableCoroutine { continuation ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // This callback is called in two situations:
                    // 1. Instant verification
                    // 2. Auto-retrieval of SMS code
                    Log.d(TAG, "Verification completed automatically")
                    signInWithPhoneCredential(credential, continuation)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    val errorMsg = when (e) {
                        is FirebaseAuthInvalidCredentialsException -> "Invalid phone number format"
                        is FirebaseTooManyRequestsException -> "Too many requests. Try again later"
                        is FirebaseAuthMissingActivityForRecaptchaException -> "reCAPTCHA verification needed"
                        else -> e.message ?: "Phone verification failed"
                    }
                    Log.e(TAG, "Verification failed: $errorMsg", e)
                    if (continuation.isActive) {
                        continuation.resume(AuthResult.Error(errorMsg))
                    }
                }

                override fun onCodeSent(
                    verificationIdResult: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d(TAG, "OTP sent to $phoneNumber")
                    verificationId = verificationIdResult
                    forceResendingToken = token
                    if (continuation.isActive) {
                        continuation.resume(AuthResult.Success(null)) // OTP sent, waiting for user input
                    }
                }
            }

            try {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            } catch (e: Exception) {
                val errorMsg = "Failed to send OTP: ${e.message}"
                Log.e(TAG, errorMsg, e)
                if (continuation.isActive) {
                    continuation.resume(AuthResult.Error(errorMsg))
                }
            }
        }
    }

    override suspend fun verifyOTP(otp: String): AuthResult {
        return try {
            if (verificationId == null) {
                AuthResult.Error("Verification ID not found. Please request OTP again.")
            } else {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
                suspendCancellableCoroutine { continuation: CancellableContinuation<AuthResult> ->
                    signInWithPhoneCredential(credential, continuation)
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "OTP verification failed")
        }
    }

    private fun signInWithPhoneCredential(
        credential: PhoneAuthCredential,
        continuation: CancellableContinuation<AuthResult>
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Phone sign in successful")
                    val user = task.result.user
                    if (continuation.isActive) {
                        continuation.resume(AuthResult.Success(user))
                    }
                } else {
                    val errorMsg = task.exception?.message ?: "Phone sign in failed"
                    Log.e(TAG, "Phone sign in failed: $errorMsg")
                    if (continuation.isActive) {
                        continuation.resume(AuthResult.Error(errorMsg))
                    }
                }
            }
    }

    override suspend fun isUserAdmin(userId: String): Boolean {
        return try {
            val document = usersCollection.document(userId).get().await()
            document.getBoolean("isAdmin") ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking admin status: ${e.message}", e)
            false
        }
    }

    override suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = usersCollection.get().await()
            val users = snapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all users: ${e.message}", e)
            Result.failure(e)
        }
    }
}

suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.exception != null) {
                cont.resumeWithException(task.exception!!)
            } else {
                cont.resume(task.result)
            }
        }
    }
}

