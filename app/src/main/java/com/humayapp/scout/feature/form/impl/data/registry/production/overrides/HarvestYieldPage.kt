package com.humayapp.scout.feature.form.impl.data.registry.production.overrides

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.util.fastForEach
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.production.Production
import com.humayapp.scout.feature.form.impl.data.registry.production.Production.Companion.AVG_BAG_WEIGHT_KEY
import com.humayapp.scout.feature.form.impl.data.registry.production.Production.Companion.BAGS_HARVESTED_KEY
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry
import com.humayapp.scout.feature.form.impl.ui.components.WizardField

@Composable
fun HarvestYieldPage(page: Production.HarvestYield) {

    val formState = LocalFormState.current

    WizardEntry(page) { entry ->
        entry.fields.fastForEach { field ->
            when (field.key) {
                BAGS_HARVESTED_KEY -> {
                    WizardField(
                        modifier = Modifier.fillMaxWidth(),
                        field = field,
                        value = { formState.getFieldData(field.key) },
                        onValueChange = { formState.setFieldData(field.key, it) },
                        imeAction = ImeAction.Next,
                    )
                }

                AVG_BAG_WEIGHT_KEY -> {
                    val bagsHarvested = formState.getFieldData(BAGS_HARVESTED_KEY)
                    WizardField(
                        modifier = Modifier.fillMaxWidth(),
                        visible = bagsHarvested != "0",
                        field = field,
                        value = { formState.getFieldData(field.key) },
                        onValueChange = { formState.setFieldData(field.key, it) },
                        imeAction = ImeAction.Done
                    )
                }
            }

            Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
        }
    }
}