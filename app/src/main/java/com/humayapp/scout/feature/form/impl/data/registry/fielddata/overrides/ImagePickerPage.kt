package com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides

import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.ui.components.FormImagesLayout
import com.humayapp.scout.feature.form.impl.ui.components.ImagePickerBox
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry

@Composable
fun ImagesPage(page: FieldData.Images) {

    val state = LocalFormState.current

    WizardEntry(page) {
        FormImagesLayout(
            items = page.fields
        ) { field, aspectRatio, modifier ->
            ImagePickerBox(
                fieldKey = field.key,
                label = field.label,
                aspectRatio = aspectRatio,
                modifier = modifier,
                uri = state.getFieldData(field.key).toUri(),
                onImageSelected = { k, uri ->
                    state.setFieldData(k, uri.toString())
                }
            )
        }
    }
}

