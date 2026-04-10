package com.humayapp.scout.feature.main.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humayapp.scout.core.database.model.CollectionTaskUiModel
import com.humayapp.scout.core.ui.theme.ScoutTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlin.uuid.ExperimentalUuidApi

val dateFormatter = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED); char(' '); day(); chars(", "); year()
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun TaskCard(
    task: CollectionTaskUiModel,
    onClick: () -> Unit
) {

    val statusColor = when (task.status.lowercase()) {
        "pending" -> Color(0xFFFFC107)
        "collected", "completed" -> Color(0xFF2196F3)
        "approved" -> Color(0xFF4CAF50)
        "rejected", "cancelled" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    val barColor = when (task.verificationStatus?.lowercase()) {
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFF44336)
        else -> statusColor
    }

    val formattedActivity = remember(task.activityType) {
        task.activityType
            .replace("-", " ")
            .split(Regex("\\s+"))
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    val address = remember(task) {
        task.fullAddress
            ?: listOfNotNull(task.barangay, task.cityMunicipality, task.province)
                .joinToString(", ")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ScoutTheme.extras.colors.white),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(barColor)
            )

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formattedActivity,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (task.isRetake) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = ScoutTheme.shapes.cornerLarge,
                                color = Color(0xFFFFC107),
                                contentColor = Color.Black
                            ) {
                                Text(
                                    text = "Retake",
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Season ${task.seasonId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(4.dp))

                task.farmerName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(2.dp))

                if (address.isNotBlank()) {
                    Text(
                        text = address,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${task.startDate.format(dateFormatter)} → ${task.endDate?.format(dateFormatter)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (task.isOverdue) {
                        Text(
                            text = "OVERDUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "#${task.mfid}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
