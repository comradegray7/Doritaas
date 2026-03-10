package com.example.myapp.view.admin

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import com.example.myapp.data.dataclass.CategoryItem
import com.example.myapp.data.dataclass.CategoryNode
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.CategoryViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomFilterChip
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
 * CategoryManagementScreen - Management interface for hierarchical product categories
 *
 * Supports full CRUD operations for product categories including recursive subcategory
 * management. Administrators can view categories in a tree or flat list format.
 *
 * ## Features
 * - **Dual View Modes**: Switch between hierarchical "Tree" and flat "List" representations
 * - **Recursive Hierarchy**: Unlimited nesting support for categories/subcategories
 * - **Full CRUD**: Create, read, update, and delete categories at any level
 * - **Analytics Integration**: View product counts per category
 * - **Breadcrumb Support**: Automatic path generation (e.g., Electronics > Computers > Laptops)
 *
 * @param viewModel ViewModel responsible for category CRUD and hierarchy tree computation
 * @param onNavigateBack Callback to return to the previous screen
 * @param networkManager Manager to monitor connectivity status
 */
@Composable
fun CategoryManagementScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val categoryState by viewModel.categoryState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val windowSizeClass = LocalWindowSizeConstant.current

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var selectedParentForAdd by remember { mutableStateOf<CategoryItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf(ViewMode.TREE) } // TREE or LIST

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }
    val networkState = rememberNetworkState(networkManager)

    LaunchedEffect(Unit) {
        viewModel.loadCategoryTree()
        viewModel.loadCategories()
    }

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
                viewModel.loadCategories()
                viewModel.loadCategoryTree()
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }
        },
        title = R.string.manage_categories,
        snackBarHostState = snackBarHostState,
        showBottomBar = false,
        verticalArrangement = Arrangement.Top,
        onNavigateBack = { onNavigateBack() },
        floatingBtnContent = {
            CustomFloatingPointButton(
                onClick = {
                    selectedParentForAdd = null
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

            CustomSpacer()
            PaddedSection(
                alignment = Alignment.CenterHorizontally,
                content = {
                    // search bar
                    CustomSpacer()
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
                                stringResource(R.string.search_categories),
                                style = windowSizeClass.bodyTextStyle
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
                    CustomSpacer()

                    when {
                        categoryState.isLoading -> {
                            CustomListCardShimmer()
                        }

                        categoryState.error != null -> {
                            CustomEmptyState(
                                btnLabel = R.string.retry,
                                subTitle = R.string.category_error,
                                onBtnClick = {
                                    viewModel.loadCategories()
                                    viewModel.loadCategoryTree()
                                },
                                leadingIcon = Icons.Filled.Error,
                            )
                        }

                        searchQuery.isNotEmpty() && categoryState.categories.isEmpty() -> {
                            CustomEmptyState(
                                titleStr = "No results found",
                                showBtn = false,
                                leadingIcon = Icons.Filled.SearchOff,
                            )
                        }

                        else -> {
                            if (viewMode == ViewMode.TREE) {
                                // Tree View
                                if (categoryState.categoryTree.isEmpty()) {
                                    CustomEmptyState(
                                        title = R.string.no_categories_available,
                                        subTitle = R.string.add_first_category,
                                        showBtn = false,
                                        leadingIcon = Icons.Filled.Category,
                                    )
                                } else {
                                    CustomLazyColumn {
                                        item {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    stringResource(R.string.view_mode),
                                                    style = windowSizeClass.bodyTextStyle
                                                )

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(
                                                        windowSizeClass.normalVerticalPadding
                                                    )
                                                ) {
                                                    CustomFilterChip(
                                                        isSelected = viewMode == ViewMode.TREE,
                                                        onClick = { viewMode = ViewMode.TREE },
                                                        label = "Tree",
                                                        leadingIcon = if (viewMode == ViewMode.TREE) {
                                                            {
                                                                CustomIcon(
                                                                    icon = Icons.Filled.AccountTree,
                                                                    contentDescription = null
                                                                )
                                                            }
                                                        } else null
                                                    )

                                                    CustomFilterChip(
                                                        isSelected = viewMode == ViewMode.LIST,
                                                        onClick = { viewMode = ViewMode.LIST },
                                                        label = "List",
                                                        leadingIcon = if (viewMode == ViewMode.LIST) {
                                                            {
                                                                CustomIcon(
                                                                    icon = Icons.AutoMirrored.Filled.List,
                                                                    contentDescription = null,
                                                                    iconSize = customSpacing.custom18
                                                                )
                                                            }
                                                        } else null
                                                    )
                                                }
                                            }
                                        }

                                        items(items = categoryState.categoryTree) { node ->
                                            CategoryTreeItem(
                                                node = node,
                                                onExpand = { viewModel.toggleCategoryExpansion(it) },
                                                onEdit = {
                                                    selectedCategory = it
                                                    showEditDialog = true
                                                },
                                                onDelete = {
                                                    selectedCategory = it
                                                    showDeleteDialog = true
                                                },
                                                onAddSubcategory = {
                                                    selectedParentForAdd = it
                                                    showAddDialog = true
                                                }
                                            )
                                        }
                                        item {
                                            CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))
                                        }
                                    }
                                }
                            } else {
                                // List View
                                if (categoryState.categories.isEmpty()) {
                                    CustomEmptyState(
                                        titleStr = "No categories yet",
                                        showBtn = false,
                                        leadingIcon = Icons.Filled.Category,
                                    )
                                } else {
                                    CustomLazyColumn {

                                        items(categoryState.categories) { category ->
                                            CategoryListCard(
                                                category = category,
                                                onEdit = {
                                                    selectedCategory = category
                                                    showEditDialog = true
                                                },
                                                onDelete = {
                                                    selectedCategory = category
                                                    showDeleteDialog = true
                                                },
                                                onAddSubcategory = {
                                                    selectedParentForAdd = category
                                                    showAddDialog = true
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
                    }
                })

            // Dialogs
            if (showAddDialog) {
                AddCategoryDialog(
                    parentCategory = selectedParentForAdd,
                    onDismiss = {
                        showAddDialog = false
                        selectedParentForAdd = null
                    },
                    onConfirm = { categoryName, categoryImage, description ->
                        viewModel.createCategory(
                            categoryName = categoryName,
                            parentId = selectedParentForAdd?.id,
                            categoryImage = categoryImage,
                            description = description
                        )
                        showAddDialog = false
                        selectedParentForAdd = null
                    }
                )
            }

            if (showEditDialog && selectedCategory != null) {
                EditCategoryDialog(
                    category = selectedCategory!!,
                    onDismiss = {
                        showEditDialog = false
                        selectedCategory = null
                    },
                    onConfirm = { categoryName, categoryImage, description ->
                        viewModel.updateCategory(
                            categoryId = selectedCategory!!.id,
                            categoryName = categoryName,
                            categoryImage = categoryImage,
                            description = description
                        )
                        showEditDialog = false
                        selectedCategory = null
                    }
                )
            }

            if (showDeleteDialog && selectedCategory != null) {
                DeleteCategoryDialog(
                    category = selectedCategory!!,
                    onDismiss = {
                        showDeleteDialog = false
                        selectedCategory = null
                    },
                    onConfirm = {
                        viewModel.deleteCategory(
                            selectedCategory!!.id,
                            selectedCategory!!.categoryName
                        )
                        showDeleteDialog = false
                        selectedCategory = null
                    }
                )
            }
        },
        actions = {
            if (categoryState.isLoading) {
                TopBarActionsShimmer()
            } else {
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                CustomSpacer()

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Refresh),
                    onClick = {
                        viewModel.loadCategories()
                        viewModel.loadCategoryTree()
                    },
                    contentDescription = "Refresh"
                )
            }
        }
    )
}

