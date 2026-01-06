package com.humayapp.scout.feature.form.api

import com.humayapp.scout.feature.form.impl.data.registry.fielddata.FieldData
import com.humayapp.scout.feature.form.impl.model.WizardEntry
import kotlinx.serialization.Serializable

@Serializable
enum class FormType(
    val label: String,
    val description: String,
) {
    FIELD_DATA(
        label = "Farmer's Profile",
        description = "Register farmers"
    ) {
        override val startEntry = FieldData.startEntry
        override val entries = FieldData.entries
    },

    CULTURAL_MANAGEMENT(
        label = "Cultural Management",
        description = "Field & crop practices"
    ) {
        override val startEntry = null
        override val entries = emptyList<WizardEntry>()
    },
    NUTRIENT_MANAGEMENT(
        label = "Fertilization",
        description = "Fertilizer & soil data"
    ) {
        override val startEntry = null
        override val entries = emptyList<WizardEntry>()
    },
    PRODUCTION(
        label = "Production",
        description = "Record yields"
    ) {
        override val startEntry = null
        override val entries = emptyList<WizardEntry>()
    },
    MONITORING_VISIT(
        label = "Monitoring Visit",
        description = "Field checkups"
    ) {
        override val startEntry = null
        override val entries = emptyList<WizardEntry>()
    },
    DAMAGE_ASSESSMENT(
        label = "Damage Assessment",
        description = "Assess crop losses"
    ) {
        override val startEntry = null
        override val entries = emptyList<WizardEntry>()
    };

    // change to non-null when all forms are filled
    abstract val startEntry: WizardEntry?
    abstract val entries: List<WizardEntry>

    companion object {
        val coreTypes = listOf(FIELD_DATA, CULTURAL_MANAGEMENT, NUTRIENT_MANAGEMENT, PRODUCTION)
        val optionalTypes = listOf(MONITORING_VISIT, DAMAGE_ASSESSMENT)
    }
}

val FormType.id: String get() = name.lowercase().replace('_', '-')

