package com.example.myapp.view.utils

import java.util.Locale
import kotlin.math.max

/**
 * calculateDiscountedPrice
 *
 *
 * @param newPrice The newPrice parameter
 * @param oldPrice The oldPrice parameter
 */
fun calculateDiscountedPrice(newPrice: Double, oldPrice: Double): Double {
    // Handle edge cases
    if (oldPrice <= 0) return 0.0
    if (newPrice <= 0) return 0.0

    // Calculate discount percentage
    val discountPercentage = ((oldPrice - newPrice) / oldPrice) * 100

    // Return only positive discounts (you can't have "negative" discount)
    return max(0.0, discountPercentage)
}

/**
 * formatPrice
 *
 * @param price The price parameter
 */
fun formatPrice(price: Double): String {
    return "$${String.format(Locale.US, "%.2f", price)}"
}
