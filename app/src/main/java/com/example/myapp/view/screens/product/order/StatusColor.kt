package com.example.myapp.view.screens.product.order

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.myapp.ui.theme.colors

/**
 * getStatusColor - Returns the color associated with a specific order status.
 *
 * Maps order status strings to specific UI colors for visual indication associated with the status.
 *
 * @param status The order status string (e.g., "confirmed", "processing", "shipped").
 * @return The [Color] corresponding to the status, or a default surface color if not matched.
 */
@Composable
fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "confirmed" -> colors.green
        "processing" -> colors.blue
        "shipped" -> colors.lightPurple
        "delivered" -> colors.green
        "cancelled" -> colors.customColor6
        "pending" -> colors.orange
        else -> MaterialTheme.colorScheme.onSurface
    }
}
