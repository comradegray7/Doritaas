package com.example.myapp.data

import android.util.Log
import com.example.myapp.data.dataclass.CheckoutSummary
import com.example.myapp.data.dataclass.DeliveryAddress
import com.example.myapp.data.dataclass.Order
import com.example.myapp.data.dataclass.OrderItem
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.repository.AuthRepository
import com.example.myapp.data.repository.CartRepository
import com.example.myapp.data.repository.FavoritesRepository
import com.example.myapp.data.repository.OrderRepository
import com.example.myapp.view.components.isValidEmail
import com.example.myapp.view.utils.primeUtils.calculateEstimatedDelivery
import com.example.myapp.view.utils.primeUtils.calculateFinalShippingCost
import com.example.myapp.view.utils.primeUtils.calculateOriginalShippingCost
import com.example.myapp.view.utils.primeUtils.calculatePrimeDiscount
import com.example.myapp.view.utils.primeUtils.calculateSubtotal
import com.example.myapp.view.utils.primeUtils.calculateTax
import com.example.myapp.view.utils.primeUtils.calculateTotalDiscount
import com.example.myapp.view.utils.primeUtils.calculateTotalPrimeDiscount
import com.example.myapp.view.utils.primeUtils.extractProductTags
import com.example.myapp.view.utils.primeUtils.logPrimeBenefits
import com.google.firebase.Timestamp
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * UseCase for adding a product to the shopping cart.
 *
 * Handles logic for:
 * - Validating product ID
 * - Checking if item already exists in cart (increments quantity if so)
 * - Adding new item if not present
 *
 * @property cartRepository Repository for cart operations.
 */
class AddToCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    /**
     * Executes the add to cart operation.
     *
     * @param product The product to add.
     * @param quantity The quantity to add (default 1).
     * @return Result<Boolean> indicating success.
     */
    suspend operator fun invoke(product: ProductItem, quantity: Int = 1): Result<Boolean> {
        return try {
            // Validate product has a valid ID
            val validatedProduct = validateAndFixProduct(product)

            Log.d("AddToCartUseCase", "Adding to cart: productId=${validatedProduct.id}, id=${validatedProduct.id}")

            val isInCart = cartRepository.isInCart(validatedProduct.id).first()

            if (isInCart) {
                // Increment quantity for existing item
                val currentCartItems = cartRepository.getCartItems().first()
                val existingItem = currentCartItems.find { it.id == validatedProduct.id }
                val currentQuantity = existingItem?.quantity ?: 0

                cartRepository.updateQuantity(
                    productId = validatedProduct.id,
                    quantity = currentQuantity + quantity
                ).getOrThrow()
            } else {
                // Add new item
                val cartItem = validatedProduct.copy(
                    quantity = quantity,
                    isInCart = true
                )
                cartRepository.addToCart(cartItem).getOrThrow()
            }
            Result.success(true)
        } catch (e: Exception) {
            Log.e("AddToCartUseCase", "Error adding to cart: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun validateAndFixProduct(product: ProductItem): ProductItem {
        // Ensure we have a valid productId
        val validProductId = when {
            product.id.isNotBlank() -> product.id
            else -> throw IllegalArgumentException("Product has no valid ID: $product")
        }

        return product.copy(
            id = validProductId
        )
    }
}

/**
 * UseCase for toggling a product's favorite status.
 *
 * If the product is favorited, it is removed.
 * If not, it is added.
 *
 * @property favoritesRepository Repository for favorites operations.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    /**
     * Executes the toggle favorite operation.
     *
     * @param product The product to toggle.
     * @return Result<Boolean>: true if added, false if removed.
     */
    suspend operator fun invoke(product: ProductItem): Result<Boolean> {
        return try {
            // Validate product has a valid ID
            val validatedProduct = validateAndFixProduct(product)

            Log.d("ToggleFavoriteUseCase", "Toggling favorite: productId=${validatedProduct.id}")

            val isFavorite = favoritesRepository.isFavorite(validatedProduct.id).first()

            if (isFavorite) {
                favoritesRepository.removeFromFavorites(validatedProduct.id).getOrThrow()
                Result.success(false)
            } else {
                val favoriteItem = validatedProduct.copy(
                    isFavorite = true,
                    quantity = 1
                )
                favoritesRepository.addToFavorites(favoriteItem).getOrThrow()
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e("ToggleFavoriteUseCase", "Error toggling favorite: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun validateAndFixProduct(product: ProductItem): ProductItem {
        val validProductId = when {
            product.id.isNotBlank() -> product.id
            else -> throw IllegalArgumentException("Product has no valid ID: $product")
        }

        return product.copy(
            id = validProductId
        )
    }
}

/**
 * UseCase for initiating a password reset email.
 *
 * @property authRepository Repository for authentication operations.
 */
class ForgotPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Sends a password reset email to the specified address.
     * Validates email format before sending.
     *
     * @param email The target email address.
     * @return Result<Unit> indicating success or failure.
     */
    suspend operator fun invoke(email: String): Result<Unit> {

        // Validate email format before making the call
        if (!isValidEmail(email)) {
            return Result.failure(Exception("Invalid email format"))
        }

        return authRepository.sendPasswordResetEmail(email)
    }
}

/**
 * UseCase for creating a new order.
 *
 * Orchestrates the order creation process:
 * - Converts ProductItems to OrderItems.
 * - Calculates totals (subtotal, shipping, tax).
 * - Creates Order object with payment and address details.
 * - Saves order to repository.
 * - Clears cart upon successful creation.
 *
 * @property orderRepository Repository for order operations.
 * @property cartRepository Repository for cart operations (to clear cart).
 */
class CreateOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository, // To clear cart after order
) {
    /**
     * Creates and submits a new order.
     */
    suspend operator fun invoke(
        userId: String,
        userEmail: String,
        userName: String,
        items: List<ProductItem>,
        paymentIntentId: String,
        shippingAddress: DeliveryAddress?,
        billingAddress: DeliveryAddress? = null,
        checkoutSummary: CheckoutSummary? = null
    ): Result<Order> {

        Log.d("CreateOrderUseCase", "Creating order for user: $userId")
        Log.d("CreateOrderUseCase", "Items count: ${items.size}")
        Log.d("CreateOrderUseCase", "Payment Intent: $paymentIntentId")
        Log.d("CreateOrderUseCase", "Prime order: ${checkoutSummary?.isPrimeOrder ?: false}")

        // Create order items with all product details
        val orderItems = items.map { product ->
            OrderItem(
                productId = product.id,
                productName = product.productName,
                quantity = product.quantity,
                price = product.price,
                isPrimeEligible = product.isPrimeEligible,
                primeDiscountApplied = calculatePrimeDiscount(product, checkoutSummary),
                imageUrl = product.imageUrl,
                selectedSize = product.selectedSize,
                selectedColor = product.selectedColor,
                selectedShipment = product.selectedShipment,
                brand = product.brand,
                category = product.category,
                tags = product.tags
            )
        }

        // Calculate totals
        val subtotal = calculateSubtotal(items)
        val originalShippingCost = calculateOriginalShippingCost(items)
        val shippingCost = calculateFinalShippingCost(items, checkoutSummary)
        val taxAmount = checkoutSummary?.tax ?: calculateTax(subtotal)
        val discount = calculateTotalDiscount(items, checkoutSummary)
        val totalAmount = subtotal + shippingCost + taxAmount - discount

        // Calculate Prime-specific values
        val isPrimeEligible = checkoutSummary?.isPrimeOrder ?: false
        val primeDiscountApplied = calculateTotalPrimeDiscount(items, checkoutSummary)

        // Create order object matching your data class
        val order = Order(
            id = UUID.randomUUID().toString(), // Generate unique ID
            userId = userId,
            userEmail = userEmail,
            userName = userName,
            items = orderItems,
            totalAmount = totalAmount,
            discount = discount,
            subtotal = subtotal,
            isPrimeEligible = isPrimeEligible,
            primeDiscountApplied = primeDiscountApplied,
            originalShippingCost = originalShippingCost,
            tags = extractProductTags(items),
            taxAmount = taxAmount,
            shippingCost = shippingCost,
            status = "confirmed", // Payment successful, so confirmed
            paymentStatus = "paid",
            paymentMethod = "stripe",
            paymentIntentId = paymentIntentId,
            shippingAddress = shippingAddress,
            billingAddress = billingAddress ?: shippingAddress,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            estimatedDelivery = calculateEstimatedDelivery(isPrimeEligible),
            trackingNumber = "", // Will be added when shipped
            notes = ""
        )

        // Create order in Firestore
        val result = orderRepository.createOrder(order)

        // Handle success and clear cart
        result.onSuccess { createdOrder ->
            Log.d("CreateOrderUseCase", "Order created successfully: ${createdOrder.id}")
            Log.d("CreateOrderUseCase", "Total: $${totalAmount}, Prime discount: $${primeDiscountApplied}")

            // Clear user's cart
            cartRepository.clearCart()

            // Log Prime benefits if applicable
            if (createdOrder.isPrimeEligible && createdOrder.primeDiscountApplied > 0) {
                logPrimeBenefits(createdOrder)
            }
        }

        result.onFailure { error ->
            Log.e("CreateOrderUseCase", "❌ Failed to create order: ${error.message}")
        }

        return result
     }
    }