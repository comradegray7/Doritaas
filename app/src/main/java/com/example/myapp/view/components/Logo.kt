package com.example.myapp.view.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.myapp.R
import com.example.myapp.ui.theme.LocalWindowSizeConstant

/**
 * Logo - Composable function for displaying the app logo.
 *
 * This composable renders the app's logo using the app_logo vector resource.
 * It uses a consistent size based on the app's custom spacing system and
 * fits the content appropriately within the container.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Logo() {

    val windowSizeConstant = LocalWindowSizeConstant.current

    CustomIcon(
        icon = ImageVector.vectorResource(R.drawable.app_logo),
        contentDescription = "App Logo",
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(windowSizeConstant.logoPadding)
    )
}
