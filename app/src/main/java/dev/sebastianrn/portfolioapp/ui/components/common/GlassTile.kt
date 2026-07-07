package dev.sebastianrn.portfolioapp.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
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
 * Frosted stat tile for use on gold hero surfaces.
 *
 * [trend] adds a subtle gain/loss indicator: a small dot next to the label
 * and a tinted value (true = gain, false = loss, null = neutral).
 * The value auto-shrinks to always fit on a single line.
 */
@Composable
fun GlassTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subText: String? = null,
    trend: Boolean? = null,
    subContent: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(18.dp)
    val trendColor = when (trend) {
        true -> GainOnGold
        false -> LossOnGold
        null -> null
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.22f))
            .border(1.dp, Color.White.copy(alpha = 0.30f), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            trendColor?.let {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(it, CircleShape)
                )
            }
            // Sizes are capped low enough that all sibling tiles render
            // identically; auto-shrink only kicks in on very narrow screens.
            BasicText(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnGoldMuted
                ),
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 7.sp,
                    maxFontSize = 9.sp,
                    stepSize = 0.5.sp
                )
            )
        }
        BasicText(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = trendColor ?: OnGold
            ),
            maxLines = 1,
            softWrap = false,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp,
                maxFontSize = 12.sp,
                stepSize = 0.5.sp
            )
        )
        subText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = trendColor ?: OnGoldMuted
            )
        }
        subContent?.invoke()
    }
}
