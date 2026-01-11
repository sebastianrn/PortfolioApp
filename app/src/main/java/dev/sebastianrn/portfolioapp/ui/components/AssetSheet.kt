package dev.sebastianrn.portfolioapp.ui.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.data.AssetType
import dev.sebastianrn.portfolioapp.data.GoldAsset
import dev.sebastianrn.portfolioapp.ui.theme.GoldStart
import dev.sebastianrn.portfolioapp.ui.theme.TextGray
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
        WeightOption("1 oz Coin", 31.1035, AssetType.COIN),
        WeightOption("1/2 oz Coin", 15.5517, AssetType.COIN),
        WeightOption("1/4 oz Coin", 7.7758, AssetType.COIN),
        WeightOption("500g Bar", 500.0, AssetType.BAR),
        WeightOption("250g Bar", 250.0, AssetType.BAR),
        WeightOption("100g Bar", 100.0, AssetType.BAR),
        WeightOption("50g Bar", 50.0, AssetType.BAR)
    )

    val initialOption = if (asset != null) {
        options.find { abs(it.grams - asset.weightInGrams) < 0.1 && it.type == asset.type }
            ?: options[0]
    } else {
        options[0]
    }

    var name by remember { mutableStateOf(asset?.name ?: "") }
    var type by remember { mutableStateOf(asset?.type ?: AssetType.COIN) }
    var purchasePrice by remember { mutableStateOf(asset?.purchasePrice?.toString() ?: "") }
    var currentSellPrice by remember { mutableStateOf(asset?.currentSellPrice?.toString() ?: "") }
    var currentBuyPrice by remember { mutableStateOf(asset?.currentBuyPrice?.toString() ?: "") }
    var quantity by remember { mutableStateOf(asset?.quantity?.toString() ?: "1") }
    var philoroId by remember { mutableStateOf(asset?.philoroId?.toString() ?: "") }
    var selectedOption by remember { mutableStateOf(initialOption) }
    var expanded by remember { mutableStateOf(false) }

    val isEditMode = asset != null
    val title = if (isEditMode) "Edit Asset" else "Add Investment"
    val buttonText = if (isEditMode) "Update Asset" else "Add to Portfolio"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // CHANGED: Use surfaceVariant to stand out from background
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 24.dp
                )
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            ModernTextField(
                value = name,
                onValueChange = { name = it },
                label = "Asset Name (e.g. Vreneli)"
            )
            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedOption.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Weight / Type", color = TextGray) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldStart,
                        unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = GoldStart,
                        focusedLabelColor = GoldStart,
                        unfocusedLabelColor = TextGray
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    // Match the sheet container color for consistency
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
                            onClick = { selectedOption = option; expanded = false }
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
                    ModernTextField(
                        value = quantity,
                        onValueChange = { if (it.all { c -> c.isDigit() }) quantity = it },
                        label = "Qty",
                        isNumber = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = philoroId,
                onValueChange = { philoroId = it },
                label = { Text("Philoro ID (for Scraping)") },
                placeholder = { Text("e.g. 1991") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            ModernTextField(
                value = purchasePrice,
                onValueChange = { purchasePrice = it },
                label = if (isEditMode) "Bought At (Total)" else "Paid Price (Total)",
                isNumber = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val q = quantity.toIntOrNull()
                    val p = purchasePrice.toDoubleOrNull()
                    val i = philoroId.toInt()

                    if (name.isNotBlank() && q != null && p != null) {
                        onSave(
                            GoldAsset(
                                id = asset?.id ?: 0,
                                name = name,
                                type = selectedOption.type,
                                purchasePrice = p,
                                currentSellPrice = p,
                                currentBuyPrice = asset?.currentBuyPrice ?: p,
                                quantity = q,
                                weightInGrams = selectedOption.grams,
                                philoroId = i
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldStart,
                    contentColor = Color.Black
                )
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}