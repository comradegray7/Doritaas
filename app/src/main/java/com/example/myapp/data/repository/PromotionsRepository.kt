package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.DiscountType
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.PromotionProduct
import com.example.myapp.data.dataclass.PromotionsData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import jakarta.inject.Inject

import kotlinx.coroutines.tasks.await

/**
 * Interface for managing Special Offers and Promotions.
 * 
 * Handles Admin CRUD operations for marketing content.
 */
interface PromotionRepository {
    suspend fun getPromotions(): Result<List<PromotionsData>>
    suspend fun createPromotion(item: PromotionsData): Result<Unit>
    suspend fun getPromotionById(promotionId: String): Result<PromotionsData>
    suspend fun updatePromotion(item: PromotionsData): Result<Unit>
    suspend fun deletePromotion(promotionId: String): Result<Unit>
    suspend fun searchPromotions(query: String): Result<List<PromotionsData>>
    suspend fun addProductToPromotion(promotionId: String, productId: String): Result<Unit>
    suspend fun removeProductFromPromotion(promotionId: String, productId: String): Result<Unit>
    suspend fun addProductsToPromotion(promotionId: String, productIds: List<String>): Result<Unit>

    //  Query methods
    suspend fun getProductsByPromotion(promotionId: String): Result<List<ProductItem>>
    suspend fun getProductsInActivePromotions(limit: Int = 20): Result<List<ProductItem>>

    //  Tag-based auto-assignment
    suspend fun addProductsByTag(promotionId: String, tag: String): Result<Int> // Returns count
    suspend fun addProductsByCategory(promotionId: String, categoryId: String): Result<Int>

}

// ============================================================================
// REPOSITORY IMPLEMENTATION (COMPLETE)
// ============================================================================

/**
 * PromotionRepositoryImpl
 *
 * Implementation of [PromotionRepository] using Firebase Firestore. 
 * Manages specialized promotional content and junction data for associating products with promotions.
 */
class PromotionRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : PromotionRepository {
    private val promotionsCollection = firestore.collection(FirestoreCollections.PROMOTIONS)
    private val productsCollection = firestore.collection(FirestoreCollections.PRODUCT)

    private val promotionProductCollection  = firestore.collection(FirestoreCollections.PROMOTION_PRODUCTS)

    companion object {
        private const val TAG = "PromotionRepository"
    }

    override suspend fun createPromotion(item: PromotionsData): Result<Unit> {
        return try {
            val existingSnapshot = promotionsCollection
                .whereEqualTo("title", item.title)
                .get()
                .await()

            if (!existingSnapshot.isEmpty) {
                return Result.failure(Exception("Promotion '${item.title}' already exists"))
            }

            val promotionId = promotionsCollection.document().id
            val currentTime = System.currentTimeMillis()

            val promotion = item.copy(
                id = promotionId,
                createdAt = currentTime,
                updatedAt = currentTime,
                expired = item.endAt < currentTime
            )

            val promotionMap = hashMapOf(
                "id" to promotion.id,
                "title" to promotion.title,
                "description" to promotion.description,
                "startAt" to promotion.startAt,
                "endAt" to promotion.endAt,
                "createdAt" to promotion.createdAt,
                "updatedAt" to promotion.updatedAt,
                "expired" to promotion.expired,
                "discountType" to promotion.discountType.name,
                "discountValue" to promotion.discountValue,
                "minPurchaseAmount" to promotion.minPurchaseAmount,
                "maxDiscountAmount" to promotion.maxDiscountAmount,
                "applicableCategories" to promotion.applicableCategories,
                "applicableTags" to promotion.applicableTags,
                "isActive" to promotion.isActive,
                "usageLimit" to promotion.usageLimit,
                "usageCount" to promotion.usageCount,
                "priority" to promotion.priority
            )

            promotionsCollection.document(promotionId).set(promotionMap).await()
            Log.d(TAG, "Promotion created: $promotionId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create promotion", e)
            Result.failure(Exception("Failed to create promotion: ${e.message}"))
        }
    }

