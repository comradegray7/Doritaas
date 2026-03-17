package com.example.myapp.view.screens.product

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import com.example.myapp.R
import com.example.myapp.data.dataclass.CarouselItem
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomImageContainer
import com.example.myapp.view.utils.CloudinaryHelper
import com.example.myapp.view.utils.CustomShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * CarouselItem - Data class representing a single item in the product carousel.
 *
 * This data class contains the information needed to display a carousel item,
 * including the image resource and accessibility description.
 *
 */

/**
 * ProductCarousel - Horizontal scrolling carousel component for displaying promotional products.
 *
 * This composable creates an auto-scrolling horizontal pager that displays product images
 * in a carousel format. It features:
 * - Automatic scrolling with configurable interval
 * - Smooth animations with easing curves
 * - Infinite scrolling capability
 * - Responsive design with adaptive sizing
 *
 * @param autoScrollInterval Time interval between auto-scroll transitions (default: 6 seconds)
 */

@ExperimentalMaterial3Api
@OptIn(ExperimentalFoundationApi::class)

@Composable
fun ProductCarousel(
    carouselItems: List<CarouselItem>,
    autoScrollInterval: Long = 6000L,
    onCarouselClick: () -> Unit = {},
    cloudinaryHelper: CloudinaryHelper
) {
    val windowSizeConstant = LocalWindowSizeConstant.current
    val coroutineScope = rememberCoroutineScope()

    // Get carousel items from UI state instead of static list
    val carouselItems = carouselItems

    // Initialize pager state with a large initial page for infinite scrolling
    val pagerState = rememberPagerState(initialPage = Int.MAX_VALUE / 2) { Int.MAX_VALUE }

    // Auto-scroll effect - only start when carousel items are available
    LaunchedEffect(carouselItems) {
        if (carouselItems.isNotEmpty()) {
            while (true) {
                delay(autoScrollInterval)
                coroutineScope.launch {
                    val nextPage = pagerState.currentPage + 1
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        animationSpec = tween(
                            durationMillis = 800,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            }
        }
    }

    // Only show carousel when we have items
    if (carouselItems.isNotEmpty()) {
        // Main carousel container
        Box(
            modifier =  windowSizeConstant.carouselCardHeight
        ) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = windowSizeConstant.carouselPageSpacing,
                contentPadding = PaddingValues(horizontal = windowSizeConstant.contentPadding),
                pageSize = windowSizeConstant.carouselPageSize
            ) { page ->
                // Get the carousel item for current page (using modulo for infinite scrolling)
                val carousel = carouselItems[page % carouselItems.size]

                // Display carousel item image using URL from Firestore
                CustomImageContainer(
                    data = cloudinaryHelper.getImageUrl(carousel.imageUrl), // Using imageUrl from Firestore
                    shape = CustomShape.mediumShape(),
                    clipToBounds = false,
                    size = DpSize(height = windowSizeConstant.carouselImageHeight, width = windowSizeConstant.carouselImageWidth),
                    contentDescription = "Carousel Image",
                    modifier = Modifier.clickable(onClick = { onCarouselClick()}))
            }
         }
    }
}

@Composable
/**
 * CarouselPlaceholder
 *
 */
fun CarouselPlaceholder() {
    val windowSizeClass = LocalWindowSizeConstant.current
    Box(
        modifier = Modifier
            .width(customSpacing.custom400)
            .height(customSpacing.custom180),
    ) {
        Text(
            text = stringResource(R.string.loading),
            style = windowSizeClass.bodyTextStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}