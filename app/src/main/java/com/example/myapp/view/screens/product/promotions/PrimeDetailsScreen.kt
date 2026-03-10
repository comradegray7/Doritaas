package com.example.myapp.view.screens.product.promotions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.BenefitType
import com.example.myapp.data.dataclass.BenefitUsage
import com.example.myapp.data.dataclass.MembershipStatus
import com.example.myapp.data.dataclass.PrimeMembership
import com.example.myapp.data.dataclass.PrimeTransaction
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.dataclass.TransactionStatus
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.PrimeMembershipViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomHorizontalDivider
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomSurfaceContainer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.CustomShape
import com.example.myapp.view.utils.formatDate
import com.example.myapp.view.utils.formatPrice
import com.example.myapp.view.utils.primeUtils.getMembershipStatusColor
import kotlinx.coroutines.delay

/**
 * PrimeDetailsScreen - Dashboard for managing an active Prime membership.
 *
 * Displays:
 * - Current membership status (Active, Expired) and validity dates.
 * - Savings and benefits usage statistics.
 * - Transaction history for membership payments.
 * - Options to cancel membership.
 *
 * @param onBackNavigation Callback to navigate back.
 * @param viewModel [PrimeMembershipViewModel] for membership data.
 * @param onJoinPrimeClick Callback to navigate to join screen if not a member.
 * @param networkManager Manager for network connectivity.
 */
@Composable
fun PrimeDetailsScreen(
    onBackNavigation: () -> Unit,
    viewModel: PrimeMembershipViewModel = hiltViewModel(),
    onJoinPrimeClick: () -> Unit = {},
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val membershipState by viewModel.membershipState.collectAsState()

    var showCancelDialog by remember { mutableStateOf(false) }
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }
    val windowSizeConstant = LocalWindowSizeConstant.current
    val networkState = rememberNetworkState(networkManager)

    LaunchedEffect(Unit) {
        viewModel.loadUserMembership()
        viewModel.loadTransactionHistory()
        viewModel.loadBenefitUsageStats()
    }

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

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
                viewModel.loadUserMembership()
            } else {
                currentSnackBarData = SnackBarData(
                    message = "Cannot refresh - No internet connection",
                    isError = true,
                    duration = SnackbarDuration.Short
                )
                showSnackBar = true
            }
        },
        onNavigateBack = onBackNavigation,
        snackBarHostState = snackBarHostState,
        title = R.string.prime_membership,
        showBottomBar = false,
        verticalArrangement = Arrangement.Top,
        content = {
            // Network Indicator in top bar
            if (!networkState.hasInternet) {
                // Network Indicator in top bar
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

            if (membershipState.isLoading) {
                PaddedSection(content = {

                    CustomListCardShimmer()

                })
            } else if (membershipState.membership != null) {

                val membership = membershipState.membership!!

                CustomLazyColumn {
                    item {
                        CustomSpacer()
                        PaddedSection(content = {
                            MembershipStatusCard(
                                membership = membership,
                                onCancelClick = { showCancelDialog = true }
                            )
                        })
                    }

                    item {
                        PaddedSection(content = {
                            BenefitsUsageCard(
                                totalSavings = viewModel.getTotalSavings(),
                                benefitUsage = membershipState.benefitUsage
                            )
                        })
                    }

                    item {
                        PaddedSection(content = {
                            CustomSpacer()
                            Text(
                                stringResource(R.string.transaction_history),
                                style = windowSizeConstant.titleTextStyle,
                                fontWeight = FontWeight.Bold
                            )
                        })
                    }

                    item {
                        if (membershipState.transactionHistory.isEmpty()) {
                            PaddedSection(content = {
                                CustomEmptyState(
                                    showBtn = false,
                                    title = R.string.no_transactions,
                                    leadingIcon = Icons.Filled.History
                                )
                            })
                        } else {
                            membershipState.transactionHistory.forEach { transaction ->
                                PaddedSection(content = {
                                    TransactionItem(transaction)
                                })
                                CustomSpacer()
                            }
                        }
                    }
                }

            } else {
                // Not a Prime member
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(windowSizeConstant.contentVerticalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CustomIcon(
                        icon = Icons.Filled.Stars,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        iconSize = windowSizeConstant.largeIconSize
                    )

                    CustomSpacer()

                    Text(
                        text = stringResource(R.string.not_prime_member),
                        style = windowSizeConstant.titleTextStyle,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                    Text(
                        text = stringResource(R.string.join_to_unlock),
                        style = windowSizeConstant.bodyTextStyle,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    CustomButton(
                        label = R.string.join_prime,
                        onClick = { onJoinPrimeClick() },
                    )
                }
            }
        }
    )

    // Cancel Membership Dialog
    if (showCancelDialog) {
        CustomAlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = {
                CustomIcon(
                    icon = Icons.Filled.Warning,
                    contentDescription = "Warning",
                    tint = colors.orange,
                    iconSize = windowSizeConstant.largeIconSize
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.cancel_membership_warning),
                    style = windowSizeConstant.titleTextStyle,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.cancel_membership_message),
                    style = windowSizeConstant.bodyTextStyle,
                )
            },
            confirmButton = {
                CustomTextButton(
                    label = R.string.cancel_membership,
                    onClick = {
                        viewModel.cancelMembership()
                        showCancelDialog = false
                    },
                    color = MaterialTheme.colorScheme.error
                )

            },
            dismissButton = {
                CustomTextButton(
                    label = R.string.keep_membership,
                    onClick = { showCancelDialog = false })
            }
        )
    }
}

