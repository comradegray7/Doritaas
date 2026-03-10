package com.example.myapp.navigation.navigation_helper

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * NavigationAnimations - Object containing predefined navigation animations.
 * 
 * This object provides a collection of reusable animation transitions for navigation
 * between screens. It includes horizontal slides, vertical slides, and fade transitions
 * with consistent timing and easing.
 */
object NavigationAnimations {
    /** Duration for all navigation animations in milliseconds */
    private const val ANIMATION_DURATION = 300

    /**
     * Slide in from right, slide out to left
     * Used for forward navigation (pushing new screens)
     *
     * @return EnterTransition with slide and fade effect.
     */
    fun slideInFromRight(): EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth }, // Start from right edge
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))

    /**
     * Slide out to left.
     * Companion to [slideInFromRight] for the exiting screen.
     *
     * @return ExitTransition with slide and fade effect.
     */
    fun slideOutToLeft(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth }, // Exit to left edge
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))

    /**
     * Slide in from left, slide out to right (for back navigation)
     * Used when navigating back to previous screens
     *
     * @return EnterTransition with slide from left.
     */
    fun slideInFromLeft(): EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth }, // Start from left edge
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))

    /**
     * Slide out to right.
     * Companion to [slideInFromLeft] for the exiting screen during back navigation.
     *
     * @return ExitTransition with slide to right.
     */
    fun slideOutToRight(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth }, // Exit to right edge
        animationSpec = tween(ANIMATION_DURATION)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))

    /**
     * Cross fade for bottom bar navigation
     * Used for smooth transitions between bottom bar destinations
     *
     * @return EnterTransition with fade in.
     */
    fun crossFade(): EnterTransition = fadeIn(
        animationSpec = tween(ANIMATION_DURATION)
    )


    /**
     * Cross fade out.
     *
     * @return ExitTransition with fade out.
     */
    fun crossFadeOut(): ExitTransition = fadeOut(
        animationSpec = tween(ANIMATION_DURATION)
    )
}