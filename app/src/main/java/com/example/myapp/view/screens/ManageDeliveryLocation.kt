package com.example.myapp.view.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.DeliveryAddress
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.DeliveryAddressViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomDropDownMenuItem
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomFloatingPointButton
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomTextField
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.SignInRequiredDialog
import com.example.myapp.view.components.TopBarActionsShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomOutlinedButton
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CustomShape
import kotlinx.coroutines.delay

/**
 * AddressManagementScreen - Delivery address CRUD management
 *
 * Comprehensive address management screen allowing users to create, view, edit,
 * delete, and set default delivery addresses for orders.
 *
 * ## Features
 * - **Address List**: Display all saved delivery addresses
 * - **Add Address**: Create new delivery address with full form validation
 * - **Edit Address**: Modify existing address details
 * - **Delete Address**: Remove addresses with confirmation
 * - **Set Default**: Mark an address as default for checkout
 * - **Address Types**: Categorize as Home, Work, or Other
 * - **Validation**: Comprehensive field validation (email, phone, zip code, etc.)
 * - **Empty State**: Shows message when no addresses exist
 *
 * ## Address Fields
 * ### Personal Information
 * - Full Name (required)
 * - Phone Number (required, min 10 digits)
 * - Email (required, valid format)
 *
 * ### Address Information
 * - Address Line 1 (required)
 * - Address Line 2 (optional)
 * - City (required)
 * - State (required)
 * - Zip Code (required, min 3 characters)
 * - Country (required)
 *
 * ### Additional Options
 * - Address Type (Home/Work/Other)
 * - Set as Default toggle
 *
 * ## User Workflow
 * 1. View list of saved addresses
 * 2. Click FAB (+) to add new address
 * 3. Fill out address form with validation
 * 4. Save address
 * 5. Edit existing address by clicking edit icon
 * 6. Delete address with confirmation dialog
 * 7. Set default address using star icon
 *
 * ## Validation Rules
 * - Email: Must match valid email pattern
 * - Phone: Minimum 10 digits
 * - Zip Code: Minimum 3 characters
 * - All required fields must be filled
 *
 * @param onBackNavigation Callback for back navigation
 * @param viewModel ViewModel for address operations
 *
 * @see DeliveryAddressViewModel for address data operations
 * @see DeliveryAddress for address data structure
 */
