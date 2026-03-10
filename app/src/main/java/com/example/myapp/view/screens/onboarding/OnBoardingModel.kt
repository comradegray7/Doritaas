package com.example.myapp.view.screens.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.myapp.R

/**
 * OnBoardingModel - Sealed class representing onboarding screen data.
 * 
 * This sealed class defines the structure for onboarding screen content,
 * including images, titles, and descriptions. Each object represents
 * a specific onboarding screen with its associated resources.
 *
 * @param android.R.id.title String resource ID for the screen's title
 * @param android.R.attr.description String resource ID for the screen's description text
 */
/**
 * OnBoardingModel
 */
sealed class OnBoardingModel (
    @param:DrawableRes val image: Int, // Illustration image for the onboarding screen
    @param:StringRes val title: Int, // Title text for the onboarding screen
    @param:StringRes val description: Int // Description text for the onboarding screen
) {
    /**
     * FirstScreen - Welcome screen for new users.
     * 
     * Introduces users to the app with a welcoming message and
     * sets expectations for the onboarding experience.
     */
    object FirstScreen : OnBoardingModel(
        image = R.drawable.thrilled, // Thrilled/enthusiastic illustration
        title = R.string.welcome_title, // Welcome message
        description = R.string.welcome_subtitle // Welcome subtitle/description
    )
    
    /**
     * SecondScreen - AI shopping features introduction.
     * 
     * Highlights the app's AI-powered shopping features and
     * demonstrates the value proposition to users.
     */

    object SecondScreen : OnBoardingModel(
        image = R.drawable.get_started, // Get started illustration
        title = R.string.create_account_now_title, // Account creation title
        description = R.string.create_account_now_subtitle // Account creation description
    )
}