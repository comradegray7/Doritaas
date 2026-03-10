package com.example.myapp.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.ColorItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.ColorViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
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
import com.example.myapp.view.utils.CustomShape
import kotlinx.coroutines.delay

/**
 * ColorManagementScreen - Color attribute management
 *
 * Manage product color options with names and hex codes, including visual color previews.
 *
 * ## Features
 * - **Color List**: Display all colors with visual swatches
 * - **Search**: Find colors by name
 * - **Add Color**: Create new colors with name and hex code
 * - **Edit Color**: Modify color details
 * - **Delete Color**: Remove colors with confirmation
 * - **Color Preview**: Visual representation of hex codes
 * - **Floating Action Button**: Quick access to add new color
 *
 * ## Color Data
 * Each color includes:
 * - Name (e.g., "Red", "Navy Blue")
 * - Hex code (e.g., "#FF0000", "#000080")
 * - Visual preview circle
 *
 * ## User Workflow
 * 1. View list of all colors with previews
 * 2. Use search bar to find specific colors
 * 3. Click FAB (+) to add new color
 * 4. Enter color name and hex code
 * 5. Preview color before saving
 * 6. Click color card to edit
 * 7. Click delete icon to remove color (with confirmation)
 * 8. Pull down to refresh color list
 *
 * ## Loading States
 * - Shows shimmer placeholders while loading
 * - Displays error state with retry button on failure
 * - Shows empty state when no colors exist
 *
 * @param viewModel ViewModel for color operations
 * @param onNavigateBack Callback for back navigation
 *
 * @see ColorViewModel for color data operations
 * @see ColorCard for individual color display
 * @see AddColorDialog for creation dialog with preview
 * @see EditColorDialog for editing dialog with preview
 * @see DeleteColorDialog for delete confirmation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorManagementScreen(
    viewModel: ColorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    val colorState by viewModel.colorState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf<ColorItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val snackBarHostState = remember { SnackbarHostState() }
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
        onRefresh = {
            if (networkState.hasInternet) {
                viewModel.loadColors()
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }
        },
        title = R.string.manage_colors,
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
                    // searchbar
                    CustomSpacer()
                    CustomSearchBar(
                        query = searchQuery,
                        onQueryChange = { newQuery ->
                            searchQuery = newQuery
                            // Optional: Add debounce for real-time search
                            if (newQuery.isNotEmpty()) {
                                viewModel.searchColors(newQuery)
                            } else {
                                viewModel.loadColors() // Load all when empty
                            }
                        },
                        onSearch = { query ->
                            viewModel.searchColors(query)
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
                                text = stringResource(R.string.search_colors),
                                style = windowSizeClass.bodyTextStyle
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                ButtonIconComposable(
                                    showBgColor = false,
                                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Search),
                                    onClick = {
                                        searchQuery = ""
                                        viewModel.loadColors()
                                    },
                                    contentDescription = "Search"
                                )
                            }
                        }
                    )
                    CustomSpacer()

                    // Single condition check structure
                    when {
                        colorState.isLoading -> {
                            // Loading State
                            CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))

                            CustomListCardShimmer()
                        }

                        colorState.error != null -> {
                            // Error State
                            CustomEmptyState(
                                btnLabel = R.string.retry,
                                title = R.string.color_error,
                                onBtnClick = { viewModel.loadColors() },
                                btnIcon = Icons.Filled.Error,
                            )
                        }

                        colorState.colors.isEmpty() -> {
                            // Empty State
                            CustomEmptyState(
                                titleStr = if (searchQuery.isEmpty()) "No colors yet" else "No results found",
                                showBtn = false,
                                leadingIcon = Icons.Filled.SearchOff,
                            )
                        }

                        else -> {
                            // Colors List
                            CustomLazyColumn {

                                items(items = colorState.colors) { color ->
                                    ColorCard(
                                        color = color,
                                        onEdit = {
                                            selectedColor = color
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            selectedColor = color
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
                }
            )

            // Dialogs (keep these outside PaddedSection)
            if (showAddDialog) {
                AddColorDialog(
                    viewModel = viewModel,
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, hexCode ->
                        viewModel.createColor(name, hexCode)
                        showAddDialog = false
                    }
                )
            }

            if (showEditDialog && selectedColor != null) {
                EditColorDialog(
                    color = selectedColor!!,
                    onDismiss = {
                        showEditDialog = false
                        selectedColor = null
                    },
                    onConfirm = { name, hexCode ->
                        viewModel.updateColor(selectedColor!!.id, name, hexCode)
                        showEditDialog = false
                        selectedColor = null
                    }
                )
            }

            if (showDeleteDialog && selectedColor != null) {
                DeleteColorDialog(
                    color = selectedColor!!,
                    onDismiss = {
                        showDeleteDialog = false
                        selectedColor = null
                    },
                    onConfirm = {
                        viewModel.deleteColor(
                            selectedColor!!.id,
                            selectedColor!!.name,
                            selectedColor!!.hexCode
                        )
                        showDeleteDialog = false
                        selectedColor = null
                    }
                )
            }
        },
        actions = {
            if (colorState.isLoading) {
                TopBarActionsShimmer()
            } else {
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Refresh),
                    onClick = { viewModel.loadColors() },
                    contentDescription = "Refresh"
                )
            }
        }
    )
}

/**
 * ColorCard - Individual color display card with preview
 *
 * Displays a color with circular preview swatch, name, and hex code.
 *
 * @param color The color data to display
 * @param onEdit Callback when edit is clicked
 * @param onDelete Callback when delete is clicked
 */
