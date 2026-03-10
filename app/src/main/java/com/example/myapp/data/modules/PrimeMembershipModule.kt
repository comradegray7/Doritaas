package com.example.myapp.data.modules

import com.example.myapp.data.PrimeBenefitsService
import com.example.myapp.data.repository.PrimeMembershipRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
/**
 * PrimeMembershipModule
 *
 */
object PrimeMembershipModule {

    @Provides
    fun providePrimeBenefitsService(
        primeMembershipRepository: PrimeMembershipRepository,
        auth: FirebaseAuth
    ): PrimeBenefitsService {
        return PrimeBenefitsService(primeMembershipRepository, auth)
    }
}