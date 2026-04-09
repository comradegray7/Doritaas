package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.Order
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import jakarta.inject.Inject

import kotlinx.coroutines.tasks.await

/**
 * Sealed class providing structure for Order operation results.
 *
 * Typically wraps success or error states for UI consumption.
 */
sealed class OrderResult {
    /**
     * Represents a successful order operation containing the resulting data.
     */
    data class Success<T>(val data: T) : OrderResult()

    /**
     * Represents a failed order operation containing an error message.
     */
    data class Error(val message: String) : OrderResult()
}

/**
 * Interface definition for Order management operations.
 *
 * Handles creation, retrieval, updating, and deletion of customer orders.
 */
interface OrderRepository {
    /**
     * Retrieve all orders, sorted by creation date descending.
     * @return Result containing list of [Order]s
     */
    suspend fun getOrders(): Result<List<Order>>

    /**
     * Get a specific order by ID.
     * @param orderId ID of the order
     * @return Result containing [Order] or null
     */
    suspend fun getOrderById(orderId: String): Result<Order?>

    /**
     * Create a new order.
     * @param order The order data
     * @return Result containing the created [Order] with ID populated
     */
    suspend fun createOrder(order: Order): Result<Order>

    /**
     * Update the status of an order (e.g., "Shipped").
     * @param orderId ID of the order
     * @param status New status string
     * @return Result containing updated [Order]
     */
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Order>

    /**
     * Delete an order.
     * @param orderId ID of the order to delete
     * @return Result<Unit>
     */
    suspend fun deleteOrder(orderId: String): Result<Unit>

    /**
     * Search orders by ID, user ID, status, or product names.
     * @param query Search string
     * @return Result containing list of matching [Order]s
     */

    suspend fun searchOrders(query: String): Result<List<Order>>

    suspend fun searchOrdersById(query: String, userId: String): Result<List<Order>>

    /**
     * Filter orders by status.
     * @param status Status string (e.g., "Pending")
     * @return Result containing filtered list of [Order]s
     */
    suspend fun getOrdersByStatus(status: String): Result<List<Order>>

    suspend fun getOrdersByUser(userId: String): Result<List<Order>>
}

/**
 * Implementation of [OrderRepository] using Firestore.
 *
 * Operations target the "orders" collection.
 *
 * @property firestore FirebaseFirestore instance
 */
class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : OrderRepository {

    private val ordersCollection = firestore.collection(FirestoreCollections.ORDERS)

    companion object {
        private const val TAG = "OrderRepository"
    }

    override suspend fun getOrders(): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Order>()?.copy(id = doc.id)
            }
            Result.success(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching orders: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getOrderById(orderId: String): Result<Order?> {
        return try {
            val doc = ordersCollection.document(orderId).get().await()
            val order = doc.toObject<Order>()?.copy(id = doc.id)
            Result.success(order)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching order by ID: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun createOrder(order: Order): Result<Order> {
        return try {
            val orderId = ordersCollection.document().id
            val newOrder = order.copy(id = orderId)

            ordersCollection.document(orderId)
                .set(newOrder)
                .await()

            Result.success(newOrder)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating order: ${e.message}", e)
            Result.failure(Exception("Failed to create order: ${e.message}"))
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Order> {
        return try {
            val updates = mapOf(
                "status" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            ordersCollection.document(orderId)
                .update(updates)
                .await()

            // Fetch updated order
            val updatedDoc = ordersCollection.document(orderId).get().await()
            val updatedOrder = updatedDoc.toObject<Order>()?.copy(id = orderId)

            if (updatedOrder != null) {
                Result.success(updatedOrder)
            } else {
                Result.failure(Exception("Order not found after status update"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order status: ${e.message}", e)
            Result.failure(Exception("Failed to update order status: ${e.message}"))
        }
    }

    override suspend fun deleteOrder(orderId: String): Result<Unit> {
        return try {
            ordersCollection.document(orderId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting order: ${e.message}", e)
            Result.failure(Exception("Failed to delete order: ${e.message}"))
        }
    }

    override suspend fun getOrdersByUser(userId: String): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }

            Result.success(orders)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get orders for user $userId: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun searchOrders(query: String): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Order>()?.copy(id = doc.id)
            }.filter { order ->
                order.id.contains(query, ignoreCase = true) ||
                        order.userId.contains(query, ignoreCase = true) ||
                        order.status.contains(query, ignoreCase = true) ||
                        order.items.any { item ->
                            item.productName.contains(query, ignoreCase = true)
                        }
            }

            Result.success(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching orders: ${e.message}", e)
            Result.failure(Exception("Failed to search orders: ${e.message}"))
        }
    }

    override suspend fun searchOrdersById(query: String, userId: String): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Order>()?.copy(id = doc.id)
            }.filter { order ->
                order.id.contains(query, ignoreCase = true) ||
                        order.userId.contains(query, ignoreCase = true) ||
                        order.status.contains(query, ignoreCase = true) ||
                        order.items.any { item ->
                            item.productName.contains(query, ignoreCase = true)
                        }
            }

            Result.success(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching orders: ${e.message}", e)
            Result.failure(Exception("Failed to search orders: ${e.message}"))
        }
    }

    override suspend fun getOrdersByStatus(status: String): Result<List<Order>> {
        // Normalize input
        val queryStatus = status.trim()
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("status", queryStatus)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Order>()?.copy(id = doc.id)
            }

            // If Firestore query returned results, return them immediately
            if (orders.isNotEmpty()) {
                return Result.success(orders)
            }

            // If no results found with exact match, fall back to client-side filtering
            Log.w(TAG, "No orders returned for status='$queryStatus' from Firestore, falling back to client-side filtering")
            val allSnapshot = ordersCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val allOrders = allSnapshot.documents.mapNotNull { doc ->
                doc.toObject<Order>()?.copy(id = doc.id)
            }

            val filtered = allOrders.filter { order ->
                order.status.equals(queryStatus, ignoreCase = true)
            }

            Result.success(filtered)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching orders by status: ${e.message}", e)
            // Fallback: if Firestore query fails (often due to missing composite index),
            // fetch all orders and filter client-side to avoid breaking the UI.
            return try {
                Log.w(TAG, "Falling back to client-side filtering for status=$status")
                val allSnapshot = ordersCollection
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val allOrders = allSnapshot.documents.mapNotNull { doc ->
                    doc.toObject<Order>()?.copy(id = doc.id)
                }

                val filtered = allOrders.filter { order ->
                    order.status.equals(status, ignoreCase = true)
                }

                Result.success(filtered)
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Fallback filtering also failed: ${fallbackEx.message}", fallbackEx)
                Result.failure(fallbackEx)
            }
        }
    }

}