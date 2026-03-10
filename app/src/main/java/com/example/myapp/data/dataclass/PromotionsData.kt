package com.example.myapp.data.dataclass


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import android.os.Parcelable
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize

/**
 * SpecialOffer - Limited time special promotion
 * 
 * Represents a high-priority special offer often displayed in banners.
 * 
 * @property id Unique offer identifier
 * @property title Offer headline
 * @property description Details of the special offer
 */
@Parcelize
data class PromotionsData(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startAt: Long = System.currentTimeMillis(),
    val endAt: Long = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val expired: Boolean = false,
    val discountType: DiscountType = DiscountType.PERCENTAGE,
    val discountValue: Double = 0.0, // 20% or $20
    val minPurchaseAmount: Double = 0.0, // Minimum cart value
    val maxDiscountAmount: Double = 0.0, // Cap for percentage discounts
    val applicableCategories: List<String> = emptyList(), // Empty = all categories
    val applicableTags: List<String> = emptyList(), // e.g., ["flash_deal", "clearance"]
    val isActive: Boolean = true, // Manual toggle
    val usageLimit: Int = 0, // 0 = unlimited
    val usageCount: Int = 0,
    val priority: Int = 0 // Higher priority = applied first
) : Parcelable

/**
 * DiscountType
 *
 * Defines the calculation method for a discount.
 */
enum class DiscountType {
    PERCENTAGE, // 20% off
    FIXED_AMOUNT, // $20 off
    BUY_X_GET_Y, // Buy 2 Get 1 Free
    FREE_SHIPPING
}

//  Junction Collection Document
@Parcelize
data class PromotionProduct(
    val id: String = "", // Auto-generated
    val promotionId: String = "",
    val productId: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val addedBy: String = "" // Admin user ID
) : Parcelable

//  For UI display
@Parcelize
data class PromotionInfo(
    val id: String = "",
    val title: String = "",
    val discountType: DiscountType = DiscountType.PERCENTAGE,
    val discountValue: Double = 0.0,
    val badge: String = "", // "20% OFF", "FLASH DEAL"
    val endAt: Long = 0L
) : Parcelable

/**
 * Offer - General promotional offer
 * 
 * Represents a standard promotional offer with visual customization.
 * 
 * @property id Unique offer identifier
 * @property title Offer title
 * @property description Offer description
 * @property buttonText CTA button text
 * @property gradient Background gradient colors
 * @property android.R.id.icon Vector icon for the offer
 */

data class Offer(
    val id: String = "",
    val title: String = "",
    val description: String? = "",
    val buttonText: String? = "",
    val gradient: List<Color> = emptyList(),
    val leadingIcon: ImageVector? = Icons.Default.LocalOffer,
    val onClick: () -> Unit = {},
    val composableFunction: (@Composable () -> Unit)? = null
)
