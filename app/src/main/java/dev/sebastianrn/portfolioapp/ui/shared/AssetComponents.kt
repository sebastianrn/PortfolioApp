package dev.sebastianrn.portfolioapp.ui.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.data.model.AssetType
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

            AnimatedVisibility(
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
                PortfolioOutlinedTextField(
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

            PortfolioOutlinedTextField(
                value = sellPrice,
                onValueChange = {
                    sellPrice = it
                    isSellError = false
                },
                label = if (isEditMode) "Sell Price" else stringResource(R.string.new_sell_price_label),
                isError = isSellError,
                errorMessage = "Enter a valid sell price",
                suffix = "CHF"
            )

            Spacer(modifier = Modifier.height(16.dp))

            PortfolioOutlinedTextField(
                value = buyPrice,
                onValueChange = {
                    buyPrice = it
                    isBuyError = false
                },
                label = if (isEditMode) "Buy Price" else stringResource(R.string.new_buy_price_label),
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
