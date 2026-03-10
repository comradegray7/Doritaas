package com.example.myapp.view.utils.primeUtils

import android.util.Log
import com.example.myapp.data.dataclass.CheckoutSummary
import com.example.myapp.data.dataclass.Order
import com.example.myapp.data.dataclass.ProductItem
import com.google.firebase.Timestamp
import java.util.Calendar

  /**
   * calculatePrimeDiscount
   *
   *
   * @param product The product parameter
   * @param summary The summary parameter
   */
  fun calculatePrimeDiscount(product: ProductItem, summary: CheckoutSummary?): Double {
    if (summary?.isPrimeOrder == true && product.isPrimeEligible) {
        return product.price * product.quantity * 0.20 // 20% Prime discount
    }
    return 0.0
}

  /**
   * calculateTotalPrimeDiscount
   *
   *
   * @param items The items parameter
   * @param summary The summary parameter
   */
  fun calculateTotalPrimeDiscount(items: List<ProductItem>, summary: CheckoutSummary?): Double {
    if (summary?.isPrimeOrder != true) return 0.0

    return items.sumOf { product ->
        if (product.isPrimeEligible) {
            product.price * product.quantity * 0.20
        } else {
            0.0
        }
    }
}

 /**
  * calculateSubtotal
  *
  * @param items The items parameter
  */
 fun calculateSubtotal(items: List<ProductItem>): Double {
    return items.sumOf { it.price * it.quantity }
}

  /**
   * calculateOriginalShippingCost
   *
   *
   * @param items The items parameter
   */
  fun calculateOriginalShippingCost(items: List<ProductItem>): Double {
    return items.sumOf { it.shipmentCost }
}

 /**
  * calculateFinalShippingCost
  *
  *
  * @param items The items parameter
  * @param summary The summary parameter
  */
 fun calculateFinalShippingCost(items: List<ProductItem>, summary: CheckoutSummary?): Double {
    if (summary?.isPrimeOrder == true) {
        return 0.0 // Free shipping for Prime
    }
    return items.sumOf { it.shipmentCost }
}

 /**
  * calculateTax
  *
  *
  * @param subtotal The subtotal parameter
  */
 fun calculateTax(subtotal: Double): Double {
    // Default tax calculation (8.875%)
    return subtotal * 0.08875
}

  /**
   * calculateTotalDiscount
   *
   *
   * @param items The items parameter
   * @param summary The summary parameter
   */
  fun calculateTotalDiscount(items: List<ProductItem>, summary: CheckoutSummary?): Double {
    var discount = summary?.discount ?: 0.0

    // Add Prime discounts if applicable
    if (summary?.isPrimeOrder == true) {
        items.forEach { product ->
            if (product.isPrimeEligible) {
                discount += product.price * product.quantity * 0.20
            }
        }
    }

    return discount
}

  /**
   * extractProductTags
   *
   *
   * @param items The items parameter
   */
  fun extractProductTags(items: List<ProductItem>): List<String> {
    return items.flatMap { it.tags }
        .distinct()
        .toList()
}

 /**
  * calculateEstimatedDelivery
  *
  *
  * @param isPrimeOrder The isPrimeOrder parameter
  */
 fun calculateEstimatedDelivery(isPrimeOrder: Boolean): Timestamp {
    val calendar = Calendar.getInstance()

    if (isPrimeOrder) {
        // Prime delivery: 1-2 days
        calendar.add(Calendar.DAY_OF_YEAR, 2)
    } else {
        // Standard delivery: 3-7 days
        calendar.add(Calendar.DAY_OF_YEAR, 5)
    }

    return Timestamp(calendar.time)
}

  /**
   * logPrimeBenefits
   *
   *
   * @param order The order parameter
   */
  fun logPrimeBenefits(order: Order) {
    Log.d("CreateOrderUseCase", "🎯 Prime Benefits Applied:")
    Log.d("CreateOrderUseCase", "  - Order ID: ${order.id}")
    Log.d("CreateOrderUseCase", "  - User ID: ${order.userId}")
    Log.d("CreateOrderUseCase", "  - Prime Discount: $${order.primeDiscountApplied}")
    Log.d("CreateOrderUseCase", "  - Shipping Saved: $${order.originalShippingCost - order.shippingCost}")
    Log.d("CreateOrderUseCase", "  - Estimated Delivery: ${order.estimatedDelivery?.toDate()}")

    // Count Prime eligible items
    val primeItemsCount = order.items.count { it.isPrimeEligible }
    Log.d("CreateOrderUseCase", "  - Prime Eligible Items: $primeItemsCount/${order.items.size}")
}
