# Payment API Guide

## What this backend does
- Creates Stripe PaymentSheet sessions via `POST /payment-sheet`
- Receives Stripe webhooks via `POST /webhook`
- Supports **Prime membership** logic (20% off Prime-eligible items + free shipping savings)
- Provides test/diagnostic endpoints for Prime + metadata validation

## Endpoints
- `POST /payment-sheet` - Create Stripe PaymentSheet fields (PaymentIntent client secret + ephemeral key + customer id)
- `POST /webhook` - Stripe webhook receiver (requires Stripe signature header + configured secret)
- `GET /health` - Health check + feature flags
- `POST /test-prime-benefits` - Validate Prime savings calculation from a cart payload
- `POST /test-metadata` - Echo/summary endpoint to verify request shape + computed Prime benefits

## Run the server
Install deps (first time):

```bash
npm install
```

Start:

```bash
npm run start
```

Dev (auto-reload):

```bash
npm run dev
```

Ngrok is started by `server.js`. Provide your auth token via environment (recommended via `.env`).

## Environment variables
Set these in `.env` (or your deployment environment):
- `PORT` (optional)
- `STRIPE_SECRET_KEY` (required)
- `STRIPE_PUBLISHABLE_KEY` (required by clients; server returns it in the response)
- `STRIPE_WEBHOOK_SECRET` (required for `/webhook` signature verification)
- `NGROK_AUTHTOKEN` (optional, for public tunnel)

This app is currently configured for **Stripe test mode** keys. For production, switch to **live mode** keys and update your Stripe dashboard configuration accordingly.

## `POST /payment-sheet` request shape (products + Prime)
Minimum required:
- `amount` (number): total in **cents**

Supported fields:
- `customerEmail` (string)
- `customerName` (string)
- `products` (array): cart line items (see below)
- `shippingAddress` / `billingAddress` (object): address fields used for Stripe customer + metadata
- `isPrimeMember` (boolean): enables Prime savings behavior

Each `products[]` item can include:
- `productName` (string)
- `price` (number): item price in **dollars**
- `quantity` (number)
- `shipmentCost` (number): shipping cost in **dollars**
- `tags` (array of strings): include `"prime_eligible"` to mark an item eligible for Prime discount
- Optional: `selectedSize`, `selectedColor`, `brand`, `category`, `imageUrl`, `selectedShipment`/`shipment`

Example request:

```bash
curl -X POST http://localhost:3000/payment-sheet \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 2599,
    "customerEmail": "customer@example.com",
    "customerName": "Test Customer",
    "isPrimeMember": true,
    "products": [
      {
        "id": "sku_123",
        "productId": "sku_123",
        "productName": "Doritaas Chips",
        "price": 9.99,
        "quantity": 2,
        "shipmentCost": 4.50,
        "tags": ["prime_eligible"],
        "brand": "Doritaas",
        "category": "Snacks"
      }
    ],
    "shippingAddress": {
      "recipientName": "Test Customer",
      "phoneNumber": "+15551234567",
      "addressLine1": "123 Main St",
      "addressLine2": "",
      "city": "Springfield",
      "state": "CA",
      "postalCode": "99999",
      "country": "US",
      "isDefault": true
    }
  }'
```

Notes:
- Prime discount is computed per item when `isPrimeMember=true` and the item has tag `"prime_eligible"`.
- Shipping is treated as free for Prime (the server sets shipping cost to 0 for Prime in totals/metadata).
- The response includes `primeBenefits` when Prime is enabled.

## Webhooks (`POST /webhook`)
- Configure your Stripe webhook endpoint to point to `/webhook`
- Set `STRIPE_WEBHOOK_SECRET` so the server can verify the Stripe signature