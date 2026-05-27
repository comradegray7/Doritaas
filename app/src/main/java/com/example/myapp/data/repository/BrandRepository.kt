package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.BrandItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import jakarta.inject.Inject

import kotlinx.coroutines.tasks.await

/**
 * Repository for managing Brand data in Firestore.
 *
 * Handles CRUD operations for product brands, including duplication checks
 * and search functionality.
 *
 * @property firestore FirebaseFirestore instance for database access
 */
class BrandRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val brandsCollection = firestore.collection(FirestoreCollections.BRANDS)

    companion object {
        private const val TAG = "BrandRepository"
    }

    /**
     * Create a new brand.
     *
     * Checks if a brand with the same name (case-insensitive) already exists.
     * If not, generates a new ID and saves the brand to Firestore.
     *
     * @return Result containing the created [BrandItem] or an error
     */
    /**
     * Create a new brand with strict case-insensitive duplication check.
     */
    suspend fun createBrand(brandName: String): Result<BrandItem> {
        return try {
            val trimmedName = brandName.trim()

            // 1. Efficient Query: Check if the exact name exists
            // Note: Firestore queries are case-sensitive by default.
            // To be 100% safe across all cases, we fetch and then verify.
            val existingSnapshot = brandsCollection
                .whereEqualTo("brandName", trimmedName)
                .get()
                .await()

            // 2. Double-check in memory (handles variations like "Nike" vs "nike" if
            // your DB has mixed data)
            if (!existingSnapshot.isEmpty) {
                return Result.failure(Exception("Brand '$trimmedName' already exists"))
            }

            // 3. Fallback: Check all if you suspect data was entered inconsistently before
            val allSnapshot = brandsCollection.get().await()
            val isDuplicate = allSnapshot.documents.any { doc ->
                val existing = doc.getString("brandName") ?: ""
                existing.equals(trimmedName, ignoreCase = true)
            }

            if (isDuplicate) {
                Log.w(TAG, "Brand already exists (case-insensitive check): $trimmedName")
                return Result.failure(Exception("Brand '$trimmedName' already exists"))
            }

            // 4. Create new document
            val brandId = brandsCollection.document().id
            val brand = BrandItem(
                id = brandId,
                brandName = trimmedName
            )

            val brandMap = hashMapOf(
                "id" to brand.id,
                "brandName" to brand.brandName
            )

            brandsCollection.document(brandId)
                .set(brandMap)
                .await()

            Log.d(TAG, "Brand created successfully: $brandId")
            Result.success(brand)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create brand", e)
            Result.failure(Exception("Failed to create brand: ${e.message}"))
        }
    }
    /**
     * Retrieve all brands.
     *
     * Fetches all documents from the 'brands' collection, ordered alphabetically by name.
     *
     * @return Result containing a list of [BrandItem]s or an error
     */
    suspend fun getBrands(): Result<List<BrandItem>> {
        return try {
            val snapshot = brandsCollection
                .orderBy("brandName", Query.Direction.ASCENDING)
                .get()
                .await()

            val brands = snapshot.documents.mapNotNull { doc ->
                try {
                    BrandItem(
                        id = doc.getString("id") ?: doc.id,
                        brandName = doc.getString("brandName") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing document ${doc.id}", e)
                    null
                }
            }

            Log.d(TAG, "Fetched ${brands.size} brands")
            Result.success(brands)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch brands", e)
            Result.failure(Exception("Failed to fetch brands: ${e.message}"))
        }
    }

    /**
     * Update an existing brand.
     *
     * Verifies the brand exists and ensures the new name doesn't conflict with another existing brand.
     *
     * @param brandId ID of the brand to update
     * @param brandName New name for the brand
     * @return Result containing the updated [BrandItem] or an error
     */
    suspend fun updateBrand(brandId: String, brandName: String): Result<BrandItem> {
        return try {
            Log.d(TAG, "Updating brand: $brandId with value: $brandName")

            // Check if document exists first
            val docSnapshot = brandsCollection.document(brandId).get().await()
            if (!docSnapshot.exists()) {
                Log.e(TAG, "Document does not exist: $brandId")
                return Result.failure(Exception("Brand not found"))
            }

            // Check if the new brand name already exists (excluding current document)
            val existingSnapshot = brandsCollection
                .get()
                .await()

            val isDuplicate = existingSnapshot.documents.any { doc ->
                val docId = doc.getString("id") ?: doc.id
                val existingBrand = doc.getString("brandName") ?: ""
                docId != brandId && existingBrand.equals(brandName, ignoreCase = true)
            }

            if (isDuplicate) {
                Log.w(TAG, "Brand already exists: $brandName")
                return Result.failure(Exception("Brand '$brandName' already exists"))
            }

            val updates = hashMapOf<String, Any>(
                "brandName" to brandName
            )

            brandsCollection.document(brandId)
                .update(updates)
                .await()

            val updatedBrand = BrandItem(
                id = brandId,
                brandName = brandName
            )

            Log.d(TAG, "Brand updated successfully: $brandId")
            Result.success(updatedBrand)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update brand: $brandId", e)
            Result.failure(Exception("Failed to update brand: ${e.message}"))
        }
    }

    /**
     * Delete a brand.
     *
     * Removes the brand document from Firestore.
     *
     * @param brandId ID of the brand to delete
     * @return Result<Unit> indicating success or failure
     */
    suspend fun deleteBrand(brandId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting brand: $brandId")

            // Check if document exists first
            val docSnapshot = brandsCollection.document(brandId).get().await()
            if (!docSnapshot.exists()) {
                Log.e(TAG, "Document does not exist: $brandId")
                return Result.failure(Exception("Brand not found"))
            }

            brandsCollection.document(brandId)
                .delete()
                .await()

            Log.d(TAG, "Brand deleted successfully: $brandId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete brand: $brandId", e)
            Result.failure(Exception("Failed to delete brand: ${e.message}"))
        }
    }

    /**
     * Search for brands by name.
     *
     * Performs a client-side filter on all brands.
     * Note: For large datasets, a server-side query should be considered.
     *
     * @param query Search string (case-insensitive)
     * @return Result containing a list of matching [BrandItem]s
     */
    suspend fun searchBrands(query: String): Result<List<BrandItem>> {
        return try {
            val snapshot = brandsCollection.get().await()

            val brands = snapshot.documents.mapNotNull { doc ->

                try {
                    BrandItem(
                        id = doc.getString("id") ?: doc.id,
                        brandName = doc.getString("brandName") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing document ${doc.id}", e)
                    null
                }
            }.filter { brand ->
                brand.brandName.contains(query, ignoreCase = true)
            }

            Result.success(brands)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search brands", e)
            Result.failure(Exception("Failed to search brands: ${e.message}"))
        }
    }
}

