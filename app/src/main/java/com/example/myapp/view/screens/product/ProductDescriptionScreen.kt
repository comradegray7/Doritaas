package com.example.myapp.view.screens.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.ProductItem
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.AuthViewModel
import com.example.myapp.data.model.CartViewModel
import com.example.myapp.data.model.FavoriteViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.PrimeMembershipViewModel
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.admin.components.ProductColorSelection
import com.example.myapp.view.components.CustomAssistChip
import com.example.myapp.view.components.CustomBottomSection
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomHorizontalDivider
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomImageContainer
import com.example.myapp.view.components.CustomItemCardShimmer
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomLazyRow
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomSurfaceContainer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.ProductDescriptionShimmer
import com.example.myapp.view.components.SignInRequiredDialog
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomElevatedButton
import com.example.myapp.view.components.custom.buttons.CustomOutlinedButton
import com.example.myapp.view.screens.product.product_rating_and_reviews.ProductRating
import com.example.myapp.view.screens.product.product_rating_and_reviews.ProductRatingDialog
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CloudinaryHelper
import com.example.myapp.view.utils.CustomShape
import com.example.myapp.view.utils.calculateDiscountedPrice
import com.example.myapp.view.utils.formatPrice
import com.example.myapp.view.utils.primeUtils.isUserPrimeMember
import com.example.myapp.view.utils.toStripeCents

