package com.example.myapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing Design System
 * 
 * This file defines a comprehensive spacing system for the application using a custom naming convention.
 * The spacing values are organized in a progressive scale from smallest to largest, providing
 * consistent spacing throughout the UI.
 * 
 * ## Naming Convention
 * All spacing values use the `custom*` prefix followed by the dp value (e.g., `custom8` = 8.dp).
 * Special cases use descriptive suffixes (e.g., `customHalf` = 1.5.dp, `customZero2` = 0.2.dp).
 * 
 * ## Organization
 * Values are grouped into four categories:
 * - **Small values** (0dp - 90dp): For fine-grained spacing, padding, and borders
 * - **Medium values** (100dp - 180dp): For component sizing and moderate spacing
 * - **Large values** (200dp - 400dp): For major layout spacing and large components
 * - **Extra large values** (420dp - 680dp): For full-width components and special layouts
 * 
 * ## Usage
 * Access spacing values via the `customSpacing` composable property:
 * ```kotlin
 * @Composable
 /**
  * MyComponent
  *
  * TODO: Add detailed description of what this function does.
  */
 * fun MyComponent() {
 *     Box(modifier = Modifier.padding(customSpacing.custom16)) {
 *         // Content with 16dp padding
 *     }
 * }
 * ```
 * 
 * ## Legacy Mapping
 * Comments show the original semantic names for reference:
 * - custom0 (none), custom8 (small), custom12 (normal), custom16 (medium)
 * - custom20 (base), custom24 (large), custom48 (outline), custom80 (mediumLarge)
 * 
 * @see LocalSpacing for the CompositionLocal provider
 * @see customSpacing for the composable accessor
 */
@Immutable
/**
 * Spacing
 *
 * Data class representing [TODO: Add description]
 */
