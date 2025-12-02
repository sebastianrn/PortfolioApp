package com.example.portfolioapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.portfolioapp.ui.theme.GoldStart
import com.example.portfolioapp.ui.theme.TextGray
import com.example.portfolioapp.ui.theme.TextWhite

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isNumber: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextGray) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldStart,
            unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            cursorColor = GoldStart,
            focusedLabelColor = GoldStart,
            unfocusedLabelColor = TextGray
        ),
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        modifier = Modifier.fillMaxWidth()
    )
}