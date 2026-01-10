package com.humayapp.scout.feature.form.impl.data.registry.monitoring.overrides

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.util.fastForEach
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.MonitoringVisit
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.MonitoringVisit.Companion.AVG_PLANT_HEIGHT_KEY
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.MonitoringVisit.Companion.CROP_STAGE_KEY
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.MonitoringVisit.Companion.SOIL_MOISTURE_STATUS_KEY
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry
import com.humayapp.scout.feature.form.impl.ui.components.WizardField

@Composable
fun ConditionPage(page: MonitoringVisit.Conditions) {

    val formState = LocalFormState.current

    WizardEntry(page) { entry ->
        entry.fields.fastForEach { field ->
            when (field.key) {
                CROP_STAGE_KEY -> {
                    WizardField(
                        modifier = Modifier.fillMaxWidth(),
                        field = field,
                        value = { formState.getAnswer(field.key) },
                        onValueChange = { formState.setAnswer(field.key, it) },
                        imeAction = ImeAction.Next,
                    )
                }

                SOIL_MOISTURE_STATUS_KEY -> {
                    WizardField(
                        modifier = Modifier.fillMaxWidth(),
                        field = field,
                        value = { formState.getAnswer(field.key) },
                        onValueChange = { formState.setAnswer(field.key, it) },
                    )
                }

                AVG_PLANT_HEIGHT_KEY -> {
                    val cropStage = formState.getAnswer(CROP_STAGE_KEY)
                    WizardField(
                        modifier = Modifier.fillMaxWidth(),
                        field = field,
                        visible = cropStage != "Not Yet Planted" && formState.hasAnswer(CROP_STAGE_KEY),
                        value = { formState.getAnswer(field.key) },
                        onValueChange = { formState.setAnswer(field.key, it) },
                        imeAction = ImeAction.Done,
                    )
                }
            }

            Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
        }
    }
}