package com.example.myapp.view.screens.product

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil3.ImageLoader
import com.example.myapp.R
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.ui.theme.customSpacing
import com.example.myapp.view.components.CustomImageContainer
import com.example.myapp.view.components.CustomLazyRow
import com.example.myapp.view.components.PaddedSection
import com.example.myapp.view.utils.CustomShape


/**
 * SmallProductImages - Composable function for displaying a horizontal list of small product images.
 * 
 * This composable creates a scrollable row of small product images that can be selected.
 * It's typically used in product detail screens to show multiple product images
 * in a thumbnail format, with the selected image highlighted.
 * 
 * @param productImages List of SmallImageItem objects containing image data
 * @param selectedIndex Index of the currently selected image
 * @param onImageSelected Callback function when an image is selected
 * 
 * Usage:
 * ```
 * SmallProductImages(
 *     productImages = product.images,
 *     selectedIndex = selectedImageIndex,
 *     onImageSelected = { index -> selectedImageIndex = index }
 * )
 * ```
 */
@Composable
fun SmallProductImages(
    productImages: List<String>,
    selectedIndex: Int,
    onImageSelected: (Int) -> Unit,
    imageLoader: ImageLoader? = null  
) {
    val spacing = customSpacing
    val windowSizeClass = LocalWindowSizeConstant.current

    if (productImages.isEmpty()) {
        // Show placeholder when no images available
        PaddedSection(
            content = {
                Text(
                    stringResource(R.string.select_images),
                    style = windowSizeClass.labelTextStyle,
                    modifier = Modifier.padding(windowSizeClass.basePadding)
                )
            }
        )

        return
    }

    CustomLazyRow {
        itemsIndexed(productImages) { index, imageUrl ->
            Card(
                modifier = Modifier
                    .padding(horizontal = spacing.custom4)
                    .size(spacing.custom60)
                    .clickable { onImageSelected(index) },
                shape = CustomShape.mediumShape(),
                border = if (index == selectedIndex)
                    BorderStroke(windowSizeClass.borderSize, MaterialTheme.colorScheme.primary)
                else null
            ) {
                CustomImageContainer(
                    data = imageUrl,
                    contentDescription = "Product image ${index + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    imageLoader = imageLoader
                )
            }
        }
    }
}
