package com.example.myapp.data.dataclass

import com.google.firebase.Timestamp

/**
 * Order - Complete order data model
 *
 * Represents a customer order, containing line items, payment status, shipping details,
 * and current processing status. Used in order history, management, and confirmation.
 *
 * @property id Unique order identifier (Firestore document ID)
 * @property userId ID of the user who placed the order
 * @property userEmail Contact email for the order
 * @property userName Name of the user
 * @property items List of products included in the order
 * @property totalAmount Total order cost including tax and shipping
 * @property subtotal Sum of product prices
 * @property taxAmount Calculated tax
 * @property shippingCost Cost of shipping
 * @property status Order lifecycle status (pending, confirmed, processing, shipped, delivered, cancelled)
 * @property paymentStatus Status of payment (pending, paid, failed, refunded)
 * @property paymentMethod Method used for payment
 * @property paymentIntentId Stripe payment intent ID
 * @property shippingAddress Delivery address details
 * @property billingAddress Billing address details
 * @property createdAt Order placement timestamp
 * @property updatedAt Last update timestamp
 * @property estimatedDelivery Estimated delivery date
 * @property trackingNumber Shipping tracking number
 * @property notes Additional order notes
 */

data class Order(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val discount: Double = 0.0,
    val subtotal: Double = 0.0,
    val isPrimeEligible: Boolean = false, // Can this product use Prime benefits?
    val primeDiscountApplied: Double = 0.0, // Applied Prime discount amount
    val originalShippingCost: Double = 0.0, // Original shipping before Prime benefits
    val tags: List<String> = emptyList(), // Include "prime_eligible" tag
    val taxAmount: Double = 0.0,
    val shippingCost: Double = 0.0,
    val status: String = "pending", // pending, confirmed, processing, shipped, delivered, cancelled
    val paymentStatus: String = "pending", // pending, paid, failed, refunded
    val paymentMethod: String = "",
    val paymentIntentId: String = "", // Stripe payment intent ID
    val shippingAddress: DeliveryAddress? = null,
    val billingAddress: DeliveryAddress? = null,
    val createdAt: Timestamp? = Timestamp.now(),
    val updatedAt: Timestamp? = Timestamp.now(),
    val estimatedDelivery: Timestamp? = null,
    val trackingNumber: String = "",
    val notes: String = "",
)

/**
 * OrderItem - Individual product line item in an order
 *
 * Represents a specific product, its quantity, and price at the time of purchase.
 *
 * @property productId ID of the ordered product
 * @property productName Name of the product
 * @property quantity Number of units ordered
 * @property price Unit price at purchase time
 */
data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val isPrimeEligible: Boolean = false,
    val primeDiscountApplied: Double = 0.0, // Prime discount on this item
    val primeShippingSaved: Double = 0.0, // Shipping saved on this item

    val imageUrl: String = "",
    val selectedSize: String = "",
    val selectedColor: String = "",
    val selectedShipment: String = "",
    val brand: String = "",
    val category: String = "",
    val tags: List<String>? = emptyList(),

    )