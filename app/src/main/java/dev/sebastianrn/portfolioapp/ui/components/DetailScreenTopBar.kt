import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import dev.sebastianrn.portfolioapp.ui.theme.ExpressiveOnSurface
import dev.sebastianrn.portfolioapp.ui.theme.ExpressiveSurfaceHigh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreemTopBar(
    title: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onEditClick) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit Asset"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ExpressiveSurfaceHigh,
            titleContentColor = ExpressiveOnSurface,
            navigationIconContentColor = ExpressiveOnSurface,
            actionIconContentColor = ExpressiveOnSurface
        )
    )
}