package com.humayapp.scout.feature.form.impl.data.registry.fielddata.overrides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.ui.components.FormReviewItem
import com.humayapp.scout.feature.form.impl.ui.components.WizardEntry
import com.humayapp.scout.feature.form.impl.ui.components.WizardField

@Composable
fun ConfirmFarmerPage(
    page: FieldData.ConfirmFarmer
) {
    val formState = LocalFormState.current
    val confirmKey = "confirm_farmer"

    WizardEntry(page) { entry ->
        Column(verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium)) {
            Column(verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.small)) {
                val firstName = formState.getFieldData(FieldData.FIRST_NAME_KEY)
                val lastName = formState.getFieldData(FieldData.LAST_NAME_KEY)
                val farmerName = listOf(firstName, lastName)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                    .ifEmpty { "Not provided" }

                FormReviewItem(
                    label = "Farmer Name",
                    value = farmerName
                )

                val gender = formState.getFieldData(FieldData.GENDER_KEY)
                if (gender.isNotBlank()) {
                    FormReviewItem(label = "Gender", value = gender)
                }

                val dob = formState.getFieldData(FieldData.DATE_OF_BIRTH_KEY)
                if (dob.isNotBlank()) {
                    FormReviewItem(label = "Date of Birth", value = dob)
                }

                val phone = formState.getFieldData(FieldData.CELLPHONE_NO_KEY)
                if (phone.isNotBlank()) {
                    FormReviewItem(label = "Cellphone No.", value = phone)
                }
            }
        }

        entry.fields.fastForEach { field ->
            when (field.key) {
                confirmKey -> {
                    Spacer(modifier = Modifier.height(ScoutTheme.spacing.small))
                    WizardField(
                        field = field,
                        value = { formState.getFieldData(confirmKey) },
                        onValueChange = { formState.setFieldData(confirmKey, it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
