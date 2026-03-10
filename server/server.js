 import express from "express";
import cors from "cors";
import Stripe from "stripe";
import dotenv from "dotenv";
import ngrok from "@ngrok/ngrok";

dotenv.config();

/**
 * Express server providing Stripe PaymentSheet creation and Stripe webhook handling.
 *
 * The server also supports Prime membership logic for discounts and shipping savings,
 * and exposes lightweight diagnostic endpoints for validating calculations/metadata.
 */
const app = express();
const PORT = process.env.PORT || 3000;

// 🔧 Use consistent API version
const STRIPE_API_VERSION = "2025-08-27.basil";

const stripe = new Stripe(
  process.env.STRIPE_SECRET_KEY ||
    "sk_test_51L8W9ZFSOt52vVKnbadKMtIrAJHwp6D9p7NjcHTEtu51NHjzgL714A8x7LGoT3JiVGxjwBkUqDUXNFLZx6HiCbZA00GvImlApz",
  {
    apiVersion: STRIPE_API_VERSION,
  }
);

// Middleware
app.use(express.json());
app.use(cors({ origin: "*" }));

/**
 * Compute Prime-member savings for a set of products.
 *
 * Prime behavior:
 * - Prime-eligible items get 20% off item subtotal
 * - Shipping costs are treated as savings for Prime members
 *
 * @param {Array<Object>} products - Cart line items.
 * @param {boolean} isPrimeMember - Whether the customer is a Prime member.
 * @returns {{
 *   primeDiscount: number,
 *   shippingSavings: number,
 *   totalSavings: number,
 *   eligibleItemsCount: number
 * }}
 */
function calculatePrimeBenefits(products, isPrimeMember) {
  let primeDiscount = 0;
  let shippingSavings = 0;
  let eligibleItemsCount = 0;

  if (isPrimeMember && Array.isArray(products)) {
    products.forEach((product) => {
      // Check if product is Prime eligible
      const isPrimeEligible = product.tags?.includes("prime_eligible") || false;
      
      if (isPrimeEligible) {
        // 20% discount on eligible items
        const itemTotal = (product.price || 0) * (product.quantity || 0);
        primeDiscount += itemTotal * 0.20;
        eligibleItemsCount++;
      }
      
      // Add shipping savings (all items get free shipping for Prime)
      shippingSavings += product.shipmentCost || 0;
    });
  }

  return {
    primeDiscount: Math.round(primeDiscount * 100) / 100, // Round to 2 decimals
    shippingSavings: Math.round(shippingSavings * 100) / 100,
    totalSavings: Math.round((primeDiscount + shippingSavings) * 100) / 100,
    eligibleItemsCount,
  };
}

/**
 * Create a Stripe PaymentIntent and return PaymentSheet fields for client-side checkout.
 *
 * Request body supports Prime membership. Prime effects are reflected in Stripe metadata,
 * line items (for display), and the response's `primeBenefits` payload.
 *
 * Route: POST `/payment-sheet`
 */
