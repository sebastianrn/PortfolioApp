package dev.sebastianrn.portfolioapp.ui.components.cards

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sebastianrn.portfolioapp.ui.components.common.AnimatedCounterText
import dev.sebastianrn.portfolioapp.ui.components.common.GlassTile
import dev.sebastianrn.portfolioapp.ui.components.common.TrendChip
import dev.sebastianrn.portfolioapp.ui.theme.AppGradients
import dev.sebastianrn.portfolioapp.ui.theme.GoldDeep
import dev.sebastianrn.portfolioapp.ui.theme.OnGold
import dev.sebastianrn.portfolioapp.ui.theme.OnGoldMuted
import dev.sebastianrn.portfolioapp.util.formatAsPercentage
import dev.sebastianrn.portfolioapp.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gold "vault" hero card: shimmering champagne gradient, counting balance,
 * live indicator and frosted glass stat tiles.
 */
@Composable
fun PortfolioSummaryCard(
    totalValue: Double,
    totalInvested: Double,
    totalProfit: Double,
    dailyChange: Double,
    dailyChangePercent: Double,
    lastUpdated: Long? = null
) {
    val isPositive = totalProfit >= 0
    val isDailyPositive = dailyChange >= 0
    val profitPercent = if (totalInvested > 0) (totalProfit / totalInvested) * 100 else 0.0

    val shape = MaterialTheme.shapes.extraLarge

    // Slow specular sweep across the gold surface
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by shimmerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    // Breathing live dot
    val pulse by shimmerTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 24.dp, shape = shape, ambientColor = GoldDeep, spotColor = GoldDeep)
            .clip(shape)
            .background(AppGradients.goldCard)
    ) {
        // Specular highlight overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(AppGradients.goldShimmer(shimmerProgress))
        )

        // Grounding vignette at the bottom edge
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TOTAL BALANCE",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                    fontWeight = FontWeight.Bold,
                    color = OnGoldMuted
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(OnGold.copy(alpha = pulse), CircleShape)
                    )
                    Text(
                        text = lastUpdated?.let { timestamp ->
                            val formatted = remember(timestamp) {
                                SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                                    .format(Date(timestamp))
                            }
                            "Live · $formatted"
                        } ?: "Live",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OnGoldMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            AnimatedCounterText(
                value = totalValue,
                style = MaterialTheme.typography.displaySmall.copy(letterSpacing = (-1).sp),
                color = OnGold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrendChip(
                    text = "${totalProfit.formatCurrency(short = true)} (${profitPercent.formatAsPercentage()})",
                    positive = isPositive,
                    onGold = true
                )
                Text(
                    "all time",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnGoldMuted
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassTile(
                    label = "Invested",
                    value = totalInvested.formatCurrency(short = true),
                    modifier = Modifier.weight(1f)
                )
                GlassTile(
                    label = "Total Gain",
                    value = totalProfit.formatCurrency(),
                    subText = profitPercent.formatAsPercentage(showSign = true),
                    trend = isPositive,
                    modifier = Modifier.weight(1f)
                )
                GlassTile(
                    label = "Today",
                    value = dailyChange.formatCurrency(),
                    subText = dailyChangePercent.formatAsPercentage(showSign = true),
                    trend = isDailyPositive,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
