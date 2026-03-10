## Doritaas App

Doritaas is a modern e‑commerce Android application built with Jetpack Compose.  
It provides a full shopping experience for customers (browse, search, cart, checkout, orders, Prime‑style membership) plus an integrated admin dashboard for managing products, categories, promotions, shipments, and more.

---

## 🚀 Core Features

- **Shopping Experience**
  - **Product catalog** with rich cards, shimmer loading, and shared‑element transitions.
  - **Advanced filtering** by category, search query, and other attributes.
  - **Search** with recent/popular queries and smart filtering.
  - **Cart & favorites** flows with quantity updates and related products.
- **User Accounts & Auth**
  - Email/password, phone‑based auth, and password reset flows.
  - Guarded routes using `AuthGuard`, `GuestGuard`, and `AdminGuard`.
- **Prime Membership**
  - Prime onboarding (`JoinPrimeScreen`) and membership management.
  - Dynamic shipping options and discounts powered by `PrimeBenefitsService`.
- **Payments & Orders**
  - Stripe Payment Sheet integration via a secure backend (`PaymentApi`).
  - Order creation, confirmation screen, and order history per user.
  - Delivery address management and shipping options per order.
- **Promotions & Discovery**
  - Dedicated promotions screen (flash deals, daily essentials, Prime offers).
  - Carousel content driving navigation into promotional campaigns.
- **Admin Dashboard**
  - Admin‑only dashboard for managing:
    - Products (CRUD with shared‑element previews)
    - Categories, brands, tags, sizes, and colors
    - Promotions and promotional carousels
    - Shipments and orders
- **UI & UX**
  - Responsive, adaptive layouts for different window sizes.
  - Custom components for app bar, chips, dialogs, bottom bar, and more.

---

## 🗂️ Project Structure (Core Modules)

```text
doritaas-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/myapp/
│   │   │   │   ├── data/                  # Repositories, services, use cases, data classes
│   │   │   │   ├── navigation/            # Navigation graph, routes, guards
│   │   │   │   ├── view/
│   │   │   │   │   ├── admin/             # Admin dashboard & management screens
│   │   │   │   │   └── screens/           # Customer‑facing screens (auth, shop, cart, orders, etc.)
│   │   │   │   ├── ui/theme/              # Theme, colors, spacing, window size constants
│   │   │   │   └── ...
│   │   │   ├── res/                       # Resources (drawables, values, etc.)
│   │   │   └── AndroidManifest.xml        # App manifest
│   ├── build.gradle.kts                   # App‑level Gradle config
├── build.gradle.kts                       # Project‑level Gradle config
├── settings.gradle.kts                    # Gradle settings
└── ...
```

---

## 🛠️ Tech Stack

- **Language & UI**
  - **Kotlin**
  - **Jetpack Compose** + **Material 3**
  - **Navigation Compose** with shared transitions
- **Architecture & DI**
  - ViewModels with Kotlin coroutines/flows
  - **Hilt** for dependency injection (`@HiltAndroidApp` `AuthApplication`, modules in `data/modules`)
- **Backend & Data**
  - **Firebase Auth** and Firestore‑backed models (e.g. product IDs, orders, membership).
  - **Retrofit** for backend APIs (e.g. Stripe payment backend via `PaymentApi`).
- **Payments & Media**
  - **Stripe Payment Sheet** for secure checkout.
  - **Coil** for image loading.
  - **Cloudinary** for media management (initialized in `AuthApplication`).

---

## 📝 Key Flows & Screens

- **Authentication**
  - Login, sign‑up, phone auth, email login, and password reset flows.
  - Guards that redirect users depending on auth/admin state.
- **Shopping & Discovery**
  - Shop, search, product detail, cart, promotions, prime details, and reviews.
  - Order confirmation and order history screens.
- **Prime Membership**
  - Join Prime, manage membership, and see Prime‑specific benefits.
- **Admin**
  - Admin dashboard entry from profile (for admin users).
  - Management screens for products, orders, shipments, sizes, brands, tags, colors, promotions, and carousels.

---

## 🧑‍💻 For Developers

- **Code Style**
  - Compose best practices with state hoisting and previews where helpful.
  - Clear separation between `data`, `view`, `navigation`, and `ui/theme`.
