package dev.sebastianrn.portfolioapp.ui.shared

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.util.formatCurrency
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

// ============================================
// PORTFOLIO HEADER
// ============================================
@Composable
fun PortfolioHeader(
    totalValue: Double,
    totalInvested: Double,
    dailyChange: Double,
    dailyChangePercent: Double,
    shimmerAlpha: Float
) {
    val isPositive = dailyChange >= 0
    val totalProfit = totalValue - totalInvested
    val totalProfitPercent = if (totalInvested > 0) (totalProfit / totalInvested) * 100 else 0.0
    val isTotalPositive = totalProfit >= 0

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExpressiveColors.SurfaceHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            ExpressiveColors.PrimaryStart.copy(alpha = 0.15f),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.total_portfolio_value),
                            style = MaterialTheme.typography.titleMedium,
                            color = ExpressiveColors.OnSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(totalValue)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = ExpressiveColors.OnSurface,
                            fontSize = 40.sp
                        )
                    }

                    // Animated pulse indicator
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .scale(1f + shimmerAlpha * 0.1f)
                            .clip(CircleShape)
                            .background(
                                if (isPositive)
                                    ExpressiveColors.TertiaryAccent.copy(alpha = shimmerAlpha)
                                else
                                    ExpressiveColors.ErrorAccent.copy(alpha = shimmerAlpha)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Daily change chip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isPositive)
                            ExpressiveColors.TertiaryAccent.copy(alpha = 0.2f)
                        else
                            ExpressiveColors.ErrorAccent.copy(alpha = 0.2f),
                        modifier = Modifier.animateContentSize()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                contentDescription = null,
                                tint = if (isPositive) ExpressiveColors.TertiaryAccent else ExpressiveColors.ErrorAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(abs(dailyChange))} (${String.format("%.2f", abs(dailyChangePercent))}%)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) ExpressiveColors.TertiaryAccent else ExpressiveColors.ErrorAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "today",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ExpressiveColors.OnSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Total Profit/Loss chip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isTotalPositive)
                            ExpressiveColors.TertiaryAccent.copy(alpha = 0.2f)
                        else
                            ExpressiveColors.ErrorAccent.copy(alpha = 0.2f),
                        modifier = Modifier.animateContentSize()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isTotalPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                contentDescription = null,
                                tint = if (isTotalPositive) ExpressiveColors.TertiaryAccent else ExpressiveColors.ErrorAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(abs(totalProfit))} (${String.format("%.2f", abs(totalProfitPercent))}%)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isTotalPositive) ExpressiveColors.TertiaryAccent else ExpressiveColors.ErrorAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "total",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ExpressiveColors.OnSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// PORTFOLIO CHART CARD
// ============================================
@Composable
fun PortfolioChartCard(
    points: List<Pair<Long, Double>>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExpressiveColors.SurfaceHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.performance_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ExpressiveColors.OnSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Enhanced chart with built-in time range selector
            Box(modifier = Modifier.fillMaxSize()) {
                if (points.isNotEmpty()) {
                    PortfolioChart(
                        points = points,
                        showTimeRangeSelector = true,
                        goldColor = ExpressiveColors.PrimaryStart
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        ExpressiveColors.PrimaryStart.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.empty_assets_list),
                            color = ExpressiveColors.OnSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// QUICK STATS ROW
// ============================================
@Composable
fun QuickStatsRow(
    totalInvested: Double,
    totalValue: Double
) {
    val overallProfitLoss = totalValue - totalInvested

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            icon = Icons.Outlined.TrendingUp,
            label = "Total Profit",
            value = overallProfitLoss.formatCurrency(),
            color = ExpressiveColors.TertiaryAccent,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            icon = Icons.Outlined.Paid,
            label = "Invested",
            value = totalInvested.formatCurrency(),
            color = ExpressiveColors.SecondaryGradient,
            modifier = Modifier.weight(1f)
        )
    }
}

// ============================================
// QUICK STAT CARD
// ============================================
@Composable
fun QuickStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExpressiveColors.SurfaceHigh
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
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExpressiveColors.OnSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = ExpressiveColors.OnSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
