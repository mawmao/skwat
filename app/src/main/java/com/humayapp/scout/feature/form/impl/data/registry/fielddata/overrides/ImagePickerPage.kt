package com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import com.humayapp.scout.core.ui.common.image.ImageCropDialog
import com.humayapp.scout.core.ui.common.image.ImageOptionsBottomSheet
import com.humayapp.scout.core.ui.common.image.ImagePreviewDialog
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.ui.components.FormImagesLayout
import com.humayapp.scout.feature.form.impl.ui.components.ImageActionState
import com.humayapp.scout.feature.form.impl.ui.components.ImagePickerBox
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry

@Composable
fun ImagesPage(page: FieldData.Images) {

    val formState = LocalFormState.current
    var actionState by remember { mutableStateOf<ImageActionState>(ImageActionState.Idle) }

    WizardEntry(page) {
        FormImagesLayout(items = page.fields) { field, aspectRatio, modifier ->
            ImagePickerBox(
                label = field.label,
                uri = formState.getFieldData(field.key).toUri(),
                aspectRatio = aspectRatio,
                modifier = modifier,
                onClick = { actionState = ImageActionState.SelectingSource(field.key) }
            )
        }
    }

    if (actionState is ImageActionState.SelectingSource) {
        val state = actionState as ImageActionState.SelectingSource

        ImageOptionsBottomSheet(
            key = state.fieldKey,
            hasImage = formState.getFieldData(state.fieldKey).isNotBlank(),
            onDismiss = { actionState = ImageActionState.Idle },
            onRemove = {
                formState.clearFieldData(state.fieldKey)
                actionState = ImageActionState.Idle
            },
            onEdit = {
                val currentUri = formState.getFieldData(state.fieldKey)
                if (currentUri.isNotBlank()) {
                    actionState = ImageActionState.Cropping(state.fieldKey, currentUri)
                }
            },
            onPreview = {
                val currentUri = formState.getFieldData(state.fieldKey)
                if (currentUri.isNotBlank()) {
                    actionState = ImageActionState.Preview(state.fieldKey, currentUri)
                }
            },
            onImageResult = { uri ->
                actionState = ImageActionState.Cropping(state.fieldKey, uri.toString())
            }
        )
    }

    if (actionState is ImageActionState.Cropping) {
        val state = actionState as ImageActionState.Cropping
        Log.d(LOG_TAG, "Show CropDialog key=${state.fieldKey} uri=${state.uri}")

        ImageCropDialog(
            uri = state.uri,
            onDismiss = {
                Log.d(LOG_TAG, "CropDialog dismissed")
                actionState = ImageActionState.Idle
            },
            onCropComplete = { croppedUri ->
                Log.d(LOG_TAG, "Crop complete uri=$croppedUri")
                formState.setFieldData(state.fieldKey, croppedUri.toString())
                actionState = ImageActionState.Idle
            }
        )
    }

    if (actionState is ImageActionState.Preview) {
        val state = actionState as ImageActionState.Preview

        ImagePreviewDialog(
            uri = state.uri,
            onDismiss = { actionState = ImageActionState.Idle },
        )
    }
}

private const val LOG_TAG = "Scout: ImagesPage"