package com.example.myapp.view.screens.bottom_bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.NetworkIndicator
import com.example.myapp.NetworkManager
import com.example.myapp.NetworkStatusBanner
import com.example.myapp.R
import com.example.myapp.data.dataclass.UserProfile
import com.example.myapp.data.model.AuthViewModel
import com.example.myapp.data.model.NetworkViewModel
import com.example.myapp.data.model.ProfileViewModel
import com.example.myapp.rememberNetworkState
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomIcon
import com.example.myapp.view.components.CustomLazyColumn
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.HeadlineWidget
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.ProfileCardShimmer
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.utils.CustomShape
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay

/**
 * ProfileScreen - User account management and overview.
 *
 * Displays user's profile information sourced from [AuthViewModel] and [ProfileViewModel].
 * Features include:
 * - Dynamic profile header with avatar and welcome message.
 * - Contact information display (Email, Phone).
 * - Admin dashboard access (if applicable).
 * - Sign In / Sign Out functionality.
 * - Real-time profile updates via Firestore.
 *
 * ## Features
 * - **Automatic Profile Creation**: Creates a Firestore profile if one doesn't exist for the auth user.
 * - **Real-time Sync**: Updates UI immediately when profile changes on server.
 * - **Adaptive UI**: Uses [LocalWindowSizeConstant] for responsive layout.
 *
 * ## User Workflow
 * 1. Unauthenticated users see "Sign In" button.
 * 2. Authenticated users see their profile card and contact info.
 * 3. Verified Admins see "Dashboard" button.
 * 4. User can sign out to clear state.
 *
 * @param onSignInClick Navigation to sign in screen.
 * @param onDashboardClick Navigation to Admin Dashboard (admin only).
 * @param onSignOut Navigation/Action after sign out.
 * @param authViewModel [AuthViewModel] for authentication state.
 * @param profileViewModel [ProfileViewModel] for user data management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onViewOrdersClick: (userId: String) -> Unit = {},
    onSignInClick: () -> Unit = {},
    onDashboardClick: () -> Unit = {},
    onSignOut: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    networkManager: NetworkManager = hiltViewModel<NetworkViewModel>().networkManager
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val authState by authViewModel.authState.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()
    val isProfileLoading by profileViewModel.isLoading.collectAsState()
    val networkState = rememberNetworkState(networkManager)

    var loading by remember { mutableStateOf(true) }
    var profileCreationAttempted by remember { mutableStateOf(false) }

    rememberCoroutineScope()

    // Create a better default profile from auth data
    val defaultProfile = remember(authState.user) {
        val user = authState.user
        UserProfile(
            fullName = user?.displayName ?: "",
            displayName = user?.displayName
                ?: user?.email?.substringBefore("@")
                ?: user?.phoneNumber
                ?: "User",
            email = user?.email ?: "",
            phone = user?.phoneNumber ?: ""
        )
    }

    //  the profile to display - use Firestore profile if available and not empty, otherwise use default
    val displayProfile = remember(userProfile, defaultProfile) {
        if (userProfile != null && !userProfile!!.isEmpty()) {
            userProfile
        } else {
            defaultProfile
        }
    }

    // Load user profile when authenticated
    LaunchedEffect(authState.user?.uid) {
        authState.user?.uid?.let { userId ->
            profileViewModel.loadUserProfile(userId)
            profileViewModel.startRealtimeUpdates(userId)
        }
        delay(1500)
        loading = false
    }

    // Create profile if null - WITH BETTER ERROR HANDLING
    LaunchedEffect(
        loading,
        isProfileLoading,
        authState.user,
        userProfile,
        profileCreationAttempted
    ) {
        val userId = authState.user?.uid
        val user = authState.user

        if (!loading &&
            !isProfileLoading &&
            !profileCreationAttempted &&
            userId != null &&
            user != null &&
            (userProfile == null || userProfile!!.isEmpty())
        ) {

            profileCreationAttempted = true

            //   Create profile with guaranteed non-empty displayName
            val displayName = user.displayName?.takeIf { it.isNotBlank() }
                ?: user.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                ?: user.phoneNumber?.takeIf { it.isNotBlank() }
                ?: "User_${userId.take(6)}"

            val now = Timestamp.now()
            val initialProfile = UserProfile(
                fullName = user.displayName ?: "",
                displayName = displayName,
                email = user.email ?: "",
                phone = user.phoneNumber ?: "",
                createdAt = now,
                updatedAt = now,
                lastLogin = now
            )

            profileViewModel.createProfile(userId, initialProfile)
        }
    }

    CustomScaffoldContainer(
        showTopBar = false,
        showBottomBar = false,
        showBackArrow = false,
        verticalArrangement = Arrangement.Center,
        content = {

            if (loading || isProfileLoading) {
                PaddedSection(
                    alignment = Alignment.CenterHorizontally,
                    content = {
                        ProfileCardShimmer()
                    }
                )
            } else {
                CustomLazyColumn {
                    item {
                        // Network Status Banner
                        // Network Indicator in top bar

                        if (!networkState.hasInternet) {
                            CustomSpacer()

                            NetworkIndicator(networkState = networkState)

                            CustomSpacer()
                            // Network Status Banner
                            PaddedSection(
                                alignment = Alignment.CenterHorizontally,
                                content = {
                                    NetworkStatusBanner(
                                        networkState = networkState,
                                    )
                                }
                            )
                        }
                    }

                    item {
                        // Gradient Header

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .height(customSpacing.custom260)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.tertiary,
                                            MaterialTheme.colorScheme.secondary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                ),
                        )

                        PaddedSection(
                            content = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    //   Use displayProfile which intelligently chooses between Firestore and auth data
                                    if (displayProfile != null) {
                                        ProfileCard(
                                            profile = displayProfile
                                        )
                                    }

                                    // Content below profile card
                                    Column(
                                        modifier = Modifier
                                            .padding(top = windowSizeConstant.adaptiveProfileVerticalSpacer),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {

                                        // Contact Info Section
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            HeadlineWidget(middleText = R.string.contact_info)
                                            CustomSpacer()
                                            if (displayProfile != null) {
                                                ContactInfoCard(
                                                    email = displayProfile.email.takeIf { it.isNotBlank() }
                                                        ?: "No email",
                                                    phone = displayProfile.phone.takeIf { it.isNotBlank() }
                                                        ?: "No phone",
                                                )
                                            }
                                        }

                                        CustomSpacer()

                                        // Sign in/out button
                                        if (authState.user == null) {
                                            CustomButton(
                                                label = R.string.sign_in,
                                                onClick = { onSignInClick() },
                                            )
                                        } else {
                                            if (authState.admin || authState.superAdmin) {
                                                CustomButton(
                                                    label = R.string.dashboard,
                                                    onClick = {
                                                        onDashboardClick()
                                                    },
                                                )
                                            }

                                            CustomSpacer()

                                            CustomButton(
                                                label = R.string.my_orders,
                                                onClick = {
                                                    authState.user?.uid?.let { userId ->
                                                        onViewOrdersClick(userId)
                                                    }
                                                },
                                            )

                                            CustomSpacer()

                                            CustomButton(
                                                label = R.string.sign_out,
                                                onClick = {
                                                    profileCreationAttempted = false
                                                    profileViewModel.stopRealtimeUpdates()
                                                    profileViewModel.clearProfile()
                                                    // signOut() is NOT suspend — it launches its own coroutine internally.
                                                    // Call it directly; do NOT wrap in scope.launch or onSignOut() fires twice.
                                                    authViewModel.signOut()
                                                    onSignOut()
                                                },
                                            )
                                        }

                                        CustomSpacer(modifier = Modifier.height(windowSizeConstant.customSpacerLarge))
                                    }
                                }
                            })
                    }
                }
            }
        }
    )
}

/**
 * ProfileCard - Displays the user's avatar and welcome header.
 *
 * Floating card design that overlaps with the background header.
 * Generates initials from the display name for the avatar.
 *
 * @param profile The [UserProfile] data content.
 */
