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
import com.example.myapp.data.dataclass.UserProfile
import com.example.myapp.data.model.AuthResponse
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repository contract for authentication and user profile administration.
 *
 * Implementations are responsible for Firebase Auth provider flows, current-session access,
 * password reset requests, and privileged Firestore user-management operations.
 */
interface AuthRepository {
    /**
     * Signs in an existing Firebase Auth user with email/password credentials.
     *
     * @param email User email address.
     * @param password User password.
     * @return [AuthResponse.Success] with the signed-in user, or [AuthResponse.Error].
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResponse

    /**
     * Creates a new Firebase Auth user with email/password credentials.
     *
     * @param email Email address for the new account.
     * @param password Password for the new account.
     * @return [AuthResponse.Success] with the created user, or [AuthResponse.Error].
     */
    suspend fun signUpWithEmail(email: String, password: String): AuthResponse

    /**
     * Initiates Google Sign-In through Android Credential Manager.
     *
     * @param activity Activity context required by Credential Manager.
     * @return [AuthResponse.Success] with the signed-in Firebase user, or [AuthResponse.Error].
     */
    suspend fun signInWithGoogle(activity: ComponentActivity): AuthResponse

    /**
     * Sends a Firebase password reset email.
     *
     * @param email Account email that should receive the reset link.
     * @return Success when Firebase accepts the request, otherwise a failure with a user-facing error.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Returns the current Firebase Auth user from local session state.
     *
     * @return Current [FirebaseUser], or null when signed out.
     */
    fun getCurrentUser(): FirebaseUser?

    /**
     * Signs out the current Firebase Auth session.
     */
    fun signOut()


    /**
     * Returns the UID for the current Firebase user.
     *
     * @return Current user id, or null when signed out.
     */
    fun getCurrentUserId(): String?

    /**
     * Checks whether a Firestore user document has admin privileges.
     *
     * @param userId Firebase UID to inspect.
     * @return True when the user is marked admin.
     */
    suspend fun isUserAdmin(userId: String): Boolean

    /**
     * Checks whether a Firestore user document has superAdmin privileges.
     *
     * @param userId Firebase UID to inspect.
     * @return True when the user is marked superAdmin.
     */
    suspend fun isUserSuperAdmin(userId: String): Boolean

    /**
     * Retrieves all Firestore user profile documents.
     *
     * @return Result containing user profiles, or a failure from Firestore.
     */
    suspend fun getAllUsers(): Result<List<UserProfile?>>

    /**
     * Searches Firestore user profiles by full name or email.
     *
     * @param query Case-insensitive search query.
     * @return Result containing matching profiles, or a failure from Firestore.
     */
    suspend fun searchUsers(query: String): Result<List<UserProfile>>

    /**
     * Updates a user's admin role flag.
     *
     * @param userId Firebase UID of the target profile.
     * @param makeAdmin True to promote, false to demote.
     * @return Success when the Firestore update completes.
     */
    suspend fun toggleAdminStatus(userId: String, makeAdmin: Boolean): Result<Unit>

    /**
     * Deletes a user's Firestore profile document.
     *
     * This does not delete the Firebase Auth account; that requires server-side Admin SDK access.
     *
     * @param userId Firebase UID of the target profile.
     * @return Success when the Firestore delete completes.
     */
    suspend fun deleteUser(userId: String): Result<Unit>

    /**
     * Updates selected fields in a Firestore user profile.
     *
     * @param userId Firebase UID of the target profile.
     * @param profileUpdates Firestore field/value map to update.
     * @return Success when the Firestore update completes.
     */
    suspend fun updateUserProfile(userId: String, profileUpdates: Map<String, Any>): Result<Unit>
}

/**
 * Implementation of [AuthRepository] utilizing Firebase Authentication and Cloud Firestore.
 *
 * @param firestore Firestore instance used for user document management.
 */
class AuthRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
        private const val FIELD_ADMIN       = "admin"
        private const val FIELD_SUPER_ADMIN = "superAdmin"
    }

    private val auth            = FirebaseAuth.getInstance()
    private val usersCollection = firestore.collection(FirestoreCollections.USERS)

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Saves or updates the user profile in Firestore upon successful authentication.
     *
     * @param user The authenticated Firebase user.
     */
    private suspend fun saveUserToFirestore(user: FirebaseUser) {
        try {
            val document = usersCollection.document(user.uid).get().await()
            if (document.exists()) {
                usersCollection.document(user.uid)
                    .update("lastLogin", Timestamp.now())
                    .await()
                Log.d(TAG, "Updated last login for: ${user.uid}")
            } else {
                val userData = UserProfile(
                    id          = user.uid,
                    email       = user.email ?: "",
                    displayName = user.displayName ?: "",
                    lastLogin   = Timestamp.now()
                )
                usersCollection.document(user.uid).set(userData).await()
                Log.d(TAG, "New user saved: ${user.uid}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception saving user to Firestore: ${e.message}", e)
        }
    }

    // -------------------------------------------------------------------------
    // Auth operations
    // -------------------------------------------------------------------------

    override suspend fun signInWithEmail(email: String, password: String): AuthResponse {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user   = result.user
            if (user != null) saveUserToFirestore(user)
            AuthResponse.Success(user)
        } catch (e: Exception) {
            AuthResponse.Error(e.message ?: "Sign in failed")
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): AuthResponse {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user   = result.user
            if (user != null) saveUserToFirestore(user)
            AuthResponse.Success(user)
        } catch (e: Exception) {
            AuthResponse.Error(e.message ?: "Sign up failed")
        }
    }

    override suspend fun signInWithGoogle(activity: ComponentActivity): AuthResponse {
        return try {
            val result = signInWithGoogleCredentialManager(activity)
            if (result is AuthResponse.Success && result.user != null) {
                saveUserToFirestore(result.user)
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Google sign in failed: ${e.message}", e)
            AuthResponse.Error(e.message ?: "Google sign in failed")
        }
    }

    /**
     * Internal implementation of Google Sign-In using Android's Credential Manager.
     */
    private suspend fun signInWithGoogleCredentialManager(activity: ComponentActivity): AuthResponse {
        return suspendCancellableCoroutine { continuation ->
            try {
                val credentialManager = CredentialManager.create(activity)
                val webClientId       = activity.getString(R.string.web_client_id)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true)
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
                                val firebaseCredential =
                                    GoogleAuthProvider.getCredential(credential.idToken, null)
                                signInToFirebase(firebaseCredential, continuation)
                            }
                            is CustomCredential -> {
                                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    try {
                                        val googleIdTokenCredential =
                                            GoogleIdTokenCredential.createFrom(credential.data)
                                        val firebaseCredential = GoogleAuthProvider.getCredential(
                                            googleIdTokenCredential.idToken, null
                                        )
                                        signInToFirebase(firebaseCredential, continuation)
                                    } catch (e: GoogleIdTokenParsingException) {
                                        Log.e(TAG, "Invalid Google ID token: ${e.message}", e)
                                        if (continuation.isActive)
                                            continuation.resume(AuthResponse.Error("Invalid Google ID token"))
                                    }
                                } else {
                                    val msg = "Unexpected credential type: ${credential.type}"
                                    Log.e(TAG, msg)
                                    if (continuation.isActive)
                                        continuation.resume(AuthResponse.Error(msg))
                                }
                            }
                            else -> {
                                val msg = "Unsupported credential: ${credential::class.java.simpleName}"
                                Log.e(TAG, msg)
                                if (continuation.isActive)
                                    continuation.resume(AuthResponse.Error(msg))
                            }
                        }
                    } catch (e: GetCredentialException) {
                        val msg = when (e) {
                            is GetCredentialCancellationException         -> "Google Sign-In was cancelled"
                            is NoCredentialException                      -> "No Google accounts available on this device"
                            is GetCredentialProviderConfigurationException -> "Google Sign-In is not properly configured"
                            is GetCredentialUnsupportedException          -> "Google Sign-In is not supported on this device"
                            else                                           -> "Google Sign-In failed: ${e.message}"
                        }
                        Log.e(TAG, "Credential exception: $msg", e)
                        if (continuation.isActive) continuation.resume(AuthResponse.Error(msg))
                    } catch (e: Exception) {
                        val msg = "Unexpected error during Google Sign-In: ${e.message}"
                        Log.e(TAG, msg, e)
                        if (continuation.isActive) continuation.resume(AuthResponse.Error(msg))
                    }
                }
                continuation.invokeOnCancellation {
                    Log.d(TAG, "Google Sign-In coroutine cancelled")
                }
            } catch (e: Exception) {
                val msg = "Failed to initialize Google Sign-In: ${e.message}"
                Log.e(TAG, msg, e)
                continuation.resume(AuthResponse.Error(msg))
            }
        }
    }

    /**
     * Signs into Firebase using the provided [AuthCredential].
     */
    private fun signInToFirebase(
        firebaseCredential: AuthCredential,
        continuation: CancellableContinuation<AuthResponse>
    ) {
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (continuation.isActive)
                        continuation.resume(AuthResponse.Success(auth.currentUser))
                } else {
                    val msg = task.exception?.message ?: "Firebase authentication failed"
                    Log.e(TAG, "Firebase auth failed: $msg")
                    if (continuation.isActive) continuation.resume(AuthResponse.Error(msg))
                }
            }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val message = when (e) {
                is FirebaseAuthInvalidUserException        -> "No account found with this email address"
                is FirebaseAuthInvalidCredentialsException -> "Invalid email address format"
                is FirebaseNetworkException                -> "Network error. Please check your connection"
                else                                       -> e.message ?: "Failed to send reset email"
            }
            Result.failure(Exception(message))
        }
    }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser
    override fun signOut() = auth.signOut()
    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    // -------------------------------------------------------------------------
    // Admin / User Management
    // -------------------------------------------------------------------------

    override suspend fun isUserAdmin(userId: String): Boolean {
        return try {
            val doc = usersCollection.document(userId).get().await()
            doc.getBoolean(FIELD_ADMIN) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking admin status: ${e.message}", e)
            false
        }
    }

    override suspend fun isUserSuperAdmin(userId: String): Boolean {
        return try {
            val doc = usersCollection.document(userId).get().await()
            doc.getBoolean(FIELD_SUPER_ADMIN) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking superAdmin status: ${e.message}", e)
            false
        }
    }

    override suspend fun getAllUsers(): Result<List<UserProfile?>> {
        return try {
            val snapshot = usersCollection.get().await()
            Result.success(snapshot.toObjects(UserProfile::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching users: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): Result<List<UserProfile>> {
        return try {
            val snapshot = usersCollection.get().await()
            val filtered = snapshot.documents
                .mapNotNull { it.toObject(UserProfile::class.java) }
                .filter { user ->
                    user.fullName.contains(query, ignoreCase = true) ||
                            user.email.contains(query, ignoreCase = true)
                }
            Result.success(filtered)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun toggleAdminStatus(userId: String, makeAdmin: Boolean): Result<Unit> {
        return try {
            usersCollection.document(userId).update(FIELD_ADMIN, makeAdmin).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(userId: String, profileUpdates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(userId).update(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// -------------------------------------------------------------------------
// Task.await() extension
// -------------------------------------------------------------------------

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
