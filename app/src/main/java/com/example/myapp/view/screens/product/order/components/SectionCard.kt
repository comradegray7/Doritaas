package com.example.myapp.view.screens.product.order.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.utils.CustomShape

/**
 * SectionCard - A styled card container for a section of content.
 *
 * Provides a consistent background, padding, and title for a group of related elements.
 * Used to group details like "Order Information", "Items", etc. in dialogs.
 *
 * @param title The title of the section displayed at the top.
 * @param content The composable content to display inside the card.
 */
@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = CustomShape.mediumShape()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(windowSizeClass.basePadding),
            verticalArrangement = Arrangement.spacedBy(windowSizeClass.baseNormalVerticalPadding)
        ) {
            Text(
                text = title,
                style = windowSizeClass.titleTextStyle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            content()
        }
    }
}