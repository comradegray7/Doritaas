package com.example.myapp.data

import com.example.myapp.data.dataclass.MembershipStatus
import com.example.myapp.data.repository.PrimeMembershipRepository
import com.google.firebase.auth.FirebaseAuth
import jakarta.inject.Inject

// ============================================
// SERVICE TO AUTOMATICALLY APPLY PRIME BENEFITS
// ============================================

/**
 * PrimeBenefitsService
 *
 * Service class responsible for automatically applying Prime membership benefits.
 * Determines available shipping options and pricing based on the user's Prime membership status.
 *
 * @property primeMembershipRepository Repository for accessing Prime membership data
 * @property auth Firebase authentication instance for user identification
 */
class PrimeBenefitsService @Inject constructor(
    private val primeMembershipRepository: PrimeMembershipRepository,
    private val auth: FirebaseAuth
) {
    /**
     * Get shipping options based on Prime membership
     */
    suspend fun getShippingOptions(userId: String): List<ShippingOption> {
        val isPrime = primeMembershipRepository.getMembership(userId).fold(
            onSuccess = { it?.status == MembershipStatus.ACTIVE },
            onFailure = { false }
        )

        return if (isPrime) {
            listOf(
                ShippingOption("prime_free", "Prime Free Shipping", 0.0, "2-3 business days", isPrime = true),
                ShippingOption("prime_next_day", "Prime Next Day", 5.99, "Next business day", isPrime = true),
                ShippingOption("prime_same_day", "Prime Same Day", 9.99, "Same day delivery", isPrime = true)
            )
        } else {
            listOf(
                ShippingOption("standard", "Standard Shipping", 5.99, "5-7 business days", isPrime = false),
                ShippingOption("express", "Express Shipping", 12.99, "2-3 business days", isPrime = false),
                ShippingOption("next_day", "Next Day Delivery", 19.99, "Next business day", isPrime = false)
            )
        }
    }

}

/**
 * ShippingOption
 *
 * Data class representing a shipping option available to users.
 * Contains information about delivery method, cost, estimated delivery time,
 * and whether it's exclusive to Prime members.
 *
 * @property id Unique identifier for the shipping option
 * @property name Display name of the shipping option
 * @property cost Cost of shipping in the default currency
 * @property estimatedDelivery Estimated delivery timeframe as a human-readable string
 * @property isPrime Whether this shipping option is exclusive to Prime members
 */
data class ShippingOption(
    val id: String,
    val name: String,
    val cost: Double,
    val estimatedDelivery: String,
    val isPrime: Boolean = false
)

