package com.example.myapp.view.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.CarouselItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.CarouselViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomCircularProgressIndicator
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomFloatingPointButton
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomImageContainer
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomTextField
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.ProductShimmerList
import com.example.myapp.view.components.TopBarActionsShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CloudinaryHelper
import com.example.myapp.view.utils.CustomShape
import com.example.myapp.view.utils.formatTimestamp
import com.example.myapp.view.utils.isValidUrl
import kotlinx.coroutines.delay

/**
 * CarouselManagementScreen - Management interface for product carousels
 *
 * This screen allows administrators to perform CRUD operations on carousel items,
 * including uploading images via Cloudinary and managing redirect URLs.
 *
 * ## Features
 * - **Carousel List**: Real-time display of all active carousels
 * - **Image Upload**: Upload and preview images directly within the app
 * - **Search**: Filter carousels by title or description
 * - **Rich Content**: Support for titles, descriptions, and optional redirect URLs
 * - **Actions**: Quick edit and delete functionality for each item
 *
 * ## Workflow
 * 1. View all carousels on the main screen
 * 2. Use the search bar to filter items
 * 3. Click the floating action button (+) to add a new carousel
 * 4. Use the edit and delete icons on individual cards to manage entries
 *
 * @param viewModel ViewModel responsible for carousel operations and image uploads
 * @param onNavigateBack Callback to return to the previous screen
 * @param networkManager Manager to monitor connectivity status
 */

@Composable
fun CarouselManagementScreen(
    viewModel: CarouselViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val carouselState by viewModel.carouselState.collectAsState()
    val imageLoader = viewModel.getImageLoader()
    val networkState = rememberNetworkState(networkManager)

    val snackBarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCarousel by remember { mutableStateOf<CarouselItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    // Handle snack bar data
    LaunchedEffect(Unit) {
        viewModel.snackBarData.collect { snackBarData ->
            currentSnackBarData = snackBarData
            showSnackBar = true

            // Auto-dismiss after duration
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
        onRefresh = {
            if (networkState.hasInternet) {
                viewModel.loadCarousels()
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }
        },
        title = R.string.manage_carousel,
        snackBarHostState = snackBarHostState,
        showBottomBar = false,
        verticalArrangement = Arrangement.Top,
        onNavigateBack = { onNavigateBack() },
        floatingBtnContent = {
            CustomFloatingPointButton(
                onClick = {
                    showAddDialog = true
                }
            )
        },
        content = {
            // Network Status Banner
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

            PaddedSection(
                alignment = Alignment.CenterHorizontally,
                content = {
                    CustomSpacer()
                    CustomSearchBar(
                        query = searchQuery,
                        onQueryChange = { newQuery ->
                            searchQuery = newQuery
                            if (newQuery.isNotEmpty()) {
                                viewModel.searchCarousels(newQuery)
                            } else {
                                viewModel.loadCarousels()
                            }
                        },
                        onSearch = { query ->
                            viewModel.searchCarousels(query)
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
                                stringResource(R.string.search_carousels),
                                style = windowSizeClass.bodyTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                ButtonIconComposable(
                                    showBgColor = false,
                                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Clear),
                                    onClick = {
                                        searchQuery = ""
                                        viewModel.loadCarousels()
                                    },
                                    contentDescription = "Clear Search"
                                )
                            }
                        }
                    )
                    CustomSpacer()

                    when {
                        carouselState.isLoading -> {
                            CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))
                            ProductShimmerList()
                        }

                        carouselState.error != null -> {
                            CustomEmptyState(
                                btnLabel = R.string.retry,
                                title = R.string.carousel_error,
                                onBtnClick = { viewModel.loadCarousels() },
                                scrollState = rememberScrollState(),
                                leadingIcon = Icons.Filled.Error,
                            )
                        }

                        carouselState.carousels.isEmpty() -> {
                            CustomEmptyState(
                                titleStr = if (searchQuery.isEmpty()) "No carousels yet" else "No results found",
                                showBtn = false,
                                leadingIcon = Icons.Filled.SearchOff
                            )
                        }

                        else -> {
                            CustomLazyColumn {

                                items(carouselState.carousels) { carousel ->
                                    CarouselCard(
                                        imageLoader = imageLoader,
                                        carousel = carousel,
                                        onEdit = {
                                            selectedCarousel = carousel
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            selectedCarousel = carousel
                                            showDeleteDialog = true
                                        }
                                    )
                                }

                                item {
                                    CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))
                                }
                            }
                        }
                    }
                })

            // Dialogs
            if (showAddDialog) {
                AddCarouselDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { title, description, imageUrl, redirectUrl ->
                        viewModel.createCarousel(title, description, imageUrl, redirectUrl)
                        showAddDialog = false
                    }
                )
            }

            if (showEditDialog && selectedCarousel != null) {
                EditCarouselDialog(
                    carousel = selectedCarousel!!,
                    onDismiss = {
                        showEditDialog = false
                        selectedCarousel = null
                    },
                    onConfirm = { updatedCarousel ->
                        viewModel.updateCarousel(updatedCarousel)
                        showEditDialog = false
                        selectedCarousel = null
                    }
                )
            }

            if (showDeleteDialog && selectedCarousel != null) {
                DeleteCarouselDialog(
                    carousel = selectedCarousel!!,
                    onDismiss = {
                        showDeleteDialog = false
                        selectedCarousel = null
                    },
                    onConfirm = {
                        viewModel.deleteCarousel(selectedCarousel!!)
                        showDeleteDialog = false
                        selectedCarousel = null
                    }
                )
            }
        },
        actions = {
            if (carouselState.isLoading) {
                TopBarActionsShimmer()
            } else {
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Refresh),
                    onClick = { viewModel.loadCarousels() },
                    contentDescription = "Refresh"
                )
            }
        }
    )
}


