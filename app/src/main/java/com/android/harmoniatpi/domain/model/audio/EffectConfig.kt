package com.android.harmoniatpi.domain.model.audio

/**
 * Representa la configuración de parámetros necesaria para aplicar un efecto de audio.
 *
 * Al ser una [sealed class], garantiza que el Repositorio reciba siempre
 * el conjunto correcto de parámetros asociados a un tipo de efecto específico,
 * facilitando el uso de sentencias 'when' exhaustivas.
 */

sealed class EffectConfig {
    /**
     * Configuración para el efecto de Delay (Eco).
     *
     * @property timeSec El tiempo de retraso entre la señal original y la repetición, en **SEGUNDOS**.
     * Por ejemplo: 0.5f representa 500ms.
     * @property decay El factor de decaimiento (feedback) de las repeticiones.
     * Generalmente varía entre 0.0 (sin repeticiones) y 1.0 (repeticiones infinitas/saturación).
     */
    data class Delay(val timeSec: Float, val decay: Float) : EffectConfig()

    /**
     * Configuración para el filtro Pasa-Altos (High Pass Filter).
     * Permite el paso de frecuencias por encima de [frequency] y atenúa las que están por debajo.
     * Útil para eliminar ruidos graves o "barro" en la mezcla.
     *
     * @property frequency La frecuencia de corte (Cutoff) en **Hertz (Hz)**.
     */
    data class HighPass(val frequency: Float) : EffectConfig()

    /**
     * Configuración para el efecto Flanger.
     * Produce un sonido oscilante similar al de un "avión a reacción" o un barrido metálico,
     * duplicando la señal y aplicando un retardo modulado.
     *
     * @property rate La velocidad de oscilación del efecto (LFO) en **Hertz (Hz)**.
     * Valores bajos (ej. 0.1Hz) producen barridos lentos; valores altos producen vibratos rápidos.
     * @property wet El nivel de mezcla de la señal procesada (efecto) respecto a la original (Dry).
     * Un valor de 0.0 es solo la señal original, 1.0 es máxima intensidad del efecto.
     */
    data class Flanger(val rate: Float, val wet: Float) : EffectConfig()

    data class LowPass(val frequency: Float) : EffectConfig()


    data class Reverb(val roomSize: Float, val damping: Float, val wet: Float) : EffectConfig()
    object Telephone : EffectConfig()
    data class Speed(val speed: Float) : EffectConfig()
    data class Distortion(val drive: Float) : EffectConfig()
    data class Compressor(val threshold: Float, val ratio: Float) : EffectConfig()
    data class MidBoost(val frequency: Float, val bandwidth: Float) : EffectConfig()
    data class FadeIn(val durationSeconds: Float) : EffectConfig()
    data class FadeOut(val durationSeconds: Float) : EffectConfig()
    data class Tremolo(val frequency: Float, val depth: Float) : EffectConfig()

    data class Preset(val type: PresetType) : EffectConfig()
}

enum class PresetType { MEGAPHONE, UNDERWATER, SLAPBACK }