package com.example.myapp.data.modules

import com.example.myapp.data.repository.DeliveryAddressRepository
import com.example.myapp.data.repository.DeliveryAddressRepositoryImpl
import com.example.myapp.data.repository.ProfileRepository
import com.example.myapp.data.repository.ProfileRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for Profile and Address related dependencies.
 *
 * Binds [ProfileRepository] and [DeliveryAddressRepository] to their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
/**
 * ProfileModule
 *
 */
abstract class ProfileModule {

    @Binds
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    abstract fun provideDeliveryAddressRepository(
           repository: DeliveryAddressRepositoryImpl
    ): DeliveryAddressRepository
}