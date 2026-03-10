package com.example.myapp.navigation

/**
 * AppRoutes - Centralized definition of all navigation routes used in the app.
 *
 * This singleton object contains constant string values for every navigation destination
 * in the application. Using constants prevents typos and makes route management easier.
 *
 * Routes are categorized into:
 * - Public Routes (Accessible by everyone)
 * - Auth Routes (Guest only, e.g., Login)
 * - Authenticated Routes (Require login, e.g., Cart)
 * - Admin Routes (Require admin privileges)
 */

// APP ROUTES DEFINITIONS
// ============================================

object AppRoutes {
    // ========== PUBLIC ROUTES ==========
    const val ON_BOARDING = "onboarding"
    const val MAIN_FLOW = "mian_flow"
    const val SHOP = "shop"
    const val ALL_PRODUCTS = "all_products"
    const val SEARCH = "search"
    const val PRODUCT_DETAIL = "product_detail"
    const val MANAGE_DELIVERY_LOCATION = "manage_delivery_location"
    const val PRODUCT_FILTER = "product_filter"
    const val PROMOTIONS = "promotions"

    const val LIGHTNING_DEALS = "lightning_deals"
    const val DAILY_ESSENTIALS = "daily_essentials"

    // ========== AUTH ROUTES (Guest Only) ==========
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val EMAIL = "email_login"
    const val PHONE = "phone_login"
    const val FORGET_PASSWORD = "forget_password"
    const val PASSWORD_RESET_CODE = "password_reset_code"

    // ========== AUTHENTICATED ROUTES ==========
    const val CART = "cart"
    const val FAVORITE = "favorite"
    const val PROFILE = "profile"
    const val PAYMENT = "payment"
    const val ORDER_DETAILS = "order_details"

    // ========== ADMIN ROUTES ==========
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val PRODUCT_DASHBOARD = "product_dashboard"
    const val ADD_PRODUCT = "add_product"
    const val ORDER_CONFIRMATION_SCREEN = "order_confirmation_screen"
    const val EDIT_PRODUCT = "edit_product"
    const val CATEGORY_DASHBOARD = "category_dashboard"
    const val ORDER_DASHBOARD = "order_dashboard"
    const val SIZE_DASHBOARD = "size_dashboard"
    const val BRANDS_DASHBOARD = "brands_dashboard"
    const val COLORS_DASHBOARD = "colors_dashboard"

    const val PROMOTIONS_DASHBOARD = "promotions_dashboard"

    const val SHIPMENT_DASHBOARD = "shipment_dashboard"
    const val MANAGE_CAROUSEL = "manage_carousel"
    const val PROMOTION_DETAILS = "promotion_details/{promotionId}"


    const val PRIME_MANAGEMENT = "prime_management"
    const val JOIN_PRIME = "join_prime"
    const val PRIME_DETAILS = "PRIME_DETAILS"

    const val MANAGE_TAGS = "manage_tags"
    const val REVIEWS = "reviews"

}