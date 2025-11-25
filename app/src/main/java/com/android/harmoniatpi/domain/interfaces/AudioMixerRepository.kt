package com.android.harmoniatpi.domain.interfaces

import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.domain.model.audio.EffectConfig
import com.android.harmoniatpi.domain.model.audio.Track
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Interfaz principal para la gestión y orquestación de múltiples pistas de audio.
 *
 * Define las operaciones disponibles para:
 * - Control de reproducción (Play, Pause, Stop).
 * - Gestión de archivos (Crear, Borrar, Importar).
 * - Edición destructiva (Recortar, Cortar, Pegar).
 * - Procesamiento de señales (DSP) y Efectos.
 * - Gestión de estado (Volumen, Mute, Solo).
 */
interface AudioMixerRepository {
    /**
     * Reproduce las pistas.
     */
    fun play(excludeTrackId: Long? = null)

    /**
     * Pausa la reproducción de las pistas.
     */
    fun pause()

    /**
     * Para la reproducción de las pistas.
     */
    fun stop()

    /**
     * Crea una nueva pista.
     */
    fun createTrack(sourceType: AudioSourceType)

    /**
     * Crea una nueva pista a partir de un archivo de audio existente.
     * @param sourceFilePath Ruta del archivo de audio de origen.
     */
    suspend fun createTrackFromFile(sourceFilePath: String): Result<Unit>


    /**
     * Elimina una pista.
     * @param id Id de la pista a eliminar.
     */
    fun removeTrack(id: Long)

    /**
     * Recorta una pista de audio.
     * @param id Id de la pista a recortar.
     * @param startMs El tiempo de inicio del recorte en milisegundos.
     * @param endMs El tiempo de finalización del recorte en milisegundos.
     * @return Result<Unit> indicando el éxito o fallo de la operación.
     */
    fun trimTrack(id: Long, startMs: Long, endMs: Long): Result<Unit>

    /**
     * Deshace el último recorte realizado en una pista, restaurando el archivo original.
     * @param id Id de la pista.
     * @return Result<Unit> indicando el éxito o fallo de la operación.
     */
    fun undoTrim(id: Long): Result<Unit>

    /**
     * Obtiene las pistas actuales.
     */
    fun getTracks(): StateFlow<List<Track>>


    /**
     * Verifica si todas las pistas han sido reproducidas.
     */
    suspend fun allTracksWerePlayed(): StateFlow<Boolean>

    /**
     * Mutea una pista.
     * @param id Id de la pista a mutear.
     */
    fun muteTrack(id: Long)

    /**
     * Desmutea una pista.
     * @param id Id de la pista a desmutear.
     */
    fun unMuteTrack(id: Long)

    /**
     * Verifica si una pista está muteada.
     * @param id Id de la pista.
     */
    fun setTrackVolume(id: Long, volume: Float)

    /**
    * Establece un desplazamiento de inicio para la pista.
    * @param id Id de la pista.
    * @param offsetMs El tiempo de desplazamiento en milisegundos.
    */
    fun setTrackOffset(id: Long, offsetMs: Long)

    /**
     * Obtiene la posición de reproducción actual en milisegundos.
     */
    suspend fun getCurrentPlaybackPosition(): StateFlow<Long>

    /**
     * Mueve el cabezal de reproducción a una posición específica.
     * @param ms Posición en milisegundos.
     */
    fun seekTo(ms: Long)

    /**
    * Define un rango de reproducción específico dentro del archivo de audio de la pista (Loop o región).
    *
    * @param trackId Id de la pista.
    * @param startMs Inicio de la región en ms.
    * @param endMs Fin de la región en ms.
    * @param totalDurationMs Duración total para validaciones.
    */
    fun setPlaybackRange(trackId: Long, startMs: Long, endMs: Long, totalDurationMs: Long): Result<Unit>

    /**
     * Carga una pista PCM existente desde el disco (usado al restaurar proyectos guardados).
     */
    suspend fun loadPcmTrack(file: File, id: Long, sourceType: AudioSourceType, startOffsetMs: Long)

