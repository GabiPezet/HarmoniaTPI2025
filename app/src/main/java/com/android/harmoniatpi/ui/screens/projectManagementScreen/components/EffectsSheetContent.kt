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
import androidx.compose.material.icons.filled.AutoFixHigh
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import kotlin.math.ln

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
    onApplyLowPass: (id: Long, frequency: Float) -> Unit,
    onApplyFadeIn: (Long, Float) -> Unit,
    onApplyFadeOut: (Long, Float) -> Unit,
    onApplyTelephone: (Long) -> Unit,
    onApplyDistortion: (Long, Float) -> Unit,
    onApplyTremolo: (Long, Float, Float) -> Unit,
    onNormalize: (id: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Delay",
        "High Pass",
        "Low Pass",
        "Flanger",
        "Fade In",
        "Fade Out",
        "Telephone",
        "Distorsión",
        "Trémolo",
    )

    // Estado local para cada efecto
    var delayTimeMs by remember { mutableFloatStateOf(500f) }
    var delayDecay by remember { mutableFloatStateOf(0.5f) }
    var hpfFrequency by remember { mutableFloatStateOf(100f) }
    var flangerRate by remember { mutableFloatStateOf(0.1f) }
    var flangerWet by remember { mutableFloatStateOf(0.5f) }
    var lpfFrequency by remember { mutableFloatStateOf(5000f) }
    var pitchFactor by remember { mutableFloatStateOf(1.0f) }
    var fadeInSec by remember { mutableFloatStateOf(2.0f) }
    var fadeOutSec by remember { mutableFloatStateOf(2.0f) }
    val decimalFormat = remember { DecimalFormat("0.##") }
    var distDrive by remember { mutableFloatStateOf(0.5f) }
    var tremFreq by remember { mutableFloatStateOf(5.0f) }
    var tremDepth by remember { mutableFloatStateOf(0.8f) }

    fun getCurrentConfig(): EffectConfig {
        return when (selectedTabIndex) {
            0 -> EffectConfig.Delay(delayTimeMs / 1000f, delayDecay)
            1 -> EffectConfig.HighPass(hpfFrequency)
            2 -> EffectConfig.LowPass(lpfFrequency)
            3 -> EffectConfig.Flanger(flangerRate, flangerWet)
            4 -> EffectConfig.FadeIn(fadeInSec)
            5 -> EffectConfig.FadeOut(fadeOutSec)
            6 -> EffectConfig.Telephone
            7 -> EffectConfig.Distortion(distDrive)
            8 -> EffectConfig.Tremolo(tremFreq, tremDepth)
            else -> EffectConfig.Delay(0.5f, 0.5f)
        }
    }
    val isEffectPremium = selectedTabIndex != 0
    val canApply = isPremium || !isEffectPremium

    // Observador de cambios en tiempo real
    LaunchedEffect(delayTimeMs, delayDecay, hpfFrequency, flangerRate, flangerWet) {
        if (isPreviewing) {
            onParamChange(getCurrentConfig())
        }
    }

    // Observador de cambios en tiempo real:
    // Si el usuario mueve un slider mientras el preview está activo, actualizamos el motor de audio.
    LaunchedEffect(
        delayTimeMs,
        delayDecay,
        hpfFrequency,
        lpfFrequency,
        flangerRate,
        flangerWet,
        pitchFactor
    ) {
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
            .fillMaxHeight(0.65f)
            .padding(vertical = 16.dp, horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Efectos: ${track.title}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            // Botón de Acción Rápida: Normalizar
            OutlinedButton(
                onClick = { onNormalize(track.id) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Normalizar")
            }
        }

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // --- PESTAÑAS SCROLLABLES ---
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- PANELES DE CONTROL ---
            when (selectedTabIndex) {
                0 -> DelayControlPanel(
                    timeMs = delayTimeMs,
                    decay = delayDecay,
                    onTimeChange = { delayTimeMs = it },
                    onDecayChange = { delayDecay = it }
                )

                1 -> FilterControlPanel(
                    title = "Filtro Pasa-Altos (High Pass)",
                    description = "Elimina frecuencias graves (ruido de fondo, golpes) por debajo del corte.",
                    frequency = hpfFrequency,
                    range = 20f..1000f,
                    onFrequencyChange = { hpfFrequency = it }
                )

                2 -> FilterControlPanel(
                    title = "Filtro Pasa-Bajos (Low Pass)",
                    description = "Elimina frecuencias agudas (silbidos) por encima del corte. Crea sonido 'apagado'.",
                    frequency = lpfFrequency,
                    range = 200f..20000f,
                    onFrequencyChange = { lpfFrequency = it }
                )

                3 -> FlangerControlPanel(
                    rate = flangerRate,
                    wet = flangerWet,
                    onRateChange = { flangerRate = it },
                    onWetChange = { flangerWet = it }
                )

                4 -> FadeControlPanel("Fade In", fadeInSec) { fadeInSec = it }
                5 -> FadeControlPanel("Fade Out", fadeOutSec) { fadeOutSec = it }

                6 -> Column {
                    Text("Efecto Teléfono", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Aplica un filtro de banda y distorsión ligera para simular una llamada.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                7 -> Column {
                    Text("Drive (Intensidad): ${(distDrive * 100).toInt()}%")
                    Slider(value = distDrive, onValueChange = { distDrive = it }, valueRange = 0.0f..1.0f)
                    Text("Agrega suciedad y saturación a la señal.", style = MaterialTheme.typography.bodySmall)
                }

                8 -> Column {
                    Text("Velocidad: ${String.format("%.1f", tremFreq)} Hz")
                    Slider(value = tremFreq, onValueChange = { tremFreq = it }, valueRange = 0.5f..15.0f)
                    Spacer(Modifier.height(8.dp))
                    Text("Profundidad: ${(tremDepth * 100).toInt()}%")
                    Slider(value = tremDepth, onValueChange = { tremDepth = it }, valueRange = 0.0f..1.0f)
                }

            }

            Spacer(Modifier.height(16.dp))
        }
        // Barra de Acciones Inferior (fija)
        EffectsActionButtons(
            canApply = canApply,
            isPreviewing = isPreviewing,
            onPreviewClick = { onPreviewToggle(getCurrentConfig()) },
            onCancelClick = onDismiss,
            onApplyClick = {
                // Despacha la acción correspondiente al Tab activo
                when (selectedTabIndex) {
                    0 -> onApplyDelay(track.id, delayTimeMs / 1000f, delayDecay)
                    1 -> onApplyHighPass(track.id, hpfFrequency)
                    2 -> onApplyLowPass(track.id, lpfFrequency)
                    3 -> onApplyFlanger(track.id, flangerRate, flangerWet)
                    4 -> onApplyFadeIn(track.id, fadeInSec)
                    5 -> onApplyFadeOut(track.id, fadeOutSec)
                    6 -> onApplyTelephone(track.id)
                    7 -> onApplyDistortion(track.id, distDrive)
                    8 -> onApplyTremolo(track.id, tremFreq, tremDepth)
                }
            },
            onUpsellClick = onShowUpsell
        )
    }
}
    @Composable
    fun FadeControlPanel(title: String, seconds: Float, onValueChange: (Float) -> Unit) {
        Column {
            Text("$title: ${String.format("%.1f", seconds)} segundos")
            Slider(
                value = seconds,
                onValueChange = onValueChange,
                valueRange = 0.1f..10.0f
            )
        }
    }


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
    title: String,
    description: String,
    frequency: Float,
    range: ClosedFloatingPointRange<Float>,
    onFrequencyChange: (Float) -> Unit
) {
    Column {
        Text(
            "$title: ${frequency.toInt()} Hz",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Slider(
            value = frequency,
            onValueChange = onFrequencyChange,
            valueRange = range
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