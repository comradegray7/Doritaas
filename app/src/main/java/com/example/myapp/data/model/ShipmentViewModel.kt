package com.example.myapp.data.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.dataclass.ShipmentItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.repository.ShipmentRepository
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
 * ShipmentState - UI State for Shipping Options
 * 
 * Tracks the state of the shipment methods list and admin operations.
 * 
 * @property error Error message if operations fail
 */
data class ShipmentState(
    val isLoading: Boolean = false,
    val shipments: List<ShipmentItem> = emptyList(),
    val currentShipment: ShipmentItem? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

/**
 * ShipmentViewModel - ViewModel for Managing Shipping Methods
 * 
 * Handles CRUD operations for shipping options (e.g., Express, Standard).
 * Used by admin screens to configure logistics and user screens to select delivery.
 * 
 * ## Dependencies
 * - ShipmentRepository: Data source for shipment options
 */
@HiltViewModel
class ShipmentViewModel @Inject constructor(
    private val shipmentRepository: ShipmentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ShipmentViewModel"
    }

    private val _shipmentState = MutableStateFlow(ShipmentState())
    val shipmentState: StateFlow<ShipmentState> = _shipmentState.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData: SharedFlow<SnackBarData> = _snackBarData.asSharedFlow()

    init {
        loadShipments()
    }

    /**
     * Load all shipment options
     * 
     * Fetches the complete list of available shipping methods.
     * Updates [shipmentState] with the results.
     */
    fun loadShipments() {
        viewModelScope.launch {
            _shipmentState.value = _shipmentState.value.copy(
                isLoading = true,
                error = null
            )

            shipmentRepository.getShipments().fold(
                onSuccess = { shipments ->
                    Log.d(TAG, "Shipment options loaded: ${shipments.size}")
                    _shipmentState.value = _shipmentState.value.copy(
                        isLoading = false,
                        shipments = shipments,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to load shipment options: ${exception.message}")
                    _shipmentState.value = _shipmentState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to load shipment options",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Create a new shipment option
     * 
     * Adds a new shipping method to the system.
     * 
     * @param name Display name of the shipment method (e.g., "Express Delivery")
     * @param deliveryMethod Internal code or description of the method
     * @param price Cost of the shipping method
     */
    fun createShipment(name: String, deliveryMethod: String, price: Double) {
        if (name.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Name cannot be empty", "Error"))
            }
            return
        }

        if (deliveryMethod.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Delivery method cannot be empty", "Error"))
            }
            return
        }

        if (price < 0) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Price must be a positive number", "Error"))
            }
            return
        }

        viewModelScope.launch {
            _shipmentState.value = _shipmentState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            shipmentRepository.createShipment(name, deliveryMethod, price).fold(
                onSuccess = { shipment ->
                    Log.d(TAG, "Shipment option created: ${shipment.name}")
                    _shipmentState.value = _shipmentState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Shipment option '${shipment.name}' created successfully"))
                    loadShipments() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to create shipment option: ${exception.message}")
                    _shipmentState.value = _shipmentState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to create shipment option",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Update an existing shipment option
     * 
     * @param shipmentId ID of the shipment method to update
     * @param name New display name
     * @param deliveryMethod New delivery method details
     * @param price New shipping cost
     */
    fun updateShipment(shipmentId: String, name: String, deliveryMethod: String, price: Double) {
        if (name.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Name cannot be empty", "Error"))
            }
            return
        }

        if (deliveryMethod.isBlank()) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Delivery method cannot be empty", "Error"))
            }
            return
        }

        if (price < 0) {
            viewModelScope.launch {
                _snackBarData.emit(SnackBarData("Price must be a positive number", "Error"))
            }
            return
        }

        viewModelScope.launch {
            _shipmentState.value = _shipmentState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            shipmentRepository.updateShipment(shipmentId, name, deliveryMethod, price).fold(
                onSuccess = { shipment ->
                    Log.d(TAG, "Shipment option updated: ${shipment.name}")
                    _shipmentState.value = _shipmentState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Shipment option updated successfully"))
                    loadShipments() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to update shipment option: ${exception.message}")
                    _shipmentState.value = _shipmentState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to update shipment option",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Delete a shipment option
     * 
     * @param shipmentId ID of the shipment method to remove
     * @param name Name of the shipment method (for confirmation)
     */
    fun deleteShipment(shipmentId: String, name: String) {
        viewModelScope.launch {
            _shipmentState.value = _shipmentState.value.copy(
                isLoading = true,
                error = null
            )

            shipmentRepository.deleteShipment(shipmentId).fold(
                onSuccess = {
                    Log.d(TAG, "Shipment option deleted: $name")
                    _shipmentState.value = _shipmentState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    _snackBarData.emit(SnackBarData("Shipment option '$name' deleted successfully"))
                    loadShipments() // Refresh list
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to delete shipment option: ${exception.message}")
                    _shipmentState.value = _shipmentState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    _snackBarData.emit(
                        SnackBarData(
                            exception.message ?: "Failed to delete shipment option",
                            "Error"
                        )
                    )
                }
            )
        }
    }

    /**
     * Search shipment options
     * 
     * Filters the list of shipment methods based on a text query.
     * 
     * @param query Search keywords
     */
    fun searchShipments(query: String) {
        if (query.isBlank()) {
            loadShipments()
            return
        }

        viewModelScope.launch {
            _shipmentState.value = _shipmentState.value.copy(
                isLoading = true,
                error = null
            )

            shipmentRepository.searchShipments(query).fold(
                onSuccess = { shipments ->
                    Log.d(TAG, "Search results: ${shipments.size}")
                    _shipmentState.value = _shipmentState.value.copy(
                        isLoading = false,
                        shipments = shipments,
                        error = null
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to search shipment options: ${exception.message}")
                    _shipmentState.value = _shipmentState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

}