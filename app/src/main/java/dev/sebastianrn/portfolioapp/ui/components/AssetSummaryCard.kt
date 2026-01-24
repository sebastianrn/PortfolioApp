package dev.sebastianrn.portfolioapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AssetSummaryCard(asset: GoldAsset) {
    val isPositive = asset.totalProfitOrLoss >= 0
    val totalInvested = asset.purchasePrice * asset.quantity
    val percentage = if (totalInvested > 0) {
        (asset.totalProfitOrLoss / totalInvested) * 100
    } else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                // Asset badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = asset.type.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${asset.weightInGrams}g",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Current Value
                Text(
                    "Current Value",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(asset.totalCurrentValue.toInt())}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Performance badge
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
                            if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            "${if (isPositive) "+" else ""}${String.format("%.1f", percentage)}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            "total return",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(20.dp))

                // Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailItem("Quantity", "${asset.quantity}")
                    DetailItem(
                        "Purchase Price",
                        "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(asset.purchasePrice.toInt())}"
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
