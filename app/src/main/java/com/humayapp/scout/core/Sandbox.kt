package com.humayapp.scout.core

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.core.system.Permission
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.navigation.RootNavKey

const val SANDBOX_ENABLE = false

fun EntryProviderScope<NavKey>.sandbox() {
    entry<RootNavKey.Sandbox>(metadata = NavTransition.fade()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(ScoutIcons.LocationOn),
                contentDescription = null
            )
            Spacer(Modifier.height(ScoutTheme.spacing.large))
            Text(text = "", style = ScoutTheme.material.typography.headlineSmall)
            Spacer(Modifier.height(ScoutTheme.spacing.small))
            Text(
                text = "",
                style = ScoutTheme.material.typography.bodyMedium,
                color = ScoutTheme.extras.colors.mutedOnBackground
            )
        }
    }
}
