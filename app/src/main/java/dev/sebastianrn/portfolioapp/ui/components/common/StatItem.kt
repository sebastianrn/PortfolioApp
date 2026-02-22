package dev.sebastianrn.portfolioapp.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.util.formatAsPercentage
import dev.sebastianrn.portfolioapp.util.formatCurrency

@Composable
fun StatItem(
    label: String,
    value: Double,
    percentage: Double? = null,
    isCurrency: Boolean = true,
    isValueFormatShort: Boolean = false,
    neutralColorNeeded: Boolean = false,
    isPositive: Boolean? = null,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    val color = when {
        neutralColorNeeded -> MaterialTheme.colorScheme.onSurface
        isPositive == true -> MaterialTheme.colorScheme.secondary
        isPositive == false -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    val valueFormatted = if (isCurrency) {
        value.formatCurrency(short = isValueFormatShort)
    } else {
        value.toInt().toString()
    }

    Column(horizontalAlignment = alignment) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valueFormatted,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        if (percentage != null && isPositive != null) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = color.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        if (isPositive) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = color
                    )
                    Text(
                        percentage.formatAsPercentage(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = color
                    )
                }
            }
        }
    }
}
