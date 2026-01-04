package com.humayapp.scout.feature.form.impl.model



fun field(key: String, label: String, type: FieldType, options: List<String>? = null) =
    WizardField(key, label, type, options)

data class WizardField(
    val key: String,
    val label: String,
    val type: FieldType,
    val options: List<String>? = null
)

enum class FieldType {
    TEXT,
    NAME,
    NUM_DECIMAL,
    NUM_PHONE,
    DROPDOWN,
    DROPDOWN_SEARCHABLE,
    GPS,
    CARD_RADIO,
    DATE,
}