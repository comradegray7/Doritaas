package com.example.myapp.data.model

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.AddToCartUseCase
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.CartRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * CartState - UI State for Shopping Cart
 *
 * Represents the current state of the user's shopping cart.
 *
 * @property isLoading Loading indicator for cart operations
 * @property cartItems List of products currently in the cart
 * @property currentCart Deprecated, use [cartItems] instead
 * @property error Error message if operations fail
 * @property isSuccess Success flag for add/remove/update operations
 */
data class CartState(
    val isLoading: Boolean = true,
    val cartItems: List<ProductItem> = emptyList(),
    val currentCart: ProductItem? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

/**
 * CartViewModel - ViewModel for Cart Management
 *
 * Handles all shopping cart operations including adding items, removing items,
 * updating quantities, and clearing the cart. Uses a real-time observer to
 * keep the cart badge count updated across the app.
 */
@HiltViewModel
class CartViewModel @Inject constructor(
    private val addToCartUseCase: AddToCartUseCase,
    private val cartRepository: CartRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {

    companion object {
        private const val TAG = "CartViewModel"
    }

    private val _cartItems = MutableStateFlow(0)
    val cartItems: StateFlow<Int> = _cartItems

    private var recentlyDeletedItem: ProductItem? = null
    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()
    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    private var observeCartJob: Job? = null

    init {
        startObservation()
    }

    //  encapsulate initialization
    private fun startObservation() {
        loadCartItems()
        observeCartItems()
    }

    // Load all favorites (consistent with SizeViewModel pattern)

    private fun observeCartItems() {
        observeCartJob?.cancel()
        observeCartJob = viewModelScope.launch {
            try {
                cartRepository.getCartItemCount().collect { count ->
                    Log.d("CartVM", "🛒 New cart count: $count")
                    _cartItems.value = count
                }
            } catch (e: Exception) {
                Log.e("CartVM", "Error observing cart: ${e.message}")
                _cartItems.value = 0
            }
        }
    }

    /**
     * Clear all items from the cart
     *
     * Removes all products from the user's cart.
     * Updates [cartState] and shows a snackbar on completion.
     */
    fun clearCart() {
        viewModelScope.launch {
            _cartState.value = _cartState.value.copy(
                isLoading = true,
                error = null
            )

            cartRepository.clearCart().fold(
                onSuccess = {
                    Log.d(TAG, "Cart cleared")
                    _cartState.value = _cartState.value.copy(
                        isLoading = false,
                        cartItems = emptyList(),
                        error = null
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            "Shopping Cart cleared",
                            duration = SnackbarDuration.Long
                        )
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to clear cart: ${exception.message}")
                    _cartState.value = _cartState.value.copy(
                        isLoading = false,

                        error = exception.message
                    )

                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to clear cart",
                            "Error",
                            duration = SnackbarDuration.Long
                        )
                    )
                }
            )
        }
    }

    /**
     * Load all cart items
     *
     * Refreshes the cart item list from the repository.
     * Updates [cartState] with the result.
     */
    fun loadCartItems() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _cartState.update {
                it.copy(
                    isLoading = false,
                    cartItems = emptyList(),
                    error = null // Ensure error is null if just logged out
                )
            }
            return
        }

        viewModelScope.launch {
            _cartState.value = _cartState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // Force refresh from repository
                cartRepository.refreshCartItems().fold(
                    onSuccess = { items ->
                        Log.d("CartViewModel", "Cart items loaded: ${items.size}")
                        // Items will automatically update via Flow
                        _cartState.value = _cartState.value.copy(
                            isLoading = false,
                            cartItems = items,
                            error = null
                        )
                    },

                    onFailure = { exception ->
                        Log.e("CartViewModel", "Failed to load cart: ${exception.message}")
                        _cartState.value = _cartState.value.copy(
                            isLoading = false,
                            cartItems = emptyList(),
                            error = null
                        )

                        _snackBarData.emit(
                            SnackBarData(
                                message = exception.message ?: "Failed to load cart",
                                isError = true,
                                duration = SnackbarDuration.Short
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error loading cart", e)
                _cartState.value = _cartState.value.copy(
                    isLoading = false,
                    error = "error: ${e.message}"
                    )
            }
        }
    }

    /**
     * getCartStatus - Checks if a product is in the cart (Flow)
     *
     * @param productId The unique ID of the product
     */
    fun getCartStatus(productId: String): Flow<Boolean> {
        return cartRepository.isInCart(productId)
    }

    /**
     * isInCart - Checks if a product is in the cart (Flow)
     *
     * @param productId The unique ID of the product
     */
    fun isInCart(productId: String): Flow<Boolean> {
        return if (productId.isBlank()) {
            flowOf(false)
        } else {
            cartRepository.isInCart(productId)
        }
    }

    /**
     * Undo the last removal action
     *
     * Restores [recentlyDeletedItem] to the favorites list.
     */
    private fun undoRemoveFromCart() {
        viewModelScope.launch {
            recentlyDeletedItem?.let { item ->
                _cartState.value = _cartState.value.copy(
                    isLoading = true,
                    error = null
                )

                cartRepository.addToCart(item).fold(
                    onSuccess = {
                        _cartState.value = _cartState.value.copy(
                            isLoading = false,
                            error = null
                        )

                        _snackBarData.emit(
                            SnackBarData(
                                message = "${item.productName} restored to cart",
                                duration = SnackbarDuration.Short
                            )
                        )

                        loadCartItems() // Refresh list
                    },
                    onFailure = { exception ->
                        _cartState.value = _cartState.value.copy(
                            isLoading = false,
                            error = exception.message
                        )

                        _snackBarData.emit(
                            SnackBarData(
                                exception.message ?: "Failed to restore item",
                                "Error",
                                duration = SnackbarDuration.Long
                            )
                        )
                    }
                )
                recentlyDeletedItem = null
            }
        }
    }

    /**
     * Add a product to the cart
     *
     * Adds a single quantity of the specified product to the cart.
     * Uses [AddToCartUseCase] to handle business logic.
     *
     * @param product The product to add
     */
    fun addToCart(product: ProductItem) {
        viewModelScope.launch {
            _cartState.value = _cartState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            addToCartUseCase(product, quantity = 1).fold(
                onSuccess = {
                    Log.d(TAG, "Product added to cart: ${product.productName}")
                    _cartState.value = _cartState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            "Product added to Shopping Cart",
                            duration = SnackbarDuration.Long
                        )
                    )
                    loadCartItems() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to add to cart: ${exception.message}")
                    _cartState.value = _cartState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to add Product to Shopping Cart",
                            "Error",
                            duration = SnackbarDuration.Long
                        )
                    )
                }
            )
        }
    }

    /**
     * Remove item from cart
     *
     */
    fun removeFromCart(item: ProductItem) {
        viewModelScope.launch {
            recentlyDeletedItem = item
            _cartState.value = _cartState.value.copy(
                isLoading = true,
                error = null
            )

            cartRepository.removeFromCart(item.id).fold(
                onSuccess = {
                    _cartState.value = _cartState.value.copy(
                        isLoading = false,
                        error = null
                    )

                    _snackBarData.emit(
                        SnackBarData(
                            message = "${item.productName} item removed from cart",
                            actionLabel = "UNDO",
                            duration = SnackbarDuration.Long,
                            onActionClick = {
                                undoRemoveFromCart()
                            }
                        )
                    )

                    loadCartItems() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to remove from cart: ${exception.message}")
                    _cartState.value = _cartState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to remove item",
                            "Error",
                            duration = SnackbarDuration.Short
                        )
                    )
                }
            )
        }
    }

    /**
     * Update item quantity
     *
     * @param productId ID of the product
     * @param quantity New quantity
     */
    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            _cartState.value = _cartState.value.copy(
                isLoading = true,
                error = null
            )

            cartRepository.updateQuantity(productId, quantity).fold(
                onSuccess = {
                    Log.d(TAG, "Quantity updated for product: $productId")
                    _cartState.value = _cartState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            "Cart updated 🎉",
                            duration = SnackbarDuration.Short
                        )
                    )
                    loadCartItems() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to update quantity: ${exception.message}")
                    _cartState.value = _cartState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to update cart",
                            "Error",
                            duration = SnackbarDuration.Short
                        )
                    )
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        observeCartJob?.cancel()
    }
}