- **Theming**
  - Centralized colors, typography, spacing, and window‑size driven constants.
- **Data & Domain**
  - Repositories per domain (auth, products, cart, payments, orders, prime, etc.).
  - Use cases and services for encapsulating business logic (`PrimeBenefitsService`, order fetching, search, etc.).
- **Documentation**
  - Many core classes and flows are documented with KDoc.
  - `KDOC_PROGRESS.md` in `app/` tracks documentation coverage.

---

## 🏁 Getting Started

1. **Clone the repository**
2. **Open in Android Studio** (Giraffe or newer recommended)
3. **Configure Firebase / backend keys** as required in your local environment
4. **Sync Gradle**
5. **Run on an emulator or physical device**

## 📂 Notable Paths

- `app/src/main/AndroidManifest.xml` — Application declaration and entry points.
- `app/src/main/java/com/example/myapp/navigation/` — Main navigation graph and route definitions.
- `app/src/main/java/com/example/myapp/data/` — Data models, repositories, services, and DI modules.
- `app/src/main/java/com/example/myapp/view/screens/` — Customer‑facing screens (shop, auth, cart, orders, promotions, etc.).
- `app/src/main/java/com/example/myapp/view/admin/` — Admin dashboard and management UIs.
- `app/src/main/java/com/example/myapp/ui/theme/` — Theming, typography, spacing, and window‑size constants.

---
## 🖥️ Local Backend Server

The payment backend is a Node.js/Express server that handles Stripe Payment Sheet creation and webhook events.

### 📦 Tech Stack
- **Node.js** + **Express**
- **Stripe** Node SDK
- **ngrok** — for exposing the local server to the internet (used during development)

---

### ⚙️ Setup

1. **Install dependencies**
```bash
   cd server
   npm install
```

2. **Configure environment variables**
   Create a `.env` file in the server root:
```env
   STRIPE_SECRET_KEY=sk_test_your_key_here
   STRIPE_PUBLISHABLE_KEY=pk_test_your_key_here
   STRIPE_WEBHOOK_SECRET=whsec_your_secret_here
   NGROK_AUTHTOKEN=your_ngrok_token_here
   PORT=3000
```

3. **Run the server**
```bash
   node server.js
```
On startup, ngrok automatically creates a public tunnel and prints the URLs:
```
   🌐 Ngrok tunnel established at: https://xxxx-xx-xx.ngrok-free.app
   📌 Payment endpoint: https://xxxx-xx-xx.ngrok-free.app/payment-sheet
   🔔 Webhook endpoint:  https://xxxx-xx-xx.ngrok-free.app/webhook
```

---

### 🔔 Stripe Webhook Setup

Each time the server restarts, ngrok generates a **new URL**. You must update your Stripe webhook accordingly:

1. Copy the `/webhook` URL printed in the console
2. Go to **Stripe Dashboard → Developers → Webhooks**
3. Update or add the endpoint URL
4. Copy the new **Signing Secret** into your `.env` as `STRIPE_WEBHOOK_SECRET`

---

### 📡 API Endpoints

| Method | Endpoint               | Description                                 |
|--------|------------------------|---------------------------------------------|
| `POST` | `/payment-sheet`       | Creates Stripe PaymentIntent + EphemeralKey |
| `POST` | `/webhook`             | Handles Stripe webhook events               |
| `GET`  | `/health`              | Server health check                         |
| `POST` | `/test-prime-benefits` | Test Prime discount calculations            |
| `POST` | `/test-metadata`       | Test metadata payload handling              |

---

### ⚠️ Important Notes

- **ngrok URL changes on every restart** — always update `BASE_URL` in the Android app's `PaymentApi` and the Stripe webhook dashboard.
- Never commit your `.env` file — add it to `.gitignore`.
- The server exposes `0.0.0.0` so it's reachable from both emulator and physical devices on the same network.

---

## 📧 Contact & Contribution

For questions, suggestions, or contributions, please open an issue or submit a pull request.

---
## 📝 License

This project is licensed under the UMUNTHU HUB Noncommercial License.

- **Noncommercial use is permitted.**
- **Commercial use requires a paid license.**

See the [LICENSE](./LICENSE) file for details and contact information.
