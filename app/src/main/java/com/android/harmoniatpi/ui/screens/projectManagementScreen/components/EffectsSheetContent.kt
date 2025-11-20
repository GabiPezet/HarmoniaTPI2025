package com.android.harmoniatpi.ui.screens.projectManagementScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.harmoniatpi.domain.model.audio.EffectConfig
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import java.text.DecimalFormat

/**
 * Contenido del BottomSheet para la configuración y aplicación de efectos de audio.
 *
 * Gestiona internamente el estado de los sliders (Delay, Filtro, Flanger) y expone
 * los eventos de previsualización (Preview) y aplicación final (Apply).
 *
 * @param track La pista de audio sobre la cual se aplicarán los efectos.
 * @param isPremium Indica si el usuario tiene acceso a funcionalidades de pago (Botón Aplicar).
 * @param isPreviewing Estado actual del reproductor de previsualización.
 * @param onPreviewToggle Callback para iniciar/detener la preescucha en tiempo real.
 * @param onParamChange Callback invocado cuando los sliders se mueven durante el preview para actualizar el DSP en vivo.
 * @param onApplyDelay Acción final para aplicar Delay y cerrar el sheet.
 * @param onApplyHighPass Acción final para aplicar Filtro y cerrar el sheet.
 * @param onApplyFlanger Acción final para aplicar Flanger y cerrar el sheet.
 * @param onDismiss Acción para cancelar y cerrar el sheet.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsSheetContent(
    track: TrackUi,
    isPremium: Boolean,
    isPreviewing: Boolean,
    onPreviewToggle: (EffectConfig) -> Unit,
    onParamChange: (EffectConfig) -> Unit,
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

    fun getCurrentConfig(): EffectConfig {
        return when (selectedTabIndex) {
            0 -> EffectConfig.Delay(delayTimeMs / 1000f, delayDecay)
            1 -> EffectConfig.HighPass(hpfFrequency)
            2 -> EffectConfig.Flanger(flangerRate, flangerWet)
            else -> EffectConfig.Delay(0.5f, 0.5f)
        }
    }

    // Efecto para notificar cambios en sliders si se está previsualizando
    LaunchedEffect(delayTimeMs, delayDecay, hpfFrequency, flangerRate, flangerWet) {
        if (isPreviewing) {
            onParamChange(getCurrentConfig())
        }
    }

    // Cuando cambiamos de tab, paramos el preview por seguridad
    LaunchedEffect(selectedTabIndex) {
        if (isPreviewing) {
            // Podrías optar por parar el preview al cambiar de tab
            onPreviewToggle(getCurrentConfig()) // Esto actuará como stop si ya está sonando y no manejas logica extra, o mejor llamar a un stop explícito en el parent.
        }
    }
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

            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

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
        Spacer(modifier = Modifier.weight(1f))

        // Botón de Aplicar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón de PREVIEW
            OutlinedButton(
                onClick = { onPreviewToggle(getCurrentConfig()) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if(isPreviewing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = if (isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isPreviewing) "Detener" else "Preescuchar")
            }

            Row {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                Spacer(Modifier.width(8.dp))

                // Botón APLICAR (Premium)
                Button(
                    onClick = {
                        when (selectedTabIndex) {
                            0 -> onApplyDelay(track.id, delayTimeMs / 1000f, delayDecay)
                            1 -> onApplyHighPass(track.id, hpfFrequency)
                            2 -> onApplyFlanger(track.id, flangerRate, flangerWet)
                        }
                    },
                    enabled = isPremium // Solo habilitado si es Premium
                ) {
                    if (!isPremium) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Premium",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text("Aplicar")
                }
            }
        }
    }
}