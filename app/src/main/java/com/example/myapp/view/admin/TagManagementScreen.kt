package com.example.myapp.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.ProductTag
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.dataclass.TagCategory
import com.example.myapp.data.model.ColorViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.TagViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.admin.components.ColorPicker
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
 * TagManagementScreen - Admin screen for managing product tags.
 *
 * This screen allows administrators to view, create, edit, search, and delete product tags.
 * Tags are used to categorize and highlight products (e.g., "New Arrival", "Best Seller").
 * System tags (marked with a lock icon) are protected and cannot be deleted, but may be editable.
 *
 * ## Features
 * - **Tag System**: List of all available tags with color-coded previews.
 * - **Search Filter**: Real-time filtering of tags by name, display name, or description.
 * - **CRUD Operations**: Complete Create, Read, Update, Delete functionality.
 * - **Color Picker**: Integration with [ColorViewModel] to assign colors to tags.
 * - **System Tag Protection**: Visual indicators and safeguards for system-critical tags.
 * - **Floating Action Button**: Quick access to create new tags.
 *
 * ## Tag Properties
 * - **Name**: Internal identifier (lowercase, underscore_separated).
 * - **Display Name**: User-friendly name shown in the UI.
 * - **Description**: Brief explanation of the tag's purpose.
 * - **Color**: Visual indicator color for the tag.
 * - **Category**: Grouping for tags (e.g., Feature, Status).
 *
 * @param viewModel [TagViewModel] for handling tag data operations.
 * @param colorViewModel [ColorViewModel] for providing color options for tags.
 * @param onNavigateBack Callback to navigate back to the previous screen.
 */
@Composable
fun TagManagementScreen(
    viewModel: TagViewModel = hiltViewModel(),
    colorViewModel: ColorViewModel = hiltViewModel(),  
    onNavigateBack: () -> Unit,
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val tagState by viewModel.tagState.collectAsState()
    val networkState = rememberNetworkState(networkManager)
    val windowSize = LocalWindowSizeConstant.current

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSystemTagWarning by remember { mutableStateOf(false) }
    var selectedTag by remember { mutableStateOf<ProductTag?>(null) }

    val snackBarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    //  Filter unique tags
    val uniqueTags = remember(tagState.tags) {
        tagState.tags.distinctBy { it.name }
    }

    val displayedTags = remember(searchQuery, uniqueTags) {
        if (searchQuery.isBlank()) {
            uniqueTags
        } else {

            uniqueTags.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.displayName.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Handle snack bar data
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
        onRefresh = {
            if (networkState.hasInternet) {
                viewModel.loadAllTags()
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            } },
        title = R.string.manage_tags,
        snackBarHostState = snackBarHostState,
        showBottomBar = false,
        verticalArrangement = Arrangement.Top,
        onNavigateBack = { onNavigateBack() },
        floatingBtnContent = {
            CustomFloatingPointButton(
                onClick = { showAddDialog = true }
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
                                .padding(top = windowSize.baseSize),
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
                            if (newQuery.isNotEmpty()) {
                                viewModel.searchTags(newQuery)
                            } else {
                                viewModel.loadAllTags() // Load all when empty
                            }
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
                                stringResource(R.string.search_tags),
                                style = windowSize.bodyTextStyle,
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
                                    },
                                    contentDescription = "Clear Search"
                                )
                            }
                        }
                    )
                    CustomSpacer()

                when {
                    tagState.isLoading -> {
                        CustomSpacer(modifier = Modifier.height( windowSize.customSpacerSmall ))
                        CustomListCardShimmer()
                    }

                    tagState.error != null -> {
                        CustomEmptyState(
                            btnLabel = R.string.retry,
                            titleStr = tagState.error ?: "Error loading tags",
                            onBtnClick = { viewModel.loadAllTags() },
                            scrollState = rememberScrollState(),
                            leadingIcon = Icons.Filled.Error,
                        )
                    }

                    displayedTags.isEmpty() -> {
                        CustomEmptyState(
                            titleStr = if (searchQuery.isEmpty()) "No tags yet" else "No results found",
                            showBtn = false,
                            leadingIcon = Icons.Filled.SearchOff
                        )
                    }

                    else -> {
                        CustomLazyColumn {
                            item{
                                // Show count
                                Text(
                                    text = "${displayedTags.size} tag${if (displayedTags.size != 1) "s" else ""}",
                                    style = windowSize.bodyTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = windowSize.normalVerticalPadding)
                                )
                            }
                            items(
                                items = displayedTags,
                                key = { it.id }
                            ) { tag ->
                                TagManagementItemCard(
                                    tag = tag,
                                    onEdit = {
                                        selectedTag = tag
                                        showEditDialog = true
                                    },
                                    onDelete = {
                                        if (tag.isSystemTag) {
                                            showSystemTagWarning = true
                                        } else {
                                            selectedTag = tag
                                            showDeleteDialog = true
                                        }
                                    }
                                )
                            }

                            item {
                                CustomSpacer(modifier = Modifier.height( windowSize.customSpacerSmall))
                            }
                        }
                    }
                }
            })
        },
        actions = {
            if (tagState.isLoading) {
                TopBarActionsShimmer()
            } else {
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Refresh),
                    onClick = { viewModel.loadAllTags() },
                    contentDescription = "Refresh"
                )
            }
        }
    )

    //   Dialogs - pass colorViewModel
    if (showAddDialog) {
        AddEditTagDialog(
            tag = null,
            onDismiss = { showAddDialog = false },
            onSave = { tag ->
                viewModel.createTag(tag)
                showAddDialog = false
            },
            colorViewModel = colorViewModel
        )
    }

    if (showEditDialog && selectedTag != null) {
        AddEditTagDialog(
            tag = selectedTag,
            onDismiss = {
                showEditDialog = false
                selectedTag = null
            },
            onSave = { tag ->
                viewModel.updateTag(tag)
                showEditDialog = false
                selectedTag = null
            },
            colorViewModel = colorViewModel
        )
    }

    if (showDeleteDialog && selectedTag != null) {
        DeleteTagDialog(
            tag = selectedTag!!,
            onDismiss = {
                showDeleteDialog = false
                selectedTag = null
            },
            onConfirm = {
                viewModel.deleteTag(selectedTag!!.id)
                showDeleteDialog = false
                selectedTag = null
            }
        )
    }

    if (showSystemTagWarning) {
        SystemTagWarningDialog(
            onDismiss = { showSystemTagWarning = false }
        )
    }
}