@Composable
fun AddressManagementScreen(
    onBackNavigation: () -> Unit,
    onSignInClick: () -> Unit,
    viewModel: DeliveryAddressViewModel = hiltViewModel(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val state by viewModel.state.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    var showAuthDialog by remember { mutableStateOf(false) }
    val windowSizeClass = LocalWindowSizeConstant.current

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedAddress by remember { mutableStateOf<DeliveryAddress?>(null) }
    val networkState = rememberNetworkState(networkManager)

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    // Handle snack bar data
    LaunchedEffect(Unit) {
        viewModel.snackBarData.collect { snackBarData ->
            currentSnackBarData = snackBarData
            showSnackBar = true

            // Auto-dismiss after duration (unless indefinite)
            if (snackBarData.duration != SnackbarDuration.Indefinite) {
                delay(
                    when (snackBarData.duration) {
                        SnackbarDuration.Short -> 3000L
                        SnackbarDuration.Long -> 5000L
                        else -> 3000L
                    }
                )
                showSnackBar = false
            }
        }
    }

    CustomScaffoldContainer(
        title = R.string.delivery_address,
        onNavigateBack = { onBackNavigation() },
        actions = {
            if (state.isLoading) {
                TopBarActionsShimmer()
            } else {
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Refresh),
                    onClick = { viewModel.loadUserAddresses() },
                    contentDescription = "Refresh"
                )
            }
        },
        showBottomBar = false,
        verticalArrangement = Arrangement.Top,
        snackBarHostState = snackBarHostState,
        floatingBtnContent = {
            CustomFloatingPointButton(onClick = { showAddDialog = true })
        },
        content = {
            if (!networkState.hasInternet) {

                CustomSpacer()

                PaddedSection(
                    alignment = Alignment.CenterHorizontally,
                    content = {
                        NetworkStatusBanner(
                            networkState = networkState,
                        )
                    }
                )

                CustomSpacer()
            }

            currentSnackBarData?.let { snackBarData ->
                PaddedSection(
                    alignment = Alignment.CenterHorizontally,
                    content = {
                        FloatingCustomSnackBar(
                            snackBarData = snackBarData,
                            visible = showSnackBar,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(top = windowSizeClass.baseSize),
                            onDismiss = {
                                showSnackBar = false
                                currentSnackBarData = null
                            }
                        )
                    }
                )
            }

            when {
                state.isLoading -> {
                    PaddedSection(
                        content = {
                            CustomListCardShimmer()
                        }
                    )
                }

                state.addresses.isEmpty() -> {
                    CustomEmptyState(
                        titleStr = "No addresses yet",
                        subTitle = R.string.no_saved_addresses,
                        showBtn = false,
                        enableScroll = false,
                        leadingIcon = Icons.Filled.LocationOff,
                    )
                }

                else -> {
                    PaddedSection(
                        content = {
                            CustomLazyColumn {
                                items(state.addresses) { address ->
                                    AddressItemCard(
                                        address = address,
                                        onEdit = {
                                            selectedAddress = address
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            selectedAddress = address
                                            showDeleteDialog = true
                                        },
                                        onSetDefault = { viewModel.setDefaultAddress(address.id) }
                                    )
                                }

                                item {
                                    CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))
                                }
                            }
                        })
                }
            }

            // Dialogs
            if (showAddDialog) {
                AddAddressDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { address ->
                        viewModel.createAddress(address)
                        showAddDialog = false
                    }
                )
            }

            if (showEditDialog && selectedAddress != null) {
                EditAddressDialog(
                    address = selectedAddress!!,
                    onDismiss = {
                        showEditDialog = false
                        selectedAddress = null
                    },
                    onConfirm = { address ->
                        viewModel.updateAddress(address)
                        showEditDialog = false
                        selectedAddress = null
                    }
                )
            }

            if (showDeleteDialog && selectedAddress != null) {
                DeleteAddressDialog(
                    address = selectedAddress!!,
                    onDismiss = {
                        showDeleteDialog = false
                        selectedAddress = null
                    },
                    onConfirm = {
                        viewModel.deleteAddress(selectedAddress!!.id)
                        showDeleteDialog = false
                        selectedAddress = null
                    }
                )
            }
        }
    )

    if (showAuthDialog) {
        SignInRequiredDialog(
            onDismiss = { showAuthDialog = false },
            onSignInClick = {
                showAuthDialog = false
                onSignInClick()
            }
        )
    }
}

