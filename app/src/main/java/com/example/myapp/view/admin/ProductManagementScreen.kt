package com.example.myapp.view.admin

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomDropDownMenuItem
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomFloatingPointButton
import com.example.myapp.view.components.CustomHorizontalDivider
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomItemCard
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomSurfaceContainer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.TopBarActionsShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CustomShape
import kotlinx.coroutines.delay

/**
 * ProductManagementScreen - Product listing and management interface
 *
 * This screen provides administrators with a comprehensive view of all products,
 * along with tools to search, filter, edit, delete, and manage product status.
 *
 * ## Features
 * - **Product List**: Displays all products with key information
 * - **Search**: Real-time search by product name
 * - **Filtering**: Filter by Featured, Trending, or In Stock status
 * - **Quick Actions**: Edit, delete, and toggle product status
 * - **Bulk Operations**: Toggle featured/trending/stock status
 * - **Floating Action Button**: Quick access to add new product
 *
 * ## Product Information Displayed
 * - Product image
 * - Product name
 * - Category
 * - Price (formatted as currency)
 * - Stock status with quantity
 * - Featured badge
 * - Trending badge
 *
 * ## Available Actions
 * - **Edit**: Navigate to edit product screen
 * - **Delete**: Remove product with confirmation dialog
 * - **Toggle Featured**: Mark/unmark as featured product
 * - **Toggle Trending**: Mark/unmark as trending product
 * - **Toggle Stock**: Mark as in stock or out of stock
 *
 * ## Filter Options
 * - All Products (default)
 * - Featured Products
 * - Trending Products
 * - In Stock Products
 *
 * ## Search Functionality
 * - Real-time search as user types
 * - Clear button to reset search
 * - Automatically loads all products when search is cleared
 *
 * ## User Workflow
 * 1. View list of all products
 * 2. Use search bar to find specific products
 * 3. Apply filters to narrow down results
 * 4. Click product card to edit
 * 5. Use dropdown menu for quick actions
 * 6. Click FAB to add new product
 * 7. Pull down to refresh product list
 *
 * ## Loading States
 * - Shows shimmer placeholders while loading
 * - Displays error state with retry button on failure
 * - Shows empty state when no products match criteria
 *
 * @param viewModel ViewModel for product CRUD operations
 * @param onNavigateBack Callback to navigate back from this screen
 * @param onAddProduct Callback to navigate to add product screen
 * @param onEditProduct Callback to navigate to edit product screen with selected product
 *
 * @see ProductCrudViewModel for product data operations
 * @see ProductManagementCard for individual product display
 * @see DeleteProductDialog for delete confirmation
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProductManagementScreen(
    viewModel: ProductCrudViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onAddProduct: (ProductItem) -> Unit = {},
    onEditProduct: (ProductItem) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {

    val productState by viewModel.productState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val networkState = rememberNetworkState(networkManager)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Products") }
    val windowSizeConstant = LocalWindowSizeConstant.current
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    // Filter products based on selected filter
    val filteredProducts = remember(productState.products, productState.searchResults, selectedFilter, searchQuery) {
        val sourceList = if (searchQuery.isNotBlank() && productState.searchResults != null) {
            productState.searchResults
        } else {
            productState.products  // ← always fall back to full list when search is empty
        }

        when (selectedFilter) {
            "Is Prime" -> sourceList?.filter { it.isPrimeEligible }
            "In Stock" -> sourceList?.filter { it.inStock }
            else -> sourceList
        } ?: productState.products
    }

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

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.loadProducts()
        }
    }

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
                viewModel.loadProducts()
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }
        },
        title = R.string.manage_products,
        snackBarHostState = snackBarHostState,
        showBottomBar = false,
        onNavigateBack = { onNavigateBack() },
        verticalArrangement = Arrangement.Top,
        floatingBtnContent = {
            CustomFloatingPointButton(
                onClick = {
                    onAddProduct(ProductItem())
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
                            if (newQuery.isNotBlank()) {
                                viewModel.searchProductsWithFallback(newQuery) // Call the new ViewModel function
                            } else {
                                viewModel.loadProducts() // Clear the search results
                            }
                        },
                        onSearch = { query ->
                            viewModel.searchProductsWithFallback(query)
                        },
                        leadingIcon = {
                            CustomIcon(icon = Icons.Filled.Search, contentDescription = "Search")
                        },
                        placeholder = {
                            Text(
                                stringResource(R.string.search_products),
                                style = windowSizeConstant.bodyTextStyle
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                ButtonIconComposable(
                                    showBgColor = false,
                                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Search),
                                    onClick = {
                                        searchQuery = ""
                                        viewModel.loadProducts()
                                    },
                                    contentDescription = "search"
                                )
                            }
                        }
                    )

                    when {
                        productState.isLoading -> {
                            CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall))

                            CustomListCardShimmer()
                        }

                        productState.error != null -> {
                            // Error State
                            CustomEmptyState(
                                btnLabel = R.string.retry,
                                title = R.string.size_error,
                                onBtnClick = { viewModel.loadProducts() },
                                scrollState = rememberScrollState(),
                                leadingIcon = Icons.Filled.Error,
                            )
                        }

                        filteredProducts.isEmpty() -> {
                            // Empty State
                            CustomEmptyState(
                                titleStr = if (searchQuery.isEmpty()) "No ${if (selectedFilter != "All Products") selectedFilter else ""} Products${if (selectedFilter != "All Products") " found" else " yet"}" else "No results found",
                                showBtn = false,
                                leadingIcon = Icons.Filled.SearchOff
                            )
                        }

                        else -> {
                            CustomSpacer()
                            CustomLazyColumn {
                                items(items = filteredProducts) { product ->
                                    ProductManagementCard(
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedContentScope = animatedContentScope,
                                        onProductClick = {},
                                        product = product,
                                        onEdit = {
                                            onEditProduct(product)
                                        },
                                        onDelete = {
                                            selectedProduct = product
                                            showDeleteDialog = true
                                        },
                                        useDefaultActions = true
                                    )
                                }

                                // Spacing at bottom for FAB
                                item {
                                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall))
                                }
                            }
                        }
                    }
                }
            )

            // Delete Confirmation Dialog
            if (showDeleteDialog && selectedProduct != null) {
                DeleteProductDialog(
                    product = selectedProduct!!,
                    onDismiss = {
                        showDeleteDialog = false
                        selectedProduct = null
                    },
                    onConfirm = {
                        viewModel.deleteProduct(
                            selectedProduct!!.id,
                            selectedProduct!!.productName
                        )
                        showDeleteDialog = false
                        selectedProduct = null
                    }
                )
            }
        },
        actions = {
            if (productState.isLoading) {
                TopBarActionsShimmer()
            } else {
                // Network Indicator in top bar
                NetworkIndicator(networkState = networkState)

                ButtonIconComposable(
                    showBgColor = false,
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Refresh),
                    onClick = { viewModel.loadProducts() },
                    contentDescription = "Refresh"
                )

        }
        }
    )
}

/**
 * ProductManagementCard - Individual product display card for management
 *
 * Displays a single product with its key information and provides quick access
 * to management actions through a dropdown menu.
 *
 * ## Visual Layout
 * ```
 * ┌────────────────────────────────────┐
 * │ [Image] Product Name          [⋮] │
 * │         Category                   │
 * │         $Price [Stock Status]      │
 * │         ⭐ 📈 (badges)              │
 * └────────────────────────────────────┘
 * ```
 *
 * ## Displayed Information
 * - Product image (80x80dp)
 * - Product name (truncated if too long)
 * - Category chip
 * - Price (formatted as USD)
 * - Stock status badge (green for in stock, red for out of stock)
 * - Quantity (if in stock)
 * - Featured badge (star icon)
 * - Trending badge (trending up icon)
 *
 * ## Actions Menu
 * Accessible via the three-dot menu button:
 * - **Edit**: Open product in edit screen
 * - **Toggle Featured**: Add/remove featured status
 * - **Toggle Trending**: Add/remove trending status
 * - **Toggle Stock**: Mark in/out of stock
 * - **Delete**: Remove product (with confirmation)
 *
 * ## Interaction
 * - Clicking the card opens edit screen
 * - Clicking menu button shows action dropdown
 * - Each action triggers immediate update
 *
 * @param product The product item to display
 * @param onEdit Callback when edit action is selected
 * @param onDelete Callback when delete action is selected
 *
 * @see ProductItem for product data structure
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProductManagementCard(
    product: ProductItem,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onProductClick: () -> Unit,
    actions: (@Composable (isAuthenticated: Boolean, showSignInDialog: () -> Unit) -> Unit)? = null,
    onSignInClick: () -> Unit = {},
    useDefaultActions: Boolean = false
) {
    val windowSizeAppConstant = LocalWindowSizeConstant.current

    var showMenu by remember { mutableStateOf(false) }

    // Default actions (used when user didn't provide `actions` and useDefaultActions == true)
    val defaultActions: @Composable (Boolean, () -> Unit) -> Unit =
        { isAuthenticated, showSignInDialog ->
            if (isAuthenticated) {
                Box {
                    ButtonIconComposable(
                        showBgColor = false,
                        buttonIcon = ButtonIcon.Vector(Icons.Filled.MoreVert),
                        onClick = { showMenu = true },
                        contentDescription = "More Options"
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.edit),
                                    style = windowSizeAppConstant.bodyTextStyle
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                CustomIcon(
                                    icon = Icons.Filled.Edit,
                                    contentDescription = null
                                )
                            }
                        )

                        CustomHorizontalDivider(
                            modifier = Modifier,
                            thickness = DividerDefaults.Thickness,
                            color = DividerDefaults.color
                        )

                        CustomDropDownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.delete),
                                    style = windowSizeAppConstant.bodyTextStyle
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                CustomIcon(
                                    icon = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            textColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                showSignInDialog()
            }
        }

    val resolvedActions: @Composable (Boolean, () -> Unit) -> Unit =
        (actions ?: if (useDefaultActions) {
            defaultActions
        } else {
            { isAuthenticated, showSignInDialog ->
                if (isAuthenticated) {
                    /* no actions provided by user and default disabled */
                } else {
                    showSignInDialog()
                }
            }
        })

    CustomItemCard(
        product = product,
        actions = resolvedActions,
        bottomComponent = {
            // Status Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(windowSizeAppConstant.smallVerticalPadding)
            ) {
                if (product.isPrimeEligible) {
                    CustomSurfaceContainer(
                        color = colors.customColor16,
                        icon = Icons.Filled.Verified,
                        text = R.string.prime,
                        contentDescription = "prime eligible"
                    )
                }

                // Stock Status
                Surface(
                    color = if (product.inStock)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer,
                    shape = CustomShape.mediumShape()
                ) {
                    Text(
                        text = if (product.inStock) "In Stock (${product.quantity})" else "Out of Stock",
                        style = windowSizeAppConstant.labelTextStyle,
                        color = if (product.inStock)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(
                            horizontal = windowSizeAppConstant.baseVerticalPadding,
                            vertical = windowSizeAppConstant.cardElevationPadding
                        )
                    )
                }
            }
        },
        onProductClick = { onProductClick() },
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
        onSignInClick = onSignInClick
    )
}

/**
 * DeleteProductDialog - Confirmation dialog for product deletion
 *
 * Displays a warning dialog to confirm product deletion, preventing accidental removals.
 *
 * ## Features
 * - Warning icon to indicate destructive action
 * - Product name displayed for confirmation
 * - Warning message about irreversibility
 * - Confirm and cancel buttons
 *
 * ## User Flow
 * 1. User clicks delete in product menu
 * 2. Dialog appears with product name
 * 3. User confirms or cancels
 * 4. If confirmed, product is deleted
 * 5. Dialog closes automatically
 *
 * @param product The product to be deleted
 * @param onDismiss Callback when dialog is dismissed without deleting
 * @param onConfirm Callback when user confirms deletion
 *
 * @see ProductManagementCard for where this dialog is triggered
 */
@Composable
fun DeleteProductDialog(
    product: ProductItem,
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
                stringResource(R.string.delete_product),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Column {
                Text("Are you sure you want to delete '${product.productName}'?",
                    style = windowSizeClass.bodyTextStyle
                    )

                CustomSpacer(modifier = Modifier.height(windowSizeClass.normalVerticalPadding))

                Text(
                    stringResource(R.string.action_warning),
                    style = windowSizeClass.labelTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        dismissButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.cancel
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