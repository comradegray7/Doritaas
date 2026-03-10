package com.example.myapp.data.dataclass

/**
 * ShipmentItem - Shipping method data model
 * 
 * Represents a shipping option available to customers during checkout.
 * 
 * @property id Unique shipment method identifier
 * @property name Display name (e.g., "Express Delivery")
 * @property deliveryMethod Description of method (e.g., "Air Freight")
 * @property price Cost of shipping
 */
data class ShipmentItem(
    val id: String = "",
    val name: String = "",
    val deliveryMethod: String = "",
    val price: Double = 0.0
)
