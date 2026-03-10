package com.example.myapp.view.screens.product.categories

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.CartViewModel
import com.example.myapp.data.model.FavoriteViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomFilterChip
import com.example.myapp.view.components.CustomHorizontalDivider
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomItemCard
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomLazyRow
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.ProductShimmerList
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.custom.buttons.CustomCartButton
import com.example.myapp.view.components.custom.buttons.CustomOutlinedButton
import com.example.myapp.view.components.custom.buttons.FavoriteSplashButton
import com.example.myapp.view.components.custom.buttons.ShoppingCartBadge
import com.example.myapp.view.screens.product.ProductCard
import com.example.myapp.view.screens.product.RelatedProductsSection
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CustomShape

/**
 * DisplayMode - Layout mode for product listing
 *
 * Defines how products are displayed in the list.
 */
enum class DisplayMode {
    ROW, GRID
}

// Data classes for filter state
/**
 * FilterState - Manges state of filter options
 *
 * @param selectedCategory Currently selected product category
 * @param selectedPriceRange Currently selected price range filter
 * @param selectedRating Minimum rating filter value
 * @param sortBy Current sort order
 * @param showFilters Boolean flag to toggle filter section visibility
 */
data class FilterState(
    val selectedCategory: String = "All",
    val selectedPriceRange: String = "All",
    val selectedRating: Float = 0f,
    val sortBy: String = "Name",
    val showFilters: Boolean = false
)

/**
 * FilterActions - Callback actions for filter interactions
 */
data class FilterActions(
    val onCategoryChange: (String) -> Unit,
    val onPriceRangeChange: (String) -> Unit,
    val onRatingChange: (Float) -> Unit,
    val onSortByChange: (String) -> Unit,
    val onToggleFilters: () -> Unit,
    val onClearFilters: () -> Unit
)

/**
 * FilterData - Data for filter options
 *
 * @param categories List of available categories
 * @param priceRanges List of price range options
 * @param sortOptions List of sorting options
 */
data class FilterData(
    val categories: List<String> = listOf("All"), // Default fallback
    val priceRanges: List<String> = listOf("All", "$0-$50", "$51-$100", "$101-$200", "$200+"),
    val sortOptions: List<String> = listOf(
        "Name",
        "Price Low to High",
        "Price High to Low",
        "Rating"
    )
)

// Empty State Composable
/**
 * EmptyFilterResults - Displays when no products match the current filters
 *
 * @param onClearFilters Callback to clear all active filters
 */
@Composable
fun EmptyFilterResults(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(windowSizeConstant.baseSize),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CustomIcon(
            icon = Icons.Filled.SearchOff,
            contentDescription = stringResource(R.string.no_results),
            iconSize = windowSizeConstant.largeIconSize,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

        Text(
            text = stringResource(R.string.no_results),
            style = windowSizeConstant.titleTextStyle,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

        Text(
            text = stringResource(R.string.adjust_search),
            style = windowSizeConstant.titleTextStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseSize))

        CustomOutlinedButton(
            icon = ButtonIcon.Vector(Icons.Filled.Clear),
            onClick = onClearFilters,
            label = R.string.clear_filter
        )
    }
}

