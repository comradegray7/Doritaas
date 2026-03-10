package com.example.myapp.view.components

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.myapp.ui.theme.LocalWindowSizeConstant

/**
 * CustomFilterChip - A styled Material 3 FilterChip with adaptive typography and custom color support.
 *
 * Filter chips use tags or descriptive words to filter content. This component provides
 * a consistent look and feel across the application with simplified property overrides.
 *
 * @param modifier The modifier to be applied to the chip
 * @param onClick Callback called when the chip is clicked
 * @param isSelected Whether the chip is currently selected/active
 * @param label The text to be displayed on the chip
 * @param selectedContainerColor Background color when the chip is selected
 * @param selectedLabelColor Text color when the chip is selected
 * @param disabledContainerColor Background color when the chip is disabled
 * @param disabledLabelColor Text color when the chip is disabled
 * @param borderColor Border color for the unselected state
 * @param selectedBorderColor Border color when the chip is selected
 * @param disabledBorderColor Border color when the chip is disabled
 * @param disabledSelectedBorderColor Border color when the chip is disabled but was selected
 * @param leadingIcon Optional composable to be displayed at the start of the chip (e.g., a checkmark)
 * @param trailingIcon Optional composable to be displayed at the end of the chip
 */
@Composable
fun CustomFilterChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    label: String,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor: Color = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    disabledLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    selectedBorderColor: Color = MaterialTheme.colorScheme.primaryContainer,
    disabledBorderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    disabledSelectedBorderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    FilterChip(
        modifier = modifier,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = windowSizeConstant.labelTextStyle,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                overflow = TextOverflow.Ellipsis
            )
        },
        selected = isSelected,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = selectedContainerColor,
            selectedLabelColor = selectedLabelColor,
            disabledContainerColor = disabledContainerColor,
            disabledLabelColor = disabledLabelColor,
            disabledLeadingIconColor = disabledLabelColor,
            disabledTrailingIconColor = disabledLabelColor,
            selectedLeadingIconColor = selectedLabelColor,
            selectedTrailingIconColor = selectedLabelColor,
            labelColor = selectedLabelColor,
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = borderColor,
            selectedBorderColor = selectedBorderColor,
            disabledBorderColor = disabledBorderColor,
            disabledSelectedBorderColor = disabledSelectedBorderColor,
            enabled = isSelected,
            selected = isSelected,
        )
    )
}