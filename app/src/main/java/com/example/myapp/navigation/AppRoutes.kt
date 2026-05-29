package com.example.myapp.navigation

/**
 * Central route registry for the app's Compose navigation graph.
 *
 * Keep route names here instead of duplicating string literals across screens. Dynamic routes
 * are represented by their base route, while destinations that own a path argument include the
 * placeholder in the constant value, such as [PROMOTION_DETAILS].
 */
object AppRoutes {
    /**
     * Public destinations available without an authenticated session.
     */
    const val ON_BOARDING = "onboarding"

    /**
     * Root route for the main application flow.
     *
     * The string value keeps the existing persisted route spelling for compatibility.
     */
    const val MAIN_FLOW = "mian_flow"

    const val SHOP = "shop"
    const val ALL_PRODUCTS = "all_products"
    const val SEARCH = "search"

    /**
     * Base product-detail route. Navigate with `"$PRODUCT_DETAIL/{productId}"`.
     */
    const val PRODUCT_DETAIL = "product_detail"

    const val MANAGE_DELIVERY_LOCATION = "manage_delivery_location"

    /**
     * Product filter route base. The graph appends `searchQuery` and `category` query arguments.
     */
    const val PRODUCT_FILTER = "product_filter"

    const val PROMOTIONS = "promotions"

    const val LIGHTNING_DEALS = "lightning_deals"
    const val DAILY_ESSENTIALS = "daily_essentials"

    /**
     * Guest-only authentication destinations.
     */
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val EMAIL = "email_login"
    const val FORGET_PASSWORD = "forget_password"
    const val PASSWORD_RESET_CODE = "password_reset_code"

    /**
     * Destinations that require or commonly depend on an authenticated session.
     */
    const val CART = "cart"
    const val FAVORITE = "favorite"
    const val PROFILE = "profile"

    /**
     * Checkout payment route base. Navigate with `"$PAYMENT/{amountInCents}"`.
     */
    const val PAYMENT = "payment"

    /**
     * User order list/details route base. Navigate with `"$ORDER_DETAILS/{userId}"`.
     */
    const val ORDER_DETAILS = "order_details"

    /**
     * Admin and management destinations guarded by admin role checks in the graph.
     */
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val PRODUCT_DASHBOARD = "product_dashboard"
    const val ADD_PRODUCT = "add_product"

    /**
     * Confirmation route base. Navigate with `"$ORDER_CONFIRMATION_SCREEN/{orderId}"`.
     */
    const val ORDER_CONFIRMATION_SCREEN = "order_confirmation_screen"

    /**
     * Product edit route base. Navigate with `"$EDIT_PRODUCT/{productId}"`.
     */
    const val EDIT_PRODUCT = "edit_product"

    const val CATEGORY_DASHBOARD = "category_dashboard"
    const val ORDER_DASHBOARD = "order_dashboard"
    const val SIZE_DASHBOARD = "size_dashboard"
    const val BRANDS_DASHBOARD = "brands_dashboard"
    const val COLORS_DASHBOARD = "colors_dashboard"

    const val PROMOTIONS_DASHBOARD = "promotions_dashboard"

    const val SHIPMENT_DASHBOARD = "shipment_dashboard"
    const val MANAGE_CAROUSEL = "manage_carousel"

    /**
     * Promotion details destination with a required `promotionId` path argument.
     */
    const val PROMOTION_DETAILS = "promotion_details/{promotionId}"

    const val PRIME_MANAGEMENT = "prime_management"
    const val JOIN_PRIME = "join_prime"
    const val PRIME_DETAILS = "PRIME_DETAILS"

    const val MANAGE_TAGS = "manage_tags"

    /**
     * Product reviews route base. Navigate with `"$REVIEWS/{productId}"`.
     */
    const val REVIEWS = "reviews"

    const val MANAGE_USERS = "manage_users"
}
