package com.parsaplanner.app.util

import android.content.Context
import android.media.MediaRecorder
import java.io.File

/** Minimal voice-memo recorder. Files are saved under the app's private storage. */
class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun start(): File {
        val dir = File(context.filesDir, "voice_notes").apply { mkdirs() }
        val file = File(dir, "voice_${System.currentTimeMillis()}.m4a")
        outputFile = file

        @Suppress("DEPRECATION")
        val mr = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            MediaRecorder(context) else MediaRecorder()

        mr.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = mr
        return file
    }

    /** Returns the saved file, or null if nothing was recorded. */
    fun stop(): File? {
        return try {
            recorder?.apply { stop(); release() }
            recorder = null
            outputFile
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            null
        }
    }

    fun cancel() {
        try { recorder?.apply { stop(); release() } } catch (_: Exception) {}
        recorder = null
        outputFile?.delete()
        outputFile = null
    }
}
