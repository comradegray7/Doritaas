// kotlin
package com.example.myapp.view.screens.product_search

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.example.myapp.R
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomAlertDialog
import com.example.myapp.view.components.custom.buttons.ButtonIconComposable
import com.example.myapp.view.components.custom.buttons.CustomTextButton
import com.example.myapp.view.utils.ButtonIcon

/**
 * A composable button that starts a voice-based search flow.
 *
 * Behavior:
 * - Checks whether speech recognition is available on the device.
 * - Requests RECORD_AUDIO permission at runtime if needed.
 * - Launches the system speech recognizer and returns the first recognized result
 *   via [onVoiceResult].
 * - Shows a rationale dialog directing the user to app settings if permission is denied.
 *
 * @param onVoiceResult Callback invoked with the recognized spoken text when voice input completes.
 * @param modifier Optional [Modifier] for layout/appearance adjustments.
 */
@Composable
fun VoiceSearchButton(
    onVoiceResult: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    val windowSizeClass = LocalWindowSizeConstant.current

    // Check if speech recognition is available on the device
    val isSpeechRecognitionAvailable = remember {
        context.packageManager.queryIntentActivities(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),
            PackageManager.MATCH_DEFAULT_ONLY
        ).isNotEmpty()
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.firstOrNull()?.let { spokenText ->
                onVoiceResult(spokenText)
            }
        }
    }

    /**
     * Starts the system voice recognition activity.
     *
     * if launching fails (device not supporting voice recognizer or other errors).
     */
    fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechRecognizerLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Voice search not supported on this device", Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * Requests the RECORD_AUDIO permission if not already granted, otherwise starts recognition.
     */
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecognition()
        } else {
            showPermissionDialog = true
        }
    }

    /**
     * Helper that checks RECORD_AUDIO permission and either requests it or starts recognition.
     */
    fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceRecognition()
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // The main UI button
    ButtonIconComposable(
        modifier = modifier,
        showBgColor = false,
        buttonIcon = if (isSpeechRecognitionAvailable) ButtonIcon.Vector(Icons.Filled.Mic) else ButtonIcon.Vector(
            Icons.Filled.MicNone
        ),
        onClick = {
            if (isSpeechRecognitionAvailable) {
                checkAndRequestPermission()
            } else {
                Toast.makeText(
                    context,
                    "Voice search not available on this device",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        contentDescription = "Refresh"
    )

    // Permission rationale dialog
    if (showPermissionDialog) {
        CustomAlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text(
                    stringResource(R.string.search_by_voice),
                    style = windowSizeClass.titleTextStyle
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.voice_search_permission_required
                    ),
                    style = windowSizeClass.bodyTextStyle
                )
            },
            dismissButton = {
                CustomTextButton(
                    onClick = { showPermissionDialog = false },
                    label = R.string.cancel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                CustomTextButton(
                    onClick = {
                        showPermissionDialog = false
                        // Open app settings for manual permission grant
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    label = R.string.open_settings
                )
            },
        )
    }
}
