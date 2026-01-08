package com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.ui.components.ImagePickerBox
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry

@Composable
fun ImagesPage(page: FieldData.Images) {

    val state = LocalFormState.current

    WizardEntry(page) {
        Spacer(Modifier.height(ScoutTheme.spacing.medium))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium)
        ) {
            page.fields.take(4).forEach { field ->
                ImagePickerBox(
                    fieldKey = field.key,
                    label = field.label,
                    aspectRatio = 1f,
                    modifier = Modifier.weight(1f),
                    uri = state.answers[field.key] as? Uri,
                    onImageSelected = { key, uri ->
                        state.setAnswer(key, uri)
                    }
                )
            }
        }

        Spacer(Modifier.height(ScoutTheme.spacing.medium))

        page.fields.getOrNull(4)?.let { field ->
            ImagePickerBox(
                fieldKey = field.key,
                label = field.label,
                aspectRatio = 16f / 9f,
                modifier = Modifier.fillMaxWidth(),
                uri = state.answers[field.key] as? Uri,
                onImageSelected = { key, uri ->
                    state.setAnswer(key, uri)
                }
            )
        }
    }
}

