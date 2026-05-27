package com.example.myapp.data.model

import android.net.Uri
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import com.example.myapp.data.dataclass.CarouselItem
import com.example.myapp.data.dataclass.CategoryItem
import com.example.myapp.data.dataclass.ColorItem
import com.example.myapp.data.dataclass.Order
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.ProductTag
import com.example.myapp.data.dataclass.Review
import com.example.myapp.data.dataclass.SearchResult
import com.example.myapp.data.dataclass.SearchType
import com.example.myapp.data.dataclass.ShipmentItem
import com.example.myapp.data.dataclass.SizeItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.CarouselRepository
import com.example.myapp.data.repository.CategoryRepository
import com.example.myapp.data.repository.ColorRepository
import com.example.myapp.data.repository.OrderRepository
import com.example.myapp.data.repository.ProductCrudRepository
import com.example.myapp.data.repository.ShipmentRepository
import com.example.myapp.data.repository.SizeRepository
import com.example.myapp.data.repository.TagRepository
import com.example.myapp.view.utils.CloudinaryHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * ProductCrudState - UI state for admin product management.
 *
 * Holds loading flags, product lists, currently selected product, tag-based
 * groupings, and search-related metadata for the admin product dashboard.
 *
 * @property isLoading Indicates whether product-related data is being loaded
 * @property products All loaded products for the current view
 * @property currentProduct Currently selected product being viewed or edited
 * @property primeEligibleProducts Products tagged as prime-eligible
 * @property flashDealProducts Products participating in flash deals
 * @property featuredProducts Products highlighted as featured
 * @property trendingProducts Products highlighted as trending
 * @property bestSeller Products tagged as best sellers
 * @property error Optional error message for failed operations
 * @property isSuccess Flag for one-off success events (e.g., create/update)
 * @property currentPage Current pagination index for product list
 * @property hasMoreProducts True if the next page is expected to have items
 * @property carousel Marketing carousel items shown in the admin views
 * @property categories Available product categories
 * @property shipmentItem Available shipment options
 * @property colorItem Available color options
 * @property orderItem Orders related to products (for overview)
 * @property sizeItem Available size options
 * @property tags All available product tags
 * @property isLoadingFeatured Loading state for featured products section
 * @property isLoadingTrending Loading state for trending products section
 * @property isLoadingCategories Loading state for categories section
 * @property productDetails Detailed data for a specific product
 * @property isLoadingDetails Loading state for `productDetails`
 * @property isLoadingProductDetail Legacy/alternate loading flag for details
 * @property selectedCategory Currently selected category filter (if any)
 * @property isLoadingCart Loading state for cart-related operations
 * @property isLoadingColors Loading state for color options
 * @property isLoadingSizes Loading state for size options
 * @property hasMoreCartItems True if there are more cart items to load
 * @property isLoadingShipment Loading state for shipment options
 * @property searchResults Current search results list (if searching)
 * @property currentSearchType Strategy/type used for the last search
 */
data class ProductCrudState(
    val isLoading: Boolean = true,
    val products: List<ProductItem> = emptyList(),
    val currentProduct: ProductItem? = null,
    val primeEligibleProducts: List<ProductItem> = emptyList(),
    val flashDealProducts: List<ProductItem> = emptyList(),
    val featuredProducts: List<ProductItem> = emptyList(),
    val trendingProducts: List<ProductItem> = emptyList(),
    val bestSeller: List<ProductItem> = emptyList(),
    val error: String? = null,
    val isSuccess: Boolean = false,
    val currentPage: Int = 0,
    val hasMoreProducts: Boolean = true,
    val carousel: List<CarouselItem> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val shipmentItem: List<ShipmentItem> = emptyList(),
    val colorItem: List<ColorItem> = emptyList(),
    val orderItem: List<Order> = emptyList(),
    val sizeItem: List<SizeItem> = emptyList(),
    val tags: List<ProductTag> = emptyList(),
    val isLoadingFeatured: Boolean = false,
    val isLoadingTrending: Boolean = false,
    val isLoadingCategories: Boolean = false,
    val productDetails: ProductItem? = null,
    val isLoadingDetails: Boolean = false,
    val isLoadingProductDetail: Boolean = false,
    val selectedCategory: String? = null,
    val isLoadingCart: Boolean = false,
    val isLoadingColors: Boolean = false,
    val isLoadingSizes: Boolean = false,
    val hasMoreCartItems: Boolean = false,
    val isLoadingShipment: Boolean = false,
    val searchResults: List<ProductItem>? = null,
    val currentSearchType: SearchType? = null,
)