/**
 * MembershipStatusCard - Card displaying key membership details.
 *
 * Shows status (Active/Expired), renewal date, payment method, and auto-renew status.
 * Provides an option to cancel the membership if active.
 *
 * @param membership The [PrimeMembership] object containing details.
 * @param onCancelClick Callback to initiate cancellation flow.
 */
@Composable
fun MembershipStatusCard(
    membership: PrimeMembership,
    onCancelClick: () -> Unit
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = getMembershipStatusColor(status = membership.status)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeConstant.baseSize)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomIcon(
                        icon = Icons.Filled.Stars,
                        contentDescription = "Stars",
                        tint = colors.customColor9,
                        iconSize = windowSizeConstant.largeIconSize
                    )

                    CustomSpacer(modifier = Modifier.width(windowSizeConstant.baseNormalVerticalPadding))

                    Column {
                        Text(
                            membership.membershipType.displayName,
                            style = windowSizeConstant.titleTextStyle
                        )

                        CustomSpacer(modifier = Modifier.width(windowSizeConstant.baseNormalVerticalPadding))

                        Text(
                            text = membership.status.name,
                            style = windowSizeConstant.bodyTextStyle.copy(
                                color = when (membership.status) {
                                    MembershipStatus.ACTIVE -> colors.customColor5
                                    MembershipStatus.EXPIRED -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        )
                    }
                }

                if (membership.status == MembershipStatus.ACTIVE) {
                    Surface(
                        color = colors.customColor5,
                        shape = CustomShape.extraLargeShape()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(windowSizeConstant.baseNormalVerticalPadding)
                        )
                    }
                }
            }

            CustomHorizontalDivider()

            // Membership details
            MembershipDetailRow(
                icon = Icons.Filled.CalendarMonth,
                label = "Member Since",
                value = formatDate(membership.startDate)
            )

            CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

            MembershipDetailRow(
                icon = Icons.Filled.Update,
                label = "Renewal Date",
                value = formatDate(membership.endDate)
            )

            CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

            MembershipDetailRow(
                icon = Icons.Filled.CreditCard,
                label = "Payment Method",
                value = membership.paymentMethod
            )

            CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

            MembershipDetailRow(
                icon = Icons.Filled.Autorenew,
                label = "Auto-Renew",
                value = if (membership.autoRenew) "Enabled" else "Disabled"
            )

            if (membership.status == MembershipStatus.ACTIVE) {
                CustomSpacer()

                Box(
                    modifier = Modifier.fillMaxWidth(), // Fill the width of the card
                    contentAlignment = Alignment.Center // Center the content (the button)
                ) {
                    CustomButton(
                        onClick = onCancelClick,
                        tintColor = MaterialTheme.colorScheme.error,
                        label = R.string.cancel_membership,
                    )
                }
            }
        }
    }
}

