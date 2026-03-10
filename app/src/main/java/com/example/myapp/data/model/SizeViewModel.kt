package com.example.myapp.data.model

// SizeViewModel.kt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.dataclass.SizeItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.SizeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SizeState - UI State for Size Management
 * 
 * Tracks the state of the size list, size operations, and details.
 * 
 * @property error Error message if operations fail
 */
/**
 * SizeState
 *
 */
data class SizeState(
    val isLoading: Boolean = false,
    val sizes: List<SizeItem> = emptyList(),
    val currentSize: SizeItem? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

/**
 * SizeViewModel - ViewModel for Size Variant Management
 * 
 * Handles fetching, creating, updating, and deleting product sizes.
 * Used by admin screens for size management and user screens for product variant selection.
 * 
 * ## Dependencies
 * - SizeRepository: Data source for size operations
 */
@HiltViewModel
/**
 * SizeViewModel
 *
 */
class SizeViewModel @Inject constructor(
    private val sizeRepository: SizeRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SizeViewModel"
    }

    private val _sizeState = MutableStateFlow(SizeState())
    val sizeState: StateFlow<SizeState> = _sizeState.asStateFlow()
    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    init {
        loadSizes()
    }

    /**
     * Load all sizes
     * 
     * Fetches the complete list of sizes from the repository.
     * Updates [sizeState] with the result.
     */
    fun loadSizes() {
        viewModelScope.launch {
            _sizeState.value = _sizeState.value.copy(
                isLoading = true,
                error = null
            )

            sizeRepository.getSizes().fold(
                onSuccess = { sizes ->
                    Log.d(TAG, "Sizes loaded: ${sizes.size}")
                    _sizeState.value = _sizeState.value.copy(
                        isLoading = false,
                        sizes = sizes,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to load sizes: ${exception.message}")
                    _sizeState.value = _sizeState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to load sizes",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Create a new size
     * 
     * Adds a new size variant to the database.
     * 
     * @param size Name/Value of the new size (e.g., "M", "42", "Large")
     */
    fun createSize(size: String) {
        if (size.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Size cannot be empty", "Error"))
            }
            return
        }

        viewModelScope.launch {
            _sizeState.value = _sizeState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            sizeRepository.createSize(size).fold(
                onSuccess = { sizeItem ->
                    Log.d(TAG, "Size created: ${sizeItem.size}")
                    _sizeState.value = _sizeState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Size '${sizeItem.size}' created successfully"))
                    loadSizes() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to create size: ${exception.message}")
                    _sizeState.value = _sizeState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to create size",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Update an existing size
     * 
     * @param sizeId ID of the size to update
     * @param size New name/value for the size
     */
    fun updateSize(sizeId: String, size: String) {
        if (size.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Size cannot be empty", "Error"))
            }
            return
        }

        viewModelScope.launch {
            _sizeState.value = _sizeState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            sizeRepository.updateSize(sizeId, size).fold(
                onSuccess = { sizeItem ->
                    Log.d(TAG, "Size updated: ${sizeItem.size}")
                    _sizeState.value = _sizeState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Size updated successfully"))
                    loadSizes() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to update size: ${exception.message}")
                    _sizeState.value = _sizeState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to update size",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Delete a size
     * 
     * @param sizeId ID of the size to remove
     * @param size Name of the size (for display in success message)
     */
    fun deleteSize(sizeId: String, size: String) {
        viewModelScope.launch {
            _sizeState.value = _sizeState.value.copy(
                isLoading = true,
                error = null
            )

            sizeRepository.deleteSize(sizeId).fold(
                onSuccess = {
                    Log.d(TAG, "Size deleted: $size")
                    _sizeState.value = _sizeState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Size '$size' deleted successfully"))
                    loadSizes() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to delete size: ${exception.message}")
                    _sizeState.value = _sizeState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to delete size",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Search sizes
     * 
     * Filters the size list based on a search query.
     * 
     * @param query Search string
     */
    fun searchSizes(query: String) {
        if (query.isBlank()) {
            loadSizes()
            return
        }

        viewModelScope.launch {
            _sizeState.value = _sizeState.value.copy(
                isLoading = true,
                error = null
            )

            sizeRepository.searchSizes(query).fold(
                onSuccess = { sizes ->
                    Log.d(TAG, "Search results: ${sizes.size}")
                    _sizeState.value = _sizeState.value.copy(
                        isLoading = false,
                        sizes = sizes,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to search sizes: ${exception.message}")
                    _sizeState.value = _sizeState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

}