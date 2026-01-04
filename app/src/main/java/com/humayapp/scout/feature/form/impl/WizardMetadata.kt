package com.humayapp.scout.feature.form.impl

import androidx.compose.runtime.Stable
import com.humayapp.scout.feature.form.api.navigation.WizardNavKey


@JvmInline
value class WizardGroupId(val id: String)
data class WizardPageCounts(
    val fixed: Int,
    val repeatablePerInstance: Int,
    val repeatableGroups: Int
)

@Stable
data class WizardMetadata(
    val startKey: WizardNavKey,
    val schemaKeys: List<WizardNavKey>,
    val expandedKeys: List<WizardNavKey>,
    val pageCounts: WizardPageCounts
)

fun wizardMetadata(
    startKey: WizardNavKey,
    keys: List<WizardNavKey>,
    repeatCount: Int = 1
): WizardMetadata {
    val expanded = expandKeys(keys, repeatCount)
    val counts = countPages(keys)

    return WizardMetadata(
        startKey = startKey,
        schemaKeys = keys,
        expandedKeys = expanded,
        pageCounts = counts
    )
}

private fun expandKeys(
    keys: List<WizardNavKey>,
    repeatCount: Int
): List<WizardNavKey> {
    val result = mutableListOf<WizardNavKey>()
    val buffer = mutableListOf<WizardNavKey>()
    var currentGroup: WizardGroupId? = null

    fun flush() {
        if (buffer.isEmpty()) return
        if (currentGroup == null) {
            result += buffer
        } else {
            repeat(repeatCount) { result += buffer }
        }
        buffer.clear()
    }

    for (key in keys) {
        when {
            key.group == null -> {
                flush()
                currentGroup = null
                result += key
            }

            key.group != currentGroup -> {
                flush()
                currentGroup = key.group
                buffer += key
            }

            else -> buffer += key
        }
    }

    flush()
    return result
}

private fun countPages(keys: List<WizardNavKey>): WizardPageCounts =
    WizardPageCounts(
        fixed = keys.count { it.group == null },
        repeatablePerInstance = keys
            .filter { it.group != null }
            .groupBy { it.group }
            .values
            .sumOf { it.size },
        repeatableGroups = keys
            .mapNotNull { it.group }
            .distinct()
            .size
    )

