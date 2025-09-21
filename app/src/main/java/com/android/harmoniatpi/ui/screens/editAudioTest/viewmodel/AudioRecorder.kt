package com.android.harmoniatpi.ui.screens.editAudioTest.viewmodel

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecorder(
    private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun startRecording(): Uri? {
        stopRecording() // por si había una previa

        outputFile = createAudioFile(context)

        return try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile!!.absolutePath)

                prepare()
                start()
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                outputFile!!
            )
        } catch (e: Exception) {
            e.printStackTrace()
            releaseRecorder()
            null
        }
    }

    fun stopRecording(): Uri? {
        val file = outputFile
        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
        }
        return if (file != null && file.exists()) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } else null
    }

    private fun releaseRecorder() {
        try {
            mediaRecorder?.release()
        } catch (_: Exception) {}
        mediaRecorder = null
    }

    private fun createAudioFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "AUDIO_${timeStamp}.m4a"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        return File(storageDir, fileName)
    }
}