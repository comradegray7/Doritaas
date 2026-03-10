package com.example.myapp.view.screens.product.promotions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.utils.CustomShape
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit

// ============================================================================
// PROMOTION COUNTDOWN TIMER
// ============================================================================

/**
 * PromotionCountdownTimer - A real-time countdown timer for promotions.
 *
 * Displays the time remaining until a specified end time.
 * Formats time as "Xd Xh", "Xh Xm", or "mm:ss" depending on duration left.
 * Updates every second.
 *
 * @param color Background color of the timer container.
 * @param endAt The timestamp (in milliseconds) when the promotion ends.
 * @param textColor The color of the timer text and icon.
 */
@Composable
fun PromotionCountdownTimer(
    color: Color = colors.transparent,
    endAt: Long,
    textColor: Color = MaterialTheme.colorScheme.error
) {
    var timeRemaining by remember { mutableStateOf("") }
    val windowSizeAppConstants = LocalWindowSizeConstant.current

    LaunchedEffect(endAt) {
        while (true) {
            val now = System.currentTimeMillis()
            val diff = endAt - now

            if (diff <= 0) {
                timeRemaining = "EXPIRED"
                break
            }

            val days = TimeUnit.MILLISECONDS.toDays(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

            timeRemaining = when {
                days > 0 -> "${days}d ${hours}h"
                hours > 0 -> "${hours}h ${minutes}m"
                else -> String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }

            delay(1000)
        }
    }

    Row(
        modifier = Modifier
            .background(
                shape = CustomShape.circleShape(),
                color = color
            )
            .padding(vertical = windowSizeAppConstants.smallVerticalPadding, horizontal = windowSizeAppConstants.baseNormalVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(windowSizeAppConstants.smallVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomIcon(
            icon = Icons.Filled.AccessTime,
            contentDescription = null,
            tint = textColor,
        )

        Text(
            text = timeRemaining,
            style =  windowSizeAppConstants.labelTextStyle,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold
        )
    }
}