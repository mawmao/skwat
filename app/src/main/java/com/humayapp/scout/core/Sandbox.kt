package com.humayapp.scout.core

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.humayapp.scout.core.navigation.NavTransition
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.navigation.RootNavKey

const val SANDBOX_ENABLE = false

fun EntryProviderScope<NavKey>.sandbox() {
    entry<RootNavKey.Sandbox>(metadata = NavTransition.fade()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ScoutTheme.margin),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(ScoutTheme.shapes.cornerMedium)
                    .border(1.dp, ScoutTheme.material.colorScheme.onSurfaceVariant, ScoutTheme.shapes.cornerMedium),
            ) {
                val type = "Organic"
                val brand = "Brand"
                val amount = "45"
                val unit = "g"
                val n = "10"
                val p = "10"
                val k = "18"
                val stage = "Vegetative"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(type, style = ScoutTheme.material.typography.titleMedium)
                        Text(
                            brand,
                            style = ScoutTheme.material.typography.bodyLarge,
                            color = ScoutTheme.material.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$n% $p% $k%",
                            style = ScoutTheme.material.typography.bodyMedium,
                            color = ScoutTheme.material.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$amount $unit", style = ScoutTheme.material.typography.headlineSmall)
                        Text(stage)
                    }
                }
            }
        }
    }
}
