package com.example.myapp.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.myapp.data.dataclass.Offer
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.utils.CustomShape

@Composable
fun HeroCard(
    offer: Offer,
) {
    val windowSizeAppConstants = LocalWindowSizeConstant.current

    Column(
        modifier = Modifier
            .clickable(onClick = offer.onClick)
            .size(windowSizeAppConstants.heroCardPadding)
    ) {
        // Hero Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = CustomShape.mediumShape()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            offer.gradient
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(windowSizeAppConstants.smallVerticalPadding),
                    verticalArrangement = Arrangement.spacedBy(windowSizeAppConstants.smallVerticalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Only show the icon if provided
                    offer.leadingIcon?.let {
                        CustomIcon(
                            icon = it,
                            tint = colors.white,
                            contentDescription = "Leading icon",
                            iconSize = windowSizeAppConstants.heroIconSize
                        )
                    }

                    // Only show the title if provided
                    if (offer.title.isNotEmpty()) {
                        Text(
                            offer.title,
                            softWrap = true,
                            style = windowSizeAppConstants.titleTextStyle,
                            fontWeight = FontWeight.Bold,
                            color = colors.white
                        )
                    }

                    // Only show the description if provided
                    if (!offer.description.isNullOrEmpty()) {
                        Text(
                            offer.description,
                            style = windowSizeAppConstants.bodyTextStyle,
                            color = colors.white.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Only show the button if provided
                    if (!offer.buttonText.isNullOrEmpty()) {
                        val gradientBrush = Brush.horizontalGradient(
                            colors = offer.gradient,
                            startX = 0.0f,
                            endX = 500.0f,
                        )

                        CustomButton(
                            modifier = Modifier.padding(horizontal = windowSizeAppConstants.customSpacerSmall),
                            strLabel = offer.buttonText,
                            onClick = offer.onClick,
                            textModifier = Modifier
                                .graphicsLayer { alpha = 0.99f }
                                .drawWithContent {
                                    drawContent()
                                    drawRect(
                                        brush = gradientBrush,
                                        blendMode = BlendMode.SrcAtop
                                    )
                                },
                            buttonColors = ButtonDefaults.buttonColors(containerColor = colors.white),
                            shape = CustomShape.extraLargeShape()
                        )
                    }

                    // Only show the composable function if provided
                    offer.composableFunction?.invoke()
                }
            }
        }
    }
}