// Stateless Filter Section Composable
/**
 * FilterSection - UI section containing all filter controls
 *
 * Displays horizontal scrolling lists for:
 * - Categories
 * - Price Ranges
 * - Rating Slider
 * - Sort Options
 *
 * @param filterState Current state of filters
 * @param filterActions Callbacks for filter updates
 * @param filterData Available filter options
 * @param viewModel ProductViewModel for data access
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    modifier: Modifier = Modifier,
    filterState: FilterState,
    filterActions: FilterActions,
    filterData: FilterData = FilterData(),
    viewModel: ProductCrudViewModel = hiltViewModel()
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    val uiState by viewModel.productState.collectAsState()
    uiState.categories

    val dynamicCategories = remember(uiState.categories) {
        listOf("All") + uiState.categories.map { it.categoryName } // Assuming your category model has a 'name' property
    }
    if (filterState.showFilters) {
        CustomSpacer()
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(CustomShape.mediumShape())
                .background(MaterialTheme.colorScheme.surfaceContainer),
            verticalArrangement = Arrangement.spacedBy(windowSizeConstant.basePadding)
        ) {
            // Category Filter
            PaddedSection(content = {
                Text(
                    text = stringResource(R.string.category),
                    style = windowSizeConstant.bodyTextStyle,
                    fontWeight = FontWeight.SemiBold
                )

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                CustomLazyRow {
                    items(dynamicCategories) { category ->
                        CustomFilterChip(
                            onClick = { filterActions.onCategoryChange(category) },
                            label = category,
                            isSelected = filterState.selectedCategory == category
                        )
                    }
                }
            }
            )

            // Price Range Filter
            PaddedSection(content = {
                Text(
                    text = stringResource(R.string.price_range),
                    style = windowSizeConstant.bodyTextStyle,
                    fontWeight = FontWeight.SemiBold
                )

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                CustomLazyRow {
                    items(filterData.priceRanges) { range ->
                        CustomFilterChip(
                            onClick = { filterActions.onPriceRangeChange(range) },
                            label = range,
                            isSelected = filterState.selectedPriceRange == range
                        )
                    }
                }
            })

            // Rating Filter
            PaddedSection(content = {
                Text(
                    text = "Minimum Rating: ${filterState.selectedRating.toInt()}+ stars",
                    style = windowSizeConstant.labelTextStyle,
                    fontWeight = FontWeight.SemiBold
                )

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                Slider(
                    value = filterState.selectedRating,
                    onValueChange = filterActions.onRatingChange,
                    valueRange = 0f..5f,
                    steps = 4
                )
            })

            // Sort Options
            PaddedSection(content = {
                Text(
                    text = stringResource(R.string.sort_by),
                    style = windowSizeConstant.labelTextStyle,
                    fontWeight = FontWeight.SemiBold
                )
                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                CustomLazyRow {
                    items(filterData.sortOptions) { option ->
                        CustomFilterChip(
                            onClick = { filterActions.onSortByChange(option) },
                            label = option,
                            isSelected = filterState.sortBy == option
                        )
                    }
                }
            })
        }
    }
}

// Updated FilterProducts composable using the stateless filter
/**
 * FilterProducts - Main screen for browsing and filtering products
 *
 * Features:
 * - Product search with history
 * - Category filtering
 * - Advanced filtering (Price, Rating, Sort)
 * - Grid/List view toggle
 * - Shopping cart access
 *
 * @param searchQuery Initial search query
 * @param onBackNavigation Callback for back navigation
 * @param onSearchClick Callback to open search
 * @param onCartClick Callback to open cart
 * @param sharedTransitionScope Scope for shared element transitions
 * @param animatedContentScope Scope for animation content
 * @param viewModel ViewModel for product data
 * @param favoriteViewModel ViewModel for favorites
 * @param cartViewModel ViewModel for cart
 * @param initialCategory Initial category to filter by
 * @param onProductClick Callback when a product is clicked
 * @param onSignInClick Callback for sign in required actions
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

@Composable
fun FilterProducts(
    searchQuery: String = "",
    onBackNavigation: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: ProductCrudViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    initialCategory: String = "All",
    onProductClick: (ProductItem) -> Unit = {},
    onSignInClick: () -> Unit,
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val uiState by viewModel.productState.collectAsState()
    val imageLoader = viewModel.getImageLoader()
    val searchResult by viewModel.searchResult.collectAsState()
    val networkState = rememberNetworkState(networkManager)
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    var displayMode by remember { mutableStateOf(DisplayMode.GRID) }
    var hasSearched by remember { mutableStateOf(false) }

    //  Simple loading state - show shimmer when loading AND no products yet
    val shouldShowShimmer = uiState.isLoading && uiState.products.isEmpty()

    // Determine screen title based on context
    val screenTitle = when {
        searchQuery.isNotBlank() -> "Search: \"$searchQuery\""
        initialCategory != "All" -> initialCategory
        else -> "Products"
    }

    // Handle search queries
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            hasSearched = true
            viewModel.searchProductsWithFallback(searchQuery)
        } else if (hasSearched) {
            viewModel.clearSearch()
            hasSearched = false
        }
    }

// only load once on initial composition
    LaunchedEffect(Unit) {
        if (uiState.products.isEmpty() && !uiState.isLoading && uiState.error == null) {
            viewModel.loadProducts()
        }
    }
    // Base products selection
    val baseProducts by remember {
        derivedStateOf {
            when {
                searchQuery.isNotBlank() -> {
                    when {
                        searchResult != null -> {
                            searchResult!!.exactMatches.ifEmpty {
                                searchResult!!.similarProducts
                            }
                        }

                        uiState.searchResults != null -> uiState.searchResults!!
                        else -> emptyList()
                    }
                }

                initialCategory != "All" -> {
                    uiState.products.filter {
                        it.category.equals(initialCategory, ignoreCase = true)
                    }
                }

                else -> uiState.products
            }
        }
    }

    // Filter state initialization
    var filterState by remember(initialCategory) {
        mutableStateOf(FilterState(selectedCategory = initialCategory))
    }

    // Filtered items with all filter options applied
    val filteredItems by remember {
        derivedStateOf {
            var filtered = baseProducts

            if (filterState.selectedCategory != "All" && searchQuery.isBlank()) {
                filtered = filtered.filter {
                    it.category.equals(filterState.selectedCategory, ignoreCase = true)
                }
            }

            if (filterState.selectedPriceRange != "All") {
                filtered = when (filterState.selectedPriceRange) {
                    "$0-$50" -> filtered.filter { it.price <= 50 }
                    "$51-$100" -> filtered.filter { it.price in 51.0..100.0 }
                    "$101-$200" -> filtered.filter { it.price in 101.0..200.0 }
                    "$200+" -> filtered.filter { it.price > 200 }
                    else -> filtered
                }
            }

            if (filterState.selectedRating > 0) {
                filtered = filtered.filter { it.rating >= filterState.selectedRating }
            }

            when (filterState.sortBy) {
                "Name" -> filtered.sortedBy { it.productName }
                "Price Low to High" -> filtered.sortedBy { it.price }
                "Price High to Low" -> filtered.sortedByDescending { it.price }
                "Rating" -> filtered.sortedByDescending { it.rating }
                else -> filtered
            }
        }
    }

    // Available categories for filter
    val availableCategories by remember {
        derivedStateOf {
            listOf("All") + uiState.products.map { it.category }.distinct().sorted()
        }
    }

    val filterActions = remember {
        FilterActions(
            onCategoryChange = { category ->
                filterState = filterState.copy(selectedCategory = category)
            },
            onPriceRangeChange = { priceRange ->
                filterState = filterState.copy(selectedPriceRange = priceRange)
            },
            onRatingChange = { rating ->
                filterState = filterState.copy(selectedRating = rating)
            },
            onSortByChange = { sortBy ->
                filterState = filterState.copy(sortBy = sortBy)
            },
            onToggleFilters = {
                filterState = filterState.copy(showFilters = !filterState.showFilters)
            },
            onClearFilters = {
                filterState = FilterState(selectedCategory = initialCategory)
            }
        )
    }

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
                if (searchQuery.isNotBlank()) {
                    viewModel.searchProductsWithFallback(searchQuery)
                } else {
                    viewModel.refreshProducts()
                }
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }

        },
        showBottomBar = false,
        onNavigateBack = {
            viewModel.clearSearch()
            onBackNavigation()
        },
        strTitle = screenTitle,
        verticalArrangement = Arrangement.Top,
        actions = {
            ButtonIconComposable(
                showBgColor = false,
                buttonIcon = ButtonIcon.Vector(Icons.Filled.Search),
                onClick = { onSearchClick() },
                contentDescription = "Search"
            )

            CustomSpacer(modifier = Modifier.height(windowSizeConstant.basePadding))

            val badgeNumber = cartViewModel.cartItems
            ShoppingCartBadge(badgeNumber, onCartClick)
        },
        content = {

            // Network Status Banner

            if (!networkState.hasInternet) {
                CustomSpacer()

                NetworkIndicator(networkState = networkState)

                CustomSpacer()
                // Network Status Banner
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

            when {
                //  Show shimmer only on initial load
                shouldShowShimmer -> {
                    PaddedSection(
                        content = {
                            ProductShimmerList()
                        }
                    )
                }

                // Show error state (only when not loading)
                uiState.error != null && !uiState.isLoading -> {
                    ErrorState(
                        message = uiState.error ?: "Unknown error",
                        onRetry = {
                            if (searchQuery.isNotBlank()) {
                                viewModel.searchProductsWithFallback(searchQuery)
                            } else {
                                viewModel.refreshProducts()
                            }
                        }
                    )
                }

                // Show empty search state
                baseProducts.isEmpty() && searchQuery.isNotBlank() && !uiState.isLoading -> {
                    EmptyFilterState(
                        searchQuery = searchQuery,
                        viewModel = viewModel,
                        filterActions = filterActions,
                        onClearFilters = filterActions.onClearFilters
                    )
                }

                // Show products
                else -> {
                    when {
                        filteredItems.isEmpty() -> {
                            EmptyFilterResults(
                                onClearFilters = {
                                    if (searchQuery.isNotBlank()) {
                                        viewModel.clearSearch()
                                    }
                                    filterActions.onClearFilters()
                                }
                            )
                        }

                        displayMode == DisplayMode.ROW -> {
                            CustomSpacer()
                            CustomLazyColumn {
                                // Header with filter controls
                                item {
                                    PaddedSection(
                                        content = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = when {
                                                            searchQuery.isNotBlank() -> "Search Results"
                                                            initialCategory != "All" -> "$initialCategory Products"
                                                            else -> "All Products"
                                                        },
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        style = windowSizeConstant.bodyTextStyle,
                                                        fontWeight = FontWeight.Bold
                                                    )

                                                    Text(
                                                        text = "${filteredItems.size} ${if (filteredItems.size == 1) "item" else "items"}",
                                                        style = windowSizeConstant.bodyTextStyle,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(
                                                        windowSizeConstant.normalVerticalPadding
                                                    ),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Filter button
                                                    ButtonIconComposable(
                                                        showBgColor = false,
                                                        buttonIcon = ButtonIcon.Vector(Icons.Filled.FilterList),
                                                        tint = if (filterState.showFilters)
                                                            MaterialTheme.colorScheme.primary
                                                        else
                                                            MaterialTheme.colorScheme.onSurface,
                                                        onClick = { filterActions.onToggleFilters() },
                                                        contentDescription = "Filter"
                                                    )

                                                    // View mode toggle
                                                    Row(
                                                        modifier = Modifier
                                                            .background(
                                                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                                                CustomShape.mediumShape()
                                                            )
                                                    ) {
                                                        ButtonIconComposable(
                                                            showBgColor = false,
                                                            buttonIcon = ButtonIcon.Vector(Icons.AutoMirrored.Filled.ViewList),
                                                            modifier = Modifier
                                                                .background(
                                                                    if (displayMode == DisplayMode.ROW)
                                                                        MaterialTheme.colorScheme.primary
                                                                    else colors.transparent,
                                                                    CustomShape.circleShape()
                                                                ),
                                                            onClick = {
                                                                displayMode = DisplayMode.ROW
                                                            },
                                                            contentDescription = "List view",
                                                            tint = if (displayMode == DisplayMode.ROW)
                                                                MaterialTheme.colorScheme.onPrimary
                                                            else
                                                                MaterialTheme.colorScheme.onSurface
                                                        )

                                                        ButtonIconComposable(
                                                            showBgColor = false,
                                                            buttonIcon = ButtonIcon.Vector(Icons.Filled.GridView),
                                                            modifier = Modifier
                                                                .background(
                                                                    if (displayMode == DisplayMode.GRID)
                                                                        MaterialTheme.colorScheme.primary
                                                                    else colors.transparent,
                                                                    CustomShape.circleShape()
                                                                ),
                                                            onClick = {
                                                                displayMode = DisplayMode.GRID
                                                            },
                                                            contentDescription = "Grid view",
                                                            tint = if (displayMode == DisplayMode.GRID)
                                                                MaterialTheme.colorScheme.onPrimary
                                                            else
                                                                MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }

                                // Filter section
                                item {
                                    FilterSection(
                                        filterState = filterState,
                                        filterActions = filterActions,
                                        filterData = FilterData(categories = availableCategories)
                                    )
                                }

                                // Product items
                                items(
                                    items = filteredItems,
                                    key = { it.id }
                                ) { product ->
                                    val productIdForCart = product.id

                                    val isFavorite by favoriteViewModel
                                        .getFavoriteStatus(product.id)
                                        .collectAsState(initial = false)

                                    val isInCart by cartViewModel
                                        .isInCart(productIdForCart)
                                        .collectAsState(initial = false)

                                    RowItemCard(
                                        product = product,
                                        isFavorite = isFavorite,
                                        isInCart = isInCart,
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedContentScope = animatedContentScope,
                                        onAddToCart = {
                                            if (isInCart) {
                                                cartViewModel.removeFromCart(product)
                                            } else {
                                                cartViewModel.addToCart(product)
                                            }
                                        },
                                        onAddToFavoriteClick = {
                                            favoriteViewModel.toggleFavorite(product)
                                        },
                                        onSignInClick = onSignInClick,
                                        onProductClick = {
                                            onProductClick(product)
                                        }
                                    )
                                }

                                // Related products
                                if (filteredItems.isNotEmpty()) {
                                    item {
                                        PaddedSection(
                                            content = {
                                                CustomHorizontalDivider()
                                            }
                                        )

                                        RelatedProductsSection(
                                            searchQuery = searchQuery,
                                            categoryName = if (searchQuery.isBlank()) initialCategory else "",
                                            onProductClick = { product ->
                                                onProductClick(product)
                                            },
                                            maxItems = 10,
                                            onSignInClick = onSignInClick
                                        )
                                    }
                                }
                            }
                        }

                        displayMode == DisplayMode.GRID -> {
                            // Header with filter controls (non-scrollable)

                            PaddedSection(
                                content = {
                                    CustomSpacer()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Same header content as above
                                        Column {
                                            Text(
                                                text = "${filteredItems.size} ${if (filteredItems.size == 1) "Item found" else "Items found"}",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = windowSizeConstant.bodyTextStyle,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(
                                                windowSizeConstant.normalVerticalPadding
                                            ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Same buttons as above
                                            ButtonIconComposable(
                                                showBgColor = false,
                                                buttonIcon = ButtonIcon.Vector(Icons.Filled.FilterList),
                                                tint = if (filterState.showFilters)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface,
                                                onClick = { filterActions.onToggleFilters() },
                                                contentDescription = "Filter"
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .background(
                                                        MaterialTheme.colorScheme.surfaceContainerHigh,
                                                        CustomShape.mediumShape()
                                                    )
                                            ) {
                                                ButtonIconComposable(
                                                    showBgColor = false,
                                                    buttonIcon = ButtonIcon.Vector(Icons.AutoMirrored.Filled.ViewList),
                                                    modifier = Modifier
                                                        .background(
                                                            if (displayMode == DisplayMode.ROW)
                                                                MaterialTheme.colorScheme.primary
                                                            else colors.transparent,
                                                            CustomShape.circleShape()
                                                        ),
                                                    onClick = { displayMode = DisplayMode.ROW },
                                                    contentDescription = "List view",
                                                    tint = if (displayMode == DisplayMode.ROW)
                                                        MaterialTheme.colorScheme.onPrimary
                                                    else
                                                        MaterialTheme.colorScheme.onSurface
                                                )

                                                ButtonIconComposable(
                                                    showBgColor = false,
                                                    buttonIcon = ButtonIcon.Vector(Icons.Filled.GridView),
                                                    modifier = Modifier
                                                        .background(
                                                            if (displayMode == DisplayMode.GRID)
                                                                MaterialTheme.colorScheme.primary
                                                            else colors.transparent,
                                                            CustomShape.circleShape()
                                                        ),
                                                    onClick = {
                                                        displayMode = DisplayMode.GRID
                                                    },
                                                    contentDescription = "Grid view",
                                                    tint = if (displayMode == DisplayMode.GRID)
                                                        MaterialTheme.colorScheme.onPrimary
                                                    else
                                                        MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            )

                            // Filter section (non-scrollable)
                            FilterSection(
                                filterState = filterState,
                                filterActions = filterActions,
                                filterData = FilterData(categories = availableCategories)
                            )

                            CustomSpacer()

                            // Grid (scrollable)
                            CustomLazyColumn {
                                // Product items
                                item {
                                    CustomLazyRow {
                                        items(
                                            items = filteredItems,
                                            key = { it.id }
                                        ) { product ->
                                            val isFavorite by favoriteViewModel
                                                .getFavoriteStatus(product.id)
                                                .collectAsState(initial = false)

                                            val isInCart by cartViewModel
                                                .isInCart(product.id)
                                                .collectAsState(initial = false)

                                            ProductCard(
                                                product = product,
                                                isFavorite = isFavorite,
                                                isInCart = isInCart,
                                                imageLoader = imageLoader,
                                                onProductClick = { onProductClick(product) },
                                                onAddToCart = {
                                                    if (isInCart) {
                                                        cartViewModel.removeFromCart(product)
                                                    } else {
                                                        cartViewModel.addToCart(product)
                                                    }
                                                },
                                                onFavoriteClick = {
                                                    favoriteViewModel.toggleFavorite(product)
                                                },
                                                onSignInClick = onSignInClick
                                            )
                                        }
                                    }
                                }

                                // Related products section
                                if (filteredItems.isNotEmpty()) {
                                    item {
                                        Column {
                                            PaddedSection(
                                                content = {
                                                    CustomHorizontalDivider()
                                                }
                                            )

                                            RelatedProductsSection(
                                                searchQuery = searchQuery,
                                                categoryName = if (searchQuery.isBlank()) initialCategory else "",
                                                onProductClick = { product ->
                                                    onProductClick(product)
                                                },
                                                maxItems = 10,
                                                onSignInClick = onSignInClick
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

/**
 * ErrorState - Displays an error message with a retry button
 *
 * @param message The error message to display
 * @param onRetry Callback triggered when the retry button is clicked
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(windowSizeClass.baseSize),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CustomIcon(
            icon = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            iconSize = windowSizeClass.largeIconSize,
            tint = MaterialTheme.colorScheme.error
        )

        CustomSpacer()

        Text(
            text = stringResource(R.string.error),
            style = windowSizeClass.titleTextStyle,
            fontWeight = FontWeight.Bold
        )

        CustomSpacer(modifier = Modifier.height(windowSizeClass.normalVerticalPadding))

        Text(
            text = message,
            style = windowSizeClass.bodyTextStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        CustomSpacer(modifier = Modifier.height(windowSizeClass.baseSize))

        CustomButton(onClick = onRetry, label = R.string.retry)
    }
}

/**
 * RowItemCard - Horizontal list item representation of a product
 *
 * @param modifier Optional modifier for the card
 * @param product The product data to display
 * @param sharedTransitionScope Scope for shared element transitions
 * @param animatedContentScope Scope for animation content
 * @param onAddToCart Callback for adding/removing product from cart
 * @param isInCart Whether the product is currently in the cart
 * @param isFavorite Whether the product is marked as favorite
 * @param onAddToFavoriteClick Callback for toggling favorite status
 * @param onProductClick Callback when the card is clicked
 * @param onSignInClick Callback to trigger sign-in dialog
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RowItemCard(
    modifier: Modifier = Modifier,
    product: ProductItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onAddToCart: () -> Unit = { },
    isInCart: Boolean = false,
    isFavorite: Boolean = false,
    onAddToFavoriteClick: () -> Unit,
    onProductClick: () -> Unit,
    onSignInClick: () -> Unit
) {

    val windowSizeClass = LocalWindowSizeConstant.current

    PaddedSection(
        content = {
            CustomItemCard(
                modifier,
                product = product,
                onSignInClick = onSignInClick,
                onProductClick = { onProductClick() },
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                actions = { isAuthenticated, showSignInDialog ->
                    // Favorite toggle button
                    FavoriteSplashButton(
                        isFavorite = isFavorite,
                        onToggle = {
                            if (isAuthenticated) {
                                onAddToFavoriteClick
                            } else {
                                showSignInDialog() // Trigger the dialog
                            }
                        }
                    )

                    CustomSpacer(modifier = Modifier.width(windowSizeClass.basePadding))

                    // Add to Cart button
                    CustomCartButton(
                        isInCart = isInCart,
                        onAddToCart = {
                            if (isAuthenticated) {
                                onAddToCart
                            } else {
                                showSignInDialog() // Trigger the dialog
                            }
                        },
                        useRoundedButton = true
                    )
                },
            )
        }
    )
}

/**
 * EmptyFilterState - Displays when a search returns no matches
 *
 * @param searchQuery The query that returned no results
 * @param viewModel ViewModel to handle search clearing
 * @param filterActions Actions to handle filter resetting
 * @param onClearFilters Callback to clear all active filters
 */
@Composable
private fun EmptyFilterState(
    searchQuery: String,
    viewModel: ProductCrudViewModel,
    filterActions: FilterActions,
    onClearFilters: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(windowSizeClass.baseSize),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CustomIcon(
            icon = Icons.Filled.SearchOff,
            contentDescription = "No results",
            iconSize = windowSizeClass.largeIconSize,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CustomSpacer(modifier = Modifier.height(windowSizeClass.normalVerticalPadding))

        HeadlineWidget(
            middleTextStr = "No exact matches for \"$searchQuery\"",
            subMiddleText = R.string.adjust_search
        )

        CustomSpacer(modifier = Modifier.height(windowSizeClass.baseSize))

        CustomOutlinedButton(
            icon = ButtonIcon.Vector(Icons.Filled.Clear),
            onClick = {
                onClearFilters()
                viewModel.clearSearch()
                filterActions.onClearFilters()
            },
            label = R.string.clear_filter
        )
    }
}