package com.example.myapp.view.components.custom.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.myapp.ui.theme.LocalWindowSizeConstant

/**
 * CustomTextButton - A simple clickable text element.
 *
 * This composable renders text that acts as a button, typically used for less prominent
 * actions like "See all" or "Cancel".
 *
 * @param label Drawable resource ID for the button label text.
 * @param onClick Callback invoked when the text is clicked.
 * @param color Color of the text.
 */
@Composable
fun CustomTextButton(
    modifier: Modifier = Modifier,
    @DrawableRes label: Int? = null,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    strLabel: String = "",
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    useStringResourceLabel: Boolean = (label != 0 && strLabel.isBlank()), // Derived: true if label is valid and strLabel is not
    useDirectStringLabel: Boolean = strLabel.isNotBlank(),
) {
    val windowSizeAppConstant  = LocalWindowSizeConstant.current

   when {
        useDirectStringLabel -> { // If strLabel is provided and not blank
            Text(
                text =  strLabel,
                style = windowSizeAppConstant.labelTextStyle,
                color = color,
                modifier = modifier.then(Modifier.clickable(onClick = onClick, enabled = enabled)) // Make text clickable
            )

        }

        useStringResourceLabel -> { // Else, if label is a valid resource ID
            label?.let {
                Text(
                    text =  stringResource(it),
                    style = windowSizeAppConstant.labelTextStyle,
                    color = color,
                    modifier = modifier.then(Modifier.clickable(onClick = onClick, enabled = enabled)) // Make text clickable
                )
            }
        }

       else -> {}
   }
}