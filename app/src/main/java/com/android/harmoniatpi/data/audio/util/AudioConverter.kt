package com.android.harmoniatpi.data.audio.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioConverter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "AudioConverter"
    private val TARGET_SAMPLE_RATE = 44100
    private val TARGET_CHANNEL_COUNT = 1 // Mono
    private val BYTES_PER_SAMPLE = 2 // 16-bit PCM

    private val MAX_CHUNK_SIZE = 64 * 1024
    private val reusableRawChunk = ByteArray(MAX_CHUNK_SIZE)
    private val reusableMonoChunk = ByteArray(MAX_CHUNK_SIZE / 2)
    // ----------------------------------------------------
/*
    /**
     * Convierte el audio desde la URI de origen (ej. MP3, WAV) a PCM y lo guarda en el archivo de destino.
     *
     * @param inputUri URI del archivo de origen (ej. mp3).
     * @param outputPcmFile Archivo de destino donde se guardará el PCM.
     * @return Result<Unit>
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun convertToPcm(inputUri: Uri, outputPcmFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        var decoder: MediaCodec? = null
        var outputStream: BufferedOutputStream? = null

        try {
            outputStream = BufferedOutputStream(FileOutputStream(outputPcmFile), 64 * 1024)
            context.contentResolver.openFileDescriptor(inputUri, "r")?.use { pfd ->
                extractor.setDataSource(pfd.fileDescriptor)
            } ?: return@withContext Result.failure(Exception("No se pudo abrir el archivo de origen."))

            var trackIndex = -1
            var inputFormat: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                if (format.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                    trackIndex = i
                    inputFormat = format
                    break
                }
            }
            if (trackIndex == -1 || inputFormat == null) {
                return@withContext Result.failure(Exception("No se encontró una pista de audio válida."))
            }

            extractor.selectTrack(trackIndex)
            val mime = inputFormat.getString(MediaFormat.KEY_MIME) ?: return@withContext Result.failure(Exception("Formato MIME no encontrado."))

            decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(inputFormat, null, null, 0)
            decoder.start()

            val bufferInfo = MediaCodec.BufferInfo()
            var isExtractorEOS = false
            var isDecoderEOS = false

            val timeoutUs = 10000L
            var outputSampleRate = 0
            var outputChannelCount = 0

            while (!isDecoderEOS) {

                if (!isExtractorEOS) {
                    val inputBufferId = decoder.dequeueInputBuffer(timeoutUs)
                    if (inputBufferId >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputBufferId)!!
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)

                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isExtractorEOS = true
                        } else {
                            decoder.queueInputBuffer(inputBufferId, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outputBufferId = decoder.dequeueOutputBuffer(bufferInfo, timeoutUs)
                if (outputBufferId >= 0) {
                    val outputBuffer = decoder.getOutputBuffer(outputBufferId)!!
                    val chunkSize = bufferInfo.size

                    if (chunkSize > 0) {

                        outputBuffer.get(reusableRawChunk, 0, chunkSize)

                        val bytesToWrite: ByteArray
                        val sizeToWrite: Int

                        if (outputChannelCount == 2) {
                            val monoSize = mixStereoToMono(chunkSize)
                            bytesToWrite = reusableMonoChunk
                            sizeToWrite = monoSize
                        } else {
                            bytesToWrite = reusableRawChunk
                            sizeToWrite = chunkSize
                        }

                        outputStream.write(bytesToWrite, 0, sizeToWrite)
                    }

                    decoder.releaseOutputBuffer(outputBufferId, false)

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isDecoderEOS = true
                    }
                } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val newFormat = decoder.outputFormat
                    outputSampleRate = newFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    outputChannelCount = newFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

                    if (outputSampleRate != TARGET_SAMPLE_RATE) {
                        Log.w(TAG, "ADVERTENCIA: Sample Rate de salida ($outputSampleRate Hz) es diferente al objetivo ($TARGET_SAMPLE_RATE Hz). Esto causará velocidad incorrecta.")
                    }
                    if (outputChannelCount > TARGET_CHANNEL_COUNT) {
                        Log.i(TAG, "Se detectó audio estéreo ($outputChannelCount canales). Se realizará la mezcla a Mono.")
                    }
                }
            }
            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error en la conversión de audio: ${e.message}", e)
            return@withContext Result.failure(Exception("Fallo en la conversión del archivo de audio a PCM. ${e.localizedMessage}"))
        } finally {
            extractor.release()
            decoder?.stop()
            decoder?.release()
            outputStream?.close()
        }
    }

    /**
     * Convierte un chunk de audio PCM de 16 bits Estéreo a Mono.
     *
     * @param stereoSize La cantidad de bytes válidos en el `reusableRawChunk`.
     * @return La cantidad de bytes válidos escritos en el `reusableMonoChunk`.
     */
    private fun mixStereoToMono(stereoSize: Int): Int {
        val monoSize = stereoSize / 2
        var monoIndex = 0

        // Procesa 4 bytes a la vez (Short Izquierdo + Short Derecho)
        var stereoIndex = 0
        while (stereoIndex < stereoSize) {
            // Combina los bytes a un Short (Little Endian)
            val left = (reusableRawChunk[stereoIndex].toInt() and 0xFF) or (reusableRawChunk[stereoIndex + 1].toInt() shl 8)
            val right = (reusableRawChunk[stereoIndex + 2].toInt() and 0xFF) or (reusableRawChunk[stereoIndex + 3].toInt() shl 8)

            // Realiza el promedio
            val monoSample = (left + right) / 2

            // Descompone el Short de vuelta a bytes (Little Endian) en el buffer mono
            reusableMonoChunk[monoIndex] = (monoSample and 0xFF).toByte()
            reusableMonoChunk[monoIndex + 1] = (monoSample shr 8 and 0xFF).toByte()

            stereoIndex += 4
            monoIndex += 2
        }
        return monoSize
    }

 */

    /**
     * Convierte el audio desde la URI de origen a PCM de forma paralela usando un patrón Productor-Consumidor.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun convertToPcm(inputUri: Uri, outputPcmFile: File): Result<Unit> = runBlocking(Dispatchers.IO) {
        val extractor = MediaExtractor()
        var decoder: MediaCodec? = null

        try {
            // Preparamos el extractor y el decodificador
            val (d, e, format) = setupMediaComponents(inputUri, extractor)
            decoder = d
            val inputFormat = format

            // Creamos un Channel para comunicar los chunks de PCM decodificados
            // desde el productor (decoder) al consumidor (writer).
            val pcmChannel = Channel<Pair<ByteArray, Int>>(Channel.BUFFERED)

            // 1. Inicia el CONSUMIDOR (escritor/mezclador) en una coroutine.
            //    'async' nos permite esperar a que termine y capturar cualquier excepción.
            val consumerJob = async {
                consumeAndWritePcm(pcmChannel, outputPcmFile, inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT))
            }

            // 2. Inicia el PRODUCTOR (decodificador) en otra coroutine.
            launch {
                producePcmChunks(decoder, extractor, pcmChannel)
            }

            // 3. Espera a que el consumidor termine su trabajo. Si hubo una excepción, se lanzará aquí.
            consumerJob.await()

            return@runBlocking Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error en la conversión paralela de audio: ${e.message}", e)
            return@runBlocking Result.failure(e)
        } finally {
            // Limpieza final
            extractor.release()
            decoder?.stop()
            decoder?.release()
        }
    }

    /**
     * PRODUCTOR: Decodifica el audio y envía los chunks de PCM a través del canal.
     */
    private suspend fun producePcmChunks(decoder: MediaCodec, extractor: MediaExtractor, channel: Channel<Pair<ByteArray, Int>>) {
        try {
            val bufferInfo = MediaCodec.BufferInfo()
            var isExtractorEOS = false
            var isDecoderEOS = false
            val timeoutUs = 10000L

            while (!isDecoderEOS) {
                // Alimentar al decodificador
                if (!isExtractorEOS) {
                    val inputBufferId = decoder.dequeueInputBuffer(timeoutUs)
                    if (inputBufferId >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputBufferId)!!
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isExtractorEOS = true
                        } else {
                            decoder.queueInputBuffer(inputBufferId, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                // Obtener datos decodificados
                val outputBufferId = decoder.dequeueOutputBuffer(bufferInfo, timeoutUs)
                if (outputBufferId >= 0) {
                    if (bufferInfo.size > 0) {
                        val outputBuffer = decoder.getOutputBuffer(outputBufferId)!!
                        val chunk = ByteArray(bufferInfo.size)
                        outputBuffer.get(chunk)

                        // Envía el chunk al consumidor
                        channel.send(chunk to bufferInfo.size)
                    }

                    decoder.releaseOutputBuffer(outputBufferId, false)

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isDecoderEOS = true
                    }
                }
            }
        } finally {
            // Cierra el canal para señalar al consumidor que no hay más datos.
            channel.close()
        }
    }

    /**
     * CONSUMIDOR: Recibe chunks de PCM, los mezcla a mono si es necesario, y los escribe en el archivo.
     */
    private suspend fun consumeAndWritePcm(channel: Channel<Pair<ByteArray, Int>>, outputPcmFile: File, sourceChannelCount: Int) {
        BufferedOutputStream(FileOutputStream(outputPcmFile), 64 * 1024).use { outputStream ->
            // Itera sobre el canal hasta que el productor lo cierre
            for ((chunk, size) in channel) {
                val bytesToWrite: ByteArray
                val sizeToWrite: Int

                if (sourceChannelCount == 2) {
                    // Copiamos el chunk recibido al buffer reutilizable para procesarlo
                    System.arraycopy(chunk, 0, reusableRawChunk, 0, size)
                    val monoSize = mixStereoToMono(size)
                    bytesToWrite = reusableMonoChunk
                    sizeToWrite = monoSize
                } else {
                    bytesToWrite = chunk
                    sizeToWrite = size
                }
                outputStream.write(bytesToWrite, 0, sizeToWrite)
            }
        }
    }

    // --- Funciones de ayuda y de lógica ---

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setupMediaComponents(inputUri: Uri, extractor: MediaExtractor): Triple<MediaCodec, MediaExtractor, MediaFormat> {
        context.contentResolver.openFileDescriptor(inputUri, "r")?.use { pfd ->
            extractor.setDataSource(pfd.fileDescriptor)
        } ?: throw IOException("No se pudo abrir el archivo de origen.")

        val trackIndex = (0 until extractor.trackCount).find {
            extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
        } ?: throw IllegalArgumentException("No se encontró una pista de audio válida.")

        val inputFormat = extractor.getTrackFormat(trackIndex)
        extractor.selectTrack(trackIndex)

        val mime = inputFormat.getString(MediaFormat.KEY_MIME) ?: throw IllegalArgumentException("Formato MIME no encontrado.")
        val decoder = MediaCodec.createDecoderByType(mime)
        decoder.configure(inputFormat, null, null, 0)
        decoder.start()

        // Validar sample rate y channel count (opcional, solo para logging)
        val outputFormat = decoder.outputFormat
        val outputSampleRate = outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val outputChannelCount = outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        if (outputSampleRate != TARGET_SAMPLE_RATE) {
            Log.w(TAG, "ADVERTENCIA: Sample Rate ($outputSampleRate Hz) es diferente al objetivo ($TARGET_SAMPLE_RATE Hz).")
        }
        if (outputChannelCount > TARGET_CHANNEL_COUNT) {
            Log.i(TAG, "Audio estéreo detectado. Se mezclará a Mono en el consumidor.")
        }

        return Triple(decoder, extractor, inputFormat)
    }

    private fun mixStereoToMono(stereoSize: Int): Int {
        val stereoBuffer = ByteBuffer.wrap(reusableRawChunk, 0, stereoSize).order(ByteOrder.LITTLE_ENDIAN)
        val monoBuffer = ByteBuffer.wrap(reusableMonoChunk).order(ByteOrder.LITTLE_ENDIAN)

        while (stereoBuffer.hasRemaining()) {
            val left = stereoBuffer.getShort().toInt()
            val right = stereoBuffer.getShort().toInt()
            val monoSample = (left + right) / 2
            monoBuffer.putShort(monoSample.toShort())
        }
        return monoBuffer.position()
    }
}
