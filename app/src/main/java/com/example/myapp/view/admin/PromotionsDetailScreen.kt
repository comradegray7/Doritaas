package com.example.myapp.view.admin

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.data.model.PromotionViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomFloatingPointButton
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.utils.ButtonIcon
import kotlinx.coroutines.delay

/**
 * PromotionDetailsScreen - Interface for managing products within a specific promotion
 * 
 * Allows administrators to view all products currently assigned to a promotion,
 * add new products individually or in bulk (by category/tag), and remove products.
 * 
 * ## Features
 * - **Product Search**: Filter promotion products by name, category, or tag
 * - **Single Addition**: Select specific products to add from the catalog
 * - **Bulk Addition**: Add all products from a specific category or with a specific tag
 * - **Removal**: Quickly detach products from the promotional campaign
 * 
 * @param promotionId The unique identifier of the promotion being managed
 * @param viewModel ViewModel for promotion and product assignment operations
 * @param productViewModel ViewModel for accessing the general product catalog
 * @param onNavigateBack Callback for returning to the promotions list
 * @param sharedTransitionScope Shared transition context for image animations
 * @param animatedContentScope Animation scope for transition effects
 * @param networkManager Manager for tracking connectivity status
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PromotionDetailsScreen(
    promotionId: String,
    viewModel: PromotionViewModel = hiltViewModel(),
    productViewModel: ProductCrudViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val networkState = rememberNetworkState(networkManager)
    val promotionState by viewModel.promotionState.collectAsState()
    val productState by productViewModel.productState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showBulkAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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

    // Get products for this promotion
    val currentPromotionProducts = remember(promotionId, promotionState.promotionProductsMap) {
        promotionState.promotionProductsMap[promotionId] ?: emptyList()
    }

    LaunchedEffect(promotionId) {
        viewModel.loadProductsForPromotion(promotionId)
        productViewModel.loadProducts()
    }

    CustomScaffoldContainer(
        title = R.string.manage_promotions,
        onRefresh =  {

             viewModel.refreshPromotionDetails(promotionId)
            productViewModel.loadProducts()
        },
        snackBarHostState = snackBarHostState,
        showBottomBar = false,
        onNavigateBack = onNavigateBack,
        verticalArrangement = Arrangement.Top,
        floatingBtnContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
            ) {
                // Bulk Add
                CustomFloatingPointButton(
                    onClick = { showBulkAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    icon = Icons.Filled.AddBusiness
                )

                // Single Add
                CustomFloatingPointButton(
                    icon = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                    onClick = { showAddProductDialog = true }
                )
            }
        },
        content = {
            // Network Status Banner
            // Network Indicator in top bar

            if (!networkState.hasInternet) {
                NetworkIndicator(networkState = networkState)

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
                content = {
                    // Search Bar
                    CustomSpacer()
                    CustomSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = {},
                        leadingIcon = {
                            CustomIcon(
                                icon = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        placeholder = {
                            Text(
                                stringResource(R.string.search_products),
                                style = windowSizeConstant.bodyTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                ButtonIconComposable(
                                    showBgColor = false,
                                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Close),
                                    onClick = { searchQuery = "" },
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    )
                    CustomSpacer()

                    when {
                        // Loading State
                        promotionState.loadingPromotionId == promotionId -> {
                            CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall))

                            CustomListCardShimmer()
                        }

                        // Empty State
                        currentPromotionProducts.isEmpty() -> {
                            CustomEmptyState(
                                title = R.string.no_promotion_product,
                                subTitle = R.string.add_products,
                                showBtn = true,
                                btnLabel = R.string.add_products,
                                onBtnClick = { showAddProductDialog = true },
                                leadingIcon = Icons.Filled.Inventory,
                            )
                        }

                        // Products List
                        else -> {

                            val filteredProducts = remember(searchQuery, currentPromotionProducts) {
                                if (searchQuery.isBlank()) {
                                    currentPromotionProducts
                                } else {
                                    currentPromotionProducts.filter { product ->
                                        product.productName.contains(
                                            searchQuery,
                                            ignoreCase = true
                                        ) ||
                                                product.description.contains(
                                                    searchQuery,
                                                    ignoreCase = true
                                                ) ||
                                                product.category.contains(
                                                    searchQuery,
                                                    ignoreCase = true
                                                ) ||
                                                product.tags.any { tag ->
                                                    tag.contains(searchQuery, ignoreCase = true)
                                                }
                                    }
                                }
                            }

                            if (filteredProducts.isEmpty()) {
                                CustomEmptyState(
                                    title = R.string.no_products_found,
                                    subTitle = R.string.try_different_search,
                                    leadingIcon = Icons.Filled.SearchOff,
                                )
                            } else {
                                CustomLazyColumn {

                                    items(
                                        items = filteredProducts,
                                        key = { it.id }
                                    ) { product ->
                                        ProductManagementCard(
                                            sharedTransitionScope = sharedTransitionScope,
                                            animatedContentScope = animatedContentScope,
                                            onProductClick = {},
                                            product = product,
                                            actions = {  isAuthenticated, showSignInDialog ->
                                                if (isAuthenticated) {
                                                    ButtonIconComposable(
                                                        showBgColor = false,
                                                        buttonIcon = ButtonIcon.Vector(Icons.Filled.RemoveCircle),
                                                        onClick = {
                                                            viewModel.removeProductFromPromotion(promotionId, product.id)
                                                        },
                                                        contentDescription = "Remove from promotion",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(windowSizeConstant.customSpacerSmall)
                                                    )
                                                } else {
                                                    showSignInDialog()
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    )

    // Dialogs
    if (showAddProductDialog) {
        AddProductToPromotionDialog(
            availableProducts = productState.products.filter { product ->
                !currentPromotionProducts.any { it.id == product.id }
            },
            onDismiss = { showAddProductDialog = false },
            onConfirm = { selectedProductIds ->
                selectedProductIds.forEach { productId ->
                    viewModel.addProductToPromotion(promotionId, productId)
                }
                showAddProductDialog = false
            }
        )
    }

    if (showBulkAddDialog) {
        BulkAddProductsDialog(
            categories = productState.categories,
            tags = productState.tags,
            onDismiss = { showBulkAddDialog = false },
            onAddByTag = { tag ->
                viewModel.addProductsByTag(promotionId, tag)
                showBulkAddDialog = false
            },
            onAddByCategory = { categoryId ->
                viewModel.addProductsByCategory(promotionId, categoryId)
                showBulkAddDialog = false
            }
        )
    }
}






