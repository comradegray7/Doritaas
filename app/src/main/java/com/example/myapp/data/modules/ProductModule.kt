package com.example.myapp.data.modules

import com.example.myapp.data.repository.CarouselRepository
import com.example.myapp.data.repository.CarouselRepositoryImpl
import com.example.myapp.data.repository.CartRepository
import com.example.myapp.data.repository.CategoryRepository
import com.example.myapp.data.repository.CategoryRepositoryImpl
import com.example.myapp.data.repository.ColorRepository
import com.example.myapp.data.repository.ColorRepositoryImpl
import com.example.myapp.data.repository.DashboardRepository
import com.example.myapp.data.repository.DashboardRepositoryImpl
import com.example.myapp.data.repository.FavoritesRepository
import com.example.myapp.data.repository.FirebaseCartRepository
import com.example.myapp.data.repository.FirebaseFavoritesRepository
import com.example.myapp.data.repository.OrderRepository
import com.example.myapp.data.repository.OrderRepositoryImpl
import com.example.myapp.data.repository.PrimeMembershipRepository
import com.example.myapp.data.repository.PrimeMembershipRepositoryImpl
import com.example.myapp.data.repository.ProductCrudRepository
import com.example.myapp.data.repository.ProductCrudRepositoryImpl
import com.example.myapp.data.repository.PromotionRepository
import com.example.myapp.data.repository.PromotionRepositoryImpl
import com.example.myapp.data.repository.ShipmentRepository
import com.example.myapp.data.repository.ShipmentRepositoryImpl
import com.example.myapp.data.repository.SizeRepository
import com.example.myapp.data.repository.SizeRepositoryImpl
import com.example.myapp.data.repository.TagRepository
import com.example.myapp.data.repository.TagRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for binding Repository interfaces to their implementations.
 *
 * This adheres to the Dependency Inversion Principle.
 */
@Module
@InstallIn(SingletonComponent::class)
/**
 * ProductModule
 *
 */
abstract class ProductModule {

    @Binds
    abstract fun bindCarouselRepository(
        carouselRepositoryImpl: CarouselRepositoryImpl
    ): CarouselRepository

    @Binds
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    abstract fun bindCartRepository(
        firebaseCartRepository: FirebaseCartRepository
    ): CartRepository

    @Binds
    abstract fun bindFavoritesRepository(
        firebaseFavoritesRepository: FirebaseFavoritesRepository
    ): FavoritesRepository

    @Binds
    abstract fun bindShipmentRepository(
        firebaseShipmentRepository: ShipmentRepositoryImpl
    ): ShipmentRepository

    @Binds
    abstract fun bindSizeRepository(
        sizeRepository: SizeRepositoryImpl
    ): SizeRepository

    @Binds
    abstract fun bindColorRepository(
        colorRepository: ColorRepositoryImpl
    ): ColorRepository

    @Binds
    abstract fun bindProductCrudRepository(
        impl: ProductCrudRepositoryImpl
    ): ProductCrudRepository

    @Binds
    abstract fun providePromotionRepository(
        impl: PromotionRepositoryImpl
    ): PromotionRepository

    @Binds
    abstract fun provideOrderRepository(
        impl: OrderRepositoryImpl
    ): OrderRepository

    @Binds
    abstract fun bindDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository

    @Binds
    abstract fun bindPrimeMembershipRepository(
        impl: PrimeMembershipRepositoryImpl
    ): PrimeMembershipRepository

    @Binds
    abstract fun bindTagRepository(
        impl: TagRepositoryImpl
    ): TagRepository
}