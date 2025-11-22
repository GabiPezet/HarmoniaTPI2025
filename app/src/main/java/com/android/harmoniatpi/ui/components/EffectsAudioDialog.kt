package com.android.harmoniatpi.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsAudioDialog(
    track: TrackUi,
    onDismiss: () -> Unit,
    onApplyDelay: (id: Long, delayTimeMs: Float, decay: Float) -> Unit,
    onApplyHighPass: (id: Long, frequency: Float) -> Unit,
    onApplyFlanger: (id: Long, rate: Float, wet: Float) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Delay", "Filtro", "Flanger")

    // estado efectos
    var delayTimeMs by remember { mutableFloatStateOf(500f) }
    var delayDecay by remember { mutableFloatStateOf(0.5f) }
    var hpfFrequency by remember { mutableFloatStateOf(100f) }
    var flangerRate by remember { mutableFloatStateOf(0.1f) }
    var flangerWet by remember { mutableFloatStateOf(0.5f) }

    val decimalFormat = remember { DecimalFormat("0.##") }



    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aplicar Efecto a ${track.title}") },
        text = {
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

                when (selectedTabIndex) {
                    0 -> // delay
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

                    1 -> // highpass
                        Column {
                            Text("Filtro Pasa-Altos (HPF): ${hpfFrequency.toInt()} Hz")
                            Text(
                                "Corta las frecuencias graves por debajo de este valor.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Slider(
                                value = hpfFrequency,
                                onValueChange = { hpfFrequency = it },
                                valueRange = 20f..1000f // Rango de 20Hz a 1kHz
                            )
                        }

                    2 -> // flanger
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
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedTabIndex) {
                        0 -> onApplyDelay(track.id, delayTimeMs / 1000f, delayDecay)
                        1 -> onApplyHighPass(track.id, hpfFrequency)
                        2 -> onApplyFlanger(track.id, flangerRate, flangerWet)
                    }
                }
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}