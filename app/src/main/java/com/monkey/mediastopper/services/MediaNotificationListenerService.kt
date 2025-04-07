package com.monkey.mediastopper.services

import android.app.Notification
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.os.Build
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.monkey.mediastopper.di.MediaControllerManagerEntryPoint
import com.monkey.mediastopper.framework.MediaControllerHolder
import com.monkey.mediastopper.framework.MediaControllerMgr
import com.monkey.mediastopper.utils.Constants.MEDIA_UPDATER
import com.monkey.mediastopper.utils.Utils.getAppNameFromPackage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

@AndroidEntryPoint
class MediaNotificationListenerService : NotificationListenerService() {
    private val TAG = "MediaNotificationListenerService"

    lateinit var controllerManager: MediaControllerMgr

    override fun onCreate() {
        super.onCreate()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            MediaControllerManagerEntryPoint::class.java
        )
        controllerManager = entryPoint.mediaControllerManager()
        Log.e(TAG, "onCreate: $controllerManager" )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        fetchActiveMediaNotifications()
        Log.d("MediaStopper", "Notification listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkgName = sbn.packageName
        val extras = sbn.notification.extras

        //val token = extras.getParcelable<MediaSession.Token>(Notification.EXTRA_MEDIA_SESSION)
        val token: MediaSession.Token? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable(
                    Notification.EXTRA_MEDIA_SESSION,
                    MediaSession.Token::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                extras.getParcelable(Notification.EXTRA_MEDIA_SESSION)
            }
        if (token != null) {
            try {
                handleMediaController(sbn, token)
            } catch (e: Exception) {
                Log.e("MediaStopper", "Error reading media session from $pkgName", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("MediaStopper", "Notification removed from: ${sbn.packageName}")
    }

    private fun fetchActiveMediaNotifications() {
        val active = activeNotifications
        for (sbn in active) {
            val extras = sbn.notification.extras
            val token: MediaSession.Token? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras.getParcelable(
                        Notification.EXTRA_MEDIA_SESSION,
                        MediaSession.Token::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    extras.getParcelable(Notification.EXTRA_MEDIA_SESSION)
                }

            if (token != null) {
                handleMediaController(sbn, token)
            }
        }
    }

    private fun handleMediaController(sbn: StatusBarNotification, token: MediaSession.Token) {
        val mediaController = MediaController(this, token)
        val metadata = mediaController.metadata
        val title = metadata?.description?.title?.toString() ?: "Unknown"
        val pkg = sbn.packageName
        val durationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val currentPosition = (mediaController.playbackState?.let {
            val timeDelta = SystemClock.elapsedRealtime() - it.lastPositionUpdateTime
            it.position + (timeDelta * it.playbackSpeed).toLong()
        } ?: 0L).coerceAtMost(durationMs)

        sendMediaUpdate(pkg, title, mediaController.playbackState?.state, currentPosition, durationMs)
        addToMediaController(pkg, mediaController)
    }

    private fun addToMediaController(pkg: String, mediaController: MediaController) {
        controllerManager.addController(pkg, mediaController)
    }

    private fun sendMediaUpdate(
        pkgName: String,
        title: String,
        state: Int?,
        currentPosition: Long,
        durationMs: Long
    ) {
        val intent = Intent(MEDIA_UPDATER)
        intent.putExtra("pkgName", pkgName)
        intent.putExtra("title", title)
        state?.let {
            intent.putExtra("state", it)
        }
        val appName = getAppNameFromPackage(this, pkgName)
        intent.putExtra("appName", appName)
        intent.putExtra("duration", durationMs)
        intent.putExtra("position", currentPosition)
        Log.i(TAG, "sendMediaUpdate: currentPosition $currentPosition")

        sendBroadcast(intent)
    }
}