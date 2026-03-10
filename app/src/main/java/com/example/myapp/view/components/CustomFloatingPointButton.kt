package com.example.myapp.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * CustomFloatingPointButton - A wrapper around Material 3's FloatingActionButton.
 *
 * Provides a standardized FAB component with consistent styling and optional icon support.
 *
 * @param onClick Callback called when the FAB is clicked.
 * @param icon The icon to be displayed inside the FAB. Defaults to [Icons.Filled.Add].
 */
@Composable
fun CustomFloatingPointButton(
    containerColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    icon: ImageVector? = Icons.Filled.Add
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor
    )
    {
        if (icon != null) {
            CustomIcon(
                icon = icon,
                contentDescription = "New",
            )
        }
    }
}