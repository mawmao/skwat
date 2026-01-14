package com.humayapp.scout.feature.form.impl.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import com.humayapp.scout.core.common.unreachable
import com.humayapp.scout.core.ui.component.ScoutCardRadioGroup
import com.humayapp.scout.core.ui.component.ScoutDateField
import com.humayapp.scout.core.ui.component.ScoutDropdownMenu
import com.humayapp.scout.core.ui.component.ScoutTextField
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.LocalFormState
import com.humayapp.scout.feature.form.impl.model.FieldType.CARD_RADIO
import com.humayapp.scout.feature.form.impl.model.FieldType.DATE
import com.humayapp.scout.feature.form.impl.model.FieldType.DROPDOWN
import com.humayapp.scout.feature.form.impl.model.FieldType.DROPDOWN_SEARCHABLE
import com.humayapp.scout.feature.form.impl.model.FieldType.GPS
import com.humayapp.scout.feature.form.impl.model.FieldType.NAME
import com.humayapp.scout.feature.form.impl.model.FieldType.NUM_DECIMAL
import com.humayapp.scout.feature.form.impl.model.FieldType.NUM_PERCENT
import com.humayapp.scout.feature.form.impl.model.FieldType.NUM_PHONE
import com.humayapp.scout.feature.form.impl.model.FieldType.NUM_WHOLE
import com.humayapp.scout.feature.form.impl.model.WizardField


@Composable
fun WizardField(
    modifier: Modifier = Modifier,
    field: WizardField,
    value: () -> String,
    onValueChange: (String) -> Unit,
    dynamicOptions: List<String>? = null,
    imeAction: ImeAction = ImeAction.Unspecified,
) {
    WizardFieldImpl(
        modifier = modifier,
        field = field,
        value = value,
        onValueChange = onValueChange,
        dynamicOptions = dynamicOptions,
        imeAction = imeAction,
    )
}


@Composable
fun WizardField(
    modifier: Modifier = Modifier,
    field: WizardField,
    value: () -> String,
    onValueChange: (String) -> Unit,
    dynamicOptions: List<String>? = null,
    imeAction: ImeAction,
    visible: Boolean = true
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { fullHeight -> -fullHeight }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { fullHeight -> -fullHeight }) + fadeOut()
    ) {
        WizardFieldImpl(
            modifier = modifier,
            field = field,
            value = value,
            onValueChange = onValueChange,
            dynamicOptions = dynamicOptions,
            imeAction = imeAction,
        )
    }
}


@Composable
fun WizardFieldImpl(
    modifier: Modifier = Modifier,
    field: WizardField,
    value: () -> String,
    onValueChange: (String) -> Unit,
    dynamicOptions: List<String>? = null,
    imeAction: ImeAction,
) {

    val state = LocalFormState.current

    val focusRequester = remember { FocusRequester() }

    val error by remember(state, field.key) {
        derivedStateOf { state.getError(field.key) }
    }

    val currentValue by remember(value) {
        derivedStateOf { value() }
    }

    val focusModifier = modifier
        .focusRequester(focusRequester)
        .onFocusChanged {
            if (it.isFocused && state.hasError(field.key)) state.clearError(field.key)
        }


    when (field.type) {
        NAME, NUM_WHOLE, NUM_DECIMAL, NUM_PHONE, NUM_PERCENT -> {
            ScoutTextField(
                modifier = focusModifier,
                value = currentValue,
                onValueChange = onValueChange,
                label = field.label,
                inputTransformation = field.type.inputTransformation,
                outputTransformation = field.type.outputTransformation,
                isError = error != null,
                errorMessage = error,
                keyboardOptions = KeyboardOptions(
                    keyboardType = field.type.keyboardType,
                    imeAction = imeAction
                ),
            )
        }

        CARD_RADIO -> {
            ScoutCardRadioGroup(
                modifier = focusModifier,
                label = field.label,
                options = dynamicOptions ?: field.options
                ?: unreachable("options in this context should never be null"),
                isError = error != null,
                errorMessage = error,
                selectedOption = currentValue,
                onOptionSelected = onValueChange,
            )
        }

        DROPDOWN, DROPDOWN_SEARCHABLE -> {
            ScoutDropdownMenu(
                modifier = focusModifier,
                selectedOption = currentValue,
                onOptionSelected = onValueChange,
                label = field.label,
                options = dynamicOptions ?: field.options ?: emptyList(),
                isError = error != null,
                errorMessage = error,
            )
        }

        DATE -> {
            ScoutDateField(
                modifier = focusModifier,
                value = currentValue,
                onValueChange = onValueChange,
                label = field.label,
                isError = error != null,
                errorMessage = error,
            )
        }

        GPS -> {
            ScoutTextField(
                modifier = modifier.fillMaxWidth(),
                value = currentValue,
                onValueChange = onValueChange,
                isError = error != null,
                errorMessage = error,
                label = field.label,
                readOnly = true
            )
        }

        else -> {
            ScoutTextField(
                modifier = focusModifier,
                value = currentValue,
                onValueChange = onValueChange,
                label = field.label,
                inputTransformation = field.type.inputTransformation,
                outputTransformation = field.type.outputTransformation,
                isError = error != null,
                errorMessage = error,
                keyboardOptions = KeyboardOptions(
                    keyboardType = field.type.keyboardType,
                    imeAction = imeAction
                ),
            )
        }
    }
}


// convenience composable
// should only be used on form flow
@Composable
fun DefaultWizardField(field: WizardField, modifier: Modifier = Modifier) {
    val state = LocalFormState.current
    WizardField(
        modifier = modifier,
        field = field,
        value = { state.getFieldData(field.key) },
        onValueChange = { v -> state.setFieldData(field.key, v) },
    )
    Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
}