app.post("/payment-sheet", async (req, res) => {
  try {
    console.log(
      "📥 Received payment-sheet request:",
      JSON.stringify(req.body, null, 2)
    );

    const {
      amount,
      customerEmail,
      customerName,
      products,
      shippingAddress,
      billingAddress,
      isPrimeMember, // ✅ NEW: Prime membership status
      primeDiscount, // ✅ NEW: Pre-calculated Prime discount (optional)
    } = req.body;

    // Validation
    if (!amount || typeof amount !== "number" || amount <= 0) {
      console.error("❌ Invalid amount:", amount);
      return res
        .status(400)
        .json({ error: "Valid amount (in cents) is required." });
    }

    console.log("✅ Amount validated:", amount);
    console.log(`🔵 Prime Member: ${isPrimeMember || false}`);

    // ✅ Calculate Prime benefits
    const primeBenefits = calculatePrimeBenefits(products, isPrimeMember);
    console.log("💎 Prime Benefits Calculated:", primeBenefits);

    // 1. Create a Customer with email, name, and address
    console.log("👤 Creating customer with address...");
    const customerData = {
      email: customerEmail || undefined,
      name: customerName || undefined,
      metadata: {
        is_prime_member: isPrimeMember ? "true" : "false", // ✅ Add Prime status
      },
    };

    // Add shipping address if provided
    if (shippingAddress) {
      customerData.shipping = {
        name: shippingAddress.recipientName || customerName || "Customer",
        phone: shippingAddress.phoneNumber || undefined,
        address: {
          line1: shippingAddress.addressLine1 || "",
          line2: shippingAddress.addressLine2 || undefined,
          city: shippingAddress.city || "",
          state: shippingAddress.state || "",
          postal_code: shippingAddress.postalCode || "",
          country: shippingAddress.country || "US",
        },
      };
    }

    // Add billing address if provided (falls back to shipping address)
    if (billingAddress) {
      customerData.address = {
        line1: billingAddress.addressLine1 || "",
        line2: billingAddress.addressLine2 || undefined,
        city: billingAddress.city || "",
        state: billingAddress.state || "",
        postal_code: billingAddress.postalCode || "",
        country: billingAddress.country || "US",
      };
    } else if (shippingAddress) {
      customerData.address = {
        line1: shippingAddress.addressLine1 || "",
        line2: shippingAddress.addressLine2 || undefined,
        city: shippingAddress.city || "",
        state: shippingAddress.state || "",
        postal_code: shippingAddress.postalCode || "",
        country: shippingAddress.country || "US",
      };
    }

    const customer = await stripe.customers.create(customerData);
    console.log("✅ Customer created:", customer.id);

    // 2. Create an Ephemeral Key
    console.log("🔑 Creating ephemeral key...");
    const ephemeralKey = await stripe.ephemeralKeys.create(
      { customer: customer.id },
      { apiVersion: STRIPE_API_VERSION }
    );
    console.log("✅ Ephemeral key created");

    // 3. Prepare comprehensive metadata with Prime info
    const metadata = {
      customer_email: customerEmail || "N/A",
      customer_name: customerName || "N/A",
      order_item_count: products?.length?.toString() || "0",
      
      // ✅ Prime Membership Information
      is_prime_member: isPrimeMember ? "true" : "false",
      prime_discount: String(primeBenefits.primeDiscount),
      prime_shipping_savings: String(primeBenefits.shippingSavings),
      prime_total_savings: String(primeBenefits.totalSavings),
      prime_eligible_items: String(primeBenefits.eligibleItemsCount),
    };

    // Add shipping address to metadata
    if (shippingAddress) {
      metadata.shipping_name = shippingAddress.recipientName || "N/A";
      metadata.shipping_phone = shippingAddress.phoneNumber || "N/A";
      metadata.shipping_address_line1 = shippingAddress.addressLine1 || "N/A";
      metadata.shipping_address_line2 = shippingAddress.addressLine2 || "N/A";
      metadata.shipping_city = shippingAddress.city || "N/A";
      metadata.shipping_state = shippingAddress.state || "N/A";
      metadata.shipping_postal = shippingAddress.postalCode || "N/A";
      metadata.shipping_country = shippingAddress.country || "US";
      metadata.shipping_is_default =
        shippingAddress.isDefault?.toString() || "false";
    }

    // Calculate totals
    let itemsSubtotal = 0;
    let totalShippingCost = 0;
    let primeDiscountTotal = 0;

    // Add detailed product information
    if (products && Array.isArray(products)) {
      console.log(`📦 Processing ${products.length} products for metadata`);

      products.slice(0, 10).forEach((product, index) => {
        const shipmentCost = product.shipmentCost || 0;
        const itemPrice = product.price || 0;
        const itemQuantity = product.quantity || 0;
        const itemTotal = itemPrice * itemQuantity;
        
        // ✅ Check if item is Prime eligible
        const isPrimeEligible = product.tags?.includes("prime_eligible") || false;
        const itemPrimeDiscount = (isPrimeMember && isPrimeEligible) ? itemTotal * 0.20 : 0;
        
        itemsSubtotal += itemTotal;
        totalShippingCost += shipmentCost;
        primeDiscountTotal += itemPrimeDiscount;

        // Basic product info
        metadata[`item_${index}_id`] = String(product.id || "");
        metadata[`item_${index}_name`] = String(
          product.productName || ""
        ).substring(0, 100);
        metadata[`item_${index}_qty`] = String(itemQuantity);
        metadata[`item_${index}_price`] = String(itemPrice);
        metadata[`item_${index}_total`] = String(itemTotal);
        
        // ✅ Prime-specific item data
        metadata[`item_${index}_prime_eligible`] = isPrimeEligible ? "true" : "false";
        metadata[`item_${index}_prime_discount`] = String(itemPrimeDiscount);

        // Product details
        if (product.selectedSize) {
          metadata[`item_${index}_size`] = String(
            product.selectedSize
          ).substring(0, 100);
        }
        if (product.selectedColor) {
          metadata[`item_${index}_color`] = String(
            product.selectedColor
          ).substring(0, 100);
        }
        if (product.brand) {
          metadata[`item_${index}_brand`] = String(product.brand).substring(
            0,
            100
          );
        }
        if (product.category) {
          metadata[`item_${index}_cat`] = String(product.category).substring(
            0,
            100
          );
        }
        if (product.imageUrl) {
          metadata[`item_${index}_img`] = String(product.imageUrl).substring(
            0,
            200
          );
        }

        // Shipping info
        const shipmentMethod = product.selectedShipment || product.shipment;
        if (shipmentMethod) {
          metadata[`item_${index}_ship`] = String(shipmentMethod).substring(
            0,
            100
          );
          metadata[`item_${index}_ship_cost`] = isPrimeMember ? "0" : String(shipmentCost);
        }

        // Handle arrays (sizes and colors)
        if (product.sizes && Array.isArray(product.sizes)) {
          const sizesStr = product.sizes.slice(0, 3).join(",");
          if (sizesStr.length <= 100) {
            metadata[`item_${index}_sizes`] = sizesStr;
          } else {
            metadata[`item_${index}_sizes`] = product.sizes.slice(0, 2).join(",");
          }
        }

        if (product.colors && Array.isArray(product.colors)) {
          const colorsStr = product.colors.slice(0, 3).join(",");
          if (colorsStr.length <= 100) {
            metadata[`item_${index}_colors`] = colorsStr;
          } else {
            metadata[`item_${index}_colors`] = product.colors
              .slice(0, 2)
              .join(",");
          }
        }
      });

      // ✅ Add totals with Prime calculations
      metadata.items_subtotal = String(itemsSubtotal);
      metadata.total_shipping = isPrimeMember ? "0" : String(totalShippingCost);
      metadata.prime_discount_applied = String(primeDiscountTotal);
      
      // Calculate final amount
      const finalShipping = isPrimeMember ? 0 : totalShippingCost;
      const finalTotal = itemsSubtotal - primeDiscountTotal + finalShipping;
      metadata.grand_total = String(finalTotal);
      
      // ✅ Prime savings breakdown
      if (isPrimeMember) {
        metadata.original_total = String(itemsSubtotal + totalShippingCost);
        metadata.you_saved = String(primeDiscountTotal + totalShippingCost);
      }
    }

    // Debug logging
    console.log("🔍 Final metadata being sent to Stripe:");
    Object.keys(metadata).forEach((key) => {
      console.log(`   ${key}: ${metadata[key]}`);
    });
    console.log("📏 Total metadata keys:", Object.keys(metadata).length);

    // 4. Create line items for Stripe
    const lineItems =
      products?.map((product) => {
        const isPrimeEligible = product.tags?.includes("prime_eligible") || false;
        const basePrice = product.price || 0;
        
        // ✅ Apply Prime discount to eligible items
        const finalPrice = (isPrimeMember && isPrimeEligible) 
          ? basePrice * 0.80  // 20% off
          : basePrice;

        return {
          price_data: {
            currency: "usd",
            product_data: {
              name: product.productName || "Product",
              description: `${product.selectedSize || ""} ${
                product.selectedColor || ""
              }`.trim(),
              metadata: {
                product_id: product.productId || "",
                brand: product.brand || "",
                category: product.category || "",
                prime_eligible: isPrimeEligible ? "true" : "false",
                prime_discount_applied: (isPrimeMember && isPrimeEligible) ? "true" : "false",
              },
            },
            unit_amount: Math.round(finalPrice * 100), // Convert to cents
          },
          quantity: product.quantity || 1,
        };
      }) || [];

    // 5. Create the PaymentIntent with comprehensive metadata
    console.log("💳 Creating payment intent...");
    
    // ✅ Build description with Prime info
    let description = `Order for ${customerName || "Customer"} - ${
      products?.length || 0
    } items`;
    
    if (isPrimeMember) {
      description += ` (Prime Member - Saved $${primeBenefits.totalSavings.toFixed(2)})`;
    }

    const paymentIntent = await stripe.paymentIntents.create({
      amount: amount,
      currency: "usd",
      customer: customer.id,
      automatic_payment_methods: {
        enabled: true,
      },
      metadata: metadata,
      description: description,
      receipt_email: customerEmail || undefined,
      shipping: customerData.shipping || undefined,
    });

    console.log("✅ Payment intent created:", paymentIntent.id);

    // 6. Send response with Prime info
    const response = {
      paymentIntent: paymentIntent.client_secret,
      ephemeralKey: ephemeralKey.secret,
      customer: customer.id,
      publishableKey:
        process.env.STRIPE_PUBLISHABLE_KEY ||
        "pk_test_51L8W9ZFSOt52vVKn3eQ0ytKnk65HMXU6WWoJFMlJwPTFFIPBa0bRhxOnZs7RWMIA839EbLzlw8kFbDyqWyC6KnC400Q6ASpuTb",
      
      // ✅ Include Prime benefits in response
      primeBenefits: isPrimeMember ? {
        discount: primeBenefits.primeDiscount,
        shippingSavings: primeBenefits.shippingSavings,
        totalSavings: primeBenefits.totalSavings,
        eligibleItems: primeBenefits.eligibleItemsCount,
      } : null,
    };

    console.log("✅ Sending success response");
    console.log("📊 Order Summary:", {
      items: products?.length || 0,
      subtotal: itemsSubtotal,
      primeDiscount: isPrimeMember ? primeBenefits.primeDiscount : 0,
      shipping: isPrimeMember ? 0 : totalShippingCost,
      total: amount / 100,
      primeMember: isPrimeMember,
      savedWithPrime: isPrimeMember ? primeBenefits.totalSavings : 0,
    });

    res.json(response);
  } catch (error) {
    console.error("❌ Error in /payment-sheet:", error);
    console.error("Error details:", {
      message: error.message,
      type: error.type,
      code: error.code,
      statusCode: error.statusCode,
    });

    res.status(500).json({
      error: error.message || "Internal server error",
      type: error.type || "unknown_error",
    });
  }
});

