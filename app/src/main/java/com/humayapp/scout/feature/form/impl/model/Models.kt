package com.humayapp.scout.feature.form.impl.model

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.net.toUri
import com.humayapp.scout.core.database.model.FormImageEntity
import com.humayapp.scout.feature.form.impl.FormState
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData.Companion.DATE_OF_BIRTH_KEY
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData.Companion.EST_CROP_ESTABLISHMENT_KEY
import com.humayapp.scout.feature.form.impl.ui.util.ScoutInputTransformations
import com.humayapp.scout.feature.form.impl.ui.util.ScoutOutputTransformations
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

typealias WizardPageOverrides = Map<WizardEntry, @Composable (WizardEntry) -> Unit>

typealias FieldData = Map<String, Any?>

fun FieldData.getOrEmpty(key: String) = this[key] as? String ?: ""

fun Map<String, Any?>.toFormImages(): List<FormImageEntity> {
    return this.mapNotNull { (key, value) ->
        if (key.startsWith("img_") && value is String && value.isNotBlank()) {
            FormImageEntity(localPath = value, formId = -1)
        } else null
    }
}

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

    open val nextRule: (FormState) -> Boolean = { true }
    open fun nextScreen(answers: Map<String, Any?>): WizardEntry? = null
}

sealed interface ValidationResult {
    object Valid : ValidationResult
    data class Invalid(val message: String) : ValidationResult
}

typealias Validator = (String, Map<String, Any?>) -> ValidationResult

fun validateIf(
    condition: (Map<String, Any?>) -> Boolean,
    validator: Validator
): Validator = { value, data ->
    if (!condition(data)) ValidationResult.Valid
    else validator(value, data)
}

object Validators {

    fun allOf(vararg validators: Validator): Validator = { value, answers ->
        var result: ValidationResult = ValidationResult.Valid
        for (validator in validators) {
            result = validator(value, answers)
            if (result is ValidationResult.Invalid) break
        }
        result
    }

    val nonEmpty: Validator = { value, _ ->
        when {
            value.isBlank() -> ValidationResult.Invalid("Required")
            else -> ValidationResult.Valid
        }
    }

    val name: Validator = { value, _ ->
        when {
            value.isBlank() -> ValidationResult.Invalid("Required")
            value.any { c -> !c.isLetter() && c !in listOf(' ', '-', '\'') } ->
                ValidationResult.Invalid("Use letters, spaces, hyphens, and apostrophes only ")

            value.count { c -> c.isLetter() } < 2 -> ValidationResult.Invalid("Enter at least 2 letters")

            else ->
                ValidationResult.Valid
        }
    }

    val phone: Validator = { value, _ ->
        when {
            value.isBlank() -> ValidationResult.Invalid("Required")
            value.any { ch -> !ch.isDigit() } -> ValidationResult.Invalid("Digits only")
            value.length != 11 -> ValidationResult.Invalid("Phone number must be 11 digits")
            !value.startsWith("09") -> ValidationResult.Invalid("Phone number must start with 09")
            else -> ValidationResult.Valid
        }
    }

    fun notExceedMonitoringArea(monitoringAreaKey: String): Validator = { value, answers ->
        when {
            value.isBlank() -> ValidationResult.Invalid("Required")
            else -> {
                val maxSqm = answers[monitoringAreaKey] as? Double
                if (maxSqm == null) {
                    ValidationResult.Invalid("Monitoring field area not available")
                } else {
                    try {
                        val sqm = value.toDouble()
                        if (sqm > maxSqm) {
                            ValidationResult.Invalid(
                                "Value cannot exceed the monitoring field area ($maxSqm sqm)"
                            )
                        } else ValidationResult.Valid
                    } catch (e: Exception) {
                        ValidationResult.Invalid("Invalid number")
                    }
                }
            }
        }
    }

    fun notExceedTotalFieldArea(totalAreaHaKey: String): Validator = { value, answers ->
        when {
            value.isBlank() -> ValidationResult.Invalid("Required")
            else -> {
                val totalAreaHa = answers[totalAreaHaKey] as? Double
                if (totalAreaHa == null) {
                    ValidationResult.Invalid("Total field area not available")
                } else {
                    try {
                        val sqm = value.toDouble()
                        val maxSqm = totalAreaHa * 10000.0
                        if (sqm > maxSqm) {
                            ValidationResult.Invalid(
                                "Monitoring area cannot exceed total field area (${totalAreaHa} ha = ${maxSqm.toInt()} sqm)"
                            )
                        } else ValidationResult.Valid
                    } catch (e: Exception) {
                        ValidationResult.Invalid("Invalid number")
                    }
                }
            }
        }
    }

