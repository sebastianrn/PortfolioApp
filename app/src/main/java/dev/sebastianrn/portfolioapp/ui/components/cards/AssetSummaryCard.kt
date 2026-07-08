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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.ui.components.common.AnimatedCounterText
import dev.sebastianrn.portfolioapp.ui.components.common.GlassStatDivider
import dev.sebastianrn.portfolioapp.ui.components.common.GlassStatPanel
import dev.sebastianrn.portfolioapp.ui.components.common.GlassStatRow
import dev.sebastianrn.portfolioapp.ui.components.common.TrendChip
import dev.sebastianrn.portfolioapp.ui.theme.AppGradients
import dev.sebastianrn.portfolioapp.ui.theme.GoldDeep
import dev.sebastianrn.portfolioapp.ui.theme.OnGold
import dev.sebastianrn.portfolioapp.ui.theme.OnGoldMuted
import dev.sebastianrn.portfolioapp.util.formatAsPercentage
import dev.sebastianrn.portfolioapp.util.formatCurrency

/**
 * Gold hero header for the asset detail screen.
 */
@Composable
fun AssetSummaryCard(asset: GoldAsset) {
    val isPositive = asset.totalProfitOrLoss >= 0
    val totalInvested = asset.purchasePrice * asset.quantity
    val percentage = if (totalInvested > 0) {
        (asset.totalProfitOrLoss / totalInvested) * 100
    } else 0.0

    val shape = MaterialTheme.shapes.extraLarge

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 24.dp, shape = shape, ambientColor = GoldDeep, spotColor = GoldDeep)
            .clip(shape)
            .background(AppGradients.goldCard)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, GoldDeep.copy(alpha = 0.25f)),
                        startY = 300f
                    )
                )
        )

        Column(modifier = Modifier.padding(20.dp)) {
            // Meta chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MetaChip(asset.type.name)
                MetaChip("${asset.weightInGrams}g")
                MetaChip("×${asset.quantity}")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                stringResource(R.string.current_value_label).uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                fontWeight = FontWeight.Bold,
                color = OnGoldMuted
            )
            AnimatedCounterText(
                value = asset.totalCurrentValue,
                style = MaterialTheme.typography.displaySmall.copy(letterSpacing = (-1).sp),
                color = OnGold
            )

            Spacer(modifier = Modifier.height(8.dp))

            TrendChip(
                text = "${asset.totalProfitOrLoss.formatCurrency()} (${percentage.formatAsPercentage()})",
                positive = isPositive,
                onGold = true,
                textStyle = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(18.dp))

            GlassStatPanel {
                GlassStatRow(
                    label = stringResource(R.string.tile_unit_price),
                    value = asset.currentSellPrice.formatCurrency()
                )
                GlassStatDivider()
                GlassStatRow(
                    label = stringResource(R.string.tile_paid),
                    value = asset.purchasePrice.formatCurrency()
                )
                GlassStatDivider()
                GlassStatRow(
                    label = stringResource(R.string.tile_invested),
                    value = totalInvested.formatCurrency()
                )
            }
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.30f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = OnGold
        )
    }
}
