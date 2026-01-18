import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.ui.components.ExpressiveColors
import dev.sebastianrn.portfolioapp.ui.components.PortfolioChart

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