package dev.sebastianrn.portfolioapp.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.ui.components.chart.PortfolioChart
import dev.sebastianrn.portfolioapp.ui.theme.AppGradients

/**
 * Chart card. [referenceValue] draws a dashed break-even line (e.g. invested
 * amount for the portfolio, purchase price for a single asset).
 */
@Composable
fun PerformanceCard(
    points: List<Pair<Long, Double>>,
    referenceValue: Double? = null,
    referenceLabel: String = "Invested"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AppGradients.goldAccentLine)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    stringResource(R.string.performance_title).uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            PortfolioChart(
                points = points,
                showTimeRangeSelector = true,
                goldColor = MaterialTheme.colorScheme.primary,
                referenceValue = referenceValue,
                referenceLabel = referenceLabel
            )
        }
    }
}