/**
 * AddAddressDialog - Dialog for creating a new delivery address
 *
 * Provides a form with validation for entering new address details.
 * Supports selecting address type (Home, Work, Other) and setting as default.
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when address is created, receives [DeliveryAddress] object
 *
 * @see DeliveryAddress for address data structure
 * @see CustomTextField for input fields
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressDialog(
    onDismiss: () -> Unit,
    onConfirm: (DeliveryAddress) -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var stateName by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }

    // Define the address types list
//   val addressType = listOf("Home", "Work", "Other") // Make sure this is inside the composable

    // Errors
    var fullNameError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var addressLine1Error by remember { mutableStateOf("") }
    var cityError by remember { mutableStateOf("") }
    var stateError by remember { mutableStateOf("") }
    var zipCodeError by remember { mutableStateOf("") }
    var countryError by remember { mutableStateOf("") }

    // Validation functions
    /**
     * validateFullName
     *
     */
    fun validateFullName() {
        fullNameError = if (fullName.isBlank()) "Full name is required" else ""
    }

    /**
     * validatePhone
     *
     */
    fun validatePhone() {
        phoneError = when {
            phoneNumber.isBlank() -> "Phone number is required"
            phoneNumber.length < 10 -> "Enter a valid phone number"
            else -> ""
        }
    }

    /**
     * validateEmail
     *
     */
    fun validateEmail() {
        emailError = when {
            email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email"
            else -> ""
        }
    }

    /**
     * validateAddressLine1
     *
     */
    fun validateAddressLine1() {
        addressLine1Error = if (addressLine1.isBlank()) "Address line 1 is required" else ""
    }

    /**
     * validateCity
     *
     */
    fun validateCity() {
        cityError = if (city.isBlank()) "City is required" else ""
    }

    /**
     * validateState
     *
     */
    fun validateState() {
        stateError = if (stateName.isBlank()) "State is required" else ""
    }

    /**
     * validateZipCode
     *
     */
    fun validateZipCode() {
        zipCodeError = when {
            zipCode.isBlank() -> "Zip code is required"
            zipCode.length < 3 -> "Enter a valid zip code"
            else -> ""
        }
    }

    /**
     * validateCountry
     *
     */
    fun validateCountry() {
        countryError = if (country.isBlank()) "Country is required" else ""
    }

    /**
     * validateForm
     *
     */
    fun validateForm(): Boolean {
        validateFullName()
        validatePhone()
        validateEmail()
        validateAddressLine1()
        validateCity()
        validateState()
        validateZipCode()
        validateCountry()

        return fullNameError.isEmpty() && phoneError.isEmpty() && emailError.isEmpty() &&
                addressLine1Error.isEmpty() && cityError.isEmpty() && stateError.isEmpty() &&
                zipCodeError.isEmpty() && countryError.isEmpty()
    }

    var showAddressTypeDropdown by remember { mutableStateOf(false) }
    var addressType by remember { mutableStateOf("Home") }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.AddLocation,
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Text(
                stringResource(R.string.add_delivery_address),
                style = windowSizeClass.titleTextStyle
            )
        },
        dismissButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.cancel,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            CustomTextButton(
                onClick = {
                    if (validateForm()) {
                        val address = DeliveryAddress(
                            fullName = fullName,
                            phoneNumber = phoneNumber,
                            email = email,
                            addressLine1 = addressLine1,
                            addressLine2 = addressLine2,
                            city = city,
                            state = stateName,
                            zipCode = zipCode,
                            country = country,
                            addressType = addressType,
                            isDefault = isDefault
                        )
                        onConfirm(address)
                    }
                },
                label = R.string.save
            )
        },
        text = {
            // Personal Information
            Text(
                text = stringResource(R.string.personal_information),
                style = windowSizeClass.titleTextStyle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = windowSizeClass.normalVerticalPadding)
            )

            CustomTextField(
                label = R.string.full_name,
                placeholder = R.string.enter_full_name,
                value = fullName,
                onValueChange = {
                    fullName = it
                    if (fullNameError.isNotEmpty()) validateFullName()
                },
                isError = fullNameError.isNotEmpty(),
                errorMessage = fullNameError
            )

            CustomTextField(
                label = R.string.phone_number,
                placeholder = R.string.enter_phone_number,
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    if (phoneError.isNotEmpty()) validatePhone()
                },
                isError = phoneError.isNotEmpty(),
                errorMessage = phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            CustomTextField(
                label = R.string.email,
                placeholder = R.string.enter_email,
                value = email,
                onValueChange = {
                    email = it
                    if (emailError.isNotEmpty()) validateEmail()
                },
                isError = emailError.isNotEmpty(),
                errorMessage = emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            CustomSpacer()

            // Address Information
            Text(
                text = stringResource(R.string.address_information),
                style = windowSizeClass.titleTextStyle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = windowSizeClass.basePadding)
            )

            CustomTextField(
                label = R.string.address_line_1,
                placeholder = R.string.enter_address_line_1,
                value = addressLine1,
                onValueChange = {
                    addressLine1 = it
                    if (addressLine1Error.isNotEmpty()) validateAddressLine1()
                },
                isError = addressLine1Error.isNotEmpty(),
                errorMessage = addressLine1Error
            )

            CustomTextField(
                label = R.string.address_line_2,
                placeholder = R.string.enter_address_line_2_optional,
                value = addressLine2,
                onValueChange = { addressLine2 = it }
            )

            CustomTextField(
                label = R.string.city,
                placeholder = R.string.enter_city,
                value = city,
                onValueChange = {
                    city = it
                    if (cityError.isNotEmpty()) validateCity()
                },
                isError = cityError.isNotEmpty(),
                errorMessage = cityError,
            )

            CustomTextField(
                label = R.string.state,
                placeholder = R.string.enter_state,
                value = stateName,
                onValueChange = {
                    stateName = it
                    if (stateError.isNotEmpty()) validateState()
                },
                isError = stateError.isNotEmpty(),
                errorMessage = stateError,
            )

            CustomTextField(
                label = R.string.zip_code,
                placeholder = R.string.enter_zip_code,
                value = zipCode,
                onValueChange = {
                    zipCode = it
                    if (zipCodeError.isNotEmpty()) validateZipCode()
                },
                isError = zipCodeError.isNotEmpty(),
                errorMessage = zipCodeError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            CustomTextField(
                label = R.string.country,
                placeholder = R.string.enter_country,
                value = country,
                onValueChange = {
                    country = it
                    if (countryError.isNotEmpty()) validateCountry()
                },
                isError = countryError.isNotEmpty(),
                errorMessage = countryError,
            )

            // Additional Options
            CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

            Text(
                text = stringResource(R.string.additional_options),
                style = windowSizeClass.titleTextStyle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = windowSizeClass.normalVerticalPadding)
            )

            CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

            // Address Type Selection
            ExposedDropdownMenuBox(
                expanded = showAddressTypeDropdown,
                onExpandedChange = { showAddressTypeDropdown = it }
            ) {
                CustomTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ),
                    value = addressType,
                    onValueChange = {},
                    label = R.string.address_type,
                    placeholder = R.string.address_type,
                    trailingIconContent = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAddressTypeDropdown)
                    },
                    readOnly = true
                )

                ExposedDropdownMenu(
                    expanded = showAddressTypeDropdown,
                    onDismissRequest = { showAddressTypeDropdown = false }
                ) {
                    listOf("Home", "Work", "Other").forEach { type ->
                        CustomDropDownMenuItem(
                            text = {
                                Text(
                                    type,
                                    style = windowSizeClass.bodyTextStyle
                                )
                            },
                            onClick = {
                                addressType = type
                                showAddressTypeDropdown = false
                            }
                        )
                    }
                }
            }

            // Default Address Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.set_as_default_address),
                    style = windowSizeClass.bodyTextStyle
                )

                Switch(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it }
                )
            }
        }
    )
}

