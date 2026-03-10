package com.example.myapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapp.data.dataclass.SearchData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Extension property - MUST be at top level (outside class)
private val Context.popularSearchDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "popular_search_preferences"
)

/**
 * Smart Popular Search DataStore implementation that ranks searches by frequency and recency.
 *
 * This store maintains a history of searches and calculates a relevance score based on:
 * - Count: How many times a query has been searched.
 * - Recency: How recently the query was searched, applying a time decay factor.
 *
 * The score determines the order of popular searches returned to the user.
 *
 * @property context Application context.
 */
@Singleton
class SmartPopularSearchDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private val SEARCH_DATA_KEY = stringPreferencesKey("search_data")
        private const val MAX_POPULAR_SEARCHES = 8
        private const val TIME_DECAY_DAYS = 30
    }

    //  Create Json instance with proper configuration
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val dataStore: DataStore<Preferences>
        get() = context.popularSearchDataStore

    /**
     * A Flow emitting a list of popular search terms, ranked by score.
     * The score is calculated as (count * recencyWeight).
     */
    val popularSearches: Flow<List<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val dataJson = preferences[SEARCH_DATA_KEY] ?: "[]"

            val searchDataList = try {
                json.decodeFromString<List<SearchData>>(dataJson)  
            } catch (_: Exception) {
                emptyList()
            }

            val currentTime = System.currentTimeMillis()
            val dayInMillis = 24 * 60 * 60 * 1000L

            searchDataList
                .map { data ->
                    val daysSinceSearch = (currentTime - data.lastSearched) / dayInMillis
                    val recencyWeight = if (daysSinceSearch < TIME_DECAY_DAYS) {
                        1.0 - (daysSinceSearch.toDouble() / TIME_DECAY_DAYS)
                    } else {
                        0.1
                    }

                    val score = data.count * recencyWeight
                    data.query to score
                }
                .sortedByDescending { it.second }
                .take(MAX_POPULAR_SEARCHES)
                .map { it.first }
        }


    /**
     * Records a search query, updating its frequency count and last searched timestamp.
     *
     * If the query exists, it updates the existing entry. If not, it creates a new one.
     *
     * @param query The search term to record.
     */
    suspend fun recordSearch(query: String) {
        if (query.isBlank()) return

        dataStore.edit { preferences ->
            val dataJson = preferences[SEARCH_DATA_KEY] ?: "[]"

            val searchDataList = try {
                json.decodeFromString<List<SearchData>>(dataJson).toMutableList()
            } catch (_: Exception) {
                mutableListOf()
            }

            val existingIndex = searchDataList.indexOfFirst { it.query.equals(query, ignoreCase = true) }
            val currentTime = System.currentTimeMillis()

            if (existingIndex >= 0) {
                val existing = searchDataList[existingIndex]
                searchDataList[existingIndex] = SearchData(
                    query = query,
                    count = existing.count + 1,
                    lastSearched = currentTime
                )
            } else {
                searchDataList.add(
                    SearchData(
                        query = query,
                        count = 1,
                        lastSearched = currentTime
                    )
                )
            }

            preferences[SEARCH_DATA_KEY] = json.encodeToString(searchDataList) 
        }
    }

}