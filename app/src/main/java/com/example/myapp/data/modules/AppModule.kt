package com.example.myapp.data.modules

import com.example.myapp.data.repository.AuthRepository
import com.example.myapp.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for providing app-wide singleton dependencies.
 *
 * Provides core instances like FirebaseFirestore and AuthRepository that are used
 * throughout the application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
/**
 * AppModule
 *
 */
object AppModule {

    @Provides
            /**
             * provideFirebaseFirestore
             *
             */
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
            /**
             * provideAuthRepository
             *
             *
             * @param firestore The firestore parameter
             */
    fun provideAuthRepository(firestore: FirebaseFirestore): AuthRepository {
        return AuthRepositoryImpl(firestore)
    }

    @Provides
            /**
             * provideFirebaseAuth
             *
             */
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

}
