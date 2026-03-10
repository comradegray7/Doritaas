package com.example.myapp.view.utils

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * LocalWindowSizeClass - CompositionLocal for providing WindowSizeClass throughout the composition tree.
 * 
 * This CompositionLocal allows any composable in the hierarchy to access the current
 * window size class without explicitly passing it as a parameter. It's used for
 * implementing responsive design patterns across the entire app.
 * 
 * The error message provides a helpful reminder to wrap the UI with CompositionLocalProvider
 * if the WindowSizeClass hasn't been provided.
 */
val LocalWindowSizeClass = staticCompositionLocalOf<WindowSizeClass> {
    error("WindowSizeClass not provided. Make sure to wrap your UI with CompositionLocalProvider.")
}
