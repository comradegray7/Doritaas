package com.example.myapp.data.dataclass

// ============================================
// UPDATED CATEGORY DATA MODEL
// ============================================

/**
 * CategoryItem
 *
 * Represents a product category in the store’s classification system.
 * Supporting hierarchical structures with parent-child relationships,
 * metadata for SEO, and display ordering.
 *
 * @property id Unique identifier for the category
 * @property categoryName Display name of the category
 * @property categoryImage URL or path to the category image
 * @property description Detailed description of the category
 * @property parentId ID of the parent category, null if this is a root category
 * @property level Depth in the category hierarchy (0 for root)
 * @property path Full path string representing the hierarchy position
 * @property breadcrumb List of names representing the path from root to this category
 * @property hasSubcategories Flag indicating if this category has children
 * @property subcategoryIds List of direct child category IDs
 * @property productCount Total number of products in this category
 * @property displayOrder Sorting priority for display
 * @property isActive Flag to enable or disable the category
 * @property isFeatured Flag to showcase the category prominently
 * @property slug URL-safe version of the category name
 * @property keywords List of search keywords for discovery
 * @property createdAt Timestamp of creation
 * @property updatedAt Timestamp of last update
 */
data class CategoryItem(
    val id: String = "",
    val categoryName: String = "",
    val categoryImage: String = "",
    val description: String = "",

    val parentId: String? = null, // null for root categories
    val level: Int = 0, // 0 = root (Electronics), 1 = first level (PC), 2 = second level (Desktop PC)
    val path: String = "", // Full path: "electronics/pc/desktop-pc"
    val breadcrumb: List<String> = emptyList(), // ["Electronics", "PC", "Desktop PC"]

    val hasSubcategories: Boolean = false,
    val subcategoryIds: List<String> = emptyList(), // IDs of direct children
    val productCount: Int = 0,

    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val isFeatured: Boolean = false,

    val slug: String = "", // URL-friendly: "electronics-pc-desktop-pc"
    val keywords: List<String> = emptyList(),

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

//  ategory tree node for UI display
/**
 * CategoryNode
 *
 * A recursive data structure used to represent a category and its entire subcategory tree.
 * Primarily used for rendering nested UI components like navigation menus or tree views.
 */
data class CategoryNode(
    val category: CategoryItem,
    val subcategories: List<CategoryNode> = emptyList(),
    val isExpanded: Boolean = false,
    val depth: Int = 0
)

//  Category breadcrumb for navigation
/**
 * CategoryBreadcrumb
 *
 * A simplified category model used specifically for navigation breadcrumb components.
 */
data class CategoryBreadcrumb(
    val id: String,
    val name: String,
    val level: Int
)

//  Helper object for category operations
/**
 * CategoryHelper
 *
 * Utility object for common category-related operations such as building paths, slugs,
 * and breadcrumbs.
 */
object CategoryHelper {
    /**
     * Build category path from parent path and current name
     * Example: buildPath("electronics/pc", "Desktop PC") → "electronics/pc/desktop-pc"
     */
    fun buildPath(parentPath: String?, categoryName: String): String {
        val slug = categoryName.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')

        return if (parentPath.isNullOrEmpty()) {
            slug
        } else {
            "$parentPath/$slug"
        }
    }

    /**
     * Build breadcrumb list
     */
    fun buildBreadcrumb(parentBreadcrumb: List<String>, categoryName: String): List<String> {
        return parentBreadcrumb + categoryName
    }

    /**
     * Build slug from category name
     */
    fun buildSlug(categoryName: String): String {
        return categoryName.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }
}