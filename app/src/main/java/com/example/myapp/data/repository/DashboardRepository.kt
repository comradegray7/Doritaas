package com.example.myapp.data.repository

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.myapp.data.dataclass.BrandStats
import com.example.myapp.data.dataclass.CategoryStats
import com.example.myapp.data.dataclass.ColorStats
import com.example.myapp.data.dataclass.DashboardStats
import com.example.myapp.data.dataclass.OrderStatusData
import com.example.myapp.data.dataclass.ProductStatusData
import com.example.myapp.data.dataclass.SalesData
import com.example.myapp.data.dataclass.SizeStats
import jakarta.inject.Inject
import java.util.Calendar

/**
 * Repository providing aggregated data for the Admin Dashboard.
 *
 * Orchestrates calls to other, specialized repositories to assemble
 * comprehensive statistics on sales, inventory, and users.
 */
interface DashboardRepository {
    /**
     * Get high-level dashboard statistics (counts and total revenue).
     */
    suspend fun getDashboardStats(): Result<DashboardStats>

    /**
     * Get inventory status breakdown (in stock, low stock, etc.).
     */
    suspend fun getProductStatusData(): Result<ProductStatusData>

    /**
     * Get statistics on products per category.
     */
    suspend fun getCategoryStats(): Result<List<CategoryStats>>

    /**
     * Get statistics on products per brand.
     */
    suspend fun getBrandStats(): Result<List<BrandStats>>

    /**
     * Get statistics on products per color.
     */
    suspend fun getColorStats(): Result<List<ColorStats>>

    /**
     * Get statistics on products per size.
     */
    suspend fun getSizeStats(): Result<List<SizeStats>>

    /**
     * Get order counts grouped by status.
     */
    suspend fun getOrderStatusData(): Result<List<OrderStatusData>>

    /**
     * Get monthly sales totals.
     */
    suspend fun getMonthlySales(): Result<List<SalesData>>
}

/**
 * Implementation of [DashboardRepository].
 *
 * Aggregates data from multiple sources:
 * - ProductRepository
 * - BrandRepository
 * - CategoryRepository
 * - ColorRepository
 * - SizeRepository
 * - OrderRepository
 * - AuthRepository
 */

/**
 * DashboardRepositoryImpl
 *
 * Implementation of [DashboardRepository] that aggregates statistical data from various
 * repositories to provide a comprehensive overview for the admin dashboard.
 */
