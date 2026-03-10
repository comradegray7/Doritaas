package com.example.myapp.view.screens.product.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.myapp.R
import com.example.myapp.data.dataclass.Order
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.screens.product.order.components.DetailRow
import com.example.myapp.view.screens.product.order.components.OrderItemColumn
import com.example.myapp.view.screens.product.order.components.SectionCard
import com.example.myapp.view.utils.formatPrice
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * OrderDetailsDialog - A dialog displaying full details of an order.
 *
 * Shows comprehensive information about a specific order including:
 * - Order ID, Date, and Status
 * - List of purchased items
 * - Payment summary (Subtotal, Shipping, Tax, Total)
 * - Shipping Address
 * - Order Notes
 *
 * @param order The order object containing all the details to display.
 * @param onDismiss Callback to be invoked when the dialog is dismissed.
 */
@Composable
fun OrderDetailsDialog(
    order: Order,
    onDismiss: () -> Unit
) {
    val windowSizeAppConstants = LocalWindowSizeConstant.current

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.order_details),
                style = windowSizeAppConstants.titleTextStyle,
                fontWeight = FontWeight.Bold
            )
        },
        confirmButton = {
            CustomTextButton(
                label = R.string.close,
                onClick = onDismiss,
            )
        },
        text = {
            // Order Info Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(windowSizeAppConstants.normalVerticalPadding)
            ) {
                SectionCard(title = "Order Information") {
                    DetailRow(
                        icon = Icons.Default.Receipt,
                        label = "Order ID",
                        value = "#${order.id.take(8).uppercase()}"
                    )

                    DetailRow(
                        icon = Icons.Filled.CalendarToday,
                        label = "Date",
                        value = order.createdAt?.toDate()?.let {
                            SimpleDateFormat(
                                "MMM dd, yyyy HH:mm",
                                Locale.getDefault()
                            ).format(
                                it
                            )
                        } ?: "N/A"
                    )

                    DetailRow(
                        icon = Icons.Filled.Info,
                        label = "Status",
                        value = order.status.replaceFirstChar { it.uppercase() },
                        valueColor = getStatusColor(order.status)
                    )

                    DetailRow(
                        icon = Icons.Filled.CreditCard,
                        label = "Payment",
                        value = order.paymentStatus.replaceFirstChar { it.uppercase() },
                        valueColor = if (order.paymentStatus == "paid") colors.customColor5
                        else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Items Section
                SectionCard(title = "Items (${order.items.size})") {
                    order.items.forEach { item ->
                        OrderItemColumn(item = item)
                    }
                }

                // Payment Summary Section
                SectionCard(title = "Payment Summary") {
                    DetailRow(
                        icon = Icons.Filled.ShoppingCart,
                        label = "Subtotal",
                        value = formatPrice(order.subtotal)
                    )

                    DetailRow(
                        icon = Icons.Filled.LocalShipping,
                        label = "Shipping",
                        value = if (order.shippingCost == 0.0) "FREE"
                        else formatPrice(order.shippingCost),
                        valueColor = if (order.shippingCost == 0.0) colors.customColor5
                        else MaterialTheme.colorScheme.onSurface
                    )

                    if (order.taxAmount > 0) {
                        DetailRow(
                            icon = Icons.Default.Receipt,
                            label = "Tax",
                            value = formatPrice(order.taxAmount)
                        )
                    }

                    DetailRow(
                        icon = Icons.Filled.AttachMoney,
                        label = "Total",
                        value = formatPrice(order.totalAmount),
                        valueStyle = windowSizeAppConstants.bodyTextStyle,
                        valueColor = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Shipping Address Section
                order.shippingAddress?.let { address ->
                    SectionCard(title = "Shipping Address") {
                        Column(verticalArrangement = Arrangement.spacedBy(windowSizeAppConstants.smallVerticalPadding)) {
                            Text(
                                text = address.fullName,
                                style = windowSizeAppConstants.bodyTextStyle,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = address.addressLine1,
                                style = windowSizeAppConstants.bodyTextStyle
                            )
                            Text(
                                text = address.addressLine2,
                                style = windowSizeAppConstants.bodyTextStyle
                            )
                            Text(
                                text = "${address.city}, ${address.state} ${address.zipCode}",
                                style = windowSizeAppConstants.bodyTextStyle
                            )
                            Text(
                                text = address.country,
                                style = windowSizeAppConstants.bodyTextStyle
                            )
                            Text(
                                text = address.phoneNumber,
                                style = windowSizeAppConstants.bodyTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Notes Section (if any)
                if (order.notes.isNotEmpty()) {
                    SectionCard(title = "Notes") {
                        Text(
                            text = order.notes,
                            style = windowSizeAppConstants.bodyTextStyle
                        )
                    }
                }
            }
        }
    )
}
