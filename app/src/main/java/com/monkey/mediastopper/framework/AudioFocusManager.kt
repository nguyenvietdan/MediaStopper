package com.monkey.mediastopper.framework

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFocusManager @Inject constructor(@ApplicationContext context: Context) {

    private val TAG = "AudioFocusManager"

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager

    private val audioFocusRequest =
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        Log.i(TAG, "lost Audio focus ")
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        Log.i(TAG, "gained Audio focus ")
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        Log.i(TAG, "lost transient Audio focus ")
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        Log.i(TAG, "lost transient can duck Audio focus ")
                    }
                }
            }.build()

    fun requestFocus(): Boolean {
        val result = audioManager.requestAudioFocus(audioFocusRequest)
        Log.i(TAG, "requestFocus: $result")
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun abandonFocus() {
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
        Log.i(TAG, "abandonFocus")
    }
}