package com.example.myapp.view.screens.product.order.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.myapp.R
import com.example.myapp.data.dataclass.Order
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.CustomShape

/**
 * OrderStatusDialog - Status update or filter dialog
 *
 * Dual-purpose dialog that either updates a specific order's status or filters
 * the order list by status.
 *
 * ## Modes
 * - **Update Mode** (when currentOrder is not null): Updates specific order status
 * - **Filter Mode** (when currentOrder is null): Filters order list by status
 *
 * ## Available Statuses
 * - Pending
 * - Processing
 * - Shipped
 * - Delivered
 * - Cancelled
 *
 * @param currentOrder The order to update (null for filter mode)
 * @param currentStatus The currently selected status
 * @param onDismiss Callback when dialog is dismissed
 * @param onStatusChange Callback when a status is selected, receives new status
 */
@Composable
fun OrderStatusDialog(
    currentOrder: Order?,
    currentStatus: String,
    onDismiss: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    val statusOptions = listOf("pending", "confirmed", "processing", "shipped", "delivered", "cancelled")

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (currentOrder != null) "Update Order Status" else "Filter by Status",
                style = windowSizeClass.bodyTextStyle
            )
        },
        text = {
            Column {
                statusOptions.forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStatusChange(status) }
                            .padding(vertical = windowSizeClass.normalVerticalPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = status == currentStatus,
                            onClick = { onStatusChange(status) }
                        )

                        CustomSpacer(modifier = Modifier.width(windowSizeClass.basePadding))

                        OrderStatusChip(status = status)
                    }
                }
            }
        },
        dismissButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.cancel,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.edit_order,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    )
}

/**
 * OrderStatusChip - Color-coded status indicator.
 *
 * Displays order status with appropriate background and text colors.
 *
 * ## Status Colors
 * - **Pending**: Orange background, dark orange text
 * - **Processing**: Light blue background, dark blue text
 * - **Shipped**: Light green background, dark green text
 * - **Delivered**: Light green background, dark green text
 * - **Cancelled**: Light red background, dark red text
 * - **Default**: Light gray background, dark gray text
 *
 * @param modifier Optional modifier for the chip.
 * @param status The order status string to display.
 */
@Composable
fun OrderStatusChip(modifier: Modifier = Modifier, status: String) {
    val windowSizeClass = LocalWindowSizeConstant.current

    val (backgroundColor, textColor) = when (status.lowercase()) {
        "pending" -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        "confirmed" -> Color(0xFFE8F5E8) to Color(0xFF388E3C)
        "processing" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        "shipped" -> Color(0xFFE8F5E8) to Color(0xFF388E3C)
        "delivered" -> Color(0xFFE8F5E8) to Color(0xFF388E3C)
        "cancelled" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        else -> Color(0xFFF5F5F5) to Color(0xFF757575)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.then(
            Modifier
                .background(backgroundColor, CustomShape.mediumShape())
                .padding(windowSizeClass.smallVerticalPadding)
        )
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            style = windowSizeClass.labelTextStyle,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}
