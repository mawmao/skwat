package com.humayapp.scout.feature.form.impl

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.model.FieldType
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import java.time.LocalDate

val LocalFormState = staticCompositionLocalOf<FormState> {
    error("LocalFormState not provided")
}

@Composable
fun rememberFormState(formType: FormType): FormState {

    val pagerState = rememberPagerState(pageCount = { formType.entries.size })

    return remember(formType) {
        FormState(
            initialWizardEntry = formType.startEntry ?: error("no wizard entry provided"),
            entries = formType.entries,
            formType = formType,
            pagerState = pagerState
        )
    }
}

@Stable
class FormState(
    initialWizardEntry: WizardEntry,
    val entries: List<WizardEntry>,
    val formType: FormType,
    val pagerState: PagerState
) {
    var mfid: String = ""

    // tracks next and previous screens
    // could be changed to a single mutable state of wizard screen
    // that track the previous screen
    private val _stack = mutableStateListOf<WizardEntry>()

    private val _answers = mutableStateMapOf<String, Any>()
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

    val answers = _answers
    val currentScreen by _currentScreen

    val canScrollNext get() = _currentScreen.value.nextScreen(answers) != null
    val canScrollBack get() = pagerState.currentPage != 0

    fun setAnswer(key: String, value: Any) = _answers.set(key, value)
    fun getAnswer(key: String): String = _answers[key] as? String ?: ""

    fun scrollWizardNext() {
        val nextScreen = _currentScreen.value.nextScreen(answers)
        if (nextScreen != null) {
            _stack.add(nextScreen)
            _currentScreen.value = nextScreen
        }
    }

    fun scrollWizardBack() {
        if (_stack.size > 1) {
            _stack.removeAt(_stack.lastIndex)
            _currentScreen.value = _stack.last()
        }
    }
}

