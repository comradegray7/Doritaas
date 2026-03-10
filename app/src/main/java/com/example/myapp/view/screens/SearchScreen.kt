package com.example.myapp.view.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.model.CartViewModel
import com.example.myapp.data.model.FavoriteViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.data.model.SearchViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.ClickableSearchBarShimmer
import com.example.myapp.view.components.CustomFilterChip
import com.example.myapp.view.components.CustomHorizontalDivider
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomLazyRow
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.SearchListShimmer
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.screens.product.ProductSection
import com.example.myapp.view.screens.product_search.PhotoSearchButton
import com.example.myapp.view.screens.product_search.VoiceSearchButton
import com.example.myapp.view.utils.ButtonIcon

/**
 * SearchScreen - Composable for the product search experience.
 *
 * This screen provides a search bar, recent/popular items, and search results.
 * - Shows a shimmer while loading.
 * - Filters a static list of items based on the user's query.
 * - Displays recent searches or popular items when the query is empty.
 * - Shows search results or a "no results" message when the user types.
 * - Handles navigation back and result selection via callbacks.
 *
 * @param onBackNavigation Callback for when the user navigates back.
 * @param onResultClick Callback for when a search result is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackNavigation: () -> Unit,
    onResultClick: (ProductItem) -> Unit = {},
    viewModel: ProductCrudViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    onProductClick: (ProductItem) -> Unit = {},
    searchViewModel: SearchViewModel = hiltViewModel(),
    navigateToFilter: (searchQuery: String) -> Unit = {},
    onSignInClick: () -> Unit,
    onAllProductsClick: () -> Unit,
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager

) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val uiState by viewModel.productState.collectAsState()
    val searchUiState by searchViewModel.uiState.collectAsState()
    val imageLoader = viewModel.getImageLoader()
    val context = LocalContext.current
    val loading by viewModel.isLoading.collectAsState()
    val networkState = rememberNetworkState(networkManager)
    val windowSizeClass = LocalWindowSizeConstant.current
    val recentSearches = searchUiState.recentSearches
    val popularSearches = searchUiState.popularSearches

    val trendingProducts = remember(uiState.trendingProducts) {
        uiState.trendingProducts.ifEmpty {
            // Fallback: Show regular products if no trending tagged
            uiState.products.sortedByDescending { it.rating }.take(10)
        }
    }

    val featuredProducts = remember(uiState.featuredProducts) {
        uiState.featuredProducts.ifEmpty {
            // Fallback: Show regular products if no featured tagged
            uiState.products.filter { it.oldPrice > 0 }.take(10)
        }
    }

    // State for the search query
    var query by rememberSaveable { mutableStateOf("") }

    // Handle image search completion
    LaunchedEffect(searchUiState.imageSearchCompleted) {
        if (searchUiState.imageSearchCompleted) {
            viewModel.setLoading(false)
            val query = searchUiState.currentQuery
            if (query.isNotBlank()) {
                searchViewModel.addRecentSearch(query)
                navigateToFilter(query)
                searchViewModel.clearImageSearchFlag()
            }
        }
    }

    LaunchedEffect(searchUiState.error) {
        searchUiState.error?.let { error ->
            Toast.makeText(context, "Search error: $error", Toast.LENGTH_SHORT).show()
        }
    }

    CustomScaffoldContainer(
        verticalArrangement = Arrangement.Top,
        onNavigateBack = { onBackNavigation() },
        topBarComposable = {
            if (loading) {
                ClickableSearchBarShimmer()
            } else {
                SearchBar(
                    colors = SearchBarDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = windowSizeConstant.adaptiveWidthModifier.height(windowSizeConstant.adaptiveHeight),
                    inputField = {
                        CompositionLocalProvider(LocalTextStyle provides windowSizeConstant.bodyTextStyle) {
                            SearchBarDefaults.InputField(
                                query = query,
                                onQueryChange = { query = it },
                                onSearch = {
                                    if (query.isNotBlank()) {
                                        searchViewModel.addRecentSearch(query)
                                        navigateToFilter(query)
                                    }
                                },
                                expanded = false,
                                onExpandedChange = { },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.search_products),
                                        style = windowSizeClass.bodyTextStyle,// Medium title style
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    if (query.isNotEmpty()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            ButtonIconComposable(
                                                showBgColor = false,
                                                buttonIcon = ButtonIcon.Vector(Icons.Filled.Search),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                onClick = {
                                                    searchViewModel.addRecentSearch(query)
                                                    navigateToFilter(query)
                                                },
                                                contentDescription = "Search"
                                            )

                                            ButtonIconComposable(
                                                showBgColor = false,
                                                buttonIcon = ButtonIcon.Vector(Icons.Filled.Clear),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                onClick = { query = "" },
                                                contentDescription = "Clear"
                                            )
                                        }
                                    } else {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(
                                                windowSizeConstant.smallVerticalPadding
                                            ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            VoiceSearchButton(
                                                onVoiceResult = { text ->
                                                    query = text
                                                    searchViewModel.addRecentSearch(text)
                                                    navigateToFilter(text)
                                                }
                                            )

                                            VerticalDivider(
                                                color = MaterialTheme.colorScheme.outline,
                                                thickness = customSpacing.customHalf,
                                                modifier = Modifier.height(windowSizeConstant.baseSize)
                                            )

                                            PhotoSearchButton()
                                        }
                                    }
                                }
                            )
                        }
                    },
                    expanded = false,
                    onExpandedChange = {}
                ) {}
            }
        },
        showTitle = false,
        showBottomBar = false,
        content = {
            if (!networkState.hasInternet) {
                // Network Indicator in top bar
                CustomSpacer()

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
            }

            if (loading) {
                PaddedSection(
                    content = {
                        SearchListShimmer()
                    }
                )
            } else if (query.isNotEmpty()) {
                CustomSpacer()

                // Show Search Suggestions when query is being typed
                SearchSuggestions(
                    query = query,
                    recentSearches = recentSearches,
                    onSuggestionClick = { suggestion ->
                        query = suggestion
                        searchViewModel.addRecentSearch(suggestion)
                        navigateToFilter(suggestion)
                    }
                )
            } else {
                // Show Recent Searches, Popular Searches, and Featured/Trending Products
                CustomLazyColumn {
                    if (recentSearches.isNotEmpty()) {
                        item {
                            CustomSpacer()
                            PaddedSection(content = {
                                RecentSearchesSection(
                                    recentSearches = recentSearches,
                                    onSearchClick = { search ->
                                        query = search
                                        searchViewModel.addRecentSearch(search)
                                        navigateToFilter(search)
                                    },
                                    onRemoveClick = { search ->
                                        searchViewModel.removeRecentSearch(search)
                                    },
                                    onClearAll = {
                                        searchViewModel.clearAllRecentSearches()
                                    }
                                )
                                CustomHorizontalDivider()
                            })
                        }
                    }

                    item {
                        PaddedSection(content = {
                            PopularSearchesSection(
                                onSearchClick = { search ->
                                    query = search
                                    searchViewModel.addRecentSearch(search)
                                    navigateToFilter(search)
                                },
                                popularSearches = popularSearches,
                            )
                            CustomHorizontalDivider()

                        })
                    }

                    // Featured & Trending Products for discovery
                    // Trending products
                    if (trendingProducts.isNotEmpty()) {
                        item {
                            ProductSection(
                                titleRes = R.string.trending_products,
                                products = trendingProducts,
                                favoriteViewModel = favoriteViewModel,
                                cartViewModel = cartViewModel,
                                imageLoader = imageLoader,
                                onProductClick = onProductClick,
                                onSignInClick = onSignInClick,
                                onSeeAllClick = onAllProductsClick
                            )
                        }
                    }

                    // Featured products
                    if (featuredProducts.isNotEmpty()) {
                        item {
                            ProductSection(
                                titleRes = R.string.featured_products,
                                products = featuredProducts,
                                favoriteViewModel = favoriteViewModel,
                                cartViewModel = cartViewModel,
                                imageLoader = imageLoader,
                                onProductClick = onProductClick,
                                onSignInClick = onSignInClick,
                                onSeeAllClick = onAllProductsClick
                            )
                        }
                    }

                    item {
                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall))
                    }
                }
            }
        },
    )
}

/**
 * ClickableSearchBar - Simple search bar with icons for use in shimmer/loading state.
 *
 * @param onClick Callback when the search bar is clicked.
 */

