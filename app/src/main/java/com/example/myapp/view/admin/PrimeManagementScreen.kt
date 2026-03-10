package com.example.myapp.view.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.MembershipStatus
import com.example.myapp.data.dataclass.PrimeMembership
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.PrimeMembershipViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomFilterChip
import com.example.myapp.view.components.CustomHorizontalDivider
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomLazyRow
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomSurfaceContainer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.custom.buttons.CustomOutlinedButton
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CustomShape
import com.example.myapp.view.utils.formatDate
import com.example.myapp.view.utils.primeUtils.getMembershipStatusColor
import com.example.myapp.view.utils.toRemainingMonths
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * PrimeManagementScreen - Administrative interface for managing Prime memberships
 *
 * Allows administrators to monitor, search, filter, extend, and cancel Prime memberships.
 * Provides a summary of membership statistics and detailed member profiles.
 *
 * ## Features
 * - **Membership Dashboard**: Real-time stats on total and active members
 * - **Advanced Filtering**: Filter members by status (Active, Expired, Cancelled)
 * - **Member Search**: Find members by their unique identifiers
 * - **Lifecycle Management**: Extend memberships or cancel them with immediate effect
 * - **Visual Status Tracking**: Color-coded badges for membership health
 *
 * @param onBackNavigation Callback to return to the previous screen
 * @param viewModel ViewModel handling membership data and lifecycle operations
 * @param networkManager Manager to observe connectivity status
 */
