package com.humayapp.scout.feature.form.impl.model

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf

@Stable
class FieldValidator {
    val errors: MutableMap<String, String?> = mutableStateMapOf()

    fun hasErrors(key: String) = !errors[key].isNullOrBlank()
    fun getError(key: String): String? = errors[key]
    fun clearError(key: String) = errors.set(key, null)
    fun setError(key: String, err: String) = errors.set(key, err)

    fun validateField(
        field: WizardField,
        data: Map<String, Any?>
    ): Boolean {
        val value = data[field.key] as? String ?: ""
        val validator = field.validator ?: Validators.nonEmpty

        return when (val result = validator(value, data)) {
            ValidationResult.Valid -> {
                errors[field.key] = null
                true
            }
            is ValidationResult.Invalid -> {
                errors[field.key] = result.message
                false
            }
        }
    }

    fun validatePage(
        entry: WizardEntry,
        data: Map<String, Any?>
    ): Boolean {
        var ok = true
        entry.fields.forEach {
            if (!validateField(it, data)) ok = false
        }
        return ok
    }

    fun validatePageSilent(
        entry: WizardEntry,
        data: Map<String, Any?>
    ): Boolean =
        entry.fields.all { field ->
            val value = data[field.key] as? String ?: ""
            val validator = field.validator ?: Validators.nonEmpty
            validator(value, data) is ValidationResult.Valid
        }
}