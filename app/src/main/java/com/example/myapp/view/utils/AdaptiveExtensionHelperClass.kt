package com.example.myapp.view.utils

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

/**
 * WINDOW SIZE EXTENSION PROPERTIES
 * 
 * These extension properties provide convenient boolean checks for window size classes,
 * making it easier to implement responsive design patterns throughout the app.
 * They follow Material Design 3's window size class system.
 */

// Width-based size class extensions

/**
 * Checks if the window width is classified as Compact (e.g., standard phone in portrait).
 */
val WindowSizeClass.isCompact: Boolean
    get() = this.widthSizeClass == WindowWidthSizeClass.Compact

/**
 * Checks if the window width is classified as Medium (e.g., tablet in portrait or large phone in landscape).
 */
val WindowSizeClass.isMedium: Boolean
    get() = this.widthSizeClass == WindowWidthSizeClass.Medium

/**
 * Checks if the window width is classified as Expanded (e.g., tablet in landscape or desktop).
 */
val WindowSizeClass.isExpanded: Boolean
    get() = this.widthSizeClass == WindowWidthSizeClass.Expanded

