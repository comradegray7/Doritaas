package com.example.myapp.view.admin.components

import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.CategoryItem
import com.example.myapp.data.dataclass.CategoryNode
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.ProductTag
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.dataclass.SupportingImageData
import com.example.myapp.data.model.BrandViewModel
import com.example.myapp.data.model.CategoryViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.data.model.SizeViewModel
import com.example.myapp.data.model.TagViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.admin.CustomSearchBar
import com.example.myapp.view.admin.filterCategoryTree
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomAssistChip
import com.example.myapp.view.components.CustomCircularProgressIndicator
import com.example.myapp.view.components.CustomDropDownMenuItem
import com.example.myapp.view.components.CustomHorizontalDivider
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomImageContainer
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomLazyRow
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomTextField
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.custom.buttons.CustomOutlinedButton
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CloudinaryHelper
import com.example.myapp.view.utils.isValidUrl
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * AddProductScreen - Admin interface for creating new products
 *
 * This screen provides a comprehensive form for administrators to add new products to the catalog.
 * It handles all product attributes including basic information, pricing, inventory, media, and status.
 *
 * ## Features
 * - **Form Validation**: Real-time validation with error messages for all required fields
 * - **Multi-Select Dropdowns**: Support for selecting multiple sizes and colors
 * - **Default Values**: Automatic fallback to default sizes (M, L) and colors (Black, White)
 * - **Image Management**: Support for main product image and multiple supporting images
 * - **Status Toggles**: Control product visibility (In Stock, Featured, Trending)
 * - **Responsive Feedback**: Loading states and success/error snackbar notifications
 *
 * ## Form Sections
 *
 * ### 1. Basic Information
 * - Product Name (required)
 * - Description (required, max 500 characters)
 *
 * ### 2. Pricing
 * - Current Price (required, must be > 0)
 * - Old Price (optional, must be higher than current price if provided)
 *
 * ### 3. Category & Brand
 * - Brand (required, dropdown selection)
 * - Category (required, dropdown selection)
 *
 * ### 4. Inventory
 * - Quantity (required, must be >= 0)
 * - Sizes (multi-select, defaults to M, L)
 * - Colors (multi-select, defaults to Black, White)
 *
 * ### 5. Media
 * - Main Image URL (required, validated)
 * - Supporting Images (optional, multiple URLs, validated)
 *
 * ### 6. Status
 * - In Stock (toggle)
 * - Featured Product (toggle)
 * - Trending Product (toggle)
 *
 * ## Validation Rules
 * - Product name must not be blank
 * - Description must not be blank and <= 500 characters
 * - Price must be a valid number > 0
 * - Old price (if provided) must be higher than current price
 * - Category and brand must be selected
 * - Quantity must be a valid integer >= 0
 * - All image URLs must be valid HTTP/HTTPS URLs
 * - At least one size and color must be selected (or defaults will be used)
 *
 * ## User Workflow
 * 1. Fill in all required fields
 * 2. Optionally select custom sizes/colors or use defaults
 * 3. Add supporting images if needed
 * 4. Toggle product status flags
 * 5. Click "Add Product" to submit
 * 6. On success, automatically navigate back after 3 seconds
 *
 * ## Error Handling
 * - Individual field errors shown inline with red text
 * - Form-level validation prevents submission if any field is invalid
 * - Network errors shown via floating snack bar
 * - Loading state prevents duplicate submissions
 *
 * @param viewModel ViewModel for product CRUD operations
 * @param categoryViewModel ViewModel providing available categories
 * @param brandViewModel ViewModel providing available brands
 * @param sizeViewModel ViewModel providing available sizes
 * @param onNavigateBack Callback invoked when user cancels or navigates back
 *
 * @see ProductCrudViewModel for product creation logic
 * @see isValidUrl for URL validation logic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: ProductCrudViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    brandViewModel: BrandViewModel = hiltViewModel(),
    sizeViewModel: SizeViewModel = hiltViewModel(),
    tagViewModel: TagViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    cloudinaryHelper: CloudinaryHelper = CloudinaryHelper(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val productState by viewModel.productState.collectAsState()
    val brandState by brandViewModel.brandState.collectAsState()
    val sizeState by sizeViewModel.sizeState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val maxSupportingImages = 5

    val windowSizeAppConstants = LocalWindowSizeConstant.current
    val networkState = rememberNetworkState(networkManager)

    // Form State
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var oldPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedBrand by remember { mutableStateOf("") }
    var selectedColors by remember { mutableStateOf(listOf<String>()) }
    var selectedSizes by remember { mutableStateOf(listOf<String>()) }
    var inStock by remember { mutableStateOf(true) }
    var imageUrl by remember { mutableStateOf("") }
    var mainImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingMainImage by remember { mutableStateOf(false) }
    var mainImageUploadProgress by remember { mutableFloatStateOf(0f) }
    var selectedCategoryItem by remember { mutableStateOf<CategoryItem?>(null) }

    var isUploadingSupportingImages by remember { mutableStateOf(false) }
    var supportingImages by remember {
        mutableStateOf(listOf<SupportingImageData>())
    }

    // Dropdowns
    var showBrandDropdown by remember { mutableStateOf(false) }
    var showSizeDropdown by remember { mutableStateOf(false) }

    // Errors
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }
    var brandError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }
    var imageUrlError by remember { mutableStateOf(false) }
    var supportingImagesError by remember { mutableStateOf(false) }

    // Default values
    val defaultColors = remember { listOf("Black", "White") }
    val defaultSizes = remember { listOf("M", "L") }

    // Validation
    var isFormValid by remember { mutableStateOf(false) }

    val tagState by tagViewModel.tagState.collectAsState()
    var selectedTags by remember { mutableStateOf(listOf<String>()) }
    var showTagDialog by remember { mutableStateOf(false) }

    //  Simpler inline validation
    LaunchedEffect(
        productName,
        description,
        price,
        selectedCategoryItem,
        selectedBrand,
        quantity,
        imageUrl,
        supportingImages
    ) {
        isFormValid = productName.isNotBlank() &&
                description.isNotBlank() && description.length <= 500 &&
                price.toDoubleOrNull() != null && price.toDouble() > 0 &&
                selectedCategoryItem != null &&
                selectedBrand.isNotBlank() &&
                quantity.toIntOrNull() != null && quantity.toInt() >= 0 &&
                imageUrl.isNotBlank() && isValidImageIdentifier(imageUrl)
    }

    var showCategoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(productState.isSuccess) {
        if (productState.isSuccess) {
            onNavigateBack()              // ✅ Navigate first
            viewModel.resetSuccessState() // ✅ Then reset (won't cancel navigation)
        }
    }

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackBarData.collect { snackBarData ->
            currentSnackBarData = snackBarData
            showSnackBar = true
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

    LaunchedEffect(Unit) {
        categoryViewModel.loadCategories()  // This loads flat categories

        categoryViewModel.loadCategoryTree()
    }

    //   Main image picker launcher
    val mainImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            mainImageUri = it
            // Automatically upload when selected
            viewModel.uploadMainImage(it, cloudinaryHelper) { publicId ->
                imageUrl = publicId
            }
        }
    }

    val supportingImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxSupportingImages)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            //  Create unique IDs for each image
            val newImages = uris.map { uri ->
                SupportingImageData(
                    id = UUID.randomUUID().toString(), // Unique ID
                    uri = uri,
                    cloudinaryUrl = null,
                    isUploading = true
                )
            }

            // APPEND to existing images
            val allImages = supportingImages + newImages
            val limitedImages = allImages.take(maxSupportingImages)

            supportingImages = limitedImages
            isUploadingSupportingImages = true

            // Upload only the NEW images
            viewModel.uploadSupportingImages(uris, cloudinaryHelper) { publicIds ->
                //   Update the images with their Cloudinary URLs
                supportingImages = supportingImages.mapIndexed { index, img ->
                    if (img.isUploading && index >= supportingImages.size - publicIds.size) {
                        val urlIndex = index - (supportingImages.size - publicIds.size)
                        img.copy(
                            cloudinaryUrl = publicIds.getOrNull(urlIndex),
                            isUploading = false
                        )
                    } else {
                        img
                    }
                }
                isUploadingSupportingImages = false
            }
        }
    }

    CustomScaffoldContainer(
        title = R.string.add_product,
        showBottomBar = false,
        snackBarHostState = snackBarHostState,
        onNavigateBack = { onNavigateBack() },
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
                                .padding(top = windowSizeAppConstants.baseSize),
                            onDismiss = {
                                showSnackBar = false
                                currentSnackBarData = null
                            }
                        )
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                if (productState.isLoading) {
                    PaddedSection(
                        content = {
                            CustomListCardShimmer()
                        }
                    )
                } else {
                    PaddedSection(
                        alignment = Alignment.CenterHorizontally,
                        content = {
                            // Product Name
                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(),
                                label = R.string.product_name,
                                placeholder = R.string.product_name,
                                value = productName,
                                onValueChange = {
                                    productName = it
                                    nameError = it.isBlank()
                                },
                                isError = nameError,
                                errorMessage = if (nameError) stringResource(R.string.required) else ""
                            )

                            CustomSpacer()

                            // Description
                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = description,
                                onValueChange = {
                                    description = it
                                    descriptionError = it.isBlank()
                                },
                                singleLine = false,
                                label = R.string.description,
                                isError = descriptionError,
                                placeholder = R.string.enter_description,
                                supportingText = {
                                    if (descriptionError) {
                                        Text(
                                            stringResource(R.string.required),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else if (description.length > 500) {
                                        Text(
                                            "${description.length}/500",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else {
                                        Text("${description.length}/500")
                                    }
                                },
                                minLines = 3,
                                maxLines = 5
                            )

                            CustomSpacer()

                            // Pricing Section
                            HeadlineWidget(
                                leadingText = R.string.pricing
                            )

                            CustomSpacer()

                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(),
                                label = R.string.price,
                                placeholder = R.string.price,
                                value = price,
                                onValueChange = {
                                    price = it
                                    priceError =
                                        it.toDoubleOrNull() == null || (it.toDoubleOrNull()
                                            ?: 0.0) <= 0
                                },
                                isError = priceError,
                                errorMessage = if (priceError) stringResource(R.string.invalid) else "",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                trailingIconContent = {
                                    Text(
                                        text = stringResource(R.string.price_tag),
                                        modifier = Modifier.padding(end = windowSizeAppConstants.normalVerticalPadding)
                                    )
                                }
                            )

                            CustomSpacer()

                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(),
                                label = R.string.old_price,
                                placeholder = R.string.old_price,
                                value = oldPrice,
                                onValueChange = { oldPrice = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                errorMessage = run {
                                    val oldPriceValue = oldPrice.toDoubleOrNull()
                                    val currentPriceValue = price.toDoubleOrNull()
                                    if (oldPriceValue != null && currentPriceValue != null && oldPriceValue <= currentPriceValue) {
                                        "Old price should be higher"
                                    } else ""
                                },
                                trailingIconContent = {
                                    Text(
                                        text = stringResource(R.string.price_tag),
                                        modifier = Modifier.padding(end = windowSizeAppConstants.normalVerticalPadding)
                                    )
                                }
                            )
                            CustomSpacer()

                            // Category & Brand Section
                            HeadlineWidget(
                                leadingText = R.string.category_and_brand
                            )

                            CustomSpacer()

                            // Brand Dropdown
                            ExposedDropdownMenuBox(
                                expanded = showBrandDropdown,
                                onExpandedChange = { showBrandDropdown = it }
                            ) {
                                CustomTextField(
                                    value = selectedBrand,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = R.string.brand,
                                    trailingIconContent = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBrandDropdown)
                                    },
                                    isError = brandError,
                                    placeholder = R.string.brand_name,
                                    supportingText = {
                                        if (brandError) {
                                            Text(
                                                style = windowSizeAppConstants.bodyTextStyle,
                                                text = stringResource(R.string.brand_required),
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(
                                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                            enabled = true
                                        )
                                )

                                ExposedDropdownMenu(
                                    expanded = showBrandDropdown,
                                    onDismissRequest = {
                                        showBrandDropdown = false
                                        brandError = selectedBrand.isBlank()
                                    }
                                ) {
                                    if (brandState.brands.isEmpty()) {
                                        CustomDropDownMenuItem(
                                            text = {
                                                Text(
                                                    style = windowSizeAppConstants.bodyTextStyle,
                                                    text = stringResource(R.string.brand_empty_state)
                                                )
                                            },
                                            onClick = {}
                                        )
                                    } else {
                                        brandState.brands.forEach { brand ->
                                            CustomDropDownMenuItem(
                                                text = {
                                                    Text(
                                                        style = windowSizeAppConstants.bodyTextStyle,
                                                        text = brand.brandName
                                                    )
                                                },
                                                onClick = {
                                                    selectedBrand = brand.brandName
                                                    brandError = false
                                                    showBrandDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            CustomTextField(
                                value = selectedCategoryItem?.breadcrumb?.joinToString(" > ") ?: "",
                                onValueChange = {},
                                readOnly = true,
                                trailingIconContent = {
                                    Row {
                                        if (selectedCategoryItem != null) {
                                            ButtonIconComposable(
                                                showBgColor = false,
                                                buttonIcon = ButtonIcon.Vector(Icons.Filled.Clear),
                                                onClick = {
                                                    selectedCategoryItem = null
                                                    categoryError = false
                                                },
                                                contentDescription = "Clear"
                                            )
                                        }

                                        ButtonIconComposable(
                                            showBgColor = false,
                                            buttonIcon = ButtonIcon.Vector(Icons.Filled.ArrowDropDown),
                                            onClick = { showCategoryDialog = true },
                                            contentDescription = "Clear"
                                        )
                                    }
                                },
                                isError = categoryError,
                                supportingText = {
                                    if (categoryError) {
                                        Text(
                                            style = windowSizeAppConstants.bodyTextStyle,
                                            text = stringResource(R.string.category_name_empty_state),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else if (selectedCategoryItem != null) {
                                        Column {
                                            Text(
                                                "Level ${selectedCategoryItem!!.level}",
                                                style = windowSizeAppConstants.labelTextStyle,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (selectedCategoryItem!!.path.isNotEmpty()) {
                                                Text(
                                                    "Path: ${selectedCategoryItem!!.path}",
                                                    style = windowSizeAppConstants.labelTextStyle,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                },
                                label = R.string.category,
                                placeholder = R.string.add_category,
                                modifier = Modifier.fillMaxWidth()
                            )

                            CustomSpacer()

                            // Inventory Section
                            HeadlineWidget(
                                leadingText = R.string.inventory
                            )

                            CustomSpacer()

                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(),
                                label = R.string.quantity,
                                placeholder = R.string.quantity,
                                value = quantity,
                                onValueChange = {
                                    quantity = it
                                    quantityError = it.toIntOrNull() == null || it.toInt() < 0
                                },
                                isError = quantityError,
                                errorMessage = if (quantityError) stringResource(R.string.quantity_state) else "",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            CustomSpacer()

                            // Size & Color Dropdowns (keep as is - they're multi-select)
                            ExposedDropdownMenuBox(
                                expanded = showSizeDropdown,
                                onExpandedChange = { showSizeDropdown = it }
                            ) {
                                CustomTextField(
                                    value = if (selectedSizes.isEmpty()) "Default: ${
                                        defaultSizes.joinToString(
                                            ", "
                                        )
                                    }"
                                    else selectedSizes.joinToString(", "),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = R.string.size,
                                    trailingIconContent = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSizeDropdown)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(
                                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                            enabled = true
                                        ),
                                    placeholder = R.string.default_size
                                )

                                ExposedDropdownMenu(
                                    expanded = showSizeDropdown,
                                    onDismissRequest = { showSizeDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = selectedSizes.isEmpty(),
                                                    onCheckedChange = null
                                                )
                                                Text(
                                                    style = windowSizeAppConstants.bodyTextStyle,
                                                    text = "Use default sizes (${
                                                        defaultSizes.joinToString(
                                                            ", "
                                                        )
                                                    })"
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedSizes = emptyList()
                                            showSizeDropdown = false
                                        }
                                    )

                                    CustomHorizontalDivider()

                                    if (sizeState.sizes.isEmpty()) {
                                        CustomDropDownMenuItem(
                                            text = {
                                                Text(
                                                    style = windowSizeAppConstants.bodyTextStyle,
                                                    text = stringResource(R.string.no_size_available)
                                                )
                                            },
                                            onClick = {}
                                        )
                                    } else {
                                        sizeState.sizes.forEach { size ->
                                            CustomDropDownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Checkbox(
                                                            checked = selectedSizes.contains(size.size),
                                                            onCheckedChange = null
                                                        )

                                                        Text(
                                                            style = windowSizeAppConstants.bodyTextStyle,
                                                            text = size.size
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedSizes =
                                                        if (selectedSizes.contains(size.size)) {
                                                            selectedSizes - size.size
                                                        } else {
                                                            selectedSizes + size.size
                                                        }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            CustomSpacer()

                            ProductColorSelection(
                                selectedColors = selectedColors,
                                onColorsChanged = { selectedColors = it },
                                defaultColors = defaultColors,
                                multiSelect = true
                            )

                            CustomSpacer()

                            // Media Section
                            HeadlineWidget(
                                leadingText = R.string.media
                            )

                            CustomSpacer()

                            // Main Image Preview & Picker
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(customSpacing.custom200),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        isUploadingMainImage -> {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                CustomCircularProgressIndicator(
                                                    progress = mainImageUploadProgress
                                                )

                                                CustomSpacer(
                                                    modifier = Modifier.height(
                                                        windowSizeAppConstants.normalVerticalPadding
                                                    )
                                                )

                                                Text(
                                                    text = "Uploading... ${(mainImageUploadProgress * 100).toInt()}%",
                                                    style = windowSizeAppConstants.bodyTextStyle
                                                )
                                            }
                                        }

                                        mainImageUri != null -> {
                                            CustomImageContainer(
                                                data = mainImageUri,
                                                contentDescription = "main image",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        imageUrl.isNotBlank() -> {

                                            CustomImageContainer(
                                                data = cloudinaryHelper.getImageUrl(imageUrl),
                                                contentDescription = "main image",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        else -> {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                CustomIcon(
                                                    icon = Icons.Filled.Image,
                                                    contentDescription = null,
                                                    iconSize = windowSizeAppConstants.customSpacerSmall,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Text(
                                                    stringResource(R.string.select_image),
                                                    style = windowSizeAppConstants.bodyTextStyle,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    // Remove button overlay
                                    if (mainImageUri != null || imageUrl.isNotBlank()) {

                                        ButtonIconComposable(
                                            showBgColor = false,
                                            buttonIcon = ButtonIcon.Vector(Icons.Filled.Close),
                                            onClick = {
                                                mainImageUri = null
                                                imageUrl = ""
                                            },
                                            contentDescription = "Edit",
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(windowSizeAppConstants.normalVerticalPadding)
                                                .background(
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }

                            CustomSpacer()

                            // Select Main Image Button
                            CustomButton(
                                strLabel = if (imageUrl.isBlank()) "Select Main Image" else "Change Main Image",
                                onClick = {
                                    mainImagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                icon = ButtonIcon.Vector(Icons.Filled.Image),
                                enabled = !isUploadingMainImage
                            )

                            if (imageUrlError) {
                                Text(
                                    stringResource(R.string.valid_image_required),
                                    color = MaterialTheme.colorScheme.error,
                                    style = windowSizeAppConstants.labelTextStyle,
                                    modifier = Modifier.padding(
                                        start = windowSizeAppConstants.basePadding,
                                        top = windowSizeAppConstants.smallVerticalPadding
                                    )
                                )
                            }

                            CustomSpacer()

                            // Section Header
                            HeadlineWidget(
                                leadingText = R.string.supporting_images
                            )

                            CustomSpacer(modifier = Modifier.height(windowSizeAppConstants.normalVerticalPadding))

                            Text(
                                text = "Add up to $maxSupportingImages  additional product images",
                                style = windowSizeAppConstants.labelTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            CustomSpacer(modifier = Modifier.height(windowSizeAppConstants.baseNormalVerticalPadding))

                            // Supporting Images Grid

                            if (supportingImages.isNotEmpty()) {
                                CustomLazyRow(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(
                                        items = supportingImages,
                                        key = { it.id }
                                    ) { imageData ->
                                        SupportingImageCard(
                                            imageData = imageData,
                                            cloudinaryHelper = cloudinaryHelper,
                                            onRemove = {
                                                supportingImages =
                                                    supportingImages.filter { it.id != imageData.id }
                                            }
                                        )
                                    }
                                }
                                CustomSpacer()
                            }

                            //   Add Supporting Images Button
                            val uploadedImagesCount =
                                supportingImages.count { it.cloudinaryUrl != null }

                            if (uploadedImagesCount < maxSupportingImages) {
                                CustomButton(
                                    strLabel = if (uploadedImagesCount == 0)
                                        "Add Supporting Images"
                                    else
                                        "Add More ($uploadedImagesCount/$maxSupportingImages)",
                                    onClick = {
                                        supportingImagePickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    icon = ButtonIcon.Vector(Icons.Filled.Add),
                                    enabled = !isUploadingSupportingImages,
                                )
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.5f
                                        )
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(windowSizeAppConstants.baseNormalVerticalPadding),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            windowSizeAppConstants.normalVerticalPadding
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CustomIcon(
                                            icon = Icons.Filled.Info,
                                            contentDescription = "Info",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Text(
                                            "Maximum of $maxSupportingImages supporting images reached",
                                            style = windowSizeAppConstants.labelTextStyle,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }


                            CustomSpacer()

                            //TAGS SECTION (same as AddProductScreen)
                            HeadlineWidget(
                                leadingText = R.string.product_tags
                            )

                            CustomSpacer(modifier = Modifier.height(windowSizeAppConstants.smallVerticalPadding))

                            // Display selected tags
                            if (selectedTags.isNotEmpty()) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(
                                        windowSizeAppConstants.normalVerticalPadding
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(
                                        windowSizeAppConstants.normalVerticalPadding
                                    )
                                ) {
                                    selectedTags.forEach { tagName ->
                                        val tag = tagState.tags.find { it.name == tagName }

                                        CustomAssistChip(
                                            onClick = {
                                                selectedTags = selectedTags - tagName
                                            },
                                            label = tag?.displayName ?: tagName,
                                            leadingIcon = {
                                                CustomIcon(
                                                    icon = Icons.Filled.Check,
                                                    contentDescription = "Check",
                                                    iconSize = windowSizeAppConstants.basePadding
                                                )
                                            },
                                            trailingIcon = {
                                                CustomIcon(
                                                    icon = Icons.Filled.Close,
                                                    contentDescription = "Remove",
                                                    iconSize = windowSizeAppConstants.basePadding
                                                )
                                            },
                                            containerColor = try {
                                                Color((tag?.color ?: "#2196F3").toColorInt())
                                                    .copy(alpha = 0.2f)
                                            } catch (_: Exception) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            }
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    stringResource(R.string.no_selected_tags),
                                    style = windowSizeAppConstants.bodyTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            CustomSpacer()

                            CustomButton(
                                strLabel = if (selectedTags.isEmpty()) "Add Tags" else "Edit Tags (${selectedTags.size})",
                                onClick = { showTagDialog = true },
                                icon = ButtonIcon.Vector(Icons.Filled.Edit),
                                modifier = Modifier.fillMaxWidth()
                            )

                            CustomSpacer()

                            // Status Section
                            HeadlineWidget(
                                leadingText = R.string.status
                            )

                            Row(
                                modifier = Modifier.then(windowSizeAppConstants.adaptiveWidthModifier),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.in_stock))
                                Switch(checked = inStock, onCheckedChange = { inStock = it })
                            }

                            Row(
                                modifier = Modifier.then(windowSizeAppConstants.adaptiveWidthModifier),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CustomOutlinedButton(
                                    onClick = onNavigateBack,
                                    label = R.string.cancel
                                )

                                CustomSpacer(modifier = Modifier.width(windowSizeAppConstants.normalVerticalPadding))

                                CustomButton(
                                    onClick = {
                                        // Field validation
                                        nameError = productName.isBlank()
                                        priceError =
                                            price.toDoubleOrNull() == null || price.toDouble() <= 0
                                        descriptionError = description.isBlank()
                                        categoryError = selectedCategoryItem == null
                                        brandError = selectedBrand.isBlank()
                                        quantityError =
                                            quantity.toIntOrNull() == null || quantity.toInt() < 0

                                        imageUrlError = imageUrl.isBlank()

                                        supportingImagesError = supportingImages.any {
                                            it.cloudinaryUrl?.isBlank() == true && !it.isUploading
                                        }

                                        val mainImageCloudinaryUrl = if (imageUrl.isNotBlank()) {
                                            cloudinaryHelper.getImageUrl(imageUrl)
                                        } else ""

                                        //   Convert supporting images public_ids to full Cloudinary URLs
                                        val supportingImageCloudinaryUrls = supportingImages
                                            .mapNotNull { it.cloudinaryUrl }
                                            .filter { it.isNotBlank() }
                                            .map { publicId ->
                                                cloudinaryHelper.getImageUrl(publicId)
                                            }

                                        // Check if all validations pass
                                        if (!nameError && !priceError && !descriptionError &&
                                            !categoryError && !brandError && !quantityError &&
                                            !imageUrlError && !supportingImagesError
                                        ) {

                                            // Create product with Cloudinary public IDs
                                            val product = ProductItem(
                                                productName = productName,
                                                description = description,
                                                price = price.toDouble(),
                                                oldPrice = oldPrice.toDoubleOrNull() ?: 0.0,
                                                quantity = quantity.toIntOrNull() ?: 0,
                                                brand = selectedBrand,
                                                category = selectedCategoryItem?.categoryName ?: "",
                                                sizes = selectedSizes.ifEmpty { defaultSizes },
                                                colors = selectedColors.ifEmpty { defaultColors },
                                                supportingImageUrls = supportingImageCloudinaryUrls,
                                                imageUrl = mainImageCloudinaryUrl,
                                                inStock = inStock,
                                                tags = selectedTags,
                                                createdAt = Timestamp.now(),
                                                updatedAt = Timestamp.now()
                                            )
                                            viewModel.createProduct(product)
                                        }
                                    },
                                    enabled = isFormValid,
                                    label = R.string.add_product
                                )

                                CustomSpacer(modifier = Modifier.height(windowSizeAppConstants.customSpacerSmall))

                            }
                        }
                    )
                }
            }
        }
    )

    if (showCategoryDialog) {
        HierarchicalCategoryDialog(
            categoryViewModel = categoryViewModel,
            onCategorySelected = { category ->
                selectedCategoryItem = category
                categoryError = false
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }

    //  Tag Selection Dialog
    if (showTagDialog) {
        TagSelectionDialog(
            tags = tagState.tags,
            selectedTags = selectedTags,
            onTagsSelected = { newTags ->
                selectedTags = newTags
                showTagDialog = false
            },
            onDismiss = { showTagDialog = false }
        )
    }
}

/**
 * TagSelectionDialog - Multi-select search dialog for product tags
 *
 * Provides a searchable list of available tags with checkbox selection.
 *
 * @param tags Full list of available product tags
 * @param selectedTags Currently selected tag names
 * @param onTagsSelected Callback with updated list of selected tag names
 * @param onDismiss Callback to close the dialog
 * @param viewModel ViewModel for tag search and loading
 */
@Composable
fun TagSelectionDialog(
    tags: List<ProductTag>,
    selectedTags: List<String>,
    onTagsSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit,
    viewModel: TagViewModel = hiltViewModel()
) {
    val windowSizeAppConstant = LocalWindowSizeConstant.current

    var tempSelectedTags by remember { mutableStateOf(selectedTags.toSet()) }
    var searchQuery by remember { mutableStateOf("") }

    val uniqueTags = remember(tags) {
        tags.distinctBy { it.name }
    }

    val filteredTags = remember(searchQuery, uniqueTags) {
        if (searchQuery.isBlank()) {
            uniqueTags
        } else {
            uniqueTags.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.displayName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_tag),
                style = windowSizeAppConstant.titleTextStyle
            )
        },
        text = {
            //   NO verticalScroll here
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = customSpacing.custom400),
                verticalArrangement = Arrangement.spacedBy(windowSizeAppConstant.baseNormalVerticalPadding)
            ) {
                CustomSearchBar(
                    query = searchQuery,
                    onQueryChange = { newQuery ->
                        searchQuery = newQuery
                    },
                    onSearch = { query ->
                        viewModel.searchTags(query)
                    },
                    leadingIcon = {
                        CustomIcon(
                            icon = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    placeholder = {
                        Text(
                            style = windowSizeAppConstant.bodyTextStyle,
                            text = stringResource(R.string.search_tags),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            ButtonIconComposable(
                                showBgColor = false,
                                buttonIcon = ButtonIcon.Vector(Icons.Filled.Close),
                                onClick = {
                                    searchQuery = ""
                                    viewModel.loadAllTags()
                                },
                                contentDescription = "Clear"
                            )
                        }
                    }
                )

                //   Use LazyColumn for the scrollable list (THIS is the only scrolling)
                CustomLazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    if (filteredTags.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_selected_tags),
                                style = windowSizeAppConstant.bodyTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(windowSizeAppConstant.basePadding)
                            )
                        }
                    } else {
                        items(filteredTags, key = { it.id }) { tag ->
                            val isSelected = tempSelectedTags.contains(tag.name)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        tempSelectedTags =
                                            if (isSelected) {
                                                tempSelectedTags - tag.name
                                            } else {
                                                tempSelectedTags + tag.name
                                            }
                                    }
                                    .padding(windowSizeAppConstant.normalVerticalPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(windowSizeAppConstant.normalVerticalPadding)
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null
                                )

                                Text(
                                    text = tag.displayName,
                                    style = windowSizeAppConstant.bodyTextStyle,
                                    modifier = Modifier.weight(1f)
                                )

                                // 🎨 Tag color indicator
                                Box(
                                    modifier = Modifier
                                        .size(windowSizeAppConstant.baseNormalVerticalPadding)
                                        .clip(CircleShape)
                                        .background(
                                            try {
                                                Color(tag.color.toColorInt())
                                            } catch (_: Exception) {
                                                MaterialTheme.colorScheme.primary
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.cancel,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            CustomTextButton(
                onClick = {
                    onTagsSelected(tempSelectedTags.toList())
                },
                label = R.string.save_tag,
            )
        }
    )
}

/**
 * SupportingImageCard - Visual preview and management for additional product images
 *
 * Shows a loading indicator while uploading, a preview from Cloudinary once uploaded,
 * or a local URI preview if not yet uploaded. Includes a remove button.
 *
 * @param imageData Information about the supporting image (URI, Cloudinary URL, upload status)
 * @param cloudinaryHelper Helper for generating full Cloudinary URLs
 * @param onRemove Callback to remove this image from the selection
 */
@Composable
fun SupportingImageCard(
    imageData: SupportingImageData,
    cloudinaryHelper: CloudinaryHelper,
    onRemove: () -> Unit
) {
    val windowSizeAppConstant = LocalWindowSizeConstant.current

    Card(
        modifier = Modifier.size(customSpacing.custom120),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                //  Show loading while uploading
                imageData.isUploading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CustomCircularProgressIndicator()

                            CustomSpacer(modifier = Modifier.height(windowSizeAppConstant.smallVerticalPadding))

                            Text(
                                style = windowSizeAppConstant.bodyTextStyle,
                                text = stringResource(R.string.uploading)
                            )
                        }
                    }
                }
                //  Show uploaded image from Cloudinary
                imageData.cloudinaryUrl != null -> {
                    CustomImageContainer(
                        data = cloudinaryHelper.getImageUrl(imageData.cloudinaryUrl),
                        contentDescription = "supporting image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                //  Show local URI preview
                else -> {
                    CustomImageContainer(
                        data = imageData.uri,
                        contentDescription = "supporting image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Remove button overlay
            ButtonIconComposable(
                modifier = Modifier
                    .align(Alignment.TopEnd),
                showBgColor = false,
                buttonIcon = ButtonIcon.Vector(Icons.Filled.Close),
                onClick = { onRemove() },
                contentDescription = "remove"
            )
        }
    }
}

/**
 * HierarchicalCategoryDialog - Drill-down category selection tool
 *
 * Displays the category tree in a searchable dialog, allowing users to select
 * a specific category (including deep children) for a product.
 *
 * @param categoryViewModel ViewModel providing category state and expansion logic
 * @param onCategorySelected Callback when a category is chosen
 * @param onDismiss Callback to close the dialog
 * @param viewModel Secondary ViewModel reference for search operations
 */
@Composable
fun HierarchicalCategoryDialog(
    categoryViewModel: CategoryViewModel,
    onCategorySelected: (CategoryItem) -> Unit,
    onDismiss: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val windowSizeAppConstant = LocalWindowSizeConstant.current

    val categoryState by categoryViewModel.categoryState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Filter categories based on search
    val filteredTree = remember(categoryState.categoryTree, searchQuery) {
        if (searchQuery.isBlank()) {
            categoryState.categoryTree
        } else {
            filterCategoryTree(categoryState.categoryTree, searchQuery)
        }
    }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_category),
                style = windowSizeAppConstant.titleTextStyle
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = customSpacing.custom500)
            ) {
                // Search bar
                CustomSearchBar(
                    query = searchQuery,
                    onQueryChange = { newQuery ->
                        searchQuery = newQuery
                        if (newQuery.isNotEmpty()) {
                            viewModel.searchCategories(newQuery)
                        } else {
                            viewModel.loadCategories()
                        }
                    },
                    onSearch = { query ->
                        viewModel.searchCategories(query)
                    },
                    leadingIcon = {
                        CustomIcon(
                            icon = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    placeholder = {
                        Text(
                            style = windowSizeAppConstant.bodyTextStyle,
                            text = stringResource(R.string.search_categories)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            ButtonIconComposable(
                                showBgColor = false,
                                buttonIcon = ButtonIcon.Vector(Icons.Filled.Clear),
                                onClick = {
                                    searchQuery = ""
                                    viewModel.loadCategories()
                                },
                                contentDescription = "Clear"
                            )
                        }
                    }
                )

                CustomSpacer(modifier = Modifier.height(windowSizeAppConstant.normalVerticalPadding))

                // Category tree
                if (categoryState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(customSpacing.custom200),
                        contentAlignment = Alignment.Center
                    ) {
                        CustomCircularProgressIndicator()
                    }
                } else if (filteredTree.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(customSpacing.custom200),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_categories_available),
                            style = windowSizeAppConstant.bodyTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    CustomLazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredTree) { node ->
                            SelectableCategoryTreeItem(
                                node = node,
                                onExpand = { categoryViewModel.toggleCategoryExpansion(it) },
                                onSelect = { category ->
                                    onCategorySelected(category)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            CustomTextButton(onClick = onDismiss, label = R.string.cancel)
        }
    )
}

/**
 * SelectableCategoryTreeItem - Individual node in the category hierarchy tree
 *
 * Displays category name, level indicator, and breadcrumbs. Supports expansion
 * to reveal children and direct selection.
 *
 * @param node The category tree node to display
 * @param onExpand Callback to toggle expansion state
 * @param onSelect Callback when this specific category is selected
 */
@Composable
private fun SelectableCategoryTreeItem(
    node: CategoryNode,
    onExpand: (String) -> Unit,
    onSelect: (CategoryItem) -> Unit
) {
    val windowSizeAppConstant = LocalWindowSizeConstant.current

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = windowSizeAppConstant.baseSize * node.depth,
                    top = windowSizeAppConstant.smallVerticalPadding,
                    bottom = windowSizeAppConstant.smallVerticalPadding
                )
                .clickable { onSelect(node.category) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(windowSizeAppConstant.baseNormalVerticalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Expand/Collapse icon
                    if (node.category.hasSubcategories) {
                        ButtonIconComposable(
                            showBgColor = false,
                            buttonIcon = if (node.isExpanded)
                                ButtonIcon.Vector(Icons.Filled.ExpandMore)
                            else
                                ButtonIcon.Vector(Icons.Filled.ChevronRight),
                            onClick = { onExpand(node.category.id) },
                            contentDescription = if (node.isExpanded) "Collapse" else "Expand"
                        )
                    } else {
                        CustomSpacer(modifier = Modifier.size(windowSizeAppConstant.baseSize))
                    }

                    CustomSpacer(modifier = Modifier.width(windowSizeAppConstant.normalVerticalPadding))

                    // Category icon based on level (same as CategoryManagementScreen)
                    CustomIcon(
                        icon = when (node.category.level) {
                            0 -> Icons.Filled.Folder
                            1 -> Icons.Filled.FolderOpen
                            else -> Icons.AutoMirrored.Filled.InsertDriveFile
                        },
                        contentDescription = null,
                        tint = when (node.category.level) {
                            0 -> colors.customColor1
                            1 -> colors.customColor4
                            else -> colors.customColor5
                        },
                        modifier = Modifier.size(windowSizeAppConstant.baseSize)
                    )

                    CustomSpacer(modifier = Modifier.width(windowSizeAppConstant.normalVerticalPadding))

                    Column {
                        Text(
                            text = node.category.categoryName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (node.category.level == 0)
                                    FontWeight.Bold
                                else
                                    FontWeight.Medium
                            )
                        )

                        // Breadcrumb
                        if (node.category.breadcrumb.size > 1) {
                            Text(
                                text = node.category.breadcrumb.joinToString(" > "),
                                style = windowSizeAppConstant.labelTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Stats (optional - show subcategory count)
                        if (node.category.hasSubcategories) {
                            Text(
                                "${node.category.subcategoryIds.size} subcategories",
                                style = windowSizeAppConstant.labelTextStyle,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Level indicator
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "L${node.category.level}",
                        modifier = Modifier.padding(
                            horizontal = windowSizeAppConstant.baseVerticalPadding,
                            vertical = windowSizeAppConstant.cardElevationPadding
                        ),
                        style = windowSizeAppConstant.labelTextStyle,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Render subcategories if expanded (same recursive pattern)
        if (node.isExpanded && node.subcategories.isNotEmpty()) {
            Column {
                node.subcategories.forEach { subNode ->
                    SelectableCategoryTreeItem(
                        node = subNode,
                        onExpand = onExpand,
                        onSelect = onSelect
                    )
                }
            }
        }
    }
}

/**
 * isValidCloudinaryId - Validates Cloudinary public IDs
 *
 * Checks if the string matches Cloudinary's naming conventions (alphanumeric with basic symbols).
 *
 * @param publicId The Cloudinary public identifier to validate
 * @return True if valid, false otherwise
 */
fun isValidCloudinaryId(publicId: String): Boolean {
    // Cloudinary public IDs can contain letters, numbers, underscores, hyphens, dots, and slashes
    val cloudinaryPattern = Regex("^[a-zA-Z0-9_\\-/.]+$")
    return publicId.isNotBlank() && cloudinaryPattern.matches(publicId)
}

/**
 * isValidImageIdentifier - Validates if a string is a valid web URL or Cloudinary ID
 *
 * Used to ensure image strings provided by the user can be used for fetching images.
 *
 * @param identifier The string to validate (URL or Cloudinary ID)
 * @return True if it matches web URL patterns or Cloudinary ID patterns
 */
fun isValidImageIdentifier(identifier: String): Boolean {
    // Check if it's a valid URL OR a valid Cloudinary public ID
    return Patterns.WEB_URL.matcher(identifier).matches() ||
            isValidCloudinaryId(identifier)
}

