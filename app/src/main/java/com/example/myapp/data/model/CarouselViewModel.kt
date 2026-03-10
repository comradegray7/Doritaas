package com.example.myapp.data.model

import android.net.Uri
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import com.example.myapp.data.dataclass.CarouselItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.CarouselRepository
import com.example.myapp.view.utils.CloudinaryHelper
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
 * CarouselState - UI State for Carousel management
 *
 * Tracks the state of the billboard/carousel items, loading status, and errors.
 *
 * @property carousels List of active carousel billboard items
 * @property isLoading Loading indicator for background operations
 * @property error Error message for failed operations
 */
data class CarouselState(
    val carousels: List<CarouselItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * CarouselViewModel - ViewModel for Billboard Carousel management
 *
 * Handles loading, creation, updating, and deletion of home screen carousel items.
 * Integrates with Cloudinary for image uploads and Firebase via repository.
 */
@HiltViewModel
class CarouselViewModel @Inject constructor(
    private val carouselRepository: CarouselRepository,
    private val imageLoader: ImageLoader
) : ViewModel() {
    /**
     * getImageLoader - Provides the ImageLoader instance for UI components
     */
    fun getImageLoader(): ImageLoader = imageLoader

    companion object {
        private const val TAG = "CarouselViewModel"
    }

    private val _carouselState = MutableStateFlow(CarouselState())
    val carouselState: StateFlow<CarouselState> = _carouselState.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    init {
        loadCarousels()
    }

    /**
     * Load all carousels
     *
     * Fetches the complete list of carousels from the repository.
     * Updates [carouselState] with the result.
     */
    fun loadCarousels() {
        viewModelScope.launch {
            _carouselState.value = _carouselState.value.copy(
                isLoading = true,
                error = null
            )

            carouselRepository.getCarousels().fold(
                onSuccess = { carousels ->
                    Log.d(TAG, "Carousels loaded: ${carousels.size}")
                    _carouselState.value = _carouselState.value.copy(
                        isLoading = false,
                        carousels = carousels,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to load carousels: ${exception.message}")
                    _carouselState.value = _carouselState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to load carousels",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Create a new carousel
     *
     * Validates input and creates a carousel via repository.
     * Shows success/error snackbar and reloads carousels on success.
     */
    /**
     * Upload carousel image to Cloudinary
     */
    fun uploadCarouselImage(
        imageUri: Uri,
        cloudinaryHelper: CloudinaryHelper,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _carouselState.value = _carouselState.value.copy(isLoading = true)

            cloudinaryHelper.uploadImage(
                fileUri = imageUri,
                folder = "carousel"
            ).fold(
                onSuccess = { publicId ->
                    Log.d(TAG, "Carousel image uploaded: $publicId")
                    _carouselState.value = _carouselState.value.copy(isLoading = false)
                    onSuccess(publicId)

                    _snackBarData.emit(
                        SnackBarData(
                            message = "Image uploaded successfully",
                            duration = SnackbarDuration.Short
                        )
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Upload failed: ${error.message}")
                    _carouselState.value = _carouselState.value.copy(
                        isLoading = false,
                        error = error.message
                    )

                    _snackBarData.emit(
                        SnackBarData(
                            message = "Failed to upload image: ${error.message}",
                            isError = true,
                            duration = SnackbarDuration.Long
                        )
                    )
                }
            )
        }
    }

    /**
     * Create carousel with public_id (store public_id, generate URL when displaying)
     */
    fun createCarousel(
        title: String,
        description: String,
        imagePublicId: String,
        redirectUrl: String?
    ) {
        viewModelScope.launch {
            _carouselState.value = _carouselState.value.copy(isLoading = true)

            val carousel = CarouselItem(
                title = title,
                description = description,
                imageUrl = imagePublicId,
                redirectUrl = redirectUrl?.ifBlank { null }
            )

            carouselRepository.createCarousel(carousel).fold(
                onSuccess = {
                    Log.d(TAG, "Carousel created: ${it.id}")
                    _snackBarData.emit(
                        SnackBarData(
                            "Carousel '$title' created successfully",
                            "Success"
                        )
                    )
                    loadCarousels()
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to create carousel: ${exception.message}")
                    _carouselState.value = _carouselState.value.copy(isLoading = false)
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to create carousel",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Update an existing carousel
     *
     * Updates carousel via repository.
     * Shows success/error snackbar and reloads carousels on success.
     */
    fun updateCarousel(carousel: CarouselItem) {
        viewModelScope.launch {
            _carouselState.value = _carouselState.value.copy(isLoading = true)

            carouselRepository.updateCarousel(carousel).fold(
                onSuccess = {
                    Log.d(TAG, "Carousel updated: ${carousel.id}")
                    _snackBarData.emit(
                        SnackBarData(
                            "Carousel '${carousel.title}' updated successfully",
                            "Success"
                        )
                    )
                    loadCarousels()
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to update carousel: ${exception.message}")
                    _carouselState.value = _carouselState.value.copy(isLoading = false)
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to update carousel",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Delete a carousel
     *
     * Deletes carousel via repository.
     * Shows success/error snackbar and reloads carousels on success.
     */
    fun deleteCarousel(item: CarouselItem) {
        viewModelScope.launch {
            carouselRepository.deleteCarousel(item.id).fold(
                onSuccess = {
                    Log.d(TAG, "Carousel deleted: ${item.title}")
                    _snackBarData.emit(
                        SnackBarData(
                            "Carousel '${item.title}' deleted successfully",
                            "Success"
                        )
                    )
                    loadCarousels()
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to delete carousel: ${exception.message}")
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to delete carousel",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Search carousels by query
     *
     * Searches carousels by title or description.
     * Updates [carouselState] with filtered results.
     */
    fun searchCarousels(query: String) {
        viewModelScope.launch {
            _carouselState.value = _carouselState.value.copy(isLoading = true)

            carouselRepository.searchCarousels(query).fold(
                onSuccess = { carousels ->
                    Log.d(TAG, "Search completed: ${carousels.size} results")
                    _carouselState.value = _carouselState.value.copy(
                        isLoading = false,
                        carousels = carousels,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Search failed: ${exception.message}")
                    _carouselState.value = _carouselState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Search failed",
                            "Error"
                        )
                    )
                }
            )
        }
    }
}