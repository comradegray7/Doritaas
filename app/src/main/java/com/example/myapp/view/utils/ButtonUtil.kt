package com.example.myapp.view.utils

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ButtonIcon - Sealed class representing different types of button icons.
 * 
 * This sealed class provides a type-safe way to handle different icon types
 * that can be used in buttons throughout the app. It supports both drawable
 * resources and vector icons.
 */
// Sealed class to represent different icon types
sealed class ButtonIcon {
    /**
     * Resource - Represents a drawable resource icon.
     * 
     * @param drawableRes The resource ID of the drawable to be used as an icon
     */
    data class Resource(@param:DrawableRes val drawableRes: Int) : ButtonIcon()
    
    /**
     * Vector - Represents a vector icon.
     * 
     * @param imageVector The ImageVector to be used as an icon
     */
    data class Vector(val imageVector: ImageVector) : ButtonIcon()


    /**
     * None - Represents an empty icon state.
     *
     * Used when no icon is needed for the button.
     */
    object None : ButtonIcon()
}
