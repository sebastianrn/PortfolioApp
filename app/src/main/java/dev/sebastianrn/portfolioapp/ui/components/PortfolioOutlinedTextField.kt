package dev.sebastianrn.portfolioapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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