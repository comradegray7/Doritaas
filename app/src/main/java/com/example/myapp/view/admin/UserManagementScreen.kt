package com.example.myapp.view.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.SnackBarData
import com.example.myapp.data.dataclass.UserProfile
import com.example.myapp.data.model.AuthViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomEmptyState
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomListCardShimmer
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.FloatingCustomSnackBar
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.ButtonIcon
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Admin-facing screen for browsing, searching, promoting, demoting, and deleting users.
 *
 * The screen delegates all data loading and mutation to [AuthViewModel], while keeping local
 * UI-only state for search text, transient snackbars, and network banners.
 *
 * @param viewModel Authentication/admin ViewModel that provides users and role actions.
 * @param networkManager Source used to show offline state and block refreshes while offline.
 * @param onNavigateBack Callback invoked when the top app bar back action is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager,
    onNavigateBack: () -> Unit = {}
) {
    val userState by viewModel.authState.collectAsState()
    val networkState = rememberNetworkState(networkManager)
    val windowSizeClass = LocalWindowSizeConstant.current

    // The currently signed-in user's uid — used to prevent self-demotion in the card
    val currentUserId = userState.user?.uid

    var searchQuery by remember { mutableStateOf("") }

    val snackBarHostState = remember { SnackbarHostState() }
    var currentSnackBarData by remember { mutableStateOf<SnackBarData?>(null) }
    var showSnackBar by remember { mutableStateOf(false) }

    // ---- Initial load ----
    LaunchedEffect(Unit) {
        viewModel.loadAllUsers()
    }

    // ---- Snack bar events — separate coroutine so collect starts immediately ----
    LaunchedEffect(Unit) {
        viewModel.snackBarData.collect { snackBarData ->
            currentSnackBarData = snackBarData
            showSnackBar = true
            if (snackBarData.duration != SnackbarDuration.Indefinite) {
                delay(if (snackBarData.duration == SnackbarDuration.Short) 3000L else 5000L)
                showSnackBar = false
            }
        }
    }

    CustomScaffoldContainer(
        title = R.string.manage_users,
        onNavigateBack = { onNavigateBack() },
        verticalArrangement = Arrangement.Top,
        snackBarHostState = snackBarHostState,
        showBottomBar = false,
        onRefresh = {
            if (networkState.hasInternet) viewModel.loadAllUsers()
            else {
                currentSnackBarData = SnackBarData("No internet", isError = true)
                showSnackBar = true
            }
        },
        content = {

            // Network banner
            if (!networkState.hasInternet) {
                CustomSpacer()
                PaddedSection(alignment = Alignment.CenterHorizontally, content = {
                    NetworkStatusBanner(networkState = networkState)
                })
                CustomSpacer()
            }

            // Floating snack bar
            currentSnackBarData?.let { data ->
                PaddedSection(alignment = Alignment.CenterHorizontally, content = {
                    FloatingCustomSnackBar(
                        snackBarData = data,
                        visible = showSnackBar,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(top = windowSizeClass.baseSize),
                        onDismiss = { showSnackBar = false }
                    )
                })
            }

            PaddedSection(alignment = Alignment.CenterHorizontally, content = {
                CustomSpacer()

                CustomSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { viewModel.searchUsers(it) },
                    leadingIcon = {
                        CustomIcon(
                            icon = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_users),
                            style = windowSizeClass.bodyTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            ButtonIconComposable(
                                showBgColor = false,
                                buttonIcon = ButtonIcon.Vector(Icons.Filled.Close),
                                onClick = { searchQuery = "" },
                                contentDescription = "Clear search"
                            )
                        }
                    }
                )

                CustomSpacer()

                when {
                    userState.isLoading -> {
                        CustomSpacer(modifier = Modifier.height(windowSizeClass.customSpacerSmall))
                        CustomListCardShimmer()
                    }

                    userState.error != null -> {
                        CustomEmptyState(
                            btnLabel = R.string.retry,
                            title = R.string.user_error,
                            onBtnClick = { viewModel.loadAllUsers() },
                            btnIcon = Icons.Filled.Error,
                        )
                    }

                    userState.users.isEmpty() -> {
                        CustomEmptyState(
                            titleStr = if (searchQuery.isEmpty()) "No users found"
                            else "No results matching '$searchQuery'",
                            showBtn = false,
                            leadingIcon = Icons.Filled.SearchOff,
                        )
                    }

                    else -> {
                        CustomLazyColumn {
                            items(
                                items = userState.users,
                                key = { user -> user?.id ?: UUID.randomUUID().toString() }
                            ) { user ->
                                UserCard(
                                    user = user,
                                    currentUserId = currentUserId,
                                    callerIsSuperAdmin = userState.currentUserIsSuperAdmin,
                                    onToggleAdmin = { newAdminStatus ->
                                        user?.let {
                                            viewModel.toggleAdminStatus(it.id, newAdminStatus)
                                        }
                                    },
                                    onDeleteUser = { userId ->
                                        viewModel.deleteUser(userId)
                                    }
                                )
                            }
                            item {
                                CustomSpacer(
                                    modifier = Modifier.height(windowSizeClass.customSpacerSmall)
                                )
                            }
                        }
                    }
                }
            })
        }
    )
}

