package dev.sebastianrn.portfolioapp.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.ui.theme.GainOnGold
import dev.sebastianrn.portfolioapp.ui.theme.LossOnGold

/**
 * Pill chip showing a trend with arrow, tinted green for gains and red for
 * losses. When [onGold] is true the tint uses the deep gain/loss inks that
 * stay readable on gold gradient surfaces.
 */
@Composable
fun TrendChip(
    text: String,
    positive: Boolean,
    modifier: Modifier = Modifier,
    onGold: Boolean = false
) {
    val contentColor = when {
        onGold && positive -> GainOnGold
        onGold -> LossOnGold
        positive -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }
    val containerColor = if (onGold) {
        Color.White.copy(alpha = 0.30f)
    } else {
        contentColor.copy(alpha = 0.14f)
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (positive) {
                    Icons.AutoMirrored.Filled.TrendingUp
                } else {
                    Icons.AutoMirrored.Filled.TrendingDown
                },
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