@HiltViewModel
/**
 * ProductCrudViewModel - ViewModel for admin product management.
 *
 * Orchestrates product CRUD operations, search, tagging, and related metadata
 * loading for the admin dashboard. Also coordinates image upload via
 * Cloudinary and exposes snackbar events for user feedback.
 */
class ProductCrudViewModel @Inject constructor(
    private val productRepository: ProductCrudRepository,
    private val ordersRepository: OrderRepository,
    private val categoryRepository: CategoryRepository,
    private val shipmentRepository: ShipmentRepository,
    private val sizeRepository: SizeRepository,
    private val colorRepository: ColorRepository,
    private val imageLoader: ImageLoader,
    private val savedStateHandle: SavedStateHandle,
    private val tagRepository: TagRepository,
    private val carouselRepository: CarouselRepository,
) : ViewModel() {
    /**
     * getImageLoader
     *
     */
    fun getImageLoader(): ImageLoader = imageLoader

    // 1. Guard flag to prevent double initialization
    private var isInitialized = false

    companion object {
        private const val TAG = "ProductCrudViewModel"
        private const val PAGE_SIZE = 20
        private const val KEY_SEARCH_QUERY = "search_query"
        private const val KEY_PENDING_IMAGE_SEARCH = "pending_image_search"
    }


    /**
     * initializeShopContent - The consolidated entry point for ShopScreen.
     * Prevents the "Double Refresh" by checking the [isInitialized] flag.
     */
    fun initializeShopContent(isLoggedIn: Boolean) {
        if (isInitialized) {
            Log.d(TAG, "Shop content already initialized. Skipping.")
            return
        }
        refreshProducts(isLoggedIn)
        isInitialized = true
    }

    private val _productState = MutableStateFlow(ProductCrudState())
    val productState: StateFlow<ProductCrudState> = _productState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _searchResult = MutableStateFlow<SearchResult?>(null)
    val searchResult: StateFlow<SearchResult?> = _searchResult.asStateFlow()

    private val _currentUserReview = MutableStateFlow<Review?>(null)
    val currentUserReview: StateFlow<Review?> = _currentUserReview.asStateFlow()
    private var hasMoreProducts = true

    // Debouncing for search
    private var searchJob: Job? = null

    private val _taggedProducts = MutableStateFlow<Map<String, List<ProductItem>>>(emptyMap())
    val taggedProducts: StateFlow<Map<String, List<ProductItem>>> = _taggedProducts.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    // Pagination state
    private var currentPage = 0

    init {
        loadInitialData()
    }

    /**
     * saveSearchQuery
     *
     *
     * @param query The query parameter
     */
    fun saveSearchQuery(query: String) {
        savedStateHandle["search_query"] = query
    }

    /**
     * setLoading
     *
     *
     * @param value The value parameter
     */
    fun setLoading(value: Boolean) {
        _isLoading.value = value
    }

    /**
     * getLastSearchQuery
     *
     */
    fun getLastSearchQuery(): String? {
        return savedStateHandle.get<String>("search_query")
    }

    private fun loadInitialData() {
        Log.d(TAG, "📥 loadInitialData() called")

        viewModelScope.launch {
            _productState.value = _productState.value.copy(isLoading = true, error = null)

            try {
                // Launch all data loads in parallel using async
                val productsDeferred = async {
                    productRepository.getProducts(page = 0, pageSize = PAGE_SIZE)
                }
                val categoriesDeferred = async { categoryRepository.getCategories() }
                val shipmentDeferred = async { shipmentRepository.getShipments() }
                val sizeDeferred = async { sizeRepository.getSizes() }
                val colorDeferred = async { colorRepository.getColors() }
                val orderDeferred = async { ordersRepository.getOrders() }
                val tagDeferred = async { tagRepository.getAllTags() }
                val carouselsDeferred = async { carouselRepository.getCarousels() }
                val featuredDeferred = async {
                    productRepository.getProductsByTag("featured", limit = 20)
                }
                val trendingDeferred = async {
                    productRepository.getProductsByTag("trending", limit = 20)
                }

                // Await all results
                val productsResult = productsDeferred.await()
                val categoriesResult = categoriesDeferred.await()
                val shipmentResult = shipmentDeferred.await()
                val sizeResult = sizeDeferred.await()
                val colorResult = colorDeferred.await()
                val orderResult = orderDeferred.await()
                val tagResult = tagDeferred.await()
                val carouselResult = carouselsDeferred.await()
                val featuredResult = featuredDeferred.await()
                val trendingResult = trendingDeferred.await()

                // Update UI state with all results
                _productState.value = _productState.value.copy(
                    isLoading = false,
                    products = productsResult.getOrNull() ?: emptyList(),
                    categories = categoriesResult.getOrNull() ?: emptyList(),
                    shipmentItem = shipmentResult.getOrNull() ?: emptyList(),
                    sizeItem = sizeResult.getOrNull() ?: emptyList(),
                    colorItem = colorResult.getOrNull() ?: emptyList(),
                    carousel = carouselResult.getOrNull() ?: emptyList(),
                    orderItem = orderResult.getOrNull() ?: emptyList(),
                    tags = tagResult.getOrNull() ?: emptyList(),
                    featuredProducts = featuredResult.getOrNull() ?: emptyList(),
                    trendingProducts = trendingResult.getOrNull() ?: emptyList(),
                    error = null
                )

                currentPage = 0
                hasMoreProducts = (productsResult.getOrNull()?.size ?: 0) >= PAGE_SIZE

            } catch (e: Exception) {
                _productState.value = _productState.value.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    /**
     * loadAllTaggedProducts
     *
     */
//    fun loadAllTaggedProducts() {
//        viewModelScope.launch {
//            _productState.update { it.copy(isLoading = true) }
//
//            try {
//                val featuredDeferred = async {
//                    productRepository.getProductsByTag("featured", limit = 20)
//                }
//
//                val trendingDeferred = async {
//                    productRepository.getProductsByTag("trending", limit = 20)
//                }
//
//                val flashDealsDeferred = async {
//                    productRepository.getProductsByTag("flash_deal", limit = 20)
//                }
//
//                val primeEligibleDeferred = async {
//                    productRepository.getProductsByTag("prime_eligible", limit = 20)
//                }
//
//                val isBestSellerDeferred = async {
//                    productRepository.getProductsByTag("best_seller", limit = 20)
//                }
//
//                val featured = featuredDeferred.await().getOrNull() ?: emptyList()
//                val trending = trendingDeferred.await().getOrNull() ?: emptyList()
//                val flashDeals = flashDealsDeferred.await().getOrNull() ?: emptyList()
//                val primeEligible = primeEligibleDeferred.await().getOrNull() ?: emptyList()
//                val bestSeller  = isBestSellerDeferred.await().getOrNull() ?: emptyList()
//
//                // Update tagged products map
//                _taggedProducts.value = mapOf(
//                    "featured" to featured,
//                    "trending" to trending,
//                    "flash_deal" to flashDeals,
//                    "prime_eligible" to primeEligible,
//                    "best_seller" to bestSeller
//                )
//
//                // Update state
//                _productState.update {
//                    it.copy(
//                        isLoading = false,
//                        featuredProducts = featured,
//                        trendingProducts = trending,
//                        bestSeller = bestSeller,
//                        primeEligibleProducts = primeEligible,
//                        flashDealProducts = flashDeals
//                    )
//                }
//
//                Log.d(
//                    TAG,
//                    "✅ Loaded tagged products - Featured: ${featured.size}, Trending: ${trending.size}"
//                )
//            } catch (e: Exception) {
//                _productState.update {
//                    it.copy(isLoading = false, error = e.message)
//                }
//                Log.e(TAG, "Failed to load tagged products", e)
//            }
//        }
//    }

    /**
     * Legacy support/Manual trigger for tagged products
     */
    fun loadAllTaggedProducts() {
        // This is now redundant during initial load but useful for manual refreshes
        viewModelScope.launch {
            val tags = listOf("flash_deal", "prime_eligible")
            val results = tags.associateWith { tag ->
                productRepository.getProductsByTag(tag, 15).getOrNull() ?: emptyList()
            }
            _taggedProducts.value = results
        }
    }
    /**
     * setPendingImageSearch
     *
     *
     * @param pending The pending parameter
     */
    fun setPendingImageSearch(pending: Boolean) {
        savedStateHandle["pending_image"] = pending
    }

    /**
     * hasPendingImageSearch
     *
     */
    fun hasPendingImageSearch(): Boolean {
        return savedStateHandle.get<Boolean>("pending_image") ?: false
    }

    /**
     * Load products with pagination
     *
     * Fetches a specific page of products.
     *
     */

    fun loadProducts() {
        viewModelScope.launch {
            _productState.value = _productState.value.copy(isLoading = true, error = null)

            try {
                productRepository.getProducts(
                    page = 0,
                    pageSize = PAGE_SIZE
                ).fold(
                    onSuccess = { products ->
                        _productState.value = _productState.value.copy(
                            isLoading = false,
                            products = products,
                            error = null  
                        )
                        currentPage = 0
                        hasMoreProducts = products.size >= PAGE_SIZE
                    },
                    onFailure = { exception ->
                        _productState.value = _productState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Unknown error occurred",
                            products = emptyList()  
                        )
                    }
                )
            } catch (e: CancellationException) {
                // Do not catch - rethrow it
                throw e
            } catch (e: Exception) {
                _productState.value = _productState.value.copy(
                    isLoading = false,
                    error = "Unexpected error: ${e.message}",
                    products = emptyList()
                )
            }
        }
    }

    /**
     * Create a new product
     *
     * Adds a new product configuration to the catalog.
     * Validates input before submission.
     *
     * @param product ProductItem containing initial details
     */
    fun createProduct(product: ProductItem) {
        if (product.productName.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Product name cannot be empty", "Error"))
            }
            return
        }

        if (product.price <= 0) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Price must be greater than 0", "Error"))
            }
            return
        }

        viewModelScope.launch {
            _productState.value = _productState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            productRepository.createProduct(product).fold(
                onSuccess = { createdProduct ->
                    Log.d(TAG, "Product created: ${createdProduct.productName}")
                    _productState.value = _productState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Product '${createdProduct.productName}' created successfully"))
                    loadProducts() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to create product: ${exception.message}")
                    _productState.value = _productState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to create product",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    // Get product by ID
    /**
     * getProductById
     *
     *
     * @param productId The productId parameter
     */
    fun getProductById(productId: String) {
        viewModelScope.launch {
            _productState.value = _productState.value.copy(
                isLoading = true,
                error = null
            )

            productRepository.getProductById(productId).fold(
                onSuccess = { product ->
                    Log.d(TAG, "Product fetched: ${product?.productName}")
                    _productState.value = _productState.value.copy(
                        isLoading = false,
                        currentProduct = product,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to fetch product: ${exception.message}")
                    _productState.value = _productState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to fetch product",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Update an existing product
     *
     * Updates details for a product (pricing, description, stock, etc.).
     *
     * @param product ProductItem with updated fields
     */
    fun updateProduct(product: ProductItem) {
        if (product.productName.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Product name cannot be empty", "Error"))
            }
            return
        }

        if (product.price <= 0) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Price must be greater than 0", "Error"))
            }
            return
        }

        viewModelScope.launch {
            _productState.value = _productState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            productRepository.updateProduct(product).fold(
                onSuccess = { updatedProduct ->
                    Log.d(TAG, "Product updated: ${updatedProduct.productName}")
                    _productState.value = _productState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Product updated successfully"))
                    loadProducts() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to update product: ${exception.message}")
                    _productState.value = _productState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to update product",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * clearCurrentProduct
     *
     */
    fun clearCurrentProduct() {
        _productState.value = _productState.value.copy(currentProduct = null)
    }

    /**
     * updateProductQuantity
     *
     *
     * @param quantity The quantity parameter
     */
    fun updateProductQuantity(quantity: Int) {
        viewModelScope.launch {
            // You might want to validate quantity here
            if (quantity > 0) {
                // Update the UI state with new quantity
                _productState.update { currentState ->
                    currentState.copy(
                        currentProduct = currentState.currentProduct?.copy(
                            quantity = quantity
                        )
                    )
                }
            }
        }
    }

    /**
     * Delete a product
     *
     * Removes the product from the catalog.
     *
     * @param productId ID of the product to remove
     * @param productName Name of the product (for confirmation message)
     */
    fun deleteProduct(productId: String, productName: String) {
        viewModelScope.launch {
            _productState.value = _productState.value.copy(
                isLoading = true,
                error = null
            )

            productRepository.deleteProduct(productId).fold(
                onSuccess = {
                    Log.d(TAG, "Product deleted: $productName")
                    _productState.value = _productState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Product '$productName' deleted successfully"))
                    loadProducts() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to delete product: ${exception.message}")
                    _productState.value = _productState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to delete product",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Search products
     *
     * Filters list based on query string.
     *
     * @param query Search term
     */

    fun searchProductsWithFallback(query: String) {
        try {
            searchJob?.cancel()

            if (query.isBlank()) {
                clearSearch()
                return
            }

            searchJob = viewModelScope.launch {
                delay(300) // Debounce

                _productState.update { it.copy(isLoading = true, error = null) }

                productRepository.searchProductsWithFallback(query).fold(
                    onSuccess = { result ->
                        _searchResult.value = result

                        // Ensure both states are updated consistently
                        val displayResults = result.exactMatches.ifEmpty { result.similarProducts }
                        _productState.update {
                            it.copy(
                                searchResults = displayResults,
                                isLoading = false
                            )
                        }
                    },
                    onFailure = { exception ->
                        _searchResult.value = null
                        _productState.update {
                            it.copy(
                                error = exception.message,
                                isLoading = false,
                                searchResults = null  
                            )
                        }
                    }
                )
            }
        } catch (e: CancellationException) {
            // Don't catch cancellation - rethrow it
            throw e
        } catch (e: Exception) {
            _productState.value = _productState.value.copy(
                isLoading = false,
                error = "Unexpected error: ${e.message}",
                products = emptyList()
            )
        }
    }

    /**
     * clearSearch
     *
     */
    fun clearSearch() {
        searchJob?.cancel()
        _searchResult.value = null
        _productState.update { it.copy(searchResults = null) }  
        savedStateHandle.remove<String>(KEY_SEARCH_QUERY)
        savedStateHandle.remove<Boolean>(KEY_PENDING_IMAGE_SEARCH)

    }

    /**
     * loadProductsByCategory
     *
     *
     * @param categoryId The categoryId parameter
     * @param page The page parameter
     */
    fun loadProductsByCategory(categoryId: String = "", page: Int = 0) {
        viewModelScope.launch {
            _productState.value = _productState.value.copy(
                isLoading = true,
                selectedCategory = categoryId
            )

            productRepository.getProductsByCategory(
                categoryId = categoryId,
                page = page,
                pageSize = PAGE_SIZE
            ).fold(
                onSuccess = { products ->
                    _productState.value = _productState.value.copy(
                        isLoading = false,
                        products = products
                    )
                },
                onFailure = { exception ->
                    _productState.value = _productState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    /**
     * loadSimilarProducts
     *
     *
     * @param query The query parameter
     * @param excludeId The excludeId parameter
     * @param limit The limit parameter
     */
    fun loadSimilarProducts(query: String, excludeId: String = "", limit: Int = 10) {
        viewModelScope.launch {
            _productState.update { it.copy(isLoading = true) }

            productRepository.searchProductsWithFallback(query).fold(
                onSuccess = { result ->
                    val allResults = result.exactMatches + result.similarProducts

                    // Split comma-separated IDs
                    val excludeIds =
                        excludeId.split(",").map { it.trim() }.filter { it.isNotBlank() }

                    val filtered = allResults
                        .distinctBy { it.id } // Remove duplicates
                        .filter { it.id !in excludeIds } // Exclude current products
                        .take(limit)

                    _productState.update {
                        it.copy(
                            searchResults = filtered,
                            isLoading = false
                        )
                    }

                },
                onFailure = { exception ->
                    _productState.update {
                        it.copy(
                            error = exception.message,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    /**
     * Toggle featured status
     *
     * Promotes or demotes product to/from featured list.
     *
     */
    /**
     * Toggle trending status
     *
     * Marks product as trending or not.
     *
     * @param productId ID of the product
     */

    fun loadProductReviews(productId: String, limit: Int = 20) {
        viewModelScope.launch {

            productRepository.getProductReviews(productId, limit).fold(
                onSuccess = { reviews ->
                    _reviews.value = reviews
                },
                onFailure = { exception ->
                    _reviews.value = emptyList()

                    _snackBarData.emit(
                        SnackBarData(
                            message = "Failed to load reviews",
                            actionLabel = "Retry",
                            isError = false
                        )
                    )
                }
            )
        }
    }

    private suspend fun refreshProductDetails(productId: String) {
        productRepository.getProductById(productId).fold(
            onSuccess = { product ->
                if (product != null) {
                    // Update the product in the current lists
                    _productState.update { state ->
                        state.copy(
                            isLoading = false,
                            products = state.products.map {
                                if (it.id == productId) product else it
                            },
                            featuredProducts = state.featuredProducts.map {
                                if (it.id == productId) product else it
                            },
                            trendingProducts = state.trendingProducts.map {
                                if (it.id == productId) product else it
                            }
                        )
                    }
                }
            },
            onFailure = {
                _productState.update { it.copy(isLoading = false) }
            }
        )
    }

    /**
     * submitProductRating
     *
     *
     * @param productId The productId parameter
     * @param rating The rating parameter
     * @param review The review parameter
     */
    fun submitProductRating(productId: String, rating: Float, review: String?) {
        viewModelScope.launch {
            _productState.update { it.copy(isLoading = true) }

            productRepository.submitProductRating(productId, rating, review).fold(
                onSuccess = {
                    Log.d(TAG, "Rating submitted successfully")

                    // Show success message
                    _snackBarData.emit(
                        SnackBarData(
                            message = "Thank you for your rating!",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Short,
                            isError = false
                        )
                    )

                    // Refresh the product to get updated rating
                    refreshProductDetails(productId)
                },
                onFailure = { exception ->
                    Log.e(
                        TAG,
                        "Failed to submit rating: ${exception.message}"
                    )

                    _snackBarData.emit(
                        SnackBarData(
                            message = "Failed to submit rating: ${exception.message}",
                            actionLabel = "Retry",
                            duration = SnackbarDuration.Short,
                            isError = true
                        )
                    )

                    _productState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                }
            )
        }
    }

    /**
     * markReviewHelpful
     *
     *
     * @param reviewId The reviewId parameter
     */
    fun markReviewHelpful(reviewId: String) {
        viewModelScope.launch {
            productRepository.markReviewHelpful(reviewId).fold(
                onSuccess = {
                    _snackBarData.emit(
                        SnackBarData(
                            message = "Marked as helpful!",
                            actionLabel = "OK",
                            isError = false
                        )
                    )
                },
                onFailure = { exception ->
                    _snackBarData.emit(
                        SnackBarData(
                            message = "Failed to mark as helpful",
                            actionLabel = "Retry",
                            isError = true
                        )
                    )
                }
            )
        }
    }

    /**
     * loadCurrentUserReview
     *
     *
     * @param productId The productId parameter
     */
    fun loadCurrentUserReview(productId: String) {
        viewModelScope.launch {
            productRepository.getCurrentUserReview(productId).fold(
                onSuccess = {
                    _snackBarData.emit(
                        SnackBarData(
                            message = "Loaded current review",
                            actionLabel = "OK",
                            isError = false
                        )
                    )
                    _currentUserReview.value = it
                },
                onFailure = { exception ->
                    Log.e(
                        TAG,
                        "❌ Error loading user review: ${exception.message}"
                    )

                    _snackBarData.emit(
                        SnackBarData(
                            message = "Failed to load current review",
                            actionLabel = "Retry",
                            isError = true
                        )
                    )
                    _currentUserReview.value = null
                }
             )
            }
        }

    /**
     * searchReviews
     *
     *
     * @param query The query parameter
     */
    fun searchReviews(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                // If query is empty, reload all reviews for current product
                _productState.value.currentProduct?.id?.let { productId ->
                    loadProductReviews(productId)
                }
                return@launch
            }

            _productState.update { it.copy(isLoading = true) }

            productRepository.searchProductReviews(query).fold(
                onSuccess = { searchResults ->
                    _reviews.value = searchResults
                    _productState.update { it.copy(isLoading = false) }
                },
                onFailure = { exception ->
                    _productState.update { it.copy(isLoading = false) }

                    _snackBarData.emit(
                        SnackBarData(
                            message = "Failed to search reviews",
                            actionLabel = "OK",
                            isError = true
                        )
                    )
                }
            )
        }
    }

    // Reset success state
    /**
     * resetSuccessState
     *
     */
    fun resetSuccessState() {
        _productState.value = _productState.value.copy(isSuccess = false)
    }

    /**
     * Upload main product image
     */
    fun uploadMainImage(
        imageUri: Uri,
        cloudinaryHelper: CloudinaryHelper,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _productState.value = _productState.value.copy(isLoading = true)

            cloudinaryHelper.uploadImage(
                fileUri = imageUri,
                folder = "products/main"
            ).fold(
                onSuccess = { publicId ->
                    Log.d("ProductCrudVM", "Main image uploaded: $publicId")
                    _productState.value = _productState.value.copy(isLoading = false)
                    onSuccess(publicId)

                    _snackBarData.emit(
                        SnackBarData(
                            message = "Image uploaded successfully",
                            duration = SnackbarDuration.Short
                        )
                    )
                },
                onFailure = { error ->
                    Log.e("ProductCrudVM", "Upload failed: ${error.message}")
                    _productState.value = _productState.value.copy(
                        isLoading = false,
                        error = error.message
                    )

                    _snackBarData.emit(
                        SnackBarData(
                            message = "Failed to upload image: ${error.message}",
                            isError = true,
                            duration = SnackbarDuration.Long
                        )
                    )
                }
            )
        }
    }

    /**
     * Upload supporting images
     */
    fun uploadSupportingImages(
        imageUris: List<Uri>,
        cloudinaryHelper: CloudinaryHelper,
        onSuccess: (List<String>) -> Unit
    ) {
        viewModelScope.launch {
            val publicIds = mutableListOf<String>()

            imageUris.forEachIndexed { index, uri ->
                cloudinaryHelper.uploadImage(
                    fileUri = uri,
                    folder = "products/supporting"
                ).fold(
                    onSuccess = { publicId ->
                        publicIds.add(publicId)
                        Log.d(
                            "ProductCrudVM",
                            "Supporting image ${index + 1}/${imageUris.size} uploaded: $publicId"
                        )

                        // Show progress
                        if (index == imageUris.size - 1) {
                            // All done
                            _snackBarData.emit(
                                SnackBarData(
                                    message = "${publicIds.size} images uploaded successfully",
                                    duration = SnackbarDuration.Short
                                )
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e(
                            "ProductCrudVM",
                            "Failed to upload image ${index + 1}: ${error.message}"
                        )
                    }
                )
            }

            // Return all successfully uploaded IDs
            onSuccess(publicIds)
        }
    }

    /**
     * refreshProducts
     *
     */
//    fun refreshProducts() {
//
//        if (_isLoading.value) {
//            Log.d(TAG, "   ⚠️ Already refreshing, skipping...")
//            return
//        }
//
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                // Clear repository cache
//                Log.d(TAG, "   🗑️ Clearing cache...")
//                productRepository.clearCache()
//
//                // Reset pagination
//                currentPage = 0
//                hasMoreProducts = true
//
//                // Reload all data
//                Log.d(TAG, "   📥 Loading initial data...")
//                loadInitialData()
//                Log.d(TAG, "   ✅ Refresh complete")
//            } catch (e: CancellationException) {
//                // Rethrow cancellation
//                throw e
//            } catch (e: Exception) {
//                Log.e(TAG, "   ❌ Error refreshing products", e)
//                _productState.update {
//                    it.copy(
//                        isLoading = false,
//                        error = e.message ?: "Failed to refresh products"
//                    )
//                }
//            } finally {
//                Log.d(TAG, "   🏁 Setting _isRefreshing to false")
//                _isLoading.value = false
//            }
//        }
//    }


    /**
     * refreshProducts - Orchestrates a single, unified data fetch for the entire screen.
     */
    fun refreshProducts(isLoggedIn: Boolean = false) {
        viewModelScope.launch {
            _productState.update { it.copy(isLoading = true, error = null) }

            try {
                // Use async to fetch everything in parallel for maximum speed
                val productsDeferred = async { productRepository.getProducts(0, PAGE_SIZE) }
                val categoriesDeferred = async { categoryRepository.getCategories() }
                val tagDeferred = async { tagRepository.getAllTags() }
                val carouselsDeferred = async { carouselRepository.getCarousels() }

                // Fetch specific tagged sections
                val featuredDeferred = async { productRepository.getProductsByTag("featured", 15) }
                val trendingDeferred = async { productRepository.getProductsByTag("trending", 15) }
                val flashDealsDeferred = async { productRepository.getProductsByTag("flash_deal", 15) }

                // Conditional fetch for Prime
                val primeDeferred = if (isLoggedIn) {
                    async { productRepository.getProductsByTag("prime_eligible", 15) }
                } else null

                // Await all values
                val products = productsDeferred.await().getOrNull() ?: emptyList()
                val categories = categoriesDeferred.await().getOrNull() ?: emptyList()
                val tags = tagDeferred.await().getOrNull() ?: emptyList()
                val carousels = carouselsDeferred.await().getOrNull() ?: emptyList()
                val featured = featuredDeferred.await().getOrNull() ?: emptyList()
                val trending = trendingDeferred.await().getOrNull() ?: emptyList()
                val flashDeals = flashDealsDeferred.await().getOrNull() ?: emptyList()
                val primeEligible = primeDeferred?.await()?.getOrNull() ?: emptyList()

                // 2. Update the specific Tagged Products map (flash deals, etc.)
                val tagsMap = mutableMapOf<String, List<ProductItem>>()
                tagsMap["flash_deal"] = flashDeals
                if (isLoggedIn) tagsMap["prime_eligible"] = primeEligible

                _taggedProducts.value = tagsMap

                // 3. Update the primary UI state
                _productState.update {
                    it.copy(
                        isLoading = false,
                        products = products,
                        categories = categories,
                        tags = tags,
                        carousel = carousels,
                        featuredProducts = featured,
                        trendingProducts = trending,
                        flashDealProducts = flashDeals,
                        primeEligibleProducts = primeEligible,
                        error = null
                    )
                }

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error refreshing products: ${e.message}")
                _productState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    /**
     * clearReviewsState
     *
     */
    fun clearReviewsState() {
        _reviews.value = emptyList()
        _currentUserReview.value = null
    }

    /**
     * clearCurrentUserReview
     *
     */
    fun clearCurrentUserReview() {
        _currentUserReview.value = null
    }
}