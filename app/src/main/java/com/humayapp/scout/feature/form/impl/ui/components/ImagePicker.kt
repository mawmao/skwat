package com.humayapp.scout.feature.form.impl.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.humayapp.scout.core.ui.component.ScoutLabel
import com.humayapp.scout.core.ui.theme.InputFieldTokens
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme
import java.io.File

@Composable
fun ImageBox(
    modifier: Modifier = Modifier,
    uri: Uri? = null,
    aspectRatio: Float = 0f,
) {

    val color = InputFieldTokens.unfocusedColor

    Box(
        modifier = modifier
            .then(if (aspectRatio > 0f) Modifier.aspectRatio(aspectRatio) else Modifier)
            .border(1.dp, color, ScoutTheme.shapes.cornerMedium)
            .clip(ScoutTheme.shapes.cornerMedium),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                painter = painterResource(ScoutIcons.Image),
                contentDescription = null,
                tint = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerBox(
    fieldKey: String,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 0f,
    label: String,
    uri: Uri?,
    onImageSelected: (String, Uri?) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }

    val pickGallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { picked ->
        onImageSelected(fieldKey, picked)
    }

    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            onImageSelected(fieldKey, cameraUri)
        }
    }

    fun launchCamera() {
        val file = File(
            context.cacheDir,
            "camera_${System.currentTimeMillis()}.jpg"
        )
        cameraUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        takePicture.launch(cameraUri!!)
    }

    Column(modifier = modifier) {
        ScoutLabel(label = label, enableHorizontalPadding = false)

        ImageBox(
            uri = uri,
            modifier = Modifier.clickable { showSheet = true },
            aspectRatio = aspectRatio
        )
    }

    // TODO: clean up
    if (showSheet) {
        ModalBottomSheet(
            modifier = Modifier.padding(ScoutTheme.margin),
            sheetState = sheetState,
            onDismissRequest = { showSheet = false },
        ) {
            ScoutLabel(label = "Choose Source")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ScoutTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium)
            ) {
                SheetItem(
                    icon = ScoutIcons.MobileCamera,
                    label = "Take photo"
                ) {
                    showSheet = false
                    launchCamera()
                }

                SheetItem(
                    icon = ScoutIcons.Image,
                    label = "Choose from gallery"
                ) {
                    showSheet = false
                    pickGallery.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            }
        }
    }
}


@Composable
private fun SheetItem(
    icon: Int,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ScoutTheme.shapes.cornerMedium)
            .clickable(onClick = onClick)
            .padding(ScoutTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null
        )
        Spacer(Modifier.width(ScoutTheme.spacing.medium))
        ScoutLabel(label = label)
    }
}
