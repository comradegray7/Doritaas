package com.example.myapp.view.components

import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow

/**
 * AppBarTitle - Composable function for displaying app bar titles.
 * 
 * This composable provides a flexible way to display titles in app bars,
 * supporting both string resources and direct string values. It uses
 * Material Design 3 typography for consistent styling.
 * 
 * @param title Optional string resource ID for the title
 * @param titleStr Optional direct string value for the title
 *
 * ## Usage:
 * ```kotlin
 * AppBarTitle(title = R.string.my_title)
 * AppBarTitle(titleStr = "Custom Title")
 * ```
 */
@Composable
fun AppBarTitle(@StringRes title: Int? = null, titleStr: String? = null) {
    val resolvedTitle = when {
        title != null -> stringResource(title)
        !titleStr.isNullOrBlank() -> titleStr
        else -> null
    }

    resolvedTitle?.let {
        Text(
            modifier = Modifier,
            text = it,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

