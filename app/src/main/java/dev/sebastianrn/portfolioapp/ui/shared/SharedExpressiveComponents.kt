package dev.sebastianrn.portfolioapp.ui.shared

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel

@Composable
fun PortfolioOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    suffix: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            },
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder, color = ExpressiveColors.OnSurface.copy(alpha = 0.4f)) }
            } else null,
            isError = isError,
            singleLine = true,
            readOnly = readOnly,
            trailingIcon = trailingIcon,
            suffix = if (suffix != null) {
                { Text(suffix, color = ExpressiveColors.OnSurface.copy(alpha = 0.6f)) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ExpressiveColors.PrimaryStart,
                unfocusedBorderColor = ExpressiveColors.OnSurface.copy(alpha = 0.3f),
                focusedTextColor = ExpressiveColors.OnSurface,
                unfocusedTextColor = ExpressiveColors.OnSurface,
                cursorColor = ExpressiveColors.PrimaryStart,
                focusedLabelColor = ExpressiveColors.PrimaryStart,
                unfocusedLabelColor = ExpressiveColors.OnSurface.copy(alpha = 0.6f),
                errorBorderColor = ExpressiveColors.ErrorAccent,
                errorLabelColor = ExpressiveColors.ErrorAccent,
                errorCursorColor = ExpressiveColors.ErrorAccent,
                errorTextColor = ExpressiveColors.OnSurface
            ),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = ExpressiveColors.ErrorAccent,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioTopBar(
    viewModel: GoldViewModel,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ExpressiveColors.PrimaryStart,
                                    ExpressiveColors.SecondaryGradient
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        stringResource(R.string.app_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Portfolio Dashboard",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExpressiveColors.PrimaryStart
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { viewModel.updatePricesFromScraper() }) {
                Icon(
                    Icons.Default.Refresh,
                    "Update Prices",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ExpressiveColors.SurfaceHigh,
            titleContentColor = ExpressiveColors.OnSurface,
            actionIconContentColor = ExpressiveColors.OnSurface
        )
    )
}

// ============================================
// ANIMATED FLOATING ACTION BUTTON
// ============================================
@Composable
fun AnimatedFloatingActionButton(
    onClick: () -> Unit
) {
    val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    FloatingActionButton(
        onClick = onClick,
        containerColor = ExpressiveColors.PrimaryStart,
        contentColor = Color.Black,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add Asset",
                modifier = Modifier.size(32.dp)
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .rotate(rotation)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                ExpressiveColors.SecondaryGradient.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}
