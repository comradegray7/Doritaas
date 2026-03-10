package com.example.myapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Constants data class that holds commonly used numeric values throughout the app.
 * This class is marked as @Immutable to ensure thread safety and prevent accidental modifications.
 * 
 * These constants are typically used for:
 * - Animation durations and delays
 * - UI spacing and sizing
 * - Default values for various components
 */
@Immutable
/**
 * Constants
 *
 * Data class representing [TODO: Add description]
 */
data class Constants(
    /** Default value representing "none" or zero state */
    val none: Int = 0,
    /** Single unit value, commonly used for minimal spacing or counts */
    val one: Int = 1,
    /** Standard spacing unit, often used for padding and margins */
    val five: Int = 5,
    val tween1: Int = 200,
    val tween2: Int = 300,
    val twee3: Int = 800,
    val tween8: Int = 3000,

    /** Animation duration for quick transitions (1.5 seconds) */
    val tween: Int = 1500,
    /** Animation duration for medium-paced transitions (2 seconds) */
    val tweenMedium: Int = 2000,
    /** Quick animation duration (5 milliseconds) */
    val tweenNormal: Int = 5,
    /** Boundary or limit value, often used for validation */
    val bound: Int = 5,
    /** Animation duration for single-step animations */
    val animationOne: Int = 5,
)

/**
 * CompositionLocal that provides access to Constants throughout the composition tree.
 * This allows any composable in the hierarchy to access these constants without
 * explicitly passing them as parameters.
 */
val LocalConstants = staticCompositionLocalOf { Constants() }

/**
 * Extension property on MaterialTheme that provides easy access to Constants.
 * 
 * Usage example:
 * ```
 * val constants = MaterialTheme.constant
 * val spacing = constants.five
 * ```
 * 
 * This property is marked as @Composable and @ReadOnlyComposable to ensure
 * it's only called from composable functions and doesn't trigger recomposition.
 */
val constant: Constants
    @Composable
    @ReadOnlyComposable
    get() = LocalConstants.current
