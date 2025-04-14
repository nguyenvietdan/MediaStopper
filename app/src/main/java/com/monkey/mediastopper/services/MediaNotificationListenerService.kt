package com.monkey.mediastopper.services

import android.app.Notification
import android.content.Intent
import android.media.session.MediaController
import android.media.session.MediaSession
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.monkey.mediastopper.di.MediaControllerManagerEntryPoint
import com.monkey.mediastopper.framework.MediaControllerMgr
import com.monkey.mediastopper.utils.Constants.EXTRA_IS_POSTED
import com.monkey.mediastopper.utils.Constants.EXTRA_PACKAGE
import com.monkey.mediastopper.utils.Constants.MEDIA_UPDATER
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

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
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        fetchActiveMediaNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        handleMediaSession(sbn, true)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(TAG, "Notification ${sbn.packageName} is removed isRemoved(sbn.packageName) ${isRemoved(sbn.packageName)}")
        if (isRemoved(sbn.packageName)) {
            removeMediaFromController(sbn.packageName)
            sendMediaUpdate(sbn.packageName, false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controllerManager.clearAll()
    }

    private fun handleMediaSession(sbn: StatusBarNotification, isPosted: Boolean) {
        // get token from notification and check if it's a media notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            sbn.notification.extras.getParcelable(
                Notification.EXTRA_MEDIA_SESSION,
                MediaSession.Token::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            sbn.notification.extras.getParcelable(Notification.EXTRA_MEDIA_SESSION)
        } ?.let {
            handleMediaController(sbn, it, isPosted)
        }
    }

    private fun fetchActiveMediaNotifications() {
        val active = activeNotifications
        for (sbn in active) {
            handleMediaSession(sbn, true)
        }
    }

    private fun isRemoved(pkg: String): Boolean {
        val active = activeNotifications
        for (sbn in active) {
            if (sbn.packageName == pkg) return false
        }
        return true
    }

    private fun handleMediaController(
        sbn: StatusBarNotification,
        token: MediaSession.Token?,
        isPosted: Boolean
    ) {
        if (isPosted && token != null) addToMediaController(
            sbn.packageName,
            MediaController(this, token)
        ) else removeMediaFromController(sbn.packageName)
        sendMediaUpdate(sbn.packageName, isPosted)
    }

    private fun addToMediaController(pkg: String, mediaController: MediaController) =
        controllerManager.addController(pkg, mediaController)

    private fun removeMediaFromController(pkg: String) = controllerManager.removeMedia(pkg)


    private fun sendMediaUpdate(
        pkgName: String,
        isPosted: Boolean
    ) {
        Log.i(TAG, "sendMediaUpdate: $pkgName $isPosted")
        sendBroadcast(Intent(MEDIA_UPDATER).apply {
            this.putExtra(EXTRA_PACKAGE, pkgName)
            this.putExtra(EXTRA_IS_POSTED, isPosted)
        })
    }
}