/**
 * ProductDescriptionScreen - Detailed product view with images, specifications, and purchase options.
 *
 * This composable displays comprehensive product information including:
 * - Product images with selection capability
 * - Color and size selection options
 * - Product specifications and details
 * - Shipping options
 * - Quantity selection
 * - Purchase actions
 *
 * @param productId Unique identifier of the product to display.
 * @param onBackNavigation Callback for back navigation.
 * @param viewModel ViewModel for product data.
 * @param primeViewModel ViewModel for Prime membership data.
 * @param onNavigateToPayment Callback to initiate payment process.
 * @param cartViewModel ViewModel for shopping cart operations.
 * @param favoriteViewModel ViewModel for wishlist operations.
 * @param authViewModel ViewModel for authentication status.
 * @param onSignInClick Callback when sign-in is required.
 * @param onViewReviews Callback to navigate to reviews screen.
 * @param onRelatedProductClick Callback when a related product is clicked.
 * @param networkManager Manager for network connectivity status.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDescriptionScreen(
    productId: String,
    onBackNavigation: () -> Unit,
    viewModel: ProductCrudViewModel = hiltViewModel(),
    primeViewModel: PrimeMembershipViewModel = hiltViewModel(),
    onNavigateToPayment: (
        amountInCents: Int,
        productItem: List<ProductItem>,
        customerEmail: String?, customerName: String?,
        isPrimeMember: Boolean,
        primeDiscount: Double,
    ) -> Unit,
    cartViewModel: CartViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onSignInClick: () -> Unit,
    onViewReviews: (ProductItem) -> Unit,
    onRelatedProductClick: (ProductItem) -> Unit,
    cloudinaryHelper: CloudinaryHelper = CloudinaryHelper(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {

    val windowSizeConstant = LocalWindowSizeConstant.current
    val uiState by viewModel.productState.collectAsState()
    val imageLoader = viewModel.getImageLoader()
    val product = uiState.currentProduct
    val primeState by primeViewModel.membershipState.collectAsState()
    val currentUserReview by viewModel.currentUserReview.collectAsState()
    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedProductForRating by remember { mutableStateOf<ProductItem?>(null) }

    val membershipStatus = primeState.membership?.status // This is your MembershipStatus enum

    val isPrimeMember = isUserPrimeMember(membershipStatus) // Returns true if ACTIVE
    val isNewArrival = product?.tags?.contains("new_arrival")
    val isLimitedEdition = product?.tags?.contains("limited_edition")
    val isEcoFriendly = product?.tags?.contains("eco_friendly")
    val isPrimeEligible = product?.tags?.contains("prime_eligible")
    val isFlashDeal = product?.tags?.contains("flash_deal")
    val isBestSeller = product?.tags?.contains("best_seller")

    val showPrimeBadge = isPrimeEligible == true && isPrimeMember
    val networkState = rememberNetworkState(networkManager)

    val user by authViewModel.authState.collectAsState()
    val customerEmail = user.user?.email // Get from your auth system
    val customerName = user.user?.displayName // Get from user profile

    // State management for product selection and display
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    var selectedSize by remember { mutableStateOf("M") }
    var quantity by remember { mutableIntStateOf(1) }
    var isExpanded by remember { mutableStateOf(false) }
    var selectedShipping by remember { mutableStateOf("") } // Changed to empty string
    val defaultColor = remember { listOf("Black") }
    var selectedColor by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        viewModel.getProductById(productId)
        viewModel.loadCurrentUserReview(productId)
    }

    // Clear current product when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearCurrentProduct()
        }
    }

    // Calculate the actual savings amount
    val savingsAmount = product?.oldPrice?.minus((product.price * quantity))?.toFloat()

    val allImages = remember(product?.imageUrl, product?.supportingImageUrls) {
        buildList {
            product?.imageUrl?.let { url ->
                if (url.isNotBlank()) add(url)
            }
            product?.supportingImageUrls?.forEach { url ->
                if (url.isNotBlank()) add(url)
            }
        }
    }

    // Calculate base product total
    val baseTotal = (product?.price ?: 0.0) * quantity

// Add selected shipping cost if applicable
    val shippingCost = uiState.shipmentItem.find { it.name == selectedShipping }?.price ?: 0.0

    val primeDiscount = remember(product, isPrimeMember) {
        if (isPrimeMember && product?.tags?.contains("prime_eligible") == true) {
            (product.price * quantity) * 0.20 // 20% discount
        } else {
            0.0
        }
    }

    // Calculate total with Prime discount
    val totalPrice = remember(baseTotal, shippingCost, primeDiscount, isPrimeMember) {
        val shipping = if (isPrimeMember) 0.0 else shippingCost
        baseTotal - primeDiscount + shipping
    }

    val productsForPayment = remember(
        product,
        quantity,
        selectedSize,
        selectedColor,
        selectedShipping,
        shippingCost
    ) {
        product?.let {
            selectedColor?.let { inStock ->
                listOf(
                    it.copy(
                        quantity = quantity,
                        selectedSize = selectedSize,
                        selectedColor = inStock,
                        selectedShipment = selectedShipping,
                        shipmentCost = shippingCost
                    )
                )
            }
        } ?: emptyList()
    }

    // Set default selections when product loads
    LaunchedEffect(product) {
        product?.let {
            // Set default size
            if (selectedSize.isEmpty() && it.sizes.isNotEmpty()) {
                selectedSize = it.sizes.first()
            }
            // Set default colors
            if (selectedColor.isNullOrEmpty()) {
                selectedColor = "" // Empty means use default
            }
        }
    }

    // Set default shipping to "Free Shipping" if available
    LaunchedEffect(product, uiState.shipmentItem) {
        if (selectedShipping.isEmpty() && uiState.shipmentItem.isNotEmpty()) {
            val freeShipping = uiState.shipmentItem.find {
                it.name.contains("Free", ignoreCase = true)
            }
            selectedShipping = freeShipping?.name ?: uiState.shipmentItem.first().name
        }
    }

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    // Main screen layout with scaffold container
    CustomScaffoldContainer(
        onNavigateBack = { onBackNavigation() },
        title = R.string.product_details_title,
        bottomBarContent = {

            PaddedSection(
                alignment = Alignment.CenterHorizontally,
                content = {
                    CustomBottomSection(
                        total = totalPrice,
                        onClick = {
                            if (productsForPayment.isNotEmpty()) {
                                val amountInCents = totalPrice.toStripeCents
                                onNavigateToPayment(
                                    amountInCents,
                                    productsForPayment,
                                    customerEmail,
                                    customerName,
                                    isPrimeMember,
                                    primeDiscount
                                )
                            }
                        },
                        actionLabel = R.string.buy_now,
                    )
                }
            )

        },
        content = {
            if (!networkState.hasInternet) {
                CustomSpacer()

                PaddedSection(
                    alignment = Alignment.CenterHorizontally,
                    content = {
                        NetworkStatusBanner(
                            networkState = networkState,
                        )
                    }
                )

                CustomSpacer()
            }

            currentSnackBarData?.let { snackBarData ->
                PaddedSection(
                    alignment = Alignment.CenterHorizontally,
                    content = {
                        FloatingCustomSnackBar(
                            snackBarData = snackBarData,
                            visible = showSnackBar,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(top = windowSizeConstant.baseSize),
                            onDismiss = {
                                showSnackBar = false
                                currentSnackBarData = null
                            }
                        )
                    }
                )
            }

            if (showRatingDialog && selectedProductForRating != null) {
                ProductRatingDialog(
                    productName = selectedProductForRating!!.productName,
                    currentUserRating = currentUserReview?.rating ?: 0f,
                    currentReviewText = currentUserReview?.review ?: "",
                    onDismiss = {
                        showRatingDialog = false
                        selectedProductForRating = null
                        viewModel.clearCurrentUserReview()
                    },
                    onSubmitRating = { rating, review ->
                        viewModel.submitProductRating(
                            selectedProductForRating!!.id,
                            rating,
                            review
                        )
                        showRatingDialog = false
                        selectedProductForRating = null
                    }
                )
            }

            if (uiState.isLoadingDetails) {
                PaddedSection(
                    content = {
                        ProductDescriptionShimmer()
                    }
                )
            } else if (uiState.error != null) {
                // Error state
                CustomEmptyState(
                    btnLabel = R.string.retry,
                    titleStr = uiState.error ?: "Error loading products",
                    onBtnClick = { viewModel.refreshProducts() },
                    leadingIcon = Icons.Filled.Error,
                )

            } else {
                CustomLazyColumn {
                    item {
                        PaddedSection(
                            alignment = Alignment.CenterHorizontally,
                            content = {
                                // Main product image card with discount badge
                                Card(
                                    modifier = windowSizeConstant.productImageSize.then(
                                        Modifier.height(windowSizeConstant.customImageHeight)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = windowSizeConstant.smallVerticalPadding),
                                    shape = CustomShape.mediumShape()
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Product image container with background
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.outlineVariant),
                                            contentAlignment = Alignment.TopStart
                                        ) {
                                            val allImages =
                                                listOfNotNull(product?.imageUrl) + (product?.supportingImageUrls
                                                    ?: emptyList())

                                            CustomImageContainer(
                                                data = cloudinaryHelper.getImageUrl(
                                                    allImages.getOrNull(
                                                        selectedImageIndex
                                                    )
                                                ),
                                                shape = CustomShape.mediumShape(),
                                                contentDescription = "product image",
                                                imageLoader = imageLoader
                                            )
                                        }

                                        // Discount Badge - only show if there's actually a discount
                                        product?.let { prod ->
                                            val hasDiscount =
                                                product.oldPrice > 0 && product.oldPrice > product.price

                                            val discount = if (hasDiscount) {
                                                calculateDiscountedPrice(
                                                    product.price,
                                                    product.oldPrice
                                                )
                                            } else {
                                                0.00
                                            }

                                            if (discount > 0) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .zIndex(1f)
                                                        .offset(
                                                            x = -windowSizeConstant.baseNormalVerticalPadding,
                                                            y = -windowSizeConstant.contentVerticalPadding
                                                        )
                                                        .padding(
                                                            horizontal = windowSizeConstant.normalVerticalPadding,
                                                            vertical = windowSizeConstant.baseVerticalPadding
                                                        )
                                                ) {
                                                    CustomSurfaceContainer(
                                                        color = colors.white,
                                                        textStr = "${discount.toInt()}% OFF",
                                                        contentDescription = "discount",
                                                        textColor = colors.customColor6
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }

                    //  Small image gallery for product image selection
                    item {
                        SmallProductImages(
                            productImages = listOf(
                                cloudinaryHelper.getImageUrl(
                                    allImages.getOrNull(
                                        selectedImageIndex
                                    )
                                )
                            ),
                            selectedIndex = selectedImageIndex,
                            imageLoader = imageLoader,
                            onImageSelected = { index -> selectedImageIndex = index }
                        )
                    }

                    // Product information section
                    item {
                        PaddedSection(
                            content = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
                                )
                                {
                                    // Product Title - main product name
                                    Text(
                                        text = product?.productName ?: "",
                                        style = windowSizeConstant.titleTextStyle,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = true,
                                    )

                                    // Brand & Category
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        product?.category?.let {
                                            if (it.isNotEmpty()) {
                                                CustomAssistChip(
                                                    onClick = { /*DO NOTHING */ },
                                                    label = product.category,
                                                    textStyle = windowSizeConstant.labelTextStyle,
                                                    modifier = Modifier.height(windowSizeConstant.baseSize)
                                                )
                                            } else {
                                                CustomAssistChip(
                                                    onClick = { /*DO NOTHING */ },
                                                    label = stringResource(R.string.status_pending),
                                                    textStyle = windowSizeConstant.labelTextStyle,
                                                    modifier = Modifier.height(windowSizeConstant.baseSize)
                                                )
                                            }
                                        }

                                        CustomSpacer(modifier = Modifier.width(windowSizeConstant.normalVerticalPadding))

                                        product?.let {
                                            if (it.inStock) {
                                                Text(
                                                    text = stringResource(R.string.in_stock),
                                                    style = windowSizeConstant.labelTextStyle,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            } else {
                                                Text(
                                                    text = stringResource(R.string.out_of_stock),
                                                    style = windowSizeConstant.labelTextStyle,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    textDecoration = TextDecoration.LineThrough
                                                )
                                            }
                                        }
                                    }

                                    // Rating and Reviews
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (product?.rating != null) {
                                            CustomSpacer(
                                                modifier = Modifier.height(
                                                    windowSizeConstant.smallVerticalPadding
                                                )
                                            )

                                            ProductRating(
                                                rating = product.rating,
                                                maxRating = 5,
                                                onRatingClick = {
                                                    if (authState.isSignedIn) {
                                                        onViewReviews(
                                                            product
                                                        )
                                                    } else {
                                                        showAuthDialog = true
                                                    }
                                                }
                                            )
                                        } else {
                                            ProductRating(
                                                rating = 0f,
                                                maxRating = 5
                                            )
                                        }

                                        CustomSpacer(modifier = Modifier.width(windowSizeConstant.normalVerticalPadding))

                                        product?.reviewCount?.let {
                                            if (it > 0) {
                                                Text(
                                                    "(${product.reviewCount} reviews)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = colors.green,
                                                    modifier = Modifier.clickable(onClick = {
                                                        if (authState.isSignedIn) {
                                                            onViewReviews(
                                                                product
                                                            )
                                                        } else {
                                                            showAuthDialog = true
                                                        }
                                                    })
                                                )
                                            } else {
                                                CustomSpacer(
                                                    modifier = Modifier.height(
                                                        windowSizeConstant.smallVerticalPadding
                                                    )
                                                )

                                                Text(
                                                    stringResource(R.string.no_reviews),
                                                    style = windowSizeConstant.labelTextStyle,
                                                    textDecoration = TextDecoration.Underline,
                                                    modifier = Modifier.clickable(onClick = {
                                                        if (authState.isSignedIn) {
                                                            onViewReviews(
                                                                product
                                                            )
                                                        } else {
                                                            showAuthDialog = true
                                                        }
                                                    })
                                                )
                                            }
                                        }
                                    }

                                    // Price
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(
                                            windowSizeConstant.smallVerticalPadding
                                        )
                                    ) {
                                        if (product?.price != null) {
                                            Text(
                                                text = formatPrice(product.price),
                                                style = windowSizeConstant.bodyTextStyle,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Text(
                                                text = stringResource(R.string.status_pending),
                                                style = windowSizeConstant.labelTextStyle,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textDecoration = TextDecoration.LineThrough
                                            )
                                        }

                                        //old price
                                        product?.oldPrice?.let {
                                            if (it > 0) {
                                                Text(
                                                    text = formatPrice(product.oldPrice),
                                                    style = windowSizeConstant.labelTextStyle,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    textDecoration = TextDecoration.LineThrough
                                                )
                                            } else {
                                                Text(
                                                    text = stringResource(R.string.status_pending),
                                                    style = windowSizeConstant.labelTextStyle,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    textDecoration = TextDecoration.LineThrough
                                                )
                                            }
                                        }

                                        CustomSpacer(modifier = Modifier.width(windowSizeConstant.smallVerticalPadding))

                                        if (savingsAmount != null)
                                            Text(
                                                text = "You Save $${savingsAmount}",
                                                style = windowSizeConstant.bodyTextStyle,
                                                color = colors.green,
                                                fontWeight = FontWeight.Medium
                                            )
                                        else {
                                            Text(
                                                text = stringResource(R.string.no_savings),
                                                style = windowSizeConstant.bodyTextStyle,
                                                color = colors.green,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }

                    item {
                        // Color Selection
                        CustomLazyRow {
                            //  Prime Badge (shows if ACTIVE member + eligible product)
                            item {
                                if (showPrimeBadge) {
                                    CustomSurfaceContainer(
                                        color = colors.customColor16,
                                        icon = Icons.Filled.Verified,
                                        text = R.string.prime,
                                        contentDescription = "prime eligible"
                                    )
                                }
                            }

                            //  Flash Deal Badge (from tags)
                            item {
                                if (isFlashDeal == true) {
                                    CustomSurfaceContainer(
                                        color = colors.customColor6,
                                        icon = Icons.Filled.FlashOn,
                                        text = R.string.flash,
                                        contentDescription = "flash deal"
                                    )
                                }
                            }

                            // Best Seller Badge (from tags)
                            item {
                                if (isBestSeller == true) {
                                    CustomSurfaceContainer(
                                        color = colors.customColor5,
                                        icon = Icons.Filled.VerifiedUser,
                                        text = R.string.best_seller,
                                        contentDescription = "best seller"
                                    )
                                }
                            }

                            //  Arrival Badge (from tags)
                            item {
                                if (isNewArrival == true) {
                                    CustomSurfaceContainer(
                                        color = colors.customColor5,
                                        icon = Icons.Filled.NewReleases,
                                        text = R.string.new_arrival,
                                        contentDescription = "new arrival"
                                    )
                                }
                            }
                            //   Limited Edition Badge (from tags)
                            item {
                                if (isLimitedEdition == true) {
                                    CustomSurfaceContainer(
                                        color = colors.customColor2,
                                        icon = Icons.Filled.Star,
                                        text = R.string.limited_edition,
                                        contentDescription = "limited edition"
                                    )
                                }
                            }

                            // Eco Friendly Badge (from tags)
                            item {
                                if (isEcoFriendly == true) {
                                    CustomSurfaceContainer(
                                        color = colors.customColor17,
                                        icon = Icons.Filled.Eco,
                                        text = R.string.eco_friendly,
                                        contentDescription = "eco friendly"
                                    )
                                }
                            }
                        }
                    }

                    item {
                        PaddedSection(
                            content = {
                                ProductColorSelection(
                                    selectedColor = selectedColor,
                                    onColorSelected = { color ->
                                        selectedColor = color
                                    },
                                    defaultColors = defaultColor,
                                    multiSelect = false,
                                )
                            })
                    }

                    // Size Selection
                    item {
                        if (uiState.isLoadingSizes) {
                            // Show loading state for sizes options
                            PaddedSection(
                                content = {
                                    CustomItemCardShimmer()
                                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                                })
                        } else if (product?.sizes?.isNotEmpty() == true) {
                            PaddedSection(
                                content = {
                                    Text(
                                        text = "Size: $selectedSize",
                                        style = windowSizeConstant.bodyTextStyle,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            )

                            CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                            CustomLazyRow {
                                items(product.sizes.size) { index ->
                                    val size = product.sizes[index]

                                    CustomOutlinedButton(
                                        onClick = { selectedSize = size },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (selectedSize == size)
                                                MaterialTheme.colorScheme.primary else Color.Transparent,
                                            contentColor = if (selectedSize == size)
                                                colors.white else MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.width(windowSizeConstant.customSpacerMedium),
                                        labelStr = size
                                    )
                                }
                            }
                        } else {
                            // No sizes available for this product
                            PaddedSection(
                                content = {
                                    Text(
                                        text = stringResource(R.string.no_size_options),
                                        style = windowSizeConstant.bodyTextStyle,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                })
                        }

                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))
                    }

                    // Shipping Options
                    // headline  widget
                    item {
                        PaddedSection(
                            content = {
                                HeadlineWidget(
                                    leadingText = R.string.shipping,
                                    trailing = {
                                        CustomIcon(
                                            icon = Icons.Filled.DeliveryDining,
                                            contentDescription = "Delivery dining"
                                        )
                                    }
                                )
                            }
                        )

                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                        // Display shipment options
                        if (uiState.isLoadingShipment) {
                            // Show loading state for shipment options
                            PaddedSection(
                                content = {
                                    CustomItemCardShimmer()
                                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                                })
                        } else if (uiState.shipmentItem.isNotEmpty()) {
                            PaddedSection(
                                content = {
                                    uiState.shipmentItem.forEach { option ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedShipping = option.name
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (selectedShipping == option.name)
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                else Color.Gray.copy(alpha = 0.05f)
                                            ),
                                            border = if (selectedShipping == option.name)
                                                BorderStroke(
                                                    customSpacing.customHalf,
                                                    MaterialTheme.colorScheme.primary
                                                )
                                            else null
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(windowSizeConstant.basePadding),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = option.name,
                                                        fontWeight = FontWeight.Medium,
                                                        style = windowSizeConstant.bodyTextStyle
                                                    )

                                                    Text(
                                                        text = option.deliveryMethod,
                                                        style = windowSizeConstant.labelTextStyle,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Text(
                                                    text = "$${option.price}",
                                                    style = windowSizeConstant.bodyTextStyle,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseNormalVerticalPadding))

                                    }
                                })
                        } else {
                            // No shipment options available
                            PaddedSection(
                                content = {
                                    Text(
                                        text = stringResource(R.string.no_shipping_option),
                                        style = windowSizeConstant.bodyTextStyle,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                })
                        }

                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                    }

                    // Product Specifications
                    item {
                        PaddedSection(
                            content = {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.2f
                                        )
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(windowSizeConstant.basePadding)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stringResource(R.string.product_specifications),
                                                style = windowSizeConstant.titleTextStyle,
                                            )

                                            ButtonIconComposable(
                                                showBgColor = false,
                                                buttonIcon = ButtonIcon.Vector(
                                                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore
                                                ),
                                                onClick = {
                                                    isExpanded = !isExpanded
                                                },
                                                tint = if (isPrimeMember) colors.customColor16 else MaterialTheme.colorScheme.onSurfaceVariant,
                                                contentDescription = if (isExpanded) "Collapse" else "Expand"
                                            )
                                        }

                                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                                        // Collapsed state (limited lines)
                                        if (!isExpanded) {
                                            Text(
                                                text = product?.description ?: "",
                                                style = windowSizeConstant.bodyTextStyle,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Start,
                                                maxLines = 4,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Expanded state (all text)
                                        AnimatedVisibility(
                                            visible = isExpanded,
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut() + shrinkVertically()
                                        ) {
                                            Text(
                                                text = product?.description ?: "",
                                                style = windowSizeConstant.bodyTextStyle,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Start
                                            )
                                        }

                                        // Show Read More/Show Less link
                                        Text(
                                            text = if (isExpanded) "Show Less" else "Read More",
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .padding(top = windowSizeConstant.normalVerticalPadding)
                                                .clickable { isExpanded = !isExpanded }
                                        )
                                    }
                                }
                            }
                        )

                        CustomSpacer()

                        // Quantity and Add to Cart
                        CustomLazyRow {
                            // Quantity Selector
                            item {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.qty),
                                        style = windowSizeConstant.titleTextStyle,
                                        fontWeight = FontWeight.Medium
                                    )

                                    CustomSpacer(modifier = Modifier.width(windowSizeConstant.baseVerticalPadding))

                                    QuantitySelector(
                                        quantity = quantity, // Use the local state, not product.quantity
                                        onQuantityChange = { newQuantity ->
                                            // Update local state
                                            quantity = newQuantity

                                            // Also update in ViewModel if needed
                                            viewModel.updateProductQuantity(
                                                newQuantity
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseVerticalPadding))

                        // Action Button
                        PaddedSection(
                            content = {
                                val isFavorite by favoriteViewModel.getFavoriteStatus(
                                    product?.id ?: ""
                                )
                                    .collectAsState(initial = false)

                                val isInCart by cartViewModel.getCartStatus(product?.id ?: "")
                                    .collectAsState(initial = false)

                                product?.let { item ->
                                    ButtonsContent(
                                        item,
                                        isInCart,
                                        isFavorite,
                                        cartViewModel,
                                        favoriteViewModel,
                                        onSignInClick = onSignInClick
                                    )
                                }
                            }
                        )
                    }

                    item {
                        PaddedSection(
                            content = {
                                CustomHorizontalDivider()
                            }
                        )

                        // Related Products Section
                        product?.let { currentProduct ->
                            RelatedProductsSection(
                                categoryName = currentProduct.category,
                                currentProductId = currentProduct.id,
                                onProductClick = {
                                    onRelatedProductClick(it)
                                },
                                maxItems = 8, // Show up to 8 related products
                                onSignInClick = onSignInClick
                            )
                        }
                    }

                    item {
                        // Order Summary Card
                        PaddedSection(
                            content = {
                                PrimeSummaryCard(
                                    isPrimeMember = isPrimeMember,
                                    baseTotal = baseTotal,
                                    primeDiscount = primeDiscount,
                                    shippingCost = if (isPrimeMember) 0.0 else shippingCost
                                )
                            }
                        )
                    }
                }
            }
        },
    )

    if (showAuthDialog) {
        SignInRequiredDialog(
            onDismiss = { showAuthDialog = false },
            onSignInClick = {
                showAuthDialog = false
                onSignInClick()
            }
        )
    }
}

