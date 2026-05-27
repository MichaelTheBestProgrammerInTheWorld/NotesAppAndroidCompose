package com.example.notesappandroidcompose.presentation.note_detail

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class VoicePlayer(private val context: Context) {
    private var player: MediaPlayer? = null

    fun play(uri: Uri) {
        try {
            stop()
            player = MediaPlayer().apply {
                setDataSource(context, uri)
                prepare()
                start()
                setOnCompletionListener {
                    stop()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
    }
}