    override suspend fun getPromotions(): Result<List<PromotionsData>> {
        return try {
            val snapshot = promotionsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val currentTime = System.currentTimeMillis()
            val promotions = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toPromotionData(currentTime)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing promotion ${doc.id}", e)
                    null
                }
            }

            Log.d(TAG, "Fetched ${promotions.size} promotions")
            Result.success(promotions)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch promotions", e)
            Result.failure(Exception("Failed to fetch promotions: ${e.message}"))
        }
    }

    override suspend fun getPromotionById(promotionId: String): Result<PromotionsData> {
        return try {
            val snapshot = promotionsCollection.document(promotionId).get().await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("Promotion not found"))
            }

            val currentTime = System.currentTimeMillis()
            val promotion = snapshot.toPromotionData(currentTime)
                ?: return Result.failure(Exception("Failed to parse promotion"))

            Result.success(promotion)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch promotion: $promotionId", e)
            Result.failure(Exception("Failed to fetch promotion: ${e.message}"))
        }
    }

    override suspend fun updatePromotion(item: PromotionsData): Result<Unit> {
        return try {
            val docSnapshot = promotionsCollection.document(item.id).get().await()
            if (!docSnapshot.exists()) {
                return Result.failure(Exception("Promotion not found"))
            }

            val existingSnapshot = promotionsCollection
                .whereEqualTo("title", item.title)
                .get()
                .await()

            val isDuplicate = existingSnapshot.documents.any { doc ->
                doc.id != item.id
            }

            if (isDuplicate) {
                return Result.failure(Exception("Promotion '${item.title}' already exists"))
            }

            val currentTime = System.currentTimeMillis()
            val updates = hashMapOf<String, Any>(
                "title" to item.title,
                "description" to item.description,
                "startAt" to item.startAt,
                "endAt" to item.endAt,
                "updatedAt" to currentTime,
                "expired" to (item.endAt < currentTime),
                "discountType" to item.discountType.name,
                "discountValue" to item.discountValue,
                "minPurchaseAmount" to item.minPurchaseAmount,
                "maxDiscountAmount" to item.maxDiscountAmount,
                "isActive" to item.isActive
            )

            promotionsCollection.document(item.id).update(updates).await()
            Log.d(TAG, "Promotion updated: ${item.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update promotion: ${item.id}", e)
            Result.failure(Exception("Failed to update promotion: ${e.message}"))
        }
    }

    override suspend fun deletePromotion(promotionId: String): Result<Unit> {
        return try {
            val docSnapshot = promotionsCollection.document(promotionId).get().await()
            if (!docSnapshot.exists()) {
                return Result.failure(Exception("Promotion not found"))
            }

            // Delete all product associations
            val junctionSnapshot = promotionsCollection
                .whereEqualTo("promotionId", promotionId)
                .get()
                .await()

            junctionSnapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            promotionsCollection.document(promotionId).delete().await()
            Log.d(TAG, "Promotion deleted: $promotionId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete promotion: $promotionId", e)
            Result.failure(Exception("Failed to delete promotion: ${e.message}"))
        }
    }

    override suspend fun searchPromotions(query: String): Result<List<PromotionsData>> {
        return try {
            val snapshot = promotionsCollection.get().await()
            val currentTime = System.currentTimeMillis()

            val promotions = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toPromotionData(currentTime)
                } catch (_: Exception) {
                    null
                }
            }.filter { promotion ->
                promotion.title.contains(query, ignoreCase = true) ||
                        promotion.description.contains(query, ignoreCase = true)
            }

            Result.success(promotions)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search promotions", e)
            Result.failure(Exception("Failed to search promotions: ${e.message}"))
        }
    }


    override suspend fun addProductToPromotion(
        promotionId: String,
        productId: String
    ): Result<Unit> {
        return try {
            val promotion = getPromotionById(promotionId).getOrNull()
                ?: return Result.failure(Exception("Promotion not found"))

            if (promotion.expired) {
                return Result.failure(Exception("Cannot add products to expired promotion"))
            }

            //  Check if already exists using promotionProductCollection
            val existing = promotionProductCollection
                .whereEqualTo("promotionId", promotionId)
                .whereEqualTo("productId", productId)
                .limit(1)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.success(Unit)
            }

            //  Create new document in promotionProductCollection
            val junctionDoc = PromotionProduct(
                id = promotionProductCollection.document().id,  
                promotionId = promotionId,
                productId = productId,
                addedAt = System.currentTimeMillis(),
                addedBy = auth.currentUser?.uid ?: ""
            )

            promotionProductCollection.document(junctionDoc.id)
                .set(junctionDoc)
                .await()

            Log.d(TAG, "Added product $productId to promotion $promotionId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add product to promotion", e)
            Result.failure(e)
        }
    }

    override suspend fun removeProductFromPromotion(
        promotionId: String,
        productId: String
    ): Result<Unit> {
        return try {
            val snapshot = promotionProductCollection
                .whereEqualTo("promotionId", promotionId)
                .whereEqualTo("productId", productId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            Log.d(TAG, "Removed product $productId from promotion $promotionId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove product from promotion", e)
            Result.failure(e)
        }
    }

    override suspend fun addProductsToPromotion(
        promotionId: String,
        productIds: List<String>
    ): Result<Unit> {
        return try {
            val batch = promotionProductCollection.firestore.batch()

            productIds.forEach { productId ->
                val docRef = promotionProductCollection.document()
                val junctionDoc = PromotionProduct(
                    id = docRef.id,
                    promotionId = promotionId,
                    productId = productId,
                    addedAt = System.currentTimeMillis(),
                    addedBy = auth.currentUser?.uid ?: ""
                )
                batch.set(docRef, junctionDoc)
            }

            batch.commit().await()
            Log.d(TAG, "Added ${productIds.size} products to promotion $promotionId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add products to promotion", e)
            Result.failure(e)
        }
    }

    // Use promotionProductCollection instead of promotionsCollection
    override suspend fun getProductsByPromotion(
        promotionId: String
    ): Result<List<ProductItem>> {
        return try {
            Log.d(TAG, "Getting products for promotion: $promotionId")

            //  Query the correct collection
            val junctionSnapshot = promotionProductCollection
                .whereEqualTo("promotionId", promotionId)
                .get()
                .await()

            Log.d(TAG, "Found ${junctionSnapshot.size()} junction records")

            val productIds = junctionSnapshot.documents.mapNotNull {
                it.getString("productId")
            }

            if (productIds.isEmpty()) {
                Log.d(TAG, "No products found for promotion $promotionId")
                return Result.success(emptyList())
            }

            Log.d(TAG, "Product IDs: $productIds")

            val products = mutableListOf<ProductItem>()
            productIds.chunked(10).forEach { chunk ->
                val snapshot = productsCollection
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()

                products.addAll(snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ProductItem::class.java)
                })
            }

            Log.d(TAG, "Retrieved ${products.size} products for promotion $promotionId")
            Result.success(products)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get products for promotion", e)
            Result.failure(e)
        }
    }

    // Get products from active promotions
    override suspend fun getProductsInActivePromotions(
        limit: Int
    ): Result<List<ProductItem>> {
        return try {
            val currentTime = System.currentTimeMillis()

            //  First, get active promotion IDs
            val activePromotionsSnapshot = promotionsCollection
                .whereEqualTo("isActive", true)
                .whereGreaterThan("endAt", currentTime)
                .get()
                .await()

            val promotionIds = activePromotionsSnapshot.documents.map { it.id }

            if (promotionIds.isEmpty()) {
                Log.d(TAG, "No active promotions found")
                return Result.success(emptyList())
            }

            Log.d(TAG, "Found ${promotionIds.size} active promotions")

            //   Get product IDs from junction table
            val productIds = mutableSetOf<String>()
            promotionIds.chunked(10).forEach { chunk ->
                val snapshot = promotionProductCollection
                    .whereIn("promotionId", chunk)
                    .get()
                    .await()

                productIds.addAll(snapshot.documents.mapNotNull {
                    it.getString("productId")
                })
            }

            if (productIds.isEmpty()) {
                Log.d(TAG, "No products found in active promotions")
                return Result.success(emptyList())
            }

            Log.d(TAG, "Found ${productIds.size} products in active promotions")

            //   Get actual products
            val products = mutableListOf<ProductItem>()
            productIds.toList().chunked(10).forEach { chunk ->
                val snapshot = productsCollection
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()

                products.addAll(snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ProductItem::class.java)
                })
            }

            Log.d(TAG, "Retrieved ${products.size} products from active promotions")
            Result.success(products.take(limit))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get products in active promotions", e)
            Result.failure(e)
        }
    }

    override suspend fun addProductsByTag(
        promotionId: String,
        tag: String
    ): Result<Int> {
        return try {
            val snapshot = productsCollection
                .whereArrayContains("tags", tag)
                .get()
                .await()

            val productIds = snapshot.documents.map { it.id }

            if (productIds.isEmpty()) {
                return Result.success(0)
            }

            addProductsToPromotion(promotionId, productIds)
            Result.success(productIds.size)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add products by tag", e)
            Result.failure(e)
        }
    }

    override suspend fun addProductsByCategory(
        promotionId: String,
        categoryId: String
    ): Result<Int> {
        return try {
            val snapshot = productsCollection
                .whereEqualTo("category", categoryId)
                .get()
                .await()

            val productIds = snapshot.documents.map { it.id }

            if (productIds.isEmpty()) {
                return Result.success(0)
            }

            addProductsToPromotion(promotionId, productIds)
            Result.success(productIds.size)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add products by category", e)
            Result.failure(e)
        }
    }

    // Extension function to convert Firestore document to PromotionsData
