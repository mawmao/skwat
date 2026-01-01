package com.humayapp.scout.feature.form.api

import kotlinx.serialization.Serializable

@Serializable
enum class FormType(
    val label: String,
    val description: String,
) {
    FIELD_DATA(
        label = "Farmer's Profile",
        description = "Register farmers"
    ),
    CULTURAL_MANAGEMENT(
        label = "Cultural Management",
        description = "Field & crop practices"
    ),
    NUTRIENT_MANAGEMENT(
        label = "Fertilization",
        description = "Fertilizer & soil data"
    ),
    PRODUCTION(
        label = "Production",
        description = "Record yields"
    ),
    MONITORING_VISIT(
        label = "Monitoring Visit",
        description = "Field checkups"
    ),
    DAMAGE_ASSESSMENT(
        label = "Damage Assessment",
        description = "Assess crop losses"
    );

    companion object {
        val coreTypes = listOf(FIELD_DATA, CULTURAL_MANAGEMENT, NUTRIENT_MANAGEMENT, PRODUCTION)
        val optionalTypes = listOf(MONITORING_VISIT, DAMAGE_ASSESSMENT)
    }
}

// good for logging
val FormType.id: String
    get() = name.lowercase().replace('_', '-')
