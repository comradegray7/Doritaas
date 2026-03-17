package com.example.myapp.view.utils

import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.get
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
/**
 * CloudinaryHelper
 */
class CloudinaryHelper @Inject constructor() {

    private val cloudName: String
        get() = MediaManager.get().cloudinary.config.cloudName

    /**
     * Generate optimized image URL for display
     */
    fun getImageUrl(
        publicId: String? = null,
        width: Int? = null,
        height: Int? = null,
        crop: String = "fill",
        quality: String = "auto",
        format: String = "auto"
    ): String {
        val transformations = buildList {
            if (width != null) add("w_$width")
            if (height != null) add("h_$height")
            add("c_$crop")
            add("q_$quality")
            add("f_$format")
        }.joinToString(",")

        return "https://res.cloudinary.com/$cloudName/image/upload/$transformations/$publicId"
    }

    /**
     * Get product list image (optimized for list views)
     **/
    /**
     * Upload image to Cloudinary
     */
    suspend fun uploadImage(
        fileUri: Uri,
        folder: String = "products",
        publicId: String? = null
    ): Result<String> = suspendCancellableCoroutine { continuation ->

        val options = mutableMapOf<String, Any>(
            "folder" to folder,
            "resource_type" to "image"
        )

        if (publicId != null) {
            options["public_id"] = publicId
        }

        val requestId = MediaManager.get().upload(fileUri)
            .options(options)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("Cloudinary", "Upload started: $requestId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progress = (bytes.toDouble() / totalBytes * 100).toInt()
                    Log.d("Cloudinary", "Upload progress: $progress%")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val uploadedPublicId = resultData["public_id"] as? String

                    if (uploadedPublicId != null) {
                        Log.d("Cloudinary", "Upload success: $uploadedPublicId")
                        continuation.resume(Result.success(uploadedPublicId))
                    } else {
                        continuation.resumeWithException(
                            Exception("Failed to get public_id from upload result")
                        )
                    }
                }

                override fun onError(
                    requestId: String,
                    error: ErrorInfo
                ) {
                    Log.d("Cloudinary Quickstart", "Upload failed")
                }

                override fun onReschedule(
                    requestId: String,
                    error: ErrorInfo
                ) {
                }
            })
            .dispatch()

        continuation.invokeOnCancellation {
            MediaManager.get().cancelRequest(requestId)
        }
    }
}