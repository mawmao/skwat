package com.humayapp.scout.feature.form.impl.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.humayapp.scout.core.ui.component.ImageBox
import com.humayapp.scout.core.ui.component.ScoutLabel
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.util.scoutClickable

sealed class ImageActionState {
    data object Idle : ImageActionState()

    data class SelectingSource(val fieldKey: String) : ImageActionState()

    data class Preview(
        val fieldKey: String,
        val uri: Uri
    ) : ImageActionState()

    data class Cropping(
        val fieldKey: String,
        val uri: Uri
    ) : ImageActionState()
}

@Composable
fun ImagePickerBox(
    label: String,
    uri: Uri?,
    modifier: Modifier = Modifier,
    aspectRatio: Float? = null,
    onClick: () -> Unit
) {
    Column(modifier = modifier) {
        ScoutLabel(label = label, enableHorizontalPadding = false)
        ImageBox(
            uri = uri,
            aspectRatio = aspectRatio,
            modifier = Modifier
                .clip(ScoutTheme.shapes.cornerMedium)
                .scoutClickable(onClick = onClick)
        )
    }
}

