package com.example.myapp.view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * CustomAlertDialog - Reusable Alert Dialog component.
 *
 * A wrapper around Material Design 3 [AlertDialog] providing a standard interface
 * for confirmation dialogs across the application. Supports optional icons and
 * vertical scrolling for long content.
 *
 * @param modifier The modifier to be applied to the dialog
 * @param onDismissRequest Callback invoked when the user tries to dismiss the dialog (e.g., clicking outside).
 * @param title Composable for the dialog title.
 * @param text Composable for the main content/message of the dialog.
 * @param confirmButton Composable for the confirm action button.
 * @param dismissButton Composable for the dismiss/cancel action button (optional).
 * @param icon Optional composable for the icon displayed above the title.
 * @param scrollable Whether the content should be vertically scrollable. Set to false for lazy components.
 */
@Composable
fun CustomAlertDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit = {},
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit = {},
    dismissButton: @Composable () -> Unit = {},
    icon: @Composable () -> Unit = {},
    scrollable: Boolean = true
) {
    AlertDialog(
        modifier = modifier,
        icon = icon,
        onDismissRequest = { onDismissRequest() },
        title = { title() },
        text = {
            // Only add scrolling when scrollable = true
            if (scrollable) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    text()
                }
            } else {
                //  For lazy components, just render directly
                Box(modifier = Modifier.fillMaxWidth()) {
                    text()
                }
            }
        },
        confirmButton = { confirmButton() },
        dismissButton = { dismissButton() }
    )
}