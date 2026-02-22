package dev.sebastianrn.portfolioapp.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.data.model.HistoricalStats
import dev.sebastianrn.portfolioapp.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun HistoricalStatsCard(
    stats: HistoricalStats,
    modifier: Modifier = Modifier
) {
    val hasData = stats.allTimeHighDate != 0L

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Historical Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!hasData) {
                Text(
                    "Not enough data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Row 1: All-Time High | All-Time Low
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HistoricalStatItem(
                        label = "All-Time High",
                        value = stats.allTimeHigh.formatCurrency(),
                        date = stats.allTimeHighDate,
                        color = MaterialTheme.colorScheme.secondary,
                        alignment = Alignment.Start
                    )
                    HistoricalStatItem(
                        label = "All-Time Low",
                        value = stats.allTimeLow.formatCurrency(),
                        date = stats.allTimeLowDate,
                        color = MaterialTheme.colorScheme.onSurface,
                        alignment = Alignment.End
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Row 2: Best Day | Worst Day
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HistoricalStatItem(
                        label = "Best Day",
                        value = "+${stats.bestDayAbsolute.formatCurrency()}",
                        percentage = stats.bestDayPercent,
                        date = stats.bestDayDate,
                        color = MaterialTheme.colorScheme.secondary,
                        alignment = Alignment.Start
                    )
                    HistoricalStatItem(
                        label = "Worst Day",
                        value = stats.worstDayAbsolute.formatCurrency(),
                        percentage = stats.worstDayPercent,
                        date = stats.worstDayDate,
                        color = MaterialTheme.colorScheme.error,
                        alignment = Alignment.End
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Row 3: Max Drawdown | Total Return
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PercentStatItem(
                        label = "Max Drawdown",
                        percentage = -stats.maxDrawdownPercent,
                        color = MaterialTheme.colorScheme.error,
                        alignment = Alignment.Start
                    )
                    PercentStatItem(
                        label = "Total Return",
                        percentage = stats.totalReturnPercent,
                        color = if (stats.totalReturnPercent >= 0) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        alignment = Alignment.End
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoricalStatItem(
    label: String,
    value: String,
    date: Long,
    color: Color,
    alignment: Alignment.Horizontal,
    percentage: Double? = null
) {
    val formattedDate = remember(date) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(date))
    }

    Column(horizontalAlignment = alignment) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        if (percentage != null) {
            Text(
                "${String.format("%.1f", percentage)}%",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
        Text(
            formattedDate,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PercentStatItem(
    label: String,
    percentage: Double,
    color: Color,
    alignment: Alignment.Horizontal
) {
    Column(horizontalAlignment = alignment) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "${String.format("%.1f", abs(percentage))}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