/**
 * Displays a single user profile with role controls and optional delete action.
 *
 * Role and delete controls are intentionally locked for self-management, superAdmin targets,
 * and regular admins attempting to modify another admin. The ViewModel/repository enforce the
 * same rules again before writing to Firestore.
 *
 * @param user User profile to render. A blank [UserProfile] is used when null.
 * @param currentUserId Firebase UID for the currently signed-in user.
 * @param callerIsSuperAdmin Whether the caller may modify admin users and delete users.
 * @param onToggleAdmin Called with the next admin value when role toggle is requested.
 * @param onDeleteUser Called with the target user id after delete confirmation.
 */
@Composable
fun UserCard(
    user: UserProfile? = null,
    currentUserId: String?,
    callerIsSuperAdmin: Boolean,
    onToggleAdmin: (Boolean) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current
    val userProfile = user ?: UserProfile()

    var showDeleteDialog by remember { mutableStateOf(false) }

    val isSelf = currentUserId != null && currentUserId == userProfile.id
    val isSuperAdmin = userProfile.superAdmin

    // Toggle is disabled when:
    //   - It's the signed-in user's own card (self-demotion)
    //   - The target is a superAdmin (immutable from the app)
    //   - The caller is a regular admin trying to modify another admin
    //     (only superAdmins can do that; also enforced server-side)
    val toggleEnabled = !isSelf &&
            !isSuperAdmin &&
            (callerIsSuperAdmin || !userProfile.admin)

    // Delete is only shown to superAdmins, and never for self or other superAdmins
    val deleteVisible = callerIsSuperAdmin && !isSelf && !isSuperAdmin

    Card(
        modifier = windowSizeClass.adaptiveWidthModifier
            .fillMaxWidth()
            .padding(vertical = windowSizeClass.basePadding / 2),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = windowSizeClass.basePadding,
                    vertical = windowSizeClass.baseNormalVerticalPadding
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ---- Info column ----
            Column(modifier = Modifier.weight(1f)) {

                // Name row with optional badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(windowSizeClass.basePadding / 2)
                ) {
                    Text(
                        text = userProfile.fullName,
                        style = windowSizeClass.bodyTextStyle,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSuperAdmin) {
                        CustomIcon(
                            icon = Icons.Filled.Shield,
                            contentDescription = "SuperAdmin",
                            tint = colors.customColor16
                        )
                    }
                    if (isSelf) {
                        Text(
                            text = "(You)",
                            style = windowSizeClass.labelTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = userProfile.email,
                    style = windowSizeClass.labelTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                CustomSpacer(
                    modifier = Modifier.padding(vertical = windowSizeClass.basePadding / 4)
                )

                // Role label + switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(windowSizeClass.basePadding)
                ) {
                    Text(
                        text = when {
                            isSuperAdmin -> "SuperAdmin"
                            userProfile.admin -> "Admin"
                            else -> "User"
                        },
                        color = if (userProfile.admin || isSuperAdmin) colors.customColor16
                        else colors.gray,
                        style = windowSizeClass.labelTextStyle,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = userProfile.admin || isSuperAdmin,
                        onCheckedChange = { if (toggleEnabled) onToggleAdmin(it) },
                        enabled = toggleEnabled
                    )
                }

                // Contextual hint explaining why a control is locked
                val lockReason = when {
                    isSuperAdmin -> "SuperAdmin is managed by the system"
                    isSelf -> "You cannot change your own role"
                    userProfile.admin && !callerIsSuperAdmin -> "Only a SuperAdmin can modify another admin"
                    else -> null
                }
                lockReason?.let {
                    Text(
                        text = it,
                        style = windowSizeClass.labelTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ---- Action buttons ----
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(windowSizeClass.basePadding / 2)
            ) {
                ButtonIconComposable(
                    showBgColor = true,
                    buttonIcon = ButtonIcon.Vector(
                        if (userProfile.admin || isSuperAdmin) Icons.Filled.PersonOff
                        else Icons.Filled.AdminPanelSettings
                    ),
                    onClick = { if (toggleEnabled) onToggleAdmin(!userProfile.admin) },
                    tint = when {
                        !toggleEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                        userProfile.admin -> colors.red
                        else -> colors.green
                    },
                    contentDescription = "Toggle admin"
                )

                if (deleteVisible) {
                    ButtonIconComposable(
                        showBgColor = true,
                        buttonIcon = ButtonIcon.Vector(Icons.Filled.Delete),
                        onClick = { showDeleteDialog = true },
                        tint = colors.red,
                        contentDescription = "Delete user"
                    )
                }
            }
        }
    }

    // ---- Delete confirmation dialog ----
    if (showDeleteDialog) {
        CustomAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete user?",
                    style = windowSizeClass.bodyTextStyle,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will permanently delete ${userProfile.fullName}'s account. This cannot be undone.",
                    style = windowSizeClass.labelTextStyle
                )
            },
            confirmButton = {
                CustomTextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteUser(userProfile.id)
                    }, label = R.string.delete,
                    color = MaterialTheme.colorScheme.error
                )
            },
            dismissButton = {
                CustomTextButton(onClick = { showDeleteDialog = false }, label = R.string.cancel)
            }
        )
    }
}
