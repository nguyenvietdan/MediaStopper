package com.monkey.mediastopper.common

import android.content.Context
import com.monkey.mediastopper.framework.MediaControllerMgr
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledMediaStopper @Inject constructor(
    @ApplicationContext context: Context,
    private val mediaController: MediaControllerMgr
) {
    private val schedule: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    fun scheduleStopMedia(delay: Long) {
        shutdown()
        schedule.schedule({
            stopAllMedia()
            shutdown()
        }, delay, TimeUnit.MILLISECONDS)
    }

    private fun shutdown() {
        // todo add it to destroy activity
        if (!schedule.isShutdown) {
            schedule.shutdownNow()
        }
    }

    private fun stopAllMedia() {
        mediaController.stopAllMedia()
        // todo checking stopAllMediaByChangeFocus
    }
}