package com.humayapp.scout.feature.form.impl

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.impl.model.FieldStore
import com.humayapp.scout.feature.form.impl.model.FieldValidator
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardField
import com.humayapp.scout.feature.form.impl.model.WizardNavigator

val LocalFormState = staticCompositionLocalOf<FormState> {
    error("LocalFormState not provided")
}

@Composable
fun rememberFormState(formType: FormType): FormState {

    val pagerState = rememberPagerState(pageCount = { formType.entries.size })

    return remember(formType) {
        FormState(
            initialWizardEntry = formType.startEntry,
            pagerEntries = formType.entries,
            formType = formType,
            pagerState = pagerState
        )
    }
}


@Stable
class FormState(
    initialWizardEntry: WizardEntry,
    pagerEntries: List<WizardEntry>,

    val formType: FormType,
    val pagerState: PagerState
) {
    var mfid: String = ""

    val allFields = pagerEntries.flatMap { it.fields }

    private val _fieldStore = FieldStore(allFields)
    private val _fieldValidator = FieldValidator()
    private val _navigator = WizardNavigator(initialWizardEntry)

    var dialogState by mutableStateOf<Dialog?>(null)

    val fieldData: Map<String, Any?> get() = _fieldStore.data
    val currentScreen get() = _navigator.currentScreen

    val canScrollNext get() = _navigator.hasNext(fieldData)
    val canScrollBack get() = pagerState.currentPage != 0

    fun scrollWizardNext() = _navigator.next(fieldData)
    fun scrollWizardBack() = _navigator.back()

    fun hasError(key: String) = _fieldValidator.hasErrors(key)
    fun getError(key: String): String? = _fieldValidator.getError(key)
    fun clearError(key: String) = _fieldValidator.clearError(key)

    fun clearFieldData(key: String) = _fieldStore.clear(key)
    fun getFieldData(key: String): String = _fieldStore.getString(key)
    fun hasFieldData(key: String) = getFieldData(key).isNotEmpty()
    fun setFieldData(key: String, value: Any?) {
        _fieldValidator.clearError(key)
        _fieldStore.set(key, value)
    }

    fun validateField(field: WizardField): Boolean = _fieldValidator.validateField(field, fieldData)
    fun validatePage(entry: WizardEntry): Boolean = _fieldValidator.validatePage(entry, fieldData)

    fun setDialog(state: Dialog) {
        dialogState = state
    }

    fun clearDialog() {
        dialogState = null
    }

    data class Dialog(val title: String = "", val message: String = "")
}

