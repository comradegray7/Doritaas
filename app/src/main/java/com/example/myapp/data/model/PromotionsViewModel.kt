package com.example.myapp.data.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.PromotionsData
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.PromotionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * PromotionState - UI state for promotions management.
 *
 * Holds loading flags, lists of promotions and their products, and
 * error/success flags used by admin and customer-facing promotion screens.
 *
 * @property loadingPromotionId ID of the promotion currently loading products for
 * @property isLoading Indicates whether promotion metadata is loading
 * @property promotions List of all loaded promotions
 * @property currentPromotion Promotion currently selected in the UI
 * @property promotionProducts Products attached to the active promotion
 * @property isLoadingProducts Loading flag for promotionProducts
 * @property error Optional error message when operations fail
 * @property promotionProductsMap Cached map of promotionId → products
 * @property isSuccess Flag for one-off success events (e.g., create/update)
 */
data class PromotionState(
    val loadingPromotionId: String? = null,
    val isLoading: Boolean = false,
    val promotions: List<PromotionsData> = emptyList(),
    val currentPromotion: PromotionsData? = null,
    val promotionProducts: List<ProductItem> = emptyList(),
    val isLoadingProducts: Boolean = false,
    val error: String? = null,
    val promotionProductsMap: Map<String, List<ProductItem>> = emptyMap(),
    val isSuccess: Boolean = false
)

@HiltViewModel
/**
 * PromotionViewModel - ViewModel for managing promotional campaigns.
 *
 * Coordinates CRUD operations on promotions, loading of related products,
 * bulk association of products via tags/categories, and exposes snackbar
 * feedback events for admin flows and customer promotion surfaces.
 */