// ============================================================================
//  ADD/EDIT TAG DIALOG
// ============================================================================

/**
 * AddEditTagDialog - Form for creating or modifying product tags
 * 
 * Includes fields for tag name, description, category, and a color picker.
 * 
 * @param tag The tag to edit, or null if creating a new tag
 * @param onDismiss Callback to close the dialog
 * @param onSave Callback with the configured tag data
 * @param colorViewModel ViewModel for providing color options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTagDialog(
    tag: ProductTag?,
    onDismiss: () -> Unit,
    onSave: (ProductTag) -> Unit,
    colorViewModel: ColorViewModel = hiltViewModel()
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    var displayName by remember { mutableStateOf(tag?.displayName ?: "") }
    var description by remember { mutableStateOf(tag?.description ?: "") }
    var selectedColor by remember { mutableStateOf(tag?.color ?: "") }
    var selectedCategory by remember { mutableStateOf(tag?.category ?: TagCategory.FEATURE) }

    // Validation state
    var displayNameError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }

    // Set default color when colors are loaded
    val colorState by colorViewModel.colorState.collectAsState()
    LaunchedEffect(colorState.colors) {
        if (selectedColor.isBlank() && colorState.colors.isNotEmpty()) {
            selectedColor = colorState.colors.first().hexCode
        }
    }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Tag,
                contentDescription = "Add tag",
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Text(
                text = if (tag == null) "Add New Tag" else "Edit Tag",
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(windowSizeClass.baseNormalVerticalPadding)
            ) {
                // Display Name
                CustomTextField(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        displayNameError = it.isBlank() || it.length < 2
                    },
                    label = R.string.tag_name,
                    placeholder = R.string.tag_placeholder,
                    isError = displayNameError,
                    errorMessage = when {
                        displayName.isBlank() -> "Tag name is required"
                        displayName.length < 2 -> "Name must be at least 2 characters"
                        else -> ""
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                CustomTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        descriptionError = it.isBlank()
                    },
                    label = R.string.description,
                    placeholder = R.string.tag_description_placeholder,
                    isError = descriptionError,
                    errorMessage = if (descriptionError) "Description is required" else "",
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Category Dropdown
                var categoryExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    CustomTextField(
                        value = selectedCategory.name,
                        onValueChange = {},
                        label = R.string.category,
                        placeholder = R.string.select_category,
                        readOnly = true,
                        trailingIconContent = {
                            CustomIcon(
                                icon = if (categoryExpanded)
                                    Icons.Default.ArrowDropUp
                                else
                                    Icons.Default.ArrowDropDown,
                                contentDescription = "Select category"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        TagCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(
                                    text = category.name,
                                    style = windowSizeClass.bodyTextStyle)},
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                //  Reusable ColorPicker Component
                ColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it },
                    colorViewModel = colorViewModel,
                    showLabel = true,
                    multiSelect = false,
                    modifier = Modifier.fillMaxWidth()
                )

                // System tag warning
                if (tag?.isSystemTag == true) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(windowSizeClass.baseNormalVerticalPadding),
                            horizontalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomIcon(
                                icon = Icons.Filled.Warning,
                                contentDescription = "Warning",
                                tint = colors.orange,
                            )

                            Text(
                                text = stringResource(R.string.tag_caution_warning),
                                style = windowSizeClass.labelTextStyle,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
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
                    // Validation
                    if (displayName.isBlank() || displayName.length < 2) {
                        displayNameError = true
                        return@CustomTextButton
                    }

                    if (description.isBlank()) {
                        descriptionError = true
                        return@CustomTextButton
                    }

                    if (selectedColor.isBlank()) {
                        return@CustomTextButton
                    }

                    val newTag = ProductTag(
                        id = tag?.id ?: "",
                        name = displayName.lowercase().replace(" ", "_"),
                        displayName = displayName.trim(),
                        description = description.trim(),
                        color = selectedColor,
                        category = selectedCategory,
                        isSystemTag = tag?.isSystemTag ?: false
                    )
                    onSave(newTag)
                },
                label = if (tag == null) R.string.create_tag else R.string.update_tag,
                enabled = !displayNameError &&
                        !descriptionError &&
                        displayName.isNotBlank() &&
                        description.isNotBlank() &&
                        selectedColor.isNotBlank()
            )
        }
    )
}

// ============================================================================
// DELETE TAG CONFIRMATION DIALOG
// ============================================================================

/**
 * DeleteTagDialog - Confirmation for tag removal
 * 
 * Displays a warning along with a preview of the tag to be deleted.
 * 
 * @param tag The tag to be deleted
 * @param onDismiss Callback to cancel the operation
 * @param onConfirm Callback to confirm deletion
 */
