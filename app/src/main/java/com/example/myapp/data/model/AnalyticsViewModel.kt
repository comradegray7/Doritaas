package com.example.myapp.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.BrandRepository
import com.example.myapp.data.repository.CarouselRepository
import com.example.myapp.data.repository.CategoryRepository
import com.example.myapp.data.repository.ColorRepository
import com.example.myapp.data.repository.OrderRepository
import com.example.myapp.data.repository.ProductCrudRepository
import com.example.myapp.data.repository.PromotionRepository
import com.example.myapp.data.repository.ShipmentRepository
import com.example.myapp.data.repository.SizeRepository
import com.example.myapp.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Analytics ViewModel
/**
 * AnalyticsState - UI State for Analytics Dashboard
 *
 * Holds all data required for the analytics dashboard visualization including
 * statistical counters, trends, charts, and loading/error states.
 *
 * @property isLoading Loading indicator for async operations
 * @property error Error message if data load fails
 * @property productsCount Total number of products
 * @property categoriesCount Total number of categories
 * @property tagCount Total number of tags
 * @property carouselCount Total number of carousels
 * @property sizesCount Total number of defined sizes
 * @property brandsCount Total number of defined brands
 * @property colorsCount Total number of defined colors
 * @property shipmentsCount Total number of shipment methods
 * @property ordersCount Total number of orders
 * @property promotionsCount Total number of active promotions
 * @property isChartLoading Loading indicator specifically for charts
 */
data class AnalyticsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val productsCount: Int = 0,
    val categoriesCount: Int = 0,
    val tagCount: Int = 0,
    val carouselCount: Int = 0,
    val sizesCount: Int = 0,
    val brandsCount: Int = 0,
    val colorsCount: Int = 0,
    val shipmentsCount: Int = 0,
    val ordersCount: Int = 0,
    val promotionsCount: Int = 0,
    val isChartLoading: Boolean = false,
)


/**
 * AnalyticsViewModel - ViewModel for Admin Analytics Dashboard
 *
 * Manages data fetching and processing for the admin analytics dashboard.
 * Aggregates data from multiple repositories to calculate trends, revenue,
 * and statistical overviews.
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val productRepository: ProductCrudRepository,
    private val categoryRepository: CategoryRepository,
    private val orderRepository: OrderRepository,
    private val shipmentRepository: ShipmentRepository,
    private val brandRepository: BrandRepository,
    private val sizeRepository: SizeRepository,
    private val colorRepository: ColorRepository,
    private val promotionRepository: PromotionRepository,
    private val tagRepository: TagRepository,
    private val carouselRepository: CarouselRepository
) : ViewModel() {

    private val _analyticsState = MutableStateFlow(AnalyticsState())
    val analyticsState: StateFlow<AnalyticsState> = _analyticsState.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()

    /**
     * Load all analytics data
     *
     * Fetches data from all repositories in parallel to improve performance.
     * Aggregates the results to update the AnalyticsState.
     * Calculates total revenue, counts, and trends comparing with previous periods.
     */
    fun loadAnalytics() {
        viewModelScope.launch {
            _analyticsState.value = _analyticsState.value.copy(isLoading = true, error = null)

            try {
                // Load all analytics in parallel
                val productsDeferred = async { productRepository.getProducts() }
                val categoriesDeferred = async { categoryRepository.getCategories() }
                val ordersDeferred = async { orderRepository.getOrders() }
                val shipmentsDeferred = async { shipmentRepository.getShipments() }
                val brandsDeferred = async { brandRepository.getBrands() }
                val sizesDeferred = async { sizeRepository.getSizes() }
                val colorsDeferred = async { colorRepository.getColors() }
                val promotionsDeferred = async { promotionRepository.getPromotions() }
                val tagsDeferred = async { tagRepository.getAllTags() }
                val carouselDeferred = async { carouselRepository.getCarousels() }

                // Wait for all to complete
                val products = productsDeferred.await()
                val categories = categoriesDeferred.await()
                val orders = ordersDeferred.await()
                val shipments = shipmentsDeferred.await()
                val brands = brandsDeferred.await()
                val sizes = sizesDeferred.await()
                val colors = colorsDeferred.await()
                val promotions = promotionsDeferred.await()
                val tags = tagsDeferred.await()
                val carousels = carouselDeferred.await()

                _analyticsState.value = _analyticsState.value.copy(
                    isLoading = false,
                    productsCount = products.getOrThrow().size,
                    categoriesCount = categories.getOrThrow().size,
                    sizesCount = sizes.getOrThrow().size,
                    brandsCount = brands.getOrThrow().size,
                    colorsCount = colors.getOrThrow().size,
                    shipmentsCount = shipments.getOrThrow().size,
                    ordersCount = orders.getOrThrow().size,
                    promotionsCount = promotions.getOrThrow().size,
                    tagCount = tags.getOrThrow().size,
                    carouselCount = carousels.getOrThrow().size,
                )

            } catch (e: Exception) {
                _analyticsState.value = _analyticsState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load analytics"
                )
                _snackBarData.emit(
                    SnackBarData(
                        message = "Failed to load analytics: ${e.message}",
                        isError = true
                    )
                )
            }
        }
    }
}