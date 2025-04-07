package com.monkey.mediastopper.framework

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import javax.inject.Inject

class VolumeObserver @Inject constructor(
    private val context: Context,
    private val onVolumeChanged: VolumeChangeListener
) : ContentObserver(Handler(Looper.getMainLooper())) {

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        onVolumeChanged.onVolumeChanged(selfChange)
    }

    fun register() {
        context.contentResolver.registerContentObserver(
            android.provider.Settings.System.CONTENT_URI,
            true,
            this
        )
    }

    fun unregister() {
        context.contentResolver.unregisterContentObserver(this)
    }
}