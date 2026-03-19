package com.example.myapp.view.admin.components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.CategoryItem
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
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomAssistChip
import com.example.myapp.view.components.CustomCircularProgressIndicator
import com.example.myapp.view.components.CustomDropDownMenuItem
import com.example.myapp.view.components.CustomHorizontalDivider
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomImageContainer
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
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CloudinaryHelper
import com.example.myapp.view.utils.isValidUrl
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * EditProductScreen - Product editing interface
 *
 * This screen allows administrators to modify existing product details. The form is pre-populated
 * with the current product data and uses the same validation rules as the AddProductScreen.
 *
 * ## Features
 * - **Pre-populated Form**: All fields loaded with current product data
 * - **Real-time Validation**: Same validation as product creation
 * - **Multi-select Options**: Sizes and colors can be modified
 * - **Image Management**: Update main and supporting images
 * - **Status Toggles**: Modify stock, featured, and trending status
 * - **Auto-save on Success**: Navigates back after successful update
 *
 * ## Form Sections
 *
 * ### 1. Basic Information
 * - Product Name (required, pre-filled)
 * - Description (required, pre-filled, max 500 characters)
 *
 * ### 2. Pricing
 * - Current Price (required, pre-filled)
 * - Old Price (optional, pre-filled if exists)
 *
 * ### 3. Category & Brand
 * - Brand (dropdown, pre-selected)
 * - Category (dropdown, pre-selected)
 *
 * ### 4. Inventory
 * - Quantity (pre-filled)
 * - Sizes (multi-select, pre-selected)
 * - Colors (multi-select, pre-selected)
 *
 * ### 5. Media
 * - Main Image URL (pre-filled)
 * - Supporting Images (pre-filled, can add/remove)
 *
 * ### 6. Status
 * - In Stock (toggle, pre-set)
 * - Featured Product (toggle, pre-set)
 * - Trending Product (toggle, pre-set)
 *
 * ## User Workflow
 * 1. Screen loads with product ID
 * 2. Product data is fetched and form is populated
 * 3. User modifies desired fields
 * 4. Validation occurs in real-time
 * 5. Click "Update Product" to save changes
 * 6. On success, automatically navigate back after 2 seconds
 *
 * ## Validation Rules
 * - Same as AddProductScreen
 * - All required fields must be filled
 * - Prices must be valid positive numbers
 * - Image URLs must be valid HTTP/HTTPS URLs
 *
 * ## Loading States
 * - Shows shimmer while loading product data
 * - Disables submit button during update operation
 * - Shows snackbar for success/error feedback
 *
 * @param productId The ID of the product to edit
 * @param viewModel ViewModel for product CRUD operations
 * @param categoryViewModel ViewModel providing available categories
 * @param brandViewModel ViewModel providing available brands
 * @param sizeViewModel ViewModel providing available sizes
 * @param onNavigateBack Callback invoked when user cancels or navigates back
 *
 * @see ProductCrudViewModel for product update logic
 * @see AddProductScreen for similar form structure
 * @see isValidUrl for URL validation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    productId: String,
    viewModel: ProductCrudViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    brandViewModel: BrandViewModel = hiltViewModel(),
    sizeViewModel: SizeViewModel = hiltViewModel(),
    tagViewModel: TagViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    cloudinaryHelper: CloudinaryHelper = CloudinaryHelper(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    val productState by viewModel.productState.collectAsState()
    val brandState by brandViewModel.brandState.collectAsState()
    val sizeState by sizeViewModel.sizeState.collectAsState()
    val tagState by tagViewModel.tagState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val currentProduct = productState.currentProduct
    val maxSupportingImages = 5

    // Load product data
    LaunchedEffect(productId) {
        viewModel.getProductById(productId)
        categoryViewModel.loadCategories()
        categoryViewModel.loadCategoryTree()
    }

    // Form State
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var oldPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("0") }
    var selectedBrand by remember { mutableStateOf("") }
    var selectedCategoryItem by remember { mutableStateOf<CategoryItem?>(null) }
    var selectedColors by remember { mutableStateOf(listOf<String>()) }
    var selectedSizes by remember { mutableStateOf(listOf<String>()) }
    var selectedTags by remember { mutableStateOf(listOf<String>()) }
    var inStock by remember { mutableStateOf(true) }

    //  Image upload state
    var imageUrl by remember { mutableStateOf("") }
    var mainImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingMainImage by remember { mutableStateOf(false) }

    var supportingImages by remember { mutableStateOf(listOf<SupportingImageData>()) }
    var isUploadingSupportingImages by remember { mutableStateOf(false) }

    // Dropdowns & Dialogs
    var showBrandDropdown by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showSizeDropdown by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }

    // Errors
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }
    var brandError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }
    var imageUrlError by remember { mutableStateOf(false) }

    // Default values
    val defaultColors = remember { listOf("Black", "White") }
    val defaultSizes = remember { listOf("M", "L") }
    val networkState = rememberNetworkState(networkManager)

    var isFormInitialized by remember { mutableStateOf(false) }

    //  Update form when product loads
    LaunchedEffect(currentProduct) {
        currentProduct?.let { product ->
            if (!isFormInitialized) {
                productName = product.productName
                description = product.description
                price = product.price.toString()
                oldPrice = if (product.oldPrice > 0) product.oldPrice.toString() else ""
                quantity = product.quantity.toString()
                selectedBrand = product.brand

                // Find and set the category item
                val categoryItem = categoryViewModel.categoryState.value.categories
                    .find { it.categoryName == product.category }
                selectedCategoryItem = categoryItem

                selectedSizes = product.sizes
                selectedColors = product.colors
                selectedTags = product.tags
                imageUrl = product.imageUrl

                // Convert existing supporting image URLs to SupportingImageData
                supportingImages = product.supportingImageUrls.map { url ->
                    SupportingImageData(
                        id = UUID.randomUUID().toString(),
                        uri = Uri.EMPTY,
                        cloudinaryUrl = url,
                        isUploading = false
                    )
                }
                inStock = product.inStock

                isFormInitialized = true

                Log.d("EditProduct", "Form initialized - Quantity: $quantity")
            }
        }
    }

    //  Main image picker launcher
    val mainImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            mainImageUri = it
            isUploadingMainImage = true
            viewModel.uploadMainImage(it, cloudinaryHelper) { publicId ->
                imageUrl = publicId
                isUploadingMainImage = false
            }
        }
    }

    //   Supporting images picker launcher
    val supportingImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxSupportingImages)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newImages = uris.map { uri ->
                SupportingImageData(
                    id = UUID.randomUUID().toString(),
                    uri = uri,
                    cloudinaryUrl = null,
                    isUploading = true
                )
            }

            val allImages = supportingImages + newImages
            val limitedImages = allImages.take(maxSupportingImages)

            supportingImages = limitedImages
            isUploadingSupportingImages = true

            viewModel.uploadSupportingImages(uris, cloudinaryHelper) { publicIds ->
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

    CustomScaffoldContainer(
        title = R.string.edit_product,
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
                                .padding(top = windowSizeClass.basePadding),
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
                    CustomListCardShimmer()
                } else {
                    PaddedSection(
                        content = {
                            // Product Name
                            CustomTextField(
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
                                            color = MaterialTheme.colorScheme.error,
                                            style = windowSizeClass.labelTextStyle

                                        )
                                    } else if (description.length > 500) {
                                        Text(
                                            "${description.length}/500",
                                            color = MaterialTheme.colorScheme.error,
                                            style = windowSizeClass.labelTextStyle
                                        )
                                    } else {
                                        Text(
                                            "${description.length}/500",
                                            style = windowSizeClass.labelTextStyle

                                        )
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
                                label = R.string.price,
                                placeholder = R.string.price,
                                value = price,
                                onValueChange = {
                                    price = it
                                    priceError = it.toDoubleOrNull() == null || it.toDouble() <= 0
                                },
                                isError = priceError,
                                errorMessage = if (priceError) stringResource(R.string.invalid) else "",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                trailingIconContent = {
                                    Text(
                                        style = windowSizeClass.bodyTextStyle,
                                        text = stringResource(R.string.price_tag),
                                        modifier = Modifier.padding(end = windowSizeClass.normalVerticalPadding)
                                    )
                                }
                            )

                            CustomSpacer()

                            CustomTextField(
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
                                        style = windowSizeClass.bodyTextStyle,
                                        modifier = Modifier.padding(end = windowSizeClass.normalVerticalPadding)
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
                                                style = windowSizeClass.bodyTextStyle,
                                                text = stringResource(R.string.brand_required),
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    },
                                    modifier = Modifier.menuAnchor(
                                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                        enabled = true
                                    ),
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
                                                    style = windowSizeClass.bodyTextStyle,
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
                                                        style = windowSizeClass.bodyTextStyle,
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

                            CustomSpacer()

                            // Hierarchical Category Selection
                            CustomTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = selectedCategoryItem?.breadcrumb?.joinToString(" > ") ?: "",
                                onValueChange = {},
                                readOnly = true,
                                trailingIconContent = {
                                    Row {
                                        if (selectedCategoryItem != null) {
                                            ButtonIconComposable(
                                                showBgColor = false,
                                                buttonIcon = ButtonIcon.Vector(
                                                    Icons.Filled.Clear,
                                                ),
                                                onClick = {
                                                    selectedCategoryItem = null
                                                    categoryError = false
                                                },
                                                contentDescription = "Clear"
                                            )
                                        }

                                        ButtonIconComposable(
                                            showBgColor = false,
                                            buttonIcon = ButtonIcon.Vector(
                                                Icons.Filled.ArrowDropDown,
                                            ),
                                            onClick = { showCategoryDialog = true },
                                            contentDescription = "Select"
                                        )
                                    }
                                },
                                isError = categoryError,
                                supportingText = {
                                    if (categoryError) {
                                        Text(
                                            style = windowSizeClass.bodyTextStyle,
                                            text = stringResource(R.string.category_name_empty_state),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else if (selectedCategoryItem != null) {
                                        Column {
                                            Text(
                                                "Level ${selectedCategoryItem!!.level}",
                                                style = windowSizeClass.labelTextStyle,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            if (selectedCategoryItem!!.path.isNotEmpty()) {
                                                Text(
                                                    "Path: ${selectedCategoryItem!!.path}",
                                                    style = windowSizeClass.labelTextStyle,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                },
                                label = R.string.category,
                                placeholder = R.string.add_category,
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

                            // Size Multi-Select Dropdown
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
                                                    "Use default sizes (${
                                                        defaultSizes.joinToString(
                                                            ", "
                                                        )
                                                    })",
                                                    style = windowSizeClass.bodyTextStyle
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
                                                    stringResource(R.string.no_size_available),
                                                    style = windowSizeClass.bodyTextStyle
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
                                                            size.size,
                                                            style = windowSizeClass.bodyTextStyle
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

                            // Color Multi-Select Dropdown
                            ProductColorSelection(
                                selectedColors = selectedColors,
                                onColorsChanged = { selectedColors = it },
                                defaultColors = defaultColors,
                                multiSelect = true
                            )

                            CustomSpacer()

                            // MEDIA SECTION WITH IMAGE PICKER
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
                                                CustomCircularProgressIndicator()
                                                CustomSpacer(
                                                    modifier = Modifier.height(
                                                        windowSizeClass.normalVerticalPadding
                                                    )
                                                )

                                                Text(
                                                    stringResource(R.string.uploading),
                                                    style = windowSizeClass.bodyTextStyle
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
                                                    modifier = Modifier.size(windowSizeClass.largeIconSize),
                                                    contentDescription = "Image"
                                                )

                                                Text(
                                                    stringResource(R.string.select_image),
                                                    style = windowSizeClass.bodyTextStyle,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    // Remove button overlay
                                    if (mainImageUri != null || imageUrl.isNotBlank()) {

                                        ButtonIconComposable(
                                            buttonIcon = ButtonIcon.Vector(
                                                Icons.Filled.Close
                                            ),
                                            onClick = {
                                                mainImageUri = null
                                                imageUrl = ""
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(windowSizeClass.normalVerticalPadding)
                                                .background(
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                    CircleShape
                                                ),
                                            contentDescription = "Remove image",
                                            tint = MaterialTheme.colorScheme.error
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
                                    style = windowSizeClass.bodyTextStyle,
                                    modifier = Modifier.padding(
                                        start = windowSizeClass.basePadding,
                                        top = windowSizeClass.smallVerticalPadding
                                    )
                                )
                            }

                            CustomSpacer()

                            //  SUPPORTING IMAGES WITH IMAGE PICKER
                            HeadlineWidget(
                                leadingText = R.string.supporting_images
                            )

                            CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

                            Text(
                                text = "Add up to $maxSupportingImages additional product images",
                                style = windowSizeClass.bodyTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            CustomSpacer(modifier = Modifier.height(windowSizeClass.baseNormalVerticalPadding))

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

                            // Add Supporting Images Button
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
                                            .padding(windowSizeClass.baseNormalVerticalPadding),
                                        horizontalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CustomIcon(
                                            icon = Icons.Filled.Info,
                                            iconSize = windowSizeClass.basePadding,
                                            contentDescription = "Info"
                                        )

                                        Text(
                                            "Maximum of $maxSupportingImages supporting images reached",
                                            style = windowSizeClass.bodyTextStyle,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            CustomSpacer()

                            // TAGS SECTION
                            HeadlineWidget(
                                leadingText = R.string.product_tags
                            )

                            CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

                            // Display selected tags
                            if (selectedTags.isNotEmpty()) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding),
                                    verticalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding)
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
                                                    iconSize = windowSizeClass.basePadding,
                                                    contentDescription = "Check"
                                                )
                                            },
                                            trailingIcon = {
                                                CustomIcon(
                                                    icon = Icons.Filled.Close,
                                                    iconSize = windowSizeClass.basePadding,
                                                    contentDescription = "Remove"
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
                                    style = windowSizeClass.bodyTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            CustomSpacer()

                            CustomButton(
                                strLabel = if (selectedTags.isEmpty()) "Add Tags" else "Edit Tags (${selectedTags.size})",
                                onClick = { showTagDialog = true },
                                icon = ButtonIcon.Vector(Icons.Filled.Edit),
                            )

                            CustomSpacer()

                            // Status Section (same as AddProductScreen)
                            HeadlineWidget(
                                leadingText = R.string.status
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.in_stock),
                                    style = windowSizeClass.bodyTextStyle,
                                )
                                Switch(checked = inStock, onCheckedChange = { inStock = it })
                            }

                            CustomSpacer()

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CustomOutlinedButton(
                                    onClick = onNavigateBack,
                                    label = R.string.cancel
                                )

                                CustomSpacer(modifier = Modifier.width(windowSizeClass.normalVerticalPadding))

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

                                        // Check if all validations pass
                                        if (!nameError && !priceError && !descriptionError &&
                                            !categoryError && !brandError && !quantityError &&
                                            !imageUrlError && currentProduct != null
                                        ) {


                                            val updatedProduct = currentProduct.copy(
                                                productName = productName,
                                                description = description,
                                                price = price.toDouble(),
                                                oldPrice = oldPrice.toDoubleOrNull() ?: 0.0,
                                                quantity = quantity.toIntOrNull() ?: 0,
                                                brand = selectedBrand,
                                                category = selectedCategoryItem?.categoryName ?: "",
                                                sizes = selectedSizes,
                                                colors = selectedColors,
                                                supportingImageUrls = supportingImages
                                                    .mapNotNull { it.cloudinaryUrl }
                                                    .filter { it.isNotBlank() },  // Store public IDs, not full URLs
                                                imageUrl = imageUrl,
                                                inStock = inStock,
                                                tags = selectedTags,
                                                updatedAt = Timestamp.now()
                                            )
                                            viewModel.updateProduct(
                                                updatedProduct
                                            )
                                        }
                                    },
                                    enabled = currentProduct != null,
                                    label = R.string.update_product
                                )
                            }

                            CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))
                        }
                    )
                }
            }
        }
    )

    // Category Dialog (same as AddProductScreen)
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

    // Tag Dialog (same as AddProductScreen)
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

