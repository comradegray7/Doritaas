package com.example.myapp.view.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.customSpacing

/**
 * HeadlineWidget - A flexible composable for displaying a headline section with optional leading, middle, sub-middle, and trailing content.
 *
 * This widget is typically used at the top of screens or sections to provide a prominent title,
 * optional subtitle, and optional leading/trailing actions or icons.
 *
 * @param modifier Modifier to be applied to the Row container.
 * @param leadingText Optional string resource for leading text (e.g., section label).
 * @param leadingStr Optional direct string for leading text.
 * @param middleText Optional string resource for the main headline.
 * @param middleTextStr Optional direct string for the main headline.
 * @param subMiddleText Optional string resource for the sub-headline (subtitle).
 * @param subMiddleTextStr Optional direct string for the sub-headline.
 * @param trailing Optional composable for trailing content (e.g., action button or icon).
 * @param verticalAlignment Alignment for the row's vertical axis.
 *
 * ## Usage:
 * ```kotlin
 * HeadlineWidget(
 *     leadingText = R.string.section_label,
 *     middleText = R.string.headline,
 *     subMiddleText = R.string.subtitle,
 *     trailing = { Icon(...) }
 * )
 * ```
 */
@Composable
fun HeadlineWidget(
    modifier: Modifier = Modifier,
    @StringRes leadingText: Int? = null,
    leadingStr: String? = null,
    @StringRes middleText: Int? = null,
    middleTextStr: String? = null,
    @StringRes subMiddleText: Int? = null,
    subMiddleTextStr: String? = null,
    showLeadingComposable: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    leadingComposable: (@Composable () -> Unit) = {},
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    leadingTextStyle: TextStyle? = null
) {

    val windowSizeConstant = LocalWindowSizeConstant.current
    val customStyle = leadingTextStyle ?: windowSizeConstant.titleTextStyle.copy(shadow = Shadow())

    Row(
        modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = verticalAlignment
        ) {
            // Leading text or spacer
        val resolvedLeading = leadingText?.let { stringResource(it) } ?: leadingStr

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = verticalAlignment
        ) {

               if (showLeadingComposable) {
                   leadingComposable()
                   CustomSpacer(modifier = Modifier.width(windowSizeConstant.baseVerticalPadding))
               }

            if (resolvedLeading != null) {
                   Text(
                       text = resolvedLeading,
                       maxLines = 1,
                       style = customStyle,
                       overflow = TextOverflow.Ellipsis,
                       fontWeight = FontWeight.Bold,
                       color = MaterialTheme.colorScheme.primary,
                   )
               }
           }

            // Middle section: main headline and optional sub-headline
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val resolvedMiddle = middleText?.let { stringResource(it) } ?: middleTextStr
                if (!resolvedMiddle.isNullOrBlank()) {
                    Text(
                        text = resolvedMiddle,
                        maxLines = 4,
                        style = windowSizeConstant.titleTextStyle,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseVerticalPadding))

                val resolvedSubMiddle = subMiddleText?.let { stringResource(it) } ?: subMiddleTextStr
                if (!resolvedSubMiddle.isNullOrBlank()) {
                    Text(
                        text = resolvedSubMiddle,
                        style = windowSizeConstant.bodyTextStyle,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Trailing composable or spacer
        trailing?.invoke() ?: Spacer(modifier = Modifier.width(customSpacing.custom0))
    }
}

