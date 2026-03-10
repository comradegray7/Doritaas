package com.example.myapp.view.screens.product

import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import coil3.ImageLoader
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.model.CartViewModel
import com.example.myapp.data.model.FavoriteViewModel
import com.example.myapp.view.components.CustomLazyRow
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.utils.ButtonIcon

/**
 * ProductSection - A reusable section displaying a horizontal list of products.
 *
 * Displays a headline (with optional title, middle text, or custom leading content)
 * and a horizontally scrollable list of [ProductCard]s.
 * Handles interactions like clicking, favoriting, and adding to cart.
 *
 * @param titleRes Resource ID for the section title (leading text).
 * @param products List of [ProductItem]s to display.
 * @param favoriteViewModel ViewModel for managing favorites.
 * @param cartViewModel ViewModel for managing cart items.
 * @param imageLoader ImageLoader for loading product images.
 * @param onProductClick Callback when a product is clicked.
 * @param onSignInClick Callback to initiate sign-in (for favorites/cart).
 * @param onSeeAllClick Callback when the "See All" (or trailing arrow) is clicked.
 * @param leadingComposable Optional custom composable to display at the start of the headline.
 * @param showLeadingComposable Whether to show the leading composable.
 * @param trailingComposable Optional custom composable for the trailing area (e.g., countdown).
 * @param middleText Optional text to display in the middle of the headline.
 * @param subMiddleText Optional subtitle text below the middle text.
 */
@Composable
 fun ProductSection(
    @StringRes titleRes: Int? = null,
    products: List<ProductItem>,
    favoriteViewModel: FavoriteViewModel,
    cartViewModel: CartViewModel,
    imageLoader: ImageLoader,
    onProductClick: (ProductItem) -> Unit,
    onSignInClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    leadingComposable: @Composable () -> Unit = {},
    showLeadingComposable: Boolean = true,
    trailingComposable:  @Composable (() -> Unit)? = null,
    middleText: String? = null,
    subMiddleText: String? = null
) {

    PaddedSection(
        alignment = Alignment.CenterHorizontally,
        content = {
        HeadlineWidget(
            showLeadingComposable = showLeadingComposable,
            leadingComposable = leadingComposable,
            leadingText = titleRes,
            middleTextStr = middleText,
            subMiddleTextStr = subMiddleText,
            trailing = {
                trailingComposable?.invoke() ?: ButtonIconComposable(
                    buttonIcon = ButtonIcon.Vector(Icons.Filled.ArrowCircleRight),
                    onClick = onSeeAllClick,
                    contentDescription = "more options"
                )
            }
        )
    })

    CustomSpacer()

    CustomLazyRow {
        items(
            items = products,
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