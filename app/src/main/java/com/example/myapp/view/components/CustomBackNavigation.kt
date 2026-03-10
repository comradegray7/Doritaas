package com.example.myapp.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.utils.ButtonIcon

/**
 * CustomBackNavigation - Composable function for back navigation button.
 * 
 * This composable creates a back navigation button using Material Design 3
 * styling. It uses the AutoMirrored arrow back icon which automatically
 * flips direction for RTL (right-to-left) languages.
 * 
 * @param onNavigateBack Callback function to handle back navigation
 *
 * ## Usage:
 * ```kotlin
 * CustomBackNavigation(onNavigateBack = { navController.popBackStack() })
 * ```
 */
@Composable
fun CustomBackNavigation(onNavigateBack: () -> Unit) {
    ButtonIconComposable(
        showBgColor = false,
        buttonIcon = ButtonIcon.Vector(Icons.AutoMirrored.Filled.ArrowBack),
        onClick = { onNavigateBack() },
        contentDescription = "Back"
    )
}