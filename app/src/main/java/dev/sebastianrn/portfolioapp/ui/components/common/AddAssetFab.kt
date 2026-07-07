package dev.sebastianrn.portfolioapp.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import dev.sebastianrn.portfolioapp.ui.theme.AppGradients
import dev.sebastianrn.portfolioapp.ui.theme.GoldDeep
import dev.sebastianrn.portfolioapp.ui.theme.OnGold
import androidx.compose.ui.unit.dp

/**
 * Gold gradient floating action button. Built on a plain Box so the shadow
 * wraps the gradient surface directly (a transparent Material FAB surface
 * over an external gradient can produce shadow artifacts).
 */
@Composable
fun AddAssetFab(
    onClick: () -> Unit,
    contentDescription: String = "Add Asset"
) {
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(elevation = 12.dp, shape = shape, ambientColor = GoldDeep, spotColor = GoldDeep)
            .clip(shape)
            .background(AppGradients.goldCard)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = contentDescription,
            tint = OnGold,
            modifier = Modifier.size(28.dp)
        )
    }
}
