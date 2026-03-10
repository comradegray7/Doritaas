package com.example.myapp.data.modules

import android.content.Context
import android.speech.SpeechRecognizer
import com.example.myapp.data.RecentSearchDataStore
import com.example.myapp.data.SmartPopularSearchDataStore
import com.example.myapp.data.repository.ProductCrudRepository
import com.example.myapp.data.repository.SearchRepository
import com.example.myapp.data.repository.SearchRepositoryImpl
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for Search-related dependencies.
 *
 * Provides instances for:
 * - [SearchRepository] and associated DataStores logic
 * - ML Kit [ImageLabeler] for visual search
 * - Android [SpeechRecognizer] for voice search
 * - [RecentSearchDataStore] & [SmartPopularSearchDataStore]
 */
@Module
@InstallIn(SingletonComponent::class)
/**
 * SearchModule
 *
 */
object SearchModule {
    @Provides
    fun provideSearchRepository(
        @ApplicationContext context: Context,
        productRepository: ProductCrudRepository
    ): SearchRepository {
        return SearchRepositoryImpl(context, productRepository)
    }
    @Provides
    /**
     * provideImageLabeler
     *
     */
    fun provideImageLabeler(): ImageLabeler {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
        return ImageLabeling.getClient(options)
    }

    @Provides
    fun provideSpeechRecognizer(
        @ApplicationContext context: Context
    ): SpeechRecognizer? {
        return if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

        @Provides
        fun provideRecentSearchDataStore(
            @ApplicationContext context: Context
        ): RecentSearchDataStore {
            return RecentSearchDataStore(context)
        }

    @Provides
    fun provideSmartPopularSearchDataStore(
        @ApplicationContext context: Context
    ): SmartPopularSearchDataStore {
        return SmartPopularSearchDataStore(context)
    }
}