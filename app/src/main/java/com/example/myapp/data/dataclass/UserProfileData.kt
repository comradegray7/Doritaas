package com.example.myapp.data.dataclass

import com.google.firebase.Timestamp

/**
 * UserProfile - Extended user profile information
 * 
 * Stores detailed user information for the profile screen, separate from auth credentials.
 * stored in Firestore 'users' collection.
 * 
 * @property id Unique user identifier (matches Auth UID)
 * @property fullName Full legal name
 * @property displayName Public display name
 * @property email Contact email
 * @property phone Contact phone number
 * @property photoUrl Profile picture URL
 * @property bio Short user biography
 * @property lastLogin Timestamp of last activity
 * @property createdAt Timestamp of account creation
 * @property updatedAt Timestamp of last profile update
 */
data class UserProfile(
    val id: String = "",
    val fullName: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String? = "",
    val bio: String = "",
    val lastLogin: Timestamp? = null,  
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {

    //  Helper function to check if profile is empty
    /**
     * Checks whether the profile contains any identifying information.
     */
    fun isEmpty(): Boolean {
        return displayName.isBlank() &&
                fullName.isBlank() &&
                email.isBlank() &&
                phone.isBlank()
    }

    //  Helper to get best display name
    /**
     * Retrieves the most appropriate name to display for the user,
     * falling back from display name to full name, then email prefix, then phone.
     */
    fun getBestDisplayName(): String {
        return displayName.takeIf { it.isNotBlank() }
            ?: fullName.takeIf { it.isNotBlank() }
            ?: email.takeIf { it.isNotBlank() }?.substringBefore("@")
            ?: phone.takeIf { it.isNotBlank() }
            ?: "User"
    }
}
