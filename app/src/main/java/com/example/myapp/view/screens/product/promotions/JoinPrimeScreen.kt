package com.example.myapp.view.screens.product.promotions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.MembershipType
import com.example.myapp.data.dataclass.Offer
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.PrimeMembershipViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomTextField
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.HeroCard
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.TermsOfServiceAndUse
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.components.isValidCardNumber
import com.example.myapp.view.components.isValidCvv
import com.example.myapp.view.components.isValidExpiryDate
import com.example.myapp.view.utils.CustomShape
import com.example.myapp.view.utils.formatDate
import kotlinx.coroutines.delay

/**
 * JoinPrimeScreen - Subscription signup screen for Prime membership.
 *
 * Allows users to subscribe to the Prime membership program.
 * Features:
 * - Plan selection (Monthly vs Annual).
 * - Detailed list of Prime benefits.
 * - Secure payment form with validation for:
 *   - Name
 *   - Email
 *   - Card Number (Luhn check simulation)
 *   - Expiry Date
 *   - CVV
 * - Terms and conditions agreement.
 * - Success dialog upon successful subscription.
 *
 * @param onBackNavigation Callback to navigate back.
 * @param onJoinSuccess Callback invoked when subscription is successful.
 * @param viewModel [PrimeMembershipViewModel] for handling subscription logic.
 * @param networkManager Manager for network connectivity.
 */
