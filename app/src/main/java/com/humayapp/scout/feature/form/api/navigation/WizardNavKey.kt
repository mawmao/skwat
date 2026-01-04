package com.humayapp.scout.feature.form.api.navigation

import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.feature.form.impl.WizardGroupId
import com.humayapp.scout.feature.form.impl.model.WizardField


abstract class WizardNavKey : NavKey {
    abstract val title: String
    abstract val description: String
    abstract val fields: List<WizardField>
    open val nextKey: WizardNavKey? = null
    open val group: WizardGroupId? = null
}
