package com.example.myapp.view.screens.product.promotions

import android.util.Log
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.Offer
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.CartViewModel
import com.example.myapp.data.model.CategoryViewModel
import com.example.myapp.data.model.FavoriteViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomLazyRow
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomSurfaceContainer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.HeroCard
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.ProductShimmer
import com.example.myapp.view.components.custom.buttons.ShoppingCartBadge
import com.example.myapp.view.screens.product.ProductCard
import com.example.myapp.view.utils.CustomShape

// ============================================================================
// DAILY ESSENTIALS SCREEN - Uses Tags
// ============================================================================
/**
 * DailyEssentialsScreen - A screen showcasing daily essential products.
 *
 * Displays a curated list of "daily essential" items, categorized for easy browsing.
 * Features:
 * - Dynamic category generation based on product data or predefined logical groups.
 * - Filtering products by selected category.
 * - displaying a hero banner for daily essentials.
 * - Quick stats about the essentials collection (e.g., product count, max savings).
 * - "Top Deals" section for high-value items.
 * - Grid view of essential products with sorting by rating.
 *
 * @param onBackNavigation Callback to navigate back to the previous screen.
 * @param viewModel [ProductCrudViewModel] for fetching product data.
 * @param categoryViewModel [CategoryViewModel] for handling categories.
 * @param favoriteViewModel [FavoriteViewModel] for managing favorite items.
 * @param cartViewModel [CartViewModel] for managing cart items.
 * @param onProductClick Callback when a product is clicked.
 * @param onSignInClick Callback to initiate sign-in flow.
 * @param onCartClick Callback to navigate to the cart.
 * @param onCategoryClick Callback when a category is clicked (optional).
 * @param networkManager Manager for checking network connectivity.
 */