/**
 * Stripe webhook endpoint.
 *
 * Validates Stripe signature and processes select payment events.
 * Route: POST `/webhook`
 */
app.post(
  "/webhook",
  express.raw({ type: "application/json" }),
  async (req, res) => {
    const sig = req.headers["stripe-signature"];
    const webhookSecret = process.env.STRIPE_WEBHOOK_SECRET;

    let event;

    try {
      event = stripe.webhooks.constructEvent(req.body, sig, webhookSecret);
    } catch (err) {
      console.error("⚠️ Webhook signature verification failed:", err.message);
      return res.status(400).send(`Webhook Error: ${err.message}`);
    }

    // Handle the event
    switch (event.type) {
      case "payment_intent.succeeded":
        const paymentIntent = event.data.object;
        console.log("✅ Payment succeeded:", paymentIntent.id);
        console.log("📋 Payment metadata:", paymentIntent.metadata);
        
        // ✅ Log Prime-specific info
        if (paymentIntent.metadata.is_prime_member === "true") {
          console.log("💎 Prime Order Details:");
          console.log(`   - Prime Discount: $${paymentIntent.metadata.prime_discount}`);
          console.log(`   - Shipping Savings: $${paymentIntent.metadata.prime_shipping_savings}`);
          console.log(`   - Total Savings: $${paymentIntent.metadata.prime_total_savings}`);
          console.log(`   - Eligible Items: ${paymentIntent.metadata.prime_eligible_items}`);
        }

        // Here you would:
        // 1. Create order in your database
        // 2. Track Prime benefit usage
        // 3. Update inventory
        // 4. Send confirmation email

        break;

      case "payment_intent.payment_failed":
        const failedPayment = event.data.object;
        console.error("❌ Payment failed:", failedPayment.id);
        console.error(
          "Failure reason:",
          failedPayment.last_payment_error?.message
        );
        break;

      default:
        console.log(`Unhandled event type: ${event.type}`);
    }

    res.json({ received: true });
  }
);

