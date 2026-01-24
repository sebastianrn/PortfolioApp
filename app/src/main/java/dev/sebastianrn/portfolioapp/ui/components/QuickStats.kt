package dev.sebastianrn.portfolioapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun QuickStats(
    totalInvested: Double,
    totalProfit: Double
) {
    val profitPercent = if (totalInvested > 0) {
        (totalProfit / totalInvested) * 100
    } else 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Invested",
            value = totalInvested,
            icon = Icons.Filled.AccountBalanceWallet,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "Total Gain",
            value = totalProfit,
            percentage = profitPercent,
            icon = Icons.Filled.TrendingUp,
            color = if (totalProfit >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: Double,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    percentage: Double? = null
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(value.toInt())}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (percentage != null) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = color.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "${if (percentage >= 0) "+" else ""}${String.format("%.1f", percentage)}%",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
