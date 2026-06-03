## Doritaas App

Doritaas is a modern e‑commerce Android application built with Jetpack Compose and Stripe payment Api.  
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

### Prerequisites

- **Android Studio** (Giraffe or newer recommended)
- **Node.js** (v18 or higher) and npm
- **Firebase account** (free tier is sufficient)
- **Stripe account** (test mode)
- **Cloudinary account** (free tier is sufficient)
- **ngrok account** (for local development tunneling)

---

## ⚙️ Project Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd doritaas-app
```

### 2. Firebase Setup

1. **Create a Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Click "Add project" and follow the setup wizard

2. **Enable Firebase Services**
   - **Authentication**: Enable Email/Password and Phone authentication
   - **Firestore Database**: Create a database in test mode
   - (Optional) **Analytics**: Enable if desired

3. **Add Android App to Firebase**
   - In Firebase Console, click the Android icon to add an app
   - Register your app with package name: `com.example.myapp`
   - Download `google-services.json`
   - Place `google-services.json` in `app/src/main/` directory

4. ** copy and paste the rules or Configure Firestore Security Rules** (for development):

   ```
    rules_version = '2';

    service cloud.firestore {
    match /databases/{database}/documents {
    
    // ============================================
    // HELPER FUNCTIONS
    // ============================================
    
    function isAuthenticated() {
    return request.auth != null;
    }
    
    function currentUserDoc() {
    return get(/databases/$(database)/documents/users/$(request.auth.uid)).data;
    }
    
    function isAdmin() {
    return isAuthenticated() &&
    exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
    currentUserDoc().admin == true;
    }
    
    function isSuperAdmin() {
    return isAuthenticated() &&
    exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
    currentUserDoc().superAdmin == true;
    }
    
    function isOwner(userId) {
    return isAuthenticated() && request.auth.uid == userId;
    }
    
    // ============================================
    // USERS COLLECTION
    // ============================================
    
    match /users/{userId} {
    allow read: if isOwner(userId) || isAdmin() || isSuperAdmin();
    
    // Any authenticated user can create their own account on sign-up
    allow create: if request.auth.uid == userId &&
    // Cannot self-assign admin or superAdmin on creation
    !request.resource.data.keys().hasAny(['admin', 'superAdmin']);
    
    allow update: if (
    // --- Case 1: User updating their own non-privileged fields ---
    (
    isOwner(userId) &&
    // Must not touch admin or superAdmin fields
    !request.resource.data.diff(resource.data).affectedKeys()
    .hasAny(['admin', 'superAdmin'])
    ) ||
    
         // --- Case 2: SuperAdmin can update anything EXCEPT their own superAdmin field ---
         (
           isSuperAdmin() &&
           !(isOwner(userId) &&
             request.resource.data.diff(resource.data).affectedKeys().hasAny(['superAdmin']))
         ) ||
    
         // --- Case 3: Regular admin can update non-admin users' basic fields only ---
         (
           isAdmin() &&
           !isSuperAdmin() &&
           resource.data.admin == false &&         // Target must not already be admin
           resource.data.superAdmin != true &&     // Target must not be superAdmin
           !request.resource.data.diff(resource.data).affectedKeys()
             .hasAny(['admin', 'superAdmin'])      // Cannot escalate privileges
         )
    );
    
    // Only superAdmin can delete users; admins cannot delete anyone
    allow delete: if isSuperAdmin() && !isOwner(userId);
    
    // ============================================
    // USER SUBCOLLECTIONS
    // ============================================
    
    match /cart/{cartId} {
    allow read, write: if isOwner(userId) || isAdmin() || isSuperAdmin();
    }
    
    match /favorites/{favoriteId} {
    allow read, write: if isOwner(userId) || isAdmin() || isSuperAdmin();
    }
    
    match /orders/{orderId} {
    allow read: if isOwner(userId) || isAdmin() || isSuperAdmin();
    allow create: if isOwner(userId);
    allow update, delete: if isAdmin() || isSuperAdmin();
    }
    }
    
    // ============================================
    // PRIME MEMBERSHIPS
    // ============================================
    
    match /prime_memberships/{membershipId} {
    allow read: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow create: if isAuthenticated() &&
    request.resource.data.userId == request.auth.uid;
    allow update, delete: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    }
    
    // ============================================
    // PRIME TRANSACTIONS
    // ============================================
    
    match /prime_transactions/{transactionId} {
    allow read: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow create: if isAuthenticated() &&
    (request.resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow update, delete: if isAdmin() || isSuperAdmin();
    }
    
    match /prime_benefit_usage/{benefitId} {
    allow read: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow create: if isAuthenticated() &&
    (request.resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow update, delete: if isAdmin() || isSuperAdmin();
    }
    
    match /transactions/{transactionId} {
    allow read: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow create: if isAuthenticated() &&
    (request.resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow update, delete: if isAdmin() || isSuperAdmin();
    }
    
    match /benefit_usage/{usageId} {
    allow read: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow create: if isAuthenticated() &&
    (request.resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow update, delete: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    }
    
    match /prime_benefits/{benefitId} {
    allow read: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow create: if isAuthenticated() &&
    (request.resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow update, delete: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    }
    
    // ============================================
    // DELIVERY ADDRESSES
    // ============================================
    
    match /deliveryAddresses/{addressId} {
    allow read: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow create: if isAuthenticated() &&
    request.resource.data.userId == request.auth.uid;
    allow update: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow delete: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    }
    
    // ============================================
    // PUBLIC READ COLLECTIONS (admin write only)
    // ============================================
    
    match /products/{productId} {
    allow read: if true;
    allow create, update, delete: if isAdmin() || isSuperAdmin();
    }
    
    match /image_carousel/{carouselId} {
    allow read: if true;
    allow create, update, delete: if isAdmin() || isSuperAdmin();
    }
    
    match /categories/{categoryId} {
    allow read: if true;
    allow create, update, delete: if isAdmin() || isSuperAdmin();
    }
    
    match /brands/{brandId} {
    allow read: if true;
    allow create, update, delete: if isAdmin() || isSuperAdmin();
    }
    
    match /product_tags/{tagId} {
    allow read: if true;
    allow create, update, delete: if isAdmin() || isSuperAdmin();
    }
    
    match /shipment_option/{shipmentId} {
    allow read: if true;
    allow create, update, delete: if isAdmin() || isSuperAdmin();
    }
    
    match /shipment_options/{shipmentId} {
    allow read: if true;
    allow create, update, delete: if isAdmin() || isSuperAdmin();
    }
    
    match /sizes/{sizeId} {
    allow read: if true;
    allow create, update, delete: if isAdmin() || isSuperAdmin();
    }
    
    match /colors/{colorId} {
    allow read: if true;
    allow create, update, delete: if isAdmin() || isSuperAdmin();
    }
    
    match /special_offers/{offersId} {
    allow read: if true;
    allow create, update, delete: if isAdmin() || isSuperAdmin();
    }
    
    match /promotions/{promotionId} {
    allow read: if true;
    allow write: if isAdmin() || isSuperAdmin();
    }
    
    match /promotion_products/{junctionId} {
    allow read: if true;
    allow write: if isAdmin() || isSuperAdmin();
    }
    
    match /settings/{settingId} {
    allow read: if true;
    allow write: if isAdmin() || isSuperAdmin();
    }
    
    // ============================================
    // RATINGS & REVIEWS
    // ============================================
    
    match /ratings/{ratingId} {
    allow read: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow create: if isAuthenticated() &&
    request.resource.data.userId == request.auth.uid;
    allow update, delete: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    }
    
    match /reviews/{reviewId} {
    allow read: if true;
    allow create: if isAuthenticated() &&
    request.resource.data.userId == request.auth.uid &&
    request.resource.data.productId != null;
    allow update, delete: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    }
    
    // ============================================
    // ORDERS
    // ============================================
    
    match /orders/{orderId} {
    allow read: if isAuthenticated() &&
    (resource.data.userId == request.auth.uid || isAdmin() || isSuperAdmin());
    allow create: if isAuthenticated() &&
    request.resource.data.userId == request.auth.uid;
    allow update, delete: if isAdmin() || isSuperAdmin();
    }
    
    // ============================================
    // ADMIN / SUPERADMIN ONLY
    // ============================================
    
    match /adminData/{document=**} {
    allow read, write: if isAdmin() || isSuperAdmin();
    }
    
    match /analytics/{document=**} {
    allow read, write: if isAdmin() || isSuperAdmin();
    }
    }
    }
```

### 3. Cloudinary Setup

1. **Create a Cloudinary Account**
   - Go to [Cloudinary](https://cloudinary.com/) and sign up

2. **Get Your Cloudinary Credentials**
   - Navigate to Dashboard → API Keys
   - Copy your:
     - Cloud Name
     - API Key
     - API Secret

3. **Configure Cloudinary in the App**
   - Open `app/src/main/java/com/example/myapp/data/authentication/AuthAplication.kt`
   - Update the Cloudinary config with your credentials:
   ```kotlin
   val config = hashMapOf(
       "cloud_name" to "YOUR_CLOUD_NAME",
       "api_key" to "YOUR_API_KEY",
       "api_secret" to "YOUR_API_SECRET"
   )
   MediaManager.init(this, config)
   ```

### 4. Stripe Setup

#### Backend Server Setup

1. **Navigate to the Server Directory**
   ```bash
   cd server
   ```

2. **Install Dependencies**
   ```bash
   npm install
   ```

3. **Configure Environment Variables**
   - Create a `.env` file in the `server` directory (already exists)
   - Add your Stripe credentials:
   ```env
   # Environment
   NODE_ENV=development

   # Stripe API Keys - Get these from https://dashboard.stripe.com/test/apikeys
   STRIPE_SECRET_KEY=sk_test_your_secret_key_here
   STRIPE_PUBLISHABLE_KEY=pk_test_your_publishable_key_here

   # Server configuration
   PORT=3000

   # Webhook Secret (get after setting up webhook)
   STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret_here

   # Ngrok Auth Token (get from https://dashboard.ngrok.com/get-started/your-authtoken)
   NGROK_AUTHTOKEN=your_ngrok_token_here

   # Email Credentials (for order notifications)
   EMAIL_USER=your_email@gmail.com
   EMAIL_PASSWORD=your_app_password
   ```

4. **Run the Server**
   ```bash
   node server.js
   ```
   The server will automatically start ngrok and print the tunnel URLs.

#### Stripe Webhook Setup

Each time the server restarts, ngrok generates a new URL. Update your Stripe webhook:

1. Copy the `/webhook` URL from the server console output
2. Go to **Stripe Dashboard → Developers → Webhooks**
3. Add or update the endpoint with the new ngrok URL
4. Copy the new **Signing Secret** and update `STRIPE_WEBHOOK_SECRET` in your `.env` file

#### Android App Stripe Configuration

1. **Update PaymentApi Base URL**
   - Open `app/src/main/java/com/example/myapp/data/api/PaymentApi.kt`
   - Update the `BASE_URL` to match your ngrok tunnel URL (without trailing slash):
   ```kotlin
   private const val BASE_URL = "https://xxxx-xx-xx.ngrok-free.app"
   ```

### 5. Admin Setup

Admin users are managed through Firebase Firestore. You must manually add role fields to user documents to grant admin privileges.

#### User Roles Overview

The application supports two admin roles:

- **admin**: Standard admin with access to most dashboard features
- **superAdmin**: Full admin with additional privileges (can manage other admins, delete users)

#### Manual Firestore Configuration

**Step 1: Create a User Account**
- Sign up in the app using email/password or phone authentication
- The user will be created in Firebase Authentication
- A user document will be automatically created in Firestore under the `users` collection

**Step 2: Add Admin Fields in Firestore**

1. Go to **Firebase Console → Firestore Database**
2. Navigate to the `users` collection
3. Find the user document (document ID = Firebase UID)
4. Click the document to edit it
5. Add the following fields manually:

**For Standard Admin:**
```
Field: admin
Type: boolean
Value: true
```

**For Super Admin (full privileges):**
```
Field: admin
Type: boolean
Value: true

Field: superAdmin
Type: boolean
Value: true
```

**Example Firestore Document Structure:**
```json
{
  "uid": "abc123xyz...",
  "email": "admin@example.com",
  "phone": "+265991234567",
  "name": "Admin User",
  "admin": true,
  "superAdmin": true,
  "createdAt": "2024-01-01T00:00:00Z",
  ...other user fields
}
```

#### Role Permissions

| Feature | Regular User | Admin | Super Admin |
|---------|--------------|-------|-------------|
| Shop & Browse Products | ✅ | ✅ | ✅ |
| Add to Cart & Checkout | ✅ | ✅ | ✅ |
| View Orders | ✅ | ✅ | ✅ |
| Access Admin Dashboard | ❌ | ✅ | ✅ |
| Manage Products | ❌ | ✅ | ✅ |
| Manage Categories/Brands/Tags | ❌ | ✅ | ✅ |
| Manage Promotions | ❌ | ✅ | ✅ |
| View All Orders | ❌ | ✅ | ✅ |
| Manage Shipments | ❌ | ✅ | ✅ |
| Manage Prime Memberships | ❌ | ✅ | ✅ |
| Promote/Demote Admins | ❌ | ❌ | ✅ |
| Delete Users | ❌ | ❌ | ✅ |

#### Alternative Setup Method

If the user document doesn't exist yet:

1. In Firebase Console, go to **Authentication**
2. Find the user you want to make admin
3. Copy their UID
4. Go to **Firestore Database** → `users` collection
5. Click "Add document"
6. Set the Document ID to the user's UID
7. Add the following fields:
   - `admin` (boolean): `true`
   - `superAdmin` (boolean): `true` (optional)


#### Admin Dashboard Access

- Once a user has `admin: true` in their Firestore profile
- They will see a "Dashboard" button in the Profile screen
- The dashboard provides access to:
  - Product management (CRUD operations)
  - Category, brand, tag, size, and color management
  - Promotion and carousel management
  - Order and shipment management
  - User management (promote/demote admins, delete users) - **Super Admin only**
  - Prime membership management

#### Important Notes

- **Manual field addition is required**: The app does not automatically create admin fields
- **Field names are case-sensitive**: Use `admin` and `superAdmin` exactly as shown
- **Boolean values**: Must be `true` or `false` (lowercase)
- **Super Admin includes Admin privileges**: A superAdmin automatically has admin access
- **Changes take effect immediately**: Once fields are added, the user will see the Dashboard button on next app refresh

### 6. Build and Run the App

1. **Open in Android Studio**
   - Open the project folder in Android Studio

2. **Sync Gradle**
   - Android Studio will prompt to sync Gradle
   - Click "Sync Now" or use File → Sync Project with Gradle Files

3. **Configure Build Variants**
   - Select the debug build variant for development

4. **Run the App**
   - Connect an Android device or start an emulator
   - Click the Run button in Android Studio
   - Ensure the backend server is running before testing payments

---

## 📝 Environment Variables Summary

### Server (.env)
```env
NODE_ENV=development
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
PORT=3000
NGROK_AUTHTOKEN=...
EMAIL_USER=your_email@gmail.com
EMAIL_PASSWORD=your_app_password
```

### Android App
- **Firebase**: `google-services.json` in `app/src/main/`
- **Cloudinary**: Hardcoded in `AuthApplication.kt` (consider moving to local.properties for security)
- **Stripe Base URL**: Update in `PaymentApi.kt` when ngrok URL changes

---

## 🔒 Security Notes

- **Never commit** `.env` files, `google-services.json`, or API keys to version control
- These files are already in `.gitignore`
- For production, use environment variables or secure secret management
- Consider using Firebase Remote Config for managing app configuration
- Rotate API keys regularly in production

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

This project is licensed under the UMUNTHU HUB Dual-Tier License.

- **Noncommercial & Educational use is free.**
- **Commercial use requires a Non-Exclusive or Exclusive commercial license.**

See the [LICENSE](./LICENSE) file for details and contact information.