/**
 * CarouselCard - Individual carousel item display
 *
 * Displays a carousel entry with its image, title, description, and actions.
 * Supports loading images from both Cloudinary URLs and local drawable resources.
 *
 * @param carousel The carousel item data to display
 * @param onEdit Callback when the edit action is triggered
 * @param onDelete Callback when the delete action is triggered
 * @param imageLoader Coil image loader for remote images
 * @param imageRes Optional local drawable resource for static previews
 * @param cloudinaryHelper Helper to generate Cloudinary transformation URLs
 */
@Composable
fun CarouselCard(
    carousel: CarouselItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    imageLoader: ImageLoader? = null,
    @DrawableRes imageRes: Int? = null,
    cloudinaryHelper: CloudinaryHelper = CloudinaryHelper()
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = windowSizeClass.normalVerticalPadding),
        elevation = CardDefaults.cardElevation(defaultElevation = windowSizeClass.cardElevationPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column {
            // Image
            if (carousel.imageUrl.isNotEmpty()) {
                CustomImageContainer(
                    data = cloudinaryHelper.getImageUrl(carousel.imageUrl),
                    contentDescription = "carousel image",
                    imageLoader = imageLoader,
                    shape = CustomShape.mediumShape(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(customSpacing.custom200)
                )
            } else if (imageRes != null) {
                CustomImageContainer(
                    data = imageRes,
                    contentDescription = carousel.title,
                    shape = CustomShape.mediumShape(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(customSpacing.custom200)  // Add specific height
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(customSpacing.custom200)  // Add specific height
                        .clip(
                            CustomShape.mediumShape()
                        )
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    CustomIcon(
                        icon = Icons.Filled.Image,
                        contentDescription = "No image",
                        iconSize = windowSizeClass.largeIconSize,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.padding(windowSizeClass.basePadding)
            ) {
                // Title
                Text(
                    text = carousel.title,
                    style = windowSizeClass.titleTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                CustomSpacer(modifier = Modifier.height(windowSizeClass.normalVerticalPadding))

                // Description
                Text(
                    text = carousel.description,
                    style = windowSizeClass.bodyTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Redirect URL
                carousel.redirectUrl?.let { url ->
                    CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))
                    Text(
                        text = "→ $url",
                        style = windowSizeClass.bodyTextStyle,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                CustomSpacer(modifier = Modifier.height(windowSizeClass.normalVerticalPadding))

                // Timestamp
                Text(
                    text = "Updated: ${formatTimestamp(carousel.updatedAt)}",
                    style = windowSizeClass.labelTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                CustomSpacer(modifier = Modifier.height(windowSizeClass.baseNormalVerticalPadding))

                // Actions Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ButtonIconComposable(
                        showBgColor = false,
                        buttonIcon = ButtonIcon.Vector(Icons.Filled.Edit),
                        onClick = { onEdit() },
                        contentDescription = "Edit"
                    )

                    ButtonIconComposable(
                        showBgColor = false,
                        buttonIcon = ButtonIcon.Vector(Icons.Filled.Delete),
                        onClick = { onDelete() },
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * AddCarouselDialog - Dialog for creating new carousel items
 *
 * Provides a form for entering carousel details and handles image selection/upload.
 * Validates the form before allowing submission.
 *
 * @param onDismiss Callback to close the dialog without saving
 * @param onConfirm Callback when the carousel is successfully created
 * @param imageLoader Coil image loader for previews
 * @param cloudinaryHelper Helper for Cloudinary image operations
 * @param viewModel ViewModel to handle image upload logic
 */
@Composable
fun AddCarouselDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String?) -> Unit,
    imageLoader: ImageLoader? = null,
    cloudinaryHelper: CloudinaryHelper = CloudinaryHelper(),
    viewModel: CarouselViewModel = hiltViewModel()
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var redirectUrl by remember { mutableStateOf("") }
    var imagePublicId by remember { mutableStateOf("") }

    // ✅ Image upload state
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Error states
    var titleError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }
    var imageUrlError by remember { mutableStateOf(false) }
    var redirectUrlError by remember { mutableStateOf(false) }

    //   Form validation
    val isFormValid = title.isNotBlank() &&
            description.isNotBlank() &&
            imagePublicId.isNotBlank() &&
            (redirectUrl.isBlank() || isValidUrl(redirectUrl))

    //  Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            isUploadingImage = true

            // Upload to Cloudinary
            viewModel.uploadCarouselImage(it, cloudinaryHelper) { publicId ->
                imagePublicId = publicId
                isUploadingImage = false
            }
        }
    }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Edit,
                contentDescription = "Edit carousel",
                iconSize = windowSizeClass.largeIconSize,
            )
        },
        title = {
            Text(
                text = stringResource(R.string.add_carousel),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(windowSizeClass.baseNormalVerticalPadding),
            ) {
                CustomTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = it.isBlank()
                    },
                    label = R.string.carousel_name,
                    placeholder = R.string.carousel_required,
                    singleLine = true,
                    isError = titleError,
                    errorMessage = if (titleError) stringResource(R.string.required) else "",
                    modifier = Modifier.fillMaxWidth()
                )

                CustomTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        descriptionError = it.isBlank()
                    },
                    label = R.string.carousel_description,
                    placeholder = R.string.carousel_description_placeholder,
                    minLines = 3,
                    isError = descriptionError,
                    errorMessage = if (descriptionError) stringResource(R.string.required) else "",
                    modifier = Modifier.fillMaxWidth()
                )

                //   Image Upload Section
                Text(
                    text = stringResource(R.string.carousel_image),
                    style = windowSizeClass.titleTextStyle,
                    color = MaterialTheme.colorScheme.primary
                )

                // Image Preview
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
                            isUploadingImage -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CustomCircularProgressIndicator()
                                    CustomSpacer(modifier = Modifier.height(windowSizeClass.normalVerticalPadding))
                                    Text(
                                        stringResource(R.string.uploading),
                                        style = windowSizeClass.labelTextStyle
                                    )
                                }
                            }

                            imagePublicId.isNotBlank() -> {
                                CustomImageContainer(
                                    data = cloudinaryHelper.getImageUrl(imagePublicId),
                                    contentDescription = "Carousel image preview",
                                    imageLoader = imageLoader,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            selectedImageUri != null -> {
                                CustomImageContainer(
                                    data = selectedImageUri,
                                    contentDescription = "Selected image",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CustomIcon(
                                        icon = Icons.Filled.Image,
                                        contentDescription = "icon image",
                                        iconSize = windowSizeClass.largeIconSize,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = stringResource(R.string.select_images),
                                        style = windowSizeClass.bodyTextStyle,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Remove button
                        if (imagePublicId.isNotBlank() && !isUploadingImage) {
                            ButtonIconComposable(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(windowSizeClass.normalVerticalPadding),
                                showBgColor = false,
                                buttonIcon = ButtonIcon.Vector(Icons.Filled.Close),
                                onClick = {
                                    selectedImageUri = null
                                    imagePublicId = ""
                                },
                                contentDescription = "Refresh"
                            )
                        }
                    }
                }

                // Select Image Button
                CustomButton(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isUploadingImage,
                    icon = ButtonIcon.Vector(Icons.Filled.Image),
                    strLabel = if (imagePublicId.isBlank()) "Select Image" else "Change Image"
                )

                if (imageUrlError) {
                    Text(
                        stringResource(R.string.image_error),
                        color = MaterialTheme.colorScheme.error,
                        style = windowSizeClass.labelTextStyle
                    )
                }

                CustomTextField(
                    value = redirectUrl,
                    onValueChange = { newValue ->
                        val cleanedValue = newValue.trim()
                        redirectUrl = cleanedValue
                        redirectUrlError = cleanedValue.isNotBlank() && !isValidUrl(cleanedValue)
                    },
                    label = R.string.redirect_url,
                    placeholder = R.string.redirect_url_placeholder,
                    singleLine = true,
                    isError = redirectUrlError,
                    errorMessage = if (redirectUrlError) "Invalid URL format" else "",
                    modifier = Modifier.fillMaxWidth()
                )
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
                label = R.string.add_carousel,
                onClick = {
                    titleError = title.isBlank()
                    descriptionError = description.isBlank()
                    imageUrlError = imagePublicId.isBlank()
                    redirectUrlError = redirectUrl.isNotBlank() && !isValidUrl(redirectUrl)

                    if (isFormValid) {
                        val cleanRedirectUrl = redirectUrl
                            .trim()
                            .takeIf { it.isNotBlank() }

                        onConfirm(title, description, imagePublicId, cleanRedirectUrl)
                    }
                },
                enabled = isFormValid
            )
        }
    )
}

