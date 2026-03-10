package com.example.myapp.data.modules

import com.example.myapp.view.utils.CloudinaryHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
/**
 * CloudinaryModule
 *
 */
object CloudinaryModule {

    @Provides
            /**
             * provideCloudinaryHelper
             *
             */
    fun provideCloudinaryHelper(): CloudinaryHelper {
        return CloudinaryHelper()
    }
}

