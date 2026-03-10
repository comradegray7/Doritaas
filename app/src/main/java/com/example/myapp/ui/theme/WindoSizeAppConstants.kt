package com.example.myapp.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.pager.PageSize
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * WindowSizeAppConstants - Data class for adaptive UI constants based on window size.
 *
 * This class holds all the adaptive constants (dimensions, paddings, text styles, etc.)
 * used throughout the app for responsive layouts. It is provided via CompositionLocal
 * and allows the UI to adapt to different device sizes and orientations.
 *
 * @property labelTextStyle Text style for labels
 * @property bodyTextStyle Text style for body text
 * @property iconSize Padding for icons
 * @property cardHeight Height for cards
 * @property logoPadding Size for logo images
 * @property listImagePadding Padding for images in lists
 * @property appBarPadding Padding for the app bar
 * @property contentPadding General content padding
 * @property productImageSize Size for product images
 * @property onBoardingImageSize Size for onboarding images
 * @property titleTextStyle Text style for titles
 * @property carouselPageSpacing Spacing for carousel pages
 * @property orDividerPadding Padding for "OR" dividers
 * @property contentVerticalPadding Vertical padding for content
 * @property baseVerticalPadding Base vertical padding
 * @property profileCardPadding Padding for profile cards
 * @property productSummaryImagePadding Padding for product summary images
 * @property carouselImageHeight Height for carousel images
 * @property carouselImageWidth Width for carousel images
 * @property adaptiveHeight Adaptive height for components
 * @property listRightPadding Right padding for lists
 * @property productCardHeight Height for product cards
 * @property listCardPadding Padding for list cards
 * @property adaptiveProfileVerticalSpacer Vertical spacer for profiles
 * @property adaptiveWidthModifier Modifier for adaptive width
 * @property adaptiveFormWidthModifier Modifier for adaptive form width
 * @property horizontalArrangementStyle Default horizontal arrangement
 */
@Immutable // Marked as immutable for performance optimizations with CompositionLocal
/**
 * WindowSizeAppConstants
 *
 * Data class representing [TODO: Add description]
 */
data class WindowSizeAppConstants(
    val labelTextStyle: TextStyle,
    val bodyTextStyle: TextStyle,
    val iconSize: Dp,
    val customImageHeight: Dp,
    val cardElevationPadding: Dp,
    val basePadding: Dp,
    val heroIconSize: Dp,
    val cardHeight: Dp,
    val customSpacerLarge: Dp,
    val customSpacerMedium: Dp,
    val baseIconSize : Dp,
    val largeIconSize  : Dp,
    val baseSize  : Dp,
    val customSpacerSmall : Dp,
    val mediumIconSize: Dp,
    val carouselPageSize: PageSize,
    val logoPadding: DpSize,
    val listImagePadding: DpSize,
    val productCardPaddings: DpSize,
    val appBarPadding: Dp,
    val smallSizes: Dp,
    val onBoardingPadding: Dp,
    val  topBarPadding: Dp,
    val borderSize: Dp,
    val contentPadding: Dp,
    val productImageSize: Modifier,
    val onBoardingAdaptiveWidth: Modifier,
    val onBoardingImageSize: Modifier,
    val titleTextStyle: TextStyle,
    val bottomBarLabelStyles: TextStyle,
    val carouselPageSpacing: Dp,
    val orDividerPadding: Dp,
    val contentVerticalPadding: Dp,
    val normalVerticalPadding: Dp,
    val smallVerticalPadding: Dp,
    val baseVerticalPadding: Dp,
    val baseNormalVerticalPadding: Dp,
    val profileCardPadding: DpSize,
    val adaptiveProductCardHeight: Dp,
    val productSummaryImagePadding: DpSize,
    val carouselCardHeight: Modifier,
    val carouselImageHeight: Dp,
    val carouselImageWidth: Dp,
    val customButtonPadding: Dp,
    val adaptiveHeight: Dp,
    val listRightPadding: Dp,
    val productCardHeight: Dp,
    val smallButtonWidth: Dp,
    val listCardPadding: Dp,
    val adaptiveProfileVerticalSpacer: Dp,
    val adaptiveWidthModifier: Modifier,
    val floatingSnackBarPaddings: Modifier,
    val adaptiveFormWidthModifier: Modifier,
    val adaptiveListCardWidthModifier: Modifier,
    val networkCardPadding: Modifier,
    val profileInfoPaddings: Modifier,
    val heroCardPadding: DpSize,
    val heroSectionPadding: DpSize,
    val horizontalArrangementStyle: Arrangement.Horizontal,
)

/**
 * LocalWindowSizeConstant - CompositionLocal for providing [WindowSizeAppConstants].
 *
 * This CompositionLocal is used to provide adaptive UI constants throughout the app.
 * It is initialized with default values and should be overridden at the app level.
 */
val LocalWindowSizeConstant = staticCompositionLocalOf {
    WindowSizeAppConstants(
        cardHeight = 0.dp,
        customButtonPadding = 0.dp,
        logoPadding = DpSize(0.dp, 0.dp),
        appBarPadding = 0.dp,
        contentVerticalPadding = 0.dp,
        listImagePadding = DpSize(0.dp, 0.dp),
        profileCardPadding = DpSize(0.dp, 0.dp),
        contentPadding = 0.dp,
        baseSize = 0.dp,
        baseNormalVerticalPadding = 0.dp,
        smallVerticalPadding = 0.dp,
        customImageHeight = 0.dp,
        heroIconSize = 0.dp,
        smallSizes = 0.dp,
        borderSize = 0.dp,
        onBoardingPadding = 0.dp,
        basePadding = 0.dp,
        topBarPadding = 0.dp,
        carouselPageSize = PageSize.Fill,
        productCardHeight = 0.dp,
        baseVerticalPadding = 0.dp,
        titleTextStyle = TextStyle(),
        carouselPageSpacing = 0.0.dp,
        onBoardingImageSize = Modifier,
        horizontalArrangementStyle = Arrangement.SpaceBetween,
        iconSize = 24.dp,
        adaptiveProductCardHeight = 0.dp,
        labelTextStyle = TextStyle(),
        bottomBarLabelStyles = TextStyle(),
        bodyTextStyle = TextStyle(),
        floatingSnackBarPaddings = Modifier,
        onBoardingAdaptiveWidth = Modifier,
        networkCardPadding = Modifier,
        productImageSize = Modifier,
        heroCardPadding = DpSize(0.dp, 0.dp),
        heroSectionPadding = DpSize(0.dp, 0.dp),
        orDividerPadding = 0.dp,
        customSpacerLarge = 0.dp,
        carouselImageWidth = 0.dp,
        carouselCardHeight = Modifier,
        carouselImageHeight = 0.dp,
        smallButtonWidth = 0.dp,
        listRightPadding = 0.dp,
        adaptiveHeight = 0.dp,
        cardElevationPadding = 0.dp,
        listCardPadding = 0.dp,
        baseIconSize = 0.dp,
        customSpacerSmall = 0.dp,
        customSpacerMedium = 0.dp,
        largeIconSize  = 0.dp,
        mediumIconSize = 0.dp,
        normalVerticalPadding = 0.dp,
        adaptiveWidthModifier = Modifier,
        adaptiveListCardWidthModifier = Modifier,
        profileInfoPaddings = Modifier,
        adaptiveFormWidthModifier = Modifier,
        adaptiveProfileVerticalSpacer = 0.dp,
        productSummaryImagePadding = DpSize(0.dp, 0.dp),
        productCardPaddings = DpSize(0.dp, 0.dp)
    )
}

