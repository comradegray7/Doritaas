package com.example.myapp.view.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * CustomAssistChip - A styled Material 3 AssistChip with configurable colors and icons.
 *
 * Assist chips represent a smart or temporary action that can be taken in a specific context.
 * This wrapper provides a simplified interface for common customizations used in the app.
 *
 * @param modifier The modifier to be applied to the chip
 * @param onClick Callback called when the chip is clicked
 * @param label The text to be displayed on the chip
 * @param leadingIcon Optional composable to be displayed at the start of the chip
 * @param trailingIcon Optional composable to be displayed at the end of the chip
 * @param textStyle The style to be applied to the label text
 */
@Composable
fun CustomAssistChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    label: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    textStyle: TextStyle = TextStyle.Default
) {

    AssistChip(
        modifier = modifier,
        onClick = onClick,
        label = {
            Text(
                label, style = textStyle,
                overflow = TextOverflow.Ellipsis

            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor
        )
    )
}