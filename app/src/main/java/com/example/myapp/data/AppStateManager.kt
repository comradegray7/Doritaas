package com.example.myapp.data

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

// In data/manager/AppStateManager.kt
/**
 * Manages the application's navigation state persistence.
 *
 * This class handles saving and restoring the user's last visited route, ensuring
 * they can resume their session from where they left off, provided the session
 * hasn't expired.
 *
 * @property context Application context used for SharedPreferences.
 */
class AppStateManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("app_navigation_state", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_ROUTE = "last_route"
        private const val KEY_LAST_TIMESTAMP = "last_timestamp"
        private const val TIMEOUT_MILLIS = 30 * 60 * 1000L // 30 minutes
    }


    /**
     * Saves the current navigation route and timestamp.
     *
     * @param currentRoute The route string to save.
     */
    fun saveNavigationState(currentRoute: String) {
        prefs.edit {
            putString(KEY_LAST_ROUTE, currentRoute)
            putLong(KEY_LAST_TIMESTAMP, System.currentTimeMillis())
        }
        Log.d("AppState", "✅ Saved: $currentRoute")
    }


    /**
     * Restores the last saved navigation route if valid.
     *
     * Checks if a saved route exists and if it was saved within the [TIMEOUT_MILLIS] window.
     * If the state is expired or missing, it clears the state and returns null.
     *
     * @return The last saved route string, or null if expired/not found.
     */
    fun restoreNavigationState(): String? {
        val lastRoute = prefs.getString(KEY_LAST_ROUTE, null)
        val timestamp = prefs.getLong(KEY_LAST_TIMESTAMP, 0L)
        val elapsed = System.currentTimeMillis() - timestamp

        return if (lastRoute != null && elapsed < TIMEOUT_MILLIS) {
            Log.d("AppState", " Restored: $lastRoute")
            lastRoute
        } else {
            Log.d("AppState", "❌ State expired or not found")
            clearNavigationState()
            null
        }
    }


    /**
     * Clears the stored navigation state.
     *
     * Removes the last route and timestamp from SharedPreferences.
     */
    fun clearNavigationState() {
        prefs.edit {
            remove(KEY_LAST_ROUTE)
            remove(KEY_LAST_TIMESTAMP)
        }
    }
}