@Composable
fun PrimeManagementScreen(
    onBackNavigation: () -> Unit,
    viewModel: PrimeMembershipViewModel = hiltViewModel(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val uiState by viewModel.membershipState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val networkState = rememberNetworkState(networkManager)

    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf<MembershipStatus?>(null) }
    var showExtendDialog by remember { mutableStateOf<PrimeMembership?>(null) }
    var showCancelDialog by remember { mutableStateOf<PrimeMembership?>(null) }

    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    // Handle snack bar data (same as SizeManagementScreen)
    LaunchedEffect(Unit) {
        viewModel.snackBarData.collect { snackBarData ->
            currentSnackBarData = snackBarData
            showSnackBar = true

            // Auto-dismiss after duration (unless indefinite)
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

    LaunchedEffect(Unit) {
        viewModel.loadAllPrimeMembers()
    }

    CustomScaffoldContainer(
        onRefresh = {
            if (networkState.hasInternet) {
                viewModel.loadAllPrimeMembers()
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
        title = R.string.manage_prime,
        verticalArrangement = Arrangement.Top,
        showBottomBar = false,
        content = {
            // Snack bar
            // Network Status Banner
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
                                .padding(top = windowSizeClass.baseSize),
                            onDismiss = {
                                showSnackBar = false
                                currentSnackBarData = null
                            }
                        )
                    }
                )
            }

            // Search bar
            CustomSpacer()
            CustomSearchBar(
                query = searchQuery,
                onQueryChange = { newQuery ->
                    searchQuery = newQuery
                    if (newQuery.isNotEmpty()) {
                        viewModel.searchPrime(newQuery)
                    } else {
                        viewModel.loadAllPrimeMembers() // Load all when empty
                    }
                },
                onSearch = { query ->
                    viewModel.searchPrime(query)
                },
                leadingIcon = {
                    CustomIcon(
                        icon = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                placeholder = {
                    Text(
                        stringResource(R.string.search_prime),
                        style = windowSizeClass.bodyTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        ButtonIconComposable(
                            showBgColor = false,
                            buttonIcon = ButtonIcon.Vector(Icons.Filled.Search),
                            onClick = {
                                searchQuery = ""
                                viewModel.loadAllPrimeMembers()
                            },
                            contentDescription = "Search"
                        )
                    }
                }
            )
            CustomSpacer()

            // Members List - use filteredMembers
            if (uiState.isLoading) {
                PaddedSection(
                    content = {
                        PaddedSection(
                            content = {
                                CustomListCardShimmer()
                            }
                        )
                    }
                )
            } else if (uiState.filteredMembers.isEmpty()) {  
                CustomEmptyState(
                    title = R.string.no_prime_member_found,
                    showBtn = false,
                    leadingIcon = Icons.Filled.PersonOff
                )
            } else {
                PaddedSection(
                    alignment = Alignment.CenterHorizontally,
                    content = {
                        CustomLazyColumn {
                            item {
                                // Filter Chips
                                CustomLazyRow {
                                    items(
                                        listOf(
                                            null to "All",
                                            MembershipStatus.ACTIVE to "Active",
                                            MembershipStatus.EXPIRED to "Expired",
                                            MembershipStatus.CANCELLED to "Cancelled"
                                        )
                                    ) { (status, label) ->

                                        CustomFilterChip(
                                            isSelected = filterStatus == status,
                                            onClick = {
                                                filterStatus = status
                                                viewModel.filterByStatus(status)
                                            },
                                            label = label
                                        )
                                    }
                                }

                                CustomSpacer()

                                // Summary Stats - primeMembers
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(windowSizeClass.baseNormalVerticalPadding)
                                ) {
                                    StatCard(
                                        title = "Total Members",
                                        value = uiState.filteredMembers.size.toString(),
                                        icon = Icons.Filled.People,
                                        modifier = Modifier.weight(1f)
                                    )

                                    CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

                                    StatCard(
                                        title = "Active",
                                        value = uiState.filteredMembers.count { it.status == MembershipStatus.ACTIVE }
                                            .toString(),
                                        icon = Icons.Filled.CheckCircle,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            items(
                                items = uiState.filteredMembers,  
                                key = { it.userId }
                            ) { member ->
                                // Prime Member Card
                                PrimeMemberCard(
                                    member = member,
                                    onExtend = { showExtendDialog = member },
                                    onCancel = { showCancelDialog = member }
                                )
                            }

                            item {
                                CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))
                            }
                        }
                    })
            }
        }
    )

// Extend Membership Dialog
    showExtendDialog?.let { member ->
        ExtendMembershipDialog(
            member = member,
            onDismiss = { showExtendDialog = null },
            onConfirm = { months ->
                viewModel.extendPrimeMembership(member.userId, months)
                showExtendDialog = null
            }
        )
    }

// Cancel Membership Dialog
    showCancelDialog?.let { member ->
        CustomAlertDialog(
            onDismissRequest = { showCancelDialog = null },
            icon = {
                CustomIcon(
                    icon = Icons.Filled.Warning,
                    contentDescription = "Warning",
                    tint = colors.orange,
                    iconSize = windowSizeClass.largeIconSize
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.cancel_membership),
                    style = windowSizeClass.titleTextStyle
                )
            },
            text = {
                Text(
                    "Cancel Prime membership for user ${member.userId}? This action cannot be undone.",
                    style = windowSizeClass.bodyTextStyle
                )
            },
            confirmButton = {
                CustomTextButton(
                    label = R.string.cancel_membership,
                    onClick = {
                        viewModel.cancelMembership()
                        showCancelDialog = null
                    },
                    color = MaterialTheme.colorScheme.error
                )
            },
            dismissButton = {
                CustomTextButton(
                    label = R.string.cancel,
                    onClick = { showCancelDialog = null }
                )
            }
        )
    }
}


/**
 * PrimeMemberCard - Detailed view of a single Prime membership
 *
 * Displays membership type, status, history, and active benefits.
 * Provides quick actions for membership extension and cancellation.
 *
 * @param member The Prime membership data to display
 * @param onExtend Callback to initiate a membership extension
 * @param onCancel Callback to initiate membership cancellation
 */
@Composable
fun PrimeMemberCard(
    member: PrimeMembership,
    onExtend: () -> Unit,
    onCancel: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Card(
        modifier = windowSizeClass.adaptiveListCardWidthModifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeClass.basePadding)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = getMembershipStatusColor(member.status),
                        shape = CircleShape,
                        modifier = Modifier.size(customSpacing.custom48)
                    ) {
                        CustomIcon(
                            icon = Icons.Filled.Person,
                            modifier = Modifier.padding(windowSizeClass.baseNormalVerticalPadding),
                            contentDescription = "Person",
                            tint = colors.customColor9,
                        )
                    }

                    CustomSpacer(modifier = Modifier.width(windowSizeClass.baseNormalVerticalPadding))

                    Column {
                        Text(
                            member.userId,
                            style = windowSizeClass.titleTextStyle,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        CustomSpacer(modifier = Modifier.height(windowSizeClass.baseVerticalPadding))

                        Text(
                            member.membershipType.displayName,
                            style = windowSizeClass.labelTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            CustomSpacer()
            // Status Badge
            getMembershipStatusColor(status = member.status)

            Surface(
                color = getMembershipStatusColor(status = member.status),
                shape = CustomShape.mediumShape()
            ) {
                Text(
                    member.status.name,
                    style = windowSizeClass.labelTextStyle,
                    fontWeight = FontWeight.Bold,
                    color = when (member.status) {
                        MembershipStatus.ACTIVE -> colors.customColor5
                        MembershipStatus.EXPIRED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(
                        horizontal = windowSizeClass.baseVerticalPadding,
                        vertical = windowSizeClass.smallVerticalPadding
                    )
                )
            }


            CustomHorizontalDivider()

            // Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = windowSizeClass.smallVerticalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,

                ) {
                InfoColumn(
                    label = "Started",
                    value = formatDate(member.startDate),
                    icon = Icons.Filled.CalendarMonth
                )
                InfoColumn(
                    label = "Expires",
                    value = formatDate(member.endDate),
                    icon = Icons.Filled.Update
                )
                InfoColumn(
                    label = "Payment",
                    value = member.paymentMethod,
                    icon = Icons.Filled.Payment
                )
            }

            CustomSpacer(modifier = Modifier.height(windowSizeClass.baseNormalVerticalPadding))

            // Benefits Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (member.benefits.freeShipping) {
                    BenefitBadge(
                        modifier = Modifier.weight(1f),
                        label = "Free Shipping", icon = Icons.Filled.LocalShipping
                    )
                }

                CustomSpacer(modifier = Modifier.width(windowSizeClass.contentVerticalPadding))

                if (member.benefits.exclusiveDiscountPercentage > 0) {
                    BenefitBadge(
                        modifier = Modifier.weight(1f),
                        label = "${member.benefits.exclusiveDiscountPercentage}% Off",
                        icon = Icons.Filled.Percent
                    )
                }

                CustomSpacer(modifier = Modifier.width(windowSizeClass.contentVerticalPadding))

                if (member.benefits.earlyAccessHours > 0) {
                    BenefitBadge(
                        modifier = Modifier.weight(1f),
                        label = "${member.benefits.earlyAccessHours}h Early",
                        icon = Icons.Filled.AccessTime
                    )
                }
            }

            // Actions
            if (member.status == MembershipStatus.ACTIVE) {
                CustomSpacer()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,

                    ) {
                    CustomOutlinedButton(
                        onClick = onExtend,
                        label = R.string.extend,
                        icon = ButtonIcon.Vector(Icons.Filled.Add),
                        modifier = Modifier.weight(1f)
                    )

                    CustomSpacer(modifier = Modifier.width(windowSizeClass.contentVerticalPadding))

                    CustomButton(
                        useSmallWidth = true,
                        onClick = onCancel,
                        label = R.string.cancel,
                        tintColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * ExtendMembershipDialog - Management tool for prolonging subscriptions
 *
 * Allows administrators to add time to an existing membership.
 * Previews the current expiration state and the new calculated end date.
 *
 * @param member The member whose subscription is being extended
 * @param onDismiss Callback to close the dialog without changes
 * @param onConfirm Callback with the number of months to add
 */
@Composable
fun ExtendMembershipDialog(
    member: PrimeMembership,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    //   Format end date for display
    val remainingMonths = remember(member.endDate) {
        member.endDate.toRemainingMonths()
    }

    val endDateFormatted = remember(member.endDate) {
        formatDate(member.endDate)
    }

    var selectedMonths by remember { mutableIntStateOf(1) }
    val listOfMonths = listOf(1, 3, 6, 12)

    //  Calculate new end date based on selection
    val newEndDate = remember(selectedMonths, member.endDate) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = member.endDate
        calendar.add(Calendar.MONTH, selectedMonths)
        calendar.timeInMillis
    }

    val newEndDateFormatted = remember(newEndDate) {
        formatDate(newEndDate)
    }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Default.CalendarMonth,
                iconSize = windowSizeClass.largeIconSize,
                contentDescription = "Calendar month"
            )
        },
        title = {
            Text(
                stringResource(R.string.extend_membership),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // User info
                Text(
                    "Extend Prime membership for ${member.userId}",
                    style = windowSizeClass.bodyTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                CustomSpacer()

                // Show current status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(windowSizeClass.baseNormalVerticalPadding)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Current End Date:",
                                style = windowSizeClass.labelTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                endDateFormatted,
                                style = windowSizeClass.bodyTextStyle,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Remaining:",
                                style = windowSizeClass.labelTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                "$remainingMonths month${if (remainingMonths != 1) "s" else ""}",
                                style = windowSizeClass.bodyTextStyle,
                                color = if (remainingMonths < 1) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                }

                CustomSpacer()

                Text(
                    stringResource(R.string.select_extension_period),
                    style = windowSizeClass.labelTextStyle
                )

                CustomSpacer()

                // Extension period selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(windowSizeClass.baseNormalVerticalPadding)
                ) {
                    listOfMonths.forEach { months ->
                        CustomFilterChip(
                            isSelected = selectedMonths == months,
                            onClick = { selectedMonths = months },
                            label = "$months month${if (months > 1) "s" else ""}"
                        )
                    }
                }

                CustomSpacer()

                //  Show new end date preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(windowSizeClass.baseNormalVerticalPadding)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "New End Date:",
                                    style = windowSizeClass.labelTextStyle,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Text(
                                    newEndDateFormatted,
                                    style = windowSizeClass.labelTextStyle,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            CustomIcon(
                                icon = Icons.AutoMirrored.Filled.ArrowForward,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                iconSize = windowSizeClass.mediumIconSize
                            )
                        }

                        CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

                        Text(
                            "Total: ${remainingMonths + selectedMonths} month${if (remainingMonths + selectedMonths != 1) "s" else ""} from today",
                            style = windowSizeClass.labelTextStyle,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            CustomTextButton(
                label = R.string.extend,
                onClick = { onConfirm(selectedMonths) }
            )
        },
        dismissButton = {
            CustomTextButton(
                label = R.string.cancel,
                onClick = onDismiss,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

/**
 * InfoColumn - Reusable vertical key-value display with an icon
 *
 * @param label The descriptive label for the data point
 * @param value The primary value to display
 * @param icon The icon representing the data type
 */
@Composable
fun InfoColumn(
    label: String,
    value: String,
    icon: ImageVector
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CustomIcon(
            icon = icon,
            contentDescription = "content description",
            tint = MaterialTheme.colorScheme.primary
        )

        CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

        Text(
            label,
            style = windowSizeClass.labelTextStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CustomSpacer(modifier = Modifier.height(windowSizeClass.smallVerticalPadding))

        Text(
            value,
            style = windowSizeClass.bodyTextStyle,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * BenefitBadge - Visual indicator for an active Prime benefit
 *
 * @param modifier Layout modifier
 * @param label Text description of the benefit
 * @param icon Representative icon
 */
@Composable
fun BenefitBadge(
    modifier: Modifier = Modifier,
    label: String, icon: ImageVector
) {
    CustomSurfaceContainer(
        modifier = modifier,
        color = colors.customColor9,
        icon = icon,
        textStr = label,
        contentDescription = null,
        tint = colors.customColor9
    )
}

/**
 * StatCard - Compact display for administrative metrics
 *
 * @param title Metric name
 * @param value Metric value
 * @param icon Metric icon
 * @param modifier Layout modifier
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(customSpacing.custom16),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomIcon(
                icon = icon,
                contentDescription = "Content description",
                iconSize = windowSizeClass.largeIconSize,
            )

            CustomSpacer(modifier = Modifier.height(windowSizeClass.baseVerticalPadding))

            Text(
                value,
                style = windowSizeClass.titleTextStyle,
                fontWeight = FontWeight.Bold
            )

            CustomSpacer(modifier = Modifier.height(windowSizeClass.baseVerticalPadding))

            Text(
                title,
                style = windowSizeClass.labelTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}