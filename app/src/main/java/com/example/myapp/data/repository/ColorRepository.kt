package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.ColorItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

/**
 * Interface definition for Color operations.
 *
 * Manages product colors (name and hex code) including retrieval and CRUD operations.
 */
interface ColorRepository {
    /**
     * Create a new color.
     * @param colorName Name of the color
     * @param hexCode Hexadecimal color code (e.g., "#FF0000")
     * @return Result containing the created [ColorItem]
     */
    suspend fun createColor(colorName: String, hexCode: String): Result<ColorItem>

    /**
     * Update an existing color.
     * @param colorId ID of the color to update
     * @param colorName New name for the color
     * @param hexCode New hex code
     * @return Result containing the updated [ColorItem]
     */
    suspend fun updateColor(colorId: String, colorName: String, hexCode: String): Result<ColorItem>

    /**
     * Delete a color.
     * @param colorId ID of the color to delete
     * @return Result<Unit>
     */
    suspend fun deleteColor(colorId: String): Result<Unit>

    /**
     * Search for colors by name or hex code.
     * @param query Search string
     * @return Result containing list of matchng [ColorItem]s
     */
    suspend fun searchColors(query: String): Result<List<ColorItem>>

    /**
     * Retrieve all colors.
     * @return Result containing list of all [ColorItem]s
     */
    suspend fun getColors(): Result<List<ColorItem>>
}

/**
 * Implementation of [ColorRepository] using Firebase Firestore.
 *
 * @property firestore FirebaseFirestore instance
 */
class ColorRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) :  ColorRepository  {
    private val colorsCollection = firestore.collection(FirestoreCollections.COLORS)

    companion object {
        private const val TAG = "ColorRepository"
    }

    // Create Color
    override suspend fun createColor(colorName: String, hexCode: String): Result<ColorItem> {
        return try {
            // Check if color already exists (case-insensitive)
            val existingSnapshot = colorsCollection
                .get()
                .await()

            val isDuplicate = existingSnapshot.documents.any { doc ->
                val existingColor = doc.getString("name") ?: ""
                existingColor.equals(colorName, ignoreCase = true)

                val existingHexCodeColor = doc.getString("hexCode") ?: ""
                existingHexCodeColor.equals(hexCode, ignoreCase = true)
            }

            if (isDuplicate) {
                Log.w(TAG, "Color already exists: $colorName")
                return Result.failure(Exception("Color '$colorName' with hexCode '$hexCode' already exists"))
            }

            val colorId = colorsCollection.document().id

            val color = ColorItem(
                id = colorId,
                name = colorName,
                hexCode = hexCode
            )

            // Use a map to ensure field names match exactly
            val colorMap = hashMapOf(
                "id" to color.id,
                "name" to color.name,
                "hexCode" to color.hexCode
            )

            colorsCollection.document(colorId)
                .set(colorMap)
                .await()

            Log.d(TAG, "Color created successfully: $colorId")
            Result.success(color)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create color", e)
            Result.failure(Exception("Failed to create color: ${e.message}"))
        }
    }

    // Get All Colors
    override suspend fun getColors(): Result<List<ColorItem>> {
        return try {
            val snapshot = colorsCollection
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()

            val colors = snapshot.documents.mapNotNull { doc ->
                try {
                    ColorItem(
                        id = doc.getString("id") ?: doc.id,
                        name = doc.getString("name") ?: "",
                        hexCode = doc.getString("hexCode") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing document ${doc.id}", e)
                    null
                }
            }

            Log.d(TAG, "Fetched ${colors.size} colors")
            Result.success(colors)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch colors", e)
            Result.failure(Exception("Failed to fetch colors: ${e.message}"))
        }
    }

    // Update Color
    override suspend fun updateColor(
        colorId: String,
        colorName: String,
        hexCode: String
    ): Result<ColorItem> {
        return try {
            Log.d(TAG, "Updating color: $colorId with value: $colorName")

            // Check if document exists first
            val docSnapshot = colorsCollection.document(colorId).get().await()

            if (!docSnapshot.exists()) {
                Log.e(TAG, "Document does not exist: $colorId")
                return Result.failure(Exception("Color not found"))
            }

            // Check if the new color name already exists (excluding current document)
            val existingSnapshot = colorsCollection
                .get()
                .await()

            val isDuplicate = existingSnapshot.documents.any { doc ->
                val docId = doc.getString("id") ?: doc.id
                val existingColor = doc.getString("name") ?: ""
                docId != colorId && existingColor.equals(colorName, ignoreCase = true)

                val existingColorHexCode = doc.getString("hexCode") ?: ""
                docId != colorId && existingColorHexCode.equals(colorName, ignoreCase = true)
            }

            if (isDuplicate) {
                Log.w(TAG, "Color already exists: $colorName")
                Log.w(TAG, "Color already exists: $hexCode")

                return Result.failure(Exception("Color '$colorName' with hexCode '$hexCode' already exists"))
            }

            val updates = hashMapOf<String, Any>(
                "name" to colorName,
                "hexCode" to hexCode
            )

            colorsCollection.document(colorId)
                .update(updates)
                .await()

            val updatedColor = ColorItem(
                id = colorId,
                name = colorName,
                hexCode = hexCode
            )

            Log.d(TAG, "Color updated successfully: $colorId")
            Result.success(updatedColor)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update color: $colorId", e)
            Result.failure(Exception("Failed to update color: ${e.message}"))
        }
    }

    // Delete Color
    override suspend fun deleteColor(colorId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting color: $colorId")

            // Check if document exists first
            val docSnapshot = colorsCollection.document(colorId).get().await()
            if (!docSnapshot.exists()) {
                Log.e(TAG, "Document does not exist: $colorId")
                return Result.failure(Exception("Color not found"))
            }

            colorsCollection.document(colorId)
                .delete()
                .await()

            Log.d(TAG, "Color deleted successfully: $colorId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete color: $colorId", e)
            Result.failure(Exception("Failed to delete color: ${e.message}"))
        }
    }

    // Search Colors
    override suspend fun searchColors(query: String): Result<List<ColorItem>> {
        return try {
            val snapshot = colorsCollection.get().await()

            val colors = snapshot.documents.mapNotNull { doc ->
                try {
                    ColorItem(
                        id = doc.getString("id") ?: doc.id,
                        name = doc.getString("name") ?: "",
                        hexCode = doc.getString("hexCode") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing document ${doc.id}", e)
                    null
                }
            }.filter { color ->
                color.name.contains(query, ignoreCase = true) ||
                        color.hexCode.contains(query, ignoreCase = true)
            }

            Result.success(colors)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search colors", e)
            Result.failure(Exception("Failed to search colors: ${e.message}"))
        }
    }
}