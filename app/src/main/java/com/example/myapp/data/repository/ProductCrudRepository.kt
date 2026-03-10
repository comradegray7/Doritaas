package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.Review
import com.example.myapp.data.dataclass.SearchResult
import com.example.myapp.data.dataclass.SearchType
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import jakarta.inject.Inject

import kotlinx.coroutines.tasks.await
import kotlin.collections.set

/**
 * Interface for Administrative Product Management.
 * 
 * Provides comprehensive CRUD operations and specialized queries (featured, trending)
 * for the admin dashboard.
 */
interface ProductCrudRepository {
    /**
     * Create a new product.
     * @param product Product data
     * @return Result containing created product
     */
    suspend fun createProduct(product: ProductItem): Result<ProductItem>

    /**
     * Retrieve a page of products.
     * @param page Page index
     * @param pageSize Number of items per page
     * @return Result containing list of products
     */
    suspend fun getProducts(page: Int = 0, pageSize: Int = 20): Result<List<ProductItem>>

    /**
     * Get a product by ID.
     * @param productId ID of the product
     * @return Result containing found product
     */
    suspend fun getProductById(productId: String): Result<ProductItem?>

    /**
     * Update an existing product.
     * @param product Product data to update
     * @return Result containing updated product
     */
    suspend fun updateProduct(product: ProductItem): Result<ProductItem>

    /**
     * Delete a product.
     * @param productId ID of the product
     * @return Result<Unit>
     */
    suspend fun deleteProduct(productId: String): Result<Unit>

    /**
     * Search products by name, description, brand, or category.
     * @param query Search string
     * @return Result containing list of matches
     */
    suspend fun searchProducts(query: String): Result<List<ProductItem>>

    /**
     * Get products belonging to a specific category.
     * @param categoryId ID of the category
     * @param page Page index
     * @param pageSize Number of items per page
     * @return Result containing list of products
     */
    suspend fun getProductsByCategory(categoryId: String, page: Int = 0, pageSize: Int = 20): Result<List<ProductItem>>

    /**
     * Clear any local caches.
     */
    fun clearCache()

    /**
     * Submit a user rating/review for a product.
     * @param productId ID of the product
     * @param rating Numeric rating (0-5)
     * @param review Optional text review
     * @return Result<Unit>
     */
    suspend fun submitProductRating(productId: String, rating: Float, review: String?): Result<Unit>

    /**
     * Advanced search with fallback logic.
     * Attempts exact match, then fuzzy match, then category match.
     * @param query Search string
     * @return Result containing [SearchResult] object
     */
    suspend fun searchProductsWithFallback(query: String): Result<SearchResult>

    /**
     * Get reviews for a product.
     * @param productId ID of the product
     * @param limit Maximum number of reviews to return
     * @return Result containing list of reviews
     */
    suspend fun getProductReviews(
        productId: String,
        limit: Int = 20
    ): Result<List<Review>>

    /**
     * Mark a review as helpful.
     * @param reviewId ID of the review
     * @return Result<Unit>
     */
    suspend fun markReviewHelpful(reviewId: String): Result<Unit>

    suspend fun getCurrentUserReview(productId: String): Result<Review?>
    /**
     * Search within product reviews.
     * @param query Search string
     * @return Result containing matching reviews
     */
    suspend fun searchProductReviews( query: String): Result<List<Review>>

    suspend fun getProductsByTag(tag: String, limit: Int = 20): Result<List<ProductItem>>

}

/**
 * Implementation of [ProductCrudRepository] using Firestore.
 * 
 * @property firestore FirebaseFirestore instance
 */
class ProductCrudRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : ProductCrudRepository {

    private val productsCollection = firestore.collection(FirestoreCollections.PRODUCT)
    private val ratingsCollection = firestore.collection(FirestoreCollections.RATINGS)
    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    companion object {
        private const val TAG = "ProductCrudRepository"
        private const val CACHE_EXPIRY_MS = 5 * 60 * 1000L // 5 minutes
    }