@Composable
private fun BoxScope.ProfileCard(
    profile: UserProfile,
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    //   Use the helper function from UserProfile
    val displayName = profile.getBestDisplayName()

    ElevatedCard(
        modifier = Modifier
            .size(windowSizeConstant.profileCardPadding)
            .align(Alignment.TopCenter)
            .absoluteOffset(y = -customSpacing.custom140),
        shape = CustomShape.extraLargeShape(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = windowSizeConstant.normalVerticalPadding)
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar circle with user initials
                Box(
                    modifier = Modifier
                        .size(windowSizeConstant.customSpacerMedium)
                        .clip(CustomShape.circleShape())
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getInitials(displayName),
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        color = colors.white,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                CustomSpacer()

                // Display profile name

                Text(
                    text = "Welcome, $displayName!",
                    style = windowSizeConstant.titleTextStyle,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = windowSizeConstant.basePadding)
                )
            }
        }
    }
}

/**
 * ContactInfoCard - Card displaying user's contact details.
 *
 * Lists available contact methods like Email, Phone, and Location.
 *
 * @param email User's email address.
 * @param phone User's phone number (optional).
 * @param location User's location (optional).
 */
@Composable
fun ContactInfoCard(
    email: String,
    phone: String = "",
    location: String = ""
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Card(
        modifier = windowSizeConstant.profileInfoPaddings,
        elevation = CardDefaults.cardElevation(defaultElevation = windowSizeConstant.cardElevationPadding)
    ) {
        Column(
            modifier = Modifier.padding(windowSizeConstant.basePadding)
        ) {
            ContactInfoRow(
                icon = Icons.Filled.Email,
                label = "Email",
                value = email
            )

            if (phone.isNotEmpty()) {

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                ContactInfoRow(
                    icon = Icons.Filled.Phone,
                    label = "Phone",
                    value = phone
                )
            }
            if (location.isNotEmpty()) {

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                ContactInfoRow(
                    icon = Icons.Filled.LocationOn,
                    label = "Location",
                    value = location
                )
            }
        }
    }
}

/**
 * ContactInfoRow - Single row in the contact info card.
 *
 * @param icon Icon representing the contact type.
 * @param label Label for the contact info (e.g., "Email").
 * @param value The actual contact information value.
 */
@Composable
fun ContactInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomIcon(
            icon = icon,
            contentDescription = label,
        )

        CustomSpacer(modifier = Modifier.width(windowSizeConstant.baseNormalVerticalPadding))

        Column {
            Text(
                text = label,
                style = windowSizeConstant.bodyTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = value,
                style = windowSizeConstant.bodyTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Helper function to generate initials from a name.
 *
 * Takes the first letter of the first two words in the name.
 * Returns "U" if name is empty.
 *
 * @param name Full name string.
 * @return String containing initials (max 2 chars).
 */
fun getInitials(name: String): String {
    return if (name.isNotEmpty()) {
        name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
    } else {
        "U"
    }
}


