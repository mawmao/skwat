package com.humayapp.scout.feature.form.impl.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.humayapp.scout.R
import com.humayapp.scout.core.ui.component.ScoutIconButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormSectionTopAppBar(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        title = {},
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            ScoutIconButton(
                onClick = onBack,
                icon = R.drawable.arrow_back_24px,
                contentDescription = null
            )
        }
    )
}