/**
 * EditCarouselDialog - Dialog for updating existing carousel items
 *
 * Pre-fills the form with existing data and allows administrators to modify
 * any field, including replacing the carousel image.
 *
 * @param carousel The existing carousel item to edit
 * @param onDismiss Callback to close the dialog without saving
 * @param onConfirm Callback when updates are successfully committed
 * @param imageLoader Coil image loader for previews
 * @param cloudinaryHelper Helper for Cloudinary image operations
 * @param viewModel ViewModel to handle image upload logic
 */
@Composable
fun EditCarouselDialog(
    carousel: CarouselItem,
    onDismiss: () -> Unit,
    onConfirm: (CarouselItem) -> Unit,
    imageLoader: ImageLoader? = null,
    cloudinaryHelper: CloudinaryHelper = CloudinaryHelper(),
    viewModel: CarouselViewModel = hiltViewModel()
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    var title by remember { mutableStateOf(carousel.title) }
    var description by remember { mutableStateOf(carousel.description) }
    var imagePublicId by remember { mutableStateOf(carousel.imageUrl) }
    var redirectUrl by remember { mutableStateOf(carousel.redirectUrl ?: "") }

    //  Image upload state
    var isUploadingImage by remember { mutableStateOf(false) }

    //  Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            isUploadingImage = true

            viewModel.uploadCarouselImage(it, cloudinaryHelper) { publicId ->
                imagePublicId = publicId
                isUploadingImage = false
            }
        }
    }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Edit,
                contentDescription = "Edit carousel",
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Text(
                text = stringResource(R.string.edit_carousel),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(windowSizeClass.baseNormalVerticalPadding),
            ) {
                CustomTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = R.string.carousel_name,
                    placeholder = R.string.carousel_required,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                CustomTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = R.string.carousel_description,
                    placeholder = R.string.carousel_description_placeholder,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                //  Image Upload Section
                Text(
                    text = stringResource(R.string.carousel_image),
                    style = windowSizeClass.titleTextStyle,
                    color = MaterialTheme.colorScheme.primary
                )

                // Image Preview
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
                            isUploadingImage -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CustomCircularProgressIndicator()
                                    CustomSpacer(modifier = Modifier.height(windowSizeClass.normalVerticalPadding))
                                    Text(
                                        stringResource(R.string.uploading),
                                        style = windowSizeClass.bodyTextStyle
                                    )
                                }
                            }

                            imagePublicId.isNotBlank() -> {
                                CustomImageContainer(
                                    data = cloudinaryHelper.getImageUrl(imagePublicId),
                                    contentDescription = "Carousel image",
                                    imageLoader = imageLoader,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            else -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CustomIcon(
                                        icon = Icons.Filled.Image,
                                        contentDescription = "icon image",
                                        iconSize = windowSizeClass.largeIconSize,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                }
                            }
                        }
                    }
                }

                CustomButton(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploadingImage,
                    icon = ButtonIcon.Vector(Icons.Filled.Image),
                    strLabel = if (imagePublicId.isBlank()) "Select Image" else "Change Image"
                )

                CustomTextField(
                    value = redirectUrl,
                    onValueChange = { redirectUrl = it },
                    label = R.string.redirect_url,
                    placeholder = R.string.redirect_url_placeholder,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                label = R.string.edit_carousel,
                onClick = {
                    val updatedCarousel = carousel.copy(
                        title = title,
                        description = description,
                        imageUrl = imagePublicId,
                        redirectUrl = redirectUrl.ifBlank { null }
                    )
                    onConfirm(updatedCarousel)
                },
                enabled = title.isNotBlank() &&
                        description.isNotBlank() &&
                        imagePublicId.isNotBlank() &&
                        !isUploadingImage
            )
        }
    )
}

/**
 * DeleteCarouselDialog - Confirmation dialog for carousel deletion
 *
 * Displays a warning message before permanently removing a carousel item.
 *
 * @param carousel The carousel item to be deleted
 * @param onDismiss Callback to cancel the deletion
 * @param onConfirm Callback when deletion is confirmed
 */
@Composable
fun DeleteCarouselDialog(
    carousel: CarouselItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {

    val windowSizeClass = LocalWindowSizeConstant.current

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Warning,
                contentDescription = "Warning",
                tint = colors.orange,
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Text(
                text = stringResource(R.string.delete_carousel),
                style = windowSizeClass.titleTextStyle,
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete '${carousel.title}'? This action cannot be undone.",
                style = windowSizeClass.bodyTextStyle
            )
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
                onClick = onConfirm,
                label = R.string.delete_carousel,
                color = MaterialTheme.colorScheme.error,
            )
        }
    )
}
