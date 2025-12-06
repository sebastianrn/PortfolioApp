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
    onSave: (String, AssetType, Double, Int, Double, Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 1. Define Options
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

    // 2. Initialize State
    // If asset exists (Edit Mode), try to match its weight to an option, otherwise default to first
    val initialOption = if (asset != null) {
        options.find { abs(it.grams - asset.weightInGrams) < 0.1 && it.type == asset.type } ?: options[0]
    } else {
        options[0]
    }

    var name by remember { mutableStateOf(asset?.name ?: "") }
    var quantity by remember { mutableStateOf(asset?.quantity?.toString() ?: "1") }
    var premium by remember { mutableStateOf(asset?.premiumPercent?.toString() ?: "5.0") }
    var price by remember { mutableStateOf(asset?.originalPrice?.toString() ?: "") }
    var selectedOption by remember { mutableStateOf(initialOption) }
    var expanded by remember { mutableStateOf(false) }

    val isEditMode = asset != null
    val title = if (isEditMode) "Edit Asset" else "Add Investment"
    val buttonText = if (isEditMode) "Update Asset" else "Add to Portfolio"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
        // REMOVED: windowInsets parameter causing the error
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                // Add padding for navigation bar + extra spacing
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp)
                .imePadding() // ADDED: Handles keyboard overlap manually
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Name Input
            ModernTextField(
                value = name,
                onValueChange = { name = it },
                label = "Asset Name (e.g. Vreneli)"
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Weight/Type Dropdown
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
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                selectedOption = option
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Quantity & Premium Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ModernTextField(
                        value = quantity,
                        onValueChange = { if (it.all { c -> c.isDigit() }) quantity = it },
                        label = "Qty",
                        isNumber = true
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ModernTextField(
                        value = premium,
                        onValueChange = { premium = it },
                        label = "Prem %",
                        isNumber = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Price Input
            ModernTextField(
                value = price,
                onValueChange = { price = it },
                label = if (isEditMode) "Bought At (Total)" else "Paid Price (Total)",
                isNumber = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        onSave(
                            name,
                            selectedOption.type,
                            price.toDoubleOrNull() ?: 0.0,
                            quantity.toIntOrNull() ?: 1,
                            selectedOption.grams,
                            premium.toDoubleOrNull() ?: 0.0
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