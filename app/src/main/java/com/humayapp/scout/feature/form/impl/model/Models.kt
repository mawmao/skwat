package com.humayapp.scout.feature.form.impl.model

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.humayapp.scout.feature.form.impl.ui.util.ScoutInputTransformations
import com.humayapp.scout.feature.form.impl.ui.util.ScoutOutputTransformations

typealias WizardPageOverrides = Map<WizardEntry, @Composable (WizardEntry) -> Unit>
typealias WizardAnswer = Map<String, Any>

fun field(
    key: String,
    label: String,
    type: FieldType,
    options: List<String>? = null,
    imeAction: ImeAction = ImeAction.Unspecified
) =
    WizardField(key, label, type, options, imeAction)


abstract class WizardEntry {
    abstract val title: String
    abstract val description: String
    abstract val fields: List<WizardField>

    // should take `answers` from conditional navigation
    open fun nextScreen(answers: Map<String, Any>): WizardEntry? = null
}

@Immutable
data class WizardField(
    val key: String,
    val label: String,
    val type: FieldType,
    val options: List<String>? = null,
    val imeAction: ImeAction = ImeAction.Unspecified
)

enum class FieldType(
    val inputTransformation: InputTransformation? = null,
    val outputTransformation: OutputTransformation? = null,
    val keyboardType: KeyboardType = KeyboardType.Unspecified
) {
    NAME(
        inputTransformation = ScoutInputTransformations.Name,
        keyboardType = KeyboardType.Text,
    ),
    NUM_DECIMAL(
        inputTransformation = ScoutInputTransformations.Decimal,
        outputTransformation = ScoutOutputTransformations.Decimal,
        keyboardType = KeyboardType.Number,
    ),
    NUM_PHONE(
        inputTransformation = ScoutInputTransformations.PhoneNumber,
        outputTransformation = ScoutOutputTransformations.PhoneNumber,
        keyboardType = KeyboardType.Number,
    ),
    DROPDOWN,
    DROPDOWN_SEARCHABLE,
    GPS,
    CARD_RADIO,
    DATE,
}