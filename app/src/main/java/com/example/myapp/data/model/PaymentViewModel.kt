package com.example.myapp.data.model

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.CreateOrderUseCase
import com.example.myapp.data.PrimeBenefitsService
import com.example.myapp.data.ShippingOption
import com.example.myapp.data.api.PaymentSheetInitResponse
import com.example.myapp.data.api.ProductMetadata
import com.example.myapp.data.dataclass.AppliedBenefit
import com.example.myapp.data.dataclass.BenefitType
import com.example.myapp.data.dataclass.BenefitUsage
import com.example.myapp.data.dataclass.CheckoutSummary
import com.example.myapp.data.dataclass.DeliveryAddress
import com.example.myapp.data.dataclass.MembershipStatus
import com.example.myapp.data.dataclass.Order
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.AuthRepository
import com.example.myapp.data.repository.PaymentRepository
import com.example.myapp.data.repository.PrimeMembershipRepository
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PaymentState - UI State for Payment Processing
 * 
 * Represents the various stages of the payment workflow.
 */
sealed class PaymentState {
    /** Initial state, no payment processing active */
    object Idle : PaymentState()
    /** Determining payment configuration and fetching client secret */
    object FetchConfig : PaymentState()
    /** Loading payment sheet or processing transaction */
    object Loading : PaymentState()
    /** Payment flow initialized and ready for user interaction */
    object Ready : PaymentState()
    /** Payment successfully completed
     * @property message Success message to display
     */
    data class Success(val message: String = "Payment successful! 🎉") : PaymentState()
    /** Payment failed or error occurred
     * @property message Error description
     */
    data class Error(val message: String) : PaymentState()
}
@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val authRepository: AuthRepository,
    private val createOrderUseCase: CreateOrderUseCase,
    private val primeBenefitsService: PrimeBenefitsService,  
    private val primeMembershipRepository: PrimeMembershipRepository  
) : ViewModel() {

    private val _sheetConfig = MutableStateFlow<PaymentSheetInitResponse?>(null)
    val sheetConfig: StateFlow<PaymentSheetInitResponse?> = _sheetConfig.asStateFlow()

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    private val _currentAmount = MutableStateFlow(0)
    val currentAmount: StateFlow<Int> = _currentAmount.asStateFlow()

    private val _orderCreated = MutableStateFlow<Order?>(null)
    val orderCreated: StateFlow<Order?> = _orderCreated.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    //  Checkout summary with Prime benefits
    private val _checkoutSummary = MutableStateFlow<CheckoutSummary?>(null)
    val checkoutSummary: StateFlow<CheckoutSummary?> = _checkoutSummary.asStateFlow()

    private var pendingOrderData: PendingOrderData? = null

    /**
     * PendingOrderData - Captures checkout context between payment init and completion.
     *
     * Stores cart items, customer details, delivery address, Stripe payment intent
     * ID, and the computed [CheckoutSummary] so an order can be created after a
     * successful payment.
     */
    data class PendingOrderData(
        val items: List<ProductItem>,
        val customerEmail: String?,
        val customerName: String?,
        val deliveryAddress: DeliveryAddress?,
        val paymentIntentId: String,
        val checkoutSummary: CheckoutSummary? = null  
    )

    /**
     *  Calculate checkout summary with Prime benefits
     */
    suspend fun calculateCheckoutSummary(
        productItems: List<ProductItem>
    ): CheckoutSummary {
        val userId = authRepository.getCurrentUser()?.uid

        // Calculate base totals
        val subtotal = productItems.sumOf { it.price * it.quantity }
        var shippingCost = productItems.sumOf { it.shipmentCost }
        val tax = subtotal * 0.1 // 10% tax
        var discount = 0.0
        var primeDiscountAmount = 0.0
        var primeShippingSaved = shippingCost
        val appliedBenefits = mutableListOf<AppliedBenefit>()

        //  Check if user is Prime member
        val isPrimeMember = userId?.let {
            primeMembershipRepository.getMembership(it).getOrNull()?.status == MembershipStatus.ACTIVE
        } ?: false

        //   Get shipping options based on Prime status
        val shippingOptions = if (userId != null) {
            primeBenefitsService.getShippingOptions(userId)
        } else {
            listOf(ShippingOption("standard", "Standard Shipping", 5.99, "5-7 days", false))
        }

        if (isPrimeMember) {
            //   Apply Free Shipping
            if (shippingCost > 0) {
                primeShippingSaved = shippingCost
                shippingCost = 0.0

                appliedBenefits.add(
                    AppliedBenefit(
                        benefitType =  BenefitType.FREE_SHIPPING,
                        description = "Prime Free Shipping",
                        savingsAmount = primeShippingSaved
                    )
                )
            }

            //   Apply 20% Prime discount on eligible items
            val eligibleItems = productItems.filter { it.isPrimeEligible || it.tags.contains("prime_eligible") }
            val eligibleTotal = eligibleItems.sumOf { it.price * it.quantity }
            primeDiscountAmount = eligibleTotal * 0.20
            discount = primeDiscountAmount

            if (primeDiscountAmount > 0) {
                appliedBenefits.add(
                    AppliedBenefit(
                        benefitType = BenefitType.EXCLUSIVE_DISCOUNT,
                        description = "Prime Exclusive 20% Off",
                        savingsAmount = primeDiscountAmount
                    )
                )
            }

            //  Calculate 2x reward points
            val finalTotal = subtotal - discount + tax
            val rewardsPoints = finalTotal * 2.0 // 2x multiplier

            appliedBenefits.add(
                AppliedBenefit(
                    benefitType = BenefitType.PRIME_REWARDS,
                    description = "Earned ${rewardsPoints.toInt()} Prime Reward Points",
                    savingsAmount = 0.0 // Points are future value
                )
            )
        }

        val total = subtotal - discount + tax + shippingCost
        val primeTotalSavings = primeDiscountAmount + primeShippingSaved

        return CheckoutSummary(
            items = productItems,
            subtotal = subtotal,
            tax = tax,
            shippingCost = shippingCost,
            discount = discount,
            total = total,
            isPrimeOrder = isPrimeMember,
            primeDiscountAmount = primeDiscountAmount,
            primeShippingSaved = primeShippingSaved,
            primeTotalSavings = primeTotalSavings,
            primeRewardsPoints = if (isPrimeMember) total * 2.0 else 0.0,
            appliedBenefits = appliedBenefits,
            shippingOptions = shippingOptions,
            estimatedDelivery = if (isPrimeMember) "2-3 business days" else "5-7 business days"
        )
    }


    /**
     * Update the checkout summary details locally (without fetching Stripe config)
     */
    fun updateCheckoutSummary(productItems: List<ProductItem>) {
        viewModelScope.launch {
            val summary = calculateCheckoutSummary(productItems)
            _checkoutSummary.value = summary
            val totalAmountInCents = (summary.total * 100).toInt()
            _currentAmount.value = totalAmountInCents
        }
    }

    fun fetchConfiguration(
        customerEmail: String?,
        customerName: String?,
        productItems: List<ProductItem>,
        deliveryAddress: DeliveryAddress? = null
    ) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.FetchConfig

            //   Calculate summary with Prime benefits
            val summary = calculateCheckoutSummary(productItems)
            _checkoutSummary.value = summary

            val totalAmountInCents = (summary.total * 100).toInt()
            _currentAmount.value = totalAmountInCents

            val products = productItems.map { item ->
                ProductMetadata(
                    productId = item.id,
                    productName = item.productName,
                    quantity = item.quantity,
                    price = (item.price * 100).toInt().toDouble()
                )
            }

            val result = paymentRepository.fetchPaymentSheetConfig(
                amountInCents = totalAmountInCents,
                customerEmail = customerEmail,
                customerName = customerName,
                products = products,
                deliveryAddress = deliveryAddress
            )

            result.fold(
                onSuccess = { config ->
                    _sheetConfig.value = config
                    _paymentState.value = PaymentState.Ready

                    // Store pending order data with summary
                    pendingOrderData = PendingOrderData(
                        items = productItems,
                        customerEmail = customerEmail,
                        customerName = customerName,
                        deliveryAddress = deliveryAddress,
                        paymentIntentId = config.paymentIntent,
                        checkoutSummary = summary 
                    )
                },
                onFailure = { error ->
                    _paymentState.value = PaymentState.Error(error.message ?: "Unknown error")
                    _snackBarData.emit(
                        SnackBarData(
                            message = error.message ?: "Failed to initialize payment",
                            duration = SnackbarDuration.Long
                        )
                    )
                }
            )
        }
    }

    /**
     * Handle payment result from Stripe PaymentSheet
     */
    fun updatePaymentState(result: PaymentSheetResult) {
        viewModelScope.launch {
            when (result) {
                is PaymentSheetResult.Completed -> {
                    Log.d("PaymentViewModel", "Payment completed successfully")
                    _paymentState.value = PaymentState.Success(
                        "Your Payment has been completed successfully"
                    )

                    //  Create order with Prime benefits
                    createOrderAfterPayment()

                    _snackBarData.emit(
                        SnackBarData(
                            message = "Payment successful! Order created.",
                            duration = SnackbarDuration.Short
                        )
                    )
                }
                is PaymentSheetResult.Canceled -> {
                    Log.d("PaymentViewModel", "Payment canceled")
                    _paymentState.value = PaymentState.Ready
                    _snackBarData.emit(
                        SnackBarData(
                            message = "Payment canceled",
                            duration = SnackbarDuration.Short
                        )
                    )
                }
                is PaymentSheetResult.Failed -> {
                    Log.e("PaymentViewModel", "Payment failed: ${result.error.message}")
                    _paymentState.value = PaymentState.Error(
                        result.error.message ?: "Payment failed"
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            message = "Payment failed: ${result.error.message}",
                            duration = SnackbarDuration.Long
                        )
                    )
                }
            }
        }
    }

    /**
     *  Create order with Prime benefits
     */
    private suspend fun createOrderAfterPayment() {
        val orderData = pendingOrderData ?: run {
            Log.e("PaymentViewModel", "No pending order data found")
            return
        }

        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            Log.e("PaymentViewModel", "User not authenticated")
            _snackBarData.emit(
                SnackBarData(
                    message = "User not authenticated",
                    duration = SnackbarDuration.Long
                )
            )
            return
        }

        val summary = orderData.checkoutSummary ?: run {
            Log.e("PaymentViewModel", "No checkout summary found")
            return
        }

        // Save order to Firestore
        val result = createOrderUseCase(
            userId = currentUser.uid,
            userEmail = orderData.customerEmail ?: currentUser.email ?: "",
            userName = orderData.customerName ?: currentUser.displayName ?: "",
            items = orderData.items,
            paymentIntentId = orderData.paymentIntentId,
            shippingAddress = orderData.deliveryAddress,
            billingAddress = orderData.deliveryAddress,
            checkoutSummary = orderData.checkoutSummary
        )

        result.fold(
            onSuccess = { createdOrder ->
                Log.d("PaymentViewModel", "✅ Order created with Prime benefits: ${createdOrder.id}")
                Log.d("PaymentViewModel", "Prime savings: $${summary.primeTotalSavings}")

                // track Prime benefit usage
                if (summary.isPrimeOrder) {
                    trackPrimeBenefitUsage(createdOrder, summary)
                }

                _orderCreated.value = createdOrder
                pendingOrderData = null
            },
            onFailure = { error ->
                Log.e("PaymentViewModel", "❌ Failed to create order: ${error.message}")
                _snackBarData.emit(
                    SnackBarData(
                        message = "Payment successful but failed to create order. Please contact support.",
                        duration = SnackbarDuration.Long
                    )
                )
            }
        )
    }

    /**
     *  Track Prime benefit usage
     */
    private suspend fun trackPrimeBenefitUsage(order: Order, summary: CheckoutSummary) {
        val userId = authRepository.getCurrentUser()?.uid ?: return

        summary.appliedBenefits.forEach { benefit ->
            val usage = BenefitUsage(
                userId = userId,
                benefitType = benefit.benefitType,
                orderId = order.id,
                discountAmount = benefit.savingsAmount,
                usedAt = System.currentTimeMillis()
            )

            primeMembershipRepository.trackBenefitUsage(usage)
        }
    }

    /**
     * Initialize payment configuration for a raw amount.
     *
     * Convenience helper for flows where you know the final amount in cents
     * but do not need to pass full product metadata.
     *
     * @param amountInCents Total charge amount in Stripe cents.
     */
    fun fetchConfigurationWithAmount(amountInCents: Int) {
        _currentAmount.value = amountInCents
        fetchConfiguration(customerEmail = null, customerName = null, productItems = emptyList())
    }

    /**
     * Reset the payment state to idle.
     *
     * Useful after displaying a result so the UI can start a new payment flow.
     */
    fun refreshPaymentState() {
        _paymentState.value = PaymentState.Idle
    }
}