class DashboardRepositoryImpl @Inject constructor(
    private val productRepository: ProductCrudRepository,
    private val brandRepository: BrandRepository,
    private val categoryRepository: CategoryRepository,
    private val colorRepository: ColorRepository,
    private val sizeRepository: SizeRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: AuthRepository
) : DashboardRepository {

    companion object {
        private const val TAG = "DashboardRepository"
    }

    override suspend fun getDashboardStats(): Result<DashboardStats> {
        return try {
            Log.d(TAG, "Fetching dashboard stats from repositories...")

            val productsResult = productRepository.getProducts()
            val brandsResult = brandRepository.getBrands()
            val categoriesResult = categoryRepository.getCategories()
            val colorsResult = colorRepository.getColors()
            val sizesResult = sizeRepository.getSizes()
            val ordersResult = orderRepository.getOrders()
            val usersResult = userRepository.getAllUsers()

            // Safe unpacking with default empty lists
            val products = productsResult.getOrElse { emptyList() }
            val orders = ordersResult.getOrElse { emptyList() }
            val brands = brandsResult.getOrElse { emptyList() }
            val categories = categoriesResult.getOrElse { emptyList() }
            val colors = colorsResult.getOrElse { emptyList() }
            val sizes = sizesResult.getOrElse { emptyList() }
            val users = usersResult.getOrElse { emptyList() }

            val usersCount = users.size

            // Calculate total revenue from orders
            val totalRevenue = orders.sumOf { it.totalAmount }

            val stats = DashboardStats(
                totalProducts = products.size,
                totalOrders = orders.size,
                totalUsers = usersCount,
                totalRevenue = totalRevenue,
                totalBrands = brands.size,
                totalCategories = categories.size,
                totalColors = colors.size,
                totalSizes = sizes.size
            )

            Log.d(TAG, "Dashboard stats: $stats")
            Result.success(stats)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching dashboard stats: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getProductStatusData(): Result<ProductStatusData> {
        return try {
            val products = productRepository.getProducts().getOrElse { emptyList() }

            var inStock = 0
            var outOfStock = 0
            var lowStock = 0

            products.forEach { product ->
                val quantity = product.quantity

                when {
                    !product.inStock -> outOfStock++
                    quantity == 0 -> outOfStock++
                    quantity < 10 -> lowStock++
                    else -> inStock++
                }
            }

            Result.success(
                ProductStatusData(
                    inStock = inStock,
                    outOfStock = outOfStock,
                    lowStock = lowStock,
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching product status: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getCategoryStats(): Result<List<CategoryStats>> {
        return try {
            val categories = categoryRepository.getCategories().getOrElse { emptyList() }
            val products = productRepository.getProducts().getOrElse { emptyList() }

            // Count products per category
            val categoryCountMap = mutableMapOf<String, Int>()
            products.forEach { product ->
                val categoryId = product.category
                if (categoryId.isNotEmpty()) {
                    categoryCountMap[categoryId] = (categoryCountMap[categoryId] ?: 0) + 1
                }
            }

            val colors = listOf(
                Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC05),
                Color(0xFFEA4335), Color(0xFF9C27B0), Color(0xFF00BCD4),
                Color(0xFFFF5722), Color(0xFF795548)
            )

            val stats = categories.mapIndexed { index, category ->
                CategoryStats(
                    categoryId = category.id,
                    categoryName = category.categoryName,
                    productCount = categoryCountMap[category.id]
                        ?: categoryCountMap[category.categoryName]
                        ?: 0,
                    color = colors[index % colors.size]
                )
            }

            Log.d(TAG, "Category stats: $stats")
            Result.success(stats)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching category stats: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getBrandStats(): Result<List<BrandStats>> {
        return try {
            val brands = brandRepository.getBrands().getOrElse { emptyList() }
            val products = productRepository.getProducts().getOrElse { emptyList() }

            val brandCountMap = mutableMapOf<String, Int>()
            products.forEach { product ->
                val brandId = product.brand
                if (brandId.isNotEmpty()) {
                    brandCountMap[brandId] = (brandCountMap[brandId] ?: 0) + 1
                }
            }

            val stats = brands.map { brand ->
                BrandStats(
                    brandId = brand.id,
                    brandName = brand.brandName,
                    productCount = brandCountMap[brand.id]
                        ?: brandCountMap[brand.brandName]
                        ?: 0,
                )
            }

            Log.d(TAG, "Brand stats: $stats")
            Result.success(stats)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching brand stats: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getColorStats(): Result<List<ColorStats>> {
        return try {
            val colors = colorRepository.getColors().getOrElse { emptyList() }
            val products = productRepository.getProducts().getOrElse { emptyList() }

            val colorCountMap = mutableMapOf<String, Int>()
            products.forEach { product ->
                product.colors.forEach { color ->
                    if (color.isNotEmpty()) {
                        colorCountMap[color] = (colorCountMap[color] ?: 0) + 1
                    }
                }
            }

            val stats = colors.map { color ->
                ColorStats(
                    colorId = color.id,
                    colorName = color.name,
                    hexCode = color.hexCode,
                    productCount = colorCountMap[color.id]
                        ?: colorCountMap[color.name]
                        ?: 0
                )
            }

            Log.d(TAG, "Color stats: $stats")
            Result.success(stats)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching color stats: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getSizeStats(): Result<List<SizeStats>> {
        return try {
            val sizes = sizeRepository.getSizes().getOrElse { emptyList() }
            val products = productRepository.getProducts().getOrElse { emptyList() }

            val sizeCountMap = mutableMapOf<String, Int>()
            products.forEach { product ->
                product.sizes.forEach { size ->
                    if (size.isNotEmpty()) {
                        sizeCountMap[size] = (sizeCountMap[size] ?: 0) + 1
                    }
                }
            }

            val stats = sizes.map { size ->
                SizeStats(
                    sizeId = size.id,
                    sizeName = size.size,
                    productCount = sizeCountMap[size.id]
                        ?: sizeCountMap[size.size]
                        ?: 0
                )
            }

            Log.d(TAG, "Size stats: $stats")
            Result.success(stats)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching size stats: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getOrderStatusData(): Result<List<OrderStatusData>> {
        return try {
            val orders = orderRepository.getOrders().getOrElse { emptyList() }

            val statusCountMap = mutableMapOf<String, Int>()
            orders.forEach { order ->
                val status = order.status.lowercase()
                statusCountMap[status] = (statusCountMap[status] ?: 0) + 1
            }

            val statusColors = mapOf(
                "pending" to Color(0xFFFFA726), // Orange
                "processing" to Color(0xFF42A5F5), // Blue
                "shipped" to Color(0xFF7E57C2), // Purple
                "delivered" to Color(0xFF66BB6A), // Green
                "completed" to Color(0xFF66BB6A), // Green
                "cancelled" to Color(0xFFEF5350) // Red
            )

            val stats = statusCountMap.map { (status, count) ->
                OrderStatusData(
                    status = status.replaceFirstChar { it.uppercase() },
                    count = count,
                    color = statusColors[status] ?: Color.Gray
                )
            }.sortedByDescending { it.count }

            Log.d(TAG, "Order status stats: $stats")
            Result.success(stats)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching order status: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getMonthlySales(): Result<List<SalesData>> {
        return try {
            val orders = orderRepository.getOrders().getOrElse { emptyList() }

            val monthNames = listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )

            val monthlySalesMap = mutableMapOf<String, Double>()
            monthNames.forEach { monthlySalesMap[it] = 0.0 }

            orders.forEach { order ->
                order.createdAt?.let { timestamp ->
                    val calendar = Calendar.getInstance()
                    calendar.time = timestamp.toDate()
                    val monthIndex = calendar.get(Calendar.MONTH)
                    val monthName = monthNames[monthIndex]
                    monthlySalesMap[monthName] =
                        (monthlySalesMap[monthName] ?: 0.0) + order.totalAmount
                }
            }

            val salesData = monthNames.map { month ->
                SalesData(month = month, sales = monthlySalesMap[month] ?: 0.0)
            }

            Log.d(TAG, "Monthly sales: $salesData")
            Result.success(salesData)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching monthly sales: ${e.message}", e)
            Result.failure(e)
        }
    }
}