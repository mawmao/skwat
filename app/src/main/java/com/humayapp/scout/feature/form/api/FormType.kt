package com.humayapp.scout.feature.form.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.util.fastFirst
import com.humayapp.scout.feature.form.impl.FormState
import com.humayapp.scout.feature.form.impl.data.registry.cultural.CulturalManagement
import com.humayapp.scout.feature.form.impl.data.registry.damage.DamageAssessment
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement
import com.humayapp.scout.feature.form.impl.data.registry.production.Production
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import com.humayapp.scout.feature.form.impl.ui.components.FormDetailsContent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
enum class FormType(
    val label: String,
    val description: String,
) {
    @Serializable
    FIELD_DATA(label = "Farmer's Profile", description = "Register farmers") {
        override val startEntry = FieldData.startEntry
        override val entries = FieldData.entries
        override val overrides = FieldData.pageOverrides
        override val reviewContent: @Composable ((FormState) -> Unit) = { state -> FormDetailsContent(state) }

        override fun serializeAnswers(answers: Map<String, Any?>) = FieldData.serialize(answers)
    },

    CULTURAL_MANAGEMENT(label = "Cultural Management", description = "Field & crop practices") {
        override val startEntry = CulturalManagement.startEntry
        override val entries = CulturalManagement.entries
        override val overrides = CulturalManagement.pageOverrides

        override val reviewContent: @Composable ((FormState) -> Unit) = { state ->
            CulturalManagement.reviewContent(state)
        }

        override fun serializeAnswers(answers: Map<String, Any?>) = CulturalManagement.serialize(answers)
    },

    NUTRIENT_MANAGEMENT(label = "Fertilization", description = "Fertilizer & soil data") {
        override val startEntry = NutrientManagement.startEntry
        override val entries = NutrientManagement.entries
        override val overrides = NutrientManagement.pageOverrides
        override val reviewContent: @Composable ((FormState) -> Unit) = { state ->
            NutrientManagement.reviewContent(state)
        }

        override fun serializeAnswers(answers: Map<String, Any?>) = NutrientManagement.serialize(answers)
    },

    PRODUCTION(label = "Production", description = "Record yields") {
        override val startEntry = Production.startEntry
        override val entries = Production.entries
        override val overrides = Production.pageOverrides
        override val reviewContent: @Composable ((FormState) -> Unit) = { state -> FormDetailsContent(state) }

        override fun serializeAnswers(answers: Map<String, Any?>) = Production.serialize(answers)
    },

    DAMAGE_ASSESSMENT(label = "Damage Assessment", description = "Assess crop losses") {
        override val startEntry = DamageAssessment.startEntry
        override val entries = DamageAssessment.entries
        override val overrides = null
        override val reviewContent: @Composable ((FormState) -> Unit) = { state -> FormDetailsContent(state) }

        override fun serializeAnswers(answers: Map<String, Any?>) = DamageAssessment.serialize(answers)
    };

    abstract val startEntry: WizardEntry
    abstract val entries: List<WizardEntry>

    abstract val overrides: WizardPageOverrides?
    abstract val reviewContent: @Composable (state: FormState) -> Unit

    abstract fun serializeAnswers(answers: Map<String, Any?>): JsonObject

    companion object {
        fun fromActivityType(activityTypeName: String): FormType = entries.fastFirst {
            it.id.equals(activityTypeName, ignoreCase = true)
        }
    }
}

val FormType.id: String get() = name.lowercase().replace('_', '-')


