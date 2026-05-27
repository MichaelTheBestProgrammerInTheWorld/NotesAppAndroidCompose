package com.example.notesappandroidcompose.presentation.note_detail

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null

    fun start(outputFile: File) {
        try {
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            recorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder?.release()
            recorder = null
        }
    }
}
