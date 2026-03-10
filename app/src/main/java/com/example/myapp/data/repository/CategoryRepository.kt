package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.dataclass.CategoryHelper
import com.example.myapp.data.dataclass.CategoryItem
import com.example.myapp.data.dataclass.CategoryNode
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

/**
 * CategoryRepository
 *
 * Interface defining the contract for operations on product categories, including
 * fetching root categories, subcategories, and building a hierarchical category tree.
 */
interface CategoryRepository {
    suspend fun getCategories(): Result<List<CategoryItem>>
    suspend fun getSubcategories(parentId: String): Result<List<CategoryItem>>
    suspend fun getCategoryById(categoryId: String): Result<CategoryItem>
    suspend fun getCategoryTree(): Result<List<CategoryNode>>

    suspend fun createCategory(
        categoryName: String,
        parentId: String? = null,
        categoryImage: String = "",
        description: String = ""
    ): Result<CategoryItem>

    suspend fun updateCategory(
        categoryId: String,
        categoryName: String,
        categoryImage: String = "",
        description: String = ""
    ): Result<CategoryItem>

    suspend fun deleteCategory(categoryId: String): Result<Unit>
    suspend fun searchCategories(query: String): Result<List<CategoryItem>>
}

/**
 * CategoryRepositoryImpl
 *
 * Implementation of [CategoryRepository] using Firebase Firestore.
 * Handles complex hierarchical logic such as path building and recursive tree generation.
 */
class CategoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    private val categoriesCollection = firestore.collection("categories")

    companion object {
        private const val TAG = "CategoryRepository"
    }

    //  Get all categories
    override suspend fun getCategories(): Result<List<CategoryItem>> {
        return try {
            Log.d(TAG, "Fetching all categories from Firestore...")

            val snapshot = categoriesCollection
                .orderBy("level")
                .orderBy("displayOrder")
                .get()
                .await()

            val categories = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CategoryItem::class.java)
            }

            Log.d(TAG, "Successfully fetched ${categories.size} categories")
            Result.success(categories)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching categories: ${e.message}", e)
            Result.failure(e)
        }
    }

    //  Get subcategories of a specific parent
    override suspend fun getSubcategories(parentId: String): Result<List<CategoryItem>> {
        return try {
            val snapshot = categoriesCollection
                .whereEqualTo("parentId", parentId)
                .orderBy("displayOrder")
                .get()
                .await()

            val categories = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CategoryItem::class.java)
            }

            Log.d(TAG, "Fetched ${categories.size} subcategories for parent: $parentId")
            Result.success(categories)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching subcategories: ${e.message}", e)
            Result.failure(e)
        }
    }

    //  Get category by ID
    override suspend fun getCategoryById(categoryId: String): Result<CategoryItem> {
        return try {
            val snapshot = categoriesCollection.document(categoryId)
                .get()
                .await()

            val category = snapshot.toObject(CategoryItem::class.java)

            if (category != null) {
                Result.success(category)
            } else {
                Result.failure(Exception("Category not found"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch category: ${e.message}"))
        }
    }

    //  Get full category tree
    override suspend fun getCategoryTree(): Result<List<CategoryNode>> {
        return try {
            val allCategories = getCategories().getOrThrow()
            val tree = buildCategoryTree(allCategories, null, 0)
            Result.success(tree)
        } catch (e: Exception) {
            Log.e(TAG, "Error building category tree: ${e.message}", e)
            Result.failure(e)
        }
    }

    //   Helper: Build category tree recursively
    private fun buildCategoryTree(
        allCategories: List<CategoryItem>,
        parentId: String?,
        depth: Int
    ): List<CategoryNode> {
        return allCategories
            .filter { it.parentId == parentId }
            .sortedBy { it.displayOrder }
            .map { category ->
                val subcategories = buildCategoryTree(allCategories, category.id, depth + 1)
                CategoryNode(
                    category = category,
                    subcategories = subcategories,
                    isExpanded = false,
                    depth = depth
                )
            }
    }

    //   Create category with hierarchy support
    override suspend fun createCategory(
        categoryName: String,
        parentId: String?,
        categoryImage: String,
        description: String
    ): Result<CategoryItem> {
        return try {
            // Generate deterministic ID based on parent + name to prevent duplicates
            val categoryId = if (parentId != null) {
                "${parentId}_${categoryName.lowercase().replace(" ", "_")}"
            } else {
                categoryName.lowercase().replace(" ", "_")
            }

            // Check if category already exists using document ID
            val existingDoc = categoriesCollection.document(categoryId).get().await()
            if (existingDoc.exists()) {
                return Result.failure(Exception("Category '$categoryName' already exists at this level"))
            }

            // Get parent category info if exists
            val parent = if (parentId != null) {
                getCategoryById(parentId).getOrNull()
            } else null

            val level = (parent?.level ?: -1) + 1
            val parentPath = parent?.path
            val parentBreadcrumb = parent?.breadcrumb ?: emptyList()

            val path = CategoryHelper.buildPath(parentPath, categoryName)
            val breadcrumb = CategoryHelper.buildBreadcrumb(parentBreadcrumb, categoryName)
            val slug = CategoryHelper.buildSlug(path)

            // Get display order from sibling count
            val siblingsSnapshot = if (parentId != null) {
                categoriesCollection.whereEqualTo("parentId", parentId).get().await()
            } else {
                categoriesCollection.whereEqualTo("level", 0).get().await()
            }

            val category = CategoryItem(
                id = categoryId,
                categoryName = categoryName,
                categoryImage = categoryImage,
                description = description,
                parentId = parentId,
                level = level,
                path = path,
                breadcrumb = breadcrumb,
                slug = slug,
                hasSubcategories = false,
                subcategoryIds = emptyList(),
                displayOrder = siblingsSnapshot.documents.size
            )

            categoriesCollection.document(categoryId)
                .set(category)
                .await()

            // Update parent's hasSubcategories flag
            if (parentId != null) {
                updateParentSubcategoryInfo(parentId, categoryId, true)
            }

            Log.d(TAG, "Created category: $categoryName at level $level")
            Result.success(category)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create category: ${e.message}", e)
            Result.failure(Exception("Failed to create category: ${e.message}"))
        }
    }

    // Update parent's subcategory info
    private suspend fun updateParentSubcategoryInfo(
        parentId: String,
        subcategoryId: String,
        isAdding: Boolean
    ) {
        try {

            val parent = getCategoryById(parentId).getOrNull() ?: return

            val updatedSubcategoryIds = if (isAdding) {
                parent.subcategoryIds + subcategoryId
            } else {
                parent.subcategoryIds - subcategoryId
            }

            categoriesCollection.document(parentId).update(
                mapOf(
                    "hasSubcategories" to updatedSubcategoryIds.isNotEmpty(),
                    "subcategoryIds" to updatedSubcategoryIds
                )
            ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating parent subcategory info: ${e.message}")
        }
    }

    //  Update category
    override suspend fun updateCategory(
        categoryId: String,
        categoryName: String,
        categoryImage: String,
        description: String
    ): Result<CategoryItem> {
        return try {
            val existingCategory = getCategoryById(categoryId).getOrNull()
                ?: return Result.failure(Exception("Category not found"))

            // Check for duplicates using deterministic ID (only if name is changing)
            if (!existingCategory.categoryName.equals(categoryName, ignoreCase = true)) {
                val newCategoryId = if (existingCategory.parentId != null) {
                    "${existingCategory.parentId}_${categoryName.lowercase().replace(" ", "_")}"
                } else {
                    categoryName.lowercase().replace(" ", "_")
                }

                val existingDoc = categoriesCollection.document(newCategoryId).get().await()
                if (existingDoc.exists()) {
                    return Result.failure(Exception("Category '$categoryName' already exists at this level"))
                }
            }

            // Rebuild path and breadcrumb if name changed
            val parentPath = if (existingCategory.parentId != null) {
                getCategoryById(existingCategory.parentId).getOrNull()?.path
            } else null

            val newPath = CategoryHelper.buildPath(parentPath, categoryName)
            val newBreadcrumb = if (existingCategory.parentId != null) {
                val parent = getCategoryById(existingCategory.parentId).getOrNull()
                CategoryHelper.buildBreadcrumb(parent?.breadcrumb ?: emptyList(), categoryName)
            } else {
                listOf(categoryName)
            }
            val newSlug = CategoryHelper.buildSlug(newPath)

            val updates = mapOf(
                "categoryName" to categoryName,
                "categoryImage" to categoryImage,
                "description" to description,
                "path" to newPath,
                "breadcrumb" to newBreadcrumb,
                "slug" to newSlug,
                "updatedAt" to System.currentTimeMillis()
            )

            categoriesCollection.document(categoryId)
                .update(updates)
                .await()

            // Update all subcategories' paths and breadcrumbs
            if (existingCategory.hasSubcategories) {
                updateSubcategoriesPaths(categoryId, newPath, newBreadcrumb)
            }

            val updatedCategory = existingCategory.copy(
                categoryName = categoryName,
                categoryImage = categoryImage,
                description = description,
                path = newPath,
                breadcrumb = newBreadcrumb,
                slug = newSlug,
                updatedAt = System.currentTimeMillis()
            )

            Log.d(TAG, "Updated category: $categoryName")
            Result.success(updatedCategory)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update category: ${e.message}", e)
            Result.failure(Exception("Failed to update category: ${e.message}"))
        }
    }

    //  Update subcategories paths recursively
    private suspend fun updateSubcategoriesPaths(
        parentId: String,
        newParentPath: String,
        newParentBreadcrumb: List<String>
    ) {
        try {
            val subcategories = getSubcategories(parentId).getOrNull() ?: return

            subcategories.forEach { subcategory ->
                val newPath = "$newParentPath/${CategoryHelper.buildSlug(subcategory.categoryName)}"
                val newBreadcrumb = newParentBreadcrumb + subcategory.categoryName
                val newSlug = CategoryHelper.buildSlug(newPath)

                categoriesCollection.document(subcategory.id).update(
                    mapOf(
                        "path" to newPath,
                        "breadcrumb" to newBreadcrumb,
                        "slug" to newSlug
                    )
                ).await()

                // Recursively update children
                if (subcategory.hasSubcategories) {
                    updateSubcategoriesPaths(subcategory.id, newPath, newBreadcrumb)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating subcategory paths: ${e.message}")
        }
    }

    //  Delete category
    override suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            val category = getCategoryById(categoryId).getOrNull()
                ?: return Result.failure(Exception("Category not found"))

            // Check if category has subcategories
            if (category.hasSubcategories) {
                return Result.failure(Exception("Cannot delete category with subcategories. Delete subcategories first."))
            }

            // Check if category has products
            val productsSnapshot = firestore.collection("products")
                .whereEqualTo("category", category.categoryName)
                .limit(1)
                .get()
                .await()

            if (!productsSnapshot.isEmpty) {
                return Result.failure(Exception("Cannot delete category with products. Remove products first."))
            }

            // Delete category
            categoriesCollection.document(categoryId)
                .delete()
                .await()

            // Update parent
            if (category.parentId != null) {
                updateParentSubcategoryInfo(category.parentId, categoryId, false)
            }

            Log.d(TAG, "Deleted category: ${category.categoryName}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete category: ${e.message}", e)
            Result.failure(Exception("Failed to delete category: ${e.message}"))
        }
    }

    //   Search categories
    override suspend fun searchCategories(query: String): Result<List<CategoryItem>> {
        return try {
            val snapshot = categoriesCollection.get().await()

            val categories = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CategoryItem::class.java)
            }.filter { category ->
                category.categoryName.contains(query, ignoreCase = true) ||
                        category.breadcrumb.any { it.contains(query, ignoreCase = true) }
            }

            Log.d(TAG, "Search found ${categories.size} categories for query: $query")
            Result.success(categories)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search categories: ${e.message}", e)
            Result.failure(Exception("Failed to search categories: ${e.message}"))
        }
    }
}