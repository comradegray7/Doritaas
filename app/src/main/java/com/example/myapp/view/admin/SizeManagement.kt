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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
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
import com.example.myapp.data.dataclass.SizeItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.SizeViewModel
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
 * SizeManagementScreen - Size attribute CRUD operations
 *
 * Manage product size options (XS, S, M, L, XL, etc.) with full CRUD functionality.
 *
 * ## Features
 * - **Size List**: Display all available sizes
 * - **Search**: Find sizes by name
 * - **Add Size**: Create new size options via dialog
 * - **Edit Size**: Modify size labels
 * - **Delete Size**: Remove sizes with confirmation
 * - **Floating Action Button**: Quick access to add new size
 *
 * ## User Workflow
 * 1. View list of all sizes
 * 2. Use search bar to find specific sizes
 * 3. Click FAB (+) to add new size
 * 4. Click size card to edit
 * 5. Click edit icon to modify size
 * 6. Click delete icon to remove size (with confirmation)
 * 7. Pull down to refresh size list
 *
 * ## Loading States
 * - Shows shimmer placeholders while loading
 * - Displays error state with retry button on failure
 * - Shows empty state when no sizes exist
 *
 * @param viewModel ViewModel for size operations
 * @param onNavigateBack Callback for back navigation
 *
 * @see SizeViewModel for size data operations
 * @see SizeCard for individual size display
 * @see AddSizeDialog for creation dialog
 * @see EditSizeDialog for editing dialog
 * @see DeleteSizeDialog for delete confirmation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizeManagementScreen(
    viewModel: SizeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val sizeState by viewModel.sizeState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val networkState = rememberNetworkState(networkManager)

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedSize by remember { mutableStateOf<SizeItem?>(null) }
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
                viewModel.loadSizes()
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }
        },
        verticalArrangement = Arrangement.Top,
        snackBarHostState = snackBarHostState,
        showBottomBar = false,
        title = R.string.manage_sizes,
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
                                .padding(top = windowSizeConstant.baseSize),
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
                                viewModel.searchSizes(newQuery)
                            } else {
                                viewModel.loadSizes() // Load all when empty
                            }
                        },
                        onSearch = { query ->
                            viewModel.searchSizes(query)
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
                                stringResource(R.string.search_sizes),
                                style = windowSizeConstant.bodyTextStyle,
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
                                        viewModel.loadSizes()
                                    },
                                    contentDescription = "Search"
                                )
                            }
                        }
                    )
                    CustomSpacer()
                    when {
                        sizeState.isLoading -> {
                            CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall))

                            CustomListCardShimmer()

                        }

                        sizeState.error != null -> {
                            // Error State
                            CustomEmptyState(
                                btnLabel = R.string.retry,
                                title = R.string.size_error,
                                onBtnClick = { viewModel.loadSizes() },
                                btnIcon = Icons.Filled.Error,
                            )
                        }

                        sizeState.sizes.isEmpty() -> {
                            // Empty State
                            CustomEmptyState(
                                titleStr = if (searchQuery.isEmpty()) "No sizes yet" else "No results found",
                                showBtn = false,
                                leadingIcon = Icons.Filled.SearchOff,
                            )
                        }

                        else -> {

                            CustomLazyColumn {

                                items(sizeState.sizes) { size ->
                                    SizeCard(
                                        size = size,
                                        onEdit = {
                                            selectedSize = size
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            selectedSize = size
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                                item {
                                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall))
                                }
                            }
                        }
                    }

                }
            )

            // Dialogs
            if (showAddDialog) {
                AddSizeDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { size ->
                        viewModel.createSize(size)
                        showAddDialog = false
                    }
                )
            }

            if (showEditDialog && selectedSize != null) {
                EditSizeDialog(
                    size = selectedSize!!,
                    onDismiss = {
                        showEditDialog = false
                        selectedSize = null
                    },
                    onConfirm = { size ->
                        viewModel.updateSize(selectedSize!!.id, size)
                        showEditDialog = false
                        selectedSize = null
                    }
                )
            }

            if (showDeleteDialog && selectedSize != null) {
                DeleteSizeDialog(
                    size = selectedSize!!,
                    onDismiss = {
                        showDeleteDialog = false
                        selectedSize = null
                    },
                    onConfirm = {
                        viewModel.deleteSize(selectedSize!!.id, selectedSize!!.size)
                        showDeleteDialog = false
                        selectedSize = null
                    }
                )
            }
        },
        actions = {
            if (sizeState.isLoading) {
                TopBarActionsShimmer()
            } else {
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Refresh),
                    onClick = { viewModel.loadSizes() },
                    contentDescription = "Refresh"
                )
            }
        }
    )
}