    val positiveDecimal: Validator = { value, _ ->
        try {
            val d = value.toDouble()
            if (d <= 0.0) ValidationResult.Invalid("Must be greater than 0") else ValidationResult.Valid
        } catch (e: Exception) {
            ValidationResult.Invalid("Invalid number")
        }
    }

    fun minDecimal(
        min: Double,
        unit: String? = null,
        message: ((Double) -> String)? = null
    ): Validator = { value, _ ->
        if (value.isBlank()) ValidationResult.Invalid("Required")

        try {
            val number = value.toDouble()
            if (number < min) {
                ValidationResult.Invalid(
                    message?.invoke(min) ?: "Must be at least $min${unit?.let { " $it" } ?: ""}"
                )
            } else ValidationResult.Valid
        } catch (e: Exception) {
            ValidationResult.Invalid("Invalid number")
        }
    }


    fun floatRange(
        min: Float? = null,
        max: Float? = null,
        unit: String? = null,
        messageProvider: ((Float?, Float?, String?) -> String)? = null
    ): Validator = { value, _ ->
        when {
            value.isBlank() -> ValidationResult.Invalid("Required")
            else -> try {
                val number = value.toFloat()

                when {
                    min != null && number < min ->
                        ValidationResult.Invalid(
                            messageProvider?.invoke(min, max, unit)
                                ?: "Value must be at least $min${unit?.let { " $it" } ?: ""}")

                    max != null && number > max ->
                        ValidationResult.Invalid(
                            messageProvider?.invoke(min, max, unit)
                                ?: "Value must be at most $max${unit?.let { " $it" } ?: ""}")

                    else -> ValidationResult.Valid
                }
            } catch (e: Exception) {
                ValidationResult.Invalid("Invalid number")
            }
        }
    }

    fun intRange(
        min: Int? = null,
        max: Int? = null,
        unit: String? = null,
        messageProvider: ((Int?, Int?, String?) -> String)? = null
    ): Validator = { value, _ ->
        when {
            value.isBlank() -> ValidationResult.Invalid("Required")
            else -> try {
                val number = value.toInt()

                when {
                    min != null && number < min ->
                        ValidationResult.Invalid(
                            messageProvider?.invoke(min, max, unit)
                                ?: "Value must be at least $min${unit?.let { " $it" } ?: ""}"
                        )

                    max != null && number > max ->
                        ValidationResult.Invalid(
                            messageProvider?.invoke(min, max, unit)
                                ?: "Value must be at most $max${unit?.let { " $it" } ?: ""}"
                        )

                    else -> ValidationResult.Valid
                }
            } catch (e: Exception) {
                ValidationResult.Invalid("Invalid number")
            }
        }
    }

    private fun Period.toTotalMonthsOrDays(): String = when {
        this.years > 0 -> "${this.years} year(s)"
        this.months > 0 -> "${this.months} month(s)"
        else -> "${this.days} day(s)"
    }

    fun mustBeToday(): Validator = { value, _ ->
        when {
            value.isBlank() -> ValidationResult.Invalid("Required")
            else -> try {
                val date = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                val today = LocalDate.now()

                when {
                    date.isBefore(today) -> ValidationResult.Invalid("Date cannot be in the past")
                    date.isAfter(today) -> ValidationResult.Invalid("Date cannot be in the future")
                    else -> ValidationResult.Valid
                }
            } catch (e: Exception) {
                ValidationResult.Invalid("Invalid date")
            }
        }
    }

    fun notFutureDate(): Validator = { value, _ ->
        when {
            value.isBlank() -> ValidationResult.Invalid("Required")
            else -> try {
                val d = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                if (d.isAfter(LocalDate.now())) ValidationResult.Invalid("Date cannot be in the future")
                else ValidationResult.Valid
            } catch (e: Exception) {
                ValidationResult.Invalid("Invalid date")
            }
        }
    }

    fun dateOfBirth(min: Int, max: Int): Validator = { value, _ ->
        when {
            value.isBlank() -> ValidationResult.Invalid("Required")
            else -> try {
                val dob = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                val today = LocalDate.now()

                if (dob.isAfter(today)) ValidationResult.Invalid("Date cannot be in the future")

                val age = ChronoUnit.YEARS.between(dob, today).toInt()

                when {
                    age < min -> ValidationResult.Invalid("Must be at least $min years old")
                    age > max -> ValidationResult.Invalid("Must be no more than $max years old")
                    else -> ValidationResult.Valid
                }
            } catch (e: Exception) {
                ValidationResult.Invalid("Invalid date")
            }
        }
    }

