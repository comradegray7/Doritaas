package com.example.myapp.data.model

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.ToggleFavoriteUseCase
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.FavoritesRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * FavoriteState - UI State for Favorites/Wishlist
 *
 * Tracks the state of the user's favorite products list.
 *
 * @property error Error message if operations fail
 */
/**
 * FavoriteState
 *
 */
data class FavoriteState(
    val isLoading: Boolean = true,
    val favoriteItems: List<ProductItem> = emptyList(),
    val currentFavorite: ProductItem? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

/**
 * FavoriteViewModel - ViewModel for Wishlist Management
 *
 * Handles adding, removing, and listing favorite products.
 * Supports undo functionality for removal and real-time status updates.
 *
 * ## Dependencies
 * - ToggleFavoriteUseCase: Encapsulated logic for toggling favorite status
 * - FavoritesRepository: Data source for favorite operations
 */
@HiltViewModel
/**
 * FavoriteViewModel
 *
 */
class FavoriteViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "FavoriteViewModel"
    }

    private val _favoriteState = MutableStateFlow(FavoriteState())
    val favoriteState: StateFlow<FavoriteState> = _favoriteState.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    // Store recently deleted item for undo functionality
    private var recentlyDeletedItem: ProductItem? = null
    private var observeFavoritesJob: Job? = null

    init {
        startObservation()
    }

    /**
     * Initialize observation of favorite items
     *
     * Loads the initial list and sets up any necessary listeners.
     */
    private fun startObservation() {
        loadFavorites()
        viewModelScope.launch {
            refreshFavorites()
        }
    }

    /**
     * Load favorite items
     *
     * Fetches the current user's wishlist from the repository.
     * Updates [favoriteState] with the list of products.
     */
    fun loadFavorites() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _favoriteState.update {
                it.copy(
                    isLoading = false,
                    favoriteItems = emptyList(),
                    error = null // Ensure error is null if just logged out
                )
            }
            return
        }

        viewModelScope.launch {
            _favoriteState.value = _favoriteState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                favoritesRepository.refreshFavorites().fold(
                    onSuccess = { items ->
                        Log.d("FavoriteViewModel", "Favorites loaded successfully")
                        _favoriteState.value = _favoriteState.value.copy(
                            isLoading = false,
                            favoriteItems = items,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        Log.e("FavoriteViewModel", "Failed to load favorites: ${exception.message}")
                        _favoriteState.value = _favoriteState.value.copy(
                            isLoading = false,
                            favoriteItems = emptyList(),
                            error = null
                        )

                        _snackBarData.emit(
                            SnackBarData(
                                message = exception.message ?: "Failed to load favorites",
                                isError = true,
                                duration = SnackbarDuration.Short
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error loading favorites", e)
                _favoriteState.value = _favoriteState.value.copy(
                    isLoading = false,
                    error = "error: ${e.message}"
                )
            }
        }
    }

    /**
     * Check if a product is in favorites
     *
     * @param productId Unique identifier of the product
     * @return Flow emitting true if the product is a favorite, false otherwise
     */
    fun getFavoriteStatus(productId: String): Flow<Boolean> {
        return favoritesRepository.isFavorite(productId)
    }

    /**
     * Toggle favorite status for a product
     *
     * Adds the product if not present, removes it if already a favorite.
     * Shows a snackbar feedback message.
     *
     * @param product The product item to toggle
     */
    fun toggleFavorite(product: ProductItem) {
        viewModelScope.launch {
            _favoriteState.value = _favoriteState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            toggleFavoriteUseCase(product).fold(
                onSuccess = { isFavorite ->
                    val message = if (isFavorite) {
                        "${product.productName} added to favorites 🎉"
                    } else {
                        "${product.productName} removed from favorites"
                    }

                    Log.d(TAG, message)
                    _favoriteState.value = _favoriteState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    )

                    // Refresh the list if item was removed from favorites
                    if (!isFavorite) {
                        loadFavorites()
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to toggle favorite: ${exception.message}")
                    _favoriteState.value = _favoriteState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )

                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to update favorites",
                            "Error",
                            duration = SnackbarDuration.Long
                        )
                    )
                }
            )
        }
    }

    /**
     * Remove item from favorites with undo option
     *
     * Removes the item and shows a snackbar with an "UNDO" action.
     * Stores the item temporarily in [recentlyDeletedItem].
     *
     * @param item The product item to remove
     */

    fun removeFromFavorites(item: ProductItem) {
        viewModelScope.launch {
            recentlyDeletedItem = item
            _favoriteState.value = _favoriteState.value.copy(
                isLoading = true,
                error = null
            )

            favoritesRepository.removeFromFavorites(item.id).fold(
                onSuccess = {
                    Log.d(TAG, "Item removed from favorites: ${item.productName}")
                    _favoriteState.value = _favoriteState.value.copy(
                        isLoading = false,
                        error = null
                    )

                    _snackBarData.emit(
                        SnackBarData(
                            message = "${item.productName} removed from wishlist",
                            actionLabel = "UNDO",
                            duration = SnackbarDuration.Long,
                            onActionClick = {
                                undoRemoveFromFavorites()
                            }
                        )
                    )

                    loadFavorites() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to remove from favorites: ${exception.message}")
                    _favoriteState.value = _favoriteState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to remove item",
                            "Error",
                            duration = SnackbarDuration.Long
                        )
                    )
                }
            )
        }
    }

    /**
     * Undo the last removal action
     *
     * Restores [recentlyDeletedItem] to the favorites list.
     */
    private fun undoRemoveFromFavorites() {
        viewModelScope.launch {
            recentlyDeletedItem?.let { item ->
                _favoriteState.value = _favoriteState.value.copy(
                    isLoading = true,
                    error = null
                )

                favoritesRepository.addToFavorites(item).fold(
                    onSuccess = {
                        Log.d(TAG, "Item restored to favorites: ${item.productName}")
                        _favoriteState.value = _favoriteState.value.copy(
                            isLoading = false,
                            error = null
                        )

                        _snackBarData.emit(
                            SnackBarData(
                                message = "${item.productName} restored to wishlist",
                                duration = SnackbarDuration.Short
                            )
                        )
                        loadFavorites() // Refresh list
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to restore item: ${exception.message}")
                        _favoriteState.value = _favoriteState.value.copy(
                            isLoading = false,
                            error = exception.message
                        )

                        _snackBarData.emit(
                            SnackBarData(
                                exception.message ?: "Failed to restore item",
                                "Error",
                                duration = SnackbarDuration.Long
                            )
                        )
                    }
                )
                recentlyDeletedItem = null
            }
        }
    }

    /**
     * Delete all items from favorites
     *
     * Clears the entire wishlist for the current user.
     */
    fun deleteAllFromFavorites() {
        viewModelScope.launch {
            _favoriteState.value = _favoriteState.value.copy(
                isLoading = true,
                error = null
            )

            favoritesRepository.clearAllFavorites().fold(
                onSuccess = {
                    Log.d(TAG, "All favorites cleared")
                    _favoriteState.value = _favoriteState.value.copy(
                        isLoading = false,
                        favoriteItems = emptyList(),
                        error = null
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            message = "All items removed from wishlist",
                            duration = SnackbarDuration.Long
                        )
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to clear favorites: ${exception.message}")
                    _favoriteState.value = _favoriteState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )

                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to clear wishlist",
                            "Error",
                            duration = SnackbarDuration.Long
                        )
                    )
                }
            )
        }
    }

    /**
     * Clear local favorites state
     *
     * Used when the user logs out to prevent cross-user data leakage.
     */
    fun clearFavorites() {
        _favoriteState.value = _favoriteState.value.copy(
            isLoading = false,
            favoriteItems = emptyList(),
            error = null
        )

        // Redundant set, but ensuring consistency
        _favoriteState.value = _favoriteState.value.copy(
            isLoading = false,
            favoriteItems = emptyList(),
            error = null
        )

        Log.d("FavoriteViewModel", "✅ Cleared all favorites")
    }

    /**
     * Refresh favorites list
     *
     * Forces a fresh fetch from the repository.
     */
    suspend fun refreshFavorites() {
        favoritesRepository.refreshFavorites()
    }

    override fun onCleared() {
        super.onCleared()
        observeFavoritesJob?.cancel()
    }
}