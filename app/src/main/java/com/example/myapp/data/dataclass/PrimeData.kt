package com.example.myapp.data.dataclass

import com.example.myapp.data.ShippingOption

// ============================================
// DATA MODELS
// ============================================

/**
 * PrimeMembership
 *
 * Represents a user's Prime membership subscription details, including type,
 * duration, benefits, and current status.
 */
data class PrimeMembership(
    val userId: String = "",
    val membershipType: MembershipType = MembershipType.NONE,
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val autoRenew: Boolean = true,
    val paymentMethod: String = "",
    val benefits: PrimeBenefits = PrimeBenefits(),
    val status: MembershipStatus = MembershipStatus.INACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * MembershipType
 *
 * Defines the available tiers of Prime membership (None, Monthly, or Annual)
 * and their respective pricing.
 */
enum class MembershipType(val displayName: String, val monthlyPrice: Double, val annualPrice: Double) {
    NONE("None", 0.0, 0.0),
    MONTHLY("Monthly Prime", 9.99, 0.0),
    ANNUAL("Annual Prime", 0.0, 99.0);

    /**
     * Calculates the duration of the membership type in milliseconds.
     */
    fun getDurationInMillis(): Long {
        return when (this) {
            MONTHLY -> 30L * 24 * 60 * 60 * 1000 // 30 days
            ANNUAL -> 365L * 24 * 60 * 60 * 1000 // 365 days
            NONE -> 0L
        }
    }
}

/**
 * MembershipStatus
 *
 * Represents the current lifecycle state of a Prime membership.
 */
enum class MembershipStatus {
    ACTIVE,
    INACTIVE,
    EXPIRED,
    CANCELLED,
}

/**
 * PrimeBenefits
 *
 * Configuration for the specific benefits enabled for a Prime member,
 * such as free shipping, discounts, and rewards.
 */
data class PrimeBenefits(
    val freeShipping: Boolean = true,
    val exclusiveDiscountPercentage: Int = 20, // Extra 20% off
    val earlyAccessHours: Int = 24, // 24 hours early access to deals
    val primeRewardsMultiplier: Double = 2.0, // 2x points on purchases
    val freeReturns: Boolean = true,
    val prioritySupport: Boolean = true
)

/**
 * PrimeTransaction
 *
 * Represents a financial transaction related to Prime membership purchase or renewal.
 */
data class PrimeTransaction(
    val id: String = "",
    val userId: String = "",
    val membershipType: MembershipType = MembershipType.NONE,
    val amount: Double = 0.0,
    val paymentMethod: String = "",
    val transactionDate: Long = System.currentTimeMillis(),
    val status: TransactionStatus = TransactionStatus.PENDING,
    val receiptUrl: String = "",
    val description: String = ""
)

/**
 * TransactionStatus
 *
 * Defines the status of a Prime membership payment transaction.
 */
enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
}

// Track benefits usage
/**
 * BenefitUsage
 *
 * Records an instance where a user utilized their Prime benefits during a checkout.
 */
data class BenefitUsage(
    val userId: String = "",
    val benefitType: BenefitType = BenefitType.FREE_SHIPPING,
    val orderId: String = "",
    val discountAmount: Double = 0.0,
    val usedAt: Long = System.currentTimeMillis()
)

/**
 * BenefitType
 *
 * Categorizes the different types of Prime benefits available.
 */
enum class BenefitType {
    FREE_SHIPPING,
    EXCLUSIVE_DISCOUNT,
    PRIME_REWARDS,
}

/**
 * AppliedBenefit
 *
 * Represents a specific benefit applied to a transaction or order, including the calculated savings.
 */
data class AppliedBenefit(
    val benefitType: BenefitType,
    val description: String,
    val savingsAmount: Double
)

/**
 * CheckoutSummary
 *
 * A comprehensive model summarizing the costs, discounts, and Prime benefits for a checkout session.
 */
data class CheckoutSummary(
    val items: List<ProductItem>,
    val subtotal: Double,
    val tax: Double,
    val shippingCost: Double,
    val discount: Double,
    val total: Double,

    //   Prime information
    val isPrimeOrder: Boolean,
    val primeDiscountAmount: Double,
    val primeShippingSaved: Double,
    val primeTotalSavings: Double,
    val primeRewardsPoints: Double,
    val appliedBenefits: List<AppliedBenefit>,

    val shippingOptions: List<ShippingOption>,
    val estimatedDelivery: String
)
