package com.example.myapp.data.dataclass

import androidx.compose.ui.graphics.Color

// 2. Data Models (keep these from before)
/**
 * DashboardStats - Aggregated statistics for the admin dashboard
 *
 * @property totalProducts Total number of products in inventory
 * @property totalOrders Total number of orders placed
 * @property totalUsers Total number of registered users
 * @property totalRevenue Total revenue formatted as double
 * @property totalBrands Count of unique brands
 * @property totalCategories Count of unique categories
 * @property totalColors Count of defined colors
 * @property totalSizes Count of defined sizes
 */
data class DashboardStats(
    val totalProducts: Int = 0,
    val totalOrders: Int = 0,
    val totalUsers: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalBrands: Int = 0,
    val totalCategories: Int = 0,
    val totalColors: Int = 0,
    val totalSizes: Int = 0
)

/**
 * ProductStatusData - Product inventory status breakdown
 *
 * @property inStock Count of products in stock
 * @property outOfStock Count of products out of stock
 * @property lowStock Count of products with low stock warning
 * @property featured Count of featured products
 * @property trending Count of trending products
 */
data class ProductStatusData(
    val inStock: Int = 0,
    val outOfStock: Int = 0,
    val lowStock: Int = 0,
    val featured: Int = 0,
    val trending: Int = 0
)

/**
 * CategoryStats - Product distribution by category
 *
 * @property categoryId Unique category identifier
 * @property categoryName Display name of the category
 * @property productCount Number of products in this category
 * @property color Chart color for visual representation
 */
data class CategoryStats(
    val categoryId: String,
    val categoryName: String,
    val productCount: Int,
    val color: Color = Color.Blue
)

/**
 * BrandStats - Product distribution by brand
 *
 * @property brandId Unique brand identifier
 * @property brandName Name of the brand
 * @property productCount Number of products for this brand
 * @property logoUrl URL of the brand logo
 */
data class BrandStats(
    val brandId: String,
    val brandName: String,
    val productCount: Int,
    val logoUrl: String = ""
)

/**
 * ColorStats - Product distribution by color
 *
 * @property colorId Unique color identifier
 * @property colorName Common name of color (e.g., Red)
 * @property hexCode Color hex code (e.g., #FF0000)
 * @property productCount Number of products available in this color
 */
data class ColorStats(
    val colorId: String,
    val colorName: String,
    val hexCode: String,
    val productCount: Int
)

/**
 * SizeStats - Product distribution by size
 *
 * @property sizeId Unique size identifier
 * @property sizeName Display name (e.g., XL, 42)
 * @property productCount Number of products available in this size
 */
data class SizeStats(
    val sizeId: String,
    val sizeName: String,
    val productCount: Int
)

/**
 * OrderStatusData - Order counts by status
 *
 * @property status Order status label (e.g., Pending, Shipped)
 * @property count Number of orders in this status
 * @property color Chart color for this status
 */
data class OrderStatusData(
    val status: String,
    val count: Int,
    val color: Color
)

/**
 * SalesData - Monthly revenue data
 *
 * @property month Month name or label
 * @property sales Total sales amount for the month
 */
data class SalesData(
    val month: String,
    val sales: Double
)

// 3. Dashboard UI State
/**
 * DashboardUiState - Complete UI state for Admin Dashboard
 *
 * Holds all statistical data required to render the admin dashboard charts and counters.
 *
 * @property isLoading Loading state for dashboard data
 * @property dashboardStats General system overview stats
 * @property productStatus Product inventory status
 * @property categoryStats Breakdown by category
 * @property brandStats Breakdown by brand
 * @property colorStats Breakdown by color
 * @property sizeStats Breakdown by size
 * @property orderStatus Breakdown by order status
 * @property monthlySales Revenue data for charts
 * @property error Error message if data fetch fails
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val dashboardStats: DashboardStats = DashboardStats(),
    val productStatus: ProductStatusData = ProductStatusData(),
    val categoryStats: List<CategoryStats> = emptyList(),
    val brandStats: List<BrandStats> = emptyList(),
    val colorStats: List<ColorStats> = emptyList(),
    val sizeStats: List<SizeStats> = emptyList(),
    val orderStatus: List<OrderStatusData> = emptyList(),
    val monthlySales: List<SalesData> = emptyList(),
    val error: String? = null
)