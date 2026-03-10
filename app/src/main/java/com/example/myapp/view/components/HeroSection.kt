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
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.utils.CustomShape

/**
 * HeroSection - A prominent section for highlighting offers or features.
 *
 * This composable renders a large, visually appealing card that typically contains a background gradient,
 * a title, an optional description, a call-to-action button, and an optional leading icon.
 * It is designed to grab the user's attention and direct them to a specific action or detail view.
 *
 * @param offer The [Offer] data object containing the content (title, description, gradient, etc.) to be displayed.
 */
@Composable
fun HeroSection(
    offer: Offer
) {
    val windowSizeAppConstants = LocalWindowSizeConstant.current

    Column(
        modifier = Modifier
            .clickable(onClick = offer.onClick)
            .size(windowSizeAppConstants.heroSectionPadding)
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
                    offer.leadingIcon?.let {
                        CustomIcon(
                            icon = it,
                            tint = colors.white,
                            contentDescription = "Leading icon",
                            iconSize = windowSizeAppConstants.heroIconSize
                        )
                    }

                    Text(
                        offer.title,
                        softWrap = true,
                        style = windowSizeAppConstants.titleTextStyle,
                        fontWeight = FontWeight.Bold,
                        color = colors.white
                    )

                    offer.buttonText?.let { it ->
                        if (it.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(windowSizeAppConstants.normalVerticalPadding)
                                    .size(customSpacing.custom150)
                                    .background(
                                        shape = CustomShape.extraLargeShape(),
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                colors.white.copy(alpha = 0.9f),
                                                colors.white,
                                                colors.white
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {

                                val gradientBrush = Brush.horizontalGradient(
                                    colors = offer.gradient,
                                    startX = 0.0f,
                                    endX = 500.0f,
                                )

                                Text(
                                    offer.buttonText,
                                    style = windowSizeAppConstants.bodyTextStyle,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .graphicsLayer { alpha = 0.99f } // Force a graphics layer
                                        .drawWithContent {
                                            drawContent()
                                            drawRect(
                                                brush = gradientBrush,
                                                blendMode = BlendMode.SrcAtop
                                            )
                                        }
                                )
                            }
                        }
                    }

                    CustomSpacer(modifier = Modifier.size(windowSizeAppConstants.smallVerticalPadding))

                    if (offer.composableFunction != null) {
                        offer.composableFunction()
                    }
                }
            }
        }
    }
}

