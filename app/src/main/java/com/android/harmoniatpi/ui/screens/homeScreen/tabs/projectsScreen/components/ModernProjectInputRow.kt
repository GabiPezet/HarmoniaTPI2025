package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Un Composable de entrada de texto "moderno" y sin bordes para el diálogo.
 */
@Composable
fun ModernProjectInputRow(
    labelText: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default) {
    // Columna para incluir el campo de texto y el texto de soporte/error
    Column {

        Text(
            text = labelText,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp)
        )

        if (supportingText == null) {
            Spacer(Modifier.height(4.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Icon(
                imageVector = icon,
                contentDescription = null,
                // --- 3. CAMBIAMOS EL COLOR DEL ICONO ---
                // Lo hacemos menos prominente, a menos que sea un error
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, end = 16.dp)
            )

            // Campo de texto sin bordes (sin cambios)
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholderText, style = MaterialTheme.typography.bodyLarge) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = imeAction
                ),
                keyboardActions = keyboardActions,
                modifier = modifier.weight(1f),
                singleLine = singleLine,
                textStyle = MaterialTheme.typography.bodyLarge,
                isError = isError
            )
        }

        // Muestra el texto de soporte/error si existe (sin cambios)
        if (supportingText != null) {
            Text(
                text = supportingText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 56.dp) // Alineado con el inicio del texto
            )
        }

        // Divisor ligero para separar los campos (sin cambios)
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}