package com.example.myapp.data.model

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.RecentSearchDataStore
import com.example.myapp.data.SmartPopularSearchDataStore
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.repository.ProductCrudRepository
import com.example.myapp.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * SearchViewModel - ViewModel for Search Functionality
 * 
 * Handles text, voice, and image-based product searching.
 * Manages recent and popular search history using DataStores.
 * 
 * ## Dependencies
 * - ProductRepository: For executing text-based product searches
 * - SearchRepository: For handling image processing and ML-based search
 * - RecentSearchDataStore: Persists user's recent search queries
 * - SmartPopularSearchDataStore: Tracks and provides popular search terms
 */

@HiltViewModel
/**
 * SearchViewModel
 *
 */
class SearchViewModel @Inject constructor(
    private val productRepository: ProductCrudRepository,
    private val searchRepository: SearchRepository,
    private val recentSearchDataStore: RecentSearchDataStore,
    private val popularSearchDataStore: SmartPopularSearchDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadRecentSearches()
        loadPopularSearches()
    }

    /**
     * Load recent search history
     * 
     * Collects the recent searches flow from DataStore and updates UI state.
     */
    private fun loadRecentSearches() {
        viewModelScope.launch {
            recentSearchDataStore.recentSearches.collect { searches ->
                _uiState.update { it.copy(recentSearches = searches) }
            }
        }
    }

    //  Add this function
    /**
     * Load popular search terms
     * 
     * Collects the popular searches flow from DataStore and updates UI state.
     * Popular searches are aggregated from all user activity.
     */
    private fun loadPopularSearches() {
        viewModelScope.launch {
            popularSearchDataStore.popularSearches.collect { searches ->
                _uiState.update { it.copy(popularSearches = searches) }
            }
        }
    }

    /**
     * Save a search query without executing search
     * 
     * Used when the user selects a suggestion or navigates to search results.
     * Updates both recent and popular search stores.
     * 
     * @param query The search text to save
     */
    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            recentSearchDataStore.saveSearch(query)
            popularSearchDataStore.recordSearch(query)
        }
    }

    // Renamed for clarity: This is for actual background search

    /**
     * Execute visual product search
     * 
     * Processes an image URI to detect labels (objects) and then searches
     * for products matching those labels.
     * 
     * @param imageUri URI of the image to analyze
     */
    fun searchByImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val labelsResult = searchRepository.searchByImage(imageUri)

                labelsResult.fold(
                    onSuccess = { labels ->
                        val productsResult = searchRepository.processImageLabels(labels)

                        productsResult.fold(
                            onSuccess = { products ->
                                val searchQuery = labels.take(3).joinToString(" ")

                                if (searchQuery.isNotBlank()) {
                                    recentSearchDataStore.saveSearch(searchQuery)
                                    popularSearchDataStore.recordSearch(searchQuery)
                                }

                                _uiState.update {
                                    it.copy(
                                        searchResults = products,
                                        imageLabels = labels,
                                        isLoading = false,
                                        currentQuery = searchQuery,
                                        imageSearchCompleted = true
                                    )
                                }
                            },
                            onFailure = { exception ->
                                _uiState.update {
                                    it.copy(isLoading = false, error = exception.message)
                                }
                            }
                        )
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(isLoading = false, error = exception.message)
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    /**
     * Remove a single recently searched term
     */
    fun removeRecentSearch(query: String) {
        viewModelScope.launch {
            recentSearchDataStore.removeSearch(query)
        }
    }

    /**
     * Clear search history
     * 
     * Removes all entries from the recent search DataStore.
     */
    fun clearAllRecentSearches() {
        viewModelScope.launch {
            recentSearchDataStore.clearAll()
        }
    }

    /**
     * Reset image search completion flag
     */
    fun clearImageSearchFlag() {
        _uiState.update { it.copy(imageSearchCompleted = false) }
    }
}

/**
 * SearchUiState - UI State for Search Screen
 *
 */
/**
 * SearchUiState
 *
 * Data class representing [TODO: Add description]
 */
data class SearchUiState(
    val isLoading: Boolean = false,
    val searchResults: List<ProductItem> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val popularSearches: List<String> = emptyList(),
    val imageLabels: List<String> = emptyList(),
    val voiceQuery: String? = null,
    val currentQuery: String = "",
    val error: String? = null,
    val imageSearchCompleted: Boolean = false
)