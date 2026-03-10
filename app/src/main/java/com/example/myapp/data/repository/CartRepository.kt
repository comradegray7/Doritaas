package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.ProductItem
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Interface definition for Cart data operations.
 *
 * Manages the user's shopping cart through Firebase Firestore.
 */
interface CartRepository {
    /**
     * Add a product to the cart.
     * If item exists, increments quantity.
     *
     * @param item The product item to add
     * @return Result containing Success or Failure
     */
    suspend fun addToCart(item: ProductItem): Result<Unit>

    /**
     * Remove a product from the cart.
     *
     * @param productId ID of the product to remove
     * @return Result<Unit>
     */
    suspend fun removeFromCart(productId: String): Result<Unit>

    /**
     * Update quantity of a cart item.
     * If quantity <= 0, the item is removed.
     *
     * @param productId ID of the product
     * @param quantity New quantity value
     * @return Result<Unit>
     */
    suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit>

    /**
     * Observe all cart items in real-time.
     *
     * @return Flow emitting list of [ProductItem]s
     */
    fun getCartItems(): Flow<List<ProductItem>>

    /**
     * Remove all items from the cart.
     *
     * @return Result<Unit>
     */
    suspend fun clearCart(): Result<Unit>

    /**
     * Check if a specific product is currently in the cart.
     *
     * @param productId ID of the product to check
     * @return Flow emitting true if in cart
     */
    fun isInCart(productId: String): Flow<Boolean>

    /**
     * Observe the total number of unique items in the cart.
     *
     * @return Flow emitting the count
     */
    fun getCartItemCount(): Flow<Int>

    /**
     * Force refresh of cart items from server.
     * Bypasses local cache.
     *
     * @return Result containing fresh list of products
     */
    suspend fun refreshCartItems(): Result<List<ProductItem>>

    /**
     * Observe the total price of all items in the cart.
     *
     * @return Flow emitting total calculation (price * quantity for all items)
     */

}

/**
 * Firebase implementation of [CartRepository].
 *
 * Stores cart items in a 'cart' sub-collection under the user's document using FirebaseAuth UID.
 *
 * @property firestore FirebaseFirestore instance
 * @property auth FirebaseAuth instance
 */
class FirebaseCartRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : CartRepository {

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    private fun getCartCollection() = firestore
        .collection(FirestoreCollections.USERS)
        .document(getCurrentUserId() ?: "")
        .collection(FirestoreCollections.CART)

