package com.humayapp.scout.feature.form.impl.model

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import com.humayapp.scout.feature.form.impl.FormState

fun fieldThresholdRule(
    key: String,
    threshold: Float,
    message: (Float) -> String = { "Value exceeds limit. Press OK to proceed." },
    shouldTrigger: (Float, Float) -> Boolean = { value, limit -> value > limit }
): (FormState) -> Boolean {
    return { state ->
        val value = state.getFieldData(key).toFloatOrNull()

        if (value != null &&
            shouldTrigger(value, threshold) &&
            !state.acknowledgedWarnings.contains(key)
        ) {
            state.setDialog(
                FormState.Dialog(
                    title = "Warning",
                    message = message(value),
                    fieldKey = key
                )
            )
            false
        } else {
            true
        }
    }
}

@Stable
class FieldValidator {
    val errors: MutableMap<String, String?> = mutableStateMapOf()

    fun hasErrors(key: String) = !errors[key].isNullOrBlank()
    fun getError(key: String): String? = errors[key]
    fun clearError(key: String) = errors.set(key, null)

    fun validateField(field: WizardField, data: Map<String, Any?>): Boolean {

        val value = data[field.key] as? String ?: ""
        val validator = field.validator ?: Validators.nonEmpty

        return when (val result = validator(value, data)) {
            ValidationResult.Valid -> {
                errors[field.key] = null
                Log.v(LOG_TAG, "  [OK] ${field.key}")
                true
            }

            is ValidationResult.Invalid -> {
                errors[field.key] = result.message
                Log.v(LOG_TAG, "  [FAIL] ${field.key}")
                false
            }
        }
    }

    fun validatePage(entry: WizardEntry, data: Map<String, Any?>): Boolean {
        var ok = true
        Log.v(LOG_TAG, "[Validator] Validating ${entry.title}.")
        entry.fields.forEach { field ->
            if (!validateField(field, data)) ok = false
        }
        Log.v(LOG_TAG, "[Validator] ${entry.title} is ${if (ok) "valid" else "invalid"}.")
        return ok
    }

    companion object {
        private const val LOG_TAG = "Scout: FieldValidator"
    }

}