package com.humayapp.scout.feature.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.humayapp.scout.core.ui.component.ScoutDialog
import com.humayapp.scout.core.ui.component.ScoutTextButton
import com.humayapp.scout.core.ui.theme.ScoutTheme
import com.humayapp.scout.core.ui.theme.ScoutTypography
import com.humayapp.scout.feature.auth.model.ScoutUser
import java.util.Locale.getDefault

@Composable
fun UserProfileDialog(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    user: ScoutUser?,
    onDismissRequest: () -> Unit,
    onSignOut: () -> Unit
) {
    ScoutDialog(
        isVisible = isVisible,
        onDismiss = onDismissRequest,
        content = {
            Column(
                modifier = Modifier.padding(ScoutTheme.spacing.large)
            ) {
                Text(
                    text = "Profile",
                    style = ScoutTheme.material.typography.headlineSmall,
                    color = ScoutTheme.extras.colors.mutedOnBackground
                )
                Spacer(Modifier.height(ScoutTheme.spacing.medium))

                if (user != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.small)) {
                        DetailRow("Name", "${user.firstName ?: ""} ${user.lastName ?: ""}".trim().ifEmpty { "—" })
                        DetailRow("Email", user.email ?: "—")
                        DetailRow("Role",
                            user.role?.replace("_", " ")
                                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }
                                ?: "—")
                        user.dateOfBirth?.let { DetailRow("Date of Birth", it.toString()) }
                        DetailRow("Status", if (user.isActive == true) "Active" else "Inactive")
                    }
                } else {
                    Text("User information not available")
                }

                Spacer(Modifier.height(ScoutTheme.spacing.large))

                ScoutTextButton(
                    text = "Sign Out",
                    color = ScoutTheme.extras.colors.danger,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    onClick = onSignOut,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = ScoutTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = ScoutTypography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