    fun withinRange(
        period: Period // Period.ofDays() or Period.ofMonths
    ): Validator = { value, _ ->
        when {
            value.isBlank() -> ValidationResult.Invalid("Required")
            else -> try {
                val date = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                val today = LocalDate.now()

                val minDate = today.minus(period)
                val maxDate = today.plus(period)

                when {
                    date.isBefore(minDate) ->
                        ValidationResult.Invalid("Date must be within ${period.toTotalMonthsOrDays()} before today")

                    date.isAfter(maxDate) ->
                        ValidationResult.Invalid("Date must be within ${period.toTotalMonthsOrDays()} after today")

                    else ->
                        ValidationResult.Valid
                }
            } catch (e: Exception) {
                ValidationResult.Invalid("Invalid date")
            }
        }
    }

    fun isAfterBy(
        otherKey: String,
        after: Period? = null // Period.ofMonths()
    ): Validator = { value, answers ->
        when {
            value.isBlank() -> ValidationResult.Invalid("Required")
            else -> {
                val otherValue = answers[otherKey] as? String
                if (otherValue.isNullOrBlank())
                    ValidationResult.Invalid("The referenced date is required first")

                try {
                    val currentDate = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                    val otherDate = LocalDate.parse(otherValue, DateTimeFormatter.ISO_LOCAL_DATE)

                    val otherKeyName = otherKey.replace('_', ' ').lowercase()

                    when {
                        !currentDate.isAfter(otherDate) ->
                            ValidationResult.Invalid("Date must be after $otherKeyName")

                        after != null && currentDate.isAfter(otherDate.plus(after)) ->
                            ValidationResult.Invalid("Date must be within ${after.toTotalMonthsOrDays()} after ${otherKeyName}")

                        else -> ValidationResult.Valid
                    }
                } catch (e: Exception) {
                    ValidationResult.Invalid("Invalid date")
                }
            }
        }
    }

    fun lengthBasedOn(
        otherKey: String,
        mapping: Map<String, Int>,
        allowOther: Boolean = true
    ): Validator = { value, answers ->
        if (value.isBlank()) ValidationResult.Invalid("Required")
        val referenceValue = answers[otherKey] as? String ?: ValidationResult.Invalid("Select $otherKey first")
        val requiredLength = mapping[referenceValue]
        if (requiredLength != null) {
            if (value.length != requiredLength) {
                ValidationResult.Invalid("Must be a $requiredLength-digit number for $referenceValue")
            } else ValidationResult.Valid
        } else {
            if (allowOther) ValidationResult.Valid
            else ValidationResult.Invalid("No rule defined for $referenceValue")
        }
    }


    val image: Validator = { value, _ ->
        when {
            value.isBlank() -> ValidationResult.Invalid("Required")
            else ->
                try {
                    val uri = value.toUri()
                    if (uri.scheme.isNullOrBlank() || uri.path.isNullOrBlank()) {
                        ValidationResult.Invalid("Invalid image URI")
                    } else {
                        ValidationResult.Valid
                    }
                } catch (e: Exception) {
                    ValidationResult.Invalid("Invalid image URI")
                }
        }
    }


    val optionalImage: Validator = { value, _ ->
        when {
            value.isBlank() -> ValidationResult.Valid
            else ->
                try {
                    val uri = value.toUri()
                    if (uri.scheme.isNullOrBlank() || uri.path.isNullOrBlank()) {
                        ValidationResult.Invalid("Invalid image URI")
                    } else {
                        ValidationResult.Valid
                    }
                } catch (e: Exception) {
                    ValidationResult.Invalid("Invalid image URI")
                }
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


@Stable
class FieldStore(
    fields: List<WizardField>
) {
    private val _data = mutableStateMapOf<String, Any?>()

    val data: Map<String, Any?> get() = _data

    init {
        fields
            .filter { it.type == FieldType.DATE }
            .forEach { field ->
                _data[field.key] = defaultDateFor(field.key)
            }
    }

    fun getString(key: String): String =
        _data[key] as? String ?: ""

    fun hasValue(key: String): Boolean =
        getString(key).isNotBlank()

    fun set(key: String, value: Any?) {
        _data[key] = value
    }

    fun clear(key: String) {
        _data[key] = ""
    }

    private fun defaultDateFor(key: String): String =
        when (key) {
            DATE_OF_BIRTH_KEY ->
                LocalDate.now().minusYears(16).toString()

            EST_CROP_ESTABLISHMENT_KEY ->
                LocalDate.now().plusDays(1).toString()

            else ->
                LocalDate.now().toString()
        }
}
