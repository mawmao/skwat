package com.humayapp.scout.feature.form.impl.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.ui.component.ScoutCardRadioGroup
import com.humayapp.scout.core.ui.component.ScoutDateField
import com.humayapp.scout.core.ui.component.ScoutDropdownMenu
import com.humayapp.scout.core.ui.component.ScoutTextField
import com.humayapp.scout.feature.form.impl.model.FieldType.CARD_RADIO
import com.humayapp.scout.feature.form.impl.model.FieldType.DATE
import com.humayapp.scout.feature.form.impl.model.FieldType.DROPDOWN
import com.humayapp.scout.feature.form.impl.model.FieldType.DROPDOWN_SEARCHABLE
import com.humayapp.scout.feature.form.impl.model.FieldType.NAME
import com.humayapp.scout.feature.form.impl.model.FieldType.NUM_DECIMAL
import com.humayapp.scout.feature.form.impl.model.FieldType.NUM_PHONE
import com.humayapp.scout.feature.form.impl.model.WizardField


@Composable
fun ColumnScope.WizardField(
    field: WizardField,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction,
) = when (field.type) {

    NAME, NUM_DECIMAL, NUM_PHONE -> ScoutTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = field.label,
        inputTransformation = field.type.inputTransformation,
        outputTransformation = field.type.outputTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = field.type.keyboardType,
            imeAction = imeAction
        )
    )

    CARD_RADIO -> ScoutCardRadioGroup(
        modifier = modifier,
        selectedOption = value,
        onOptionSelected = onValueChange,
        label = field.label,
        options = field.options ?: unreachable("options in this context should never be null"),
    )


    DROPDOWN, DROPDOWN_SEARCHABLE -> {
        ScoutDropdownMenu(
            modifier = modifier,
            selectedOption = value,
            onOptionSelected = onValueChange,
            label = field.label,
            options = field.options ?: emptyList(),
        )
    }

    DATE -> {
        ScoutDateField(
            modifier = modifier,
            value = value,
            onValueChange = { onValueChange(it) },
            label = field.label
        )
    }

    else -> {
        ScoutTextField(
            modifier = modifier,
            value = value,
            onValueChange = onValueChange,
            label = field.label,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = imeAction
            )
        )
    }
}
