package com.example.myapp.view.components.custom.buttons

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.utils.ButtonIcon
import kotlinx.coroutines.flow.StateFlow

/**
 * ShoppingCartBadge - A standalone button with a badge for the shopping cart.
 *
 * This component displays a shopping cart icon with a badge showing the current item count.
 * The badge animates when the count changes.
 *
 * @param badgeNumber StateFlow emitting the current number of items in the cart.
 * @param onCartClick Callback invoked when the cart button is clicked.
 */
@Composable
fun ShoppingCartBadge(
    badgeNumber:  StateFlow<Int>,
    onCartClick: () -> Unit = {}
) {
    // Animate badge number changes
    val animatedBadgeNumber by animateIntAsState(
        targetValue = badgeNumber.collectAsState().value,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "badge_number_animation"
    )

    BadgedBox(
        badge = {
            // Only show badge if count > 0
            if (animatedBadgeNumber > 0) {
                Badge(
                    modifier = Modifier
                        .offset(-customSpacing.custom10, customSpacing.custom3)
                        .scale(
                            animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "badge_scale"
                            ).value
                        ),
                    containerColor = colors.customColor6,
                ) {
                    Text(
                        text = if (animatedBadgeNumber > 99) "99+" else "$animatedBadgeNumber",
                        color = colors.white
                    )
                }
            }
        },
        content = {
            ButtonIconComposable(
                showBgColor = false,
                buttonIcon = ButtonIcon.Vector(Icons.Filled.ShoppingCart),
                onClick = { onCartClick() },
                contentDescription = "shopping cart"
            )
        }
    )
}