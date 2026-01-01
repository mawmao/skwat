package com.humayapp.scout.feature.form.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.api.id

private fun createLogTag(formType: FormType) = "Scout: [${formType.id}] state"

// TODO: make to rememberSaveable

@Composable
fun rememberFormState(formType: FormType): FormState {
    return remember(formType) { FormState(formType) }
}



class FormState(
    val formType: FormType
) {

}
