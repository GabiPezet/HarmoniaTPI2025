package com.android.harmoniatpi.ui.screens.menuPrincipal.content.optionsScreens.userProfile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.model.MenuUiState
import com.android.harmoniatpi.ui.screens.menuPrincipal.content.viewmodel.DrawerContentViewModel

@Composable
fun WorkProfileCard(
    uiState: MenuUiState,
    viewModel: DrawerContentViewModel
) {
    var isEditing by remember { mutableStateOf(false) }
    var instrument by remember(uiState.instrument) { mutableStateOf(uiState.instrument) }
    var genres by remember(uiState.genres) { mutableStateOf(uiState.genres) }
    var location by remember(uiState.location) { mutableStateOf(uiState.location) }
    var rating by remember(uiState.rating) { mutableFloatStateOf(uiState.rating) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Perfil Profesional",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isEditing) {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar perfil",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (isEditing) {
                EditableProfileRow(
                    label = "Tu Instrumento:",
                    value = instrument,
                    onValueChange = { instrument = it },
                    leading = Icons.Default.Mic
                )
                Spacer(modifier = Modifier.height(8.dp))
                EditableProfileRow(
                    label = "Género Favorito:",
                    value = genres,
                    onValueChange = { genres = it },
                    leading = Icons.Default.MusicNote
                )
                Spacer(modifier = Modifier.height(8.dp))
                EditableProfileRow(
                    label = "Ubicación:",
                    value = location,
                    onValueChange = { location = it },
                    leading = Icons.Default.Place
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tu valoración: ${"%.1f".format(rating)} / 5.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = rating,
                        onValueChange = { rating = it },
                        valueRange = 0f..5f,
                        steps = 9
                    )
                    RatingBar(
                        rating = rating,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        instrument = uiState.instrument
                        genres = uiState.genres
                        location = uiState.location
                        rating = uiState.rating
                        isEditing = false
                    }) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        viewModel.updateWorkProfile(instrument, genres, location)
                        viewModel.updateRating(rating)
                        viewModel.updateUserPreferences()
                        isEditing = false
                    }) {
                        Text("Guardar")
                    }
                }

            } else {
                // MODO VISUALIZACIÓN
                ProfileRow(
                    label = "Tu Instrumento:",
                    value = uiState.instrument,
                    leading = Icons.Default.Mic
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProfileRow(
                    label = "Género Favorito:",
                    value = uiState.genres,
                    leading = Icons.Default.MusicNote
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProfileRow(
                    label = "Ubicación:", value = uiState.location, leading = Icons.Default.Place
                )
                Spacer(modifier = Modifier.height(16.dp))
                RatingBar(rating = uiState.rating, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