@Composable
private fun PrimeSummaryCard(
    isPrimeMember: Boolean,
    baseTotal: Double,
    primeDiscount: Double,
    shippingCost: Double
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = customSpacing.custom8),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimeMember)
                colors.customColor16.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeConstant.normalVerticalPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.order_summary),
                    style = windowSizeConstant.labelTextStyle,
                    fontWeight = FontWeight.SemiBold
                )

                //  Prime badge in summary
                if (isPrimeMember) {
                    CustomSurfaceContainer(
                        color = colors.customColor16,
                        icon = Icons.Filled.Verified,
                        text = R.string.prime,
                        contentDescription = "prime"
                    )
                }
            }

            CustomSpacer(modifier = Modifier.height(windowSizeConstant.smallVerticalPadding))

            // Subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.sub_total),
                    style = windowSizeConstant.labelTextStyle,
                )

                Text(
                    formatPrice(baseTotal),
                    style = windowSizeConstant.labelTextStyle,
                )
            }

            //   Prime discount
            if (primeDiscount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.prime_discount),
                        style = windowSizeConstant.labelTextStyle,
                        color = colors.customColor16,
                    )

                    Text(
                        "-${formatPrice(primeDiscount)}",
                        style = windowSizeConstant.labelTextStyle,
                        color = colors.customColor16,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Shipping
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.shipping),
                    style = windowSizeConstant.labelTextStyle,
                )
                if (isPrimeMember) {
                    Text(
                        stringResource(R.string.free),
                        style = windowSizeConstant.labelTextStyle,
                        color = colors.customColor16,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        formatPrice(shippingCost),
                        style = windowSizeConstant.labelTextStyle,
                    )
                }
            }
        }
    }
}