@Composable
fun ClickableSearchBar(
    onClick: () -> Unit,
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Box(
        modifier = windowSizeConstant.adaptiveWidthModifier
            .clip(MaterialTheme.shapes.medium)
            .height(windowSizeConstant.adaptiveHeight)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = windowSizeConstant.baseNormalVerticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Left side - Search icon and text
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CustomIcon(
                    icon = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(windowSizeConstant.iconSize)
                )

                CustomSpacer(modifier = Modifier.width(windowSizeConstant.normalVerticalPadding))

                Text(
                    text = stringResource(R.string.search_products),
                    style = windowSizeConstant.bodyTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right side - Camera and Mic icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.smallVerticalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ButtonIconComposable(
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.Mic),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = {
                        // Prevent event bubbling and handle mic click
                        onClick()
                    },
                    contentDescription = "Mic"
                )

                VerticalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    thickness = customSpacing.customHalf,
                    modifier = Modifier.height(windowSizeConstant.baseSize)
                )

                ButtonIconComposable(
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.CameraAlt),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = {
                        // Prevent event bubbling and handle camera click
                        onClick()
                    },
                    contentDescription = "Camera"
                )
            }
        }
    }
}

/**
 * RecentSearchesSection - Displays a horizontal scrollable list of recent searches.
 *
 * Shows the user's search history as filter chips with remove buttons.
 * Includes a "Clear All" action to remove all recent searches.
 *
 * @param recentSearches List of recent search queries
 * @param onSearchClick Callback when a search item is clicked
 * @param onRemoveClick Callback when remove button on a search item is clicked
 * @param onClearAll Callback when "Clear All" button is clicked
 */
