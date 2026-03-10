package com.example.myapp.data.repository

import com.example.myapp.data.FirestoreCollections
import com.example.myapp.data.dataclass.DeliveryAddress
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

/**
 * Interface for Delivery Address data management.
 *
 * Handles CRUD operations for user delivery addresses stored in Firestore.
 */
interface DeliveryAddressRepository {
    /**
     * Retrieve all addresses for a specific user.
     * @param userId ID of the user
     * @return List of [DeliveryAddress] objects
     */
    suspend fun getAddresses(userId: String): List<DeliveryAddress>

    /**
     * Create a new delivery address.
     * @param address The address data
     * @return The created [DeliveryAddress]
     */
    suspend fun createAddress(address: DeliveryAddress): DeliveryAddress

    /**
     * Update an existing address.
     * @param address The updated address data
     * @return The updated [DeliveryAddress]
     */
    suspend fun updateAddress(address: DeliveryAddress): DeliveryAddress

    /**
     * Delete an address.
     * @param addressId ID of the address to delete
     * @return true if successful
     */
    suspend fun deleteAddress(addressId: String): Boolean

    /**
     * Set a specific address as the default one for the user.
     * Automatically unsets default status for other addresses.
     *
     * @param userId ID of the user
     * @param addressId ID of the address to make default
     * @return true if successful
     */
    suspend fun setDefaultAddress(userId: String, addressId: String): Boolean
}

/**
 * Implementation of [DeliveryAddressRepository] using Firestore.
 *
 * Stores addresses in a "deliveryAddresses" collection.
 *
 * @property firestore FirebaseFirestore instance
 * @property authRepository Repository to verify user identity
 */
class DeliveryAddressRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : DeliveryAddressRepository {

    private val addressesCollection = firestore.collection(FirestoreCollections.DELIVERY_ADDRESSES)

    override suspend fun getAddresses(userId: String): List<DeliveryAddress> {
        return try {
            // Debug: Check authentication
            val currentUserId = authRepository.getCurrentUserId()
            println("DEBUG: Current user ID: $currentUserId")
            println("DEBUG: Requested user ID: $userId")

            if (currentUserId != userId) {
                throw Exception("Unauthorized access - user mismatch")
            }

            println("DEBUG: Querying collection: deliveryAddresses")
            println("DEBUG: Query filter: userId == $userId")

            val snapshot = addressesCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            println("DEBUG: Query successful, found ${snapshot.documents.size} documents")

            snapshot.documents.mapNotNull { doc ->
                println("DEBUG: Processing document: ${doc.id}")
                mapToAddress(doc.data)
            }
        } catch (e: Exception) {
            println("DEBUG: Error in getAddresses: ${e.message}")
            e.printStackTrace()
            throw Exception("Failed to fetch addresses: ${e.message}")
        }
    }

    override suspend fun createAddress(address: DeliveryAddress): DeliveryAddress {
        return try {
            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId.isNullOrEmpty()) {
                throw Exception("User not authenticated")
            }

            val addressId = addressesCollection.document().id
            val newAddress = address.copy(
                id = addressId,
                userId = currentUserId
            )
            val addressMap = addressToMap(newAddress)

            addressesCollection.document(addressId)
                .set(addressMap)
                .await()

            newAddress
        } catch (e: Exception) {
            throw Exception("Failed to create address: ${e.message}")
        }
    }

    override suspend fun updateAddress(address: DeliveryAddress): DeliveryAddress {
        return try {
            val addressMap = addressToMap(address.copy(updatedAt = Timestamp.now()))
            addressesCollection.document(address.id)
                .set(addressMap)
                .await()
            address
        } catch (e: Exception) {
            throw Exception("Failed to update address: ${e.message}")
        }
    }

    override suspend fun deleteAddress(addressId: String): Boolean {
        return try {
            addressesCollection.document(addressId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            throw Exception("Failed to delete address: ${e.message}")
        }
    }

    override suspend fun setDefaultAddress(userId: String, addressId: String): Boolean {
        return try {
            // First, set all addresses to non-default
            val addressesSnapshot = addressesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val batch = firestore.batch()
            addressesSnapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isDefault", false)
            }
            batch.commit().await()

            // Then set the selected address as default
            addressesCollection.document(addressId)
                .update("isDefault", true, "updatedAt", Timestamp.now())
                .await()

            true
        } catch (e: Exception) {
            throw Exception("Failed to set default address: ${e.message}")
        }
    }

    private fun addressToMap(address: DeliveryAddress): Map<String, Any?> {
        return mapOf(
            "id" to address.id,
            "userId" to address.userId,
            "fullName" to address.fullName,
            "phoneNumber" to address.phoneNumber,
            "email" to address.email,
            "addressLine1" to address.addressLine1,
            "addressLine2" to address.addressLine2,
            "city" to address.city,
            "state" to address.state,
            "zipCode" to address.zipCode,
            "country" to address.country,
            "isDefault" to address.isDefault,
            "addressType" to address.addressType,
            "createdAt" to address.createdAt,
            "updatedAt" to address.updatedAt
        )
    }

    private fun mapToAddress(data: Map<String, Any?>?): DeliveryAddress? {
        if (data == null) return null
        return try {
            DeliveryAddress(
                id = data["id"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                fullName = data["fullName"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                email = data["email"] as? String ?: "",
                addressLine1 = data["addressLine1"] as? String ?: "",
                addressLine2 = data["addressLine2"] as? String ?: "",
                city = data["city"] as? String ?: "",
                state = data["state"] as? String ?: "",
                zipCode = data["zipCode"] as? String ?: "",
                country = data["country"] as? String ?: "",
                isDefault = data["isDefault"] as? Boolean ?: false,
                addressType = data["addressType"] as? String ?: "Home",
                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                updatedAt = data["updatedAt"] as? Timestamp ?: Timestamp.now()
            )
        } catch (_: Exception) {
            null
        }
    }
}