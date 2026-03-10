package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.ProductTag
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * TagRepository
 *
 * Interface defining the contract for managing product tags, including CRUD operations
 * and duplicate prevention logic.
 */
interface TagRepository {
    suspend fun getAllTags(): Result<List<ProductTag>>
    suspend fun createTag(tag: ProductTag): Result<Unit>
    suspend fun updateTag(tag: ProductTag): Result<Unit>
    suspend fun deleteTag(tagId: String): Result<Unit>
    suspend fun tagNameExists(name: String, excludeId: String? = null): Result<Boolean> // ✅ NEW
}

// ============================================
// 2. UPDATE TagRepositoryImpl with Duplicate Prevention
// ============================================

/**
 * TagRepositoryImpl
 *
 * Implementation of [TagRepository] using Firebase Firestore. 
 * Includes logic to prevent duplicate tag names and protect system tags from deletion.
 */
class TagRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore
) : TagRepository {

    private val tagsCollection = firestore.collection(FirestoreCollections.PRODUCT_TAGS)

    override suspend fun getAllTags(): Result<List<ProductTag>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = tagsCollection
                .orderBy("displayName", Query.Direction.ASCENDING)
                .get()
                .await()

            val tags = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ProductTag::class.java)?.copy(id = doc.id)
            }

            //   FIX: Remove duplicates based on name
            val uniqueTags = tags
                .groupBy { it.name }
                .map { (_, duplicates) ->
                    // Keep the first one or prefer system tags
                    duplicates.firstOrNull { it.isSystemTag } ?: duplicates.first()
                }

            Result.success(uniqueTags)
        } catch (e: Exception) {
            Log.e("TagRepository", "Error fetching tags", e)
            Result.failure(e)
        }
    }

    override suspend fun createTag(tag: ProductTag): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val normalizedName = tag.name.lowercase().replace(" ", "_")

            //   CHECK: Prevent duplicate tag names
            val existingTag = tagsCollection
                .whereEqualTo("name", normalizedName)
                .limit(1)
                .get()
                .await()

            if (!existingTag.isEmpty) {
                return@withContext Result.failure(
                    IllegalArgumentException("A tag with the name '${tag.displayName}' already exists")
                )
            }

            val docRef = tagsCollection.document()
            val tagWithId = tag.copy(
                id = docRef.id,
                name = normalizedName,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            docRef.set(tagWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TagRepository", "Error creating tag", e)
            Result.failure(e)
        }
    }

    override suspend fun updateTag(tag: ProductTag): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val normalizedName = tag.name.lowercase().replace(" ", "_")

            //  CHECK: Prevent updating to an existing tag name (excluding current tag)
            val existingTag = tagsCollection
                .whereEqualTo("name", normalizedName)
                .limit(2) // Get up to 2 to check if there's another one
                .get()
                .await()

            val conflictingTag = existingTag.documents.firstOrNull { doc ->
                doc.id != tag.id && doc.getString("name") == normalizedName
            }

            if (conflictingTag != null) {
                return@withContext Result.failure(
                    IllegalArgumentException("A tag with the name '${tag.displayName}' already exists")
                )
            }

            tagsCollection.document(tag.id)
                .update(
                    mapOf(
                        "name" to normalizedName,  
                        "displayName" to tag.displayName,
                        "description" to tag.description,
                        "color" to tag.color,
                        "category" to tag.category.name,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TagRepository", "Error updating tag", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTag(tagId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check if it's a system tag
            val tag = tagsCollection.document(tagId).get().await()
                .toObject(ProductTag::class.java)

            if (tag?.isSystemTag == true) {
                return@withContext Result.failure(
                    IllegalStateException("Cannot delete system tags")
                )
            }

            tagsCollection.document(tagId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TagRepository", "Error deleting tag", e)
            Result.failure(e)
        }
    }

    override suspend fun tagNameExists(name: String, excludeId: String?): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val normalizedName = name.lowercase().replace(" ", "_")

                val snapshot = tagsCollection
                    .whereEqualTo("name", normalizedName)
                    .get()
                    .await()

                // Check if any document matches (excluding the specified ID if provided)
                val exists = snapshot.documents.any { doc ->
                    excludeId == null || doc.id != excludeId
                }

                Result.success(exists)
            } catch (e: Exception) {
                Log.e("TagRepository", "Error checking tag name", e)
                Result.failure(e)
            }
        }
}