    /**
     * Represents a cached result with a timestamp for expiration checking.
     */
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        /**
         * Checks if the cache entry has exceeded its time-to-live.
         */
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS
    }

    private val productCache = mutableMapOf<String, CacheEntry<ProductItem>>()
    private val listCache = mutableMapOf<String, CacheEntry<List<ProductItem>>>()
    private val documentReferenceCache = mutableMapOf<String, CacheEntry<Map<String, Any>>>()

    override suspend fun createProduct(product: ProductItem): Result<ProductItem> {
        return try {
            // Check if product with same name already exists (case-insensitive)
            val existingSnapshot = productsCollection
                .whereEqualTo("productName", product.productName)
                .get()
                .await()

            if (!existingSnapshot.isEmpty) {
                Log.w(TAG, "Product already exists: ${product.productName}")
                return Result.failure(Exception("Product '${product.productName}' already exists"))
            }
            val documentId = productsCollection.document().id

            val newProduct = product.copy(
                id = documentId,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            // Convert to map for Firestore
            val productMap = productToMap(newProduct)

            productsCollection.document(documentId)
                .set(productMap)
                .await()

            Log.d(TAG, "Product created successfully: $documentId")
            Result.success(newProduct)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create product", e)
            Result.failure(Exception("Failed to create product: ${e.message}"))
        }
    }

    // Get All Products with Pagination
    // In ProductCrudRepositoryImpl.kt - Update just the getProducts() method

    override suspend fun getProducts(page: Int, pageSize: Int): Result<List<ProductItem>> {
        return try {
            val snapshot = productsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())
                .get()
                .await()

            val products = snapshot.documents.mapNotNull { doc ->
                try {
                    mapToProduct(doc.data)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing product document ${doc.id}", e)
                    null
                }
            }

            Log.d(TAG, "Fetched ${products.size} products")
            Result.success(products)

        } catch (e: FirebaseFirestoreException) {
            // Check for specific "failed to get documents from server" error
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE ||
                e.message?.contains("failed to get documents from server") == true) {

                Log.w(TAG, "Network issue - Firebase server unavailable", e)

                // Show user-friendly error
                Result.failure(
                    Exception("⚠️ Network connection issue. Please check your internet and try again.")
                )
            } else {
                // Other Firebase errors
                Log.e(TAG, "Firestore error fetching products", e)
                Result.failure(
                    Exception("Failed to load products. Error: ${e.message}")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch products", e)
            Result.failure(
                Exception("Failed to load products. Please try again.")
            )
        }
    }

    // Update Product
    override suspend fun updateProduct(product: ProductItem): Result<ProductItem> {
        return try {
            Log.d(TAG, "Updating product: ${product.id}")

            // Check if document exists
            val docSnapshot = productsCollection.document(product.id).get().await()
            if (!docSnapshot.exists()) {
                Log.e(TAG, "Product does not exist: ${product.id}")
                return Result.failure(Exception("Product not found"))
            }

            // Check if new name already exists in another product
            if (product.productName.isNotBlank()) {
                val existingSnapshot = productsCollection
                    .whereEqualTo("productName", product.productName)
                    .get()
                    .await()

                val isDuplicate = existingSnapshot.documents.any { doc ->
                    doc.id != product.id
                }

                if (isDuplicate) {
                    Log.w(TAG, "Product name already exists: ${product.productName}")
                    return Result.failure(Exception("Product '${product.productName}' already exists"))
                }
            }

            val updatedProduct = product.copy(updatedAt = Timestamp.now())
            val productMap = productToMap(updatedProduct)

            productsCollection.document(product.id)
                .set(productMap)
                .await()

            Log.d(TAG, "Product updated successfully: ${product.id}")
            Result.success(updatedProduct)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update product: ${product.id}", e)
            Result.failure(Exception("Failed to update product: ${e.message}"))
        }
    }

    // Delete Product
    override suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting product: $productId")

            val docSnapshot = productsCollection.document(productId).get().await()
            if (!docSnapshot.exists()) {
                Log.e(TAG, "Product does not exist: $productId")
                return Result.failure(Exception("Product not found"))
            }

            productsCollection.document(productId)
                .delete()
                .await()

            Log.d(TAG, "Product deleted successfully: $productId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete product: $productId", e)
            Result.failure(Exception("Failed to delete product: ${e.message}"))
        }
    }

    // Get Products by Category
    override suspend fun getProductsByCategory(
        categoryId: String,
        page: Int,
        pageSize: Int
    ): Result<List<ProductItem>> {
        return try {
            val snapshot = productsCollection
                .whereEqualTo("category", categoryId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())
                .get()
                .await()

            val products = snapshot.documents.mapNotNull { doc ->
                try {
                    mapToProduct(doc.data)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing product document ${doc.id}", e)
                    null
                }
            }

            Result.success(products)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch products by category", e)
            Result.failure(Exception("Failed to fetch products: ${e.message}"))
        }
    }

    override suspend fun getProductsByTag(tag: String, limit: Int): Result<List<ProductItem>> {
        return try {
            val snapshot = productsCollection
                .whereArrayContains("tags", tag)
                .limit(limit.toLong())
                .get()
                .await()

            val products = snapshot.documents.mapNotNull { doc ->
                try {
                    mapToProduct(doc.data)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing product document ${doc.id}", e)
                    null
                }
            }

            Result.success(products)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch products by tag: $tag", e)
            Result.failure(Exception("Failed to fetch products: ${e.message}"))
        }
    }

    // Clear Cache
    override fun clearCache() {
        // Implement cache clearing if needed
        productCache.clear()
        listCache.clear()
        documentReferenceCache.clear()
    }

    // Helper: Convert Product to Map
    private fun productToMap(product: ProductItem): Map<String, Any?> {
        return hashMapOf(
            "id" to product.id,
            "userId" to product.userId,
            "inStock" to product.inStock,
            "productName" to product.productName,
            "price" to product.price,
            "brand" to product.brand,
            "category" to product.category,
            "shipment" to product.shipment,
            "sizes" to product.sizes,
            "description" to product.description,
            "imageUrl" to product.imageUrl,
            "supportingImageUrls" to product.supportingImageUrls,
            "rating" to product.rating,
            "colors" to product.colors,
            "oldPrice" to product.oldPrice,
            "reviewCount" to product.reviewCount,
            "quantity" to product.quantity,
            "isFavorite" to product.isFavorite,
            "isInCart" to product.isInCart,
            "userId" to product.userId,
            "tags" to product.tags,
            "createdAt" to product.createdAt,
            "updatedAt" to product.updatedAt
        )
    }

    // Helper: Convert Map to Product
    private fun mapToProduct(data: Map<String, Any?>?): ProductItem? {
        if (data == null) return null

        return try {
            ProductItem(
                id = data["id"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                inStock = data["inStock"] as? Boolean ?: true,
                productName = data["productName"] as? String ?: "",
                price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                brand = data["brand"] as? String ?: "",
                category = data["category"] as? String ?: "",
                shipment = data["shipment"] as? String ?: "",
                sizes = (data["sizes"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                description = data["description"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String ?: "",
                supportingImageUrls = (data["supportingImageUrls"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                rating = (data["rating"] as? Number)?.toFloat() ?: 0f,
                colors = (data["colors"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                oldPrice = (data["oldPrice"] as? Number)?.toDouble() ?: 0.0,
                reviewCount = (data["reviewCount"] as? Number)?.toInt() ?: 0,
                quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                isFavorite = data["isFavorite"] as? Boolean ?: false,
                isInCart = data["isInCart"] as? Boolean ?: false,
                tags = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                createdAt = data["createdAt"] as? Timestamp,
                updatedAt = data["updatedAt"] as? Timestamp
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping product data", e)
            null
        }
    }

    override suspend fun searchProducts(query: String): Result<List<ProductItem>> {
        val cacheKey = "search_${query.lowercase().trim()}"

        // Check cache
        listCache[cacheKey]?.let { cached ->
            if (!cached.isExpired()) {
                return Result.success(cached.data)
            }
        }

        return try {

            // Fetch all products (consider using cached version)
            val allProductsResult = getProducts(pageSize = 100) // Limit search scope
            if (allProductsResult.isFailure) {
                return Result.failure(
                    allProductsResult.exceptionOrNull() ?: Exception("Failed to fetch products")
                )
            }

            val allProducts = allProductsResult.getOrNull() ?: emptyList()
            val searchTerm = query.trim().lowercase()

            // Step 1: Try exact/partial matching
            val exactMatches = allProducts.filter { product ->
                product.productName.lowercase().contains(searchTerm) ||
                        product.description.lowercase().contains(searchTerm) ||
                        product.category.lowercase().contains(searchTerm) ||
                        product.brand.lowercase().contains(searchTerm)
            }

            if (exactMatches.isNotEmpty()) {
                listCache[cacheKey] = CacheEntry(exactMatches)
                return Result.success(exactMatches)
            }

            // Step 2: Try word-by-word matching
            val words = searchTerm.split(" ").filter { it.length > 2 }
            if (words.isNotEmpty()) {
                val wordMatches = allProducts.filter { product ->
                    val productText =
                        "${product.productName} ${product.description} ${product.category} ${product.brand}".lowercase()
                    words.any { word -> productText.contains(word) }
                }

                if (wordMatches.isNotEmpty()) {
                    listCache[cacheKey] = CacheEntry(wordMatches)
                    return Result.success(wordMatches)
                }
            }

            // Step 3: Try fuzzy matching
            val fuzzyMatches = findFuzzyMatches(allProducts, searchTerm)
            if (fuzzyMatches.isNotEmpty()) {
                listCache[cacheKey] = CacheEntry(fuzzyMatches)
                return Result.success(fuzzyMatches)
            }

            // Step 4: Category suggestions
            if (isValidSearchTerm(searchTerm)) {
                val suggestions = getCategorySuggestions(allProducts, searchTerm)
                if (suggestions.isNotEmpty()) {
                    listCache[cacheKey] = CacheEntry(suggestions)
                    return Result.success(suggestions)
                }
            }

            Result.success(emptyList())

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun findFuzzyMatches(products: List<ProductItem>, query: String): List<ProductItem> {
        val synonymMap = mapOf(
            "shoes" to listOf("shoe", "footwear", "sneaker", "boot", "sandal", "slipper"),
            "shirt" to listOf("shirts", "top", "blouse", "tee", "t-shirt"),
            "pants" to listOf("pant", "trouser", "jeans", "slacks", "bottoms"),
            "dress" to listOf("dresses", "gown", "frock"),
            "bag" to listOf("bags", "purse", "handbag", "backpack", "tote"),
            "watch" to listOf("watches", "timepiece", "wristwatch"),
            "chair" to listOf("chairs", "seat", "seating", "stool"),
            "table" to listOf("tables", "desk", "furniture"),
            "jacket" to listOf("jackets", "coat", "blazer", "hoodie"),
            "wear" to listOf("clothing", "apparel", "garment", "attire"),
            "cloth" to listOf("clothes", "clothing", "fabric", "textile"),
            "pipe" to listOf("pipes", "plumbing", "tube", "fitting")
        )

        val relatedTerms = mutableListOf(query)
        synonymMap.forEach { (key, synonyms) ->
            if (query.contains(key) || synonyms.any { query.contains(it) }) {
                relatedTerms.add(key)
                relatedTerms.addAll(synonyms)
            }
        }

        return products.filter { product ->
            val productText =
                "${product.productName} ${product.description} ${product.category} ${product.brand}".lowercase()
            relatedTerms.any { term -> productText.contains(term) }
        }.distinctBy { it.id }
    }

    private fun isValidSearchTerm(query: String): Boolean {
        if (query.length < 2 || query.length > 50) return false

        val vowels = "aeiou"
        val vowelCount = query.count { it in vowels }
        if (vowelCount == 0 && query.length > 3) return false

        val repeatingPattern = Regex("""(.)\1{3,}""")
        if (repeatingPattern.containsMatchIn(query)) return false

        val alternatingPattern = Regex("""^([a-z])([a-z])\1\2""")
        return !alternatingPattern.containsMatchIn(query)
    }

    private fun getCategorySuggestions(
        products: List<ProductItem>,
        query: String
    ): List<ProductItem> {
        val categoryHints = mapOf(
            "shoes" to listOf("Footwear", "Shoes", "Sports", "Fashion"),
            "shirt" to listOf("Clothing", "Men", "Women", "Fashion", "Apparel"),
            "pants" to listOf("Clothing", "Men", "Women", "Fashion", "Apparel"),
            "dress" to listOf("Clothing", "Women", "Fashion", "Apparel"),
            "bag" to listOf("Accessories", "Fashion", "Bags"),
            "watch" to listOf("Accessories", "Jewelry", "Electronics"),
            "furniture" to listOf("Home", "Furniture", "Decor"),
            "chair" to listOf("Furniture", "Home", "Office"),
            "table" to listOf("Furniture", "Home", "Office"),
            "wear" to listOf("Clothing", "Fashion", "Apparel"),
            "cloth" to listOf("Clothing", "Fashion", "Apparel", "Fabric"),
            "pipe" to listOf("Plumbing", "Hardware", "Home", "Tools")
        )

        val relevantCategories = categoryHints.entries
            .filter { (key, _) -> query.contains(key) }
            .flatMap { it.value }
            .distinct()

        if (relevantCategories.isEmpty()) return emptyList()

        return products.filter { product ->
            relevantCategories.any { category ->
                product.category.contains(category, ignoreCase = true)
            }
        }.take(20)
    }

    override suspend fun getProductById(productId: String): Result<ProductItem?> {
        // Check cache first
        productCache[productId]?.let { cached ->
            if (!cached.isExpired()) {
                return Result.success(cached.data)
            }
        }

        return try {
            val document =  productsCollection
                .document(productId)
                .get()
                .await()

            if (!document.exists()) {
                return Result.success(null)
            }

            val product = mapToProduct(document.data)

            // Cache the product
            product?.let { p ->
                productCache[productId] = CacheEntry(p)
            }

            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchProductsWithFallback(query: String): Result<SearchResult> {
        return try {

            // Validate search term first
            if (!isValidSearchTerm(query.trim())) {
                return Result.success(
                    SearchResult(
                        exactMatches = emptyList(),
                        similarProducts = emptyList(),
                        searchType = SearchType.NO_RESULTS
                    )
                )
            }

            // Fetch all products (use cached version from pagination)
            val allProductsResult = getProducts(pageSize = 100)
            if (allProductsResult.isFailure) {
                return Result.failure(
                    allProductsResult.exceptionOrNull() ?: Exception("Failed to fetch products")
                )
            }

            val allProducts = allProductsResult.getOrNull() ?: emptyList()
            val searchTerm = query.trim().lowercase()

            //  Try exact/partial matching
            val exactMatches = findExactMatches(allProducts, searchTerm)
            if (exactMatches.isNotEmpty()) {
                return Result.success(
                    SearchResult(
                        exactMatches = exactMatches,
                        similarProducts = emptyList(),
                        searchType = SearchType.EXACT_MATCH
                    )
                )
            }

            // Try word-by-word matching
            val wordMatches = findWordMatches(allProducts, searchTerm)
            if (wordMatches.isNotEmpty()) {
                return Result.success(
                    SearchResult(
                        exactMatches = wordMatches,
                        similarProducts = emptyList(),
                        searchType = SearchType.EXACT_MATCH
                    )
                )
            }

            // Try fuzzy matching with synonyms
            val fuzzyMatches = findFuzzyMatches(allProducts, searchTerm)
            if (fuzzyMatches.isNotEmpty()) {
                return Result.success(
                    SearchResult(
                        exactMatches = fuzzyMatches,
                        similarProducts = emptyList(),
                        searchType = SearchType.SIMILAR_MATCH
                    )
                )
            }

            //  Try category suggestions
            val categorySuggestions = getCategorySuggestions(allProducts, searchTerm)
            if (categorySuggestions.isNotEmpty()) {
                return Result.success(
                    SearchResult(
                        exactMatches = emptyList(),
                        similarProducts = categorySuggestions,
                        searchType = SearchType.CATEGORY_MATCH
                    )
                )
            }

            //  Fallback to popular/default products
            val defaultProducts = getDefaultFallbackProducts(allProducts)
            Result.success(
                SearchResult(
                    exactMatches = emptyList(),
                    similarProducts = defaultProducts,
                    searchType = SearchType.DEFAULT_PRODUCTS
                )
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun findExactMatches(products: List<ProductItem>, query: String): List<ProductItem> {
        return products.filter { product ->
            product.productName.lowercase().contains(query) ||
                    product.description.lowercase().contains(query) ||
                    product.category.lowercase().contains(query) ||
                    product.brand.lowercase().contains(query) ||
                    // Add more flexible matching
                    query.split(" ").any { word ->
                        word.length > 2 && (
                                product.productName.lowercase().contains(word) ||
                                        product.description.lowercase().contains(word) ||
                                        product.category.lowercase().contains(word) ||
                                        product.brand.lowercase().contains(word)
                                )
                    }
        }
    }

    // Helper: Find word-by-word matches
    private fun findWordMatches(products: List<ProductItem>, query: String): List<ProductItem> {
        val words = query.split(" ").filter { it.length > 2 }
        if (words.isEmpty()) return emptyList()

        return products.filter { product ->
            val productText =
                "${product.productName} ${product.description} ${product.category} ${product.brand}".lowercase()
            words.any { word -> productText.contains(word) }
        }
    }

    // Helper: Get default fallback products (featured, trending, or top-rated)
    private fun getDefaultFallbackProducts(allProducts: List<ProductItem>): List<ProductItem> {

        // Fallback to top-rated products from all products
        return allProducts
            .sortedByDescending { it.rating }
            .take(12)
    }

    override suspend fun submitProductRating(
        productId: String,
        rating: Float,
        review: String?
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            // Create rating document - use auto-generated ID for simpler rules
            val ratingData = hashMapOf(
                "userId" to userId,
                "productId" to productId,
                "rating" to rating,
                "review" to review,
                "timestamp" to Timestamp.now()
            )

            // Store individual rating with auto-generated ID
            ratingsCollection
                .add(ratingData) // Use add() instead of set() with custom ID
                .await()

            // Calculate and update product's average rating
            updateProductAverageRating(productId)

            // Clear cache for this product
            productCache.remove(productId)
            listCache.clear()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateProductAverageRating(productId: String) {
        try {
            // Get all ratings for this product
            val ratingsSnapshot = ratingsCollection
                .whereEqualTo("productId", productId)
                .get()
                .await()

            val ratings = ratingsSnapshot.documents.mapNotNull {
                (it.data?.get("rating") as? Number)?.toFloat()
            }

            if (ratings.isNotEmpty()) {
                val averageRating = ratings.average().toFloat()
                val reviewCount = ratings.size

                // Update product document
                productsCollection
                    .document(productId)
                    .update(
                        mapOf(
                            "rating" to averageRating,
                            "reviewCount" to reviewCount
                        )
                    )
                    .await()

            }
        } catch (_: Exception) {
        }
    }

    override suspend fun getProductReviews(
        productId: String,
        limit: Int
    ): Result<List<Review>> {
        return try {

            // Query reviews collection directly (like querying cart)
            val snapshot = ratingsCollection
                .whereEqualTo("productId", productId)  // Filter by productId
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val reviews = snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null

                    Review(
                        id = document.id,
                        userId = data["userId"] as? String ?: "",
                        userName = data["userName"] as? String ?: "Anonymous",
                        userProfileImage = data["userProfileImage"] as? String ?: "",
                        productId = data["productId"] as? String ?: "",
                        rating = (data["rating"] as? Number)?.toFloat() ?: 0f,
                        review = data["review"] as? String ?: "",
                        timestamp = data["timestamp"] as? Timestamp,
                        helpful = (data["helpful"] as? Number)?.toInt() ?: 0,
                        verified = data["verified"] as? Boolean ?: false,
                        updatedAt = data["updatedAt"] as? Timestamp
                    )
                } catch (_: Exception) {
                    null
                }
            }

            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markReviewHelpful(reviewId: String): Result<Unit> {
        return try {
            //   Use RATINGS_COLLECTION instead of REVIEWS_COLLECTION
            ratingsCollection
                .document(reviewId)
                .update(
                    mapOf(
                        "helpful" to FieldValue.increment(1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserReview(productId: String): Result<Review?> {
        return try {
            val userId = getCurrentUserId()
                ?: return Result.success(null)

            // Use the same composite ID pattern
            val reviewId = "${productId}_${userId}"

            val document = ratingsCollection
                .document(reviewId)
                .get()
                .await()

            if (!document.exists()) {
                return Result.success(null)
            }

            val data = document.data
            val review = data?.let {
                mapToRatingsReview(data)
            }

            Result.success(review)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchProductReviews(query: String): Result<List<Review>> {
        return try {

            val searchTerm = query.trim().lowercase()

            if (searchTerm.isEmpty()) {
                return Result.success(emptyList())
            }

            // Get all reviews (you might want to add pagination here)
            val snapshot = ratingsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100) // Reasonable limit for search
                .get()
                .await()

            val allReviews = snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null

                    mapToRatingsReview(data)
                } catch (_: Exception) {
                    null
                }
            }

            // Filter reviews based on search term
            val filteredReviews = allReviews.filter { review ->
                review.review.lowercase().contains(searchTerm) ||
                        review.userName.lowercase().contains(searchTerm)
            }

            Result.success(filteredReviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Maps a Firestore document data map to a [Review] object.
     * 
     * @param data The raw map data from Firestore.
     * @return A [Review] object if successful, null otherwise.
     */
    fun mapToRatingsReview(data: Map<String, Any?>?): Review? {
        if (data == null) return null

        return try {
            Review(
                id = data["id"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                userName = data["userName"] as? String ?: "Anonymous",
                userProfileImage = data["userProfileImage"] as? String ?: "",
                productId = data["productId"] as? String ?: "",
                rating = (data["rating"] as? Number)?.toFloat() ?: 0f,
                review = data["review"] as? String ?: "",
                timestamp = data["timestamp"] as? Timestamp,
                helpful = (data["helpful"] as? Number)?.toInt() ?: 0,
                verified = data["verified"] as? Boolean ?: false,
                updatedAt = data["updatedAt"] as? Timestamp
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping review data", e)
            null
        }
    }
}
