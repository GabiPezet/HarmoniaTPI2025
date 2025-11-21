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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import com.android.harmoniatpi.ui.components.PremiumAwareButton
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
 * @param onShowUpsell Callback para mostrar el diálogo de compra (Premium).
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsSheetContent(
    track: TrackUi,
    isPreviewing: Boolean,
    isPremium: Boolean,
    onShowUpsell: () -> Unit,
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

    // Observador de cambios en tiempo real:
    // Si el usuario mueve un slider mientras el preview está activo, actualizamos el motor de audio.
    LaunchedEffect(delayTimeMs, delayDecay, hpfFrequency, flangerRate, flangerWet) {
        if (isPreviewing) {
            onParamChange(getCurrentConfig())
        }
    }

    // Seguridad: Si cambiamos de efecto (Tab), detenemos el preview para evitar
    // que suene un Delay con los parámetros de un Flanger.
    LaunchedEffect(selectedTabIndex) {
        if (isPreviewing) {
            onPreviewToggle(getCurrentConfig()) // Actúa como toggle off
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //Header
        Text(
            "Efectos: ${track.title}",
            style = MaterialTheme.typography.titleLarge
        )
        // Selector de Efecto
        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        // Panel de Control (Modularizado)
        Column(modifier = Modifier.weight(1f, fill = false)) {
            when (selectedTabIndex) {
                0 -> DelayControlPanel(
                    timeMs = delayTimeMs,
                    decay = delayDecay,
                    onTimeChange = { delayTimeMs = it },
                    onDecayChange = { delayDecay = it }
                )

                1 -> FilterControlPanel(
                    frequency = hpfFrequency,
                    onFrequencyChange = { hpfFrequency = it }
                )

                2 -> FlangerControlPanel(
                    rate = flangerRate,
                    wet = flangerWet,
                    onRateChange = { flangerRate = it },
                    onWetChange = { flangerWet = it }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Barra de Acciones Inferior
        EffectsActionButtons(
            isPremium = isPremium,
            onShowUpsell = onShowUpsell,
            isPreviewing = isPreviewing,
            onPreviewClick = { onPreviewToggle(getCurrentConfig()) },
            onCancelClick = onDismiss,
            onApplyClick = {
                // Despacha la acción correspondiente al Tab activo
                when (selectedTabIndex) {
                    0 -> onApplyDelay(track.id, delayTimeMs / 1000f, delayDecay)
                    1 -> onApplyHighPass(track.id, hpfFrequency)
                    2 -> onApplyFlanger(track.id, flangerRate, flangerWet)
                }
            }
        )
    }
}

// --- Sub-componentes de UI para mejor legibilidad y mantenimiento ---

@Composable
private fun DelayControlPanel(
    timeMs: Float,
    decay: Float,
    onTimeChange: (Float) -> Unit,
    onDecayChange: (Float) -> Unit
) {
    val decimalFormat = remember { DecimalFormat("0.##") }

    Column {
        Text("Tiempo: ${timeMs.toInt()} ms", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = timeMs,
            onValueChange = onTimeChange,
            valueRange = 100f..2000f
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Decay (Repeticiones): ${decimalFormat.format(decay)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = decay,
            onValueChange = onDecayChange,
            valueRange = 0.1f..0.9f
        )
    }
}

@Composable
private fun FilterControlPanel(
    frequency: Float,
    onFrequencyChange: (Float) -> Unit
) {
    Column {
        Text(
            "Frecuencia de corte: ${frequency.toInt()} Hz",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Elimina frecuencias graves por debajo de este valor.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Slider(
            value = frequency,
            onValueChange = onFrequencyChange,
            valueRange = 20f..1000f
        )
    }
}

@Composable
private fun FlangerControlPanel(
    rate: Float,
    wet: Float,
    onRateChange: (Float) -> Unit,
    onWetChange: (Float) -> Unit
) {
    val decimalFormat = remember { DecimalFormat("0.##") }

    Column {
        Text(
            "Rate (Velocidad): ${decimalFormat.format(rate)} Hz",
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = rate,
            onValueChange = onRateChange,
            valueRange = 0.01f..1.0f
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Intensidad (Wet): ${decimalFormat.format(wet * 100)}%",
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = wet,
            onValueChange = onWetChange,
            valueRange = 0.1f..1.0f
        )
    }
}

@Composable
private fun EffectsActionButtons(
    isPremium: Boolean,
    isPreviewing: Boolean,
    onPreviewClick: () -> Unit,
    onCancelClick: () -> Unit,
    onApplyClick: () -> Unit,
    onShowUpsell: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón de PREVIEW (Izquierda)
        OutlinedButton(
            onClick = onPreviewClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isPreviewing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = if (isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = if (isPreviewing) "Detener preview" else "Iniciar preview"
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isPreviewing) "Detener" else "Preescuchar")
        }

        // Botones de Acción (Derecha)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onCancelClick) {
                Text("Cancelar")
            }
            Spacer(Modifier.width(8.dp))

            PremiumAwareButton(
                text = "Aplicar",
                isPremium = isPremium,
                onClick = onApplyClick,
                onShowUpsell = onShowUpsell
            )
        }
    }
}