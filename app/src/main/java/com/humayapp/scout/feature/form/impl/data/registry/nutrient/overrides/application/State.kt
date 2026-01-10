package com.humayapp.scout.feature.form.impl.data.registry.nutrient.overrides.application

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


@Stable
class FertilizerApplicationState(
    nextIndex: Int,
    selectedIndex: Int?,
    showBottomSheet: Boolean
) {
    var applications = mutableStateListOf<Int>()
    var nextIndex by mutableIntStateOf(nextIndex)
    var selectedIndex by mutableStateOf(selectedIndex)
    var showBottomSheet by mutableStateOf(showBottomSheet)

    val isEditMode get() = applications.contains(selectedIndex)

    fun selectApplication(index: Int) {
        selectedIndex = index
        showBottomSheet = true
    }

    fun hideBottomSheet() {
        showBottomSheet = false
        selectedIndex = null
    }

    fun addNewApplication() {
        selectedIndex = nextIndex
        showBottomSheet = true
    }

    fun confirmApplication() {
        val index = selectedIndex ?: return
        if (!applications.contains(index)) {
            applications.add(index)
            nextIndex++
        }
    }
}

@Composable
fun rememberFertilizerApplicationState(
    answerKeys: Set<String>
): FertilizerApplicationState {
    val indices = remember(answerKeys) {
        answerKeys
            .mapNotNull { "_(\\d+)$".toRegex().find(it)?.groupValues?.get(1)?.toInt() }
            .distinct()
            .sorted()
    }

    return remember {
        FertilizerApplicationState(
            nextIndex = 1,
            selectedIndex = null,
            showBottomSheet = false
        ).apply {
            applications.addAll(indices)
        }
    }
}
