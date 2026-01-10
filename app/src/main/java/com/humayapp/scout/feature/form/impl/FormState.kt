package com.humayapp.scout.feature.form.impl

import android.util.Log
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.ValidationResult
import com.humayapp.scout.feature.form.impl.model.Validators
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardField
import java.time.LocalDate

val LocalFormState = staticCompositionLocalOf<FormState> {
    error("LocalFormState not provided")
}

@Composable
fun rememberFormState(formType: FormType): FormState {

    val pagerState = rememberPagerState(pageCount = { formType.entries.size })

    return remember(formType) {
        FormState(
            initialWizardEntry = formType.startEntry,
            entries = formType.entries,
            formType = formType,
            pagerState = pagerState
        )
    }
}

data class FormDialogState(
    val title: String = "",
    val message: String = "",
)

@Stable
class FormState(
    initialWizardEntry: WizardEntry,
    val entries: List<WizardEntry>,
    val formType: FormType,
    val pagerState: PagerState
) {
    var mfid: String = ""

    private val _stack = mutableStateListOf<WizardEntry>()
    private val _answers = mutableStateMapOf<String, Any?>()
    private val _currentScreen = mutableStateOf(initialWizardEntry)

    init {
        _stack.add(initialWizardEntry)
        entries
            .flatMap { it.fields }
            .filter { it.type == FieldType.DATE }
            .forEach { field ->
                _answers[field.key] = LocalDate.now().toString()
            }
    }

    var dialogState by mutableStateOf<FormDialogState?>(null)

    val errors = mutableStateMapOf<String, String?>()

    val answers = _answers
    val currentScreen by _currentScreen

    val hasNextScreen get() = _currentScreen.value.nextScreen(answers) != null
    val canScrollNext get() = validatePageSilent(_currentScreen.value) && hasNextScreen
    val canScrollBack get() = pagerState.currentPage != 0

    fun hasError(key: String) = !errors[key].isNullOrBlank()
    fun getError(key: String): String? = errors[key]
    fun resetError(key: String) = errors.set(key, null)

    fun removeAnswer(key: String) {
        _answers[key] = ""
        Log.d("Scout: FormState", "Removing answer of $key")
    }

    fun setAnswer(key: String, value: Any?) {
        if (hasError(key)) resetError(key) // reset error when setting new answer
        Log.d("Scout: FormState", "Setting answer of $key to $value")
        _answers[key] = value
    }

    fun getAnswer(key: String): String {
        Log.d("Scout: FormState", "Getting answer of $key. Returning \"${_answers[key] as? String ?: "empty string"}\"")
        return _answers[key] as? String ?: ""
    }

    fun hasAnswer(key: String) = getAnswer(key).isNotEmpty()

    fun scrollWizardNext() {
        if (!validatePage(_currentScreen.value)) return

        val nextScreen = _currentScreen.value.nextScreen(answers)
        if (nextScreen != null) {

            _stack.add(nextScreen)
            _currentScreen.value = nextScreen

            Log.d("Scout: FormState", "Scrolling next to ${nextScreen.title}")
        }
    }

    fun scrollWizardBack() {
        if (_stack.size > 1) {
            _stack.removeAt(_stack.lastIndex)
            _currentScreen.value = _stack.last()

            Log.d("Scout: FormState", "Scrolling back to ${_currentScreen.value}")
        }
    }


    fun validateField(key: String): Boolean {
        val value = answers[key] as? String ?: ""
        val field = entries.flatMap { it.fields }.find { it.key == key } ?: return true
        val validator = field.validator ?: Validators.nonEmpty
        return when (val r = validator(value)) {
            ValidationResult.Valid -> {
                errors[key] = null
                true
            }

            is ValidationResult.Invalid -> {
                errors[key] = r.message
                false
            }
        }
    }

    fun validateField(field: WizardField): Boolean {
        val value = answers[field.key] as? String ?: ""
        val validator = field.validator ?: Validators.nonEmpty
        return when (val r = validator(value)) {
            ValidationResult.Valid -> {
                errors[field.key] = null
                true
            }

            is ValidationResult.Invalid -> {
                errors[field.key] = r.message
                false
            }
        }
    }

    fun validatePage(entry: WizardEntry): Boolean {
        var allOk = true
        entry.fields.forEach { field ->
            val ok = validateField(field.key)
            if (!ok) allOk = false
        }
        return allOk
    }

    fun setDialog(state: FormDialogState) {
        dialogState = state
    }

    fun clearDialog() {
        dialogState = null
    }

    private fun validatePageSilent(entry: WizardEntry): Boolean {
        entry.fields.forEach { f ->
            val validator = f.validator ?: Validators.nonEmpty
            val a = answers[f.key] as? String ?: ""
            if (validator(a) is ValidationResult.Invalid) return false
        }
        return true
    }
}

