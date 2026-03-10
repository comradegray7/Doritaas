package com.example.myapp.view.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomRadioButton(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: (() -> Unit)?,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    colors: RadioButtonColors = RadioButtonDefaults.colors()
) {
    RadioButton(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        colors = colors,
        interactionSource = interactionSource,
        enabled = enabled
    )
}