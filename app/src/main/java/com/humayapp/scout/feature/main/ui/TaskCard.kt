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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humayapp.scout.core.database.model.CollectionTaskUiModel
import com.humayapp.scout.core.ui.theme.ScoutColors
import com.humayapp.scout.core.ui.theme.ScoutTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import java.time.format.DateTimeFormatter
import kotlin.uuid.ExperimentalUuidApi

val dateFormatter = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED); char(' '); day(); chars(", "); year()
}

val seasonFormatter = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    year()
}

fun LocalDate.seasonHalfLabel(): String {
    val year = this.year

    val firstStart = LocalDate(year, 9, 16)
    val firstEnd = LocalDate(year + 1, 3, 15)

    val secondStart = LocalDate(year, 3, 16)
    val secondEnd = LocalDate(year, 9, 15)

    return when {
        this in firstStart..firstEnd -> "First Semester"
        this >= secondStart || this <= secondEnd -> "Second Semester"
        else -> "Second Semester"
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun TaskCard(
    task: CollectionTaskUiModel,
    onClick: () -> Unit,
    isRefreshing: Boolean = false,
) {

    val statusColor = when (task.status.lowercase()) {
        "pending" -> Color(0xFFFFC107)
        "collected", "completed" -> ScoutTheme.extras.colors.bluey
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
        enabled = !isRefreshing,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isRefreshing) 0.5f else 1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScoutTheme.extras.colors.white,
            disabledContainerColor = ScoutTheme.extras.colors.white,
        ),
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
                verticalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.extraSmall),
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f),

                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formattedActivity,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "${task.seasonStartDate.format(seasonFormatter)} - ${task.seasonEndDate.format(seasonFormatter)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!task.farmerName.isNullOrBlank()) {
                        Text(
                            text = task.farmerName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Unassigned",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = ScoutTheme.extras.colors.warning
                        )
                    }


                    Text(
                        text = task.seasonStartDate.seasonHalfLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(ScoutTheme.spacing.doubleExtraSmall))

                if (address.isNotBlank()) {
                    Text(
                        text = address,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "${task.startDate.format(dateFormatter)} → ${task.endDate?.format(dateFormatter)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                }

                Spacer(Modifier.height(ScoutTheme.spacing.doubleExtraSmall))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskCardBadge(text = task.mfid)
                    if (task.isRetake) {
                        TaskCardBadge(
                            text = "Retake",
                            containerColor = ScoutTheme.extras.colors.warning,
                        )
                    }

                    if (task.isOverdue) {
                        TaskCardBadge(
                            text = "Overdue",
                            containerColor = ScoutTheme.extras.colors.danger,
                        )
                    }

                    if (task.status == "completed" && (task.verificationStatus == "pending" || task.verificationStatus == null)) {
                        if (task.synced == true) {
                            TaskCardBadge(
                                text = "Synced",
                                containerColor = ScoutTheme.extras.colors.bluey,
                            )
                        } else {
                            TaskCardBadge(
                                text = "Not Synced",
                                containerColor = ScoutTheme.extras.colors.bluey,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCardBadge(
    modifier: Modifier = Modifier,
    text: String,
    containerColor: Color = ScoutTheme.extras.colors.mutedOnSurfaceVariant,
    textColor: Color = ScoutTheme.extras.colors.white,
) {
    Box(
        modifier = modifier
            .clip(ScoutTheme.shapes.cornerMedium)
            .background(containerColor)
            .padding(horizontal = ScoutTheme.spacing.small, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = ScoutTheme.material.typography.labelSmall,
            color = textColor
        )
    }
}
