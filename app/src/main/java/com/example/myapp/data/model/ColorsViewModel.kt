package com.example.myapp.data.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.dataclass.ColorItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.ColorRepository
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
 * ColorState - UI State for Color Management
 *
 * Tracks the state of the color list, color operations, and details.
 *
 * @property error Error message if operations fail
 */
/**
 * ColorState
 *
 */
data class ColorState(
    val isLoading: Boolean = false,
    val colors: List<ColorItem> = emptyList(),
    val currentColor: ColorItem? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

/**
 * ColorViewModel - ViewModel for Color Management
 *
 * Handles fetching, creating, updating, and deleting product colors.
 * Used by admin screens for color management and user screens for product variant selection.
 *
 * ## Dependencies
 * - ColorRepository: Data source for color operations
 */
@HiltViewModel
/**
 * ColorViewModel
 *
 */
class ColorViewModel @Inject constructor(
    private val colorRepository: ColorRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ColorViewModel"
    }

    private val _colorState = MutableStateFlow(ColorState())
    val colorState: StateFlow<ColorState> = _colorState.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    init {
        loadColors()
    }

    /**
     * Load all colors
     *
     * Fetches the complete list of colors from the repository.
     * Updates [colorState] with the result.
     */
    fun loadColors() {
        viewModelScope.launch {
            _colorState.value = _colorState.value.copy(
                isLoading = true,
                error = null
            )

            colorRepository.getColors().fold(
                onSuccess = { colors ->
                    Log.d(TAG, "Colors loaded: ${colors.size}")
                    _colorState.value = _colorState.value.copy(
                        isLoading = false,
                        colors = colors,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to load colors: ${exception.message}")
                    _colorState.value = _colorState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to load colors",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Create a new color
     *
     * Adds a new color to the database.
     *
     * @param colorName Name of the new color (e.g., "Midnight Blue")
     * @param hexCode Hexadecimal color code (e.g., "#191970")
     */
    fun createColor(colorName: String, hexCode: String) {
        if (colorName.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Color name cannot be empty", "Error"))
                _snackBarData.emit(SnackBarData("Color hex code cannot be empty", "Error"))
            }
            return
        }

        viewModelScope.launch {
            _colorState.value = _colorState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            colorRepository.createColor(colorName, hexCode).fold(
                onSuccess = { color ->
                    Log.d(TAG, "Color created: ${color.name}")
                    Log.d(TAG, "Color created: ${color.hexCode}")
                    _colorState.value = _colorState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Color '${color.name}' with '${color.hexCode}' created successfully"))
                    loadColors() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to create color: ${exception.message}")
                    _colorState.value = _colorState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to create color",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Update an existing color
     *
     * @param colorId ID of the color to update
     * @param colorName New name for the color
     * @param hexCode New hex code for the color
     */
    fun updateColor(colorId: String, colorName: String, hexCode: String) {
        if (colorName.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Color name cannot be empty", "Error"))
                _snackBarData.emit(SnackBarData("Color hex code cannot be empty", "Error"))
            }
            return
        }

        viewModelScope.launch {
            _colorState.value = _colorState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            colorRepository.updateColor(colorId, colorName, hexCode).fold(
                onSuccess = { color ->
                    Log.d(TAG, "Color updated: ${color.name}")
                    Log.d(TAG, "Color hex code updated: ${color.hexCode}")
                    _colorState.value = _colorState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Color updated successfully"))
                    loadColors() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to update color: ${exception.message}")
                    _colorState.value = _colorState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to update color",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Delete a color
     *
     * @param colorId ID of the color to remove
     * @param colorName Name of the color (for display in success message)
     * @param hexCode Hex code of the color (for display)
     */
    fun deleteColor(colorId: String, colorName: String, hexCode: String) {
        viewModelScope.launch {
            _colorState.value = _colorState.value.copy(
                isLoading = true,
                error = null
            )

            colorRepository.deleteColor(colorId).fold(
                onSuccess = {
                    Log.d(TAG, "Color deleted: $colorName")
                    _colorState.value = _colorState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Color '$colorName' with '${hexCode}' deleted successfully"))
                    loadColors() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to delete color: ${exception.message}")
                    _colorState.value = _colorState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to delete color",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Search colors
     *
     * Filters the color list based on a search query.
     *
     * @param query Search string (color name)
     */
    fun searchColors(query: String) {
        if (query.isBlank()) {
            loadColors()
            return
        }

        viewModelScope.launch {
            _colorState.value = _colorState.value.copy(
                isLoading = true,
                error = null
            )

            colorRepository.searchColors(query).fold(
                onSuccess = { colors ->
                    Log.d(TAG, "Search results: ${colors.size}")
                    _colorState.value = _colorState.value.copy(
                        isLoading = false,
                        colors = colors,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to search colors: ${exception.message}")
                    _colorState.value = _colorState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    // Clear error
    /**
     * clearError
     *
     */
    fun clearError() {
        _colorState.value = _colorState.value.copy(error = null)
    }

}