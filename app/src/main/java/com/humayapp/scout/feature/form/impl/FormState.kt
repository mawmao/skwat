package com.humayapp.scout.feature.form.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import com.humayapp.scout.feature.form.api.FormType

val LocalFormState = staticCompositionLocalOf<FormState> {
    error("LocalFormState not provided")
}

@Composable
fun rememberFormState(formType: FormType): FormState {
   return remember(formType) { FormState(formType) }
}

@Stable
class FormState(val formType: FormType) : ViewModel() {
    var mfid: String = ""
        private set

    fun setMfid(mfid: String) {
        this.mfid = mfid
    }
}

private const val LOG_TAG = "Scout: FormState"