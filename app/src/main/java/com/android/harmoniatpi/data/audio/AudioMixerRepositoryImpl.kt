package com.android.harmoniatpi.data.audio

import android.content.Context
import android.media.AudioTrack
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.effects.DelayEffect
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory

import be.tarsos.dsp.io.jvm.JVMAudioInputStream
import com.android.harmoniatpi.data.audio.player.PcmAudioPlayer
import com.android.harmoniatpi.data.audio.record.TARSOS_FORMAT
import com.android.harmoniatpi.data.audio.util.AudioConverter
import com.android.harmoniatpi.di.TrackFactory
import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.domain.model.audio.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Arrays
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.math.roundToLong

/**
 * Maneja las pistas creadas y se encarga de reproducirlas, pausarlas y pararlas.
 */
class AudioMixerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackFactory: TrackFactory,
    private val audioConverter: AudioConverter,
    private val pcmAudioPlayerProvider: javax.inject.Provider<PcmAudioPlayer>
) : AudioMixerRepository {
    /**
     * Lista de pistas disponibles
     */
    private val tracks = MutableStateFlow<List<Track>>(emptyList())

    /**
     * Estado observable para saber cuándo se terminaron de reproducir todas las pistas,
     * por más que difieran en duración
     */
    private val tracksCompleted = MutableStateFlow(false)

    /**
     * Contador de pistas que se han completado. El uso de AtomicInteger es seguro en hilos.
     */
    private val completedCount = AtomicInteger(0)

    private val currentPlaybackMs = MutableStateFlow(0L)
    private var masterPlaybackJob: Job? = null
    private val playerList: List<PcmAudioPlayer>
        get() = tracks.value.map { it.player as PcmAudioPlayer }

    private var previewPlayer: PcmAudioPlayer? = null
    private val _previewCompletedFlow = MutableSharedFlow<Unit>(replay = 0)
    private val bufferSize = 2048


    override fun play(excludeTrackId: Long?) {

        masterPlaybackJob?.cancel()
        val tracksToPlay =
            tracks.value.filter { it.hasAudio() && it.id != excludeTrackId && !it.isMuted() }

        if (tracksToPlay.isEmpty()) {
            if (tracks.value.any { it.hasAudio() }) {
                startPlaybackTracking()
            }
            tracksCompleted.value = true
            return
        }

        val globalStartMs = currentPlaybackMs.value
        completedCount.set(0)
        tracksCompleted.value = false
        masterPlaybackJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            startPlaybackTracking()

            tracksToPlay.forEach { track ->
                launch {
                    val delayMs = (track.startOffsetMs - globalStartMs).coerceAtLeast(0L)
                    val internalPlayPos = (globalStartMs - track.startOffsetMs).coerceAtLeast(0L)

                    if (delayMs > 0) {
                        delay(delayMs)
                    }

                    if (isActive) {
                        track.setOnPlaybackCompletedCallback {
                            val count = completedCount.incrementAndGet()
                            if (count >= tracksToPlay.size) {
                                stop()
                            }
                        }
                        track.play(internalPlayPos)
                    }
                }
            }
        }
    }

    override fun pause() {
        masterPlaybackJob?.cancel()
        stopPlaybackTracking()
        tracks.value.forEach { it.pause() }
    }

    override fun stop() {
        masterPlaybackJob?.cancel()
        stopPlaybackTracking()
        currentPlaybackMs.value = 0L
        tracks.value.forEach { it.stop() }
        tracksCompleted.value = true
    }

    override fun createTrack(sourceType: AudioSourceType) {
        val id = System.currentTimeMillis()
        val file = File(context.filesDir, "$id.pcm")

        // 🔹 Crea el archivo físico vacío
        if (!file.exists()) {
            file.createNewFile()
        }

        val track = trackFactory.create(
            folderPath = context.filesDir.absolutePath,
            existingFilePath = file.absolutePath,
            idExist = id,
            sourceType = sourceType
        )

        tracks.update { it + track }

        Log.i("AudioMixerRepository", "🎶 Nueva pista creada: ${file.absolutePath}")
        Log.i("AudioMixerRepository", "🎶 Nueva pista creada: ${tracks.value}")
    }

    override fun removeTrack(id: Long) {
        tracks.value.find { it.id == id }?.let { track ->
            track.delete()
            tracks.update { it -> it.filterNot { track -> track.id == id } }
        }
    }


    private var playbackTrackingJob: Job? = null
    private fun startPlaybackTracking() {
        playbackTrackingJob?.cancel()
        playbackTrackingJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val activePlayers =
                    playerList.filter { it.audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING }
                if (activePlayers.isNotEmpty()) {

                    val maxPos = activePlayers.maxOfOrNull { player ->
                        val track = tracks.value.find { it.player == player }
                        (track?.startOffsetMs ?: 0L) + player.getCurrentPositionMs()
                    } ?: currentPlaybackMs.value
                    currentPlaybackMs.value = maxPos
                }
                delay(50)
            }
        }
    }

    private fun stopPlaybackTracking() {
        playbackTrackingJob?.cancel()
    }

    override suspend fun getCurrentPlaybackPosition(): StateFlow<Long> =
        currentPlaybackMs.asStateFlow()

    override fun seekTo(ms: Long) {
        val wasPlaying =
            playerList.any { it.audioTrack.playState == android.media.AudioTrack.PLAYSTATE_PLAYING }

        stopPlaybackTracking()
        stop()

        currentPlaybackMs.value = ms.coerceAtLeast(0L)

        if (wasPlaying) {
            play()
        }
    }

    override fun setTrackOffset(id: Long, offsetMs: Long) {
        tracks.update { current ->
            current.map { track ->
                if (track.id == id) {
                    track.apply { startOffsetMs = offsetMs.coerceAtLeast(0L) }
                } else {
                    track
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun createTrackFromFile(sourceFilePath: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val sourceFile = File(sourceFilePath)
            if (!sourceFile.exists()) {
                return@withContext Result.failure(FileNotFoundException("Archivo de origen temporal no encontrado."))
            }

            val track = trackFactory.create(
                folderPath = context.filesDir.absolutePath,
                sourceType = AudioSourceType.INSTRUMENT
            )
            val destinationFile = File(track.path)

            try {
                val inputUri = Uri.fromFile(sourceFile)
                audioConverter.convertToPcm(inputUri, destinationFile)
                    .onFailure { error ->
                        sourceFile.delete()
                        return@withContext Result.failure(error)
                    }
                tracks.update { it + track }
                sourceFile.delete()
                return@withContext Result.success(Unit)
            } catch (e: Exception) {
                Log.e("AudioMixer", "Error importando/convirtiendo pista: ${e.message}", e)
                destinationFile.delete()
                sourceFile.delete()
                return@withContext Result.failure(e)
            }
        }


    override fun trimTrack(id: Long, startMs: Long, endMs: Long): Result<Unit> {
        return tracks.value.find { it.id == id }?.let { track ->
            val originalFile = File(track.path)
            val backupFile = File(track.path + ".original_trim")

            if (!originalFile.exists()) {
                return Result.failure(Exception("Archivo de audio original no encontrado: ${track.path}"))
            }

            try {

                if (backupFile.exists()) {
                    backupFile.delete()
                }

                val effectBackupFile = File(track.path + ".original_effect")
                if (effectBackupFile.exists()) {
                    effectBackupFile.delete()
                    Log.d(TAG, "Backup de efecto eliminado al hacer trim.")
                }

                if (!originalFile.renameTo(backupFile)) {
                    return Result.failure(IOException("No se pudo crear el backup para deshacer."))
                }

                val sampleRate = 44100
                val bytesPerSample = 2 // pcm de 16 bit

                val startSamples = (startMs * sampleRate / 1000f).roundToLong()
                val endSamples = (endMs * sampleRate / 1000f).roundToLong()

                val startByte = startSamples * bytesPerSample
                val endByte = endSamples * bytesPerSample

                val length = backupFile.length()
                if (startByte < 0 || endByte > length || startByte >= endByte) {

                    backupFile.renameTo(originalFile)
                    return Result.failure(IllegalArgumentException("Rango de recorte inválido."))
                }

                val tempFile = File(track.path.replace(".pcm", "_temp.pcm"))
                val bufferSize = 8192

                FileInputStream(backupFile).use { fis ->
                    FileOutputStream(tempFile).use { fos ->
                        fis.skip(startByte)
                        val bytesToRead = endByte - startByte
                        var totalRead = 0L
                        val buffer = ByteArray(bufferSize)

                        while (totalRead < bytesToRead) {
                            val remaining =
                                (bytesToRead - totalRead).toInt().coerceAtMost(bufferSize)
                            val readCount = fis.read(buffer, 0, remaining)
                            if (readCount <= 0) break
                            fos.write(buffer, 0, readCount)
                            totalRead += readCount
                        }
                    }
                }

                if (tempFile.renameTo(originalFile)) {
                    return Result.success(Unit)
                } else {
                    backupFile.renameTo(originalFile)
                    return Result.failure(IOException("Fallo al aplicar el recorte: no se pudo renombrar el archivo temporal."))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error recortando pista ${track.id}", e)
                if (backupFile.exists() && !originalFile.exists()) {
                    backupFile.renameTo(originalFile)
                }
                return Result.failure(e)
            }
        } ?: Result.failure(NoSuchElementException("Pista con ID $id no encontrada"))
    }


    override fun undoTrim(id: Long): Result<Unit> {
        return tracks.value.find { it.id == id }?.let { track ->
            val currentFile = File(track.path)
            val backupFile = File(track.path + ".original_trim")

            if (!backupFile.exists()) {
                Log.w(
                    TAG,
                    "No hay copia de seguridad para deshacer el recorte en la pista ${track.id}"
                )
                return Result.failure(FileNotFoundException("No hay copia de seguridad disponible."))
            }

            try {
                if (currentFile.exists()) {
                    currentFile.delete()
                }

                // restauro el original a partir de un rename
                if (backupFile.renameTo(currentFile)) {
                    Log.i(TAG, "Recorte deshecho exitosamente para la pista ${track.id}")
                    return Result.success(Unit)
                } else {
                    Log.e(TAG, "Fallo al renombrar el backup como archivo actual.")
                    return Result.failure(IOException("Fallo al deshacer el recorte."))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error al deshacer el recorte para la pista ${track.id}", e)
                return Result.failure(e)
            }

        } ?: Result.failure(NoSuchElementException("Pista con ID $id no encontrada"))
    }


    override fun getTracks(): StateFlow<List<Track>> = tracks.asStateFlow()
    override suspend fun allTracksWerePlayed(): StateFlow<Boolean> = tracksCompleted.asStateFlow()

    override fun muteTrack(id: Long) {
        tracks.update { current ->
            current.map { track ->
                if (track.id == id) {
                    track.mute()
                }
                track
            }
        }
    }

    override fun unMuteTrack(id: Long) {
        tracks.update { current ->
            current.map { track ->
                if (track.id == id) {
                    track.unMute()
                }
                track
            }
        }
    }

    override fun setTrackVolume(id: Long, volume: Float) {
        tracks.update { current ->
            current.map { track ->
                if (track.id == id) {
                    track.setVolume(volume)
                }
                track
            }
        }
    }

    override suspend fun loadPcmTrack(
        file: File,
        id: Long,
        sourceType: AudioSourceType,
        startOffsetMs: Long
    ) {
        if (!file.exists()) {
            Log.e(TAG, "PCM file not found: ${file.absolutePath}")
            return
        }

        val track =
            trackFactory.create(file.parent!!, file.absolutePath, id, sourceType = sourceType)
        track.startOffsetMs = startOffsetMs

        tracks.update { it + track }
        Log.i(TAG, "Track restored from PCM: ${file.name} with path ${track.path}")
    }


    override fun playPreview(filePath: String): Result<Unit> {
        stopPreviewInternal()
        if (masterPlaybackJob?.isActive == true || tracks.value.any { (it.player as PcmAudioPlayer).audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING }) {
            pause()
        }

        return try {
            Log.d(TAG, "Iniciando playPreview para: $filePath")
            val player = pcmAudioPlayerProvider.get()
            previewPlayer = player

            player.setFile(filePath)

            val internalFile = player.file
            if (internalFile == null || !internalFile.exists() || internalFile.length() == 0L) {
                Log.e(
                    TAG,
                    "playPreview - ¡ERROR POST-SETFILE! El archivo interno es null, no existe o está vacío."
                )
                Log.e(TAG, "playPreview - Path intentado: $filePath")

                throw FileNotFoundException("El archivo de audio ($filePath) no se pudo establecer o es inválido en el reproductor.")
            } else {
                Log.d(
                    TAG,
                    "playPreview - Verificación post-setFile OK: (${internalFile.path}, Tamaño: ${internalFile.length()})"
                )
            }

            player.setOnPlaybackCompletedCallback {
                Log.d(TAG, "Preview completado para: $filePath")
                stopPreviewInternal()
                CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
                    _previewCompletedFlow.emit(Unit)
                }
            }
            val playResult = player.play()

            if (playResult.isSuccess) {
                Log.d(TAG, "Preview iniciado exitosamente.")
                Result.success(Unit)
            } else {

                val exception = playResult.exceptionOrNull()
                    ?: IllegalStateException("Unknown error during player.play()")
                Log.e(TAG, "Fallo al iniciar previewPlayer.play()", exception)
                stopPreviewInternal()
                Result.failure(exception)
            }


        } catch (e: Exception) {
            Log.e(TAG, "Error en playPreview (catch block)", e)
            stopPreviewInternal()
            Result.failure(e)
        }
    }

    override fun stopPreview() {
        stopPreviewInternal()
    }

    private fun stopPreviewInternal() {
        previewPlayer?.let {
            Log.d(TAG, "Deteniendo y liberando preview player...")
            it.stop()
            it.release()
            previewPlayer = null
            Log.d(TAG, "Preview player liberado.")
        }
    }

    override fun setPlaybackRange(
        trackId: Long,
        startMs: Long,
        endMs: Long,
        totalDurationMs: Long
    ): Result<Unit> {
        return runCatching {
            tracks.value.find { it.id == trackId }?.let { track ->
                track.setPlaybackRange(startMs, endMs, totalDurationMs)
            } ?: throw NoSuchElementException("Track no encontrado: $trackId")
        }
    }

    override fun onPreviewCompleted(): SharedFlow<Unit> = _previewCompletedFlow.asSharedFlow()


    override fun cutAudioSegment(id: Long, startMs: Long, endMs: Long): Result<Unit> {
        return tracks.value.find { it.id == id }?.let { track ->
            val originalFile = File(track.path)
            val backupFile = File(track.originalPath)

            if (!originalFile.exists()) {
                return Result.failure(Exception("Archivo de audio original no encontrado: ${track.path}"))
            }

            try {

                if (backupFile.exists()) {
                    backupFile.delete()
                }
                if (!originalFile.renameTo(backupFile)) {
                    return Result.failure(IOException("No se pudo crear el backup."))
                }


                val sampleRate = TARSOS_FORMAT.sampleRate.toInt()
                val bytesPerSample = TARSOS_FORMAT.frameSize
                val startByte = (startMs * sampleRate / 1000f).roundToLong() * bytesPerSample
                val endByte = (endMs * sampleRate / 1000f).roundToLong() * bytesPerSample
                val totalLength = backupFile.length()

                if (startByte < 0 || endByte > totalLength || startByte >= endByte) {
                    backupFile.renameTo(originalFile)
                    return Result.failure(IllegalArgumentException("Rango de corte inválido."))
                }

                val tempFile = File(track.path.replace(".pcm", "_tempcut.pcm"))
                val bufferSize = 8192
                val buffer = ByteArray(bufferSize)

                FileInputStream(backupFile).use { fis ->
                    FileOutputStream(tempFile).use { fos ->
                        var totalRead = 0L
                        while (totalRead < startByte) {
                            val remaining = (startByte - totalRead).toInt().coerceAtMost(bufferSize)
                            val readCount = fis.read(buffer, 0, remaining)
                            if (readCount <= 0) break
                            fos.write(buffer, 0, readCount)
                            totalRead += readCount
                        }

                        val bytesToSkip = endByte - startByte
                        var skipped = 0L
                        while (skipped < bytesToSkip) {
                            val s = fis.skip(bytesToSkip - skipped)
                            if (s <= 0) break
                            skipped += s
                        }

                        var readCount = fis.read(buffer, 0, bufferSize)
                        while (readCount > 0) {
                            fos.write(buffer, 0, readCount)
                            readCount = fis.read(buffer, 0, bufferSize)
                        }
                    }
                }

                if (tempFile.renameTo(originalFile)) {
                    return Result.success(Unit)
                } else {
                    backupFile.renameTo(originalFile)
                    return Result.failure(IOException("Fallo al aplicar el corte."))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error cortando pista ${track.id}", e)
                if (backupFile.exists() && !originalFile.exists()) {
                    backupFile.renameTo(originalFile)
                }
                return Result.failure(e)
            }
        } ?: Result.failure(NoSuchElementException("Pista con ID $id no encontrada"))
    }

    override suspend fun addTrackFromSegment(
        sourcePath: String,
        startMs: Long,
        endMs: Long
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {

            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) {
                throw FileNotFoundException("Archivo de origen no encontrado: $sourcePath")
            }

            val newTrack = trackFactory.create(
                folderPath = context.filesDir.absolutePath,
                sourceType = AudioSourceType.INSTRUMENT
            )
            val destinationFile = File(newTrack.path)

            val sampleRate = TARSOS_FORMAT.sampleRate.toInt()
            val bytesPerSample = TARSOS_FORMAT.frameSize
            val startByte = (startMs * sampleRate / 1000f).roundToLong() * bytesPerSample
            val endByte = (endMs * sampleRate / 1000f).roundToLong() * bytesPerSample
            val length = sourceFile.length()

            if (startByte < 0 || endByte > length || startByte >= endByte) {
                destinationFile.delete()
                throw IllegalArgumentException("Rango de copiado inválido.")
            }

            val bufferSize = 8192
            FileInputStream(sourceFile).use { fis ->
                FileOutputStream(destinationFile).use { fos ->
                    fis.skip(startByte)
                    val bytesToRead = endByte - startByte
                    var totalRead = 0L
                    val buffer = ByteArray(bufferSize)

                    while (totalRead < bytesToRead) {
                        val remaining = (bytesToRead - totalRead).toInt().coerceAtMost(bufferSize)
                        val readCount = fis.read(buffer, 0, remaining)
                        if (readCount <= 0) break
                        fos.write(buffer, 0, readCount)
                        totalRead += readCount
                    }
                }
            }

            tracks.update { it + newTrack }
            Log.i(TAG, "Track pegado exitosamente en: ${newTrack.path}")


            Unit

        }.onFailure {

            Log.e(TAG, "Error en addTrackFromSegment", it)
        }

    }


    override suspend fun applyDelayEffect(
        trackId: Long,
        delayTimeInSeconds: Float,
        decay: Float
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val track = tracks.value.find { it.id == trackId }
                ?: throw IllegalStateException("Track no encontrado con id: $trackId")

            val originalFile = File(track.path)
            if (!originalFile.exists()) {
                throw IllegalStateException("Archivo de pista no encontrado: ${track.path}")
            }

            val backupFile = File(track.path + ".original_effect")
            if (backupFile.exists()) {
                backupFile.delete()
            }

            val trimBackupFile = File(track.path + ".original_trim")
            if (trimBackupFile.exists()) {
                trimBackupFile.delete()
                Log.d(TAG, "Backup de trim eliminado al aplicar efecto.")
            }

            originalFile.renameTo(backupFile)

            val tempFile = File(track.path + ".tmp")
            var fileInputStream: FileInputStream? = null
            var fileOutputStream: FileOutputStream? = null

            try {
                fileInputStream = FileInputStream(backupFile)
                fileOutputStream = FileOutputStream(tempFile)

                val delayEffect = DelayEffect(
                    delayTimeInSeconds.toDouble(),
                    decay.toDouble(),
                    TARSOS_FORMAT.sampleRate.toDouble()
                )

                val converter = TarsosDSPAudioFloatConverter.getConverter(TARSOS_FORMAT)
                val byteBuffer = ByteArray(2048)
                val floatBuffer = FloatArray(1024)
                val audioEvent = AudioEvent(TARSOS_FORMAT)
                audioEvent.setFloatBuffer(floatBuffer)

                var bytesRead = fileInputStream.read(byteBuffer, 0, byteBuffer.size)
                while (bytesRead != -1) {
                    audioEvent.setBytesProcessing(bytesRead)
                    converter.toFloatArray(byteBuffer, floatBuffer)
                    delayEffect.process(audioEvent)
                    converter.toByteArray(floatBuffer, byteBuffer)
                    fileOutputStream.write(byteBuffer, 0, bytesRead)
                    bytesRead = fileInputStream.read(byteBuffer, 0, byteBuffer.size)
                }

                Arrays.fill(floatBuffer, 0.0f)
                audioEvent.setBytesProcessing(byteBuffer.size)
                delayEffect.process(audioEvent)

                var rms = AudioEvent.calculateRMS(floatBuffer)
                var tailLoops = 0
                val maxTailLoops = (delayTimeInSeconds * 2 * TARSOS_FORMAT.sampleRate) / 1024

                while (rms > 0.0001 && tailLoops < maxTailLoops) {
                    converter.toByteArray(floatBuffer, byteBuffer)
                    fileOutputStream.write(byteBuffer, 0, byteBuffer.size)

                    Arrays.fill(floatBuffer, 0.0f)

                    delayEffect.process(audioEvent)

                    rms = AudioEvent.calculateRMS(floatBuffer)
                    tailLoops++
                }
                delayEffect.processingFinished()
                tempFile.renameTo(originalFile)

            } finally {
                fileInputStream?.close()
                fileOutputStream?.close()
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }

            Unit
        }
    }

    override fun undoEffect(id: Long): Result<Unit> {
        return tracks.value.find { it.id == id }?.let { track ->
            val currentFile = File(track.path)
            val backupFile = File(track.path + ".original_effect")

            if (!backupFile.exists()) {
                Log.w(
                    TAG,
                    "No hay copia de seguridad para deshacer el efecto en la pista ${track.id}"
                )
                return Result.failure(FileNotFoundException("No hay copia de seguridad de efecto disponible."))
            }
            try {
                if (currentFile.exists()) {
                    currentFile.delete()
                }
                if (backupFile.renameTo(currentFile)) {
                    Log.i(TAG, "Efecto deshecho exitosamente para la pista ${track.id}")
                    return Result.success(Unit)
                } else {
                    Log.e(TAG, "Fallo al renombrar el backup de efecto como archivo actual.")
                    return Result.failure(IOException("Fallo al deshacer el efecto."))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al deshacer el efecto para la pista ${track.id}", e)
                return Result.failure(e)
            }
        } ?: Result.failure(NoSuchElementException("Pista con ID $id no encontrada"))
    }


    override fun clearAllTracks() {
        tracks.update { emptyList() }
        Log.i("AudioMixerRepository", "🧹 Tracks limpiados del repositorio")
    }

    private companion object {
        const val TAG = "AudioMixerRepository"
    }
}