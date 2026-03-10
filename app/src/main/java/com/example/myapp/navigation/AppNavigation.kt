package com.example.myapp.navigation

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapp.data.authentication.AdminGuard
import com.example.myapp.data.authentication.AuthGuard
import com.example.myapp.data.authentication.GuestGuard
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.model.OrderViewModel
import com.example.myapp.navigation.navigation_helper.NavigationAnimations
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.admin.AdminDashboardScreen
import com.example.myapp.view.admin.BrandManagementScreen
import com.example.myapp.view.admin.CarouselManagementScreen
import com.example.myapp.view.admin.CategoryManagementScreen
import com.example.myapp.view.admin.ColorManagementScreen
import com.example.myapp.view.admin.OrderManagementScreen
import com.example.myapp.view.admin.PrimeManagementScreen
import com.example.myapp.view.admin.ProductManagementScreen
import com.example.myapp.view.admin.PromotionDetailsScreen
import com.example.myapp.view.admin.PromotionManagementScreen
import com.example.myapp.view.admin.ShipmentManagementScreen
import com.example.myapp.view.admin.SizeManagementScreen
import com.example.myapp.view.admin.TagManagementScreen
import com.example.myapp.view.admin.components.AddProductScreen
import com.example.myapp.view.admin.components.EditProductScreen
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.screens.AddressManagementScreen
import com.example.myapp.view.screens.SearchScreen
import com.example.myapp.view.screens.bottom_bar.AllProductsScreen
import com.example.myapp.view.screens.bottom_bar.FavoritesScreen
import com.example.myapp.view.screens.bottom_bar.ProfileScreen
import com.example.myapp.view.screens.bottom_bar.ShopScreen
import com.example.myapp.view.screens.forms.EmailLoginScreen
import com.example.myapp.view.screens.forms.ForgotPasswordScreen
import com.example.myapp.view.screens.forms.LoginScreen
import com.example.myapp.view.screens.forms.PhoneAuthScreen
import com.example.myapp.view.screens.forms.SignUpScreen
import com.example.myapp.view.screens.product.CartScreen
import com.example.myapp.view.screens.product.PaymentScreen
import com.example.myapp.view.screens.product.ProductDescriptionScreen
import com.example.myapp.view.screens.product.ProductSharedElementScreen
import com.example.myapp.view.screens.product.order.ViewOrdersScreen
import com.example.myapp.view.screens.product.categories.FilterProducts
import com.example.myapp.view.screens.product.order.OrderConfirmationScreen
import com.example.myapp.view.screens.product.product_rating_and_reviews.ProductReviewsScreen
import com.example.myapp.view.screens.product.promotions.DailyEssentialsScreen
import com.example.myapp.view.screens.product.promotions.FlashDealsScreen
import com.example.myapp.view.screens.product.promotions.JoinPrimeScreen
import com.example.myapp.view.screens.product.promotions.PrimeDetailsScreen
import com.example.myapp.view.screens.product.promotions.PromotionsScreen

/**
 * AppNavigationGraph - Main navigation graph for the entire application.
 *
 * This composable defines all the navigation routes and their associated screens,
 * including animations and navigation logic. It uses SharedTransitionLayout for
 * smooth transitions between screens and supports both light and dark themes.
 *
 * The navigation graph is organized into several sections:
 * - Onboarding screens
 * - Bottom bar destinations (main app screens)
 * - Authentication screens (AuthGuard/GuestGuard)
 * - Product-related screens (Shared Element Transitions)
 * - Admin screens (AdminGuard)
 *
 * @param navController Navigation controller for managing navigation state.
 * @param startDestination The initial destination route.
 *
 * ## Usage:
 * ```
 * AppNavigationGraph(
 *     startDestination = AppRoutes.ON_BOARDING
 * )
 * ```
 */

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3WindowSizeClassApi::class)

