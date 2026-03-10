package com.example.myapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException

// RecentSearchDataStore.kt
/**
 * Handles persistent storage of recent search queries using DataStore.
 *
 * This class keeps track of the user's localized recent search history,
 * storing a limited list of queries directly on the device.
 *
 * @property context Application context for accessing DataStore.
 */
class RecentSearchDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private val RECENT_SEARCHES_KEY = stringPreferencesKey("recent_searches")
        private const val MAX_SEARCHES = 10
    }

    private val dataStore = context.dataStore

    /**
     * A Flow emitting the list of recent search queries.
     * Emits an empty list if no data exists or on IO errors.
     */
    val recentSearches: Flow<List<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val searchesJson = preferences[RECENT_SEARCHES_KEY] ?: "[]"
            Json.decodeFromString<List<String>>(searchesJson)
        }


    /**
     * Saves a search query to the recent history.
     *
     * Adds the new query to the top of the list. If it already exists, it is moved to the top.
     * The list is truncated to [MAX_SEARCHES] items.
     *
     * @param query The search query to save.
     */
    suspend fun saveSearch(query: String) {
        if (query.isBlank()) return

        dataStore.edit { preferences ->
            val currentSearchesJson = preferences[RECENT_SEARCHES_KEY] ?: "[]"
            val currentSearches = Json.decodeFromString<List<String>>(currentSearchesJson).toMutableList()

            // Remove if exists (to move to top)
            currentSearches.remove(query)

            // Add to beginning
            currentSearches.add(0, query)

            // Keep only max searches
            val trimmedSearches = currentSearches.take(MAX_SEARCHES)

            preferences[RECENT_SEARCHES_KEY] = Json.encodeToString(trimmedSearches)
        }
    }


    /**
     * Removes a specific search query from the history.
     *
     * @param query The search query to remove.
     */
    suspend fun removeSearch(query: String) {
        dataStore.edit { preferences ->
            val currentSearchesJson = preferences[RECENT_SEARCHES_KEY] ?: "[]"
            val currentSearches = Json.decodeFromString<List<String>>(currentSearchesJson).toMutableList()

            currentSearches.remove(query)

            preferences[RECENT_SEARCHES_KEY] = Json.encodeToString(currentSearches)
        }
    }


    /**
     * Clears all recorded recent searches.
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(RECENT_SEARCHES_KEY)
        }
    }
}

// Extension for Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "search_settings")

