package com.android.harmoniatpi.ui.screens.homeScreen.tabs.projectsScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.project.CloningAccess

@Composable
fun CloningAccessSelector(
    selectedOption: CloningAccess,
    onOptionSelected: (CloningAccess) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Permisos de Clonado",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Row (verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedOption == CloningAccess.PUBLIC,
                onClick = { onOptionSelected(CloningAccess.PUBLIC) }
            )
            Text("Público (Todos)")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedOption == CloningAccess.FOLLOWERS_ONLY,
                onClick = { onOptionSelected(CloningAccess.FOLLOWERS_ONLY) }
            )
            Text("Solo Seguidores")
        }
    }
}