    // Helper: Map Firestore document to ProductItem
    private fun DocumentSnapshot.toProductItem(): ProductItem? {
        return try {
            val data = this.data ?: return null

            /**
             * Safely parses a potential list field from a Firestore document.
             *
             * @param field The raw value from the Firestore document field.
             * @return A list of strings, or an empty list if the field is null or not a list.
             */
            fun parseListField(field: Any?): List<String> {
                return when (field) {
                    is List<*> -> field.mapNotNull { it as? String }
                    is String -> if (field.isBlank()) emptyList() else listOf(field)
                    else -> emptyList()
                }
            }

            ProductItem(
                id = this.id,
                userId = data["userId"] as? String ?: "",
                inStock = data["inStock"] as? Boolean ?: true,
                productName = data["productName"] as? String ?: "",
                price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                brand = data["brand"] as? String ?: "",
                category = data["category"] as? String ?: "",
                shipment = data["shipment"] as? String ?: "",
                sizes = parseListField(data["sizes"]),
                colors = parseListField(data["colors"]),
                description = data["description"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String ?: "",
                supportingImageUrls = (data["supportingImageUrls"] as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList(),
                rating = (data["rating"] as? Number)?.toFloat() ?: 0f,
                oldPrice = (data["oldPrice"] as? Number)?.toDouble() ?: 0.0,
                reviewCount = (data["reviewCount"] as? Number)?.toInt() ?: 0,
                quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                isFavorite = data["isFavorite"] as? Boolean ?: false,
                tags = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                isInCart = data["isInCart"] as? Boolean ?: false,
                createdAt = data["createdAt"] as? Timestamp,
                updatedAt = data["updatedAt"] as? Timestamp
            )
        } catch (e: Exception) {
            Log.e("ProductItem", "Error parsing document: ${e.message}")
            null
        }
    }

    override suspend fun addToCart(item: ProductItem): Result<Unit> = try {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")

        // CRITICAL: Validate productId before using it as document ID
        val documentId = when {
            item.id.isNotBlank() -> item.id
            else -> throw Exception("Invalid product: missing productId")
        }

        Log.d("FirebaseCartRepository", "Adding to cart with documentId: $documentId")

        // Check if item already exists using the documentId
        val existingDoc = getCartCollection()
            .document(documentId)
            .get()
            .await()

        if (existingDoc.exists()) {
            // Update quantity if item exists
            val currentQuantity = existingDoc.getLong("quantity")?.toInt() ?: 1
            getCartCollection()
                .document(documentId)
                .update(
                    mapOf(
                        "quantity" to (currentQuantity + 1),
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
        } else {
            // Add new item
            val cartItem = item.copy(
                userId = userId,
                id = item.id,
                isInCart = true,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            getCartCollection()
                .document(documentId)
                .set(cartItem)
                .await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseCartRepository", "Error adding to cart: ${e.message}", e)
        Result.failure(e)
    }

    override fun getCartItems(): Flow<List<ProductItem>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = getCartCollection()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(
                        "FirebaseCartRepository",
                        "Error listening to cart: ${error.message}",
                        error
                    )

                    // Check if it's the "failed to get documents from server" error
                    if (error.code == FirebaseFirestoreException.Code.UNAVAILABLE) {

                        // Don't close the flow! Keep it open to retry when network returns
                        Log.w(
                            "FirebaseCartRepository",
                            "Network issue - showing empty cart temporarily"
                        )

                        trySend(emptyList()) // Show empty cart when network fails

                    } else {
                        // For other errors, you might want to close
                        close(error)
                    }
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toProductItem()
                } ?: emptyList()

                trySend(items)
            }

        awaitClose { listener.remove() }
    }

    override fun getCartItemCount(): Flow<Int> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val listener = getCartCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CartRepository", "Error getting cart count: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun removeFromCart(productId: String): Result<Unit> = try {
        getCurrentUserId() ?: throw Exception("User not authenticated")

        // Validate productId
        if (productId.isBlank()) {
            throw Exception("Invalid productId: cannot be empty")
        }

        Log.d("FirebaseCartRepository", "Removing from cart: $productId")

        getCartCollection()
            .document(productId)
            .delete()
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseCartRepository", "Error removing from cart: ${e.message}", e)
        Result.failure(e)
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit> = try {
        getCurrentUserId() ?: throw Exception("User not authenticated")

        // CRITICAL: Validate productId
        if (productId.isBlank()) {
            throw Exception("Invalid productId: cannot be empty")
        }

        if (quantity <= 0) {
            getCartCollection()
                .document(productId)
                .delete()
                .await()
        } else {
            getCartCollection()
                .document(productId)
                .update(
                    mapOf(
                        "quantity" to quantity,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseCartRepository", "Error updating quantity: ${e.message}", e)
        Result.failure(e)
    }

    override suspend fun clearCart(): Result<Unit> = try {
        getCurrentUserId() ?: throw Exception("User not authenticated")
        val batch = firestore.batch()

        val documents = getCartCollection().get().await().documents
        documents.forEach { document ->
            batch.delete(getCartCollection().document(document.id))
        }

        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseCartRepository", "Error clearing cart: ${e.message}", e)
        Result.failure(e)
    }

    override fun isInCart(productId: String): Flow<Boolean> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(false)
            close()
            return@callbackFlow
        }

        // CRITICAL: Validate productId
        if (productId.isBlank()) {
            Log.w("FirebaseCartRepository", "isInCart called with empty productId")
            trySend(false)
            close()
            return@callbackFlow
        }

        val listener = getCartCollection()
            .document(productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(
                        "FirebaseCartRepository",
                        "Error checking if in cart: ${error.message}",
                        error
                    )
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.exists() == true)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun refreshCartItems(): Result<List<ProductItem>> = try {
        getCurrentUserId() ?: throw Exception("User not authenticated")

        val snapshot = getCartCollection()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get(Source.SERVER)
            .await()

        val items = snapshot.documents.mapNotNull { doc ->
            doc.toProductItem()
        }

        Result.success(items)
    } catch (e: Exception) {
        Log.e("FirebaseCartRepository", "Error refreshing cart: ${e.message}", e)
        Result.failure(e)
    }
}