@Composable
private fun ButtonsContent(
    product: ProductItem,
    isInCart: Boolean,
    isFavorite: Boolean,
    cartViewModel: CartViewModel,
    favoriteViewModel: FavoriteViewModel,
    authViewModel: AuthViewModel = hiltViewModel(),
    onSignInClick: () -> Unit
) {

    val windowSizeConstants = LocalWindowSizeConstant.current
    val authState by authViewModel.authState.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }

    when {
        // Not in cart AND not in favorites → show both
        !isInCart && !isFavorite -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(windowSizeConstants.basePadding),
                verticalAlignment = Alignment.CenterVertically
            ) {// Consistent spacing between items){
                CustomElevatedButton(
                    width = windowSizeConstants.customButtonPadding,
                    label = R.string.add_to_cart,
                    onClick = {
                        if (authState.isSignedIn) {
                            cartViewModel.addToCart(product)
                        } else {
                            showAuthDialog = true
                        }
                    }
                )

                CustomElevatedButton(
                    width = windowSizeConstants.customButtonPadding,
                    label = R.string.add_to_favorite,
                    onClick = {
                        if (authState.isSignedIn) {
                            favoriteViewModel.toggleFavorite(product)
                        } else {
                            showAuthDialog = true
                        }
                    }
                )
            }
        }

//        // In cart only → show favorite button
        isInCart && !isFavorite -> {
            CustomElevatedButton(
                width = windowSizeConstants.customButtonPadding,
                label = R.string.add_to_favorite,
                onClick = {
                    if (authState.isSignedIn) {
                        favoriteViewModel.toggleFavorite(product)
                    } else {
                        showAuthDialog = true
                    }
                }
            )
        }

        // In favorites only → show cart button
        !isInCart && isFavorite -> {
            CustomElevatedButton(
                width = windowSizeConstants.customButtonPadding,
                label = R.string.add_to_cart,
                onClick = {
                    if (authState.isSignedIn) {
                        cartViewModel.addToCart(product)
                    } else {
                        showAuthDialog = true
                    }
                }
            )
        }

        // In both → show nothing
        else -> Unit
    }

    if (showAuthDialog) {
        SignInRequiredDialog(
            onDismiss = { showAuthDialog = false },
            onSignInClick = {
                showAuthDialog = false
                // navigate to sign-in screen here
                onSignInClick()
            }
        )
    }
}