package com.example.myapp.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.dataclass.DeliveryAddress
import com.example.myapp.data.dataclass.DeliveryAddressState
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.AuthRepository
import com.example.myapp.data.repository.DeliveryAddressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * DeliveryAddressViewModel - ViewModel for Managing User Shipping Addresses
 *
 * Handles CRUD operations for delivery addresses.
 * Integrates with authentication to ensure user-specific data security.
 *
 * ## Dependencies
 * - DeliveryAddressRepository: Data source for address operations
 * - AuthRepository: For retrieving the current authenticated user ID
 */
@HiltViewModel
class DeliveryAddressViewModel @Inject constructor(
    private val repository: DeliveryAddressRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DeliveryAddressState())
    val state: StateFlow<DeliveryAddressState> = _state.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    init {
        loadUserAddresses()
    }

    /**
     * Load addresses for the current user
     *
     * Fetches the user's saved addresses from the repository.
     * Updates state with the list or error message.
     * Requires the user to be signed in.
     */
    fun loadUserAddresses() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val userId = authRepository.getCurrentUserId()

            if (userId.isNullOrEmpty()) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Please sign in to manage addresses"
                )

                _snackBarData.emit(
                    SnackBarData(
                        message = "Please sign in to manage addresses",
                        isError = true
                    )
                )
                return@launch
            }

            try {
                val addresses = repository.getAddresses(userId)
                _state.value = _state.value.copy(
                    addresses = addresses,
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
                _snackBarData.emit(
                    SnackBarData(
                        message = e.message ?: "Failed to load addresses",
                        isError = true
                    )
                )
            }
        }
    }

    /**
     * Create a new delivery address
     *
     * Adds a new address to the user's profile.
     * Automatically associates the address with the logged-in user.
     *
     * @param address The address object to save
     */
    fun createAddress(address: DeliveryAddress) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val userId = authRepository.getCurrentUserId()
            if (userId.isNullOrEmpty()) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "User not authenticated"
                )
                _snackBarData.emit(
                    SnackBarData(
                        message = "Please sign in to add addresses",
                        isError = true
                    )
                )
                return@launch
            }

            try {
                val newAddress = repository.createAddress(address.copy(userId = userId))
                _state.value = _state.value.copy(
                    currentAddress = newAddress,
                    isLoading = false,
                    isSuccess = true
                )
                _snackBarData.emit(
                    SnackBarData(
                        message = "Address added successfully"
                    )
                )
                loadUserAddresses() // Refresh the list
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
                _snackBarData.emit(
                    SnackBarData(
                        message = e.message ?: "Failed to create address",
                        isError = true
                    )
                )
            }
        }
    }

    /**
     * Update an existing address
     *
     * Updates details of a specific address.
     *
     * @param address The updated address object containing the ID and new details
     */
    fun updateAddress(address: DeliveryAddress) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val updatedAddress = repository.updateAddress(address)
                _state.value = _state.value.copy(
                    currentAddress = updatedAddress,
                    isLoading = false,
                    isSuccess = true
                )
                _snackBarData.emit(
                    SnackBarData(
                        message = "Address updated successfully"
                    )
                )
                loadUserAddresses() // Refresh the list
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
                _snackBarData.emit(
                    SnackBarData(
                        message = e.message ?: "Failed to update address",
                        isError = true
                    )
                )
            }
        }
    }

    /**
     * Delete an address
     *
     * Permanently removes an address from the user's list.
     *
     * @param addressId Unique identifier of the address to delete
     */
    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val success = repository.deleteAddress(addressId)
                if (success) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            message = "Address deleted successfully"
                        )
                    )
                    loadUserAddresses() // Refresh the list
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
                _snackBarData.emit(
                    SnackBarData(
                        message = e.message ?: "Failed to delete address",
                        isError = true
                    )
                )
            }
        }
    }

    /**
     * Set default address
     *
     * Marks a specific address as the default for future orders.
     *
     * @param addressId ID of the address to make default
     */
    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId.isNullOrEmpty()) return@launch

            try {
                val success = repository.setDefaultAddress(userId, addressId)
                if (success) {
                    _snackBarData.emit(
                        SnackBarData(
                            message = "Default address updated"
                        )
                    )
                    loadUserAddresses() // Refresh the list
                }
            } catch (e: Exception) {
                _snackBarData.emit(
                    SnackBarData(
                        message = e.message ?: "Failed to set default address",
                        isError = true
                    )
                )
            }
        }
    }

}