/**
 * Health check endpoint for deployment/monitoring.
 * Route: GET `/health`
 */
app.get("/health", (req, res) => {
  res.json({
    status: "Server is running",
    timestamp: new Date().toISOString(),
    stripeConfigured: !!process.env.STRIPE_SECRET_KEY,
    webhookConfigured: !!process.env.STRIPE_WEBHOOK_SECRET,
    features: {
      primeSupport: true,
      metadataTracking: true,
    },
  });
});

/**
 * Diagnostic endpoint to validate Prime calculations against a provided cart.
 * Route: POST `/test-prime-benefits`
 */
app.post("/test-prime-benefits", (req, res) => {
  const { products, isPrimeMember } = req.body;

  const benefits = calculatePrimeBenefits(products, isPrimeMember);

  res.json({
    isPrimeMember,
    benefits,
    breakdown: products?.map((p) => ({
      name: p.productName,
      price: p.price,
      quantity: p.quantity,
      isPrimeEligible: p.tags?.includes("prime_eligible"),
      discount: p.tags?.includes("prime_eligible") && isPrimeMember
        ? (p.price * p.quantity * 0.20)
        : 0,
      shippingCost: p.shipmentCost || 0,
      shippingSavings: isPrimeMember ? (p.shipmentCost || 0) : 0,
    })),
  });
});

