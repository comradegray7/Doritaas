package com.example.myapp.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.CarouselItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

/**
 * Interface for retrieving carousel image data.
 *
 * Defines the contract for fetching images used in the home screen carousel.
 */
interface CarouselRepository {
    /**
     * Fetch all carousel images from the data source.
     *
     * @return Result containing a list of [CarouselItem]s or an error
     */
    suspend fun createCarousel(carousel: CarouselItem): Result<CarouselItem>
    suspend fun updateCarousel(carousel: CarouselItem): Result<CarouselItem>
    suspend fun deleteCarousel(carouselId: String): Result<Unit>
    suspend fun getCarousels(): Result<List<CarouselItem>>

    suspend fun searchCarousels(query: String): Result<List<CarouselItem>>

}

/**
 * Implementation of [CarouselRepository] using Firebase Firestore.
 *
 * Retrieves carousel images from the "image_carousel" collection.
 *
 * @property firestore FirebaseFirestore instance
 */

class CarouselRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CarouselRepository {

    companion object {
        private const val CAROUSEL_COLLECTION = "image_carousel"

    }

    private val carouselsCollection = firestore.collection(FirestoreCollections.IMAGE_CAROUSEL)

    /**
     * Create a new carousel item.
     *
     * Checks if a carousel with the same title (case-insensitive) already exists.
     * If not, generates a new ID and saves the carousel to Firestore.
     *
     * @param carousel The carousel item to create
     * @return Result containing the created [CarouselItem] or an error
     */
    override suspend fun createCarousel(carousel: CarouselItem): Result<CarouselItem> {
        return try {
            // Check if carousel with same title already exists (case-insensitive)
            val existingSnapshot = carouselsCollection
                .get()
                .await()

            val isDuplicate = existingSnapshot.documents.any { doc ->
                val existingTitle = doc.getString("title") ?: ""
                val existingUrl = doc.getString("imageUrl") ?: ""

                existingUrl.equals(carousel.imageUrl, ignoreCase = true) &&
                        existingTitle.equals(carousel.title, ignoreCase = true)
            }

            if (isDuplicate) {
                Log.w(TAG, "Carousel already exists: ${carousel.title}")
                return Result.failure(Exception("Carousel '${carousel.title}' and '${carousel.imageUrl}' already exists"))
            }

            val carouselId = carouselsCollection.document().id
            val now = Timestamp.now()
            val newCarousel = carousel.copy(
                id = carouselId,
                redirectUrl = carousel.redirectUrl ?: "",
                title = carousel.title,
                description = carousel.description,
                imageUrl = carousel.imageUrl,
                createdAt = now,
                updatedAt = now
            )

            // Use a map to ensure field names match exactly
            val carouselMap = hashMapOf(
                "id" to newCarousel.id,
                "imageUrl" to newCarousel.imageUrl,
                "title" to newCarousel.title,
                "description" to newCarousel.description,
                "redirectUrl" to newCarousel.redirectUrl,
                "createdAt" to newCarousel.createdAt,
                "updatedAt" to newCarousel.updatedAt
            )

            carouselsCollection.document(carouselId)
                .set(carouselMap)
                .await()

            Log.d(TAG, "Carousel created successfully: $carouselId")
            Result.success(newCarousel)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create carousel", e)
            Result.failure(Exception("Failed to create carousel: ${e.message}"))
        }
    }

    /**
     * Retrieve all carousels.
     *
     * Fetches all documents from the 'carousels' collection, ordered by creation date.
     *
     * @return Result containing a list of [CarouselItem]s or an error
     */
    override suspend fun getCarousels(): Result<List<CarouselItem>> {
        return try {

            // Simple query without ordering to avoid index requirements
            val snapshot = firestore.collection(CAROUSEL_COLLECTION)
                .get() // No ordering, no limits initially
                .await()

            if (snapshot.isEmpty) {
                return Result.success(emptyList())
            }

            val carousel = mutableListOf<CarouselItem>()

            snapshot.documents.forEachIndexed { index, document ->
                try {

                    // Manual mapping to avoid Firestore serialization issues
                    val data = document.data
                    if (data != null) {
                        val carouselItem = CarouselItem(
                            id = document.getString("id") ?: document.id,
                            imageUrl = data["imageUrl"] as? String ?: "",
                            redirectUrl = data["redirectUrl"] as? String ?: "",
                            title = data["title"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                            updatedAt = data["updatedAt"] as? Timestamp ?: Timestamp.now()

                        )
                        carousel.add(carouselItem)
                    }
                } catch (_: Exception) {
                }
            }

            Result.success(carousel)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing carousel.
     *
     * Updates the carousel in Firestore and sets the updatedAt timestamp.
     *
     * @param carousel The carousel item to update
     * @return Result containing the updated [CarouselItem] or an error
     */
    override suspend fun updateCarousel(carousel: CarouselItem): Result<CarouselItem> {
        return try {
            val updatedCarousel = carousel.copy(
                id = carousel.id,
                updatedAt = Timestamp.now()
            )

            val carouselMap = hashMapOf(
                "id" to updatedCarousel.id,
                "imageUrl" to updatedCarousel.imageUrl,
                "title" to updatedCarousel.title,
                "description" to updatedCarousel.description,
                "redirectUrl" to updatedCarousel.redirectUrl,
                "createdAt" to updatedCarousel.createdAt,
                "updatedAt" to updatedCarousel.updatedAt
            )

            carouselsCollection.document(carousel.id)
                .set(carouselMap)
                .await()

            Log.d(TAG, "Carousel updated successfully: ${carousel.id}")
            Result.success(updatedCarousel)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update carousel", e)
            Result.failure(Exception("Failed to update carousel: ${e.message}"))
        }
    }

    /**
     * Delete a carousel.
     *
     * Removes the carousel document from Firestore.
     *
     * @param carouselId ID of the carousel to delete
     * @return Result indicating success or failure
     */
    override suspend fun deleteCarousel(carouselId: String): Result<Unit> {
        return try {
            val docSnapshot = carouselsCollection.document(carouselId).get().await()
            if (!docSnapshot.exists()) {
                return Result.failure(Exception("Carousel not found"))
            }

            carouselsCollection.document(carouselId)
                .delete()
                .await()

            Log.d(TAG, "Carousel deleted successfully: $carouselId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete carousel", e)
            Result.failure(Exception("Failed to delete carousel: ${e.message}"))
        }
    }

    /**
     * Search carousels by title or description.
     *
     * Performs a client-side filter on the fetched carousels.
     *
     * @param query Search query string
     * @return Result containing filtered list of [CarouselItem]s or an error
     */
    override suspend fun searchCarousels(query: String): Result<List<CarouselItem>> {
        return try {
            val snapshot = carouselsCollection
                .get()
                .await()

            val carousels = snapshot.documents.mapNotNull { doc ->
                try {
                    CarouselItem(
                        id = doc.getString("id") ?: doc.id,
                        imageUrl = doc.getString("imageUrl") ?: "",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        redirectUrl = doc.getString("redirectUrl"),
                        createdAt = doc.getTimestamp("createdAt"),
                        updatedAt = doc.getTimestamp("updatedAt")
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing document ${doc.id}", e)
                    null
                }
            }.filter { carousel ->
                carousel.title.contains(query, ignoreCase = true) ||
                        carousel.description.contains(query, ignoreCase = true)
            }

            Log.d(TAG, "Search found ${carousels.size} carousels for query: $query")
            Result.success(carousels)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search carousels", e)
            Result.failure(Exception("Failed to search carousels: ${e.message}"))
        }
    }
}