/**
 * ViewMode - UI state for category display format
 *
 * Defines how categories are presented on the screen.
 * - [TREE]: Hierarchical view showing parent-child relationships with indentation
 * - [LIST]: Flat list view showing all categories alphabetically with breadcrumbs
 */
enum class ViewMode {
    TREE, LIST
}

/**
 * CategoryTreeItem - Recursive UI component for hierarchical display
 *
 * Renders a single category card with its subcategories nested below it when expanded.
 * Includes actions for editing, deleting, and adding direct subcategories.
 *
 * @param node The current category node in the tree structure
 * @param onExpand Callback to toggle the expansion state of this node
 * @param onEdit Callback when the edit action is triggered
 * @param onSelect Callback when the category is selected (e.g., for filtering)
 * @param onDelete Callback when the delete action is triggered
 * @param onAddSubcategory Callback to begin adding a child category to this node
 */
@Composable
fun CategoryTreeItem(
    node: CategoryNode,
    onExpand: (String) -> Unit = { },
    onEdit: (CategoryItem) -> Unit = {},
    onSelect: (CategoryItem) -> Unit = {},
    onDelete: (CategoryItem) -> Unit = {},
    onAddSubcategory: (CategoryItem) -> Unit = {}
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = windowSizeClass.baseSize * node.depth,
                top = windowSizeClass.smallVerticalPadding,
                bottom = windowSizeClass.smallVerticalPadding
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeClass.basePadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                CustomSpacer(modifier = Modifier.width(windowSizeClass.baseVerticalPadding))

                // Category icon based on level
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
                    }
                )

                CustomSpacer(modifier = Modifier.width(windowSizeClass.basePadding))

                Column {
                    Text(
                        text = node.category.categoryName,
                        style = windowSizeClass.bodyTextStyle.copy(
                            fontWeight = if (node.category.level == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    )

                    // Breadcrumb
                    if (node.category.breadcrumb.size > 1) {
                        Text(
                            text = node.category.breadcrumb.joinToString(" > "),
                            style = windowSizeClass.labelTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stats
                    if (node.category.hasSubcategories || node.category.productCount > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(windowSizeClass.baseNormalVerticalPadding)
                        ) {
                            if (node.category.hasSubcategories) {
                                Text(
                                    "${node.category.subcategoryIds.size} subcategories",
                                    style = windowSizeClass.labelTextStyle,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (node.category.productCount > 0) {
                                Text(
                                    "${node.category.productCount} products",
                                    style = windowSizeClass.labelTextStyle,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            // Actions
            Row {
                // Add subcategory button
                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.AddCircleOutline),
                    onClick = { onAddSubcategory(node.category) },
                    contentDescription = "Add Subcategory",
                    tint = MaterialTheme.colorScheme.primary
                )

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Edit),
                    onClick = { onEdit(node.category) },
                    contentDescription = "Edit"
                )

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Delete),
                    onClick = { onDelete(node.category) },
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Render subcategories if expanded
        if (node.isExpanded && node.subcategories.isNotEmpty()) {
            Column {
                node.subcategories.forEach { subNode ->
                    CategoryTreeItem(
                        node = subNode,
                        onExpand = onExpand,
                        onEdit = onEdit,
                        onDelete = onDelete,
                        onSelect = onSelect,
                        onAddSubcategory = onAddSubcategory
                    )
                }
            }
        }
    }
}

/**
 * CategoryListCard - Flat list representation of a category
 *
 * Displays essential category information in a compact card format suitable
 * for flat lists. Includes full breadcrumb path and essential actions.
 *
 * @param category The category data to display
 * @param onEdit Callback when the edit action is triggered
 * @param onDelete Callback when the delete action is triggered
 * @param onAddSubcategory Callback to add a child category to this item
 */
@Composable
fun CategoryListCard(
    category: CategoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddSubcategory: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = windowSizeClass.smallVerticalPadding)
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
                // Level indicator
                Surface(
                    shape = CircleShape,
                    color = when (category.level) {
                        0 -> colors.customColor1.copy(alpha = 0.1f)
                        1 -> colors.customColor4.copy(alpha = 0.1f)
                        else -> colors.customColor5.copy(alpha = 0.1f)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(windowSizeClass.customSpacerSmall)
                            .padding(windowSizeClass.normalVerticalPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "L${category.level}",
                            style = windowSizeClass.labelTextStyle.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (category.level) {
                                    0 -> colors.customColor1
                                    1 -> colors.customColor4
                                    else -> colors.customColor5
                                }
                            )
                        )
                    }
                }

                CustomSpacer(modifier = Modifier.width(windowSizeClass.basePadding))

                Column {
                    Text(
                        text = category.categoryName,
                        style = windowSizeClass.bodyTextStyle
                    )

                    // Full path
                    if (category.breadcrumb.isNotEmpty()) {
                        Text(
                            text = category.breadcrumb.joinToString(" > "),
                            style = windowSizeClass.labelTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Actions
            Row {
                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.AddCircleOutline),
                    onClick = onAddSubcategory,
                    contentDescription = "Add Subcategory",
                    tint = MaterialTheme.colorScheme.primary
                )

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Edit),
                    onClick = onEdit,
                    contentDescription = "Edit"
                )

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Delete),
                    onClick = onDelete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * AddCategoryDialog - Form for creating new categories or subcategories
 *
 * Handles input for category title, description, and optional image URL.
 * Automatically context-aware: if [parentCategory] is provided, it configures
 * the new category as a sub-item of that parent.
 *
 * @param parentCategory The optional parent category for subcategory creation
 * @param onDismiss Callback to close the dialog without saving
 * @param onConfirm Callback when category is successfully configured
 */
@Composable
fun AddCategoryDialog(
    parentCategory: CategoryItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    var categoryName by remember { mutableStateOf("") }
    var categoryImage by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Add,
                contentDescription = "Add category",
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Column {
                Text(
                    if (parentCategory != null)
                        "Add Subcategory"
                    else
                        "Add Category",
                    style = windowSizeClass.titleTextStyle
                )

                if (parentCategory != null) {
                    CustomSpacer(modifier = Modifier.height(windowSizeClass.normalVerticalPadding))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(windowSizeClass.baseNormalVerticalPadding)
                        ) {
                            Text(
                                stringResource(R.string.parent_category),
                                style = windowSizeClass.labelTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))
                            Text(
                                parentCategory.breadcrumb.joinToString(" > "),
                                style = windowSizeClass.labelTextStyle.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )

                            Text(
                                "Level: ${parentCategory.level + 1}",
                                style = windowSizeClass.labelTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Category Name
                CustomTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = R.string.category_name,
                    placeholderUnit = {
                        Text(
                            text = when {
                                parentCategory == null -> "e.g., Electronics"
                                parentCategory.level == 0 -> "e.g., PC"
                                parentCategory.level == 1 -> "e.g., Desktop PC"
                                else -> "e.g., Gaming PC"
                            },
                            style = windowSizeClass.bodyTextStyle
                        )
                    },
                )

                CustomSpacer()

                // Category Image URL
                CustomTextField(
                    value = categoryImage,
                    onValueChange = { categoryImage = it },
                    label = R.string.image_url,
                    placeholder = R.string.image_url_placeholder,
                )

                CustomSpacer()

                // Description
                CustomTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = R.string.description,
                    placeholder = R.string.brief_description,
                    minLines = 2,
                    maxLines = 3,
                )

                CustomSpacer(modifier = Modifier.height(windowSizeClass.baseVerticalPadding))

                // Info text
                Text(
                    stringResource(R.string.required_field),
                    style = windowSizeClass.labelTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                label = R.string.add_category,
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onConfirm(categoryName, categoryImage, description)
                    }
                },
                enabled = categoryName.isNotBlank()
            )
        }
    )
}