@Composable
fun AppNavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String,
) {
    if (startDestination.isNotEmpty()) {
        SharedTransitionLayout {
            NavHost(
                modifier = modifier,
                navController = navController,
                startDestination = startDestination,
                enterTransition = { NavigationAnimations.slideInFromLeft() },
                exitTransition = { NavigationAnimations.crossFadeOut() },
                popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                popExitTransition = { NavigationAnimations.slideOutToRight() }
            ) {
                // ============================================
                // PUBLIC ROUTES (No Auth Required)
                // ============================================

                composable(
                    route = AppRoutes.SHOP,
                    enterTransition = { NavigationAnimations.slideInFromLeft() }, // Slide in from bottom
                    exitTransition = { NavigationAnimations.slideOutToLeft() }, // Slide out to bottom
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() }, // Back navigation
                    popExitTransition = { NavigationAnimations.slideOutToRight() } // Back navigation
                ) {
                    ShopScreen(
                        onSearchClick = { navController.navigate(AppRoutes.SEARCH) },
                        onProductClick = { product ->
                            navController.navigate("product_detail/${product.id}")
                        },
                        onCarouselClick = { navController.navigate(AppRoutes.PROMOTIONS) },
                        onCartClick = { navController.navigate(AppRoutes.CART) },
                        onAllProductsClick = { navController.navigate(AppRoutes.ALL_PRODUCTS) },
                        onLocationClick = { navController.navigate(AppRoutes.MANAGE_DELIVERY_LOCATION) },
                        onSignInClick = { navController.navigate(AppRoutes.SIGN_IN) },
                        onNavigateToPrime = { navController.navigate(AppRoutes.JOIN_PRIME) },
                        onNavigateToPrimeDetails = { navController.navigate(AppRoutes.PRIME_DETAILS) }
                    )
                }

                composable(
                    route = AppRoutes.SEARCH,
                    enterTransition = {
                        NavigationAnimations.slideInFromLeft() // cross fade
                    },
                    exitTransition = {
                        NavigationAnimations.slideOutToLeft() // Slide out to left
                    },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() }, // Back navigation
                    popExitTransition = { NavigationAnimations.slideOutToRight() } // Back navigation
                ) {
                    SearchScreen(
                        navigateToFilter = { searchQuery ->
                            // Navigate with URL encoding for special characters
                            val encodedQuery = Uri.encode(searchQuery)
                            navController.navigate("${AppRoutes.PRODUCT_FILTER}?searchQuery=$encodedQuery&category=All")
                        },
                        onResultClick = { result ->
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "search_result",
                                result
                            )
                            navController.popBackStack()
                        },
                        onProductClick = { product ->
                            navController.navigate("product_detail/${product.id}")
                        },
                        onAllProductsClick = { navController.navigate(AppRoutes.ALL_PRODUCTS) },
                        onBackNavigation = { navController.popBackStack() },
                        onSignInClick = { navController.navigate(AppRoutes.SIGN_IN) },

                        )
                }

                // All Products Screen - Browse all available products
                composable(
                    route = AppRoutes.ALL_PRODUCTS,
                    enterTransition = { NavigationAnimations.crossFade() },
                    exitTransition = { NavigationAnimations.crossFadeOut() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    AllProductsScreen(
                        onSearchClick = { navController.navigate(AppRoutes.SEARCH) },
                        onCartClick = { navController.navigate(AppRoutes.CART) },
                        onCategoryClick = { category ->
                            val encodedCategory = Uri.encode(category.categoryName)
                            navController.navigate("${AppRoutes.PRODUCT_FILTER}?searchQuery=&category=$encodedCategory")
                        },
                        onAllProductsClick = {
                            navController.navigate("${AppRoutes.PRODUCT_FILTER}?searchQuery=&category=All")
                        },
                        onProductClick = { product ->
                            navController.navigate("${AppRoutes.PRODUCT_DETAIL}/${product.id}")
                        },
                        onSignInClick = { navController.navigate(AppRoutes.SIGN_IN) }

                    )
                }

                // Update the route definition to accept query parameters
                composable(
                    route = "${AppRoutes.PRODUCT_FILTER}?searchQuery={searchQuery}&category={category}",
                    arguments = listOf(
                        navArgument("searchQuery") {
                            type = NavType.StringType
                            defaultValue = ""
                        },
                        navArgument("category") {
                            type = NavType.StringType
                            defaultValue = "All"
                        }
                    ),
                    enterTransition = {
                        NavigationAnimations.slideInFromLeft()
                    },
                    exitTransition = {
                        NavigationAnimations.slideOutToLeft()
                    },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToLeft() }
                ) { backStackEntry ->
                    val searchQuery = backStackEntry.arguments?.getString("searchQuery") ?: ""
                    val category = backStackEntry.arguments?.getString("category") ?: "All"

                    FilterProducts(
                        searchQuery = searchQuery,
                        initialCategory = category,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable,
                        onBackNavigation = { navController.popBackStack() },
                        onSearchClick = { navController.navigate(AppRoutes.SEARCH) },
                        onCartClick = { navController.navigate(AppRoutes.CART) },
                        onProductClick = { product ->
                            navController.navigate("${AppRoutes.PRODUCT_DETAIL}/${product.id}")
                        },
                        onSignInClick = { navController.navigate(AppRoutes.SIGN_IN) }
                    )
                }

                // PRODUCT DETAIL SCREEN
                composable(
                    route = "${AppRoutes.PRODUCT_DETAIL}/{productId}",
                    enterTransition = { NavigationAnimations.slideInFromRight() },
                    exitTransition = { NavigationAnimations.slideOutToLeft() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToLeft() }
                ) { backStackEntry ->

                    val productId = backStackEntry.arguments?.getString("productId") ?: ""

                    ProductDescriptionScreen(
                        productId = productId,
                        onBackNavigation = { navController.popBackStack() },
                        onSignInClick = { navController.navigate(AppRoutes.SIGN_IN) },
                        onViewReviews = { product ->
                            navController.navigate("${AppRoutes.REVIEWS}/${product.id}")
                        },
                        onRelatedProductClick = { product ->
                            navController.navigate("${AppRoutes.PRODUCT_DETAIL}/${product.id}")
                        },
                        onNavigateToPayment = { amountInCents, productItems, customerEmail, customerName, isPrimeMember, primeDiscount ->
                            navController.currentBackStackEntry?.savedStateHandle?.apply {
                                set("product_items", productItems.toTypedArray())
                                set("customer_email", customerEmail)
                                set("customer_name", customerName)
                                set("is_prime_member", isPrimeMember)
                                set("prime_discount", primeDiscount)
                            }
                            navController.navigate("${AppRoutes.PAYMENT}/${amountInCents}")
                        }
                    )
                }

                // DETAILS SCREEN (Fallback for shared element transitions)
                // Handles navigation for shared element transitions when item data is not directly available

                composable(
                    route = AppRoutes.CART,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.crossFadeOut() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.crossFadeOut() }
                ) {
                    GuestGuard(
                        onAuthenticated = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        CartScreen(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable,
                            onBackNavigation = { navController.popBackStack() },
                            onSearchClick = { navController.navigate(AppRoutes.SEARCH) },

                            // Cart items → Shared Element Screen
                            onProductClick = { product ->
                                val origin = AppRoutes.CART
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "selected_item",
                                    product
                                )
                                navController.navigate("details/${product.id}?from=$origin")
                            },

                            // Related products → Product Description Screen (normal navigation)
                            onRelatedProductClick = { product ->
                                navController.navigate("${AppRoutes.PRODUCT_DETAIL}/${product.id}")
                            },
                            onCheckOutClick = { amountInCents, cartItems, customerEmail, customerName, isPrimeMember, totalPrimeDiscount ->
                                navController.currentBackStackEntry?.savedStateHandle?.apply {
                                    set("product_items", cartItems.toTypedArray())
                                    set("customer_email", customerEmail)
                                    set("customer_name", customerName)
                                    set("is_prime_member", isPrimeMember)
                                    set("prime_discount", totalPrimeDiscount)
                                }
                                navController.navigate("${AppRoutes.PAYMENT}/${amountInCents}")
                            },
                            navigateToShop = { navController.navigate(AppRoutes.SHOP) },
                            onSignInClick = { navController.navigate(AppRoutes.SIGN_IN) }
                        )
                    }
                }

                composable(
                    route = "${AppRoutes.ORDER_DETAILS}/{userId}",
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) { backStackEntry ->

                    GuestGuard(
                        onAuthenticated = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""

                        ViewOrdersScreen(
                            userId = userId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = "${AppRoutes.REVIEWS}/{productId}",
                    arguments = listOf(
                        navArgument("productId") {
                            type = NavType.StringType
                        }
                    ),
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) { backStackEntry ->

                    val productId = backStackEntry.arguments?.getString("productId") ?: ""

                    GuestGuard(
                        onAuthenticated = {
                            navController.navigate(AppRoutes.REVIEWS) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        ProductReviewsScreen(
                            productId = productId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    "details/{itemId}?from={from}",
                    arguments = listOf(
                        navArgument("itemId") { type = NavType.StringType },
                        navArgument("from") {
                            type = NavType.StringType
                            defaultValue = "main"
                        },
                    ),
                    enterTransition = { NavigationAnimations.crossFade() },
                    exitTransition = { NavigationAnimations.crossFadeOut() },
                    popEnterTransition = { NavigationAnimations.crossFade() },
                    popExitTransition = { NavigationAnimations.crossFadeOut() }
                ) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                    val from = backStackEntry.arguments?.getString("from") ?: AppRoutes.SHOP
                    val item =
                        navController.previousBackStackEntry?.savedStateHandle?.get<ProductItem>(
                            "selected_item"
                        )

                    if (item != null) {
                        ProductSharedElementScreen(
                            id = itemId,
                            item = item,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable,
                            onBackPressed = {
                                navController.popBackStack()
                            }
                        )
                    } else {
                        // fallback to origin route if item is null
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                            when (from) {
                                AppRoutes.CART -> navController.navigate(AppRoutes.CART)
                                AppRoutes.FAVORITE -> navController.navigate(AppRoutes.FAVORITE)
                                else -> navController.navigate(AppRoutes.SHOP)
                            }
                        }
                    }
                }

                //Product Promotions
                composable(
                    route = AppRoutes.PROMOTIONS,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    PromotionsScreen(
                        onBackNavigation = { navController.popBackStack() },
                        onSearchClick = { navController.navigate(AppRoutes.SEARCH) },
                        onCartClick = { navController.navigate(AppRoutes.CART) },
                        onAllProductsClick = { navController.navigate(AppRoutes.ALL_PRODUCTS) },
                        onProductClick = { product ->
                            // Navigate to product detail screen
                            navController.navigate("product_detail/${product.id}")
                        },
                        onSignInClick = { navController.navigate(AppRoutes.SIGN_IN) },
                        onLightningDealsClick = { navController.navigate(AppRoutes.LIGHTNING_DEALS) },
                        onDailyEssentialsClick = { navController.navigate(AppRoutes.DAILY_ESSENTIALS) },
                        onNavigateToPrime = {
                            navController.navigate(AppRoutes.JOIN_PRIME)
                        },
                        onNavigateToPrimeDetails = {
                            navController.navigate(AppRoutes.PRIME_DETAILS)
                        }
                    )
                }

                composable(
                    route = AppRoutes.LIGHTNING_DEALS,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    FlashDealsScreen(
                        onBackNavigation = { navController.popBackStack() },
                        onProductClick = { product ->
                            navController.navigate("${AppRoutes.PRODUCT_DETAIL}/${product.id}")
                        },
                        onSignInClick = { navController.navigate(AppRoutes.SIGN_IN) },
                        onCartClick = { navController.navigate(AppRoutes.CART) }
                    )
                }

                composable(
                    route = AppRoutes.JOIN_PRIME,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    GuestGuard(
                        onAuthenticated = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        JoinPrimeScreen(
                            onBackNavigation = { navController.popBackStack() },
                            onJoinSuccess = {
                                // Navigate to shop after successful membership
                                navController.navigate(AppRoutes.SHOP) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                composable(
                    route = AppRoutes.DAILY_ESSENTIALS,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    DailyEssentialsScreen(
                        onBackNavigation = { navController.popBackStack() },
                        onProductClick = { product ->
                            navController.navigate("${AppRoutes.PRODUCT_DETAIL}/${product.id}")
                        },
                        onSignInClick = { navController.navigate(AppRoutes.SIGN_IN) },
                        onCartClick = { navController.navigate(AppRoutes.CART) },
                        onCategoryClick = { category ->
                            val encodedCategory = Uri.encode(category)
                            navController.navigate("${AppRoutes.PRODUCT_FILTER}?searchQuery=&category=$encodedCategory")
                        }
                    )
                }

                composable(
                    route = AppRoutes.PRIME_DETAILS,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    GuestGuard(
                        onAuthenticated = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        PrimeDetailsScreen(
                            onBackNavigation = { navController.popBackStack() },
                        )
                    }
                }

                composable(
                    route = AppRoutes.PRIME_MANAGEMENT,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    GuestGuard(
                        onAuthenticated = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        PrimeManagementScreen(
                            onBackNavigation = { navController.popBackStack() },
                        )
                    }
                }

                composable(
                    route = AppRoutes.PROMOTION_DETAILS,  // Include {promotionId}
                    arguments = listOf(
                        navArgument("promotionId") { type = NavType.StringType }
                    ),
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) { backStackEntry ->
                    val promotionId = backStackEntry.arguments?.getString("promotionId") ?: ""

                    GuestGuard(
                        onAuthenticated = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        PromotionDetailsScreen(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable,
                            promotionId = promotionId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = AppRoutes.MANAGE_DELIVERY_LOCATION,
                    enterTransition = { NavigationAnimations.slideInFromLeft() }, // Slide in from bottom
                    exitTransition = { NavigationAnimations.slideOutToLeft() }, // Slide out to bottom
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() }, // Back navigation
                    popExitTransition = { NavigationAnimations.slideOutToRight() } // Back navigation
                ) {
                    AuthGuard(
                        onUnauthenticated = {
                            navController.navigate(AppRoutes.SIGN_IN) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        AddressManagementScreen(
                            onBackNavigation = { navController.popBackStack() },
                            onSignInClick = { navController.navigate(AppRoutes.SIGN_IN) }
                        )
                    }
                }

                // ============================================
                // GUEST ONLY ROUTES (Redirect if authenticated)
                // ============================================

                composable(
                    route = AppRoutes.SIGN_IN,
                    enterTransition = { NavigationAnimations.slideInFromLeft() }, // Slide in from bottom
                    exitTransition = { NavigationAnimations.slideOutToLeft() }, // Slide out to bottom
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() }, // Back navigation
                    popExitTransition = { NavigationAnimations.slideOutToRight() } // Back navigation
                ) {
                    GuestGuard(
                        onAuthenticated = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        LoginScreen(
                            onContinueWithPhoneClick = { navController.navigate(AppRoutes.PHONE) },
                            onContinueWithEmailClick = { navController.navigate(AppRoutes.EMAIL) },
                            onSignUpClick = { navController.navigate(AppRoutes.SIGN_UP) },
                            onSignInSuccess = {
                                navController.navigate(AppRoutes.SHOP) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                }

                composable(
                    route = AppRoutes.SIGN_UP,
                    enterTransition = { NavigationAnimations.slideInFromLeft() }, // Slide in from right
                    exitTransition = { NavigationAnimations.slideOutToLeft() }, // Slide out to left
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() }, // Back navigation
                    popExitTransition = { NavigationAnimations.slideOutToRight() } // Back navigation
                ) {
                    GuestGuard(
                        onAuthenticated = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        SignUpScreen(
                            onSignInClick = { navController.navigate(AppRoutes.SIGN_IN) },
                            onNavigateToShop = {
                                navController.navigate(AppRoutes.SHOP) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                composable(route = AppRoutes.EMAIL) {
                    GuestGuard(
                        onAuthenticated = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        EmailLoginScreen(
                            onNavigateToSignUpScreen = { navController.navigate(AppRoutes.SIGN_UP) },
                            onNavigateToShopScreen = { navController.navigate(AppRoutes.SHOP) },
                            onForgetPasswordClick = { navController.navigate(AppRoutes.FORGET_PASSWORD) },
                            onContinueWithPhoneClick = { navController.navigate(AppRoutes.PHONE) }
                        )
                    }
                }

                composable(
                    route = AppRoutes.PHONE,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    GuestGuard(
                        onAuthenticated = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        PhoneAuthScreen(
                            onSignInSuccess = { navController.navigate(AppRoutes.SHOP) }
                        )
                    }
                }

                composable(
                    route = AppRoutes.FORGET_PASSWORD,
                    enterTransition = { NavigationAnimations.slideInFromLeft() }, // Slide in from bottom
                    exitTransition = { NavigationAnimations.slideOutToRight() }, // Slide out to bottom
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() }, // Back navigation
                    popExitTransition = { NavigationAnimations.slideOutToRight() } // Back navigation
                ) {
                    GuestGuard(
                        onAuthenticated = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        ForgotPasswordScreen(
                            onEmailSent = {
                                navController.popBackStack()
                            },
                            rememberPasswordClick = { navController.popBackStack() }, // Navigate to sign in
                            resetPasswordClick = { navController.navigate(AppRoutes.PASSWORD_RESET_CODE) } // Navigate to reset code
                        )
                    }
                }

                // ============================================
                // AUTHENTICATED ROUTES (Auth Required)
                // ============================================

                composable(
                    route = AppRoutes.FAVORITE,
                    enterTransition = { NavigationAnimations.crossFade() },
                    exitTransition = { NavigationAnimations.crossFadeOut() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    FavoritesScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable,
                        onSearchClick = { navController.navigate(AppRoutes.SEARCH) },
                        onProductClick = { product ->
                            val origin = AppRoutes.FAVORITE
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "selected_item",
                                product
                            )
                            navController.navigate("details/${product.id}?from=$origin")
                        },
                        onRelatedProductClick = { product ->
                            navController.navigate("${AppRoutes.PRODUCT_DETAIL}/${product.id}")
                        },
                        navigateToShop = { navController.navigate(AppRoutes.SHOP) },
                        onSignInClick = { navController.navigate(AppRoutes.SIGN_IN) }
                    )

                }

                composable(
                    route = AppRoutes.PROFILE,
                    enterTransition = { NavigationAnimations.crossFade() },
                    exitTransition = { NavigationAnimations.crossFadeOut() },
                    popEnterTransition = { NavigationAnimations.crossFade() },
                    popExitTransition = { NavigationAnimations.slideOutToLeft() }
                ) { backStackEntry ->
                    ProfileScreen(
                        onViewOrdersClick = { userId ->
                            navController.navigate("${AppRoutes.ORDER_DETAILS}/$userId")
                        },
                        onSignInClick = { navController.navigate(AppRoutes.SIGN_IN) },
                        onDashboardClick = { navController.navigate(AppRoutes.ADMIN_DASHBOARD) },
                    )
                }

                composable(
                    route = "${AppRoutes.PAYMENT}/{amountInCents}",
                    arguments = listOf(navArgument("amountInCents") { type = NavType.IntType }),
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToLeft() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToLeft() }
                ) { backStackEntry ->
                    AuthGuard(
                        onUnauthenticated = {
                            navController.navigate(AppRoutes.SIGN_IN) {
                                popUpTo(AppRoutes.PAYMENT) { inclusive = true }
                            }
                        }
                    ) {
                        val previousEntry =
                            remember(backStackEntry) { navController.previousBackStackEntry }
                        val productItems =
                            previousEntry?.savedStateHandle?.get<Array<ProductItem>>("product_items")
                                ?.toList() ?: emptyList()
                        val customerEmail =
                            previousEntry?.savedStateHandle?.get<String>("customer_email")
                        val customerName =
                            previousEntry?.savedStateHandle?.get<String>("customer_name")

                        PaymentScreen(
                            onBackNavigation = { navController.popBackStack() },
                            productItems = productItems,
                            customerEmail = customerEmail,
                            customerName = customerName,
                            onPaymentSuccess = { order ->
                                // Navigate to order confirmation after payment
                                navController.navigate("${AppRoutes.ORDER_CONFIRMATION_SCREEN}/${order.id}") {
                                    popUpTo("cart") { inclusive = true }
                                }
                            },
                        )
                    }
                }
                // ============================================
                // ADMIN ROUTES (Admin privileges required)
                // ============================================

                composable(
                    route = AppRoutes.ADMIN_DASHBOARD,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    AdminGuard(
                        onUnauthorized = {
                            // Navigate to a non-admin route instead
                            navController.navigate(AppRoutes.SHOP) {
                                // Clear the back stack up to and including admin dashboard
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    ) {
                        AdminDashboardScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onCategoryClick = { navController.navigate(AppRoutes.CATEGORY_DASHBOARD) },
                            onSizeClick = { navController.navigate(AppRoutes.SIZE_DASHBOARD) },
                            onBrandClick = { navController.navigate(AppRoutes.BRANDS_DASHBOARD) },
                            onPromotionClick = { navController.navigate(AppRoutes.PROMOTIONS_DASHBOARD) },
                            onShipmentClick = { navController.navigate(AppRoutes.SHIPMENT_DASHBOARD) },
                            onProductClick = { navController.navigate(AppRoutes.PRODUCT_DASHBOARD) },
                            onColorClick = { navController.navigate(AppRoutes.COLORS_DASHBOARD) },
                            onOrderClick = { navController.navigate(AppRoutes.ORDER_DASHBOARD) },
                            onPrimeClick = { navController.navigate(AppRoutes.PRIME_MANAGEMENT) },
                            onTagClick = { navController.navigate(AppRoutes.MANAGE_TAGS) },
                            onCarouselClick = { navController.navigate(AppRoutes.MANAGE_CAROUSEL) },
                        )
                    }
                }

                composable(
                    route = AppRoutes.CATEGORY_DASHBOARD,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    AdminGuard(
                        onUnauthorized = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    ) {
                        CategoryManagementScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = AppRoutes.ORDER_DASHBOARD,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    AdminGuard(
                        onUnauthorized = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    ) {
                        OrderManagementScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = "${AppRoutes.ORDER_CONFIRMATION_SCREEN}/{orderId}",
                    arguments = listOf(
                        navArgument("orderId") { type = NavType.StringType }),
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                    val orderViewModel: OrderViewModel = hiltViewModel()
                    val order by orderViewModel.currentOrder.collectAsState()
                    val orderState by orderViewModel.orderState.collectAsState()

                    // Load order when screen opens
                    LaunchedEffect(orderId) {
                        orderViewModel.getOrderById(orderId)
                    }

                    GuestGuard(
                        onAuthenticated = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    ) {
                        when {
                            orderState.error != null -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CustomIcon(
                                            icon = Icons.Filled.Error,
                                            contentDescription = "Error",
                                            tint = MaterialTheme.colorScheme.error,
                                            iconSize = customSpacing.custom48
                                        )
                                        CustomSpacer()
                                        Text(
                                            text = orderState.error ?: "Failed to load order",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            order != null -> {
                                OrderConfirmationScreen(
                                    order = order!!,
                                    onContinueShopping = {
                                        navController.navigate(AppRoutes.SHOP) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                composable(
                    route = AppRoutes.PRODUCT_DASHBOARD,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    AdminGuard(
                        onUnauthorized = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    ) {
                        ProductManagementScreen(
                            onAddProduct = { navController.navigate(AppRoutes.ADD_PRODUCT) },
                            onEditProduct = { product ->
                                navController.navigate("${AppRoutes.EDIT_PRODUCT}/${product.id}")
                            },
                            onNavigateBack = { navController.popBackStack() },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@composable,
                        )
                    }
                }

                composable(
                    route = AppRoutes.SIZE_DASHBOARD,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    AdminGuard(
                        onUnauthorized = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    ) {
                        SizeManagementScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = AppRoutes.BRANDS_DASHBOARD,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    AdminGuard(
                        onUnauthorized = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    ) {
                        BrandManagementScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = AppRoutes.PROMOTIONS_DASHBOARD,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    AdminGuard(
                        onUnauthorized = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    ) {
                        PromotionManagementScreen(
                            onNavigateToPromotionDetails = { promotionId ->
                                // Admin navigates to manage products in this promotion
                                navController.navigate("promotion_details/$promotionId")
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = AppRoutes.SHIPMENT_DASHBOARD,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    AdminGuard(
                        onUnauthorized = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    ) {
                        ShipmentManagementScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = "${AppRoutes.EDIT_PRODUCT}/{productId}",
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId") ?: ""

                    AdminGuard(
                        onUnauthorized = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        EditProductScreen(
                            productId = productId,
                            onProductUpdated = { navController.popBackStack() },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = AppRoutes.ADD_PRODUCT,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    AdminGuard(
                        onUnauthorized = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        AddProductScreen(
                            onProductAdded = { navController.popBackStack() },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = AppRoutes.COLORS_DASHBOARD,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    AdminGuard(
                        onUnauthorized = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        ColorManagementScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = AppRoutes.MANAGE_TAGS,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    AdminGuard(
                        onUnauthorized = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        TagManagementScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = AppRoutes.MANAGE_CAROUSEL,
                    enterTransition = { NavigationAnimations.slideInFromLeft() },
                    exitTransition = { NavigationAnimations.slideOutToRight() },
                    popEnterTransition = { NavigationAnimations.slideInFromLeft() },
                    popExitTransition = { NavigationAnimations.slideOutToRight() }
                ) {
                    AdminGuard(
                        onUnauthorized = {
                            navController.navigate(AppRoutes.SHOP) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        CarouselManagementScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

