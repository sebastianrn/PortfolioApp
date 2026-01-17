package dev.sebastianrn.portfolioapp.ui.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.data.model.AssetType
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.util.formatCurrency
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// ============================================
// EXPRESSIVE COLORS OBJECT
// ============================================
object ExpressiveColors {
    val PrimaryStart = Color(0xFFFFD700) // Gold
    val PrimaryEnd = Color(0xFFFFB700) // Amber Gold
    val SecondaryGradient = Color(0xFFFF6F00) // Deep Orange
    val TertiaryAccent = Color(0xFF00E676) // Vibrant Green
    val ErrorAccent = Color(0xFFFF1744) // Bright Red
    val SurfaceContainer = Color(0xFF1A1A1A)
    val SurfaceHigh = Color(0xFF242424)
    val OnSurface = Color(0xFFE8E8E8)
}

@Composable
fun ExpressiveOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    isNumber: Boolean = false,
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
fun ExpressiveTopBar(
    isDark: Boolean,
    viewModel: GoldViewModel,
    onThemeToggle: () -> Unit,
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
                        "Expressive Dashboard",
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
// EXPRESSIVE PORTFOLIO HEADER
// ============================================
@Composable
fun ExpressivePortfolioHeader(
    totalValue: Double,
    dailyChange: Double,
    dailyChangePercent: Double,
    shimmerAlpha: Float
) {
    val isPositive = dailyChange >= 0
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExpressiveColors.SurfaceHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            ExpressiveColors.PrimaryStart.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.total_portfolio_value),
                            style = MaterialTheme.typography.titleMedium,
                            color = ExpressiveColors.OnSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(totalValue)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = ExpressiveColors.OnSurface,
                            fontSize = 40.sp
                        )
                    }

                    // Animated pulse indicator
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .scale(1f + shimmerAlpha * 0.1f)
                            .clip(CircleShape)
                            .background(
                                if (isPositive)
                                    ExpressiveColors.TertiaryAccent.copy(alpha = shimmerAlpha)
                                else
                                    ExpressiveColors.ErrorAccent.copy(alpha = shimmerAlpha)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Daily change chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isPositive)
                        ExpressiveColors.TertiaryAccent.copy(alpha = 0.2f)
                    else
                        ExpressiveColors.ErrorAccent.copy(alpha = 0.2f),
                    modifier = Modifier.animateContentSize()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            tint = if (isPositive) ExpressiveColors.TertiaryAccent else ExpressiveColors.ErrorAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(abs(dailyChange))} (${String.format("%.2f", abs(dailyChangePercent))}%)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) ExpressiveColors.TertiaryAccent else ExpressiveColors.ErrorAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpressiveColors.OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// EXPRESSIVE CHART CARD
// ============================================
@Composable
fun ExpressiveChartCard(
    points: List<Pair<Long, Double>>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExpressiveColors.SurfaceHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.performance_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ExpressiveColors.OnSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Enhanced chart with built-in time range selector
            Box(modifier = Modifier.fillMaxSize()) {
                if (points.isNotEmpty()) {
                    PortfolioChart(
                        points = points,
                        showTimeRangeSelector = true,
                        goldColor = ExpressiveColors.PrimaryStart
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        ExpressiveColors.PrimaryStart.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.empty_assets_list),
                            color = ExpressiveColors.OnSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// TIME RANGE CHIP
// ============================================
@Composable
fun TimeRangeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected)
            ExpressiveColors.PrimaryStart
        else
            ExpressiveColors.SurfaceContainer,
        modifier = Modifier.animateContentSize()
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.Black else ExpressiveColors.OnSurface.copy(alpha = 0.7f)
        )
    }
}

// ============================================
// QUICK STATS ROW
// ============================================
@Composable
fun QuickStatsRow(
    totalInvested: Double,
    assetCount: Int,
    bestPerformer: GoldAsset?
) {
    val bestPerformancePercent = bestPerformer?.let {
        if (it.purchasePrice > 0) {
            ((it.currentSellPrice - it.purchasePrice) / it.purchasePrice) * 100
        } else 0.0
    } ?: 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            icon = Icons.Outlined.TrendingUp,
            label = "Best Performer",
            value = if (bestPerformer != null) "+${String.format("%.1f", bestPerformancePercent)}%" else "N/A",
            color = ExpressiveColors.TertiaryAccent,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            icon = Icons.Outlined.AccountBalanceWallet,
            label = "Assets",
            value = "$assetCount",
            color = ExpressiveColors.PrimaryStart,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            icon = Icons.Outlined.Paid,
            label = "Invested",
            value = totalInvested.toString(),
            color = ExpressiveColors.SecondaryGradient,
            modifier = Modifier.weight(1f)
        )
    }
}

