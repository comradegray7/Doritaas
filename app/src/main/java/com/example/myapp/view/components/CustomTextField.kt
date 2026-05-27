package com.example.myapp.view.components

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.utils.ButtonIcon
import com.example.myapp.view.utils.CustomShape

/**
 * VALIDATION FUNCTIONS
 *
 * These functions provide common validation logic for form fields.
 * They can be used throughout the app to ensure data integrity.
 */

/**
 * Validates if the provided string is a valid email address.
 *
 * @param email The email string to validate
 * @return true if the email is valid, false otherwise
 */
fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

/**
 * Validates if the provided string meets password requirements.
 *
 * @param password The password string to validate
 * @return true if the password is at least 8 characters and contains both letters and numbers
 */
fun isValidPassword(password: String): Boolean {
    return password.length >= 8 &&
            password.any { it.isDigit() } &&
            password.any { it.isLetter() }
}

/**
 * Validates if the provided string is a valid full name.
 *
 * @param name The name string to validate
 * @return true if the name is at least 2 characters and contains at least 2 words
 */

fun isValidFullName(name: String): Boolean {
    return name.trim().length >= 2 && name.trim().split(" ").size >= 2
}

/**
 * isValidCardNumber - Validates if a string is a valid credit card number.
 *
 * Checks if the string length is 16 and consists only of digits.
 *
 * @param cardNumber The card number string to validate
 * @return True if valid, false otherwise
 */
fun isValidCardNumber(cardNumber: String): Boolean {
    return cardNumber.length == 16 && cardNumber.all { it.isDigit() }
}

/**
 * isValidExpiryDate - Validates credit card expiry date format (MM/YY or MM/YYYY).
 *
 * Checks for the presence of a forward slash and validates that both month and year are numerical.
 *
 * @param expiryDate The expiry date string to validate
 * @return True if formatted correctly and month is between 1-12.
 */
fun isValidExpiryDate(expiryDate: String): Boolean {
    if (!expiryDate.contains("/")) return false
    val parts = expiryDate.split("/")
    if (parts.size != 2) return false

    val month = parts[0].toIntOrNull() ?: return false
    val year = parts[1].toIntOrNull() ?: return false

    return month in 1..12 && year >= 0
}

/**
 * isValidCvv - Validates typical credit card CVV (Card Verification Value).
 *
 * Checks if the string has exactly 3 digits.
 *
 * @param cvv The CVV string to validate
 * @return True if valid, false otherwise
 */
fun isValidCvv(cvv: String): Boolean {
    return cvv.length == 3 && cvv.all { it.isDigit() }
}

/**
 * CustomTextField - Composable function for creating customizable text input fields.
 *
 * This composable creates a Material Design 3 outlined text field with support for:
 * - Labels and placeholders
 * - Password visibility toggle
 * - Custom icons
 * - Error states and messages
 * - Adaptive sizing based on window size
 *
 * @param modifier Optional modifier to apply to the text field
 * @param label String resource ID for the field label
 * @param shape Corner radius for the text field (defaults to custom normal spacing)
 * @param placeholder String resource ID for the placeholder text
 * @param icon Optional icon to display as trailing icon
 * @param onClickIcon Callback for when the icon is clicked
 * @param value Current value of the text field
 * @param onValueChange Callback for when the text value changes
 * @param isError Whether to show error state
 * @param errorMessage Error message to display when isError is true
 * @param isPassword Whether this field is for password input
 *
 * Usage:
 * ```
 * CustomTextField(
 *     label = R.string.email,
 *     placeholder = R.string.enter_email,
 *     value = email,
 *     onValueChange = { email = it },
 *     isError = emailError.isNotEmpty(),
 *     errorMessage = emailError
 * )
 * ```
 */

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    @StringRes label: Int? = null,
    labelStr: String? = "",
    shape: Shape = CustomShape.mediumShape(),
    @StringRes placeholder: Int? = null,
    placeholderUnit: @Composable (() -> Unit)? = null,
    icon: ImageVector? = null,
    onClickIcon: () -> Unit = {},
    value: String = "",
    onValueChange: (String) -> Unit = {},
    isError: Boolean = false,
    enabled: Boolean = true,
    errorMessage: String = "",
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIconContent: (@Composable (() -> Unit))? = null,
    maxLines: Int = 1,
    minLines: Int = 1,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    supportingText: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    // State for password visibility toggle
    var passwordVisible by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            modifier = modifier.then(windowSizeConstant.adaptiveFormWidthModifier),
            value = value,
            singleLine = singleLine,
            maxLines = maxLines,
            readOnly = readOnly,
            minLines = minLines,
            enabled = enabled,
            leadingIcon = leadingIcon,
            supportingText = supportingText,
            placeholder = {
                // Prioritize placeholderUnit if provided (for dynamic content)
                if (placeholderUnit != null) {
                    placeholderUnit()
                }
                // Otherwise use resource placeholder if provided
                else if (placeholder != null) {
                    Text(
                        style = windowSizeConstant.labelTextStyle,
                        text = stringResource(placeholder)
                    )
                }
            },
            onValueChange = onValueChange,
            label = {
                // Use labelStr if provided (for dynamic content)
                if (!labelStr.isNullOrBlank()) {
                    Text(
                        text = labelStr,
                        fontWeight = FontWeight.SemiBold,
                        style = windowSizeConstant.bodyTextStyle
                    )
                }
                // Otherwise use resource label if provided
                else if (label != null) {
                    Text(
                        text = stringResource(label),
                        fontWeight = FontWeight.SemiBold,
                        style = windowSizeConstant.bodyTextStyle
                    )
                }
            },
            shape = shape,
            isError = isError,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = {
                when {
                    trailingIconContent != null -> trailingIconContent()
                    isPassword -> {
                        ButtonIconComposable(
                            showBgColor = false,
                            buttonIcon = ButtonIcon.Vector(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff),
                            onClick = { passwordVisible = !passwordVisible },
                            contentDescription = "Toggle password visibility",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    icon != null -> {
                        ButtonIconComposable(
                            showBgColor = false,
                            buttonIcon = ButtonIcon.Vector(icon),
                            onClick = onClickIcon,
                            contentDescription = "Trailing icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            keyboardOptions = keyboardOptions,
        )

        // Error message display
        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                softWrap = true,
                color = MaterialTheme.colorScheme.error,
                style = windowSizeConstant.labelTextStyle,
                modifier = Modifier
                    .padding(top = windowSizeConstant.baseVerticalPadding)
                    .fillMaxWidth()
            )
        }
    }
}
