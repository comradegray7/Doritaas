package com.example.myapp.view.screens.product.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.myapp.data.dataclass.CategoryItem
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.view.components.CustomFilterChip
import com.example.myapp.view.components.CustomLazyRow

/**
 * Categories - Horizontal list of category filter chips.
 *
 * Provides a row of selectable chips representing different product categories.
 * Allows users to filter products by clicking on a category.
 *
 * @param onCategoryClick Callback triggered when a category is selected.
 * @param viewModel ViewModel for accessing the list of available categories.
 */
@Composable
fun Categories(
    onCategoryClick: (CategoryItem) -> Unit = {},
    viewModel: ProductCrudViewModel = hiltViewModel()
) {
    val categoryState by viewModel.productState.collectAsState()
    val categoryItems = categoryState.categories

    // No default selection - initially null
    var selectedCategoryIndex by remember { mutableIntStateOf(-1) }

    if (categoryItems.isNotEmpty()) {
        CustomLazyRow {
            items(count = categoryItems.size) { index ->
                val category = categoryItems[index]

                CategoryChip(
                    isSelected = selectedCategoryIndex == index,
                    category = category.categoryName,
                    onClick = {
                        // Toggle selection: if already selected, deselect it
                        if (selectedCategoryIndex == index) {
                            selectedCategoryIndex = -1
                            // Optional: Call with null or special value to indicate "show all"
                            // onCategoryClick(CategoryItem("All", "All"))
                        } else {
                            selectedCategoryIndex = index
                            onCategoryClick(category)
                        }
                    }
                )
            }
        }
    }
}

/**
 * CategoryChip - Composable for displaying a single category item chip
 *
 * This composable displays a category label in a filter chip format.
 *
 * @param modifier Modifier to be applied to the chip
 * @param category The category label text
 * @param onClick Callback when the category is clicked
 * @param isSelected Whether the category is currently selected
 */

@Composable
fun CategoryChip(
    modifier: Modifier = Modifier,
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    CustomFilterChip(
        modifier = modifier,
        onClick = onClick,
        label = category,
        isSelected = isSelected,
    )
}

