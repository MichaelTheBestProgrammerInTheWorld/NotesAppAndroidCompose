package com.example.notesappandroidcompose.presentation.note_detail

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import java.io.File

class VoicePlayer(private val context: Context) {
    private var player: MediaPlayer? = null

    fun play(uri: Uri) {
        val uriString = uri.toString()
        Log.d("VoicePlayer", "play() called with URI: $uriString")
        
        stop()
        
        try {
            val newPlayer = MediaPlayer()
            newPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            
            if (uri.scheme == "file") {
                val path = uri.path
                if (path == null) {
                    Log.e("VoicePlayer", "URI path is null")
                    Toast.makeText(context, "Invalid file path", Toast.LENGTH_SHORT).show()
                    return
                }
                
                val file = File(path)
                if (!file.exists()) {
                    Log.e("VoicePlayer", "File does not exist: $path")
                    Toast.makeText(context, "Recording file missing", Toast.LENGTH_SHORT).show()
                    return
                }
                
                Log.d("VoicePlayer", "File found, size: ${file.length()} bytes")
                if (file.length() == 0L) {
                    Toast.makeText(context, "Recording is empty", Toast.LENGTH_SHORT).show()
                    return
                }
                
                newPlayer.setDataSource(file.absolutePath)
            } else {
                Log.d("VoicePlayer", "Using context setDataSource for scheme: ${uri.scheme}")
                newPlayer.setDataSource(context, uri)
            }
            
            newPlayer.prepare()
            newPlayer.start()
            
            player = newPlayer
            Log.d("VoicePlayer", "Playback started successfully")
            Toast.makeText(context, "Playing recording...", Toast.LENGTH_SHORT).show()
            
            newPlayer.setOnCompletionListener {
                Log.d("VoicePlayer", "Playback completed naturally")
                stop()
            }
            
            newPlayer.setOnErrorListener { _, what, extra ->
                Log.e("VoicePlayer", "MediaPlayer error: what=$what, extra=$extra")
                stop()
                true
            }
            
        } catch (e: Exception) {
            Log.e("VoicePlayer", "Exception during playback setup", e)
            Toast.makeText(context, "Playback failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            stop()
        }
    }

    fun stop() {
        Log.d("VoicePlayer", "stop() called")
        try {
            player?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                Log.d("VoicePlayer", "Player released")
            }
        } catch (e: Exception) {
            Log.e("VoicePlayer", "Error stopping player", e)
        } finally {
            player = null
        }
    }
}
