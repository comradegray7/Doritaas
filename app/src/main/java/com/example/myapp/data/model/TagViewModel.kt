package com.example.myapp.data.model

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.dataclass.ProductTag
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.dataclass.TagCategory
import com.example.myapp.data.repository.TagRepository
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
 * TagState
 *
 */
data class TagState(
    val isLoading: Boolean = false,
    val tags: List<ProductTag> = emptyList(),
    val filteredTags: List<ProductTag> = emptyList(),
    val selectedCategory: TagCategory? = null,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val searchQuery: String = "",
)

@HiltViewModel
/**
 * TagViewModel
 *
 */
class TagViewModel @Inject constructor(
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _tagState = MutableStateFlow(TagState())
    val tagState: StateFlow<TagState> = _tagState.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    init {
        loadAllTags()
        initializeSystemTags()
    }

    // Check before creating system tags
    private fun initializeSystemTags() {
        viewModelScope.launch {
            val systemTags = listOf(
                ProductTag(
                    name = "prime_eligible",
                    displayName = "Prime Eligible",
                    description = "Product eligible for Prime benefits",
                    color = "#00A8E1",
                    category = TagCategory.MEMBERSHIP,
                    isSystemTag = true
                ),
                ProductTag(
                    name = "new_arrival",
                    displayName = "New Arrival",
                    description = "Recently added product",
                    color = "#4CAF50",
                    category = TagCategory.STATUS,
                    isSystemTag = true
                ),
                ProductTag(
                    name = "trending",
                    displayName = "Trending",
                    description = "Popular trending product",
                    color = "#FF5722",
                    category = TagCategory.STATUS,
                    isSystemTag = true
                ),
                ProductTag(
                    name = "limited_edition",
                    displayName = "Limited Edition",
                    description = "Limited quantity available",
                    color = "#9C27B0",
                    category = TagCategory.PROMOTION,
                    isSystemTag = true
                ),
                ProductTag(
                    name = "eco_friendly",
                    displayName = "Eco Friendly",
                    description = "Environmentally sustainable product",
                    color = "#8BC34A",
                    category = TagCategory.FEATURE,
                    isSystemTag = true
                )
            )

            // Only create if they don't exist
            systemTags.forEach { tag ->
                tagRepository.tagNameExists(tag.name).onSuccess { exists ->
                    if (!exists) {
                        tagRepository.createTag(tag)
                        Log.d("TagViewModel", "Created system tag: ${tag.name}")
                    }
                }
            }
        }
    }

    /**
     * loadAllTags
     *
     */
    fun loadAllTags() {
        viewModelScope.launch {
            _tagState.value = _tagState.value.copy(isLoading = true)

            tagRepository.getAllTags()
                .onSuccess { tags ->
                    // Remove duplicates at ViewModel level too
                    val uniqueTags = tags.distinctBy { it.name }

                    _tagState.value = _tagState.value.copy(
                        isLoading = false,
                        tags = uniqueTags,
                        filteredTags = uniqueTags,
                        error = null
                    )
                }
                .onFailure { error ->
                    _tagState.value = _tagState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                    showSnackBar("Failed to load tags: ${error.message}", isError = true)
                }
        }
    }

    /**
     * createTag
     *
     *
     * @param tag The tag parameter
     */
    fun createTag(tag: ProductTag) {
        viewModelScope.launch {
            // Check for empty name
            if (tag.displayName.isBlank()) {
                showSnackBar("Tag name cannot be empty", isError = true)
                return@launch
            }

            _tagState.value = _tagState.value.copy(isLoading = true)

            tagRepository.createTag(tag)
                .onSuccess {
                    _tagState.value = _tagState.value.copy(isLoading = false, isSuccess = true)
                    showSnackBar("Tag '${tag.displayName}' created successfully")
                    loadAllTags()
                }
                .onFailure { error ->
                    _tagState.value = _tagState.value.copy(isLoading = false)
                    //  user-friendly error message
                    val message = when {
                        error.message?.contains("already exists") == true ->
                            "A tag with this name already exists. Please choose a different name."
                        else -> "Failed to create tag: ${error.message}"
                    }
                    showSnackBar(message, isError = true)
                }
        }
    }

    /**
     * updateTag
     *
     *
     * @param tag The tag parameter
     */
    fun updateTag(tag: ProductTag) {
        viewModelScope.launch {
            //  VALIDATION: Check for empty name
            if (tag.displayName.isBlank()) {
                showSnackBar("Tag name cannot be empty", isError = true)
                return@launch
            }

            _tagState.value = _tagState.value.copy(isLoading = true)

            tagRepository.updateTag(tag)
                .onSuccess {
                    _tagState.value = _tagState.value.copy(isLoading = false, isSuccess = true)
                    showSnackBar("Tag '${tag.displayName}' updated successfully")
                    loadAllTags()
                }
                .onFailure { error ->
                    _tagState.value = _tagState.value.copy(isLoading = false)
                    //  Show user-friendly error message
                    val message = when {
                        error.message?.contains("already exists") == true ->
                            "A tag with this name already exists. Please choose a different name."
                        error.message?.contains("system tag") == true ->
                            "Cannot modify system tags"
                        else -> "Failed to update tag: ${error.message}"
                    }
                    showSnackBar(message, isError = true)
                }
        }
    }

    /**
     * deleteTag
     *
     *
     * @param tagId The tagId parameter
     */
    fun deleteTag(tagId: String) {
        viewModelScope.launch {
            _tagState.value = _tagState.value.copy(isLoading = true)

            tagRepository.deleteTag(tagId)
                .onSuccess {
                    _tagState.value = _tagState.value.copy(isLoading = false, isSuccess = true)
                    showSnackBar("Tag deleted successfully")
                    loadAllTags()
                }
                .onFailure { error ->
                    _tagState.value = _tagState.value.copy(isLoading = false)
                    val message = when {
                        error.message?.contains("system tag") == true ->
                            "Cannot delete system tags"
                        else -> "Failed to delete tag: ${error.message}"
                    }
                    showSnackBar(message, isError = true)
                }
        }
    }

    /**
     * searchTags
     *
     *
     * @param query The query parameter
     */
    fun searchTags(query: String) {
        viewModelScope.launch {
            _tagState.update { it.copy(isLoading = true) }

            val currentItems = _tagState.value.tags
            val filteredItems = if (query.isBlank()) {
                currentItems
            } else {
                currentItems.filter { item ->
                    item.name.contains(query, ignoreCase = true) ||
                            item.displayName.contains(query, ignoreCase = true) ||
                            item.description.contains(query, ignoreCase = true)
                }
            }

            _tagState.update {
                it.copy(
                    isLoading = false,
                    filteredTags = filteredItems,
                    searchQuery = query
                )
            }
        }
    }

    private fun showSnackBar(message: String, isError: Boolean = false) {
        viewModelScope.launch {
            _snackBarData.emit(
                SnackBarData(
                    message = message,
                    isError = isError,
                    duration = SnackbarDuration.Short
                )
            )
        }
    }
}
