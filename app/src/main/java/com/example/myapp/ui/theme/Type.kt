package com.example.myapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.myapp.R

/**
 * TYPOGRAPHY SYSTEM
 * 
 * This file defines the typography system for the app using Google Fonts
 * and Material Design 3 typography principles. It provides consistent
 * text styling across the entire application.
 */

// Google Fonts provider configuration for loading custom fonts
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts", // Google Fonts authority
    providerPackage = "com.google.android.gms", // Google Play Services package
    certificates = R.array.com_google_android_gms_fonts_certs // Font certificates
)

// Body font family using Roboto for general text content
val bodyFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Roboto"), // Roboto font for body text
        fontProvider = provider, // Use the configured font provider
    )
)

// Display font family using Roboto for headings and titles
val displayFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Roboto"), // Roboto font for display text
        fontProvider = provider, // Use the configured font provider
    )
)

// Default Material 3 typography values as baseline
val baseline = Typography()

/**
 * AppTypography - Custom typography configuration for the app.
 * 
 * This Typography object extends Material Design 3 typography with custom
 * font families and specific text styles. It provides consistent text
 * styling that can be used throughout the app.
 */
val AppTypography = Typography(
    // Display styles - Large, prominent text for headlines
    bodyLarge =  baseline.bodyLarge.copy(fontFamily = displayFontFamily),
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
    
    // Headline styles - Section headers and important titles
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
    headlineSmall =  baseline.headlineSmall.copy(fontFamily = displayFontFamily),
    
    // Title styles - Page titles and section headers
    titleMedium = baseline.titleLarge.copy(fontFamily = displayFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
    // titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily), // Commented out custom style
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
    
    // Body styles - Main content text
    bodyMedium = TextStyle(
        fontFamily = displayFontFamily,
        lineHeight = 20.sp, // 20sp line height for readability
        fontWeight = FontWeight.W400, // Regular weight for body text
        fontSize = 14.sp // 14sp font size
    ),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
    
    // Label styles - UI labels and small text
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
)

