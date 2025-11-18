package com.android.harmoniatpi.ui.screens.projectManagementScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsSheetContent(
    track: TrackUi,
    onApplyDelay: (id: Long, delayTimeSec: Float, decay: Float) -> Unit,
    onApplyHighPass: (id: Long, frequency: Float) -> Unit,
    onApplyFlanger: (id: Long, rate: Float, wet: Float) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Delay", "Filtro", "Flanger")

    // Estado local para cada efecto
    var delayTimeMs by remember { mutableFloatStateOf(500f) }
    var delayDecay by remember { mutableFloatStateOf(0.5f) }
    var hpfFrequency by remember { mutableFloatStateOf(100f) }
    var flangerRate by remember { mutableFloatStateOf(0.1f) }
    var flangerWet by remember { mutableFloatStateOf(0.5f) }

    val decimalFormat = remember { DecimalFormat("0.##") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Efectos: ${track.title}",
            style = MaterialTheme.typography.titleLarge
        )

        Column {
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            // Contenido de la pestaña seleccionada
            when (selectedTabIndex) {
                0 -> // Delay
                    Column {
                        Text("Tiempo (ms): ${delayTimeMs.toInt()}")
                        Slider(
                            value = delayTimeMs,
                            onValueChange = { delayTimeMs = it },
                            valueRange = 100f..2000f
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Decay (Eco): ${decimalFormat.format(delayDecay)}")
                        Slider(
                            value = delayDecay,
                            onValueChange = { delayDecay = it },
                            valueRange = 0.1f..0.9f
                        )
                    }
                1 -> // High-pass Filter
                    Column {
                        Text("Filtro Pasa-Altos (HPF): ${hpfFrequency.toInt()} Hz")
                        Text("Corta las frecuencias graves por debajo de este valor.", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = hpfFrequency,
                            onValueChange = { hpfFrequency = it },
                            valueRange = 20f..1000f
                        )
                    }
                2 -> // Flanger
                    Column {
                        Text("Rate (Velocidad): ${decimalFormat.format(flangerRate)} Hz")
                        Slider(
                            value = flangerRate,
                            onValueChange = { flangerRate = it },
                            valueRange = 0.01f..1.0f
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Wet (Efecto): ${decimalFormat.format(flangerWet * 100)}%")
                        Slider(
                            value = flangerWet,
                            onValueChange = { flangerWet = it },
                            valueRange = 0.1f..1.0f
                        )
                    }
            }
        }

        // Botón de Aplicar
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedButton(onClick = onDismiss) {
            Text("Cancelar")
        }
        Spacer(Modifier.width(8.dp))

        Button(
            onClick = {
                when (selectedTabIndex) {
                    0 -> onApplyDelay(track.id, delayTimeMs / 1000f, delayDecay)
                    1 -> onApplyHighPass(track.id, hpfFrequency)
                    2 -> onApplyFlanger(track.id, flangerRate, flangerWet)
                }
            }
        ) {
            Text("Aplicar Efecto")
        }
    }
}