/**
 * EditCategoryDialog - Form for updating existing category details
 *
 * Pre-fills fields with existing category data and allows administrators
 * to commit changes to Firestore.
 *
 * @param category The existing category item to edit
 * @param onDismiss Callback to close the dialog without saving
 * @param onConfirm Callback when updates are successfully committed
 */
@Composable
fun EditCategoryDialog(
    category: CategoryItem,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    var categoryName by remember { mutableStateOf(category.categoryName) }
    var categoryImage by remember { mutableStateOf(category.categoryImage) }
    var description by remember { mutableStateOf(category.description) }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Edit,
                contentDescription = "Edit category",
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Column {
                Text(
                    stringResource(R.string.edit_category),
                    style = windowSizeClass.titleTextStyle,
                    fontWeight = FontWeight.Bold
                )

                CustomSpacer(modifier = Modifier.height(windowSizeClass.baseVerticalPadding))

                // Category Info
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CustomShape.mediumShape()
                ) {
                    Column(
                        modifier = Modifier.padding(windowSizeClass.basePadding)
                    ) {
                        if (category.breadcrumb.size > 1) {
                            Text(
                                stringResource(R.string.full_path),
                                style = windowSizeClass.labelTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                category.breadcrumb.joinToString(" > "),
                                style = windowSizeClass.bodyTextStyle,
                                fontWeight = FontWeight.SemiBold

                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(windowSizeClass.basePadding)
                        ) {
                            Text(
                                "Level: ${category.level}",
                                style = windowSizeClass.labelTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (category.hasSubcategories) {
                                Text(
                                    "${category.subcategoryIds.size} subcategories",
                                    style = windowSizeClass.labelTextStyle,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Category Name
                CustomTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = R.string.category_name,
                    modifier = Modifier.fillMaxWidth(),
                )

                CustomSpacer()

                // Category Image URL
                CustomTextField(
                    value = categoryImage,
                    onValueChange = { categoryImage = it },
                    label = R.string.image_url,
                    placeholder = R.string.image_url_placeholder,
                    modifier = Modifier.fillMaxWidth(),
                )

                CustomSpacer()

                // Description
                CustomTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = R.string.description,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                )

                if (category.hasSubcategories) {
                    CustomSpacer()

                    Surface(
                        color = colors.customColor15,
                        shape = CustomShape.mediumShape()
                    ) {
                        Row(
                            modifier = Modifier.padding(windowSizeClass.baseNormalVerticalPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomIcon(
                                icon = Icons.Filled.Info,
                                contentDescription = "Info",
                                tint = colors.customColor14,
                            )

                            CustomSpacer(modifier = Modifier.width(windowSizeClass.baseVerticalPadding))

                            Text(
                                stringResource(R.string.subcategories_full_path),
                                style = windowSizeClass.labelTextStyle,
                                color = colors.customColor14
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            CustomTextButton(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onConfirm(categoryName, categoryImage, description)
                    }
                },
                enabled = categoryName.isNotBlank(),
                label = R.string.update
            )
        }
    )
}

/**
 * DeleteCategoryDialog - Confirmation for category removal
 *
 * Warns the administrator before deleting a category. Note that deletions
 * may cascade or fail if subcategories exist, depending on repository logic.
 *
 * @param category The category item to be deleted
 * @param onDismiss Callback to cancel the deletion
 * @param onConfirm Callback when deletion is confirmed
 */
@Composable
fun DeleteCategoryDialog(
    category: CategoryItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = MaterialTheme.colorScheme.error,
                iconSize = windowSizeClass.largeIconSize
            )
        },
        title = {
            Text(
                stringResource(R.string.delete_category),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete \"${category.categoryName}\"?",
                    style = windowSizeClass.bodyTextStyle
                )

                CustomSpacer()

                // Category details
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    shape = CustomShape.mediumShape()
                ) {
                    Column(
                        modifier = Modifier.padding(windowSizeClass.baseNormalVerticalPadding)
                    ) {
                        if (category.breadcrumb.size > 1) {
                            Text(
                                "Path: ${category.breadcrumb.joinToString(" > ")}",
                                style = windowSizeClass.titleTextStyle
                            )
                        }

                        Text(
                            "Level: ${category.level}",
                            style = windowSizeClass.bodyTextStyle
                        )

                        if (category.hasSubcategories) {
                            CustomSpacer(modifier = Modifier.height(windowSizeClass.baseVerticalPadding))
                            Text(
                                "⚠️ This category has ${category.subcategoryIds.size} subcategories",
                                style = windowSizeClass.titleTextStyle,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )

                            Text(
                                stringResource(R.string.delete_all_categories),
                                style = windowSizeClass.labelTextStyle,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (category.productCount > 0) {
                            CustomSpacer(modifier = Modifier.height(windowSizeClass.baseVerticalPadding))
                            Text(
                                " This category has ${category.productCount} products",
                                style = windowSizeClass.bodyTextStyle,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                stringResource(R.string.remove_all_reassign),
                                style = windowSizeClass.labelTextStyle,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                if (!category.hasSubcategories && category.productCount == 0) {
                    CustomSpacer()
                    Text(
                        stringResource(R.string.action_undone),
                        style = windowSizeClass.bodyTextStyle,
                        color = MaterialTheme.colorScheme.error
                    )
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
                label = R.string.delete,
                onClick = onConfirm,
                enabled = !category.hasSubcategories && category.productCount == 0,
                color = MaterialTheme.colorScheme.error
            )
        }
    )
}


// Helper function to filter category tree
fun filterCategoryTree(
    tree: List<CategoryNode>,
    query: String
): List<CategoryNode> {
    /**
     * filterNode
     *
     *
     * @param node The node parameter
     */
    fun filterNode(node: CategoryNode): CategoryNode? {
        val matchesQuery = node.category.categoryName.contains(query, ignoreCase = true) ||
                node.category.breadcrumb.any { it.contains(query, ignoreCase = true) }

        val filteredChildren = node.subcategories.mapNotNull { filterNode(it) }

        return if (matchesQuery || filteredChildren.isNotEmpty()) {
            node.copy(subcategories = filteredChildren)
        } else {
            null
        }
    }

    return tree.mapNotNull { filterNode(it) }
}
