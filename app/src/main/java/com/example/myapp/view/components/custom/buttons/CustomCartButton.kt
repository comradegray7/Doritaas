package com.example.myapp.view.components.custom.buttons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.myapp.R
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomCircularProgressIndicator
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.utils.CustomShape
import kotlinx.coroutines.delay

/**
 * CustomCartButton - An animated button for adding/removing items from the cart.
 *
 * This button transitions between "Add to Cart" and "In Cart" states with
 * specialized animations (color changes, icon scaling, text fading).
 * It supports both a standard elevated button style and a rounded icon-only style.
 *
 * @param isInCart Whether the item is currently in the cart.
 * @param isLoading Whether a cart operation is in progress (shows progress indicator).
 * @param onAddToCart Callback invoked when the button is clicked.
 * @param cartButtonColor Background color for the button.
 * @param cartButtonContentColor Content (text/icon) color for the button.
 * @param cartButtonScale Scale factor for the button (used for press animations).
 * @param useRoundedButton If true, renders a [RoundedCartButton] instead of the standard button.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CustomCartButton(
    modifier: Modifier = Modifier,
    isInCart: Boolean,
    isLoading: Boolean = true,
    onAddToCart: () -> Unit,
    cartButtonColor: Color = colors.transparent,
    cartButtonContentColor: Color = colors.transparent,
    cartButtonScale: Float = 0f,
    useRoundedButton: Boolean = false,
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    // Track animation state separately to trigger on every change
    var animationKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(isInCart) {
        animationKey++
    }

    // Add click feedback
    val interactionSource = remember { MutableInteractionSource() }

    // Smooth color animations
    val animatedContainerColor by animateColorAsState(
        targetValue = cartButtonColor,
        animationSpec = tween(durationMillis = 400, easing = EaseInOutCubic),
        label = "cart_button_container_color"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = cartButtonContentColor,
        animationSpec = tween(durationMillis = 400, easing = EaseInOutCubic),
        label = "cart_button_content_color"
    )

    if (useRoundedButton) {
        RoundedCartButton(
            isInCart = isInCart,
            onAddToCart = onAddToCart
        )
    } else {
        ElevatedButton(
            onClick = onAddToCart,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = animatedContainerColor,
                contentColor = animatedContentColor
            ),
            shape = CustomShape.mediumShape(),
            interactionSource = interactionSource,
            modifier = modifier.then(
                Modifier
                    .width(windowSizeConstant.customButtonPadding)
                    .graphicsLayer {
                        scaleX = cartButtonScale
                        scaleY = cartButtonScale
                    })
        ) {
            if (isLoading) {
                CustomCircularProgressIndicator(
                    color = animatedContentColor,
                    strokeWidth = windowSizeConstant.cardElevationPadding,
                    modifier = Modifier.size(windowSizeConstant.baseSize),
                    trackColor = colors.transparent
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
                ) {
                    // Smooth icon animation with scale effect
                    key(animationKey) {
                        var shouldScaleIcon by remember { mutableStateOf(false) }

                        LaunchedEffect(isInCart) {
                            shouldScaleIcon = true
                            delay(300)
                            shouldScaleIcon = false
                        }

                        val iconScale by animateFloatAsState(
                            targetValue = if (shouldScaleIcon) 1.3f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "cart_icon_scale"
                        )

                        CustomIcon(
                            icon = if (isInCart) Icons.Filled.ShoppingCart else Icons.Filled.AddShoppingCart,
                            contentDescription = if (isInCart) "Remove from cart" else "Add to cart",
                            tint = animatedContentColor,
                            modifier = Modifier
                                .scale(iconScale)
                        )
                    }

                    // Smooth text transition with fade
                    AnimatedContent(
                        targetState = isInCart,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300, delayMillis = 100)).togetherWith(
                                fadeOut(animationSpec = tween(100))
                            ) using
                                    SizeTransform(clip = false)
                        },
                        label = "cart_text_animation"
                    ) { inCart ->
                        Text(
                            text = stringResource(if (inCart) R.string.in_cart else R.string.add_to_cart),
                            style = windowSizeConstant.labelTextStyle,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.animateEnterExit(
                                enter = fadeIn(animationSpec = tween(300)),
                                exit = fadeOut(animationSpec = tween(100))
                            )
                        )
                    }
                }
            }
        }
    }
}
