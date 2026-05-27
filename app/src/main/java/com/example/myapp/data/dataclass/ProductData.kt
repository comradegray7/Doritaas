package com.example.myapp.data.dataclass

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

/**
 * ProductItem - Core product data model
 * 
 * Represents a complete product with pricing, inventory, media, variants, and user state.
 * Used throughout the app for product display, cart, favorites, and admin management.
 * 
 * @property id Unique product identifier (Firestore document ID)
 * @property userId Creator/owner user ID
 * @property inStock Availability status
 * @property productName Display name
 * @property price Current selling price (USD)
 * @property brand Brand name
 * @property category Category name
 * @property shipment Default shipping method
 * @property sizes Available size variants
 * @property colors Available color variants
 * @property description Product description
 * @property imageUrl Primary product image
 * @property supportingImageUrls Additional product images
 * @property rating Average rating (0-5 stars)
 * @property oldPrice Original price (for discount calculation)
 * @property reviewCount Number of reviews
 * @property quantity Available inventory
 * @property isFavorite User favorite status
 * @property isInCart User cart status
 * @property selectedSize User-selected size
 * @property selectedColor User-selected color
 * @property selectedShipment User-selected shipping
 * @property shipmentCost Calculated shipping cost
 * @property createdAt Creation timestamp
 * @property updatedAt Last update timestamp
 * @property topReviews Top product reviews
 * 
 */

@Parcelize
data class ProductItem(
    val id: String = "",
    val userId: String = "",
    val inStock: Boolean = true,
    val productName: String = "",
    val price: Double = 0.0,
    val brand : String = "",
    val category: String = "",
    val shipment: String = "",
    val sizes: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val description: String = "",
    val imageUrl: String = "",
    val supportingImageUrls: List<String> = emptyList(),
    val rating: Float = 0f,
    val oldPrice: Double = 0.0,
    val reviewCount: Int = 0,
    val quantity: Int = 0,
    val isFavorite: Boolean = false,
    val isInCart: Boolean = false,
    val selectedSize: String = "",
    val selectedColor: String = "",
    val selectedShipment: String = "",
    val shipmentCost: Double = 0.0,
    val createdAt: Timestamp? = Timestamp.now(),
    val updatedAt: Timestamp? =  Timestamp.now(),
    val categoryId: String = "",
    val categoryPath: String = "", // "electronics/pc/desktop"
    val categoryBreadcrumb: List<String> = emptyList(), // ["Electronics", "PC", "Desktop"]
    val categoryLevel: Int = 0,
    val isPrimeEligible: Boolean = false, // Can this product use Prime benefits?
    val primeDiscountApplied: Double = 0.0, // Applied Prime discount amount
    val originalShippingCost: Double = 0.0, // Original shipping before Prime benefits
    val tags: List<String> = emptyList(), // Include "prime_eligible" tag
    val topReviews: List<Review> = emptyList(),
    val activePromotions: List<PromotionInfo> = emptyList(), // Fetched on demand
    val bestPromotionDiscount: Double = 0.0, // Calculated from activePromotions
    val promotionalPrice: Double = price // price - bestPromotionDiscount
) : Parcelable

/**
 * SupportingImageData
 *
 * Represents additional image data for a product during creation/editing, including upload status.
 */
data class SupportingImageData(
    val id: String, // Unique identifier
    val uri: Uri,
    val cloudinaryUrl: String? = null,
    val isUploading: Boolean = false
)

/**
 * Review - Product review data model
 * 
 * Represents a user review for a product, including rating, text, and user details.
 * 
 * @property id Unique review identifier
 * @property userId ID of the user who wrote the review
 * @property userName Display name of the user
 * @property userProfileImage Profile image URL of the user
 * @property productId ID of the product being reviewed
 * @property rating Star rating (0-5)
 * @property review Text content of the review
 * @property timestamp Time when review was created
 * @property updatedAt Time when review was last updated
 * @property helpful Count of users who found this review helpful
 * @property verified Boolean flag for verified purchase
 */
@Parcelize
data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String = "",
    val productId: String = "",
    val rating: Float = 0f,
    val review: String = "",
    val timestamp: Timestamp? = Timestamp.now(),
    val updatedAt: Timestamp? = Timestamp.now(),
    val helpful: Int = 0, // Number of users who found this helpful
    val verified: Boolean = false // Verified purchase badge
) : Parcelable