/**
 * EditAddressDialog - Dialog for modifying an existing delivery address
 *
 * Pre-fills the form with existing address data for editing.
 * Validates inputs before saving changes.
 *
 * @param address The [DeliveryAddress] to edit
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when address is updated, receives modified [DeliveryAddress] object
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAddressDialog(
    address: DeliveryAddress,
    onDismiss: () -> Unit,
    onConfirm: (DeliveryAddress) -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    var fullName by remember { mutableStateOf(address.fullName) }
    var phoneNumber by remember { mutableStateOf(address.phoneNumber) }
    var email by remember { mutableStateOf(address.email) }
    var addressLine1 by remember { mutableStateOf(address.addressLine1) }
    var addressLine2 by remember { mutableStateOf(address.addressLine2) }
    var city by remember { mutableStateOf(address.city) }
    var stateName by remember { mutableStateOf(address.state) }
    var zipCode by remember { mutableStateOf(address.zipCode) }
    var country by remember { mutableStateOf(address.country) }
    var addressType by remember { mutableStateOf(address.addressType) }
    var isDefault by remember { mutableStateOf(address.isDefault) }

    // Errors (same validation logic as AddAddressDialog)
    var fullNameError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var addressLine1Error by remember { mutableStateOf("") }
    var cityError by remember { mutableStateOf("") }
    var stateError by remember { mutableStateOf("") }
    var zipCodeError by remember { mutableStateOf("") }
    var countryError by remember { mutableStateOf("") }

    val addressTypes = listOf("Home", "Work", "Other")

    // Validation functions (same as AddAddressDialog)
    /**
     * validateFullName
     *
     */
    fun validateFullName() {
        fullNameError = if (fullName.isBlank()) "Full name is required" else ""
    }

    /**
     * validatePhone
     *
     */
    fun validatePhone() {
        phoneError = when {
            phoneNumber.isBlank() -> "Phone number is required"
            phoneNumber.length < 10 -> "Enter a valid phone number"
            else -> ""
        }
    }

    /**
     * validateEmail
     *
     */
    fun validateEmail() {
        emailError = when {
            email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email"
            else -> ""
        }
    }

    /**
     * validateAddressLine1
     *
     */
    fun validateAddressLine1() {
        addressLine1Error = if (addressLine1.isBlank()) "Address line 1 is required" else ""
    }

    /**
     * validateCity
     *
     */
    fun validateCity() {
        cityError = if (city.isBlank()) "City is required" else ""
    }

    /**
     * validateState
     *
     */
    fun validateState() {
        stateError = if (stateName.isBlank()) "State is required" else ""
    }

    /**
     * validateZipCode
     *
     */
    fun validateZipCode() {
        zipCodeError = when {
            zipCode.isBlank() -> "Zip code is required"
            zipCode.length < 3 -> "Enter a valid zip code"
            else -> ""
        }
    }

    /**
     * validateCountry
     *
     */
    fun validateCountry() {
        countryError = if (country.isBlank()) "Country is required" else ""
    }

    /**
     * validateForm
     *
     */
    fun validateForm(): Boolean {
        validateFullName()
        validatePhone()
        validateEmail()
        validateAddressLine1()
        validateCity()
        validateState()
        validateZipCode()
        validateCountry()

        return fullNameError.isEmpty() && phoneError.isEmpty() && emailError.isEmpty() &&
                addressLine1Error.isEmpty() && cityError.isEmpty() && stateError.isEmpty() &&
                zipCodeError.isEmpty() && countryError.isEmpty()
    }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.EditLocation,
                contentDescription = "Edit location",
                iconSize = windowSizeClass.largeIconSize,
            )
        },
        title = {
            Text(
                stringResource(R.string.edit_delivery_address),
                style = windowSizeClass.titleTextStyle
            )
        },
        confirmButton = {
            CustomTextButton(
                onClick = {
                    if (validateForm()) {
                        val updatedAddress = address.copy(
                            fullName = fullName,
                            phoneNumber = phoneNumber,
                            email = email,
                            addressLine1 = addressLine1,
                            addressLine2 = addressLine2,
                            city = city,
                            state = stateName,
                            zipCode = zipCode,
                            country = country,
                            addressType = addressType,
                            isDefault = isDefault
                        )
                        onConfirm(updatedAddress)
                    }
                },
                label = R.string.update
            )
        },
        text = {
            // Personal Information
            Text(
                text = stringResource(R.string.personal_information),
                style = windowSizeClass.bodyTextStyle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = windowSizeClass.normalVerticalPadding)
            )

            CustomTextField(
                label = R.string.full_name,
                placeholder = R.string.enter_full_name,
                value = fullName,
                onValueChange = {
                    fullName = it
                    if (fullNameError.isNotEmpty()) validateFullName()
                },
                isError = fullNameError.isNotEmpty(),
                errorMessage = fullNameError
            )

            // (Rest of the form fields same as AddAddressDialog)
            CustomTextField(
                label = R.string.phone_number,
                placeholder = R.string.enter_phone_number,
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    if (phoneError.isNotEmpty()) validatePhone()
                },
                isError = phoneError.isNotEmpty(),
                errorMessage = phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            CustomTextField(
                label = R.string.email,
                placeholder = R.string.enter_email,
                value = email,
                onValueChange = {
                    email = it
                    if (emailError.isNotEmpty()) validateEmail()
                },
                isError = emailError.isNotEmpty(),
                errorMessage = emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            CustomSpacer()

            // Address Information
            Text(
                text = stringResource(R.string.address_information),
                style = windowSizeClass.titleTextStyle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = windowSizeClass.normalVerticalPadding)
            )

            CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

            CustomTextField(
                label = R.string.address_line_1,
                placeholder = R.string.enter_address_line_1,
                value = addressLine1,
                onValueChange = {
                    addressLine1 = it
                    if (addressLine1Error.isNotEmpty()) validateAddressLine1()
                },
                isError = addressLine1Error.isNotEmpty(),
                errorMessage = addressLine1Error
            )

            CustomTextField(
                label = R.string.address_line_2,
                placeholder = R.string.enter_address_line_2_optional,
                value = addressLine2,
                onValueChange = { addressLine2 = it }
            )

            CustomTextField(
                label = R.string.city,
                placeholder = R.string.enter_city,
                value = city,
                onValueChange = {
                    city = it
                    if (cityError.isNotEmpty()) validateCity()
                },
                isError = cityError.isNotEmpty(),
                errorMessage = cityError,
            )

            CustomTextField(
                label = R.string.state,
                placeholder = R.string.enter_state,
                value = stateName,
                onValueChange = {
                    stateName = it
                    if (stateError.isNotEmpty()) validateState()
                },
                isError = stateError.isNotEmpty(),
                errorMessage = stateError,
            )

            CustomTextField(
                label = R.string.zip_code,
                placeholder = R.string.enter_zip_code,
                value = zipCode,
                onValueChange = {
                    zipCode = it
                    if (zipCodeError.isNotEmpty()) validateZipCode()
                },
                isError = zipCodeError.isNotEmpty(),
                errorMessage = zipCodeError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            CustomTextField(
                label = R.string.country,
                placeholder = R.string.enter_country,
                value = country,
                onValueChange = {
                    country = it
                    if (countryError.isNotEmpty()) validateCountry()
                },
                isError = countryError.isNotEmpty(),
                errorMessage = countryError,
            )

            CustomSpacer()

            // Additional Options
            Text(
                text = stringResource(R.string.additional_options),
                style = windowSizeClass.titleTextStyle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = windowSizeClass.smallVerticalPadding)
            )

            // Address Type Selection
            var showAddressTypeDropdown by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.address_type),
                        style = windowSizeClass.labelTextStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = windowSizeClass.smallVerticalPadding)
                    )

                    CustomOutlinedButton(
                        onClick = { showAddressTypeDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        labelStr = address.addressType,
                        icon = ButtonIcon.Vector(Icons.Filled.ArrowDropDown)
                    )
                }

                // Dropdown menu
                DropdownMenu(
                    expanded = showAddressTypeDropdown,
                    onDismissRequest = { showAddressTypeDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    addressTypes.forEach { type ->
                        CustomDropDownMenuItem(
                            text = {
                                Text(
                                    text = type,
                                    style = windowSizeClass.bodyTextStyle,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            onClick = {
                                addressType = type
                                showAddressTypeDropdown = false
                            }
                        )
                    }
                }
            }

            // Default Address Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.set_as_default_address),
                    style = windowSizeClass.bodyTextStyle
                )
                Switch(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it }
                )
            }
        },
        dismissButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.cancel,
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}

