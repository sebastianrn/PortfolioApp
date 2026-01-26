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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.ui.components.common.StatItem
import dev.sebastianrn.portfolioapp.util.formatCurrency

@Composable
fun PortfolioSummaryCard(
    totalValue: Double,
    totalInvested: Double,
    totalProfit: Double,
    dailyChange: Double,
    dailyChangePercent: Double,
    pulseAlpha: Float
) {
    val isPositive = totalProfit >= 0
    val isDailyPositive = dailyChange >= 0
    val profitPercent = if (totalInvested > 0) {
        (totalProfit / totalInvested) * 100
    } else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
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
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Current Value label
                Text(
                    "Current Value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Main portfolio value with pulse indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        totalValue.formatCurrency(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = pulseAlpha),
                                shape = CircleShape
                            )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats row: Total Gain | Invested | Today
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Total Gain",
                        value = totalProfit,
                        isValueFormatShort = false,
                        percentage = profitPercent,
                        neutralColorNeeded = false,
                        isPositive = isPositive,
                        alignment = Alignment.Start
                    )

                    StatItem(
                        label = "Invested",
                        value = totalInvested,
                        isValueFormatShort = true,
                        percentage = null,
                        neutralColorNeeded = true,
                        isPositive = isPositive,
                        alignment = Alignment.CenterHorizontally
                    )

                    StatItem(
                        label = "Today",
                        value = dailyChange,
                        isValueFormatShort = false,
                        percentage = dailyChangePercent,
                        neutralColorNeeded = false,
                        isPositive = isDailyPositive,
                        alignment = Alignment.End
                    )
                }
            }
        }
    }
}
