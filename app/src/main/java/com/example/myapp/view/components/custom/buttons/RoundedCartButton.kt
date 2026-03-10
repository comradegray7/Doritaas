package com.example.myapp.view.components.custom.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.myapp.view.utils.ButtonIcon

/**
 * RoundedCartButton - A simple circular button for cart actions.
 *
 * Displays a shopping cart icon that toggles between "Add to Cart" and "In Cart" states.
 *
 * @param isInCart Whether the item is currently in the cart.
 * @param onAddToCart Callback invoked when the button is clicked.
 */
@Composable
/**
 * RoundedCartButton
 *
 *
 * @param isInCart The isInCart parameter
 * @param onAddToCart The onAddToCart parameter
 */
fun RoundedCartButton(isInCart: Boolean, onAddToCart: () -> Unit) {
        ButtonIconComposable(
            onClick = onAddToCart,
            buttonIcon = ButtonIcon.Vector(
                if (isInCart)
                    Icons.Filled.ShoppingCart
                else
                    Icons.Filled.AddShoppingCart,
            ),
            tint = if (isInCart)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = "rounded cart button"
        )
}