package com.example.myapp.view.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.BrandItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.BrandViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomAlertDialog
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
import com.example.myapp.view.components.TopBarActionsShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.ButtonIcon
import kotlinx.coroutines.delay

/**
 * BrandManagementScreen - Brand CRUD operations
 *
 * Manage product brands with full create, read, update, and delete functionality.
 *
 * ## Features
 * - **Brand List**: Display all brands
 * - **Search**: Find brands by name
 * - **Add Brand**: Create new brands via dialog
 * - **Edit Brand**: Modify brand names
 * - **Delete Brand**: Remove brands with confirmation
 * - **Floating Action Button**: Quick access to add new brand
 *
 * ## User Workflow
 * 1. View list of all brands
 * 2. Use search bar to find specific brands
 * 3. Click FAB (+) to add new brand
 * 4. Click brand card to edit
 * 5. Click edit icon to modify brand name
 * 6. Click delete icon to remove brand (with confirmation)
 * 7. Pull down to refresh brand list
 *
 * ## Loading States
 * - Shows shimmer placeholders while loading
 * - Displays error state with retry button on failure
 * - Shows empty state when no brands exist
 *
 * @param viewModel ViewModel for brand operations
 * @param onNavigateBack Callback for back navigation
 *
 * @see BrandViewModel for brand data operations
 * @see BrandCard for individual brand display
 * @see AddBrandDialog for creation dialog
 * @see EditBrandDialog for editing dialog
 * @see DeleteBrandDialog for delete confirmation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandManagementScreen(
    viewModel: BrandViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val brandState by viewModel.brandState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val networkState = rememberNetworkState(networkManager)

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedBrand by remember { mutableStateOf<BrandItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }

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
        onRefresh = {
            if (networkState.hasInternet) {
                viewModel.loadBrands()
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }
        },
        title = R.string.manage_brand,
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
                            // Optional: Add debounce for real-time search
                            if (newQuery.isNotEmpty()) {
                                viewModel.searchBrands(newQuery)
                            } else {
                                viewModel.loadBrands() // Load all when empty
                            }
                        },
                        onSearch = { query ->
                            viewModel.searchBrands(query)
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
                                stringResource(R.string.search_brands),
                                style = windowSizeClass.bodyTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                ButtonIconComposable(
                                    showBgColor = false,
                                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Search),
                                    onClick = {
                                        searchQuery = ""
                                        viewModel.loadBrands()
                                    },
                                    contentDescription = "Search"
                                )
                            }
                        }
                    )
                    CustomSpacer()

                    when {
                        brandState.isLoading -> {
                            CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))

                            CustomListCardShimmer()
                        }

                        brandState.error != null -> {
                            // Error State
                            CustomEmptyState(
                                btnLabel = R.string.retry,
                                title = R.string.brand_error,
                                onBtnClick = { viewModel.loadBrands() },
                                scrollState = rememberScrollState(),
                                leadingIcon = Icons.Filled.Error,
                            )
                        }

                        brandState.brands.isEmpty() -> {
                            // Error state
                            CustomEmptyState(
                                titleStr = if (searchQuery.isEmpty()) "No brands yet" else "No results found",
                                showBtn = false,
                                leadingIcon = Icons.Filled.SearchOff
                            )
                        }

                        else -> {
                            CustomLazyColumn {

                                items(brandState.brands) { brand ->
                                    BrandCard(
                                        brand = brand,
                                        onEdit = {
                                            selectedBrand = brand
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            selectedBrand = brand
                                            showDeleteDialog = true
                                        }
                                    )
                                }

                                // Add bottom padding
                                item {
                                    CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerMedium))
                                }
                            }
                        }
                    }
                }
            )

            // Dialogs
            if (showAddDialog) {
                AddBrandDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { brandName ->
                        viewModel.createBrand(brandName)
                        showAddDialog = false
                    }
                )
            }

            if (showEditDialog && selectedBrand != null) {
                EditBrandDialog(
                    brand = selectedBrand!!,
                    onDismiss = {
                        showEditDialog = false
                        selectedBrand = null
                    },
                    onConfirm = { brandName ->
                        viewModel.updateBrand(selectedBrand!!.id, brandName)
                        showEditDialog = false
                        selectedBrand = null
                    }
                )
            }

            if (showDeleteDialog && selectedBrand != null) {
                DeleteBrandDialog(
                    brand = selectedBrand!!,
                    onDismiss = {
                        showDeleteDialog = false
                        selectedBrand = null
                    },
                    onConfirm = {
                        viewModel.deleteBrand(selectedBrand!!.id, selectedBrand!!.brandName)
                        showDeleteDialog = false
                        selectedBrand = null
                    }
                )
            }
        },
        actions = {
            if (brandState.isLoading) {
                TopBarActionsShimmer()
            } else {
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Refresh),
                    onClick = { viewModel.loadBrands() },
                    contentDescription = "Refresh"
                )
            }
        }
    )
}

/**
 * BrandCard - Individual brand display card
 *
 * Displays a single brand with edit and delete actions.
 *
 * @param brand The brand data to display
 * @param onEdit Callback when edit is clicked
 * @param onDelete Callback when delete is clicked
 */
