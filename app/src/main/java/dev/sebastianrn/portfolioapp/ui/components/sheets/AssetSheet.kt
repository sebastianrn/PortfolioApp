package dev.sebastianrn.portfolioapp.ui.components.sheets

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.data.model.AssetType
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.ui.components.common.AppTextField
import dev.sebastianrn.portfolioapp.ui.components.common.GoldButton
import dev.sebastianrn.portfolioapp.ui.components.common.SheetHeader
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetSheet(
    asset: GoldAsset? = null,
    onDismiss: () -> Unit,
    onSave: (GoldAsset) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    data class WeightOption(val label: String, val grams: Double, val type: AssetType)

    val options = listOf(
        WeightOption(stringResource(R.string.weight_coin_1oz), 31.1035, AssetType.COIN),
        WeightOption(stringResource(R.string.weight_coin_half_oz), 15.5517, AssetType.COIN),
        WeightOption(stringResource(R.string.weight_coin_quarter_oz), 7.7758, AssetType.COIN),
        WeightOption(stringResource(R.string.weight_bar_500g), 500.0, AssetType.BAR),
        WeightOption(stringResource(R.string.weight_bar_250g), 250.0, AssetType.BAR),
        WeightOption(stringResource(R.string.weight_bar_100g), 100.0, AssetType.BAR),
        WeightOption(stringResource(R.string.weight_bar_50g), 50.0, AssetType.BAR)
    )

    val initialOption = if (asset != null) {
        options.find { abs(it.grams - asset.weightInGrams) < 0.1 && it.type == asset.type }
            ?: options[0]
    } else {
        options[0]
    }

    var name by remember { mutableStateOf(asset?.name ?: "") }
    var purchasePrice by remember { mutableStateOf(asset?.purchasePrice?.toString() ?: "") }
    var quantity by remember { mutableStateOf(asset?.quantity?.toString() ?: "1") }
    var philoroId by remember { mutableStateOf(asset?.philoroId?.toString() ?: "") }
    var selectedOption by remember { mutableStateOf(initialOption) }
    var expanded by remember { mutableStateOf(false) }

    var isNameError by remember { mutableStateOf(false) }
    var isPriceError by remember { mutableStateOf(false) }
    var isQuantityError by remember { mutableStateOf(false) }

    val isEditMode = asset != null
    val title = stringResource(if (isEditMode) R.string.sheet_edit_asset else R.string.sheet_add_asset)
    val buttonText = stringResource(if (isEditMode) R.string.update_asset else R.string.add_to_portfolio)

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
            SheetHeader(title = title)

            Spacer(modifier = Modifier.height(24.dp))

            AppTextField(
                value = name,
                onValueChange = {
                    name = it
                    isNameError = false
                },
                label = stringResource(R.string.asset_name_label),
                placeholder = stringResource(R.string.asset_name_placeholder),
                isError = isNameError,
                errorMessage = stringResource(R.string.asset_name_error)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                AppTextField(
                    value = selectedOption.label,
                    onValueChange = {},
                    label = stringResource(R.string.weight_type_label),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier.menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option.label,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                selectedOption = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AppTextField(
                        value = quantity,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() }) {
                                quantity = it
                                isQuantityError = false
                            }
                        },
                        label = stringResource(R.string.quantity_label),
                        keyboardType = KeyboardType.Number,
                        isError = isQuantityError,
                        errorMessage = stringResource(R.string.quantity_error)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    AppTextField(
                        value = philoroId,
                        onValueChange = { if (it.all { c -> c.isDigit() }) philoroId = it },
                        label = stringResource(R.string.philoro_id_label),
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = purchasePrice,
                onValueChange = {
                    purchasePrice = it
                    isPriceError = false
                },
                label = stringResource(if (isEditMode) R.string.purchase_price_total else R.string.paid_price_total),
                keyboardType = KeyboardType.Decimal,
                isError = isPriceError,
                errorMessage = stringResource(R.string.price_error),
                suffix = "CHF"
            )

            Spacer(modifier = Modifier.height(24.dp))

            GoldButton(
                text = buttonText,
                onClick = {
                    val q = quantity.toIntOrNull()
                    val p = purchasePrice.toDoubleOrNull()
                    val i = philoroId.toIntOrNull() ?: 0

                    isNameError = name.isBlank()
                    isQuantityError = (q == null)
                    isPriceError = (p == null)

                    if (!isNameError && q != null && p != null) {
                        onSave(
                            GoldAsset(
                                id = asset?.id ?: 0,
                                name = name,
                                type = selectedOption.type,
                                purchasePrice = p,
                                currentSellPrice = asset?.currentSellPrice ?: p,
                                currentBuyPrice = asset?.currentBuyPrice ?: p,
                                quantity = q,
                                weightInGrams = selectedOption.grams,
                                philoroId = i
                            )
                        )
                    }
                }
            )
        }
    }
}