@Composable
fun DailyEssentialsScreen(
    onBackNavigation: () -> Unit,
    viewModel: ProductCrudViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    onProductClick: (ProductItem) -> Unit,
    onSignInClick: () -> Unit,
    onCartClick: () -> Unit,
    onCategoryClick: (String) -> Unit = {},
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val networkState = rememberNetworkState(networkManager)

    val snackBarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.productState.collectAsState()
    val categoryState by categoryViewModel.categoryState.collectAsState()
    val badgeNumber = cartViewModel.cartItems
    val imageLoader = viewModel.getImageLoader()
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf("all") }
    var dailyEssentialsParentId by remember { mutableStateOf<String?>(null) }

    //   Build categories - try multiple approaches
    val essentialCategories = remember(
        categoryState.categories,
        categoryState.selectedSubcategories,
        categoryState.categoryTree
    ) {
        buildList {
            add(
                EssentialCategory(
                    "all",
                    "All Items",
                    Icons.Filled.Apps,
                    Color(0xFF667eea)
                )
            )

            if (categoryState.selectedSubcategories.isNotEmpty()) {
                Log.d(
                    "DailyEssentials",
                    "Using selectedSubcategories: ${categoryState.selectedSubcategories.size}"
                )
                categoryState.selectedSubcategories.forEach { category ->
                    add(
                        EssentialCategory(
                            id = category.id,
                            name = category.categoryName,
                            icon = getCategoryIcon(category.categoryName),
                            color = getCategoryColor(category.categoryName)
                        )
                    )
                }
            } else if (categoryState.categoryTree.isNotEmpty()) {
                val dailyEssentialsNode = categoryState.categoryTree.find {
                    it.category.categoryName.contains("Daily", ignoreCase = true) ||
                            it.category.categoryName.contains("Essential", ignoreCase = true)
                }

                Log.d(
                    "DailyEssentials",
                    "Found node: ${dailyEssentialsNode?.category?.categoryName}"
                )
                Log.d(
                    "DailyEssentials",
                    "Node has ${dailyEssentialsNode?.subcategories?.size} subcategories"
                )

                dailyEssentialsNode?.subcategories?.forEach { subNode ->
                    add(
                        EssentialCategory(
                            id = subNode.category.id,
                            name = subNode.category.categoryName,
                            icon = getCategoryIcon(subNode.category.categoryName),
                            color = getCategoryColor(subNode.category.categoryName)
                        )
                    )
                }
            } else if (categoryState.categories.isNotEmpty() && dailyEssentialsParentId != null) {
                Log.d("DailyEssentials", "Using parentId approach: $dailyEssentialsParentId")
                categoryState.categories
                    .filter { it.parentId == dailyEssentialsParentId }
                    .forEach { category ->
                        add(
                            EssentialCategory(
                                id = category.id,
                                name = category.categoryName,
                                icon = getCategoryIcon(category.categoryName),
                                color = getCategoryColor(category.categoryName)
                            )
                        )
                    }
            } else if (categoryState.categories.isNotEmpty()) {
                Log.d("DailyEssentials", "Ultimate fallback - showing level 1 categories")
                categoryState.categories
                    .filter { it.level == 1 }
                    .take(6) // Limit to 6
                    .forEach { category ->
                        add(
                            EssentialCategory(
                                id = category.id,
                                name = category.categoryName,
                                icon = getCategoryIcon(category.categoryName),
                                color = getCategoryColor(category.categoryName)
                            )
                        )
                    }
            }

            Log.d("DailyEssentials", "Total essential categories built: ${this.size}")
        }
    }

    val essentialCard = Offer(
        id = "3",
        title = "Daily Essentials",
        description = "Save on everyday items",
        buttonText = "Save Up to 30%",
        gradient = listOf(colors.customColor11, colors.customColor12),
        leadingIcon = Icons.Filled.ShoppingBasket
    )

    //  Get all category IDs for filtering
    val dailyEssentialsCategoryIds = remember(essentialCategories) {
        // Get all category IDs except "all"
        essentialCategories
            .filter { it.id != "all" }
            .map { it.id }
            .toSet()
            .also {
                Log.d("DailyEssentials", "Category IDs for filtering: $it")
            }
    }

    //  Filter products - Check BOTH categoryId AND category name
    val essentialProducts = remember(
        uiState.products,
        selectedCategory,
        dailyEssentialsCategoryIds,
        essentialCategories
    ) {
        val filtered = if (selectedCategory == "all") {
            // If we have specific category IDs, filter by them OR by category name
            if (dailyEssentialsCategoryIds.isNotEmpty()) {
                val categoryNames = essentialCategories
                    .filter { it.id != "all" }
                    .map { it.name.lowercase().trim() }
                    .toSet()

                uiState.products.filter { product ->
                    // Match by ID
                    product.categoryId in dailyEssentialsCategoryIds ||
                            // OR match by name (fallback)
                            product.category.lowercase().trim() in categoryNames ||
                            product.categoryId.lowercase().trim() in categoryNames
                }
            } else {
                // Otherwise show all products
                uiState.products
            }
        } else {
            // Show products in the selected subcategory
            val selectedCategoryName = essentialCategories
                .find { it.id == selectedCategory }?.name?.lowercase()?.trim()

            uiState.products.filter { product ->
                // Match by ID
                product.categoryId == selectedCategory ||
                        // OR match by name
                        (selectedCategoryName != null && (
                                product.category.lowercase().trim() == selectedCategoryName ||
                                        product.categoryId.lowercase()
                                            .trim() == selectedCategoryName
                                ))
            }
        }

        Log.d(
            "DailyEssentials",
            "Filtered products: ${filtered.size} (selectedCategory: $selectedCategory)"
        )

        filtered.sortedByDescending { it.rating }
    }

    //   Load data on first launch
    LaunchedEffect(Unit) {
        Log.d("DailyEssentials", "Initial load started")
        if (categoryState.categories.isEmpty()) {
            categoryViewModel.loadCategories()
        }
        if (categoryState.categoryTree.isEmpty()) {
            categoryViewModel.loadCategoryTree()
        }
        if (uiState.products.isEmpty()) {
            viewModel.loadProducts()
        }
    }

    //   Find "Daily Essentials" and load its subcategories
    LaunchedEffect(categoryState.categories) {
        if (categoryState.categories.isNotEmpty() && dailyEssentialsParentId == null) {
            val dailyEssentialsCategory = categoryState.categories.find {
                it.categoryName.contains("Daily", ignoreCase = true) ||
                        it.categoryName.contains("Essential", ignoreCase = true)
            }

            dailyEssentialsCategory?.let { category ->
                dailyEssentialsParentId = category.id
                categoryViewModel.loadSubcategories(category.id)
            }
        }
    }

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
                viewModel.refreshProducts()
                categoryViewModel.loadCategories()
                categoryViewModel.loadCategoryTree()
                dailyEssentialsParentId?.let {
                    categoryViewModel.loadSubcategories(it)
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
        onNavigateBack = onBackNavigation,
        snackBarHostState = snackBarHostState,
        title = R.string.daily_essentials,
        showBottomBar = false,
        verticalArrangement = Arrangement.Top,
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
                uiState.isLoading || categoryState.isLoading -> {

                    ProductShimmer()
                }

                uiState.error != null -> {
                    CustomEmptyState(
                        btnLabel = R.string.retry,
                        title = R.string.promotions_error,
                        onBtnClick = {
                            viewModel.refreshProducts()
                            categoryViewModel.loadCategories()
                        },
                        leadingIcon = Icons.Filled.Error,
                        enableScroll = false
                    )
                }

                else -> {
                    CustomLazyColumn {
                        // Hero Banner
                        item {
                            CustomSpacer()
                            PaddedSection(
                                alignment = Alignment.CenterHorizontally,
                                content = {
                                    HeroCard(
                                        offer = essentialCard
                                    )
                                }
                            )
                        }

                        // Categories
                        item {
                            PaddedSection(
                                content = {
                                    HeadlineWidget(
                                        leadingText = R.string.shop_by_category
                                    )
                                }
                            )

                            CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                            CustomLazyRow {
                                items(essentialCategories) { category ->
                                    CategoryChip(
                                        category = category,
                                        selected = selectedCategory == category.id,
                                        onClick = { selectedCategory = category.id }
                                    )
                                }
                            }
                        }

                        // Quick Stats
                        item {
                            CustomLazyRow {
                                item {
                                    StatCard(
                                        icon = Icons.Filled.Inventory,
                                        value = "${essentialProducts.size}",
                                        label = "Products",
                                        color = colors.customColor5
                                    )
                                }

                                item {
                                    StatCard(
                                        icon = Icons.Filled.LocalOffer,
                                        value = "30%",
                                        label = "Max Savings",
                                        color = colors.customColor6
                                    )
                                }

                                item {
                                    StatCard(
                                        icon = Icons.Filled.LocalShipping,
                                        value = "Free",
                                        label = "Delivery",
                                        color = colors.customColor9
                                    )
                                }
                            }
                        }

                        // Featured Deals
                        if (selectedCategory == "all" && essentialProducts.isNotEmpty()) {
                            item {
                                PaddedSection(
                                    content = {
                                        HeadlineWidget(
                                            leadingText = R.string.top_deals,
                                            trailing = {
                                                Surface(
                                                    shape = CustomShape.mediumShape(),
                                                    color = colors.customColor6.copy(alpha = 0.1f)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(
                                                            horizontal = windowSizeConstant.baseNormalVerticalPadding,
                                                            vertical = customSpacing.custom6
                                                        ),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        CustomIcon(
                                                            icon = Icons.Filled.Timer,
                                                            contentDescription = "Local offer",
                                                            tint = colors.customColor6
                                                        )

                                                        CustomSpacer(
                                                            modifier = Modifier.width(
                                                                windowSizeConstant.smallVerticalPadding
                                                            )
                                                        )


                                                        Text(
                                                            stringResource(R.string.limited_time_left),
                                                            style = windowSizeConstant.labelTextStyle.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = colors.customColor6
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    })

                                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                                CustomLazyRow {
                                    items(essentialProducts.take(5)) { product ->
                                        val isFavorite by favoriteViewModel.getFavoriteStatus(
                                            product.id
                                        ).collectAsState(initial = product.isFavorite)

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

                                CustomSpacer()
                            }
                        }

                        // Products Section Header
                        item {
                            PaddedSection(
                                content = {
                                    HeadlineWidget(
                                        leadingStr = if (selectedCategory == "all") "All Essentials"
                                        else essentialCategories.find { it.id == selectedCategory }?.name
                                            ?: "Products",
                                        subMiddleTextStr = "${essentialProducts.size} items available"
                                    )

                                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))
                                }
                            )
                        }

                        // Products Grid
                        if (essentialProducts.isEmpty()) {
                            item {
                                CustomEmptyState(
                                    btnLabel = R.string.browse_products,
                                    title = R.string.no_products_found,
                                    onBtnClick = onBackNavigation,
                                    enableScroll = false,
                                    leadingIcon = Icons.Filled.SearchOff,
                                )
                            }
                        } else {
                            item {
                                CustomLazyRow {
                                    items(
                                        items = essentialProducts,
                                        key = { it.id }
                                    ) { product ->
                                        val isFavorite by favoriteViewModel.getFavoriteStatus(
                                            product.id
                                        ).collectAsState(initial = product.isFavorite)

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
                        }
                    }
                }
            }
        },
        actions = {
            // Network Indicator in top bar
            NetworkIndicator(networkState = networkState)

            if (!uiState.isLoading) {
                ShoppingCartBadge(badgeNumber, onCartClick)
            }
        }
    )
}

// ✅ Helper function to map category names to icons
private fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "groceries" -> Icons.Filled.ShoppingBasket
        "household" -> Icons.Filled.Home
        "personal care", "care" -> Icons.Filled.Face
        "baby", "baby products" -> Icons.Filled.ChildCare
        "health", "health care" -> Icons.Filled.LocalHospital
        "electronics" -> Icons.Filled.Devices
        "clothing", "fashion" -> Icons.Filled.Checkroom
        else -> Icons.Filled.Category
    }
}

//Helper function to map category names to colors
private fun getCategoryColor(categoryName: String): Color {
    return when (categoryName.lowercase()) {
        "groceries" -> Color(0xFF11998e)
        "household" -> Color(0xFFFF6B35)
        "personal care" -> Color(0xFFE91E63)
        "baby", "baby products" -> Color(0xFF9C27B0)
        "health" -> Color(0xFF4CAF50)
        "electronics" -> Color(0xFF667eea)
        "clothing", "fashion" -> Color(0xFF38ef7d)
        else -> Color(0xFF764ba2)
    }
}

/**
 * EssentialCategory - Category data for daily essentials.
 *
 * Reputation of a category in the Daily Essentials screen, including visual properties.
 *
 * @param id The unique identifier of the category.
 * @param name The display name of the category.
 * @param icon The icon associated with the category.
 * @param color The theme color associated with the category.
 */
data class EssentialCategory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color
)

/**
 * CategoryChip - A clickable chip representing a product category.
 *
 * Displays a category with its icon and name. Changes visual state when selected.
 *
 * @param category The [EssentialCategory] to display.
 * @param selected Whether this category is currently selected.
 * @param onClick Callback when the chip is clicked.
 */
@Composable
fun CategoryChip(
    category: EssentialCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomSurfaceContainer(
        modifier = Modifier.padding(windowSizeClass.normalVerticalPadding),
        onClick = onClick,
        color = if (selected) category.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        icon = category.icon,
        iconSize = windowSizeClass.basePadding,
        textStr = category.name,
        contentDescription = "flash",
        width = if (selected) windowSizeClass.borderSize else windowSizeClass.smallSizes,
        borderColor = if (selected) category.color else MaterialTheme.colorScheme.outline,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        textColor = if (selected) category.color else MaterialTheme.colorScheme.onSurface
    )
}

/**
 * StatCard - A card displaying a statistic.
 *
 * Shows an icon, a large value, and a label to highlight key figures (e.g., "30% Off", "Free Delivery").
 *
 * @param icon The icon illustrating the statistic.
 * @param value The main value text (e.g., "500+").
 * @param label The label description (e.g., "Products").
 * @param color The theme color for the card and its content.
 */
@Composable
fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {

    val windowSizeConstant = LocalWindowSizeConstant.current

    Card(
        modifier = Modifier.width(windowSizeConstant.customSpacerLarge),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = CustomShape.mediumShape()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeConstant.baseNormalVerticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomIcon(
                icon = icon,
                contentDescription = "Stat icon",
                tint = color,
            )

            CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseNormalVerticalPadding))

            Text(
                text = value,
                style = windowSizeConstant.bodyTextStyle
            )

            Text(
                text = label,
                style = windowSizeConstant.labelTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}