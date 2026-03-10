package com.example.myapp.data.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.dataclass.BrandItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.BrandRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * BrandState - UI State for Brand Management
 *
 * Tracks the state of the brand list, brand operations, and details.
 *
 * @property isLoading Loading indicator for brand operations
 * @property brands List of available brands
 * @property currentBrand Currently selected brand for editing/details
 * @property error Error message if operations fail
 * @property isSuccess Success flag for creation/update/deletion operations
 */
data class BrandState(
    val isLoading: Boolean = false,
    val brands: List<BrandItem> = emptyList(),
    val currentBrand: BrandItem? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

/**
 * BrandViewModel - ViewModel for Brand Management
 *
 * Handles fetching, creating, updating, and deleting product brands.
 * Used by admin screens for brand management and user screens for filtering.
 */
@HiltViewModel
class BrandViewModel @Inject constructor(
    private val brandRepository: BrandRepository
) : ViewModel() {

    companion object {
        private const val TAG = "BrandViewModel"
    }

    private val _brandState = MutableStateFlow(BrandState())
    val brandState: StateFlow<BrandState> = _brandState.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    init {
        loadBrands()
    }

    /**
     * Load all brands
     *
     * Fetches the complete list of brands from the repository.
     * Updates [brandState] with the result.
     */
    fun loadBrands() {
        viewModelScope.launch {
            _brandState.value = _brandState.value.copy(
                isLoading = true,
                error = null
            )

            brandRepository.getBrands().fold(
                onSuccess = { brands ->
                    Log.d(TAG, "Brands loaded: ${brands.size}")
                    _brandState.value = _brandState.value.copy(
                        isLoading = false,
                        brands = brands,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to load brands: ${exception.message}")
                    _brandState.value = _brandState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to load brands",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Create a new brand
     *
     * Adds a new brand to the database.
     *
     * @param brandName Name of the new brand
     */
    fun createBrand(brandName: String) {
        if (brandName.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Brand name cannot be empty", "Error"))
            }
            return
        }

        viewModelScope.launch {
            _brandState.value = _brandState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            brandRepository.createBrand(brandName).fold(
                onSuccess = { brand ->
                    Log.d(TAG, "Brand created: ${brand.brandName}")
                    _brandState.value = _brandState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Brand '${brand.brandName}' created successfully"))
                    loadBrands() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to create brand: ${exception.message}")
                    _brandState.value = _brandState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to create brand",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Update an existing brand
     *
     * @param brandId ID of the brand to update
     * @param brandName New name for the brand
     */
    fun updateBrand(brandId: String, brandName: String) {
        if (brandName.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Brand name cannot be empty", "Error"))
            }
            return
        }

        viewModelScope.launch {
            _brandState.value = _brandState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            brandRepository.updateBrand(brandId, brandName).fold(
                onSuccess = { brand ->
                    Log.d(TAG, "Brand updated: ${brand.brandName}")
                    _brandState.value = _brandState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Brand updated successfully"))
                    loadBrands() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to update brand: ${exception.message}")
                    _brandState.value = _brandState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to update brand",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Delete a brand
     *
     * @param brandId ID of the brand to remove
     * @param brandName Name of the brand (for display in success message)
     */
    fun deleteBrand(brandId: String, brandName: String) {
        viewModelScope.launch {
            _brandState.value = _brandState.value.copy(
                isLoading = true,
                error = null
            )

            brandRepository.deleteBrand(brandId).fold(
                onSuccess = {
                    Log.d(TAG, "Brand deleted: $brandName")
                    _brandState.value = _brandState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Brand '$brandName' deleted successfully"))
                    loadBrands() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to delete brand: ${exception.message}")
                    _brandState.value = _brandState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to delete brand",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Search brands
     *
     * Filters the brand list based on a search query.
     *
     * @param query Search string (brand name)
     */
    fun searchBrands(query: String) {
        if (query.isBlank()) {
            loadBrands()
            return
        }

        viewModelScope.launch {
            _brandState.value = _brandState.value.copy(
                isLoading = true,
                error = null
            )

            brandRepository.searchBrands(query).fold(
                onSuccess = { brands ->
                    Log.d(TAG, "Search results: ${brands.size}")
                    _brandState.value = _brandState.value.copy(
                        isLoading = false,
                        brands = brands,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to search brands: ${exception.message}")
                    _brandState.value = _brandState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

}