package dev.sebastianrn.portfolioapp.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WaterfallChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sebastianrn.portfolioapp.data.model.HistoricalStats
import dev.sebastianrn.portfolioapp.ui.theme.AppGradients
import dev.sebastianrn.portfolioapp.util.formatAsPercentage
import dev.sebastianrn.portfolioapp.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * "Records" grid: six tinted stat tiles in a 2-column layout.
 */
@Composable
fun HistoricalStatsCard(
    stats: HistoricalStats,
    modifier: Modifier = Modifier
) {
    val hasData = stats.allTimeHighDate != 0L
    val gain = MaterialTheme.colorScheme.secondary
    val loss = MaterialTheme.colorScheme.error
    val gold = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AppGradients.goldAccentLine)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "RECORDS",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!hasData) {
                Text(
                    "Not enough data yet — records appear once your portfolio has some history.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        icon = Icons.Filled.EmojiEvents,
                        tint = gold,
                        label = "All-Time High",
                        value = stats.allTimeHigh.formatCurrency(),
                        caption = formatDate(stats.allTimeHighDate),
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        icon = Icons.Filled.SouthEast,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "All-Time Low",
                        value = stats.allTimeLow.formatCurrency(),
                        caption = formatDate(stats.allTimeLowDate),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        tint = gain,
                        label = "Best Day",
                        value = stats.bestDayAbsolute.formatCurrency(),
                        caption = "${stats.bestDayPercent.formatAsPercentage(showSign = true)} · ${formatDate(stats.bestDayDate)}",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        tint = loss,
                        label = "Worst Day",
                        value = stats.worstDayAbsolute.formatCurrency(),
                        caption = "${stats.worstDayPercent.formatAsPercentage()} · ${formatDate(stats.worstDayDate)}",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        icon = Icons.Filled.WaterfallChart,
                        tint = loss,
                        label = "Max Drawdown",
                        value = (-stats.maxDrawdownPercent).formatAsPercentage(),
                        caption = "peak to trough",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        icon = Icons.Filled.Timeline,
                        tint = if (stats.totalReturnPercent >= 0) gain else loss,
                        label = "Total Return",
                        value = stats.totalReturnPercent.formatAsPercentage(showSign = true),
                        caption = "since first entry",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    icon: ImageVector,
    tint: Color,
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = tint
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