@Composable
fun JoinPrimeScreen(
    onBackNavigation: () -> Unit,
    onJoinSuccess: () -> Unit = {},
    viewModel: PrimeMembershipViewModel = hiltViewModel(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val networkState = rememberNetworkState(networkManager)

    // Observe membership state
    val membershipState by viewModel.membershipState.collectAsState()
    val windowSizeConstant = LocalWindowSizeConstant.current

    // Form State
    var selectedPlan by remember { mutableStateOf("monthly") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }

    // Validation Errors
    var emailError by remember { mutableStateOf("") }
    var cardNumberError by remember { mutableStateOf("") }
    var expiryDateError by remember { mutableStateOf("") }
    var cvvError by remember { mutableStateOf("") }
    var termsError by remember { mutableStateOf("") }

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Handle snack bar messages from ViewModel
    LaunchedEffect(Unit) {
        viewModel.snackBarData.collect { snackBarData ->
            currentSnackBarData = snackBarData
            showSnackBar = true

            if (snackBarData.duration != SnackbarDuration.Indefinite) {
                delay(
                    when (snackBarData.duration) {
                        SnackbarDuration.Short -> 3000L
                        SnackbarDuration.Long -> 5000L
                        else -> 3000L
                    }
                )
                showSnackBar = false
            }
        }
    }

    // Show success dialog when membership is created
    LaunchedEffect(membershipState.isPrimeMember) {
        if (membershipState.isPrimeMember && !membershipState.isLoading) {
            showSuccessDialog = true
        }
    }

    // Validate Form and Subscribe
    /**
     * validateAndSubscribe - Validates form input and initiates subscription.
     *
     * Performs client-side validation on all payment fields:
     * - Card number is 16 digits.
     * - Expiry date follows MM/YY format.
     * - CVV is 3 digits.
     * - Terms are agreed to.
     *
     * If validation passes, calls [PrimeMembershipViewModel.createMembership].
     * Otherwise, displays appropriate error messages via Snack bar.
     */


    /**
     * validate expiry date
     */
    fun validateExpiryDate(): Boolean {
        return when {
            expiryDate.isEmpty() -> {
                expiryDateError = "Expiry date is required"
                false
            }

            !isValidExpiryDate(expiryDate) -> {
                expiryDateError = "Please enter a valid expiryDate"
                false
            }

            else -> {
                expiryDateError = ""
                true
            }
        }
    }

    /**
     * validate cvv
     */
    fun validateCVV(): Boolean {
        return when {
            cvv.isEmpty() -> {
                cvvError = "CVV date is required"
                false
            }

            !isValidCvv(cvv) -> {
                cvvError = "Please enter a valid cvv"
                false
            }

            else -> {
                cvvError = ""
                true
            }
        }
    }

    /**
     * validate card number
     */
    fun validateCardNumber(): Boolean {
        return when {
            cardNumber.isEmpty() -> {
                cardNumberError = "Card Number date is required"
                false
            }

            !isValidCardNumber(cardNumber) -> {
                cardNumberError = "Please enter a valid cardd number"
                false
            }

            else -> {
                cardNumberError = ""
                true
            }
        }
    }

    /**
     * validateTerms
     */
    fun validateTerms(): Boolean {
        return if (!agreedToTerms) {
            termsError = "You must agree to the terms and conditions"
            false
        } else {
            termsError = ""
            true
        }
    }

    /**
     * validateForm
     */
    fun validateForm(): Boolean {
        val isCardNumberValid = validateCardNumber()
        val isCVVValid = validateCVV()
        val isExpiryDate = validateExpiryDate()
        val isTermsValid = validateTerms()

        return isCVVValid && isCardNumberValid && isExpiryDate && isTermsValid
    }

    val essentialCard = Offer(
        id = "3",
        title = "Join Prime",
        description = "Unlock exclusive benefits and savings",
        buttonText = "Join Prime",
        gradient = listOf(colors.customColor11, colors.customColor12),
        leadingIcon = Icons.Filled.Stars
    )

    val benefits = listOf(
        Triple(
            Icons.Filled.LocalShipping,
            "Free Shipping",
            "Free delivery on all orders"
        ),
        Triple(
            Icons.Filled.Percent,
            "Exclusive Deals",
            "Extra 20% off on select items"
        ),
        Triple(
            Icons.Filled.StarRate,
            "Early Access",
            "Shop deals before everyone else"
        ),
        Triple(
            Icons.Filled.CardGiftcard,
            "Special Rewards",
            "Earn 2x points on every purchase"
        )
    )

    CustomScaffoldContainer(
        onNavigateBack = onBackNavigation,
        snackBarHostState = snackBarHostState,
        title = R.string.join_prime,
        showBottomBar = false,
        verticalArrangement = Arrangement.Top,
        content = {
            // Network Status Banner (stays the same)
            if (!networkState.hasInternet) {
                CustomSpacer()
                NetworkIndicator(networkState = networkState)
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

            // SnackBar (stays the same)
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

            CustomLazyColumn {
                if (membershipState.isLoading) {
                    // Show shimmer items when loading
                    item {
                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseSize))
                        PaddedSection(
                            content = {
                                CustomListCardShimmer()
                            }
                        )
                    }
                } else {
                    // Show actual content when not loading
                    item {
                        CustomSpacer()

                        PaddedSection(
                            alignment = Alignment.CenterHorizontally,
                            content = {
                                HeroCard(
                                    offer = essentialCard
                                )
                            }
                        )
                    }

                    // Benefits Section
                    item {
                        PaddedSection(
                            content = {
                                HeadlineWidget(
                                    leadingText = R.string.prime_benefits
                                )
                            }
                        )
                    }

                    items(count = benefits.size) { index ->
                        val benefit = benefits[index]
                        PaddedSection(content = {
                            BenefitItem(benefit.first, benefit.second, benefit.third)
                        }
                        )
                    }

                    item {
                        PaddedSection(
                            content = {
                                // Plan Selection
                                HeadlineWidget(
                                    leadingText = R.string.choose_plan
                                )

                                CustomSpacer()

                                PlanCard(
                                    title = "Monthly",
                                    price = "$9.99/month",
                                    description = "Billed monthly",
                                    selected = selectedPlan == "monthly",
                                    onClick = { selectedPlan = "monthly" }
                                )

                                CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseNormalVerticalPadding))

                                PlanCard(
                                    title = "Annual",
                                    price = "$99/year",
                                    description = "Save 17% • Best value",
                                    selected = selectedPlan == "annual",
                                    onClick = { selectedPlan = "annual" },
                                    badge = "SAVE 17%"
                                )
                            }
                        )
                    }

                    // Payment Information Section
                    item {
                        PaddedSection(
                            content = {
                                // Payment Information
                                HeadlineWidget(
                                    leadingText = R.string.payment_information
                                )

                                CustomSpacer()

                                // Card Number with validation
                                CustomTextField(
                                    label = R.string.card_number,
                                    placeholder = R.string.card_number_placeholder,
                                    value = cardNumber,
                                    onValueChange = {
                                        if (it.length <= 16) {
                                            cardNumber = it.filter { char -> char.isDigit() }
                                        }

                                        if (cardNumberError.isNotEmpty()) validateCardNumber()
                                    },
                                    icon = Icons.Filled.CreditCard,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = cardNumberError.isNotEmpty(),
                                    errorMessage = if (cardNumberError.isNotEmpty()) "16-digit card number required" else "",
                                )

                                CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseNormalVerticalPadding))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.normalVerticalPadding)
                                ) {
                                    // Expiry Date with validation
                                    Box(modifier = Modifier.weight(1f)) {
                                        CustomTextField(
                                            label = R.string.expiry_date,
                                            placeholder = R.string.expiry_placeholder,
                                            value = expiryDate,
                                            onValueChange = {
                                                if (it.length <= 5) {
                                                    // Auto-format MM/YY
                                                    val filtered =
                                                        it.filter { char -> char.isDigit() || char == '/' }
                                                    expiryDate =
                                                        if (filtered.length == 2 && !filtered.contains(
                                                                "/"
                                                            )
                                                        ) {
                                                            "$filtered/"
                                                        } else {
                                                            filtered
                                                        }
                                                    expiryDateError = it
                                                }

                                                if (emailError.isNotEmpty()) validateExpiryDate()

                                            },
                                            icon = Icons.Filled.CalendarMonth,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            isError = expiryDateError.isNotEmpty(),
                                            errorMessage = if (expiryDateError.isNotEmpty()) "MM/YY format" else "",
                                        )
                                    }

                                    // CVV with validation
                                    Box(modifier = Modifier.weight(1f)) {
                                        CustomTextField(
                                            label = R.string.cvv,
                                            placeholder = R.string.cvv_placeholder,
                                            value = cvv,
                                            onValueChange = {
                                                if (it.length <= 3) {
                                                    cvv = it.filter { char -> char.isDigit() }
                                                    cvvError = it
                                                }

                                                if (cvv.isNotEmpty()) validateCVV()

                                            },
                                            icon = Icons.Filled.Lock,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                            isPassword = true,
                                            isError = cvvError.isNotEmpty(),
                                            errorMessage = if (cvvError.isNotEmpty()) "3 digits" else "",
                                        )
                                    }
                                }

                                CustomSpacer()

                                // Terms of service agreement
                                PaddedSection(
                                    alignment = Alignment.CenterHorizontally,
                                    content = {
                                        TermsOfServiceAndUse(
                                            isChecked = agreedToTerms,
                                            onCheckedChange = {
                                                agreedToTerms = it
                                                if (termsError.isNotEmpty()) validateTerms()
                                            },
                                            termsUrl = "https://example.com/terms-of-service",
                                            privacyUrl = "https://example.com/privacy-policy",
                                            errorMessage = termsError,
                                            isError = termsError.isNotEmpty(),
                                        )
                                    }
                                )
                            }
                        )
                    }

                    // Join Button
                    item {
                        PaddedSection(
                            alignment = Alignment.CenterHorizontally,
                            content = {
                                CustomButton(
                                    label = R.string.join_prime,
                                    onClick = {
                                        if (validateForm()) {
                                            val membershipType = if (selectedPlan == "monthly")
                                                MembershipType.MONTHLY
                                            else
                                                MembershipType.ANNUAL

                                            viewModel.createMembership(
                                                membershipType = membershipType,
                                                paymentMethod = "card_ending_${cardNumber.takeLast(4)}"
                                            )
                                        }
                                    }
                                )

                                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                                Text(
                                    stringResource(R.string.money_back_guarantee),
                                    style = windowSizeConstant.bodyTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        )
                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerSmall))
                    }
                }
            }
        }
    )

    // ✅ Success Dialog
    if (showSuccessDialog) {
        CustomAlertDialog(
            onDismissRequest = {},
            icon = {
                CustomIcon(
                    icon = Icons.Filled.CheckCircle,
                    contentDescription = "Check circle",
                    tint = colors.customColor5,
                    iconSize = windowSizeConstant.largeIconSize
                )
            },
            title = {
                Text(
                    stringResource(R.string.welcome_to_prime),
                    style = windowSizeConstant.titleTextStyle
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.membership_active),
                        style = windowSizeConstant.bodyTextStyle,
                        textAlign = TextAlign.Center
                    )

                    CustomSpacer()

                    // Show membership details
                    membershipState.membership?.let { membership ->
                        Text(
                            "Membership Type: ${membership.membershipType.displayName}",
                            style = windowSizeConstant.bodyTextStyle,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            "Renewal Date: ${formatDate(membership.endDate)}",
                            style = windowSizeConstant.labelTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                CustomTextButton(
                    onClick = {
                        onJoinSuccess()
                        showSuccessDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = R.string.start_shopping
                )
            }
        )
    }
}

/**
 * BenefitItem - Displays a single benefit with an icon and description.
 *
 * @param icon The icon vector representing the benefit.
 * @param title The title of the benefit.
 * @param description A brief description of the benefit.
 */
@Composable
fun BenefitItem(icon: ImageVector, title: String, description: String) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CustomShape.extraLargeShape(),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(customSpacing.custom48)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CustomIcon(
                    icon = icon,
                    contentDescription = "Benefit item",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        CustomSpacer(modifier = Modifier.width(windowSizeConstant.baseNormalVerticalPadding))

        Column(
            verticalArrangement = Arrangement.spacedBy(windowSizeConstant.baseVerticalPadding)
        ) {
            Text(
                text = title,
                style = windowSizeConstant.bodyTextStyle
            )

            Text(
                description,
                style = windowSizeConstant.labelTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * PlanCard - A card displaying a Prime membership plan option.
 *
 * Designed to be selectable, showing price, description, and an optional badge.
 *
 * @param title The plan title (e.g., "Monthly").
 * @param price The formatted price string.
 * @param description Additional details about the plan.
 * @param selected Whether this plan is currently selected.
 * @param onClick Callback when the card is clicked.
 * @param badge Optional text badge (e.g., "SAVE 17%").
 */
@Composable
fun PlanCard(
    title: String,
    price: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    badge: String? = null
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Card(
        modifier = windowSizeConstant.adaptiveWidthModifier.then(
            Modifier
                .clickable(onClick = onClick)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (selected) windowSizeConstant.borderSize else windowSizeConstant.smallSizes,
            color = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline
        ),
        shape = CustomShape.mediumShape()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeConstant.basePadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selected,
                    onClick = onClick
                )

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseNormalVerticalPadding))

                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.baseVerticalPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = windowSizeConstant.titleTextStyle
                        )

                        if (badge != null) {
                            CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                            Surface(
                                shape = CustomShape.mediumShape(),
                                color = colors.customColor5
                            ) {
                                Text(
                                    text = badge,
                                    style = windowSizeConstant.labelTextStyle,
                                    color = colors.white,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        horizontal = windowSizeConstant.normalVerticalPadding,
                                        vertical = windowSizeConstant.smallVerticalPadding
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        description,
                        style = windowSizeConstant.labelTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                price,
                style = windowSizeConstant.bodyTextStyle.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (selected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}