package com.monkey.mediastopper.model

import android.media.session.PlaybackState

data class MediaItem (
    val appName: String = "",
    val title: String = "",
    val pkgName: String = "",
    var state: Int = 0,
    val duration: Long = 0L,
    val position: Long = 0L,
    val volume: Int = 0
) {
    fun isPlaying() = state == PlaybackState.STATE_PLAYING
}