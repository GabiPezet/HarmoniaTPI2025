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