@Composable
fun RecentSearchesSection(
    recentSearches: List<String>,
    onSearchClick: (String) -> Unit,
    onRemoveClick: (String) -> Unit,
    onClearAll: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {

        // Simple horizontal scroll with LazyRow
        HeadlineWidget(
            modifier = Modifier.padding(windowSizeClass.basePadding),
            leadingTextStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            ),
            leadingText = R.string.recent_search,
            trailing = {
                CustomTextButton(onClick = onClearAll, label = R.string.clear_all)
            }
        )

        CustomLazyRow {
            items(recentSearches) { search ->
                RecentSearchItem(
                    query = search,
                    onClick = { onSearchClick(search) },
                    onRemoveClick = { onRemoveClick(search) }
                )
            }

            item {
                CustomSpacer()
            }
        }
    }
}

/**
 * RecentSearchItem - Individual recent search chip with remove button.
 *
 * Displays a single search query as a filter chip with history icon and close button.
 *
 * @param query The search query text to display
 * @param onClick Callback when the chip is clicked
 * @param onRemoveClick Callback when the close/remove button is clicked
 * @param isSelected Whether this item is currently selected
 */
@Composable
fun RecentSearchItem(
    query: String,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    isSelected: Boolean = false,
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomFilterChip(
        onClick = onClick,
        label = query,
        isSelected = isSelected,
        leadingIcon = {
            CustomIcon(
                icon = Icons.Outlined.History,
                contentDescription = "Recent search",
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                iconSize = windowSizeClass.basePadding
            )
        },
        trailingIcon = {
            CustomIcon(
                icon = Icons.Filled.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                iconSize = windowSizeClass.basePadding,
                modifier = Modifier
                    .clickable(onClick = onRemoveClick)
            )
        },
        modifier = Modifier.wrapContentWidth()
    )
}

