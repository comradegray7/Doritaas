package com.example.myapp.view.screens.product

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.model.CartViewModel
import com.example.myapp.data.model.FavoriteViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomLazyRow
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.ProductCardShimmer

/**
 * RelatedProductsSection - Product recommendation component
 *
 * Displays a horizontal scrollable list of related or similar products based on
 * category, search query, or product similarity. Helps users discover additional
 * products they might be interested in.
 *
 * ## Features
 * - **Smart Product Matching**: Finds related products by category, search, or similarity
 * - **Exclusion Logic**: Automatically excludes the current product from recommendations
 * - **Configurable Limit**: Control maximum number of recommendations shown
 * - **Loading States**: Shows shimmer placeholders while loading
 * - **Cart Integration**: Add products to cart directly from recommendations
 * - **Favorite Integration**: Toggle favorite status on recommended products
 * - **Dynamic Titles**: Shows contextual titles ("Related to..." or "You may also like")
 *
 * ## Matching Strategies
 * 1. **Search Query**: If provided, finds products matching the search term
 * 2. **Category ID**: If provided, finds products in the same category
 * 3. **Category Name**: If provided, searches by category name
 * 4. **Fallback**: Uses similarity algorithm when other methods unavailable
 *
 * ## User Workflow
 * 1. Component loads related products based on provided criteria
 * 2. User sees horizontal scrollable list of product cards
 * 3. User can click product to view details
 * 4. User can add to cart or favorites directly from list
 * 5. Current product is automatically excluded from results
 *
 * ## UI Components
 * - Section title with context-aware text
 * - Horizontal scrollable product list
 * - Individual product cards with actions
 * - Shimmer loading placeholders
 *
 * @param modifier Modifier for the section container
 * @param categoryId Category ID to find related products
 * @param categoryName Category name for better display and fallback matching
 * @param searchQuery Search term to find related products
 * @param currentProductId ID of current product to exclude from results
 * @param onProductClick Callback when a product is clicked
 * @param viewModel ViewModel for product operations
 * @param favoriteViewModel ViewModel for favorite operations
 * @param cartViewModel ViewModel for cart operations
 * @param maxItems Maximum number of products to display (default: 10)
 * @param onSignInClick Callback when sign-in is required
 *
 * @see ProductCard for individual product display
 * @see ProductDescriptionScreen for usage example
 * @see ProductCrudViewModel for product data operations
 */
@Composable
fun RelatedProductsSection(
    modifier: Modifier = Modifier,
    categoryId: String = "",
    categoryName: String = "",  
    searchQuery: String = "",
    currentProductId: String = "",
    onProductClick: (ProductItem) -> Unit,
    viewModel: ProductCrudViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    maxItems: Int = 10, 
    onSignInClick: () -> Unit
) {
    var relatedProducts by remember { mutableStateOf<List<ProductItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val imageLoader = viewModel.getImageLoader()
    val uiState by viewModel.productState.collectAsState()
    val windowSizeClass = LocalWindowSizeConstant.current

    // Load related products based on search query or category
    LaunchedEffect(searchQuery, categoryId, currentProductId) {
        when {
            searchQuery.isNotBlank() -> {
                isLoading = true
                viewModel.searchProductsWithFallback(searchQuery)
            }

            categoryId.isNotBlank() -> {
                isLoading = true
                viewModel.loadProductsByCategory(categoryId)
            }

            categoryName.isNotBlank() -> {
                isLoading = true
                // Search by category name if ID not available
                viewModel.searchProductsWithFallback(categoryName)

                viewModel.loadSimilarProducts(
                    query = categoryName,
                    excludeId = currentProductId,
                    limit = maxItems
                )
            }
        }
    }

    // Process products based on source
    LaunchedEffect(uiState.products, uiState.searchResults, searchQuery, categoryId) {
        relatedProducts = when {
            searchQuery.isNotBlank() && uiState.searchResults != null -> {
                uiState.searchResults!!
                    .filter { it.id != currentProductId }
                    .take(maxItems)
            }

            (categoryId.isNotBlank() || categoryName.isNotBlank()) -> {
                uiState.products
                    .filter { it.id != currentProductId }
                    .take(maxItems)
            }

            else -> emptyList()
        }
        isLoading = false
    }

    // Only show if we have related products
    if (relatedProducts.isNotEmpty()) {
        Column(modifier = modifier.padding(vertical = windowSizeClass.customSpacerSmall)) {
            PaddedSection(
                content = {
                    HeadlineWidget(
                        leadingStr = if (searchQuery.isNotBlank())
                            "Related to \"$searchQuery\""
                        else
                            "You may also like",
                    )
                }
            )

            CustomSpacer()

            if (isLoading) {
                CustomLazyRow {
                    items(5) {
                        PaddedSection(
                            content = {
                                ProductCardShimmer()
                            }
                        )
                    }
                }
            } else {
                CustomLazyRow {
                    items(
                        items = relatedProducts,
                        key = { it.id }
                    ) { product ->
                        val productIdForCart = product.id

                        val isFavorite by favoriteViewModel
                            .getFavoriteStatus(product.id)
                            .collectAsState(initial = false)

                        val isInCart by cartViewModel
                            .isInCart(productIdForCart)
                            .collectAsState(initial = false)

                        ProductCard(
                            product = product,
                            imageLoader = imageLoader,
                            isFavorite = isFavorite,
                            isInCart = isInCart,
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

 