package com.example.myapp.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PageSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.sp
import com.example.myapp.view.utils.LocalWindowSizeClass
import com.example.myapp.view.utils.isCompact
import com.example.myapp.view.utils.isExpanded
import com.example.myapp.view.utils.isMedium

/**
 * Adaptive UI Constants Provider
 * 
 * This file implements a comprehensive responsive design system that adapts UI elements
 * based on the device's window size class, following Material Design 3 guidelines.
 * 
 * ## Window Size Classes
 * The system uses three main breakpoints:
 * - **Compact**: Phones in portrait mode (< 600dp width)
 * - **Medium**: Tablets in portrait, phones in landscape (600dp - 840dp width)
 * - **Expanded**: Tablets in landscape, desktops (> 840dp width)
 * 
 * ## Adaptive Elements
 * The following UI elements adapt to screen size:
 * 
 * ### Typography
 * - Price, headline, label, body, and subtitle text styles
 * - Scales from smaller on compact to larger on expanded screens
 * 
 * ### Layout Dimensions
 * - Card heights, padding values, component sizes
 * - Optimized for readability and usability on each screen size
 * 
 * ### Spacing & Alignment
 * - Horizontal/vertical padding, content spacing
 * - Alignment and arrangement strategies
 * 
 * ### Component Sizing
 * - Images, icons, badges, buttons
 * - Carousel dimensions, product cards, profile elements
 * 
 * ## Usage
 * Wrap your app content with this provider at the root level:
 * ```kotlin
 * ProvideAppConstants {
 *     // Your app content
 *     MyApp()
 * }
 * ```
 * 
 * Access constants via `LocalWindowSizeConstant.current`:
 * ```kotlin
 * @Composable
 /**
  * MyScreen
  *
  */
 * fun MyScreen() {
 *     val constants = LocalWindowSizeConstant.current
 *     Text(
 *         text = "Hello",
 *         style = constants.appHeadLineTextStyle,
 *         modifier = Modifier.padding(constants.contentPadding)
 *     )
 * }
 * ```
 * 
 * ## Design Philosophy
 * - **Mobile-first**: Compact sizes are optimized for one-handed use
 * - **Progressive enhancement**: Medium and expanded sizes add more content and features
 * - **Consistency**: All adaptive values follow a coherent scaling system
 * - **Performance**: Values are computed once per window size change
 * 
 * @param content The composable content that will have access to adaptive constants
 /**
  * containing
  *
  */
 * @see WindowSizeAppConstants for the data class containing all adaptive values
 * @see LocalWindowSizeConstant for the CompositionLocal provider
 */

@Composable

fun ProvideAppConstants(
    content: @Composable () -> Unit
) {
    // Get the current window size class to determine which constants to use
    val windowSizeClass = LocalWindowSizeClass.current

    /**
     * TYPOGRAPHY STYLES
     * Adaptive text styles that scale based on screen size
     */

    // Label text style - used for form labels and small descriptive text
    val labelTextStyle = when{
        windowSizeClass.isCompact -> MaterialTheme.typography.labelLarge
        windowSizeClass.isMedium -> MaterialTheme.typography.labelLarge
        windowSizeClass.isExpanded -> MaterialTheme.typography.labelLarge
        else -> MaterialTheme.typography.labelLarge
    }

    // main screen bottom bar text style - used for form labels and small descriptive text
    val bottomBarLabelStyles = when{
        windowSizeClass.isCompact -> MaterialTheme.typography.labelLarge
        windowSizeClass.isMedium -> MaterialTheme.typography.bodyLarge
        windowSizeClass.isExpanded -> MaterialTheme.typography.bodyLarge
        else -> MaterialTheme.typography.labelLarge
    }

    // Body text style - used for main content text
    val bodyTextStyle = when{
        windowSizeClass.isCompact -> MaterialTheme.typography.bodyLarge
        windowSizeClass.isMedium -> MaterialTheme.typography.bodyLarge
        windowSizeClass.isExpanded -> MaterialTheme.typography.titleMedium.copy(
            fontSize = 18.sp
        )

        else -> MaterialTheme.typography.bodyLarge
    }

    /**
     * LAYOUT DIMENSIONS
     * Adaptive sizing for various UI components
     */

    val smallButtonWidth = when {
        windowSizeClass.isCompact -> customSpacing.custom100
        windowSizeClass.isMedium -> customSpacing.custom110
        windowSizeClass.isExpanded -> customSpacing.custom120
        else -> customSpacing.custom100
    }

    // Card height - used for product cards and similar containers
    val cardHeight = when {
        windowSizeClass.isCompact -> customSpacing.custom300
        windowSizeClass.isMedium -> customSpacing.custom320
        windowSizeClass.isExpanded -> customSpacing.custom340
        else -> customSpacing.custom300
    }

    val productCardPaddings = when {
        windowSizeClass.isCompact -> DpSize(customSpacing.custom190, customSpacing.custom320)
        windowSizeClass.isMedium -> DpSize(customSpacing.custom210, customSpacing.custom360)
        windowSizeClass.isExpanded -> DpSize(customSpacing.custom210, customSpacing.custom360)
        else -> DpSize(customSpacing.custom180, customSpacing.custom320)
    }

    // Profile vertical spacer - spacing for profile-related layouts
    val adaptiveProfileVerticalSpacer = when {
        windowSizeClass.isCompact -> customSpacing.custom80
        windowSizeClass.isMedium -> customSpacing.custom80
        windowSizeClass.isExpanded -> customSpacing.custom100
        else -> customSpacing.custom80
    }

    // Profile card padding - dimensions for profile card containers
    val profileCardPadding = when {
        windowSizeClass.isCompact -> DpSize(customSpacing.custom360, customSpacing.custom200)
        windowSizeClass.isMedium -> DpSize(customSpacing.custom420, customSpacing.custom200)
        windowSizeClass.isExpanded -> DpSize(customSpacing.custom440, customSpacing.custom220)
        else -> DpSize(customSpacing.custom360, customSpacing.custom180)
    }

    // List image padding - sizing for images in list items
    val listImagePadding = when {
        windowSizeClass.isCompact -> DpSize(customSpacing.custom120, customSpacing.custom140)
        windowSizeClass.isMedium -> DpSize(customSpacing.custom120, customSpacing.custom140)
        windowSizeClass.isExpanded -> DpSize(customSpacing.custom160, customSpacing.custom160)
        else -> DpSize(customSpacing.custom20, customSpacing.custom140)
    }

    //logo image padding - sizing for logo image
    val logoPadding = when {
        windowSizeClass.isCompact -> DpSize(customSpacing.custom86, customSpacing.custom80)
        windowSizeClass.isMedium -> DpSize(customSpacing.custom120, customSpacing.custom100)
        windowSizeClass.isExpanded -> DpSize(customSpacing.custom130, customSpacing.custom120)
        else -> DpSize(customSpacing.custom86, customSpacing.custom80)
    }

    // List right padding - right margin for list items
    val listRightPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom12
        windowSizeClass.isMedium -> customSpacing.custom0
        windowSizeClass.isExpanded -> customSpacing.custom0
        else -> customSpacing.custom0
    }

    /**
     * CAROUSEL STYLES
     * Adaptive styling for carousel/slider components
     */

    // Carousel page spacing - gap between carousel items
    val carouselPageSpacing = when {
        windowSizeClass.isCompact -> customSpacing.custom16
        windowSizeClass.isMedium -> customSpacing.custom12
        windowSizeClass.isExpanded -> customSpacing.custom12
        else -> customSpacing.custom16
    }

    // Carousel card height - height of images in carousel
    val carouselCardHeight = when {
        windowSizeClass.isCompact ->  Modifier.fillMaxHeight()
        windowSizeClass.isMedium ->   Modifier.fillMaxHeight()
        windowSizeClass.isExpanded ->  Modifier.fillMaxHeight()
        else ->  Modifier.fillMaxHeight()
    }

    val carouselPageSize  = when {
        windowSizeClass.isCompact -> PageSize.Fixed(customSpacing.custom260)
        windowSizeClass.isMedium ->   PageSize.Fixed(customSpacing.custom280)
        windowSizeClass.isExpanded ->  PageSize.Fixed(customSpacing.custom280)
        else ->  PageSize.Fixed(customSpacing.custom260)
    }

    // Carousel image height - height of images in carousel
    val carouselImageHeight = when {
        windowSizeClass.isCompact -> customSpacing.custom130
        windowSizeClass.isMedium -> customSpacing.custom140
        windowSizeClass.isExpanded -> customSpacing.custom140
        else -> customSpacing.custom130
    }

    // Carousel image width - width of images in carousel
    val carouselImageWidth = when {
        windowSizeClass.isCompact -> customSpacing.custom280
        windowSizeClass.isMedium -> customSpacing.custom380
        windowSizeClass.isExpanded -> customSpacing.custom380
        else -> customSpacing.custom280
    }

    /**
     * CONTENT PADDING STYLES
     * Adaptive padding for content containers
     */

    // Content horizontal padding - horizontal spacing for main content
    val contentPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom16
        windowSizeClass.isMedium -> customSpacing.custom20
        windowSizeClass.isExpanded -> customSpacing.custom50
        else -> customSpacing.custom16
    }

    // top bar and bottom bar horizontal padding - horizontal spacing for top bar and bottom bar content
    val appBarPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom0
        windowSizeClass.isMedium -> customSpacing.custom20
        windowSizeClass.isExpanded -> customSpacing.custom50
        else -> customSpacing.custom0
    }

    val cardElevationPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom2
        windowSizeClass.isMedium -> customSpacing.custom4
        windowSizeClass.isExpanded -> customSpacing.custom6
        else -> customSpacing.custom2
    }

    // Content vertical padding - vertical spacing for main content
    val basePadding = when {
        windowSizeClass.isCompact -> customSpacing.custom16
        windowSizeClass.isMedium -> customSpacing.custom18
        windowSizeClass.isExpanded -> customSpacing.custom20
        else -> customSpacing.custom16
    }

    val contentVerticalPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom18
        windowSizeClass.isMedium -> customSpacing.custom20
        windowSizeClass.isExpanded -> customSpacing.custom22
        else -> customSpacing.custom18
    }

    val baseNormalVerticalPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom12
        windowSizeClass.isMedium -> customSpacing.custom14
        windowSizeClass.isExpanded -> customSpacing.custom16
        else -> customSpacing.custom12
    }

    // Base vertical padding - fundamental vertical spacing
    val normalVerticalPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom8
        windowSizeClass.isMedium -> customSpacing.custom10
        windowSizeClass.isExpanded -> customSpacing.custom12
        else -> customSpacing.custom8
    }

    val baseVerticalPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom6
        windowSizeClass.isMedium -> customSpacing.custom8
        windowSizeClass.isExpanded -> customSpacing.custom10
        else -> customSpacing.custom6
    }

    val smallVerticalPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom4
        windowSizeClass.isMedium -> customSpacing.custom6
        windowSizeClass.isExpanded -> customSpacing.custom8
        else -> customSpacing.custom4
    }

    /**
     * CATEGORY STYLES
     * Adaptive styling for category-related components
     */

    // OR divider padding - spacing for "OR" divider elements
    val orDividerPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom140
        windowSizeClass.isMedium -> customSpacing.custom180
        windowSizeClass.isExpanded -> customSpacing.custom180
        else -> customSpacing.custom140
    }

    /**
     * LAYOUT ALIGNMENT AND ARRANGEMENT
     * Adaptive layout positioning and spacing
     */

    // Horizontal arrangement - how items are spaced horizontally
    val horizontalArrangement = when {
        windowSizeClass.isCompact -> Arrangement.SpaceBetween
        windowSizeClass.isMedium -> Arrangement.SpaceAround
        windowSizeClass.isExpanded -> Arrangement.SpaceAround
        else -> Arrangement.SpaceBetween
    }

    /**
     * PRODUCT STYLES
     * Adaptive styling for product-related components
     */

    // Product title text style - used for product names
    val titleTextStyle = when {
        windowSizeClass.isCompact -> MaterialTheme.typography.titleLarge.copy(
            fontSize = 18.sp
        )
        windowSizeClass.isMedium -> MaterialTheme.typography.titleLarge
        windowSizeClass.isExpanded -> MaterialTheme.typography.titleLarge
        else -> MaterialTheme.typography.titleLarge.copy(
            fontSize = 18.sp
        )
    }

    /**
     * IMAGE STYLES
     * Adaptive sizing for various image components
     */

    // Main product image size - dimensions for primary product images
    val productImageSize = when {
        windowSizeClass.isCompact ->  Modifier.fillMaxWidth()
        windowSizeClass.isMedium ->  Modifier.fillMaxWidth()
        windowSizeClass.isExpanded ->  Modifier.fillMaxWidth()
        else ->  Modifier.fillMaxWidth()
    }

  val customImageHeight = when {
        windowSizeClass.isCompact -> customSpacing.custom180
        windowSizeClass.isMedium -> customSpacing.custom200
        windowSizeClass.isExpanded -> customSpacing.custom240
        else -> customSpacing.custom180
    }

    // Onboarding image size - dimensions for onboarding screen images
    val onBoardingImageSize = when {
        windowSizeClass.isCompact ->  Modifier.fillMaxWidth()
        windowSizeClass.isMedium ->  Modifier.fillMaxWidth()
        windowSizeClass.isExpanded ->  Modifier.fillMaxWidth()
        else ->  Modifier.fillMaxWidth()
    }

    // Product card height - height for product card containers
    val productCardHeight = when {
        windowSizeClass.isCompact -> customSpacing.custom60
        windowSizeClass.isMedium -> customSpacing.custom80
        windowSizeClass.isExpanded -> customSpacing.custom90
        else -> customSpacing.custom60
    }

    // List card padding - padding for list item cards

    val profileInfoPaddings = when {
        windowSizeClass.isCompact ->  Modifier.fillMaxWidth()
        windowSizeClass.isMedium ->  Modifier.fillMaxWidth()
        windowSizeClass.isExpanded ->  Modifier.fillMaxWidth()
        else ->  Modifier.fillMaxWidth()
    }

    // List card padding - padding for list item cards
    val listCardPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom10
        windowSizeClass.isMedium -> customSpacing.custom14
        windowSizeClass.isExpanded -> customSpacing.custom16
        else -> customSpacing.custom10
    }

    // Product summary image padding - dimensions for product summary images
    val productSummaryImagePadding = when {
        windowSizeClass.isCompact -> DpSize(customSpacing.custom60, customSpacing.custom50)
        windowSizeClass.isMedium -> DpSize(customSpacing.custom70, customSpacing.custom65)
        windowSizeClass.isExpanded -> DpSize(customSpacing.custom90, customSpacing.custom70)
        else -> DpSize(customSpacing.custom60, customSpacing.custom50)
    }

    // hero section padding - dimensions for hero section
    val heroSectionPadding = when {
        windowSizeClass.isCompact -> DpSize(customSpacing.custom280, customSpacing.custom120)
        windowSizeClass.isMedium -> DpSize(customSpacing.custom480, customSpacing.custom160)
        windowSizeClass.isExpanded -> DpSize(customSpacing.custom480, customSpacing.custom180)
        else -> DpSize(customSpacing.custom280, customSpacing.custom120)
    }

    //hero card padding - dimensions for hero card
    val heroCardPadding = when {
        windowSizeClass.isCompact -> DpSize(customSpacing.custom420, customSpacing.custom160)
        windowSizeClass.isMedium -> DpSize(customSpacing.custom600, customSpacing.custom180)
        windowSizeClass.isExpanded -> DpSize(customSpacing.custom480, customSpacing.custom180)
        else -> DpSize(customSpacing.custom420, customSpacing.custom160)
    }

    /**
     * ADAPTIVE DIMENSIONS
     * Responsive sizing that adapts to screen size
     */

    // Adaptive height - responsive height for various components
    val adaptiveHeight = when {
        windowSizeClass.isCompact -> customSpacing.custom52
        windowSizeClass.isMedium -> customSpacing.custom53
        windowSizeClass.isExpanded -> customSpacing.custom53
        else -> customSpacing.custom52
    }

    // Adaptive form height - responsive height for form elements
    val adaptiveProductCardHeight = when {
        windowSizeClass.isCompact -> customSpacing.custom160
        windowSizeClass.isMedium -> customSpacing.custom170
        windowSizeClass.isExpanded -> customSpacing.custom180
        else -> customSpacing.custom160
    }

    // Adaptive width modifier - responsive width modifier for containers
    val adaptiveWidthModifier = when {
        windowSizeClass.isCompact -> Modifier.fillMaxWidth()
        windowSizeClass.isMedium -> Modifier.fillMaxWidth()
        windowSizeClass.isExpanded -> Modifier.fillMaxWidth()
        else -> Modifier.fillMaxWidth()
    }

    // Adaptive width modifier - responsive width modifier for containers
    val floatingSnackBarPaddings = when {
        windowSizeClass.isCompact -> Modifier.fillMaxWidth()
        windowSizeClass.isMedium -> Modifier.fillMaxWidth()
        windowSizeClass.isExpanded -> Modifier.width(customSpacing.custom680)
        else -> Modifier.fillMaxWidth()
    }

    val networkCardPadding = when {
        windowSizeClass.isCompact -> Modifier.fillMaxWidth()
        windowSizeClass.isMedium -> Modifier.fillMaxWidth()
        windowSizeClass.isExpanded -> Modifier.width(customSpacing.custom480)
        else -> Modifier.fillMaxWidth()
    }

    // Adaptive form width modifier - responsive width modifier for forms
    val adaptiveFormWidthModifier = when {
        windowSizeClass.isCompact -> Modifier.fillMaxWidth()
        windowSizeClass.isMedium -> Modifier.fillMaxWidth()
        windowSizeClass.isExpanded -> Modifier.fillMaxWidth()
        else -> Modifier.fillMaxWidth()
    }

    // Adaptive form width modifier - responsive width modifier for forms
  val adaptiveListCardWidthModifier = when {
        windowSizeClass.isCompact -> Modifier.fillMaxWidth()
        windowSizeClass.isMedium -> Modifier.fillMaxWidth()
        windowSizeClass.isExpanded -> Modifier.fillMaxWidth()
        else -> Modifier.fillMaxWidth()
    }

    // Icon size - responsive icon dimensions
    val iconSize = when {
        windowSizeClass.isCompact -> customSpacing.custom24
        windowSizeClass.isMedium -> customSpacing.custom28
        windowSizeClass.isExpanded -> customSpacing.custom30
        else -> customSpacing.custom24
    }

    val largeIconSize = when {
        windowSizeClass.isCompact -> customSpacing.custom40
        windowSizeClass.isMedium -> customSpacing.custom42
        windowSizeClass.isExpanded -> customSpacing.custom44
        else -> customSpacing.custom40
    }

    val mediumIconSize = when {
        windowSizeClass.isCompact -> customSpacing.custom20
        windowSizeClass.isMedium -> customSpacing.custom24
        windowSizeClass.isExpanded -> customSpacing.custom28
        else -> customSpacing.custom20
    }

    val baseIconSize = when {
        windowSizeClass.isCompact -> customSpacing.custom16
        windowSizeClass.isMedium -> customSpacing.custom20
        windowSizeClass.isExpanded -> customSpacing.custom24
        else -> customSpacing.custom16
    }

    val onBoardingPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom2Five
        windowSizeClass.isMedium -> customSpacing.custom5
        windowSizeClass.isExpanded -> customSpacing.custom10
        else -> customSpacing.custom2Five
    }

  val onBoardingAdaptiveWidth = when {
      windowSizeClass.isCompact -> Modifier.fillMaxWidth()
      windowSizeClass.isMedium -> Modifier.fillMaxWidth()
      windowSizeClass.isExpanded -> Modifier.width(customSpacing.custom680)
      else -> Modifier.fillMaxWidth()
    }

    val customSpacerMedium = when {
        windowSizeClass.isCompact -> customSpacing.custom80
        windowSizeClass.isMedium -> customSpacing.custom100
        windowSizeClass.isExpanded -> customSpacing.custom120
        else -> customSpacing.custom80
    }

     val customSpacerSmall = when {
        windowSizeClass.isCompact -> customSpacing.custom40
        windowSizeClass.isMedium -> customSpacing.custom80
        windowSizeClass.isExpanded -> customSpacing.custom100
        else -> customSpacing.custom40
    }

    val customSpacerLarge = when {
        windowSizeClass.isCompact -> customSpacing.custom100
        windowSizeClass.isMedium -> customSpacing.custom120
        windowSizeClass.isExpanded -> customSpacing.custom140
        else -> customSpacing.custom100
    }

    val baseSize = when {
        windowSizeClass.isCompact -> customSpacing.custom24
        windowSizeClass.isMedium -> customSpacing.custom28
        windowSizeClass.isExpanded -> customSpacing.custom28
        else -> customSpacing.custom24
    }

    val topBarPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom16
        windowSizeClass.isMedium -> customSpacing.custom18
        windowSizeClass.isExpanded -> customSpacing.custom20
        else -> customSpacing.custom16
    }

    val borderSize = when {
        windowSizeClass.isCompact -> customSpacing.custom2
        windowSizeClass.isMedium -> customSpacing.custom2
        windowSizeClass.isExpanded -> customSpacing.custom2Five
        else -> customSpacing.custom2
    }

    val smallSizes = when {
        windowSizeClass.isCompact -> customSpacing.custom1
        windowSizeClass.isMedium -> customSpacing.custom1
        windowSizeClass.isExpanded -> customSpacing.custom1
        else -> customSpacing.custom1
    }

    val heroIconSize = when {
        windowSizeClass.isCompact -> customSpacing.custom32
        windowSizeClass.isMedium -> customSpacing.custom40
        windowSizeClass.isExpanded -> customSpacing.custom42
        else -> customSpacing.custom32
    }

    // Icon size - responsive icon dimensions
    val customButtonPadding = when {
        windowSizeClass.isCompact -> customSpacing.custom160
        windowSizeClass.isMedium -> customSpacing.custom200
        windowSizeClass.isExpanded -> customSpacing.custom220
        else -> customSpacing.custom180
    }

    /**
     * Create the WindowSizeAppConstants object with all the adaptive values
     * This object will be provided to the composition tree via CompositionLocal
     */
    val windowSizeConstants = WindowSizeAppConstants(
        logoPadding = logoPadding,
        appBarPadding = appBarPadding,
        onBoardingPadding = onBoardingPadding,
        onBoardingAdaptiveWidth = onBoardingAdaptiveWidth,
        cardHeight = cardHeight,
        borderSize = borderSize,
        topBarPadding = topBarPadding,
        smallSizes = smallSizes,
        customSpacerLarge = customSpacerLarge,
        customSpacerMedium = customSpacerMedium,
        baseIconSize = baseIconSize,
        largeIconSize  = largeIconSize,
        baseSize  = baseSize ,
        customSpacerSmall = customSpacerSmall,
        mediumIconSize =  mediumIconSize,
        carouselPageSize = carouselPageSize,
        listImagePadding = listImagePadding,
        contentPadding = contentPadding,
        titleTextStyle = titleTextStyle,
        bottomBarLabelStyles = bottomBarLabelStyles,
        carouselPageSpacing = carouselPageSpacing,
        horizontalArrangementStyle = horizontalArrangement,
        iconSize = iconSize,
        customImageHeight = customImageHeight,
        cardElevationPadding = cardElevationPadding,
        basePadding = basePadding,
        heroIconSize  = heroIconSize ,
        baseVerticalPadding = baseVerticalPadding,
        baseNormalVerticalPadding = baseNormalVerticalPadding,
        labelTextStyle = labelTextStyle,
        bodyTextStyle = bodyTextStyle,
        productImageSize = productImageSize,
        contentVerticalPadding = contentVerticalPadding,
        normalVerticalPadding =  normalVerticalPadding,
        smallVerticalPadding =  smallVerticalPadding,
        orDividerPadding = orDividerPadding,
        onBoardingImageSize = onBoardingImageSize,
        carouselCardHeight = carouselCardHeight,
        carouselImageHeight = carouselImageHeight,
        carouselImageWidth = carouselImageWidth,
        listRightPadding = listRightPadding,
        adaptiveListCardWidthModifier = adaptiveListCardWidthModifier,
        productCardHeight = productCardHeight,
        adaptiveWidthModifier = adaptiveWidthModifier,
        floatingSnackBarPaddings = floatingSnackBarPaddings,
        adaptiveHeight = adaptiveHeight,
        smallButtonWidth = smallButtonWidth,
        adaptiveFormWidthModifier = adaptiveFormWidthModifier,
        profileCardPadding = profileCardPadding,
        adaptiveProductCardHeight =  adaptiveProductCardHeight,
        adaptiveProfileVerticalSpacer = adaptiveProfileVerticalSpacer,
        listCardPadding = listCardPadding,
        productSummaryImagePadding = productSummaryImagePadding,
        customButtonPadding = customButtonPadding,
        productCardPaddings = productCardPaddings,
        profileInfoPaddings =  profileInfoPaddings,
        heroCardPadding =  heroCardPadding,
        heroSectionPadding =  heroSectionPadding,
        networkCardPadding  =  networkCardPadding
    )

    /**
     * Provide the adaptive constants to the composition tree
     * This makes all the window-size-dependent constants available to child composable
     */
    CompositionLocalProvider(LocalWindowSizeConstant provides windowSizeConstants) {
        content()
    }
}