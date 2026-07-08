package dev.sebastianrn.portfolioapp.ui.components.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.ui.components.common.AppTextField
import dev.sebastianrn.portfolioapp.ui.components.common.GoldButton
import dev.sebastianrn.portfolioapp.ui.components.common.SheetHeader
import dev.sebastianrn.portfolioapp.util.DateFormats
import dev.sebastianrn.portfolioapp.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHistorySheet(
    onDismiss: () -> Unit,
    initialSellPrice: Double? = null,
    initialBuyPrice: Double? = null,
    initialDate: Long? = null,
    isEditMode: Boolean = false,
    onSave: (Double, Double, Long) -> Unit
) {
    var isSellError: Boolean by remember { mutableStateOf(false) }
    var isBuyError by remember { mutableStateOf(false) }
    var sellPrice by remember { mutableStateOf(initialSellPrice?.toString() ?: "") }
    var buyPrice by remember { mutableStateOf(initialBuyPrice?.toString() ?: "") }
    var selectedDate by remember { mutableLongStateOf(initialDate ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

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
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        stringResource(R.string.cancel_action),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
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
            SheetHeader(
                title = stringResource(if (isEditMode) R.string.sheet_edit_record else R.string.update_value_title)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Date Picker Field
            Box(modifier = Modifier.fillMaxWidth()) {
                AppTextField(
                    value = remember(selectedDate) { selectedDate.formatDate(DateFormats.shortDate) },
                    onValueChange = {},
                    label = stringResource(R.string.date_field_label),
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
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

            AppTextField(
                value = sellPrice,
                onValueChange = {
                    sellPrice = it
                    isSellError = false
                },
                label = stringResource(if (isEditMode) R.string.sell_price_label else R.string.new_sell_price_label),
                keyboardType = KeyboardType.Decimal,
                isError = isSellError,
                errorMessage = stringResource(R.string.sell_price_error),
                suffix = "CHF"
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = buyPrice,
                onValueChange = {
                    buyPrice = it
                    isBuyError = false
                },
                label = stringResource(if (isEditMode) R.string.buy_price_label else R.string.new_buy_price_label),
                keyboardType = KeyboardType.Decimal,
                isError = isBuyError,
                errorMessage = stringResource(R.string.buy_price_error),
                suffix = "CHF"
            )

            Spacer(modifier = Modifier.height(24.dp))

            GoldButton(
                text = stringResource(if (isEditMode) R.string.update_action else R.string.save_action),
                onClick = {
                    val validSell = sellPrice.toDoubleOrNull()
                    val validBuy = buyPrice.toDoubleOrNull()

                    isSellError = (validSell == null)
                    isBuyError = (validBuy == null)

                    if (validSell != null && validBuy != null) {
                        onSave(validSell, validBuy, selectedDate)
                    }
                }
            )
        }
    }
}
