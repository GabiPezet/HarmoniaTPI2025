package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun EditableProfileRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leading: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(imageVector = leading, contentDescription = null) },
        singleLine = true
    )
}