class PromotionViewModel @Inject constructor(
    private val promotionRepository: PromotionRepository
) : ViewModel() {

    companion object {
        private const val TAG = "PromotionViewModel"
    }

    private val _promotionState = MutableStateFlow(PromotionState())
    val promotionState: StateFlow<PromotionState> = _promotionState.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    init {
        loadPromotions()
    }

    /**
     * Load all promotions.
     *
     * Fetches the full list of promotions from the repository and updates
     * [promotionState] with loading and error information.
     */
    fun loadPromotions() {
        viewModelScope.launch {
            _promotionState.update { it.copy(isLoading = true, error = null) }

            promotionRepository.getPromotions().fold(
                onSuccess = { promotions ->
                    _promotionState.update {
                        it.copy(
                            isLoading = false,
                            promotions = promotions,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _promotionState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message,
                            promotions = emptyList()
                        )
                    }
                }
            )
        }
    }

    //  Load all products from all active promotions
    /**
     * Load products from all active promotions.
     *
     * Populates [PromotionState.promotionProducts] with a flattened list of
     * products that currently belong to active campaigns (limited by `limit`).
     */
    fun loadAllPromotionProducts() {
        viewModelScope.launch {
            _promotionState.update { it.copy(isLoadingProducts = true) }

            promotionRepository.getProductsInActivePromotions(limit = 50).fold(
                onSuccess = { products ->
                    _promotionState.update {
                        it.copy(
                            isLoadingProducts = false,
                            promotionProducts = products
                        )
                    }
                },
                onFailure = { exception ->
                    _promotionState.update {
                        it.copy(
                            isLoadingProducts = false,
                            error = exception.message
                        )
                    }
                }
            )
        }
    }

    /**
     * Create a new promotion.
     *
     * Persists a new [PromotionsData] entry and reloads the list on success.
     *
     * @param promotion Promotion metadata to create.
     */
    fun createPromotion(promotion: PromotionsData) {
        viewModelScope.launch {
            _promotionState.update { it.copy(isLoading = true, error = null) }

            promotionRepository.createPromotion(promotion).fold(
                onSuccess = {
                    _promotionState.update { it.copy(isLoading = false, isSuccess = true) }
                    _snackBarData.emit(
                        SnackBarData("Promotion '${promotion.title}' created successfully")
                    )
                    loadPromotions()
                },
                onFailure = { exception ->
                    _promotionState.update {
                        it.copy(isLoading = false, error = exception.message)
                    }
                    _snackBarData.emit(
                        SnackBarData(exception.message ?: "Failed to create promotion", "Error")
                    )
                }
            )
        }
    }

    /**
     * Update an existing promotion.
     *
     * Persists changes to a [PromotionsData] entry and reloads the list on success.
     *
     * @param promotion Updated promotion to save.
     */
    fun updatePromotion(promotion: PromotionsData) {
        viewModelScope.launch {
            _promotionState.update { it.copy(isLoading = true, error = null) }

            promotionRepository.updatePromotion(promotion).fold(
                onSuccess = {
                    _promotionState.update { it.copy(isLoading = false, isSuccess = true) }
                    _snackBarData.emit(SnackBarData("Promotion updated successfully"))
                    loadPromotions()
                },
                onFailure = { exception ->
                    _promotionState.update {
                        it.copy(isLoading = false, error = exception.message)
                    }
                    _snackBarData.emit(
                        SnackBarData(exception.message ?: "Failed to update promotion", "Error")
                    )
                }
            )
        }
    }

    /**
     * Delete a promotion by ID.
     *
     * Removes a promotion and refreshes the list on success.
     *
     * @param promotionId ID of the promotion to delete.
     * @param promotionTitle Title used for user-facing snackbar feedback.
     */
    fun deletePromotion(promotionId: String, promotionTitle: String) {
        viewModelScope.launch {
            _promotionState.update { it.copy(isLoading = true, error = null) }

            promotionRepository.deletePromotion(promotionId).fold(
                onSuccess = {
                    _promotionState.update { it.copy(isLoading = false) }
                    _snackBarData.emit(
                        SnackBarData("Promotion '$promotionTitle' deleted successfully")
                    )
                    loadPromotions()
                },
                onFailure = { exception ->
                    _promotionState.update {
                        it.copy(isLoading = false, error = exception.message)
                    }
                    _snackBarData.emit(
                        SnackBarData(exception.message ?: "Failed to delete promotion", "Error")
                    )
                }
            )
        }
    }

    /**
     * Search promotions by query string.
     *
     * Filters promotions using repository search (typically by title/description).
     *
     * @param query Free-text search term.
     */
    fun searchPromotions(query: String) {
        viewModelScope.launch {
            _promotionState.update { it.copy(isLoading = true) }

            promotionRepository.searchPromotions(query).fold(
                onSuccess = { promotions ->
                    _promotionState.update {
                        it.copy(isLoading = false, promotions = promotions)
                    }
                },
                onFailure = { exception ->
                    _promotionState.update {
                        it.copy(isLoading = false, error = exception.message)
                    }
                }
            )
        }
    }
    /**
     * Load products for a specific promotion.
     *
     * Populates [PromotionState.promotionProducts] for the given campaign.
     *
     * @param promotionId ID of the promotion to load products for.
     */
    fun loadPromotionProducts(promotionId: String) {
        viewModelScope.launch {
            _promotionState.update { it.copy(isLoadingProducts = true) }

            promotionRepository.getProductsByPromotion(promotionId).fold(
                onSuccess = { products ->
                    _promotionState.update {
                        it.copy(
                            isLoadingProducts = false,
                            promotionProducts = products
                        )
                    }
                },
                onFailure = { exception ->
                    _promotionState.update {
                        it.copy(isLoadingProducts = false, error = exception.message)
                    }
                }
            )
        }
    }

    /**
     * Add a single product to a promotion.
     *
     * Associates a product with the given promotion and refreshes its product list.
     *
     * @param promotionId Target promotion ID.
     * @param productId Product ID to attach.
     */
    fun addProductToPromotion(promotionId: String, productId: String) {
        viewModelScope.launch {
            promotionRepository.addProductToPromotion(promotionId, productId).fold(
                onSuccess = {
                    _snackBarData.emit(SnackBarData("Product added to promotion"))
                    loadProductsForPromotion(promotionId)  
                },
                onFailure = { exception ->
                    _snackBarData.emit(
                        SnackBarData(exception.message ?: "Failed to add product", "Error")
                    )
                }
            )
        }
    }
    /**
     * Remove a product from a promotion.
     *
     * Detaches a product from the campaign and reloads its product list.
     *
     * @param promotionId Target promotion ID.
     * @param productId Product ID to remove.
     */
    fun removeProductFromPromotion(promotionId: String, productId: String) {
        viewModelScope.launch {
            promotionRepository.removeProductFromPromotion(promotionId, productId).fold(
                onSuccess = {
                    _snackBarData.emit(SnackBarData("Product removed from promotion"))
                    loadProductsForPromotion(promotionId)  
                },
                onFailure = { exception ->
                    _snackBarData.emit(
                        SnackBarData(exception.message ?: "Failed to remove product", "Error")
                    )
                }
            )
        }
    }
    /**
     * Bulk-add products to a promotion by tag.
     *
     * Finds products with the given tag and associates them with the promotion.
     *
     * @param promotionId Target promotion ID.
     * @param tag Product tag used as a filter.
     */
    fun addProductsByTag(promotionId: String, tag: String) {
        viewModelScope.launch {
            _promotionState.update { it.copy(isLoading = true) }

            promotionRepository.addProductsByTag(promotionId, tag).fold(
                onSuccess = { count ->
                    _promotionState.update { it.copy(isLoading = false) }
                    _snackBarData.emit(SnackBarData("Added $count products with tag '$tag'"))
                    loadPromotionProducts(promotionId)
                },
                onFailure = { exception ->
                    _promotionState.update { it.copy(isLoading = false) }
                    _snackBarData.emit(
                        SnackBarData(exception.message ?: "Failed to add products", "Error")
                    )
                }
            )
        }
    }

    /**
     * Bulk-add products to a promotion by category.
     *
     * Associates all matching products for the given category with the promotion.
     *
     * @param promotionId Target promotion ID.
     * @param categoryId Category identifier used as a filter.
     */
    fun addProductsByCategory(promotionId: String, categoryId: String) {
        viewModelScope.launch {
            _promotionState.update { it.copy(isLoading = true) }

            promotionRepository.addProductsByCategory(promotionId, categoryId).fold(
                onSuccess = { count ->
                    _promotionState.update { it.copy(isLoading = false) }
                    _snackBarData.emit(SnackBarData("Added $count products from category"))
                    loadPromotionProducts(promotionId)
                },
                onFailure = { exception ->
                    _promotionState.update { it.copy(isLoading = false) }
                    _snackBarData.emit(
                        SnackBarData(exception.message ?: "Failed to add products", "Error")
                    )
                }
            )
        }
    }

    /**
     *  Load products for a specific promotion
     */
    fun loadProductsForPromotion(promotionId: String) {
        viewModelScope.launch {
            _promotionState.update {
                it.copy(
                    loadingPromotionId = promotionId,
                    isLoadingProducts = true
                )
            }

            promotionRepository.getProductsByPromotion(promotionId).fold(
                onSuccess = { products ->
                    Log.d(TAG, "Loaded ${products.size} products for promotion: $promotionId")

                    // Update the map of promotion products
                    val currentMap = _promotionState.value.promotionProductsMap
                    val updatedMap = currentMap.toMutableMap().apply {
                        put(promotionId, products)
                    }

                    _promotionState.update {
                        it.copy(
                            promotionProductsMap = updatedMap,
                            promotionProducts = products, // update current list
                            loadingPromotionId = null,
                            isLoadingProducts = false
                        )
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to load products for promotion: ${exception.message}")
                    _promotionState.update {
                        it.copy(
                            loadingPromotionId = null,
                            isLoadingProducts = false,
                            error = exception.message
                        )
                    }
                    _snackBarData.emit(
                        SnackBarData(
                            "Failed to load products: ${exception.message}",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Refresh details for a specific promotion.
     *
     * Convenience wrapper that reloads products for the given promotion ID.
     *
     * @param promotionId ID of the promotion to refresh.
     */
    fun refreshPromotionDetails(promotionId: String) {
        viewModelScope.launch {
            // Your logic to reload currentPromotionProducts from the repository
            loadProductsForPromotion(promotionId)
        }
    }
}