// ============================================
// QUICK STAT CARD
// ============================================
@Composable
fun QuickStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExpressiveColors.SurfaceHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExpressiveColors.OnSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = ExpressiveColors.OnSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ============================================
// ANIMATED HOLDING CARD
// ============================================
@Composable
fun AnimatedHoldingCard(
    asset: GoldAsset,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isPositive = asset.totalProfitOrLoss >= 0
    val changePercent = if (asset.purchasePrice > 0) {
        (asset.totalProfitOrLoss / (asset.purchasePrice * asset.quantity)) * 100
    } else 0.0

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExpressiveColors.SurfaceHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (asset.type == AssetType.COIN)
                                    ExpressiveColors.PrimaryStart.copy(alpha = 0.2f)
                                else
                                    ExpressiveColors.SecondaryGradient.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = asset.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (asset.type == AssetType.COIN)
                                ExpressiveColors.PrimaryStart
                            else
                                ExpressiveColors.SecondaryGradient
                        )
                    }

                    Column {
                        Text(
                            text = asset.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpressiveColors.OnSurface
                        )
                        Text(
                            text = "${asset.type.name} • ${asset.quantity} units",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpressiveColors.OnSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = asset.totalCurrentValue.formatCurrency(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpressiveColors.OnSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPositive)
                            ExpressiveColors.TertiaryAccent.copy(alpha = 0.2f)
                        else
                            ExpressiveColors.ErrorAccent.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${if (isPositive) "+" else ""}${String.format("%.2f", changePercent)}%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) ExpressiveColors.TertiaryAccent else ExpressiveColors.ErrorAccent
                        )
                    }
                }
            }

            this.AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = ExpressiveColors.OnSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))
                    HoldingDetailRow("Quantity", "${asset.quantity} units")
                    HoldingDetailRow("Weight", "${asset.weightInGrams}g")
                    HoldingDetailRow("Purchase Price", asset.purchasePrice.formatCurrency())
                    HoldingDetailRow("Current Price", asset.currentSellPrice.formatCurrency())
                    HoldingDetailRow("Total Return", asset.totalProfitOrLoss.formatCurrency())
                }
            }
        }
    }
}

// ============================================
// HOLDING DETAIL ROW
// ============================================
@Composable
fun HoldingDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ExpressiveColors.OnSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = ExpressiveColors.OnSurface
        )
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveEditHistorySheet(
    onDismiss: () -> Unit,
    initialSellPrice: Double? = null,
    initialBuyPrice: Double? = null,
    initialDate: Long? = null,
    isEditMode: Boolean = false,
    onSave: (Double, Double, Long) -> Unit
) {
    var isSellError by remember { mutableStateOf(false) }
    var isBuyError by remember { mutableStateOf(false) }
    var sellPrice by remember { mutableStateOf(initialSellPrice?.toString() ?: "") }
    var buyPrice by remember { mutableStateOf(initialBuyPrice?.toString() ?: "") }
    var selectedDate by remember { mutableLongStateOf(initialDate ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) {
                    Text(
                        stringResource(R.string.ok_action),
                        color = ExpressiveColors.PrimaryStart
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        stringResource(R.string.cancel_action),
                        color = ExpressiveColors.OnSurface.copy(alpha = 0.6f)
                    )
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = ExpressiveColors.SurfaceHigh
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = ExpressiveColors.SurfaceHigh,
                    selectedDayContainerColor = ExpressiveColors.PrimaryStart,
                    todayContentColor = ExpressiveColors.PrimaryStart,
                    todayDateBorderColor = ExpressiveColors.PrimaryStart
                )
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ExpressiveColors.SurfaceHigh,
        contentColor = ExpressiveColors.OnSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ExpressiveColors.PrimaryStart.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEditMode) "Edit Record" else stringResource(R.string.update_value_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = ExpressiveColors.OnSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Date Picker Field
            Box(modifier = Modifier.fillMaxWidth()) {
                ExpressiveOutlinedTextField(
                    value = sdf.format(Date(selectedDate)),
                    onValueChange = {},
                    label = "Date",
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = null,
                            tint = ExpressiveColors.PrimaryStart
                        )
                    }
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExpressiveOutlinedTextField(
                value = sellPrice,
                onValueChange = {
                    sellPrice = it
                    isSellError = false
                },
                label = if (isEditMode) "Sell Price" else stringResource(R.string.new_sell_price_label),
                isNumber = true,
                isError = isSellError,
                errorMessage = "Enter a valid sell price",
                suffix = "CHF"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExpressiveOutlinedTextField(
                value = buyPrice,
                onValueChange = {
                    buyPrice = it
                    isBuyError = false
                },
                label = if (isEditMode) "Buy Price" else stringResource(R.string.new_buy_price_label),
                isNumber = true,
                isError = isBuyError,
                errorMessage = "Enter a valid buy price",
                suffix = "CHF"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val validSell = sellPrice.toDoubleOrNull()
                    val validBuy = buyPrice.toDoubleOrNull()

                    isSellError = (validSell == null)
                    isBuyError = (validBuy == null)

                    if (validSell != null && validBuy != null) {
                        onSave(validSell, validBuy, selectedDate)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExpressiveColors.PrimaryStart,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = if (isEditMode) "Update" else stringResource(R.string.save_action),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}