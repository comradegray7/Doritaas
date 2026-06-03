package com.example.myapp.data.authentication

import android.app.Application
import coil3.ImageLoader
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

/**
 * AuthApplication - Main application class with Hilt dependency injection
 *
 * Entry point for the Doritaas e-commerce application. Initializes Hilt for
 * dependency injection and sets up global application resources.
 *
 * ## Responsibilities
 * - Initialize Hilt dependency injection framework
 * - Inject global dependencies (ImageLoader, Firebase, etc.)
 * - Application lifecycle management
 * - Global configuration setup
 *
 * ## Dependencies
 * - **Hilt**: Dependency injection framework (@HiltAndroidApp)
 * - **Coil**: Image loading library (ImageLoader injection)
 *
 * ## Usage
 * Declared in AndroidManifest.xml:
 * ```xml
 * <application
 *     android:name=".data.authentication.AuthApplication"
 *     ...>
 * ```
 *
 * @see HiltAndroidApp for Hilt setup
 * @see ImageLoader for image loading configuration
 */
@HiltAndroidApp
class AuthApplication : Application(){
    /**
     * Coil ImageLoader for efficient image loading throughout the app
     * Injected by Hilt from ImageModule
     */
    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()

        // Initialize Cloudinary
        val config = hashMapOf(
            "cloud_name" to "yourcloudname",  // Required
            "api_key" to "yourapikey", // Optional: only for uploads
            "api_secret" to "yourapisecret"   // Optional: only for uploads
        )

        MediaManager.init(this, config)

    }
}