/**
 * SearchSuggestions - Displays filtered search suggestions based on user input.
 *
 * Filters recent searches that match the current query and displays them as suggestions.
 * Each suggestion highlights the matching text portion.
 *
 * @param query The current search query text
 * @param recentSearches List of all recent searches to filter from
 * @param onSuggestionClick Callback when a suggestion is clicked
 */
@Composable
fun SearchSuggestions(
    query: String,
    recentSearches: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    val scrollState = rememberScrollState()
    val filteredSuggestions = recentSearches.filter {
        it.contains(query, ignoreCase = true)
    }

    if (filteredSuggestions.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = customSpacing.custom32)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = windowSizeClass.basePadding),
                text = stringResource(R.string.suggestions),
                style = windowSizeClass.labelTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CustomSpacer(modifier = Modifier.height(windowSizeClass.normalVerticalPadding))

            filteredSuggestions.forEach { suggestion ->
                SuggestionItem(
                    suggestion = suggestion,
                    query = query,
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }
    }
}

/**
 * SuggestionItem - Individual search suggestion with highlighted matching text.
 *
 * Displays a suggestion with the matching query portion highlighted in bold.
 * Includes a search icon on the left.
 *
 * @param suggestion The full suggestion text
 * @param query The search query to highlight within the suggestion
 * @param onClick Callback when the suggestion is clicked
 */
@Composable
fun SuggestionItem(
    suggestion: String,
    query: String,
    onClick: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = windowSizeClass.normalVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomIcon(
            icon = Icons.Outlined.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        CustomSpacer(modifier = Modifier.width(windowSizeClass.normalVerticalPadding))

        // Highlighted text
        val annotatedString = buildAnnotatedString {
            val startIndex = suggestion.indexOf(query, ignoreCase = true)
            if (startIndex >= 0) {
                append(suggestion.substring(0, startIndex))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(suggestion.substring(startIndex, startIndex + query.length))
                }
                append(suggestion.substring(startIndex + query.length))
            } else {
                append(suggestion)
            }
        }

        Text(
            text = annotatedString,
            style = windowSizeClass.bodyTextStyle
        )
    }
}

/**
 * PopularSearchesSection - Displays trending/popular search terms.
 *
 * Shows a horizontal scrollable list of popular searches as suggestion chips.
 * Displays a fallback message if no popular searches are available yet.
 *
 * @param popularSearches List of popular/trending search queries
 * @param onSearchClick Callback when a popular search is clicked
 */
@Composable
fun PopularSearchesSection(
    popularSearches: List<String>,
    onSearchClick: (String) -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    if (popularSearches.isEmpty()) {
        // Show fallback if no popular searches yet
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = windowSizeClass.basePadding)
        ) {
            Text(
                text = stringResource(R.string.trending_searches),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                fontWeight = FontWeight.SemiBold
            )

            CustomSpacer(modifier = Modifier.height(windowSizeClass.normalVerticalPadding))

            Text(
                text = stringResource(R.string.start_searching),
                style = windowSizeClass.bodyTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            HeadlineWidget(
                leadingText = R.string.trending_searches,
                trailing = {
                    CustomIcon(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            )

            CustomSpacer(modifier = Modifier.height(windowSizeClass.baseVerticalPadding))

            CustomLazyRow {
                items(popularSearches) { search ->
                    SuggestionChip(
                        onClick = { onSearchClick(search) },
                        label = {
                            Text(
                                search,
                                style = windowSizeClass.bodyTextStyle
                            )
                        },
                        icon = {
                            CustomIcon(
                                icon = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                            )
                        }
                    )
                }
            }
        }
    }
}
