package com.example.myapp.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.utils.CustomShape

/**
 * CustomImageContainer - A robust, versatile image component powered by Coil.
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
                .size(Size.ORIGINAL)
                .build()
        }
    }

     // Let the caller provide sizing via the `modifier` so images respect aspect ratio/constraints.
    Box(
        modifier = modifier, // caller controls size
        contentAlignment = Alignment.Center,
    ) {
        if (imageRequest != null) {
            // Make image fill the outer Box so sizing is consistent and controlled by the caller's modifier
            AsyncImage(
                clipToBounds = clipToBounds,
                model = imageRequest,
                colorFilter = colorFilter,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
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
