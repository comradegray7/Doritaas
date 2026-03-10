package com.example.myapp.data.api


import com.example.myapp.data.dataclass.DeliveryAddress
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * PaymentApi - Stripe payment backend API interface
 *
 * Retrofit interface for communicating with the backend payment server to process
 * Stripe payments. Handles payment sheet configuration and payment intent creation.
 *
 * ## Endpoints
 * - **POST /payment-sheet**: Initialize Stripe payment sheet with configuration
 *
 * ## Flow
 * 1. Client sends payment request with amount and customer details
 * 2. Backend creates Stripe PaymentIntent and Customer
 * 3. Backend returns payment sheet configuration
 * 4. Client presents Stripe Payment Sheet to user
 * 5. User completes payment in Stripe UI
 * 6. Backend confirms payment and creates order
 *
 * ## Security
 * - All sensitive operations happen on backend
 * - Client never handles payment credentials
 * - Uses Stripe's secure payment sheet
 *
 * @see com.example.myapp.data.model.PaymentViewModel for usage
 * @see com.example.myapp.view.screens.product.PaymentScreen for UI integration
 * @see com.example.myapp.data.modules.StripeModule for API configuration
 */
interface PaymentApi {
    /**
     * Get Stripe payment sheet configuration
     *
     * Requests payment sheet configuration from backend server. Backend creates
     * a Stripe PaymentIntent and returns necessary keys for client-side payment sheet.
     *
     * @param request Payment sheet request with amount, customer info, and order details
     * @return Payment sheet configuration including paymentIntent, ephemeralKey, customer ID, and publishableKey
     *
     * @throws retrofit2.HttpException if backend request fails
     * @throws java.io.IOException if network error occurs
     */
    @POST("/payment-sheet")
    suspend fun getPaymentSheetConfig(@Body request: PaymentSheetRequest): PaymentSheetInitResponse
}

/**
 * PaymentSheetInitResponse - Stripe payment sheet configuration response
 *
 * Contains all necessary configuration data from backend to initialize Stripe Payment Sheet.
 * Backend creates these values after creating a PaymentIntent and Customer in Stripe.
 *
 * ## Properties
 * - **paymentIntent**: Stripe PaymentIntent client secret for payment processing
 * - **ephemeralKey**: Temporary key for customer operations
 * - **customer**: Stripe Customer ID
 * - **publishableKey**: Stripe publishable API key for client-side operations
 *
 * ## Usage
 * ```kotlin
 * val response = paymentApi.getPaymentSheetConfig(request)
 * PaymentSheet.Configuration(
 *     merchantDisplayName = "Doritaas",
 *     customer = PaymentSheet.CustomerConfiguration(
 *         id = response.customer,
 *         ephemeralKeySecret = response.ephemeralKey
 *     )
 * )
 * ```
 *
 * @property paymentIntent Stripe PaymentIntent client secret
 * @property ephemeralKey Ephemeral key for customer operations
 * @property customer Stripe Customer ID
 * @property publishableKey Stripe publishable API key
 *
 * @see PaymentApi.getPaymentSheetConfig
 * @see com.example.myapp.data.model.PaymentViewModel for usage
 */
data class PaymentSheetInitResponse(
    val paymentIntent: String,
    val ephemeralKey: String,
    val customer: String,
    val publishableKey: String
)

/**
 * PaymentSheetRequest - Payment initialization request
 *
 * Request body sent to backend to initialize Stripe payment. Contains order details,
 * customer information, and delivery address for payment processing.
 *
 * ## Properties
 * - **amount**: Total payment amount in cents (e.g., $19.99 = 1999 cents)
 * - **customerEmail**: Customer's email for receipt and Stripe customer creation
 * - **customerName**: Customer's name for payment records
 * - **products**: List of products being purchased with metadata
 * - **deliveryAddress**: Shipping address for order fulfillment
 *
 * ## Amount Calculation
 * Amount should include:
 * - Product subtotal
 * - Shipping cost
 * - Tax (if applicable)
 * - Discounts (if applicable)
 *
 * Convert to cents using: `(totalAmount * 100).roundToInt()`
 *
 * ## Usage Example
 * ```kotlin
 * val request = PaymentSheetRequest(
 *     amount = 1999, // $19.99
 *     customerEmail = "customer@example.com",
 *     customerName = "John Doe",
 *     products = listOf(
 *         ProductMetadata(
 *             productId = "123",
 *             productName = "T-Shirt",
 *             quantity = 2,
 *             price = 9.99
 *         )
 *     ),
 *     deliveryAddress = address
 * )
 * ```
 *
 * @property amount Total payment amount in cents (smallest currency unit)
 * @property customerEmail Customer's email address
 * @property customerName Customer's full name
 * @property products List of products with metadata for order record
 * @property deliveryAddress Shipping address for delivery
 *
 * @see PaymentApi.getPaymentSheetConfig
 * @see ProductMetadata for product details
 * @see com.example.myapp.view.utils.toStripeCents for amount conversion
 */
data class PaymentSheetRequest(
    val amount: Int, // Amount in cents
    val customerEmail: String?,
    val customerName: String?,
    val products: List<ProductMetadata>,
    val deliveryAddress: DeliveryAddress?
)

/**
 * ProductMetadata - Product information for payment records
 *
 * Contains product details to be sent with payment request for order record keeping,
 * analytics, and inventory management on the backend.
 *
 * ## Properties
 * - **productId**: Unique product identifier
 * - **productName**: Display name of product
 * - **quantity**: Number of units purchased
 * - **price**: Unit price in dollars (not cents)
 * - **category**: Product category (optional)
 * - **selectedSize**: Chosen size variant (optional)
 * - **selectedColor**: Chosen color variant (optional)
 * - **selectedShipment**: Chosen shipping method (optional)
 * - **shipmentCost**: Shipping cost for this product (optional)
 * - **imageUrl**: Product image URL (optional)
 * - **brand**: Product brand name
 *
 * ## Usage
 * This metadata is sent to backend with payment request to:
 * - Create detailed order records
 * - Update inventory counts
 * - Track product analytics
 * - Generate order confirmation emails
 * - Fulfill orders with correct variants
 *
 * @property productId Unique product identifier (Firestore document ID)
 * @property productName Product display name
 * @property quantity Number of units
 * @property price Unit price in dollars
 * @property category Product category (optional)
 * @property selectedSize Size variant (e.g., "M", "L", "XL")
 * @property selectedColor Color variant (e.g., "Red", "Blue")
 * @property selectedShipment Shipping method (e.g., "Standard", "Express")
 * @property shipmentCost Shipping cost in dollars
 * @property imageUrl Product image URL
 * @property brand Product brand name
 *
 * @see PaymentSheetRequest for usage in payment
 * @see com.example.myapp.data.dataclass.ProductItem for full product model
 */

data class ProductMetadata(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val category: String? = null,
    val selectedSize: String? = null,
    val selectedColor: String? = null,
    val selectedShipment: String? = null,
    val shipmentCost: Double? = null,
    val imageUrl: String? = null,
    val brand: String = "",
)
