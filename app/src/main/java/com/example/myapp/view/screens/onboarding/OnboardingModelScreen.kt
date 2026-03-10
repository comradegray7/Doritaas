package com.example.myapp.view.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomImageContainer
import com.example.myapp.view.components.CustomSpacer

/**
 * OnboardingModelScreen - Composable for displaying individual onboarding screen content.
 *
 * This composable renders a single onboarding screen with an image, title, and description.
 * It uses adaptive sizing based on window size constants for responsive design.
 *
 * @param modifier Optional modifier to apply to the screen content
 * @param onBoardingModel The onboarding model containing screen data (image, title, description)
 *
 * Usage:
 * ```
 * OnboardingModelScreen(
 *     onBoardingModel = OnBoardingModel.FirstScreen
 * )
 * ```
 */
@Composable
        /**
         * OnboardingModelScreen
         *
         *
         * @param modifier The modifier parameter
         * @param onBoardingModel The onBoardingModel parameter
         */
fun OnboardingModelScreen(modifier : Modifier = Modifier, onBoardingModel : OnBoardingModel) {


    val windowSizeConstant = LocalWindowSizeConstant.current // Get adaptive window size constants

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) { // Center content horizontally

            // Display onboarding illustration/image
            CustomImageContainer(
                modifier = windowSizeConstant.onBoardingImageSize.then(
                    modifier.height(
                        windowSizeConstant.customImageHeight
                    )
                ), // Adaptive image size
                data = onBoardingModel.image, // Image resource from model
                contentDescription = null, // No accessibility description needed for decorative images
            )

            CustomSpacer() // Add spacing between image and text

            // Text content section
            Column(
                verticalArrangement = Arrangement.Center, // Center content vertically
                horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
            ) {
                    // Main title text
                Text(
                    text = stringResource(onBoardingModel.title),
                    style = windowSizeConstant.titleTextStyle, // Use adaptive headline style,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                CustomSpacer(modifier = Modifier.height(windowSizeConstant.baseVerticalPadding)) // Small spacing between title and description

                    // Description text
                Text(
                    text = stringResource(onBoardingModel.description),
                    style = windowSizeConstant.bodyTextStyle, // Use adaptive sub-headline style
                        color = MaterialTheme.colorScheme.outline, // Use outline color for secondary text
                    textAlign = TextAlign.Center, // Center align the description text,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                    )

                CustomSpacer() // Add spacing between image and text
            }
        }
}
