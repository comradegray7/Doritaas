package com.example.myapp.data.modules

import android.content.Context
import com.example.myapp.NetworkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


// AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
/**
 * NetworkModule
 *
 * Singleton object for network-related dependency injection.
 */
object NetworkModule {

    @Provides
            /**
             * provideContext
             *
             * Provides the application context for dependency injection.
             *
             * @param context The application context parameter
             */
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
            /**
             * provideNetworkManager
             *
             * Provides a singleton instance of NetworkManager using the application context.
             *
             * @param context The context parameter
             */
    fun provideNetworkManager(context: Context): NetworkManager {
        return NetworkManager(context)
    }
}