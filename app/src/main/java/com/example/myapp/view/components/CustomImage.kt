
package com.example.myapp.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.utils.CustomShape

/**
 * CustomImageContainer - A robust, versatile image component powered by Coil.
 *
 * This component handles loading images from various sources (network, local files, resources)
 * with built-in support for loading states, error handling, placeholders, and fallback UI.
 * It integrates seamlessly with the app's design system using responsive sizing and shaping.
 *
 * @param modifier Modifier to be applied to the outer container.
 * @param data The data to load. Can be a URL, URI, file, resource ID, or any other type supported by Coil.
 * @param contentDescription The content description for accessibility.
 * @param contentScale How the image should be scaled to fit the bounds. Defaults to [ContentScale.Crop].
 * @param placeholder Optional painter to display while the request is loading.
 * @param error Optional painter to display when the request fails.
 * @param fallback Optional painter to display when the data is null/empty or the request fails.
 * @param colorFilter Optional color filter to apply to the image.
 * @param width Optional fixed width for the image. Defaults to dynamic sizing.
 * @param height Optional fixed height for the image. Defaults to dynamic sizing.
 * @param size Optional fixed size (width & height). Overrides width and height params if set.
 * @param crossFade If true, enables a crossfade animation when the image loads. Defaults to true.
 * @param onLoading Callback invoked when the loading starts.
 * @param onSuccess Callback invoked when the request is successful.
 * @param onError Callback invoked when the request fails.
 * @param imageLoader Optional custom [ImageLoader] to use. If null, creates a default one.
 * @param clipToBounds If true, clips the content to the bounds of the container. Defaults to true.
 */

@Composable
fun CustomImageContainer(
    modifier: Modifier = Modifier,
    data: Any?,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = CustomShape.mediumShape(),
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = null,
    colorFilter: ColorFilter? = null,
    width: Dp = customSpacing.custom0,
    height: Dp = customSpacing.custom0,
    size: DpSize = DpSize(customSpacing.custom0, customSpacing.custom0),
    crossFade: Boolean = true,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    imageLoader: ImageLoader? = null,
    clipToBounds: Boolean = true
) {
    // Window size constant for responsive design
    val windowSizeConstant = LocalWindowSizeConstant.current

    val context = LocalContext.current

    // Use provided ImageLoader or create a default one
    val loader = imageLoader ?: remember {
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .crossfade(crossFade)
            .build()
    }

    val imageRequest = remember(data) {
        data?.let {
            ImageRequest.Builder(context)
                .data(it)
                .crossfade(crossFade)
                .build()
        }
    }

    Box(
        modifier = modifier.then(
            Modifier.fillMaxSize()
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (imageRequest != null) {
            AsyncImage(
                clipToBounds = clipToBounds,
                model = imageRequest,
                colorFilter = colorFilter,
                contentDescription = contentDescription,
                modifier = Modifier
                    .then(if (width > customSpacing.custom0) Modifier.width(width) else Modifier)
                    .then(if (height > customSpacing.custom0) Modifier.height(height) else Modifier)
                    .then(
                        if (size != DpSize(
                                customSpacing.custom0,
                                customSpacing.custom0
                            )
                        ) Modifier.size(size) else Modifier
                    )
                    .clip(shape),
                contentScale = contentScale,
                placeholder = placeholder,
                error = error,
                fallback = fallback,
                imageLoader = loader,
                onLoading = { state ->
                    onLoading?.invoke(state)
                },
                onSuccess = { state ->
                    onSuccess?.invoke(state)
                },
                onError = { state ->
                    onError?.invoke(state)
                }
            )
        } else {
            // Fallback UI when data is null
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                fallback?.let {
                    CustomIcon(
                        painter = it,
                        contentDescription = contentDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                } ?: run {
                    CustomIcon(
                        modifier = Modifier.size(windowSizeConstant.listImagePadding),
                        icon = Icons.Filled.Image,
                        contentDescription = "Placeholder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

