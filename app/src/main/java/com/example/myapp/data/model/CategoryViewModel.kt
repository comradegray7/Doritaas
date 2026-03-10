package com.example.myapp.data.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.dataclass.CategoryBreadcrumb
import com.example.myapp.data.dataclass.CategoryItem
import com.example.myapp.data.dataclass.CategoryNode
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.CategoryRepository
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
 * CategoryViewModel - ViewModel for managing product categories
 *
 * Handles loading of category trees, subcategories, and CRUD operations for
 * admin management. Supports hierarchical category structures and breadcrumbs.
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CategoryViewModel"
    }

    private val _categoryState = MutableStateFlow(CategoryState())
    val categoryState: StateFlow<CategoryState> = _categoryState.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    init {
        loadCategories()
    }

    /**
     * Load all categories
     */
    /**
     * Load all categories (with tree)
     */
    fun loadCategories() {
        viewModelScope.launch {
            _categoryState.value = _categoryState.value.copy(
                isLoading = true,
                error = null
            )

            categoryRepository.getCategories().fold(
                onSuccess = { categories ->
                    Log.d(TAG, "Categories loaded: ${categories.size}")

                    // Since repository already has tree-building logic, get the tree
                    val treeResult = categoryRepository.getCategoryTree()

                    treeResult.fold(
                        onSuccess = { tree ->
                            _categoryState.value = _categoryState.value.copy(
                                isLoading = false,
                                categories = categories,
                                categoryTree = tree,  // CRITICAL: Set the tree
                                error = null
                            )
                        },
                        onFailure = { treeError ->
                            // If tree building fails, use flat list as fallback
                            Log.w(
                                TAG,
                                "Tree building failed, using flat list: ${treeError.message}"
                            )
                            val fallbackTree = categories.map { category ->
                                CategoryNode(
                                    category = category,
                                    depth = category.level,
                                    isExpanded = false,
                                    subcategories = emptyList()
                                )
                            }

                            _categoryState.value = _categoryState.value.copy(
                                isLoading = false,
                                categories = categories,
                                categoryTree = fallbackTree,
                                error = null
                            )
                        }
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to load categories: ${exception.message}")
                    _categoryState.value = _categoryState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to load categories",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     *  Load category tree structure
     */
    fun loadCategoryTree() {
        viewModelScope.launch {
            _categoryState.value = _categoryState.value.copy(
                isLoading = true,
                error = null
            )

            categoryRepository.getCategoryTree().fold(
                onSuccess = { tree ->
                    Log.d(TAG, "Category tree loaded: ${tree.size} root nodes")
                    _categoryState.value = _categoryState.value.copy(
                        isLoading = false,
                        categoryTree = tree,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to load category tree: ${exception.message}")
                    _categoryState.value = _categoryState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    /**
     *   Load subcategories for a parent
     */
    fun loadSubcategories(parentId: String) {
        viewModelScope.launch {
            categoryRepository.getSubcategories(parentId).fold(
                onSuccess = { subcategories ->
                    _categoryState.value = _categoryState.value.copy(
                        selectedSubcategories = subcategories
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to load subcategories: ${exception.message}")
                }
            )
        }
    }

    /**
     *  Toggle category expansion in tree
     */
    fun toggleCategoryExpansion(categoryId: String) {
        val currentTree = _categoryState.value.categoryTree
        val updatedTree = toggleExpansionRecursive(currentTree, categoryId)
        _categoryState.value = _categoryState.value.copy(
            categoryTree = updatedTree
        )
    }

    private fun toggleExpansionRecursive(
        nodes: List<CategoryNode>,
        targetId: String
    ): List<CategoryNode> {
        return nodes.map { node ->
            if (node.category.id == targetId) {
                node.copy(isExpanded = !node.isExpanded)
            } else {
                node.copy(
                    subcategories = toggleExpansionRecursive(node.subcategories, targetId)
                )
            }
        }
    }

    /**
     *  Create category with optional parent
     */
    fun createCategory(
        categoryName: String,
        parentId: String? = null,
        categoryImage: String = "",
        description: String = ""
    ) {
        if (categoryName.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Category name cannot be empty", "Error"))
            }
            return
        }

        viewModelScope.launch {
            _categoryState.value = _categoryState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            categoryRepository.createCategory(
                categoryName = categoryName,
                parentId = parentId,
                categoryImage = categoryImage,
                description = description
            ).fold(
                onSuccess = { category ->
                    Log.d(
                        TAG,
                        "Category created: ${category.categoryName} at level ${category.level}"
                    )
                    _categoryState.value = _categoryState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )

                    val levelText = when (category.level) {
                        0 -> "root category"
                        1 -> "subcategory"
                        else -> "level ${category.level} category"
                    }

                    _snackBarData.emit(
                        SnackBarData("${category.categoryName} created as $levelText")
                    )
                    loadCategories()
                    loadCategoryTree()
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to create category: ${exception.message}")
                    _categoryState.value = _categoryState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to create category",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Update category
     */
    fun updateCategory(
        categoryId: String,
        categoryName: String,
        categoryImage: String = "",
        description: String = ""
    ) {
        if (categoryName.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Category name cannot be empty", "Error"))
            }
            return
        }

        viewModelScope.launch {
            _categoryState.value = _categoryState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            categoryRepository.updateCategory(
                categoryId = categoryId,
                categoryName = categoryName,
                categoryImage = categoryImage,
                description = description
            ).fold(
                onSuccess = { category ->
                    Log.d(TAG, "Category updated: ${category.categoryName}")
                    _categoryState.value = _categoryState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Category updated successfully"))
                    loadCategories()
                    loadCategoryTree()
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to update category: ${exception.message}")
                    _categoryState.value = _categoryState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to update category",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Delete category
     */
    fun deleteCategory(categoryId: String, categoryName: String) {
        viewModelScope.launch {
            _categoryState.value = _categoryState.value.copy(
                isLoading = true,
                error = null
            )

            categoryRepository.deleteCategory(categoryId).fold(
                onSuccess = {
                    Log.d(TAG, "Category deleted: $categoryName")
                    _categoryState.value = _categoryState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Category '$categoryName' deleted successfully"))
                    loadCategories()
                    loadCategoryTree()
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to delete category: ${exception.message}")
                    _categoryState.value = _categoryState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to delete category",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Search categories
     */
    fun searchCategories(query: String) {
        if (query.isBlank()) {
            loadCategories()
            return
        }

        viewModelScope.launch {
            _categoryState.value = _categoryState.value.copy(
                isLoading = true,
                error = null
            )

            categoryRepository.searchCategories(query).fold(
                onSuccess = { categories ->
                    Log.d(TAG, "Search results: ${categories.size}")
                    _categoryState.value = _categoryState.value.copy(
                        isLoading = false,
                        categories = categories,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to search categories: ${exception.message}")
                    _categoryState.value = _categoryState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

}

/**
 * CategoryState - UI State for Category Management
 *
 * @property isLoading Loading indicator for background operations
 * @property categories Flat list of all available categories
 * @property categoryTree Hierarchical tree structure of categories
 * @property rootCategories List of top-level categories (level 0)
 * @property selectedSubcategories List of subcategories for a specific parent
 * @property currentBreadcrumb Path from root to selected category
 * @property error Error message if operations fail
 * @property isSuccess Success flag for CRUD operations
 */
data class CategoryState(
    val isLoading: Boolean = false,
    val categories: List<CategoryItem> = emptyList(),
    val categoryTree: List<CategoryNode> = emptyList(),
    val rootCategories: List<CategoryItem> = emptyList(),
    val selectedSubcategories: List<CategoryItem> = emptyList(),
    val currentBreadcrumb: List<CategoryBreadcrumb> = emptyList(),
    val error: String? = null,
    val isSuccess: Boolean = false
)