package com.example.myapp.view.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapp.R
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomScaffoldContainer
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.components.custom.buttons.CustomButton
import com.example.myapp.view.components.custom.buttons.CustomOutlinedButton
import kotlinx.coroutines.launch

/**
 * OnboardingScreen - Main onboarding screen with horizontal pager navigation.
 *
 * This composable creates a multi-page onboarding experience with smooth horizontal
 * paging, page indicators, and navigation buttons. Users can swipe through screens
 * or use the back/next buttons to navigate.
 *
 * @param onFinished Callback function called when onboarding is completed
 *
 * Usage:
 * ```
 * OnboardingScreen(
 *     onFinished = { /* Navigate to main app */ }
 * )
 * ```
 */

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit = {}
) {
    val windowSizeConstant = LocalWindowSizeConstant.current

    val screens = listOf(
        OnBoardingModel.FirstScreen,
        OnBoardingModel.SecondScreen,
    )

    val pagerState = rememberPagerState(initialPage = 0) {
        screens.size
    }

    val scope = rememberCoroutineScope()
    rememberScrollState()

    CustomScaffoldContainer(
        showTopBar = false,
        showBottomBar = false,
        verticalArrangement = Arrangement.Center,
        content = {
            PaddedSection(
                content = {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { index ->
                        OnboardingModelScreen(onBoardingModel = screens[index])
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = when (pagerState.currentPage) {
                            0 -> Arrangement.End
                            screens.size - 1 -> Arrangement.spacedBy(windowSizeConstant.baseNormalVerticalPadding)
                            else -> Arrangement.spacedBy(windowSizeConstant.baseNormalVerticalPadding)
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (pagerState.currentPage > 0) {
                            CustomOutlinedButton(
                                label = R.string.back,
                                useSmallWidth = true,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                },
                            )
                        }

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            OnBoardingIndicator(
                                pageSize = screens.size,
                                currentPage = pagerState.currentPage,
                            )
                        }

                        if (pagerState.currentPage < screens.size - 1) {
                            CustomButton(
                                label = R.string.next,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                },
                                useSmallWidth = true,
                            )
                        } else {
                            CustomButton(
                                label = R.string.finish,
                                onClick = { onFinished() },
                                useSmallWidth = true,
                            )

                        }
                    }
                })
        }
    )
}
