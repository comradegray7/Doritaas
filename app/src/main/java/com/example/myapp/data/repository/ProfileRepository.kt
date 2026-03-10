package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

// Profile Repository interface
/**
 * Interface for User Profile Data Management.
 * 
 * Handles storage and retrieval of extended user information (beyond auth).
 */
interface ProfileRepository {
    /**
     * Get user profile by User ID.
     * @param userId User's unique identifier
     * @return [UserProfile] object or null if not found
     */
    suspend fun getUserProfile(userId: String): UserProfile?

    /**
     * Create a new user profile.
     * @param userId User's unique identifier
     * @param profile Profile data object
     * @return true if successful
     */
    suspend fun createUserProfile(userId: String, profile: UserProfile): Boolean

    /**
     * Listen for real-time updates to a user profile.
     * 
     * @param userId User's unique identifier
     * @param onProfileChanged Callback invoked with updated profile (or null)
     * @return [ListenerRegistration] to remove the listener
     */
    fun getUserProfileRealTime(userId: String, onProfileChanged: (UserProfile?) -> Unit): ListenerRegistration
}

// Profile Repository implementation
/**
 * Implementation of [ProfileRepository] using Firestore.
 * 
 * Stores profiles in the 'users' collection.
 */
class ProfileRepositoryImpl @Inject constructor() : ProfileRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val profilesCollection = firestore.collection(FirestoreCollections.USERS)

    override suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val document = profilesCollection.document(userId).get().await()
            val profile = document.toObject<UserProfile>()
            profile?.copy(id = document.id)
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error getting user profile: $userId", e)
            null
        }
    }

    override suspend fun createUserProfile(userId: String, profile: UserProfile): Boolean {
        return try {
            val profileWithTimestamp = profile.copy(
                id = userId,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                lastLogin = Timestamp.now()
            )

            profilesCollection.document(userId).set(profileWithTimestamp).await()
            Log.d("ProfileRepository", "Profile created successfully for: $userId")
            true
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error creating user profile: $userId", e)
            false
        }
    }

    override fun getUserProfileRealTime(
        userId: String,
        onProfileChanged: (UserProfile?) -> Unit
    ): ListenerRegistration {
        return profilesCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ProfileRepository", "Error listening to user profile", error)
                    onProfileChanged(null)
                    return@addSnapshotListener
                }

                val profile = snapshot?.toObject<UserProfile>()?.copy(id = snapshot.id)
                onProfileChanged(profile)
            }
    }
}