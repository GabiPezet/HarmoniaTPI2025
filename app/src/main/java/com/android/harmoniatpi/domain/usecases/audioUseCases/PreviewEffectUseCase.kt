package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.model.audio.EffectConfig
import javax.inject.Inject

/**
 * Caso de uso responsable de gestionar la previsualización en tiempo real de efectos de audio.
 *
 * Permite al usuario escuchar cómo afectará una configuración de efectos ([EffectConfig])
 * a una pista específica sin necesidad de procesar y guardar el archivo en disco.
 * Esto es fundamental para una experiencia de usuario fluida al ajustar parámetros (knobs/sliders).
 */
class PreviewEffectUseCase @Inject constructor(
    private val repository: AudioMixerRepository
) {
    /**
     * Inicia la reproducción de la pista seleccionada aplicando el efecto configurado "al vuelo".
     *
     * @param trackId El ID de la pista que se desea preescuchar.
     * @param config La configuración del efecto (tipo y parámetros) a aplicar en tiempo real.
     */
    fun start(trackId: Long, config: EffectConfig) = repository.startEffectPreview(trackId, config)

    /**
     * Detiene la previsualización activa inmediatamente.
     * Es importante llamar a esto cuando el usuario deja de ajustar los efectos, cierra el menú
     * o decide aplicar los cambios, para liberar los recursos de audio.
     */
    fun stop() = repository.stopEffectPreview()
}