package dev.sebastianrn.portfolioapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun HeroValueCard(
    totalValue: Double,
    dailyChange: Double,
    dailyChangePercent: Double,
    pulseAlpha: Float
) {
    val isPositive = dailyChange >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = MaterialTheme.shapes.extraLarge),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    "Total Value",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(totalValue.toInt())}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Animated indicator
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPositive)
                                    MaterialTheme.colorScheme.secondary.copy(alpha = pulseAlpha * 0.3f)
                                else
                                    MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha * 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Change indicator
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (isPositive)
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(abs(dailyChange).toInt())}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            "(${String.format("%.1f", abs(dailyChangePercent))}%)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