@Composable
fun ColorCard(
    color: ColorItem,
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
                .padding(windowSizeClass.baseNormalVerticalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Color Preview
                Box(
                    modifier = Modifier
                        .size(windowSizeClass.customSpacerSmall)
                        .background(
                            color = try {
                                Color(color.hexCode.toColorInt())
                            } catch (_: Exception) {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = CustomShape.circleShape()
                        )
                        .border(
                            customSpacing.customHalf,
                            MaterialTheme.colorScheme.outline,
                            CustomShape.circleShape()
                        )
                )

                CustomSpacer(modifier = Modifier.width(windowSizeClass.baseNormalVerticalPadding))

                Column {
                    Text(
                        text = color.name,
                        style = windowSizeClass.bodyTextStyle,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = color.hexCode,
                        style = windowSizeClass.bodyTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action Buttons
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
 * AddColorDialog - Dialog for creating new colors
 *
 * Includes color name input, hex code input, and live color preview.
 *
 * @param viewModel ViewModel for color state
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when color is created, receives name and hex code
 */
@Composable
fun AddColorDialog(
    viewModel: ColorViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    val colorState by viewModel.colorState.collectAsState()
    var colorName by remember { mutableStateOf("") }
    var colorHexCode by remember { mutableStateOf("") }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Add,
                contentDescription = "Add color",
                iconSize = windowSizeClass.largeIconSize,
            )
        },
        title = {
            Text(
                stringResource(R.string.add_color),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(windowSizeClass.baseNormalVerticalPadding)
            ) {
                CustomTextField(
                    label = R.string.color_name,
                    placeholder = R.string.enter_color_name,
                    value = colorName,
                    onValueChange = {
                        colorName = it
                        if (colorState.error != null) viewModel.clearError()
                    },
                    isError = colorName.isEmpty(),
                    errorMessage = if (colorName.isEmpty()) "Color name is required" else "",
                    enabled = !colorState.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                CustomTextField(
                    label = R.string.hex_code,
                    placeholder = R.string.enter_hexCode,
                    value = colorHexCode,
                    onValueChange = {
                        colorHexCode = it
                        if (colorState.error != null) viewModel.clearError()
                    },
                    isError = colorHexCode.isEmpty(),
                    errorMessage = if (colorHexCode.isEmpty()) "Hex code is required" else "Format: #RRGGBB",
                    enabled = !colorState.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                // Color Preview
                if (colorHexCode.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = windowSizeClass.normalVerticalPadding)
                    ) {
                        Text("Preview: ", style = windowSizeClass.labelTextStyle)
                        Box(
                            modifier = Modifier
                                .size(windowSizeClass.baseSize)
                                .background(
                                    color = try {
                                        Color(colorHexCode.toColorInt())
                                    } catch (_: Exception) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = CustomShape.circleShape()
                                )
                                .border(
                                    customSpacing.customHalf,
                                    MaterialTheme.colorScheme.outline,
                                    CustomShape.circleShape()
                                )
                        )
                    }
                }
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
                label = R.string.add_color,
                onClick = {
                    if (colorName.isNotBlank() && colorHexCode.isNotBlank()) {
                        onConfirm(colorName, colorHexCode)
                    }
                }
            )
        }
    )
}

/**
 * EditColorDialog - Dialog for editing existing colors
 *
 * Pre-filled with current color data, includes live preview.
 *
 * @param color The color to edit
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when color is updated, receives new name and hex code
 */
@Composable
fun EditColorDialog(
    color: ColorItem,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    var colorName by remember { mutableStateOf(color.name) }
    var colorHexCode by remember { mutableStateOf(color.hexCode) }
    var isError by remember { mutableStateOf(false) }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Edit,
                contentDescription = "Edit color",
                iconSize = windowSizeClass.largeIconSize,
            )
        },
        title = {
            Text(
                stringResource(R.string.edit_color),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding)
            ) {
                CustomTextField(
                    label = R.string.color_name,
                    placeholder = R.string.enter_color_name,
                    value = colorName,
                    onValueChange = {
                        colorName = it
                        isError = it.isBlank()
                    },
                    isError = isError,
                    errorMessage = if (colorName.isEmpty()) "Color name is required" else "",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                CustomTextField(
                    label = R.string.hex_code,
                    placeholder = R.string.enter_hexCode,
                    value = colorHexCode,
                    onValueChange = {
                        colorHexCode = it
                        isError = it.isBlank()
                    },
                    isError = isError,
                    errorMessage = if (colorHexCode.isEmpty()) "Hex code is required" else "Format: #RRGGBB",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                // Color Preview
                if (colorHexCode.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = windowSizeClass.baseVerticalPadding)
                    ) {
                        Text("Preview: ", style = windowSizeClass.labelTextStyle)
                        Box(
                            modifier = Modifier
                                .size(windowSizeClass.baseSize)
                                .background(
                                    color = try {
                                        Color(colorHexCode.toColorInt())
                                    } catch (_: Exception) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = CustomShape.extraLargeShape()
                                )
                                .border(
                                    customSpacing.customHalf,
                                    MaterialTheme.colorScheme.outline,
                                    CustomShape.extraLargeShape()
                                )
                        )
                    }
                }
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
                label = R.string.edit_color,
                onClick = {
                    if (colorName.isNotBlank() && colorHexCode.isNotBlank()) {
                        onConfirm(colorName, colorHexCode)
                    }
                }
            )
        }
    )
}

/**
 * DeleteColorDialog - Color deletion confirmation dialog
 *
 * @param color The color to delete
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when deletion is confirmed
 */
@Composable
fun DeleteColorDialog(
    color: ColorItem,
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
                text = stringResource(R.string.delete_color),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete color '${color.name}' (${color.hexCode})? This action cannot be undone.",
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
                label = R.string.delete_color,
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}