@Composable
fun DeleteTagDialog(
    tag: ProductTag,
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
                stringResource(R.string.delete_tag),
                style = windowSizeClass.titleTextStyle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.delete_tag_prompt),
                    style = windowSizeClass.bodyTextStyle
                )

                CustomSpacer()

                // Tag Preview
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(
                            tag.color.toColorInt()
                        ).copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(windowSizeClass.baseNormalVerticalPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(windowSizeClass.baseNormalVerticalPadding)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(windowSizeClass.customSpacerSmall)
                                .background(
                                    Color(tag.color.toColorInt()),
                                    CircleShape
                                )
                        )

                        Column {
                            Text(
                                tag.displayName,
                                style = windowSizeClass.bodyTextStyle,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                tag.name,
                                style = windowSizeClass.labelTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                CustomSpacer()

                // Warning message
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(windowSizeClass.baseNormalVerticalPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding)
                    ) {
                        CustomIcon(
                            icon =Icons.Filled.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            stringResource(R.string.tag_caution),
                            style = windowSizeClass.labelTextStyle
                        )
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
                onClick = onConfirm,
                label = R.string.delete_tag,
                color = MaterialTheme.colorScheme.error,
            )
        }
    )
}

// ============================================================================
// SYSTEM TAG WARNING DIALOG
// ============================================================================

/**
 * SystemTagWarningDialog - Informational dialog for protected tags
 * 
 * Explains why certain tags cannot be modified or deleted and lists examples.
 * 
 * @param onDismiss Callback to close the dialog
 */
@Composable
fun SystemTagWarningDialog(
    onDismiss: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Lock,
                contentDescription = "System warning tag",
                iconSize = windowSizeClass.largeIconSize,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                stringResource(R.string.system_tag),
                style = windowSizeClass.titleTextStyle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.system_tag_warning),
                    style = windowSizeClass.bodyTextStyle
                )

                CustomSpacer( )

                Text(
                    stringResource(R.string.system_tag_include),
                    style = windowSizeClass.labelTextStyle,
                    fontWeight = FontWeight.Medium
                )

                CustomSpacer()

                listOf(
                    "prime_eligible - For Prime member benefits",
                    "new_arrival - Recently added products",
                    "trending - Popular products",
                    "limited_edition - Limited availability",
                    "eco_friendly - Sustainable products"
                ).forEach { description ->
                    Row(
                        modifier = Modifier.padding(vertical = windowSizeClass.smallVerticalPadding),
                        horizontalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding)
                    ) {
                        CustomIcon(
                            icon = Icons.Filled.Check,
                            contentDescription = "Check",
                            iconSize = windowSizeClass.basePadding
                        )

                        Text(
                            description,
                            style =  windowSizeClass.bodyTextStyle
                        )
                    }
                }
            }
        },
        confirmButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.got_it
            )
        }
    )
}

/**
 * TagManagementItemCard - Summary view and management actions for a single tag
 * 
 * @param tag The tag data to display
 * @param onEdit Callback when edit button is clicked
 * @param onDelete Callback when delete button is clicked
 */
@Composable
private fun TagManagementItemCard(
    tag: ProductTag,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(customSpacing.custom16),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(windowSizeClass.baseNormalVerticalPadding),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(customSpacing.custom40)
                        .background(
                            Color(tag.color.toColorInt()),
                            CircleShape
                        )
                ) {
                    if (tag.isSystemTag) {
                        CustomIcon(
                            icon =  Icons.Filled.Lock,
                            contentDescription = "Lock",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }
                }

                Column {
                    Text(
                        tag.displayName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = windowSizeClass.bodyTextStyle,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        tag.name,
                        style = windowSizeClass.bodyTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        tag.category.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = windowSizeClass.bodyTextStyle
                    )
                }
            }

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

