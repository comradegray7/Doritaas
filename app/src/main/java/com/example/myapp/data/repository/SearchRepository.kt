package com.example.myapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.myapp.data.dataclass.ProductItem
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.withContext

// SearchRepository.kt
/**
 * Interface definition for advanced search capabilities.
 * 
 * Supports text, voice, and image-based search, as well as managing recent search history.
 */
interface SearchRepository {
    /**
     * Process an image to extract potential search terms (labels).
     * @param imageUri URI of the image to analyze
     * @return Result containing list of detected labels/terms
     */
    suspend fun searchByImage(imageUri: Uri): Result<List<String>>

    /**
     * Find products matching a list of image labels.
     * @param labels List of keywords derived from image analysis
     * @return Result containing list of matching [ProductItem]s
     */
    suspend fun processImageLabels(labels: List<String>): Result<List<ProductItem>>

}

// SearchRepositoryImpl.kt
/**
 * Implementation of [SearchRepository] using ML Kit for image labeling and SharedPreferences for history.
 * 
 * @property context Application context
 * @property productRepository Repository for executing actual product searches
 */
class SearchRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val productRepository: ProductCrudRepository
) : SearchRepository {
    private val imageLabeler: ImageLabeler by lazy {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
        ImageLabeling.getClient(options)
    }

    override suspend fun searchByImage(imageUri: Uri): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                val labels = suspendCoroutine { continuation ->
                    imageLabeler.process(inputImage)
                        .addOnSuccessListener { labels ->
                            val labelTexts = labels.map { it.text }
                            continuation.resume(labelTexts)
                        }
                        .addOnFailureListener { e ->
                            continuation.resumeWithException(e)
                        }
                }
                Result.success(labels)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun processImageLabels(labels: List<String>): Result<List<ProductItem>> {
        val refinedQuery = labels.take(3).joinToString(" ")
        return productRepository.searchProducts(refinedQuery)
    }
}