    /**
     * Elimina todas las pistas de la memoria (limpieza de sesión).
     */
    fun clearAllTracks()

    // --- PREVIEW DE EDICIÓN (Trimming) ---

    /**
     * Reproduce un archivo específico para previsualizar un recorte o edición.
     * NO aplica efectos en tiempo real, solo reproduce audio crudo.
     */
    fun playPreview(filePath: String): Result<Unit>

    /**
     * Detiene la previsualización de edición.
     */
    fun stopPreview()

    /**
     * Flujo que notifica cuando la previsualización de edición ha terminado.
     */
    fun onPreviewCompleted(): SharedFlow<Unit>

    // --- EDICIÓN AVANZADA (Cortar/Pegar) ---

    /**
     * Corta un segmento de audio (lo elimina del archivo) y debería copiarlo al portapapeles (lógica externa).
     * @param id Id de la pista.
     * @param startMs Inicio del corte.
     * @param endMs Fin del corte.
     */
    fun cutAudioSegment(id: Long, startMs: Long, endMs: Long): Result<Unit>

    /**
     * Inserta un segmento de audio (pegado) en una nueva pista.
     * @param sourcePath Ruta del archivo fuente (el "portapapeles").
     * @param startMs Inicio del segmento a pegar.
     * @param endMs Fin del segmento a pegar.
     */
    suspend fun addTrackFromSegment(sourcePath: String, startMs: Long, endMs: Long): Result<Unit>

    // --- PROCESAMIENTO DE EFECTOS (Offline/Destructivo) ---

    /**
     * Aplica un efecto de Delay (Eco) al archivo físico de la pista.
     * Genera backup `.original_effect`.
     *
     * @param delayTimeInSeconds Tiempo entre repeticiones en SEGUNDOS.
     * @param decay Factor de decaimiento (0.0 a 1.0).
     */
    suspend fun applyDelayEffect(trackId: Long, delayTimeInSeconds: Float, decay: Float): Result<Unit>

    /**
     * Aplica un filtro Pasa-Altos (High Pass) eliminando frecuencias graves.
     * Genera backup `.original_effect`.
     * @param frequency Frecuencia de corte en Hz.
     */
    suspend fun applyHighPassFilter(trackId: Long, frequency: Float): Result<Unit>

    /**
     * Aplica un efecto Flanger.
     * Genera backup `.original_effect`.
     */
    suspend fun applyFlangerEffect(trackId: Long, rate: Float, wet: Float): Result<Unit>

    /**
     * Deshace el último efecto aplicado restaurando el archivo desde `.original_effect`.
     */
    fun undoEffect(id: Long): Result<Unit>

    // --- PREVIEW DE EFECTOS (Tiempo Real) ---

    /**
     * Inicia una preescucha en tiempo real aplicando un efecto DSP sin modificar el archivo en disco.
     * Utiliza TarsosDSP Streaming.
     *
     * @param trackId ID de la pista a procesar.
     * @param config Configuración del efecto (Parámetros del delay, filtro, etc).
     */
    fun startEffectPreview(trackId: Long, config: EffectConfig)

    /**
     * Detiene el flujo de preescucha de efectos.
     */
    fun stopEffectPreview()

    /**
     * Indica si actualmente se está ejecutando una preescucha de efectos.
     */
    fun isPreviewActive(): Boolean

    suspend fun applyLowPassFilter(trackId: Long, frequency: Float): Result<Unit>
    suspend fun applyFadeIn(trackId: Long, durationSeconds: Float): Result<Unit>

    suspend fun normalizeTrack(trackId: Long): Result<Unit>

    suspend fun applyTelephoneEffect(trackId: Long): Result<Unit>

    suspend fun applyFadeOut(trackId: Long, durationSeconds: Float): Result<Unit>

    suspend fun applyDistortion(trackId: Long, drive: Float): Result<Unit>
    suspend fun applyTremolo(trackId: Long, frequency: Float, depth: Float): Result<Unit>

}
