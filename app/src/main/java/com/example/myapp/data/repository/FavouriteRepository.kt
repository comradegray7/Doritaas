package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.ProductItem
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository interface for managing user's favorite products.
 *
 * Provides functionality to add, remove, and retrieve favorite items,
 * observe favorite status in real-time, and manage the favorites collection.
 */
interface FavoritesRepository {

    /**
     * Adds a product to the user's favorites.
     *
     * @param item The [ProductItem] to add.
     * @return [Result] indicating success or failure.
     */
    suspend fun addToFavorites(item: ProductItem): Result<Unit>

    /**
     * Removes a product from the user's favorites.
     *
     * @param productId The unique identifier of the product to remove.
     * @return [Result] indicating success or failure.
     */
    suspend fun removeFromFavorites(productId: String): Result<Unit>

    /**
     * Checks if a specific product is marked as favorite by the user.
     *
     * @param productId The unique identifier of the product.
     * @return A [Flow] emitting true if the product is in favorites, false otherwise.
     */
    fun isFavorite(productId: String): Flow<Boolean>

    /**
     * Forces a refresh of the favorites list from the server.
     *
     * @return [Result] containing the updated list of [ProductItem]s or an error.
     */
    suspend fun refreshFavorites(): Result<List<ProductItem>>

    /**
     * Removes all items from the user's favorites.
     *
     * @return [Result] indicating success or failure.
     */
    suspend fun clearAllFavorites(): Result<Unit>
}

/**
 * Firebase implementation of [FavoritesRepository].
 *
 * Uses Cloud Firestore to store favorites under a sub-collection "favorites"
 * within each user's document in the "users" collection.
 *
 */


/**
 * FirebaseFavoritesRepository
 *
 * Implementation of [FavoritesRepository] using Firebase Firestore.
 */
class FirebaseFavoritesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FavoritesRepository {

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    private fun getFavoritesCollection(): CollectionReference {
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("User must be authenticated to access favorites")

        return firestore
            .collection(FirestoreCollections.USERS)
            .document(userId)
            .collection(FirestoreCollections.FAVOURITES)
    }

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
                inStock = data["inStock"] as? Boolean ?: true,
                isFavorite = data["isFavorite"] as? Boolean ?: false,
                tags = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                createdAt = data["createdAt"] as? Timestamp,
                updatedAt = data["updatedAt"] as? Timestamp
            )
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Error parsing document: ${e.message}", e)
            null
        }
    }

    override suspend fun addToFavorites(item: ProductItem): Result<Unit> {
        return try {
            getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))

            // Validate productId
            if (item.id.isBlank()) {
                return Result.failure(Exception("Invalid product: missing productId"))
            }

            Log.d("FavoritesRepository", "Adding to favorites: ${item.id}")

            val favoriteItem = item.copy(
                isFavorite = true,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            getFavoritesCollection()
                .document(item.id)
                .set(favoriteItem)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Error adding to favorites: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun removeFromFavorites(productId: String): Result<Unit> {
        return try {
            getCurrentUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            // Validate productId
            if (productId.isBlank()) {
                return Result.failure(Exception("Invalid productId: cannot be empty"))
            }

            Log.d("FavoritesRepository", "Removing from favorites: $productId")

            getFavoritesCollection()
                .document(productId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Error removing from favorites: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun isFavorite(productId: String): Flow<Boolean> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            Log.w("FavoritesRepository", "User not authenticated, product not favorite")
            trySend(false)
            close()
            return@callbackFlow
        }

        // Validate productId
        if (productId.isBlank()) {
            Log.w("FavoritesRepository", "isFavorite called with empty productId")
            trySend(false)
            close()
            return@callbackFlow
        }

        val listener = getFavoritesCollection()
            .document(productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(
                        "FavoritesRepository",
                        "Error checking if favorite: ${error.message}",
                        error
                    )
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.exists() == true)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun refreshFavorites(): Result<List<ProductItem>> {
        return try {
            getCurrentUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            val snapshot = getFavoritesCollection()
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get(Source.SERVER)
                .await()

            val items = snapshot.documents.mapNotNull { doc ->
                doc.toProductItem()
            }

            Log.d("FavoritesRepository", "Refreshed ${items.size} favorites from server")
            Result.success(items)
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Error refreshing favorites: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun clearAllFavorites(): Result<Unit> {
        return try {
            getCurrentUserId()
                ?: return Result.failure(Exception("User not authenticated"))

            val documents = getFavoritesCollection()
                .get()
                .await()
                .documents

            if (documents.isEmpty()) {
                Log.d("FavoritesRepository", "No favorites to clear")
                return Result.success(Unit)
            }

            val batch = firestore.batch()
            documents.forEach { document ->
                batch.delete(document.reference)
            }

            batch.commit().await()

            Log.d("FavoritesRepository", "Cleared ${documents.size} favorites")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Error clearing favorites: ${e.message}", e)
            Result.failure(e)
        }
    }

}