@Composable
fun BrandCard(
    brand: BrandItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Card(
        modifier = windowSizeClass.adaptiveWidthModifier
            .clickable(onClick = onEdit)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeClass.basePadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                CustomIcon(
                    icon = Icons.Filled.Storefront,
                    contentDescription = "Store front",
                    tint = MaterialTheme.colorScheme.primary
                )

                CustomSpacer(modifier = Modifier.width(windowSizeClass.baseNormalVerticalPadding))

                Text(
                    text = brand.brandName,
                    style = windowSizeClass.bodyTextStyle,
                    fontWeight = FontWeight.Medium
                )
            }

            // Action Buttons
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

/**
 * AddBrandDialog - Dialog for creating new brands
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when brand is created, receives brand name
 */
@Composable
fun AddBrandDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    var brandName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Add,
                contentDescription = "Add brand",
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Text(
                stringResource(R.string.add_new_brand),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Column {
                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.brand_name,
                    placeholder = R.string.brand_name,
                    value = brandName,
                    onValueChange = {
                        brandName = it
                        isError = it.isBlank()
                    },
                    isError = isError,
                    errorMessage = if (isError) stringResource(R.string.brand_empty_state) else "",
                )
            }
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
                label = R.string.add_brand,
                onClick = {
                    if (brandName.isNotBlank()) {
                        onConfirm(brandName)
                    } else {
                        isError = true
                    }
                }
            )
        }
    )
}

/**
 * EditBrandDialog - Dialog for editing existing brands
 *
 * @param brand The brand to edit
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when brand is updated, receives new brand name
 */
@Composable
fun EditBrandDialog(
    brand: BrandItem,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    var brandName by remember { mutableStateOf(brand.brandName) }
    var isError by remember { mutableStateOf(false) }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Edit,
                contentDescription = "Edit brand",
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Text(
                stringResource(R.string.edit_brand),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Column {
                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.brand_name,
                    placeholder = R.string.brand_name,
                    value = brandName,
                    onValueChange = {
                        brandName = it
                        isError = it.isBlank()
                    },
                    isError = isError,
                    errorMessage = if (isError) stringResource(R.string.brand_empty_state) else "",
                )
            }
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
                label = R.string.edit_brand,
                onClick = {
                    if (brandName.isNotBlank()) {
                        onConfirm(brandName)
                    } else {
                        isError = true
                    }
                }
            )
        }
    )
}

/**
 * DeleteBrandDialog - Brand deletion confirmation dialog
 *
 * @param brand The brand to delete
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when deletion is confirmed
 */
@Composable
fun DeleteBrandDialog(
    brand: BrandItem,
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
                stringResource(R.string.delete_brand),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Text(
                "Are you sure you want to delete '${brand.brandName}'? This action cannot be undone.",
                style = windowSizeClass.bodyTextStyle
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
                onClick = onConfirm,
                label = R.string.delete_brand,
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}