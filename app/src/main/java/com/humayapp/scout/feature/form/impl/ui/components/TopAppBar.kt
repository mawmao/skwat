package com.humayapp.scout.feature.form.impl.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.humayapp.scout.R
import com.humayapp.scout.core.ui.component.ScoutIconButton
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.feature.form.impl.LocalFormState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormSectionTopAppBar(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {

    val state = LocalFormState.current

    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = state.formType.label,
                style = ScoutTheme.material.typography.headlineSmall,
                color = ScoutTheme.extras.colors.mutedOnBackground
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            ScoutIconButton(
                onClick = onBack,
                icon = R.drawable.arrow_back_24px,
                contentDescription = null,
                tint = ScoutTheme.extras.colors.mutedOnBackground
            )
        }
    )
}
