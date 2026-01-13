package com.humayapp.scout.feature.form.impl.data.registry.cultural.overrides

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.util.fastForEach
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.cultural.CulturalManagement
import com.humayapp.scout.feature.form.impl.data.registry.cultural.CulturalManagement.Companion.RICE_VARIETY_KEY
import com.humayapp.scout.feature.form.impl.data.registry.cultural.CulturalManagement.Companion.RICE_VARIETY_MATURITY_DURATION_KEY
import com.humayapp.scout.feature.form.impl.data.registry.cultural.CulturalManagement.Companion.RICE_VARIETY_NO_KEY
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry
import com.humayapp.scout.feature.form.impl.ui.components.WizardField

@Composable
fun RiceVarietyInformationPage(page: CulturalManagement.RiceVarietyInformation) {

    val formState = LocalFormState.current

    WizardEntry(page) { entry ->
        entry.fields.fastForEach { field ->
            when (field.key) {
                RICE_VARIETY_KEY -> {
                    WizardField(
                        modifier = Modifier.fillMaxWidth(),
                        field = field,
                        value = { formState.getFieldData(field.key) },
                        onValueChange = {
                            formState.setFieldData(field.key, it)
                            formState.clearFieldData(RICE_VARIETY_NO_KEY)
                        },
                        imeAction = ImeAction.Next,
                    )
                }

                RICE_VARIETY_NO_KEY -> {
                    val riceVariety = formState.getFieldData(RICE_VARIETY_KEY)
                    WizardField(
                        modifier = Modifier.fillMaxWidth(),
                        visible = riceVariety != "Other" && riceVariety.isNotEmpty(),
                        field = field,
                        value = {
                            if (riceVariety == "Other") {
                                formState.setFieldData(field.key, "N/A")
                                formState.getFieldData(field.key)
                            } else {
                                formState.getFieldData(field.key)
                            }
                        },
                        onValueChange = { formState.setFieldData(field.key, it) },
                        imeAction = ImeAction.Next
                    )
                }

                RICE_VARIETY_MATURITY_DURATION_KEY -> {
                    WizardField(
                        field = field,
                        value = { formState.getFieldData(field.key) },
                        onValueChange = { formState.setFieldData(field.key, it) },
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = field.imeAction
                    )
                }
            }

            Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
        }
    }

}