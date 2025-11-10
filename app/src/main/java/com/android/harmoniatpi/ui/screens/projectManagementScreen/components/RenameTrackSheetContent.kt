package com.android.harmoniatpi.ui.screens.projectManagementScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi

@Composable
fun RenameTrackSheetContent(
    track: TrackUi,
    onRename: (trackId: Long, newName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val maxLength = 11


    var newName by remember { mutableStateOf(track.title) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Para que el teclado se muestre automáticamente
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Renombrar Pista",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = newName,
            onValueChange = {
                if(it.length <= maxLength){
                newName = it }
                },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            label = { Text("Nuevo nombre") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (newName.isNotBlank()) {
                        onRename(track.id, newName)
                    }
                }
            ),
            supportingText = {
                Text(
                    text = "${newName.length} / $maxLength",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }

        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    keyboardController?.hide()
                    onRename(track.id, newName)
                },
                enabled = newName.isNotBlank() && newName != track.title
            ) {
                Icon(Icons.Default.Done, contentDescription = "Guardar")
                Spacer(Modifier.width(8.dp))
                Text("Guardar")
            }
        }
    }
}
