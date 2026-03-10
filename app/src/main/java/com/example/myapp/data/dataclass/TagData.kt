package com.example.myapp.data.dataclass

import com.google.firebase.Timestamp

// ============================================================================
// 1. TAG DATA MODEL
// ============================================================================

/**
 * ProductTag
 *
 * Represents a label applied to products for categorization, filtering, and visual badges.
 * Supports system-defined tags and user-defined categories with UI-specific metadata like colors and icons.
 */
data class ProductTag(
    val id: String = "",
    val name: String = "", // e.g., "prime_eligible", "new_arrival"
    val displayName: String = "", // e.g., "Prime Eligible", "New Arrival"
    val description: String = "",
    val color: String = "#2196F3", // Hex color for UI display
    val icon: String = "label", // Icon name (optional)
    val category: TagCategory = TagCategory.GENERAL,
    val isSystemTag: Boolean = false, // System tags can't be deleted
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

/**
 * TagCategory
 *
 * Categorizes tags to group related labels such as promotional, membership-related, or product status.
 */
enum class TagCategory {
    GENERAL,        // General purpose tags
    PROMOTION,      // Sale, discount, limited time
    MEMBERSHIP,     // Prime eligible, VIP only
    STATUS,         // New arrival, trending, bestseller
    FEATURE         // Eco-friendly, handmade, organic
}