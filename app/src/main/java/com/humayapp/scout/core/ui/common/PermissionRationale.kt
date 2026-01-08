package com.humayapp.scout.core.ui.common

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
import com.humayapp.scout.core.system.Permission
import com.humayapp.scout.core.ui.theme.ScoutTheme

@Composable
fun PermissionRationale(
    modifier: Modifier = Modifier,
    permission: Permission
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(40.dp),
            painter = painterResource(permission.icon),
            contentDescription = permission.title,
        )
        Spacer(Modifier.height(ScoutTheme.spacing.large))
        Text(text = permission.title, style = ScoutTheme.material.typography.headlineSmall)
        Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
        Text(
            text = permission.description,
            style = ScoutTheme.material.typography.bodyMedium,
            color = ScoutTheme.extras.colors.mutedOnBackground
        )
    }
}
