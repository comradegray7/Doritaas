package com.example.myapp.view.screens.product.product_rating_and_reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.admin.CustomSearchBar
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomOutlinedButton
import com.example.myapp.view.utils.ButtonIcon

/**
 * ProductReviewsScreen - Screen for displaying user reviews for a specific product.
 *
 * Use this screen to:
 * - View a list of customer reviews and ratings.
 * - Search through existing reviews.
 * - Filter reviews (if implemented in future).
 * - Mark reviews as helpful.
 *
 * @param productId The unique identifier of the product whose reviews are being displayed.
 * @param viewModel The ViewModel that manages the reviews data and UI state.
 * @param onNavigateBack Callback to be invoked when the back navigation is triggered.
 */
@Composable
fun ProductReviewsScreen(
    productId: String,
    viewModel: ProductCrudViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val reviews by viewModel.reviews.collectAsState()
    val currentUserReview by viewModel.currentUserReview.collectAsState()
    val uiState by viewModel.productState.collectAsState()
    val windowSizeConstant = LocalWindowSizeConstant.current

    var showRatingDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val networkState = rememberNetworkState(networkManager)
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    // Get the current product from the loaded products
    val currentProduct = remember(uiState.products, productId) {
        uiState.products.find { it.id == productId }
    }

    // Load data when screen opens
    LaunchedEffect(productId) {
        viewModel.loadProducts()
        viewModel.loadCurrentUserReview(productId)
        viewModel.loadProductReviews(productId)
    }

    // Clear reviews when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearReviewsState()
        }
    }

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
                viewModel.loadProducts()
                viewModel.loadCurrentUserReview(productId)
                viewModel.loadProductReviews(productId)
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }
        },
        title = R.string.customer_reviews,
        showBottomBar = false,
        onNavigateBack = onNavigateBack,
        verticalArrangement = Arrangement.Top,
        floatingBtnContent = {
            CustomOutlinedButton(
                labelStr = if (currentUserReview != null) "Update Review" else "Add Review",
                onClick = { showRatingDialog = true },
                icon = ButtonIcon.Vector(Icons.Filled.Add),
            )
        },
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

            // Show dialog when triggered and product is loaded
            if (showRatingDialog && currentProduct != null) {
                ProductRatingDialog(
                    productName = currentProduct.productName,
                    currentUserRating = currentUserReview?.rating ?: 0f,
                    currentReviewText = currentUserReview?.review ?: "",
                    onDismiss = {
                        showRatingDialog = false
                    },
                    onSubmitRating = { rating, review ->
                        viewModel.submitProductRating(
                            productId = currentProduct.id,
                            rating = rating,
                            review = review ?: ""
                        )
                        showRatingDialog = false
                        // Reload reviews after submission
                        viewModel.loadProductReviews(productId)
                    }
                )
            }

            Column {
                PaddedSection(
                    content = {
                        // Search Bar
                        CustomSearchBar(
                            query = searchQuery,
                            onQueryChange = { newQuery ->
                                searchQuery = newQuery
                                if (newQuery.isNotEmpty()) {
                                    viewModel.searchReviews(newQuery)
                                } else {
                                    viewModel.loadProductReviews(productId)
                                }
                            },
                            onSearch = { query ->
                                viewModel.searchReviews(query)
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
                                    stringResource(R.string.search_reviews),
                                    style = windowSizeConstant.bodyTextStyle,
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
                                            viewModel.loadProductReviews(productId)
                                        },
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        )

                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall))

                        // Reviews List
                        when {
                            uiState.isLoading -> {
                                CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall))
                                CustomListCardShimmer()
                            }

                            reviews.isEmpty() -> {
                                CustomEmptyState(
                                    title = R.string.no_reviews,
                                    subTitle = R.string.be_first_reviews,
                                    showBtn = false,
                                    leadingIcon = Icons.Outlined.SearchOff
                                )
                            }

                            else -> {
                                // Show result count for search
                                if (searchQuery.isNotEmpty()) {
                                    Text(
                                        "${reviews.size} review${if (reviews.size != 1) "s" else ""} found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = windowSizeConstant.normalVerticalPadding)
                                    )
                                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))
                                }

                                CustomLazyColumn {
                                    items(
                                        items = reviews,
                                        key = { it.id }
                                    ) { review ->
                                        ReviewItem(
                                            review = review,
                                            onMarkHelpful = {
                                                viewModel.markReviewHelpful(review.id)
                                            }
                                        )
                                    }

                                    item{
                                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall))
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    )
}