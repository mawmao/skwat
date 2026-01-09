package com.humayapp.scout.feature.form.impl.model

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.humayapp.scout.feature.form.impl.ui.util.ScoutInputTransformations
import com.humayapp.scout.feature.form.impl.ui.util.ScoutOutputTransformations
import java.time.LocalDate
import java.time.format.DateTimeFormatter

typealias WizardPageOverrides = Map<WizardEntry, @Composable (WizardEntry) -> Unit>
typealias WizardAnswer = Map<String, Any>

fun field(
    key: String,
    label: String,
    type: FieldType,
    options: List<String>? = null,
    imeAction: ImeAction = ImeAction.Unspecified,
    validator: Validator? = null
) = WizardField(key, label, type, options, imeAction, validator)


abstract class WizardEntry {
    abstract val title: String
    abstract val description: String
    abstract val fields: List<WizardField>

    open fun nextScreen(answers: Map<String, Any?>): WizardEntry? = null
}

sealed interface ValidationResult {
    object Valid : ValidationResult
    data class Invalid(val message: String) : ValidationResult
}

typealias Validator = (String) -> ValidationResult

object Validators {
    val nonEmpty: Validator = { if (it.isBlank()) ValidationResult.Invalid("This field is required") else ValidationResult.Valid }

    val name: Validator = {
        when {
            it.isBlank() -> ValidationResult.Invalid("Required")
            it.length < 2 -> ValidationResult.Invalid("Too short")
            else -> ValidationResult.Valid
        }
    }

    val phone: Validator = {
        val cleaned = it.filter { ch -> ch.isDigit() }
        if (cleaned.length < 10) ValidationResult.Invalid("Enter a valid phone number")
        else ValidationResult.Valid
    }

    val positiveDecimal: Validator = {
        try {
            val d = it.toDouble()
            if (d <= 0.0) ValidationResult.Invalid("Must be greater than 0") else ValidationResult.Valid
        } catch (e: Exception) { ValidationResult.Invalid("Invalid number") }
    }

    val requiredDropdown: Validator = { if (it.isBlank()) ValidationResult.Invalid("Select an option") else ValidationResult.Valid }

    fun notFutureDate(format: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE): Validator = { value ->
        if (value.isBlank()) ValidationResult.Invalid("Required")
        else try {
            val d = LocalDate.parse(value, format)
            if (d.isAfter(LocalDate.now())) ValidationResult.Invalid("Date cannot be in the future")
            else ValidationResult.Valid
        } catch (e: Exception) {
            ValidationResult.Invalid("Invalid date")
        }
    }
}

@Immutable
data class WizardField(
    val key: String,
    val label: String,
    val type: FieldType,
    val options: List<String>? = null,
    val imeAction: ImeAction = ImeAction.Unspecified,
    val validator: Validator? = null
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
    NUM_WHOLE(
        inputTransformation = ScoutInputTransformations.Whole,
        keyboardType = KeyboardType.Number,
    ),
    NUM_DECIMAL(
        inputTransformation = ScoutInputTransformations.Decimal,
        outputTransformation = ScoutOutputTransformations.Decimal,
        keyboardType = KeyboardType.Number,
    ),
    NUM_DECIMAL_OR_NA(
        inputTransformation = ScoutInputTransformations.DecimalOrNA,
        outputTransformation = ScoutOutputTransformations.DecimalOrNA,
        keyboardType = KeyboardType.Number,
    ),
    NUM_PERCENT(
        inputTransformation = ScoutInputTransformations.Percentage,
        outputTransformation = ScoutOutputTransformations.Percentage,
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
    IMAGE,
    CARD_RADIO,
    DATE,

    TEXT, // discouraged because this can lead to error-prone data
}