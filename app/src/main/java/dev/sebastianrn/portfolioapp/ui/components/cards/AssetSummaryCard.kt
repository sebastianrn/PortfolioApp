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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.ui.components.common.StatItem
import dev.sebastianrn.portfolioapp.util.formatCurrency

@Composable
fun AssetSummaryCard(asset: GoldAsset) {
    val isPositive = asset.totalProfitOrLoss >= 0
    val totalInvested = asset.purchasePrice * asset.quantity
    val percentage = if (totalInvested > 0) {
        (asset.totalProfitOrLoss / totalInvested) * 100
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
                // Top row: Badges and Return indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Asset badges
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Badge(
                            text = asset.type.name,
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                        Badge(
                            text = "${asset.weightInGrams}g",
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Current Value - prominent display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            "Current Value",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            asset.totalCurrentValue.formatCurrency(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Total return value
                    Column(horizontalAlignment = Alignment.End) {
                        StatItem(
                            label = "Return",
                            value = asset.totalProfitOrLoss,
                            isValueFormatShort = false,
                            percentage = percentage,
                            neutralColorNeeded = false,
                            isPositive = isPositive,
                            isCurrency = true,
                            alignment = Alignment.End
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Quantity",
                        value = asset.quantity.toDouble(),
                        isValueFormatShort = false,
                        percentage = null,
                        neutralColorNeeded = true,
                        isPositive = null,
                        isCurrency = false,
                        alignment = Alignment.CenterHorizontally
                    )

                    StatItem(
                        label = "Purchase Price",
                        value = asset.purchasePrice,
                        isValueFormatShort = true,
                        percentage = null,
                        neutralColorNeeded = true,
                        isPositive = null,
                        alignment = Alignment.CenterHorizontally
                    )

                    StatItem(
                        label = "Invested",
                        value = totalInvested,
                        isValueFormatShort = true,
                        percentage = null,
                        neutralColorNeeded = true,
                        isPositive = null,
                        alignment = Alignment.End
                    )
                }
            }
        }
    }
}

@Composable
private fun Badge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
