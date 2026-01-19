
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.ui.theme.ExpressiveError
import dev.sebastianrn.portfolioapp.ui.theme.ExpressiveOnSurface
import dev.sebastianrn.portfolioapp.ui.theme.ExpressivePrimaryStart
import dev.sebastianrn.portfolioapp.ui.theme.ExpressiveSecondary
import dev.sebastianrn.portfolioapp.ui.theme.ExpressiveSurfaceHigh
import dev.sebastianrn.portfolioapp.ui.theme.ExpressiveTertiary
import dev.sebastianrn.portfolioapp.util.formatCurrency

@Composable
fun AssetSummaryCard(
    asset: GoldAsset,
    shimmerAlpha: Float
) {
    val isPositive = asset.totalProfitOrLoss >= 0
    val totalInvested = asset.purchasePrice * asset.quantity
    val percentage = if (totalInvested > 0) {
        (asset.totalProfitOrLoss / totalInvested) * 100
    } else 0.0

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
            containerColor = ExpressiveSurfaceHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.run {
                        linearGradient(
                                        colors = listOf(
                                            ExpressivePrimaryStart.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Asset Type Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ExpressivePrimaryStart.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = asset.type.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ExpressivePrimaryStart
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ExpressiveSecondary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${asset.weightInGrams}g",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ExpressiveSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Value Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.current_value_label),
                            style = MaterialTheme.typography.titleMedium,
                            color = ExpressiveOnSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = asset.totalCurrentValue.formatCurrency(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = ExpressiveOnSurface,
                            fontSize = 36.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(1f + shimmerAlpha * 0.1f)
                            .clip(CircleShape)
                            .background(
                                if (isPositive)
                                    ExpressiveTertiary.copy(alpha = shimmerAlpha)
                                else
                                    ExpressiveError.copy(alpha = shimmerAlpha)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Performance Chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isPositive)
                        ExpressiveTertiary.copy(alpha = 0.2f)
                    else
                        ExpressiveError.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            tint = if (isPositive) ExpressiveTertiary else ExpressiveError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${if (isPositive) "+" else ""}${String.format("%.2f", percentage)}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) ExpressiveTertiary else ExpressiveError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.total_return),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpressiveOnSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = ExpressiveOnSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(20.dp))

                // Details Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.quantity_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpressiveOnSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${asset.quantity}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ExpressiveOnSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.bought_at_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpressiveOnSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = asset.purchasePrice.formatCurrency(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ExpressivePrimaryStart
                        )
                    }
                }
            }
        }
    }
}