package com.humayapp.scout.feature.form.api

import com.humayapp.scout.feature.form.impl.FieldData
import com.humayapp.scout.feature.form.impl.WizardMetadata
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
        override fun wizardMetadata(): WizardMetadata = FieldData.createFieldDataWizardMetadata()
    },

    CULTURAL_MANAGEMENT(
        label = "Cultural Management",
        description = "Field & crop practices"
    ) {
        override fun wizardMetadata(): WizardMetadata = TODO("no wizard state yet")
    },
    NUTRIENT_MANAGEMENT(
        label = "Fertilization",
        description = "Fertilizer & soil data"
    ) {
        override fun wizardMetadata(): WizardMetadata = TODO("no wizard state yet")
    },
    PRODUCTION(
        label = "Production",
        description = "Record yields"
    ) {
        override fun wizardMetadata(): WizardMetadata = TODO("no wizard state yet")
    },
    MONITORING_VISIT(
        label = "Monitoring Visit",
        description = "Field checkups"
    ) {
        override fun wizardMetadata(): WizardMetadata = TODO("no wizard state yet")

    },
    DAMAGE_ASSESSMENT(
        label = "Damage Assessment",
        description = "Assess crop losses"
    ) {
        override fun wizardMetadata(): WizardMetadata = TODO("no wizard state yet")
    };

    abstract fun wizardMetadata(): WizardMetadata

    companion object {
        val coreTypes = listOf(FIELD_DATA, CULTURAL_MANAGEMENT, NUTRIENT_MANAGEMENT, PRODUCTION)
        val optionalTypes = listOf(MONITORING_VISIT, DAMAGE_ASSESSMENT)
    }
}

val FormType.id: String get() = name.lowercase().replace('_', '-')

