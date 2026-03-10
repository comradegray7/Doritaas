package com.example.myapp.view.screens.product.order

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.myapp.R
import com.example.myapp.data.dataclass.Order
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CustomShape
import com.example.myapp.view.utils.formatPrice

/**
 * OrderConfirmationScreen - Post-payment order confirmation display
 *
 * Shows order confirmation after successful payment with order details summary
 * and navigation options to view full order details or continue shopping.
 *
 * ## Features
 * - **Success Animation**: Large checkmark icon with success message
 * - **Order Summary**: Order ID and total amount display
 * - **View Details**: Opens dialog with complete order information
 * - **Continue Shopping**: Returns user to shopping experience
 * - **Order Details Dialog**: Comprehensive order breakdown including items, pricing, shipping address
 *
 * ## Displayed Information
 * - Order confirmation status
 * - Order ID (truncated for display)
 * - Total amount paid
 * - Thank you message
 *
 * ## User Actions
 * 1. View order details (opens detailed dialog)
 * 2. Continue shopping (returns to shop)
 *
 * ## Order Details Dialog Includes
 * - Order ID, date, status, payment status
 * - All order items with quantities and prices
 * - Payment summary (subtotal, shipping, tax, total)
 * - Complete shipping address
 * - Order notes (if any)
 *
 * @param order The confirmed order object with all details
 * @param onContinueShopping Callback to return to shopping
 *
 * @see com.example.myapp.view.screens.product.PaymentScreen for the payment flow
 * @see Order for order data structure
 */
@SuppressLint("DefaultLocale")
@Composable
fun OrderConfirmationScreen(
    order: Order,
    onContinueShopping: () -> Unit,
) {
    // State to control dialog visibility
    var showOrderDetailsDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val windowSizeClass = LocalWindowSizeConstant.current

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(vertical = windowSizeClass.baseSize)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PaddedSection(
            alignment = Alignment.CenterHorizontally,
            content = {
                Box(
                    modifier = Modifier
                        .size(windowSizeClass.customSpacerLarge)
                        .background(
                            colors.customColor5.copy(alpha = 0.1f),
                            CustomShape.extraLargeShape()
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CustomIcon(
                        icon = Icons.Filled.CheckCircle,
                        contentDescription = "Order Confirmed",
                        tint = colors.customColor5,
                        iconSize = windowSizeClass.largeIconSize
                    )
                }

                CustomSpacer(modifier = Modifier.height(windowSizeClass.baseSize))

                Text(
                    text = stringResource(R.string.order_confirmed),
                    style = windowSizeClass.titleTextStyle,
                    fontWeight = FontWeight.Bold
                )

                CustomSpacer(modifier = Modifier.height(windowSizeClass.normalVerticalPadding))

                Text(
                    text = stringResource(R.string.thank_you),
                    style = windowSizeClass.bodyTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                CustomSpacer()

                // Order ID display
                Text(
                    text = "Order #${order.id.take(8).uppercase()}",
                    style = windowSizeClass.bodyTextStyle,
                    color = MaterialTheme.colorScheme.primary
                )

                CustomSpacer(modifier = Modifier.height(windowSizeClass.normalVerticalPadding))

                // Total Amount
                Text(
                    text = "Total: ${formatPrice(order.totalAmount)}",
                    style = windowSizeClass.bodyTextStyle,
                    fontWeight = FontWeight.Bold
                )

                CustomSpacer()


                // View Order Details Button - Opens Dialog
                CustomButton(
                    buttonColors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    onClick = { showOrderDetailsDialog = true },
                    icon = ButtonIcon.Vector(Icons.Filled.Receipt),
                    label = R.string.view_order,
                    contentDescription = "Receipt Button"
                )

                CustomSpacer()

                // Continue Shopping Button
                CustomButton(
                    label = R.string.continue_shopping,
                    onClick = onContinueShopping,
                    icon = ButtonIcon.Vector(Icons.Filled.ShoppingBag),
                    contentDescription = "Continue Shopping Button",
                )
            })
    }
    // Order Details Dialog
    if (showOrderDetailsDialog) {
        OrderDetailsDialog(
            order = order,
            onDismiss = { showOrderDetailsDialog = false }
        )
    }
}

