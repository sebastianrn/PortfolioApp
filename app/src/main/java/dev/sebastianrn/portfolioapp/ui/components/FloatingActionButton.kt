package dev.sebastianrn.portfolioapp.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.Black,
        shape = MaterialTheme.shapes.large
    ) {
        Icon(
            Icons.Filled.Add,
            "Add Asset",
            modifier = Modifier.size(28.dp)
        )
    }
}
