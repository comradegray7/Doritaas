package com.example.myapp.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.myapp.R
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.custom.buttons.CustomTextButton

/**
 * SignInRequiredDialog - Authentication prompt dialog
 *
 * Displays when user attempts an action requiring authentication (e.g., adding to favorites,
 * adding to cart). Prompts user to sign in or dismiss.
 *
 * ## Features
 * - Clear sign-in requirement message
 * - Two action buttons: Sign In and Dismiss
 * - Material Design 3 AlertDialog
 *
 * ## User Actions
 * 1. **Sign In**: Navigates to sign-in screen
 * 2. **Dismiss**: Closes dialog without action
 *
 * ## Usage Example
 * ```kotlin
 * if (showSignInDialog) {
 *     SignInRequiredDialog(
 *         onDismiss = { showSignInDialog = false },
 *         onSignInClick = {
 *             showSignInDialog = false
 *             navigateToSignIn()
 *         }
 *     )
 * }
 * ```
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onSignInClick Callback when user chooses to sign in
 */
@Composable
fun SignInRequiredDialog(
    onDismiss: () -> Unit,
    onSignInClick: () -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            CustomIcon(
                icon = Icons.Filled.Warning,
                iconSize = windowSizeClass.largeIconSize,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                stringResource(R.string.sign_in_required),
                style = windowSizeClass.titleTextStyle
            )
        },
        text = {
            Text(
                stringResource(R.string.special_offer),
                style = windowSizeClass.bodyTextStyle
            )
        },
        dismissButton = {
            CustomTextButton(
                onClick = onDismiss,
                label = R.string.cancel,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            CustomTextButton(
                onClick = onSignInClick,
                label = R.string.sign_in,
            )
        }
    )
}
