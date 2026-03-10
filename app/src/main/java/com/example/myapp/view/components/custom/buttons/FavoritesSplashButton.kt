package com.example.myapp.view.components.custom.buttons

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomIcon
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * FavoriteSplashButton - An animated favorite/heart button with particle effects.
 *
 * This button toggles between favorited and un-favorited states. When favorited,
 * it triggers a particle explosion animation and a heart scale bounce.
 *
 * @param isFavorite Whether the item is currently favorited.
 * @param onToggle Callback invoked when the button is clicked.
 */
@Composable
fun FavoriteSplashButton(
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    onToggle: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    // animation state by isFavorite to trigger on every change
    var animationKey by remember { mutableIntStateOf(0) }

    val favoriteRed = colors.favRed
    val favoriteBackground = colors.bgWhite

    LaunchedEffect(isFavorite) {
        if (isFavorite) {
            animationKey++
        }
    }

    Box(
        modifier = modifier.then(Modifier.size(customSpacing.custom48)),
        contentAlignment = Alignment.Center
    ) {
        // Particle explosion effect
        if (isFavorite) {
            key(animationKey) {
                val circles = remember { List(6) { it } }
                circles.forEach { i ->
                    val angle = (360f / circles.size) * i

                    var animationPlayed by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        animationPlayed = true
                        delay(500)
                        animationPlayed = false
                    }

                    val radius by animateFloatAsState(
                        targetValue = if (animationPlayed) 1f else 0f,
                        animationSpec = tween(500, easing = EaseOutCubic),
                        label = "circle_radius_$i"
                    )
                    val alpha by animateFloatAsState(
                        targetValue = if (animationPlayed) 0f else 1f,
                        animationSpec = tween(500),
                        label = "circle_alpha_$i"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val r = size.minDimension / 2 * radius
                        val offsetX = cos(Math.toRadians(angle.toDouble())).toFloat() * r
                        val offsetY = sin(Math.toRadians(angle.toDouble())).toFloat() * r
                        drawCircle(
                            color = Color.Red.copy(alpha = alpha),
                            radius = 6f,
                            center = center + Offset(offsetX, offsetY)
                        )
                    }
                }
            }
        }

        // Heart scale animation
        var shouldScale by remember { mutableStateOf(false) }

        LaunchedEffect(isFavorite) {
            if (isFavorite) {
                shouldScale = true
                delay(300)
                shouldScale = false
            }
        }

        CustomIcon(
            icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite)
                favoriteRed
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clickable(onClick = onToggle)
                .background(
                    shape = CircleShape,
                    color = if (isFavorite)
                        favoriteBackground // Red background when favorite
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                )
                .padding(windowSizeClass.normalVerticalPadding)
        )
    }
}
