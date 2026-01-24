package dev.sebastianrn.portfolioapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ModernHistoryCard(
    record: PriceHistory,
    onEditClick: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeSdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Card(
        onClick = if (record.isManual) onEditClick else { {} },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        enabled = record.isManual
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (record.isManual)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (record.isManual) Icons.Filled.Edit else Icons.Filled.CloudDone,
                        contentDescription = null,
                        tint = if (record.isManual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        sdf.format(Date(record.dateTimestamp)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        timeSdf.format(Date(record.dateTimestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(record.sellPrice.toInt())}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