data class Spacing(
    // ============================================
    // SMALL VALUES (0dp - 90dp)
    // Used for: Fine spacing, borders, small padding, icon sizes
    // ============================================
    
    /** 0dp - No spacing. */
    val custom0: Dp = 0.dp,

    /** 0dp - No spacing. */
    val customZero8: Dp = 0.8.dp,
    
    /** 1dp - Thin border.  */
    val custom1: Dp = 1.dp,
    
    /** 1.5dp - Standard border width.  */
    val customHalf: Dp = 1.5.dp,
    
    /** 0.2dp - Minimal spacing for elevation effects.  */
    val customZero2: Dp = 0.2.dp,
    
    /** 2dp - Base spacer for very tight spacing. */
    val custom2: Dp = 2.dp,
    
    /** 2.5dp - Subtle spacing increment.  */
    val custom2Five: Dp = 2.5.dp,

    /** 5dp - Subtle spacing increment.  */
    val custom5: Dp = 5.dp,

    /** 4dp - Extra small spacing for compact layouts.  */
    val custom4: Dp = 4.dp,

      /** 3dp - Extra small spacing for compact layouts.  */
    val custom3: Dp = 3.dp,

    /** 6dp - Smaller spacing for tight grouping.  */
    val custom6: Dp = 6.dp,
    
    /** 8dp - Small spacing, common for padding. */
    val custom8: Dp = 8.dp,
    
    /** 10dp - Custom normal spacing.  */
    val custom10: Dp = 10.dp,
    
    /** 12dp - Normal/standard spacing for most UI elements. */
    val custom12: Dp = 12.dp,
    
    /** 14dp - Base normal spacing.  */
    val custom14: Dp = 14.dp,
    
    /** 16dp - Medium spacing, very common for padding.*/
    val custom16: Dp = 16.dp,
    
    /** 18dp - Normal large spacing. */
    val custom18: Dp = 18.dp,
    
    /** 20dp - Base spacing for standard layouts.  */
    val custom20: Dp = 20.dp,
    
    /** 22dp - Base small-large spacing. */
    val custom22: Dp = 22.dp,
    
    /** 24dp - Large spacing for generous padding.  */
    val custom24: Dp = 24.dp,
    
    /** 28dp - Box spacing for containers.  */
    val custom28: Dp = 28.dp,
    
    /** 30dp - Outline normal spacing. */
    val custom30: Dp = 30.dp,
    
    /** 32dp - Base medium spacing. */
    val custom32: Dp = 32.dp,
    
    /** 34dp - Custom spacing value */
    val custom34: Dp = 34.dp,
    
    /** 35dp - Custom spacing value */
    val custom35: Dp = 35.dp,
    
    /** 36dp - Outline medium spacing.*/
    val custom36: Dp = 36.dp,
    
    /** 40dp - Box medium spacing for medium containers.*/
    val custom40: Dp = 40.dp,
    
    /** 42dp - Custom spacing value */
    val custom42: Dp = 42.dp,
    
    /** 44dp - Custom spacing value */
    val custom44: Dp = 44.dp,
    
    /** 45dp - Custom spacing value */
    val custom45: Dp = 45.dp,
    
    /** 48dp - Outline/touch target spacing. */
    val custom48: Dp = 48.dp,
    
    /** 50dp - Base large spacing. */
    val custom50: Dp = 50.dp,
    
    /** 51dp - Custom spacing value */
    val custom51: Dp = 51.dp,
    
    /** 52dp - Custom spacing value for specific components */
    val custom52: Dp = 52.dp,
    
    /** 53dp - Custom spacing value for specific components */
    val custom53: Dp = 53.dp,
    
    /** 55dp - Custom spacing value */
    val custom55: Dp = 55.dp,
    
    /** 60dp - Custom medium spacing for components. */
    val custom60: Dp = 60.dp,
    
    /** 64dp - Medium height for components. */
    val custom64: Dp = 64.dp,
    
    /** 65dp - Standard height for certain components. */
    val custom65: Dp = 65.dp,
    
    /** 70dp - Outline large spacing.  */
    val custom70: Dp = 70.dp,
    
    /** 75dp - Custom spacing value */
    val custom75: Dp = 75.dp,
    
    /** 76dp - Custom spacing value */
    val custom76: Dp = 76.dp,
    
    /** 80dp - Medium-large spacing for major sections.  */
    val custom80: Dp = 80.dp,
    
    /** 86dp - Custom spacing value */
    val custom86: Dp = 86.dp,
    
    /** 90dp - Custom spacing value */
    val custom90: Dp = 90.dp,

    // ============================================
    // MEDIUM VALUES (100dp - 180dp)
    // Used for: Component dimensions, moderate spacing
    // ============================================
    
    /** 100dp - Extra extra large spacing. */
    val custom100: Dp = 100.dp,
    
    /** 108dp - Custom component dimension */
    val custom108: Dp = 108.dp,
    
    /** 110dp - Custom component dimension */
    val custom110: Dp = 110.dp,
    
    /** 114dp - Custom component dimension */
    val custom114: Dp = 114.dp,
    
    /** 120dp - Box large spacing for large containers. */
    val custom120: Dp = 120.dp,
    
    /** 124dp - Custom component dimension */
    val custom124: Dp = 124.dp,
    
    /** 126dp - Custom component dimension */
    val custom126: Dp = 126.dp,
    
    /** 130dp - Custom component dimension */
    val custom130: Dp = 130.dp,
    
    /** 140dp - Custom spacing for larger components */
    val custom140: Dp = 140.dp,
    
    /** 150dp - Custom spacing value. */
    val custom150: Dp = 150.dp,
    
    /** 160dp - Custom component dimension */
    val custom160: Dp = 160.dp,

     /** 170dp - Custom component dimension */
    val custom170: Dp = 170.dp,

    /** 180dp - Large spacing for major sections.  */
    val custom180: Dp = 180.dp,

 /** 190dp - Large spacing for major sections.  */
    val custom190: Dp = 190.dp,

    // ============================================
    // LARGE VALUES (200dp - 400dp)
    // Used for: Major layout spacing, large component dimensions
    // ============================================
    
    /** 200dp - Large component dimension */
    val custom200: Dp = 200.dp,

    /** 210dp - Large component dimension */
    val custom210: Dp = 210.dp,

    /** 220dp - Large component dimension */
    val custom220: Dp = 220.dp,
    
    /** 240dp - Drawer/large container spacing.  */
    val custom240: Dp = 240.dp,
    
    /** 260dp - Extra extra extra large spacing.*/
    val custom260: Dp = 260.dp,
    
    /** 280dp - Drawer extra large spacing. */
    val custom280: Dp = 280.dp,
    
    /** 300dp - Very large spacing for major layouts.*/
    val custom300: Dp = 300.dp,
    
    /** 310dp - Custom large dimension */
    val custom310: Dp = 310.dp,
    
    /** 320dp - Custom large dimension */
    val custom320: Dp = 320.dp,
    
    /** 330dp - Custom large dimension */
    val custom330: Dp = 330.dp,
    
    /** 340dp - Custom large dimension */
    val custom340: Dp = 340.dp,
    
    /** 360dp - Custom large dimension */
    val custom360: Dp = 360.dp,
    
    /** 380dp - Custom large dimension */
    val custom380: Dp = 380.dp,
    
    /** 400dp - Extra large spacing for full sections.  */
    val custom400: Dp = 400.dp,

    // ============================================
    // EXTRA LARGE VALUES (420dp - 680dp)
    // Used for: Full-width components, special large layouts
    // ============================================
    
    /** 420dp - Extra large component dimension */
    val custom420: Dp = 420.dp,
    
    /** 428dp - Specific large component dimension */
    val custom428: Dp = 428.dp,
    
    /** 440dp - Extra large component dimension */
    val custom440: Dp = 440.dp,
    
    /** 480dp - Very large component dimension */
    val custom480: Dp = 480.dp,
    
    /** 500dp - Maximum width for certain full-width components */
    val custom500: Dp = 500.dp,

    /** 680dp - Maximum width for certain full-width components */
    val custom680: Dp = 680.dp,

  /** 600dp - Maximum width for certain full-width components */
    val custom600: Dp = 600.dp,

    /** 800dp - Maximum width for certain full-width components */
    val custom800: Dp = 800.dp
)

/**
 * CompositionLocal for providing Spacing throughout the composition tree.
 * 
 * This allows spacing values to be accessed from any composable function
 * without explicit parameter passing.
 */
val LocalSpacing = staticCompositionLocalOf { Spacing() }

/**
 * Composable property to access the current Spacing values.
 * 
 * Usage:
 * ```kotlin
 * @Composable
 * fun MyComponent() {
 *     val spacing = customSpacing
 *     Box(modifier = Modifier.padding(spacing.custom16))
 * }
 * ```
 * 
 * Or directly:
 * ```kotlin
 * Box(modifier = Modifier.padding(customSpacing.custom16))
 * ```
 */
val customSpacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
