package dev.sebastianrn.portfolioapp.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.data.model.AssetType
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.ui.components.common.TrendChip
import dev.sebastianrn.portfolioapp.ui.theme.AppGradients
import dev.sebastianrn.portfolioapp.ui.theme.GoldBright
import dev.sebastianrn.portfolioapp.ui.theme.OnGold
import dev.sebastianrn.portfolioapp.util.formatAsPercentage
import dev.sebastianrn.portfolioapp.util.formatCurrency

/**
 * Asset row: coins render as polished gold discs, bars as cast ingots.
 */
@Composable
fun AssetCard(
    asset: GoldAsset,
    onAssetClick: () -> Unit
) {
    val isPositive = asset.totalProfitOrLoss >= 0
    val changePercent = if (asset.purchasePrice > 0) {
        (asset.totalProfitOrLoss / (asset.purchasePrice * asset.quantity)) * 100
    } else 0.0

    Card(
        onClick = onAssetClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AssetIcon(asset)

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = asset.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${asset.quantity} × ${asset.weightInGrams}g · ${asset.type.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = asset.totalCurrentValue.formatCurrency(short = true),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TrendChip(
                    text = changePercent.formatAsPercentage(),
                    positive = isPositive
                )
            }
        }
    }
}

@Composable
private fun AssetIcon(asset: GoldAsset) {
    if (asset.type == AssetType.COIN) {
        // Polished coin with a milled ring
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(AppGradients.goldCoin)
                .border(2.dp, GoldBright.copy(alpha = 0.9f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = asset.name.take(2).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = OnGold
            )
        }
    } else {
        // Cast ingot
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppGradients.goldBar)
                .border(1.dp, GoldBright.copy(alpha = 0.7f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = asset.name.take(2).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = OnGold
            )
        }
    }
}