/**
 * DeleteAddressDialog - address deletion confirmation dialog
 *
 * @param address The [DeliveryAddress] to delete
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when deletion is confirmed
 */
@Composable
fun DeleteAddressDialog(
    address: DeliveryAddress,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Warning,
                iconSize = windowSizeClass.largeIconSize,
                tint = colors.orange,
                contentDescription = "Delete location"
            )
        },
        title = {
            Text(
                stringResource(R.string.delete_address),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Text(
                "Are you sure you want to delete the address for ${address.fullName} at ${address.addressLine1}?",
                style = windowSizeClass.bodyTextStyle
            )
        },
        dismissButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.cancel,
                color = MaterialTheme.colorScheme.surface
            )
        },
        confirmButton = {
            CustomTextButton(
                onClick = onConfirm,
                label = R.string.delete,
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}

/**
 * AddressItemCard - Individual address display card
 *
 * Displays address details including name, full address, type, and phone.
 * Provides actions for editing, deleting, and setting as default.
 * Shows a "Default" badge if the address is the user's default.
 *
 * @param address The [DeliveryAddress] data to display
 * @param onEdit Callback when edit action is clicked
 * @param onDelete Callback when delete action is clicked
 * @param onSetDefault Callback when "set as default" action is clicked
 */
@Composable
fun AddressItemCard(
    address: DeliveryAddress,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Card(
        modifier = windowSizeConstant.adaptiveWidthModifier
            .padding(vertical = windowSizeConstant.normalVerticalPadding),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = windowSizeConstant.baseVerticalPadding)
    ) {
        Column(
            modifier = Modifier
                .padding(windowSizeConstant.basePadding)
        ) {

            // Header (Full name + Address Type + Default Tag)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = address.fullName,
                        style = windowSizeConstant.titleTextStyle,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

                    Text(
                        text = address.addressType,
                        style = windowSizeConstant.bodyTextStyle,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

                if (address.isDefault) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CustomShape.mediumShape()
                            )
                            .padding(
                                horizontal = windowSizeConstant.normalVerticalPadding,
                                vertical = windowSizeConstant.smallVerticalPadding
                            )
                    ) {
                        Text(
                            text = stringResource(R.string.default_address),
                            color = MaterialTheme.colorScheme.primary,
                            style = windowSizeConstant.bodyTextStyle
                        )
                    }
                }
            }

            CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

            // Address Lines
            Text(
                text = "${address.addressLine1}, ${address.addressLine2}".trimEnd(','),
                style = windowSizeConstant.bodyTextStyle

            )

            CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

            Text(
                text = "${address.city}, ${address.state}",
                style = windowSizeConstant.bodyTextStyle

            )

            CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

            Text(
                text = "${address.zipCode}, ${address.country}",
                style = windowSizeConstant.bodyTextStyle
            )

            CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

            Text(
                text = "Phone: ${address.phoneNumber}",
                style = windowSizeConstant.bodyTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CustomSpacer()

            // ACTION BUTTONS: Edit | Delete | Set Default
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.basePadding)
            ) {
                // Edit
                ButtonIconComposable(
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Edit),
                    onClick = { onEdit() },
                    contentDescription = "edit address"
                )

                // Delete
                ButtonIconComposable(
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Delete),
                    onClick = { onDelete() },
                    tint = MaterialTheme.colorScheme.error,
                    contentDescription = "delete address"
                )

                // Set Default (Only if not default already)
                if (!address.isDefault) {
                    ButtonIconComposable(
                        buttonIcon = ButtonIcon.Vector(Icons.Filled.Star),
                        onClick = { onSetDefault() },
                        contentDescription = "set default address"
                    )
                }
            }
        }
    }
}
