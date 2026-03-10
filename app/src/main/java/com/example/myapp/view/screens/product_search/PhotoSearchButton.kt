package com.example.myapp.view.screens.product_search

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import com.example.myapp.R
import com.example.myapp.data.model.ProductCrudViewModel
import com.example.myapp.data.model.SearchViewModel
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.ButtonIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A composable that shows a camera/gallery button to perform an image-based product search.
 *
 * This composable:
 * - Presents a button with a camera icon.
 * - Shows a dialog allowing the user to choose between camera or gallery.
 * - Handles runtime camera permission requests.
 * - Launches either a camera preview (using `TakePicturePreview`) or a gallery picker.
 * - Saves a camera-captured `Bitmap` to a temporary file and converts it to a `Uri`.
 * - Uses [ProductCrudViewModel] to mark/search pending image searches and persist the last query.
 * - Uses [SearchViewModel] to execute the image search with the obtained `Uri`.
 *
 * @param viewModel The [ProductCrudViewModel] used to persist and check pending searches and last query.
 * @param searchViewModel The [SearchViewModel] used to execute searches by image `Uri`.
 */
@Composable
fun PhotoSearchButton(
    viewModel: ProductCrudViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    val windowSizeClass = LocalWindowSizeConstant.current

    // ✅ Use TakePicturePreview
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.setPendingImageSearch(true)

            lifecycleScope.launch {
                val uri = saveBitmapToFile(context, bitmap)
                if (uri != null) {
                    viewModel.saveSearchQuery(uri.toString())
                    searchViewModel.searchByImage(uri)
                } else {
                    viewModel.setPendingImageSearch(false)
                }
            }
        } else {
            viewModel.setPendingImageSearch(false)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setPendingImageSearch(true)
            searchViewModel.searchByImage(it)
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.hasPendingImageSearch()) {
            val savedUri = viewModel.getLastSearchQuery()
            if (!savedUri.isNullOrBlank()) {
                searchViewModel.searchByImage(savedUri.toUri())
                viewModel.setPendingImageSearch(false)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setPendingImageSearch(true)
            cameraLauncher.launch() // No URI needed!
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    ButtonIconComposable(
        showBgColor = false,
        buttonIcon = ButtonIcon.Vector(Icons.Filled.CameraAlt),
        onClick = { showDialog = true },
        contentDescription = "Image Search"
    )

    if (showDialog) {
        CustomAlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    stringResource(R.string.search_by_image),
                    style = windowSizeClass.titleTextStyle
                )
            },
            text = {
                Text(
                    stringResource(R.string.choose_from_gallery),
                    style = windowSizeClass.bodyTextStyle
                )
            },
            dismissButton = {
                CustomTextButton(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = {
                        showDialog = false
                        if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            viewModel.setPendingImageSearch(true)
                            cameraLauncher.launch()
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    label = R.string.camera
                )
            },
            confirmButton = {
                CustomTextButton(
                    onClick = {
                        showDialog = false
                        galleryLauncher.launch("image/*")
                    },
                    label = R.string.gallery
                )
            }
        )
    }
}

/**
 * Saves a [Bitmap] to a temporary file inside the app cache directory and returns a content `Uri`
 * that can be shared with other apps via [FileProvider].
 *
 * This function runs on [Dispatchers.IO] when called from a coroutine.
 *
 * @param context Context used to access the cache directory and [FileProvider].
 * @param bitmap The bitmap to save as a JPEG file.
 * @return A content `Uri` for the saved file on success, or `null` if an error occurred.
 */
private suspend fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFile = File(context.cacheDir, "IMG_${timeStamp}.jpg")

            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            Log.e("PhotoSearch", "Error: ${e.message}", e)
            null
        }
    }
}