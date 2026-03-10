package com.example.myapp.view.utils

import kotlin.math.roundToInt

/**
 * toStripeCents - Extension property to convert dollar amounts to Stripe cents
 * 
 * Converts a dollar amount (Double) to cents (Int) for Stripe API compatibility.
 * Stripe requires amounts to be specified in the smallest currency unit (cents for USD).
 * 
 * ## Conversion Logic
 * - Multiplies dollar amount by 100
 * - Rounds to nearest integer using roundToInt()
 * - Returns integer value in cents
 * 
 * ## Usage Examples
 * ```kotlin
 * val price = 19.99
 * val stripeCents = price.toStripeCents // Returns 1999
 * 
 * val total = 100.00
 * val stripeCents = total.toStripeCents // Returns 10000
 * 
 * val discounted = 15.50
 * val stripeCents = discounted.toStripeCents // Returns 1550
 * ```
 * 
 * ## Important Notes
 * - Uses roundToInt() for safe rounding (avoids floating-point precision issues)
 * - Stripe expects amounts in cents (smallest currency unit)
 * - For USD: $1.00 = 100 cents
 * - For other currencies, adjust accordingly
 * 
 * @receiver Double The dollar amount to convert
 * @return Int The amount in cents (smallest currency unit)
 *
 */
val Double.toStripeCents: Int
    get() = (this * 100.0).roundToInt() // Use roundToInt() for safer rounding
