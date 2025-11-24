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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Color
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

    fun getCurrentConfig(): EffectConfig {
        return when (selectedTabIndex) {
            0 -> EffectConfig.Delay(delayTimeMs / 1000f, delayDecay)
            1 -> EffectConfig.HighPass(hpfFrequency)
            2 -> EffectConfig.Flanger(flangerRate, flangerWet)
            else -> EffectConfig.Delay(0.5f, 0.5f)
        }
    }

    // Lógica crítica: El Delay (0) es gratis. Los otros (1 y 2) requieren Premium.
    val isEffectPremium = selectedTabIndex != 0
    val canApply = isPremium || !isEffectPremium

    // Observador de cambios en tiempo real
    LaunchedEffect(delayTimeMs, delayDecay, hpfFrequency, flangerRate, flangerWet) {
        if (isPreviewing) {
            onParamChange(getCurrentConfig())
        }
    }

    // Seguridad: Si cambiamos de efecto (Tab), detenemos el preview
    LaunchedEffect(selectedTabIndex) {
        if (isPreviewing) {
            onPreviewToggle(getCurrentConfig())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f) // Un poco más alto para que entren bien los controles
            .padding(vertical = 16.dp, horizontal = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                "Efectos: ${track.title}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(16.dp))

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
            Spacer(Modifier.height(16.dp))

            // Panel de Control
            Column {
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
        }

        // Barra de Acciones Inferior
        EffectsActionButtons(
            canApply = canApply,
            isPreviewing = isPreviewing,
            onPreviewClick = { onPreviewToggle(getCurrentConfig()) },
            onCancelClick = onDismiss,
            onApplyClick = {
                when (selectedTabIndex) {
                    0 -> onApplyDelay(track.id, delayTimeMs / 1000f, delayDecay)
                    1 -> onApplyHighPass(track.id, hpfFrequency)
                    2 -> onApplyFlanger(track.id, flangerRate, flangerWet)
                }
            },
            onUpsellClick = onShowUpsell
        )
    }
}

// --- Sub-componentes ---

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
    canApply: Boolean,
    isPreviewing: Boolean,
    onPreviewClick: () -> Unit,
    onCancelClick: () -> Unit,
    onApplyClick: () -> Unit,
    onUpsellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        // 1. NIVEL SUPERIOR: Botón de Preview (Alineado a la izquierda)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
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
        }

        Spacer(Modifier.height(16.dp)) // Espacio entre la fila de preview y la fila de acciones

        // 2. NIVEL INFERIOR: Cancelar y Aplicar (Alineados a la derecha, uno al lado del otro)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End, // Alineados al final
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancelClick) {
                Text("Cancelar")
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = { if (canApply) onApplyClick() else onUpsellClick() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canApply) MaterialTheme.colorScheme.primary else Color(0xFFD4AF37) // Dorado
                )
            ) {
                if (canApply) {
                    Text("Aplicar")
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Premium Only - Obtener", color = Color.White)
                    }
                }
            }
        }
    }
}