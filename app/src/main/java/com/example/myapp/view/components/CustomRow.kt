package com.example.myapp.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.myapp.ui.theme.LocalWindowSizeConstant

/**
 * CustomRow - Composable function for creating a row with leading and trailing text.
 * 
 * This composable creates a horizontal row with two text elements - a leading text
 * and a clickable trailing text. It's commonly used for navigation links or
 * action prompts in forms and lists.
 * 
 * @param leadingText String resource ID for the leading (left) text
 * @param trailingText String resource ID for the trailing (right) clickable text
 * @param onClick Callback function for when the trailing text is clicked
 *
 * ## Usage:
 * ```kotlin
 * CustomRow(
 *     leadingText = R.string.do_not_have_account,
 *     trailingText = R.string.sign_up,
 *     onClick = { /* navigate to sign up */ }
 * )
 * ```
 */
@Composable
fun CustomRow(
    leadingText: Int, trailingText: Int, onClick: () -> Unit) {
    // Get the current window size constants for adaptive behavior
    val windowSizeConstant = LocalWindowSizeConstant.current

    Row(
        modifier = Modifier.fillMaxWidth(), // Take full available width
        verticalAlignment = Alignment.CenterVertically, // Center items vertically
        horizontalArrangement = Arrangement.Center // Center items horizontally
    ) {
        // Leading text - typically descriptive or informational
        Text(
            text = stringResource(id = leadingText),
            style = windowSizeConstant.bodyTextStyle // Use adaptive body text style
        )
        
        // Small spacer between leading and trailing text
        CustomSpacer(modifier = Modifier.width(windowSizeConstant.normalVerticalPadding))
        
        // Trailing text - clickable action or link
        Text(
            text = stringResource(id = trailingText),
            color = MaterialTheme.colorScheme.tertiary, // Use tertiary color for emphasis
            style = windowSizeConstant.bodyTextStyle, // Use adaptive body text style
            modifier = Modifier.clickable(onClick = onClick) // Make text clickable
        )
    }
}