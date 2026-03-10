package com.example.myapp.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.dataclass.UserProfile
import com.example.myapp.data.repository.ProfileRepository
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



/**
 * ProfileViewModel - ViewModel for User Profile Management
 * 
 * Manages fetching, creating, and updating user profile data.
 * Supports both one-time fetching and real-time updates for the profile UI.
 * 
 * ## Dependencies
 * - ProfileRepository: Data source for profile operations
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)

    private var realtimeListener: ListenerRegistration? = null

    /**
     * Load user profile
     * 
     * Fetches the user profile from Firestore once.
     * Updates [userProfile] state if successful.
     * 
     * @param userId The unique ID of the user (from Auth)
     */
    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                println("Loading profile for userId: $userId")
                val profile = profileRepository.getUserProfile(userId)

                println("Loaded profile from Firestore: $profile")

                // ✅ Only set profile if it's not empty
                if (profile != null && !profile.isEmpty()) {
                    _userProfile.value = profile
                    println("Profile set successfully: ${profile.displayName}")
                } else {
                    println("Profile is empty or null, keeping current state")
                    // Don't override with empty profile
                }
            } catch (e: Exception) {
                println("Error loading profile: ${e.message}")
                _error.value = e.message ?: "Failed to load profile"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create a new user profile document.
     *
     * Persists initial profile data for a user and updates local state on success.
     *
     * @param userId Auth user ID to own this profile.
     * @param profile Profile fields to write to Firestore.
     */
    fun createProfile(userId: String, profile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                println("Creating profile for userId: $userId")
                println("Profile data: $profile")

                // ✅ Use createUserProfile method from your repository
                val success = profileRepository.createUserProfile(userId, profile)

                if (success) {
                    _userProfile.value = profile
                    println("Profile created successfully")
                } else {
                    _error.value = "Failed to create profile"
                    println("Profile creation failed")
                }
            } catch (e: Exception) {
                println("Error creating profile: ${e.message}")
                _error.value = e.message ?: "Failed to create profile"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Start listening for real-time profile updates
     * 
     * Sets up a Firestore snapshot listener to automatically update local state
     * when the profile changes on the server.
     * 
     * @param userId The unique ID of the user to watch
     */
    fun startRealtimeUpdates(userId: String) {
        stopRealtimeUpdates() // Stop any existing listener

        // ✅ Use getUserProfileRealTime method from your repository
        realtimeListener = profileRepository.getUserProfileRealTime(userId) { profile ->
            println("Realtime update received: $profile")

            // ✅ Only update if profile is not empty
            if (profile != null && !profile.isEmpty()) {
                _userProfile.value = profile
            }
        }
    }

    /**
     * Stop listening for real-time profile updates.
     *
     * Cleans up any active Firestore listener and releases its resources.
     */
    fun stopRealtimeUpdates() {
        realtimeListener?.remove()
        realtimeListener = null
    }

    /**
     * Clear the in-memory profile state.
     *
     * Resets the current profile and error so the UI can return to an initial state.
     */
    fun clearProfile() {
        _userProfile.value = null
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopRealtimeUpdates()
    }

}