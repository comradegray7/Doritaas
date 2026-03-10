package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.api.PaymentApi
import com.example.myapp.data.api.PaymentSheetInitResponse
import com.example.myapp.data.api.PaymentSheetRequest
import com.example.myapp.data.api.ProductMetadata
import com.example.myapp.data.dataclass.DeliveryAddress
import com.google.gson.Gson
import jakarta.inject.Inject

/**
 * Repository handling interactions with the Payment API (Stripe).
 *
 * Manages the initialization of the payment sheet and communication with the backend
 * to create PaymentIntents.
 *
 * @property paymentApi Retrofit API for payment endpoints
 */
class PaymentRepository @Inject constructor(
    private val paymentApi: PaymentApi,
) {
    companion object {
        private const val TAG = "PaymentRepository"
    }

    /**
     * Fetch configuration for Stripe PaymentSheet.
     *
     * Sends order details to the backend to create a PaymentIntent and retrieve
     * keys necessary for initializing the client-side PaymentSheet.
     *
     * @param amountInCents Total amount in lowest currency unit (e.g., cents for USD)
     * @param customerEmail Email of the paying customer
     * @param customerName Name of the paying customer
     * @param products List of product metadata for the backend record
     * @param deliveryAddress Address where items will be shipped
     * @return Result containing [PaymentSheetInitResponse] with PaymentIntent client secret
     */
    suspend fun fetchPaymentSheetConfig(
        amountInCents: Int,
        customerEmail: String?,
        customerName: String?,
        products: List<ProductMetadata>,
        deliveryAddress: DeliveryAddress?
    ): Result<PaymentSheetInitResponse> {
        return try {
            // Input validation
            if (amountInCents <= 0) {
                Log.e(TAG, "❌ Invalid amount: $amountInCents")
                return Result.failure(IllegalArgumentException("Amount must be greater than 0"))
            }

            Log.d(TAG, "🔄 Fetching payment config for amount: $${amountInCents / 100.0}")
            Log.d(TAG, "👤 Customer: $customerName ($customerEmail)")
            Log.d(TAG, "📦 Products count: ${products.size}")
            Log.d(
                TAG,
                "📍 Delivery address: ${deliveryAddress?.let { "${it.city}, ${it.country}" } ?: "Not provided"}")

            val request = PaymentSheetRequest(
                amount = amountInCents,
                customerEmail = customerEmail,
                customerName = customerName,
                products = products,
                deliveryAddress = deliveryAddress
            )

            Log.d(TAG, "📤 Request payload: ${Gson().toJson(request)}")

            val response = paymentApi.getPaymentSheetConfig(request)

            Log.d(TAG, "✅ Payment config fetched successfully")
            Log.d(TAG, "💰 PaymentIntent received: ${response.paymentIntent.take(20)}...")
            Log.d(TAG, "👤 Customer ID: ${response.customer}")

            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "No error body"
            Log.e(TAG, "❌ HTTP Error: ${e.code()} - ${e.message()}")
            Log.e(TAG, "Error response body: $errorBody")

            val errorMessage = when (e.code()) {
                400 -> "Invalid payment request. Please check your information."
                401 -> "Payment authentication failed."
                500 -> "Payment server error. Please try again."
                else -> "Server error: ${e.code()} - ${e.message()}"
            }
            Result.failure(Exception(errorMessage))
        } catch (_: java.net.UnknownHostException) {
            Log.e(TAG, "❌ Network error: Cannot reach server")
            Result.failure(Exception("Cannot connect to payment server. Please check your internet connection."))
        } catch (_: java.net.SocketTimeoutException) {
            Log.e(TAG, "❌ Timeout error: Request took too long")
            Result.failure(Exception("Payment server is taking too long to respond. Please try again."))
        } catch (e: javax.net.ssl.SSLHandshakeException) {
            Log.e(TAG, "❌ SSL Error: ${e.message}")
            Result.failure(Exception("Secure connection failed. Please check your date/time settings."))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error: ${e.message}", e)
            Result.failure(Exception("Failed to initialize payment: ${e.message}"))
        }
    }
}


