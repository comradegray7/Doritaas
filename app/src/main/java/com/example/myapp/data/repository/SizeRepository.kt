package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.SizeItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

/**
 * Interface for Size Configuration Management.
 * 
 * Handles CRUD operations for product size definitions (e.g., "S", "M", "L", "42", "44").
 */
interface SizeRepository {
    /**
     * Retrieve all defined sizes.
     * @return Result containing list of [SizeItem]s
     */
    suspend fun getSizes(): Result<List<SizeItem>>

    /**
     * Create a new size definition.
     * @param size Size label (e.g., "XL")
     * @return Result containing created [SizeItem]
     */
    suspend fun createSize(size: String): Result<SizeItem>

    /**
     * Update an existing size definition.
     * @param sizeId ID to update
     * @param size New size label
     * @return Result containing updated [SizeItem]
     */
    suspend fun updateSize(sizeId: String, size: String): Result<SizeItem>

    /**
     * Delete a size definition.
     * @param sizeId ID to delete
     * @return Result<Unit>
     */
    suspend fun deleteSize(sizeId: String): Result<Unit>

    /**
     * Search sizes by label.
     * @param query Search string
     * @return Result containing matching [SizeItem]s
     */
    suspend fun searchSizes(query: String): Result<List<SizeItem>>
}

/**
 * Implementation of [SizeRepository] using Firestore.
 * 
 * @property firestore FirebaseFirestore instance
 */
class SizeRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore
) :  SizeRepository  {


    private val sizesCollection = firestore.collection(FirestoreCollections.SIZES)

    companion object {
        private const val TAG = "SizeRepository"
    }

    override suspend fun createSize(size: String): Result<SizeItem> {
        return try {
            // Check if size already exists (case-insensitive)
            val existingSnapshot = sizesCollection
                .get()
                .await()

            val isDuplicate = existingSnapshot.documents.any { doc ->
                val existingSize = doc.getString("size") ?: ""
                existingSize.equals(size, ignoreCase = true)
            }

            if (isDuplicate) {
                Log.w(TAG, "Size already exists: $size")
                return Result.failure(Exception("Size '$size' already exists"))
            }

            val sizeId = sizesCollection.document().id
            val sizeItem = SizeItem(
                id = sizeId,
                size = size
            )

            // Use a map to ensure field names match exactly
            val sizeMap = hashMapOf(
                "id" to sizeItem.id,
                "size" to sizeItem.size
            )

            sizesCollection.document(sizeId)
                .set(sizeMap)
                .await()

            Log.d(TAG, "Size created successfully: $sizeId")
            Result.success(sizeItem)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create size", e)
            Result.failure(Exception("Failed to create size: ${e.message}"))
        }
    }

    override suspend fun updateSize(sizeId: String, size: String): Result<SizeItem> {
        return try {
            Log.d(TAG, "Updating size: $sizeId with value: $size")

            // Check if document exists first
            val docSnapshot = sizesCollection.document(sizeId).get().await()
            if (!docSnapshot.exists()) {
                Log.e(TAG, "Document does not exist: $sizeId")
                return Result.failure(Exception("Size not found"))
            }

            // Check if the new size name already exists (excluding current document)
            val existingSnapshot = sizesCollection
                .get()
                .await()

            val isDuplicate = existingSnapshot.documents.any { doc ->
                val docId = doc.getString("id") ?: doc.id
                val existingSize = doc.getString("size") ?: ""
                docId != sizeId && existingSize.equals(size, ignoreCase = true)
            }

            if (isDuplicate) {
                Log.w(TAG, "Size already exists: $size")
                return Result.failure(Exception("Size '$size' already exists"))
            }

            val updates = hashMapOf<String, Any>(
                "size" to size
            )

            sizesCollection.document(sizeId)
                .update(updates)
                .await()

            val updatedSize = SizeItem(
                id = sizeId,
                size = size
            )

            Log.d(TAG, "Size updated successfully: $sizeId")
            Result.success(updatedSize)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update size: $sizeId", e)
            Result.failure(Exception("Failed to update size: ${e.message}"))
        }
    }

    // Get All Sizes
    override suspend fun getSizes(): Result<List<SizeItem>> {
        return try {
            val snapshot = sizesCollection
                .orderBy("size", Query.Direction.ASCENDING)
                .get()
                .await()

            val sizes = snapshot.documents.mapNotNull { doc ->
                try {
                    SizeItem(
                        id = doc.getString("id") ?: doc.id,
                        size = doc.getString("size") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing document ${doc.id}", e)
                    null
                }
            }

            Log.d(TAG, "Fetched ${sizes.size} sizes")
            Result.success(sizes)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch sizes", e)
            Result.failure(Exception("Failed to fetch sizes: ${e.message}"))
        }
    }

    // Delete Size
    override suspend fun deleteSize(sizeId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting size: $sizeId")

            // Check if document exists first
            val docSnapshot = sizesCollection.document(sizeId).get().await()
            if (!docSnapshot.exists()) {
                Log.e(TAG, "Document does not exist: $sizeId")
                return Result.failure(Exception("Size not found"))
            }

            sizesCollection.document(sizeId)
                .delete()
                .await()

            Log.d(TAG, "Size deleted successfully: $sizeId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete size: $sizeId", e)
            Result.failure(Exception("Failed to delete size: ${e.message}"))
        }
    }

    // Search Sizes
    override suspend fun searchSizes(query: String): Result<List<SizeItem>> {
        return try {
            val snapshot = sizesCollection.get().await()

            val sizes = snapshot.documents.mapNotNull { doc ->
                try {
                    SizeItem(
                        id = doc.getString("id") ?: doc.id,
                        size = doc.getString("size") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing document ${doc.id}", e)
                    null
                }
            }.filter { sizeItem ->
                sizeItem.size.contains(query, ignoreCase = true)
            }

            Result.success(sizes)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search sizes", e)
            Result.failure(Exception("Failed to search sizes: ${e.message}"))
        }
    }
}
