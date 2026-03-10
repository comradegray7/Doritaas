package com.example.myapp.view.screens.product.product_rating_and_reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.myapp.R
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.colors
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.CustomSpacer
import com.example.myapp.view.components.CustomTextField
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.ButtonIcon

/**
 * ProductRatingDialog - Dialog for submitting or updating a product review.
 *
 * Allows the user to select a star rating and optionally enter a text review.
 * If a previous rating exists, it pre-populates the dialog for updating.
 *
 * @param productName The name of the product being rated (optional).
 * @param currentUserRating The user's existing rating (0f if none).
 * @param currentReviewText The user's existing review text ("" if none).
 * @param onDismiss Callback to dismiss the dialog.
 * @param onSubmitRating Callback invoked with the selected rating and review text.
 */
@Composable
fun ProductRatingDialog(
    productName: String? = null,
    currentUserRating: Float = 0f,
    currentReviewText: String = "",
    onDismiss: () -> Unit = { },
    onSubmitRating: (Float, String?) -> Unit = { _, _ -> },
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    var selectedRating by remember(currentUserRating) {
        mutableFloatStateOf(currentUserRating)
    }
    var reviewText by remember(currentReviewText) {
        mutableStateOf(currentReviewText)
    }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (currentUserRating > 0f) {
                    stringResource(R.string.update_review)
                } else {
                    stringResource(R.string.rate_product)
                },
                style = windowSizeConstant.titleTextStyle
                )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (productName != null) {
                    Text(
                        productName,
                        style = windowSizeConstant.bodyTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.normalVerticalPadding))

                // Interactive star rating
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(5) { index ->
                        ButtonIconComposable(
                            showBgColor = false,
                            buttonIcon = ButtonIcon.Vector(
                                if (index < selectedRating)
                                    Icons.Filled.Star
                                else
                                    Icons.Outlined.StarBorder
                            ),
                            onClick = { selectedRating = (index + 1).toFloat() },
                            contentDescription = "Search",
                            tint = if (index < selectedRating)
                                colors.customColor6
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(windowSizeConstant.largeIconSize)
                        )

                    }
                }

                if (selectedRating > 0) {
                    Text(
                        "${selectedRating.toInt()} star${if (selectedRating > 1) "s" else ""}",
                        style = windowSizeConstant.bodyTextStyle,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                CustomSpacer()

                // Optional review text
                CustomTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = R.string.be_first_reviews,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    minLines = 2
                )
            }
        },
        confirmButton = {
            CustomTextButton(
                onClick = {
                    onSubmitRating(selectedRating, reviewText.ifBlank { null })
                },
                enabled = selectedRating > 0,
                strLabel = if (currentUserRating > 0f) {
                    stringResource(R.string.update)
                } else {
                    stringResource(R.string.submit)
                }
            )
        },
        dismissButton = {
            CustomTextButton(
                label = R.string.cancel,
                onClick = onDismiss,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
