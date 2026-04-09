
package com.example.myapp.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * CustomScaffoldContainer - Main scaffold composable for the application.
 * 
 * This composable provides a complete screen structure with optional top bar,
 * bottom bar, pull-to-refresh functionality, and adaptive content layout.
 * It serves as the foundation for most screens in the app.
 * 
 * @param modifier Optional modifier to apply to the scaffold
 * @param title Optional string resource ID for the top bar title
 * @param topBarComposable Optional custom top bar content
 * @param showTitle Whether to show the title in the top bar
 * @param onNavigateBack Callback for back navigation
 * @param bottomBarContent Optional bottom bar content
 * @param showTopBar Whether to show the top bar
 * @param showBottomBar Whether to show the bottom bar
 * @param content The main content of the screen
 * @param strTitle Optional direct string value for the top bar title
 * @param showBackArrow Whether to show the back arrow in the top bar
 * @param actions Optional action buttons for the top bar
 * @param onRefresh Optional pull-to-refresh callback
 * @param floatingBtnContent Optional content for the floating action button
 * @param verticalArrangement Vertical arrangement for the content column
 * @param snackBarHostState State for the snackbar host
 *
 * ## Usage:
 * ```kotlin
 * CustomScaffoldContainer(
 *     title = R.string.screen_title,
 *     showBackArrow = true,
 *     onNavigateBack = { navController.popBackStack() },
 *     onRefresh = { /* refresh logic */ }
 * ) {
 *     // Screen content here
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomScaffoldContainer(
    modifier: Modifier = Modifier,
    title: Int? = null,
    topBarComposable: @Composable () -> Unit = {},
    showTitle: Boolean = true,
    onNavigateBack: () -> Unit = {},
    bottomBarContent: @Composable () -> Unit = {},
    showTopBar: Boolean = true,
    showBottomBar: Boolean = true,
    content: @Composable (() -> Unit),
    strTitle: String? = null,
    showBackArrow: Boolean = true,
    actions: @Composable () -> Unit = {},
    onRefresh: (() -> Unit)? = null,
    floatingBtnContent: @Composable (() -> Unit) = {},
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {

    // State for managing refresh animation
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Handle refresh with animation delay
    val handleRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            onRefresh?.invoke() // Execute refresh logic
            delay(1500) // Animation duration
            isRefreshing = false
        }
    }

    // Conditionally wrap with pull-to-refresh if refresh callback is provided
    if (onRefresh != null) {
        PullToRefreshComponent(
            modifier = modifier.fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = handleRefresh,
            content = {
                ScaffoldContainer(
                    showTopBar = showTopBar,
                    showTitle = showTitle,
                    title = title,
                    topBarComposable = topBarComposable,
                    showBackArrow = showBackArrow,
                    onNavigateBack = onNavigateBack,
                    actions = actions,
                    strTitle = strTitle,
                    content = content,
                    bottomBarContent = bottomBarContent,
                    showBottomBar = showBottomBar,
                    modifier = modifier,
                    verticalArrangement = verticalArrangement,
                    snackBarHostState = snackBarHostState,
                    floatingBtnContent = floatingBtnContent
                )
            }
        )
    } else {
        // Standard scaffold without pull-to-refresh
        ScaffoldContainer(
            showTopBar = showTopBar,
            showTitle = showTitle,
            title = title,
            topBarComposable = topBarComposable,
            showBackArrow = showBackArrow,
            onNavigateBack = onNavigateBack,
            actions = actions,
            strTitle = strTitle,
            content = content,
            showBottomBar = showBottomBar,
            bottomBarContent = bottomBarContent,
            modifier = modifier,
            verticalArrangement = verticalArrangement,
            snackBarHostState = snackBarHostState,
            floatingBtnContent = floatingBtnContent
        )
    }
}

/**
 * ScaffoldContainer - Private composable that creates the actual scaffold structure.
 * 
 * This composable handles the creation of the Material Design 3 scaffold with
 * top bar, bottom bar, and content area. It uses adaptive styling based on
 * window size constants.
 * 
 * @param modifier Optional modifier to apply to the scaffold
 * @param showTopBar Whether to show the top bar
 * @param showTitle Whether to show the title in the top bar
 * @param title Optional string resource ID for the title
 * @param topBarComposable Optional custom top bar content
 * @param showBackArrow Whether to show the back arrow
 * @param onNavigateBack Callback for back navigation
 * @param actions Optional action buttons
 * @param showBottomBar Whether to show the bottom bar
 * @param bottomBarContent Optional bottom bar content
 * @param content The main content
 * @param verticalArrangement Vertical arrangement for content
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ScaffoldContainer(
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    showTitle: Boolean,
    title: Int?,
    strTitle: String? = null,
    topBarComposable: @Composable (() -> Unit) = {},
    showBackArrow: Boolean,
    onNavigateBack: () -> Unit,
    actions: @Composable (() -> Unit),
    showBottomBar: Boolean,
    bottomBarContent: @Composable (() -> Unit) = {},
    content: @Composable (() -> Unit),
    floatingBtnContent: @Composable (() -> Unit) = {},
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    snackBarHostState: SnackbarHostState,
) {

    val windowSizeConstant = LocalWindowSizeConstant.current

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ), // Use background color)
                    title = {
                        if (showTitle) {
                            AppBarTitle(
                                title = title.takeIf { strTitle.isNullOrBlank() },
                                titleStr = strTitle.takeIf { title == null }
                            )
                        }// Display title if provided
                        else {
                            topBarComposable() // Use custom top bar content
                        }
                    },
                    navigationIcon = {
                        if (showBackArrow) {
                            CustomBackNavigation(onNavigateBack = onNavigateBack) // Back navigation button
                        }
                    },
                    actions = {
                        actions() // Display action buttons
                    }
                )
            }
        },
        floatingActionButton = { floatingBtnContent() },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                    Column(
                        modifier = Modifier.padding(windowSizeConstant.basePadding), // Adaptive padding
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        bottomBarContent() // Display bottom bar content
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { innerPadding ->

        Column(
            modifier = modifier.then(
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .padding(innerPadding),
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = verticalArrangement // Use provided vertical arrangement
        ) {
            content() // Display the main content
        }
    }
}


