package com.example.myapp.view.screens.product.order.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.myapp.data.dataclass.OrderItem
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.utils.formatPrice

/**
 * OrderItemColumn - Displays a single item within an order.
 *
 * Shows the product name, price, and quantity calculation for a specific order item.
 *
 * @param item The order item containing product details, price, and quantity.
 */
@Composable
fun OrderItemColumn(item: OrderItem) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    )
    {
        Text(
            text = item.productName,
            style = windowSizeConstant.bodyTextStyle,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseVerticalPadding))

        Text(
            text = formatPrice(item.price),
            style = windowSizeConstant.bodyTextStyle,
            fontWeight = FontWeight.SemiBold
        )

        CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseVerticalPadding))

        Text(
            text = "Qty: ${item.quantity} × ${formatPrice(item.price)}",
            style = windowSizeConstant.bodyTextStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