/**
 * BenefitsUsageCard - Card displaying membership value statistics.
 *
 * Visualizes the total monetary savings and the usage count of specific benefits
 * (e.g., number of free shipping orders).
 *
 * @param totalSavings The calculated total savings amount.
 * @param benefitUsage List of [BenefitUsage] statistics.
 */
@Composable
fun BenefitsUsageCard(
    totalSavings: Double,
    benefitUsage: List<BenefitUsage>
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Card(
        modifier = windowSizeConstant.adaptiveWidthModifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeConstant.baseSize)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.your_savings),
                    style = windowSizeConstant.titleTextStyle,
                )

                CustomSurfaceContainer(
                    color = colors.customColor9.copy(alpha = 0.3f),
                    textStr = formatPrice(totalSavings),
                    contentDescription = "total savings",
                    textStyle = windowSizeConstant.bodyTextStyle
                )
            }

            CustomSpacer()

            // Benefits breakdown
            val freeShippingCount =
                benefitUsage.count { it.benefitType == BenefitType.FREE_SHIPPING }
            val discountsUsed =
                benefitUsage.count { it.benefitType == BenefitType.EXCLUSIVE_DISCOUNT }
            val rewardsEarned = benefitUsage.count { it.benefitType == BenefitType.PRIME_REWARDS }

            BenefitStatRow(
                icon = Icons.Filled.LocalShipping,
                label = "Free Shipping Orders",
                value = freeShippingCount.toString(),
                color = colors.customColor1
            )

            CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseNormalVerticalPadding))

            BenefitStatRow(
                icon = Icons.Filled.Percent,
                label = "Exclusive Discounts",
                value = discountsUsed.toString(),
                color = colors.customColor4
            )

            CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseNormalVerticalPadding))

            BenefitStatRow(
                icon = Icons.Filled.CardGiftcard,
                label = "Reward Points Earned",
                value = rewardsEarned.toString(),
                color = colors.customColor2
            )
        }
    }
}

/**
 * MembershipDetailRow - Displays a single detail row within the status card.
 *
 * @param icon The icon for the detail.
 * @param label The label text.
 * @param value The value text.
 */
@Composable
fun MembershipDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(windowSizeConstant.baseNormalVerticalPadding)
    ) {
        CustomIcon(
            icon = icon,
            contentDescription = "Membership icon"
        )

        CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseNormalVerticalPadding))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = windowSizeConstant.bodyTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = windowSizeConstant.labelTextStyle
            )
        }
    }
}

@Composable
fun BenefitStatRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    val windowSizeAppConstants = LocalWindowSizeConstant.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(windowSizeAppConstants.baseNormalVerticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomSurfaceContainer(
                color = color,
                shape = CustomShape.extraLargeShape(),
                icon = icon,
                iconSize = windowSizeAppConstants.iconSize
            )

            CustomSpacer(modifier = Modifier.height(windowSizeAppConstants.baseNormalVerticalPadding))

            Text(
                text = label,
                style = windowSizeAppConstants.bodyTextStyle
            )
        }

        Text(
            text = value,
            style = windowSizeAppConstants.bodyTextStyle,
        )
    }
}

@Composable
        /**
         * TransactionItem - Displays a single transaction record.
         *
         * Shows details of a membership payment including date, amount, type, and status.
         *
         * @param transaction The [PrimeTransaction] object to display.
         */
fun TransactionItem(transaction: PrimeTransaction) {
    val windowSizeAppConstants = LocalWindowSizeConstant.current

    Card(
        modifier = windowSizeAppConstants.adaptiveWidthModifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeAppConstants.basePadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    transaction.membershipType.displayName,
                    style = windowSizeAppConstants.bodyTextStyle,
                )

                CustomSpacer(modifier = Modifier.height(windowSizeAppConstants.baseNormalVerticalPadding))

                Text(
                    formatDate(transaction.transactionDate),
                    style = windowSizeAppConstants.labelTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                CustomSpacer(modifier = Modifier.height(windowSizeAppConstants.smallVerticalPadding))

                Text(
                    transaction.status.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = when (transaction.status) {
                            TransactionStatus.COMPLETED -> colors.customColor5
                            TransactionStatus.FAILED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                )
            }

            Text(
                formatPrice(transaction.amount),
                style = windowSizeAppConstants.titleTextStyle
            )
        }
    }
}
