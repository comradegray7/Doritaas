package com.example.myapp.data

/**
 * FirestoreCollections
 *
 * Singleton object containing constant string values for all Firestore collection names.
 * Centralizes collection name references to ensure consistency across the application
 * and reduce the risk of typos when accessing Firestore collections.
 */
object FirestoreCollections {

    //product tags
    const val PRODUCT = "products"
    const val SHIPMENTS = "shipment_option"
    const val RATINGS = "ratings"
    const val PROMOTIONS = "promotions"
    const val ORDERS = "orders"
    const val BRANDS = "brands"
    const val COLORS = "colors"
    const val SIZES = "sizes"
    const val PROMOTION_PRODUCTS = "promotion_products"
    const val PRODUCT_TAGS = "product_tags"
    const val DELIVERY_ADDRESSES = "deliveryAddresses"
    const val FAVOURITES = "favorites"
    const val CART = "cart"
    const val IMAGE_CAROUSEL = "image_carousel"
    const val USERS = "users"
    const val PRIME_MEMBERSHIPS = "prime_memberships"
    const val TRANSACTIONS = "prime_transactions"
    const val BENEFIT_USAGE = "prime_benefit_usage"
}