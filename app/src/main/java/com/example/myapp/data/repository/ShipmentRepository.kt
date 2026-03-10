package com.example.myapp.data.repository

import android.util.Log
import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.ShipmentItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

/**
 * Interface for managing Shipment Options.
 * 
 * Handles CRUD operations for shipping methods (e.g., specific carriers or services).
 */
interface ShipmentRepository {
    /**
     * Retrieve all shipping options.
     * @return Result containing list of [ShipmentItem]s
     */
    suspend fun getShipments(): Result<List<ShipmentItem>>

    /**
     * Create a new shipment option.
     * @param name Name of the carrier/service
     * @param deliveryMethod Description of method (e.g., "Express", "Standard")
     * @param price Cost of shipment
     * @return Result containing created [ShipmentItem]
     */
    suspend fun createShipment(name: String, deliveryMethod: String, price: Double): Result<ShipmentItem>

    /**
     * Update an existing shipment option.
     * @param shipmentId ID to update
     * @param name New name
     * @param deliveryMethod New method description
     * @param price New price
     * @return Result containing updated [ShipmentItem]
     */
    suspend fun updateShipment(shipmentId: String, name: String, deliveryMethod: String, price: Double): Result<ShipmentItem>

    /**
     * Delete a shipment option.
     * @param shipmentId ID to delete
     * @return Result<Unit>
     */
    suspend fun deleteShipment(shipmentId: String): Result<Unit>

    /**
     * Search shipment options by name or method.
     * @param query Search string
     * @return Result containing matching [ShipmentItem]s
     */
    suspend fun searchShipments(query: String): Result<List<ShipmentItem>>
}

/**
 * Implementation of [ShipmentRepository] using Firestore.
 * 
 * @property firestore FirebaseFirestore instance
 */
class ShipmentRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore
) :  ShipmentRepository  {

    private val shipmentsCollection = firestore.collection( FirestoreCollections.SHIPMENTS)

    companion object {
        private const val TAG = "ShipmentRepository"
    }

        // Create Shipment Option
        override suspend fun createShipment(
            name: String,
            deliveryMethod: String,
            price: Double
        ): Result<ShipmentItem> {
            return try {
                // Check if shipment with same name already exists (case-insensitive)
                val existingSnapshot = shipmentsCollection
                    .get()
                    .await()

                val isDuplicate = existingSnapshot.documents.any { doc ->
                    val existingName = doc.getString("name") ?: ""
                    existingName.equals(name, ignoreCase = true)
                }

                if (isDuplicate) {
                    Log.w(TAG, "Shipment option already exists: $name")
                    return Result.failure(Exception("Shipment option '$name' already exists"))
                }

                val shipmentId = shipmentsCollection.document().id
                val shipment = ShipmentItem(
                    id = shipmentId,
                    name = name,
                    deliveryMethod = deliveryMethod,
                    price = price
                )

                // Use a map to ensure field names match exactly
                val shipmentMap = hashMapOf(
                    "id" to shipment.id,
                    "name" to shipment.name,
                    "deliveryMethod" to shipment.deliveryMethod,
                    "price" to shipment.price
                )

                shipmentsCollection.document(shipmentId)
                    .set(shipmentMap)
                    .await()

                Log.d(TAG, "Shipment option created successfully: $shipmentId")
                Result.success(shipment)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create shipment option", e)
                Result.failure(Exception("Failed to create shipment option: ${e.message}"))
            }
        }

        // Get All Shipments
        override suspend fun getShipments(): Result<List<ShipmentItem>> {
            return try {
                val snapshot = shipmentsCollection
                    .orderBy("name", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val shipments = snapshot.documents.mapNotNull { doc ->
                    try {
                        ShipmentItem(
                            id = doc.getString("id") ?: doc.id,
                            name = doc.getString("name") ?: "",
                            deliveryMethod = doc.getString("deliveryMethod") ?: "",
                            price = doc.getDouble("price") ?: 0.0
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing document ${doc.id}", e)
                        null
                    }
                }

                Log.d(TAG, "Fetched ${shipments.size} shipment options")
                Result.success(shipments)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch shipment options", e)
                Result.failure(Exception("Failed to fetch shipment options: ${e.message}"))
            }
        }

    // Update Shipment
        override suspend fun updateShipment(
            shipmentId: String,
            name: String,
            deliveryMethod: String,
            price: Double
        ): Result<ShipmentItem> {
            return try {
                Log.d(TAG, "Updating shipment option: $shipmentId")

                // Check if document exists first
                val docSnapshot = shipmentsCollection.document(shipmentId).get().await()
                if (!docSnapshot.exists()) {
                    Log.e(TAG, "Document does not exist: $shipmentId")
                    return Result.failure(Exception("Shipment option not found"))
                }

                // Check if the new name already exists (excluding current document)
                val existingSnapshot = shipmentsCollection
                    .get()
                    .await()

                val isDuplicate = existingSnapshot.documents.any { doc ->
                    val docId = doc.getString("id") ?: doc.id
                    val existingName = doc.getString("name") ?: ""
                    docId != shipmentId && existingName.equals(name, ignoreCase = true)
                }

                if (isDuplicate) {
                    Log.w(TAG, "Shipment option name already exists: $name")
                    return Result.failure(Exception("Shipment option '$name' already exists"))
                }

                val updates = hashMapOf<String, Any>(
                    "name" to name,
                    "deliveryMethod" to deliveryMethod,
                    "price" to price
                )

                shipmentsCollection.document(shipmentId)
                    .update(updates)
                    .await()

                val updatedShipment = ShipmentItem(
                    id = shipmentId,
                    name = name,
                    deliveryMethod = deliveryMethod,
                    price = price
                )

                Log.d(TAG, "Shipment option updated successfully: $shipmentId")
                Result.success(updatedShipment)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update shipment option: $shipmentId", e)
                Result.failure(Exception("Failed to update shipment option: ${e.message}"))
            }
        }

        // Delete Shipment
        override suspend fun deleteShipment(shipmentId: String): Result<Unit> {
            return try {
                Log.d(TAG, "Deleting shipment option: $shipmentId")

                // Check if document exists first
                val docSnapshot = shipmentsCollection.document(shipmentId).get().await()
                if (!docSnapshot.exists()) {
                    Log.e(TAG, "Document does not exist: $shipmentId")
                    return Result.failure(Exception("Shipment option not found"))
                }

                shipmentsCollection.document(shipmentId)
                    .delete()
                    .await()

                Log.d(TAG, "Shipment option deleted successfully: $shipmentId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete shipment option: $shipmentId", e)
                Result.failure(Exception("Failed to delete shipment option: ${e.message}"))
            }
        }

        // Search Shipments
        override suspend fun searchShipments(query: String): Result<List<ShipmentItem>> {
            return try {
                val snapshot = shipmentsCollection.get().await()

                val shipments = snapshot.documents.mapNotNull { doc ->
                    try {
                        ShipmentItem(
                            id = doc.getString("id") ?: doc.id,
                            name = doc.getString("name") ?: "",
                            deliveryMethod = doc.getString("deliveryMethod") ?: "",
                            price = doc.getDouble("price") ?: 0.0
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing document ${doc.id}", e)
                        null
                    }
                }.filter { shipment ->
                    shipment.name.contains(query, ignoreCase = true) ||
                            shipment.deliveryMethod.contains(query, ignoreCase = true)
                }

                Result.success(shipments)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to search shipment options", e)
                Result.failure(Exception("Failed to search shipment options: ${e.message}"))
            }
        }

}