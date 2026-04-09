package com.example.myapp.view.screens.product.order.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.myapp.data.dataclass.Order
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.admin.formatToString
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.utils.formatPrice

/**
 * OrderCard - Individual order display card
 *
 * Displays a single order with its key information and provides quick access
 * to status updates and deletion.
 *
 * ## Displayed Information
 * - Order ID (truncated to 8 characters)
 * - Creation date (formatted as "MMM dd, yyyy at hh:mm a")
 * - Status chip with color coding
 * - Number of items in order
 * - Total amount (formatted as currency)
 * - User ID (truncated to 8 characters)
 *
 * ## Actions
 * - **Card Click**: Navigate to order details
 * - **Status Button**: Open status update dialog
 * - **Delete Button**: Open delete confirmation dialog
 *
 * @param order The order data to display
 * @param onOrderClick Callback when card is clicked
 *
 * @see Order for order data structure
 * @see OrderStatusChip for status display
 */
@Composable
fun OrderCard(
    modifier: Modifier = Modifier,
    order: Order,
    onOrderClick: () -> Unit,
    actions: @Composable () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Card(
        modifier = modifier.then(
            windowSizeClass.adaptiveWidthModifier
                .clickable(onClick = { onOrderClick() })
        )
    ) {
        Column(
            modifier = Modifier.padding(windowSizeClass.normalVerticalPadding)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id.take(8)}",
                        overflow = TextOverflow.Ellipsis,
                        style = windowSizeClass.bodyTextStyle,
                        fontWeight = FontWeight.Bold
                    )

                    CustomSpacer(modifier = Modifier.height(customSpacing.custom4))
                    Text(
                        text = "Placed on ${order.createdAt?.toDate()?.formatToString() ?: "N/A"}",
                        style = windowSizeClass.labelTextStyle,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OrderStatusChip(status = order.status)
            }

            CustomSpacer(modifier = Modifier.height(customSpacing.custom4))
            Text(
                text = "User: ${order.userId.take(8)}",
                style = windowSizeClass.labelTextStyle,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CustomSpacer()

            // Order Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Compact item summary: show first item name truncated and " +N more" when applicable
                    val firstItemName = order.items.firstOrNull()?.productName ?: ""
                    val itemSummary = if (firstItemName.isNotBlank()) {
                        if (order.items.size > 1) {
                            val truncated = if (firstItemName.length > 30) firstItemName.take(27) + "..." else firstItemName
                            "$truncated +${order.items.size - 1}"
                        } else {
                            firstItemName
                        }
                    } else {
                        "${order.items.size} items"
                    }

                    Text(
                        text = itemSummary,
                        overflow = TextOverflow.Ellipsis,
                        style = windowSizeClass.bodyTextStyle,
                    )
                    CustomSpacer(modifier = Modifier.height(customSpacing.custom4))

                    Text(
                        text = "Total: ${formatPrice(order.totalAmount)}",
                        style = windowSizeClass.bodyTextStyle,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            CustomSpacer()

            actions()
        }
    }
}