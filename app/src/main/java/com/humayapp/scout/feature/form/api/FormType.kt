package com.humayapp.scout.feature.form.api

import androidx.compose.ui.util.fastFirst
import com.humayapp.scout.feature.form.impl.data.mapper.FormMapper
import com.humayapp.scout.feature.form.impl.data.registry.cultural.CulturalManagement
import com.humayapp.scout.feature.form.impl.data.registry.damage.DamageAssessment
import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.data.registry.monitoring.MonitoringVisit
import com.humayapp.scout.feature.form.impl.data.registry.nutrient.NutrientManagement
import com.humayapp.scout.feature.form.impl.data.registry.production.Production
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import com.humayapp.scout.feature.form.impl.model.WizardPageOverrides
import kotlinx.serialization.Serializable

@Serializable
enum class FormType(
    val label: String,
    val description: String,
) {
    FIELD_DATA(label = "Farmer's Profile", description = "Register farmers") {
        override val startEntry = FieldData.startEntry
        override val entries = FieldData.entries
        override val mapper = null
        override val overrides = FieldData.pageOverrides
    },

    CULTURAL_MANAGEMENT(label = "Cultural Management", description = "Field & crop practices") {
        override val startEntry = CulturalManagement.startEntry
        override val entries = CulturalManagement.entries
        override val mapper = CulturalManagement.mapper
        override val overrides = CulturalManagement.pageOverrides
    },

    NUTRIENT_MANAGEMENT(label = "Fertilization", description = "Fertilizer & soil data") {
        override val startEntry = NutrientManagement.startEntry
        override val entries = NutrientManagement.entries
        override val mapper = NutrientManagement.mapper
        override val overrides = NutrientManagement.pageOverrides
    },

    PRODUCTION(label = "Production", description = "Record yields") {
        override val startEntry = Production.startEntry
        override val entries = Production.entries
        override val mapper = Production.mapper
        override val overrides = Production.pageOverrides
    },

    MONITORING_VISIT(label = "Monitoring Visit", description = "Field checkups") {
        override val startEntry = MonitoringVisit.startEntry
        override val entries = MonitoringVisit.entries
        override val overrides = MonitoringVisit.pageOverrides
        override val mapper = MonitoringVisit.mapper
    },

    DAMAGE_ASSESSMENT(label = "Damage Assessment", description = "Assess crop losses") {
        override val startEntry = DamageAssessment.startEntry
        override val entries = DamageAssessment.entries
        override val mapper = DamageAssessment.mapper
        override val overrides = null
    };

    abstract val startEntry: WizardEntry
    abstract val entries: List<WizardEntry>

    abstract val mapper: FormMapper? // field data left
    abstract val overrides: WizardPageOverrides?


    companion object {
        val coreTypes = listOf(FIELD_DATA, CULTURAL_MANAGEMENT, NUTRIENT_MANAGEMENT, PRODUCTION)
        val optionalTypes = listOf(MONITORING_VISIT, DAMAGE_ASSESSMENT)

        fun fromActivityType(activityTypeName: String): FormType = entries.fastFirst {
            it.id.equals(activityTypeName, ignoreCase = true)
        }
    }
}

val FormType.id: String get() = name.lowercase().replace('_', '-')


