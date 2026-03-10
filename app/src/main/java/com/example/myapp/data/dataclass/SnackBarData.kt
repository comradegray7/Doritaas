package com.example.myapp.data.dataclass

import androidx.compose.material3.SnackbarDuration

/**
 * SnackBarData - UI state for Snackbar notifications
 * 
 * Encapsulates message and action data for displaying floating snackbars.
 * 
 * @property message Text message to display
 * @property actionLabel Text for action button (optional)
 * @property isError Whether this is an error message (affects styling)
 * @property duration How long to show the snackbar
 * @property onActionClick Callback for action button click
 */

data class SnackBarData(
    val message: String,
    val actionLabel: String? = null,
    val isError: Boolean = false,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val onActionClick: (() -> Unit)? = null
)
