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
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
        var outputStream: FileOutputStream? = null

        try {
            outputStream = FileOutputStream(outputPcmFile)
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
                        val inputBuffer = decoder.getInputBuffer(inputBufferId)
                        val sampleSize = extractor.readSampleData(inputBuffer!!, 0)

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
                    val outputBuffer = decoder.getOutputBuffer(outputBufferId)

                    // esto es IMPORTANTE - aca se remuestrea si es necesario
                    val chunk = if (outputChannelCount == 2) {

                        mixStereoToMono(outputBuffer!!, bufferInfo.size)
                    } else if (outputSampleRate != TARGET_SAMPLE_RATE) {

                        val rawChunk = ByteArray(bufferInfo.size)
                        outputBuffer?.get(rawChunk)
                        rawChunk

                    } else {
                        // chequeo que sea mono y 44100hz
                        val rawChunk = ByteArray(bufferInfo.size)
                        outputBuffer?.get(rawChunk)
                        rawChunk
                    }
                    outputStream.write(chunk)
                    outputBuffer?.clear()

                    decoder.releaseOutputBuffer(outputBufferId, false)

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isDecoderEOS = true
                    }
                } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val newFormat = decoder.outputFormat
                    outputSampleRate = newFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE, 0)
                    outputChannelCount = newFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT, 0)

                    if (outputSampleRate != TARGET_SAMPLE_RATE) {
                        Log.w(TAG, "ADVERTENCIA: Sample Rate de salida (${outputSampleRate} Hz) es diferente al objetivo (${TARGET_SAMPLE_RATE} Hz). Esto causará velocidad incorrecta.")
                    }
                    if (outputChannelCount > TARGET_CHANNEL_COUNT) {
                        Log.i(TAG, "Se detectó audio estéreo (${outputChannelCount} canales). Se realizará la mezcla a Mono.")
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
     * Convierte un buffer de audio PCM de 16 bits Estéreo a Mono.
     */
    private fun mixStereoToMono(stereoBuffer: ByteBuffer, sizeBytes: Int): ByteArray {
        stereoBuffer.rewind()
        // me aseguro los bytes en orden y calculo los mono bytes

        stereoBuffer.order(ByteOrder.LITTLE_ENDIAN)

        val numShorts = sizeBytes / BYTES_PER_SAMPLE
        val monoBytes = ByteArray(sizeBytes / 2)
        val monoBuffer = ByteBuffer.wrap(monoBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val stereoShortBuffer = stereoBuffer.asShortBuffer()

        for (i in 0 until numShorts step 2) {
            val left = stereoShortBuffer.get(i).toInt()
            val right = stereoShortBuffer.get(i + 1).toInt()

            val monoSample = ((left + right) / 2).toShort()
            monoBuffer.put(monoSample)
        }

        return monoBytes
    }
}