private fun DocumentSnapshot.toPromotionData(currentTime: Long): PromotionsData? {
    return try {
        val discountTypeStr = getString("discountType") ?: "PERCENTAGE"
        val discountType = try {
            DiscountType.valueOf(discountTypeStr)
        } catch (_: Exception) {
            DiscountType.PERCENTAGE
        }

        PromotionsData(
            id = getString("id") ?: id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            startAt = getLong("startAt") ?: currentTime,
            endAt = getLong("endAt") ?: currentTime,
            createdAt = getLong("createdAt") ?: currentTime,
            updatedAt = getLong("updatedAt") ?: currentTime,
            expired = getBoolean("expired") ?: ((getLong("endAt") ?: 0) < currentTime),
            discountType = discountType,
            discountValue = getDouble("discountValue") ?: 0.0,
            minPurchaseAmount = getDouble("minPurchaseAmount") ?: 0.0,
            maxDiscountAmount = getDouble("maxDiscountAmount") ?: 0.0,
            applicableCategories = get("applicableCategories").safeStringList(),
              applicableTags = get("applicableTags").safeStringList(),
//            applicableCategories = get("applicableCategories") as? List<String> ?: emptyList(),
//            applicableTags = get("applicableTags") as? List<String> ?: emptyList(),
            isActive = getBoolean("isActive") ?: true,
            usageLimit = getLong("usageLimit")?.toInt() ?: 0,
            usageCount = getLong("usageCount")?.toInt() ?: 0,
            priority = getLong("priority")?.toInt() ?: 0
        )
    } catch (_: Exception) {
        null
    }
}
}

fun Any?.safeStringList(): List<String> = when (this) {
    is List<*> -> this.filterIsInstance<String>()
    else -> emptyList()
}