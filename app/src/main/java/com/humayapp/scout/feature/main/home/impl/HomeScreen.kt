package com.humayapp.scout.feature.main.home.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.humayapp.scout.core.navigation.LocalRootStackNavigator
import com.humayapp.scout.core.ui.theme.ScoutColors
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.api.FormType
import com.humayapp.scout.feature.form.api.id
import com.humayapp.scout.feature.form.impl.ui.screens.scan.FormScanScreen
import com.humayapp.scout.feature.form.impl.ui.screens.scan.FormScanViewModel
import com.humayapp.scout.navigation.navigateToForms
import com.humayapp.scout.navigation.navigateToOverlay


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {

    val rootNavigator = LocalRootStackNavigator.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ScoutTheme.margin),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TemporaryFormGrid(
            onFormClick = { formType ->
                rootNavigator.navigateToOverlay {
                    // find ways to abstract this
                    FormScanScreen(formType = formType)
                }
            }
        )
    }
}

@Composable
fun TemporaryFormGrid(onFormClick: (FormType) -> Unit) {

    val coreItems = FormType.coreTypes
    val optionalItems = FormType.optionalTypes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        Text("Core", modifier = Modifier.padding(bottom = 8.dp))

        Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            for (row in 0 until 2) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (col in 0 until 2) {
                        val index = row * 2 + col
                        TemporaryFormPlaceholder(coreItems[index], modifier = Modifier.weight(1f)) {
                            onFormClick(coreItems[index])
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Optional", modifier = Modifier.padding(vertical = 8.dp))
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (item in optionalItems) {
                TemporaryFormPlaceholder(item, modifier = Modifier.weight(1f)) {
                    onFormClick(item)
                }
            }
        }
    }
}

@Composable
fun TemporaryFormPlaceholder(
    formType: FormType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(ScoutColors.White)
            .padding(ScoutTheme.spacing.medium)
            .fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.extraSmall)
        ) {
            Text(
                text = formType.label,
                style = ScoutTheme.material.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = formType.description,
                style = ScoutTheme.material.typography.bodySmall.copy(fontSize = 11.5.sp),
            )
        }
    }
}

