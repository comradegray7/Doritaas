package com.example.myapp.data.dataclass

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

/**
 * DeliveryAddress - User shipping address data model
 *
 * Represents a saved delivery address for an authenticated user.
 *
 * @property id Unique address identifier
 * @property userId ID of the user who owns this address
 * @property fullName Recipient's full name
 * @property phoneNumber Contact phone number
 * @property email Contact email
 * @property addressLine1 Street address, P.O. box
 * @property addressLine2 Apartment, suite, unit (optional)
 * @property city City or town
 * @property state State, province, or region
 * @property zipCode Postal code
 * @property country Country name
 * @property isDefault Default shipping address flag
 * @property addressType Label for the address location (e.g., Home, Work)
 * @property createdAt Timestamp of creation
 * @property updatedAt Timestamp of last update
 */
@Parcelize
data class DeliveryAddress(
    val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = "",
    val isDefault: Boolean = false,
    val addressType: String = "Home", // Home, Work, Other
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) : Parcelable

// UI State
/**
 * DeliveryAddressState - UI State for address management
 *
 * Manages the state for the address list screen, handling loading,
 * error states, and the currently selected/edited address.
 *
 * @property addresses List of user's saved addresses
 * @property currentAddress The address currently being added or edited
 * @property isLoading Loading state for address operations
 * @property isSuccess Success flag for operations (add/edit/delete)
 * @property error Error message for failed operations
 * @property isBlank Validation error message for blank fields
 */
data class DeliveryAddressState(
    val addresses: List<DeliveryAddress> = emptyList(),
    val currentAddress: DeliveryAddress? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isBlank: String = ""
)