/**
 * SizeCard - Individual size display card
 *
 * Displays a single size option with edit and delete actions.
 *
 * @param size The size data to display
 * @param onEdit Callback when edit is clicked
 * @param onDelete Callback when delete is clicked
 */
@Composable
fun SizeCard(
    size: SizeItem,
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
                .padding(windowSizeClass.baseVerticalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                CustomIcon(
                    icon = Icons.Filled.FormatSize,
                    contentDescription = "Format size",
                    tint = MaterialTheme.colorScheme.primary
                )

                CustomSpacer(modifier = Modifier.width(windowSizeClass.baseNormalVerticalPadding))

                Text(
                    text = size.size,
                    style = windowSizeClass.bodyTextStyle,
                    fontWeight = FontWeight.Medium
                )
            }

            Row {
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
 * AddSizeDialog - Dialog for creating new sizes
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when size is created, receives size label
 */
@Composable
fun AddSizeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var size by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.FormatSize,
                contentDescription = "Add size",
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Text(
                "Add New Size",
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Column {
                // *** CustomTextField Used Here ***
                CustomTextField(
                    value = size,
                    onValueChange = {
                        size = it
                        isError = it.isBlank()
                    },
                    label = R.string.size, // Assuming R.string.size exists
                    placeholder = R.string.size_placeholder, // Assuming R.string.size_placeholder exists
                    isError = isError,
                    errorMessage = if (isError) stringResource(R.string.size_empty_state) else "", // Assuming R.string.size_empty_state exists
                    modifier = Modifier.fillMaxWidth()
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
                onClick = {
                    if (size.isNotBlank()) {
                        onConfirm(size)
                    } else {
                        isError = true
                    }
                },
                label = R.string.add_size
            )
        },
    )
}

/**
 * EditSizeDialog - Dialog for editing existing sizes
 *
 * @param size The size to edit
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when size is updated, receives new size label
 */
@Composable
fun EditSizeDialog(
    size: SizeItem,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var sizeValue by remember { mutableStateOf(size.size) }
    var isError by remember { mutableStateOf(false) }
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.FormatSize,
                contentDescription = "Edit size",
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Text(
                stringResource(R.string.edit_size),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Column {
                CustomTextField(
                    value = sizeValue,
                    onValueChange = {
                        sizeValue = it
                        isError = it.isBlank()
                    },
                    label = R.string.size, // Assuming R.string.size exists
                    placeholder = R.string.edit_size,
                    isError = isError,
                    errorMessage = if (isError) stringResource(R.string.size_empty_state) else "", // Assuming R.string.size_empty_state exists
                    modifier = Modifier.fillMaxWidth()
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
                onClick = {
                    if (sizeValue.isNotBlank()) {
                        onConfirm(sizeValue)
                    } else {
                        isError = true
                    }
                },
                label = R.string.edit_size
            )
        }
    )
}

/**
 * DeleteSizeDialog - Size deletion confirmation dialog
 *
 * @param size The size to delete
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when deletion is confirmed
 */
@Composable
fun DeleteSizeDialog(
    size: SizeItem,
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
                stringResource(R.string.delete_size),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Text(
                "Are you sure you want to delete size '${size.size}'? This action cannot be undone.",
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
                label = R.string.delete_size,
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}