/**
 * Diagnostic endpoint to verify request payload and metadata-related fields.
 * Route: POST `/test-metadata`
 */
app.post("/test-metadata", (req, res) => {
  console.log("Test metadata request:", JSON.stringify(req.body, null, 2));

  const { products, shippingAddress, isPrimeMember } = req.body;

  const primeBenefits = calculatePrimeBenefits(products, isPrimeMember);

  const summary = {
    products: products?.length || 0,
    hasShipping: !!shippingAddress,
    totalItems: products?.reduce((sum, p) => sum + (p.quantity || 0), 0) || 0,
    isPrimeMember: isPrimeMember || false,
    primeBenefits,
    received: req.body,
  };

  res.json({
    summary,
    message: "Metadata received successfully",
  });
});

/**
 * Start the HTTP server and (optionally) open an ngrok tunnel.
 *
 * Ngrok requires auth to be provided via environment configuration.
 */
const server = app.listen(PORT, "0.0.0.0", async () => {
  console.log(`🚀 Server running on port ${PORT}`);
  console.log(`📍 Health check: http://localhost:${PORT}/health`);
  console.log(`🔑 Stripe API Version: ${STRIPE_API_VERSION}`);
  console.log(`💎 Prime Membership: ENABLED`);

  // Start ngrok after server is running
  try {
    const listener = await ngrok.connect({
      addr: PORT,
      authtoken_from_env: true,
    });
    console.log(`🌐 Ngrok tunnel established at: ${listener.url()}`);
    console.log(`📌 Payment endpoint: ${listener.url()}/payment-sheet`);
    console.log(`🔔 Webhook endpoint: ${listener.url()}/webhook`);
    console.log(`🧪 Test Prime: ${listener.url()}/test-prime-benefits`);
    console.log(
      `\n⚠️  IMPORTANT: Add this webhook URL to your Stripe Dashboard:`
    );
    console.log(`   ${listener.url()}/webhook`);
  } catch (ngrokError) {
    console.error("❌ Ngrok error:", ngrokError);
  }
});

/**
 * Graceful shutdown handler to close the HTTP server cleanly.
 */
process.on("SIGINT", () => {
  console.log("\n🛑 Shutting down gracefully...");
  server.close(() => {
    console.log("✅ Server closed");
    process.exit(0);
  });
});
