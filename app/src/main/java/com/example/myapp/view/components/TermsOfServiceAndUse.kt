package com.example.myapp.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import com.example.myapp.ui.theme.LocalWindowSizeConstant

/**
 * TermsOfServiceAndUse - Composable function for terms and privacy policy agreement checkbox.
 * 
 * This composable creates a checkbox with clickable links to Terms of Service and Privacy Policy.
 * It's commonly used in sign-up forms to get user consent for legal agreements.
 * The component supports error states and customizable URLs for the legal documents.
 * 
 * @param modifier Optional modifier to apply to the container
 * @param isChecked Current state of the checkbox
 * @param onCheckedChange Callback for checkbox state changes
 * @param termsUrl URL for the Terms of Service document
 * @param privacyUrl URL for the Privacy Policy document
 * @param isError Whether to show error state
 * @param errorMessage Error message to display when isError is true
 * 
 * ## Usage:
 * ```kotlin
 * TermsOfServiceAndUse(
 *     isChecked = termsAccepted,
 *     onCheckedChange = { termsAccepted = it },
 *     isError = termsError.isNotEmpty(),
 *     errorMessage = termsError
 * )
 * ```
 */
@Composable
fun TermsOfServiceAndUse(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    termsUrl: String = "https://example.com/terms",
    privacyUrl: String = "https://example.com/privacy",
    isError: Boolean = false,
    errorMessage: String = "",
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    // Build annotated string with clickable links
    val annotatedText = buildAnnotatedString {
        append("I agree with the ")

        // Terms of Service link with styling
        withLink(
            LinkAnnotation.Url(termsUrl) // Make text clickable with URL
        ) {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary, // Primary color for link
                    textDecoration = TextDecoration.Underline // Underline to indicate it's clickable
                )
            ) {
                append("Terms of Service")
            }
        }

        append(" and ")

        // Privacy Policy link with styling
        withLink(
            LinkAnnotation.Url(privacyUrl) // Make text clickable with URL
        ) {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary, // Primary color for link
                    textDecoration = TextDecoration.Underline // Underline to indicate it's clickable
                )
            ) {
                append("Privacy Policy")
            }
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        // Checkbox and text row
        Row(
            modifier = modifier.fillMaxWidth(), // Take full width
            horizontalArrangement = Arrangement.Center, // Center the content
            verticalAlignment = Alignment.CenterVertically // Center items vertically
        ) {
            // Agreement checkbox
            Checkbox(
                enabled = enabled,
                checked = isChecked, // Current checkbox state
                onCheckedChange = onCheckedChange, // Handle state changes
            )

            // Terms and privacy policy text with clickable links
            Text(
                text = annotatedText, // Use the annotated string with links
                style = windowSizeConstant.bodyTextStyle,
                color = MaterialTheme.colorScheme.onSurface // Use surface color for text
            )
        }
        
        // Error message display
        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                softWrap = true, // Allow text wrapping
                color = MaterialTheme.colorScheme.error, // Error color
                style = windowSizeConstant.labelTextStyle, // Adaptive label style
            )
        }
    }
}



