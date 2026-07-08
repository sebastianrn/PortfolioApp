package dev.sebastianrn.portfolioapp.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sebastianrn.portfolioapp.ui.theme.GainOnGold
import dev.sebastianrn.portfolioapp.ui.theme.LossOnGold
import dev.sebastianrn.portfolioapp.ui.theme.OnGold
import dev.sebastianrn.portfolioapp.ui.theme.OnGoldMuted

/**
 * Frosted stats container for gold hero surfaces. Full-width rows give
 * values room to render large without wrapping.
 */
@Composable
fun GlassStatPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.22f))
            .border(1.dp, Color.White.copy(alpha = 0.30f), shape)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        content = content
    )
}

/** Divider between [GlassStatRow]s. */
@Composable
fun GlassStatDivider() {
    HorizontalDivider(color = Color.White.copy(alpha = 0.30f))
}

/**
 * One stat row: label left, value (with optional percent) right.
 * [trend] tints the value and shows a small dot (true = gain, false = loss).
 * The value auto-shrinks but at full row width it stays large.
 */
@Composable
fun GlassStatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subText: String? = null,
    trend: Boolean? = null
) {
    val trendColor = when (trend) {
        true -> GainOnGold
        false -> LossOnGold
        null -> null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            trendColor?.let {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(it, CircleShape)
                )
            }
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                fontWeight = FontWeight.SemiBold,
                color = OnGoldMuted,
                maxLines = 1
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            BasicText(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = trendColor ?: OnGold
                ),
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 11.sp,
                    maxFontSize = MaterialTheme.typography.titleMedium.fontSize,
                    stepSize = 0.5.sp
                )
            )
            subText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = trendColor ?: OnGoldMuted,
                    maxLines = 1
                )
            }
        }
    }
}
