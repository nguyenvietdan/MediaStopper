package com.monkey.mediastopper.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils.equals
import android.text.TextUtils.isEmpty
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.navigation.NavHostController
import com.monkey.mediastopper.model.MediaItem
import com.monkey.mediastopper.utils.Constants.TAG
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

object Utils {

    fun navigateWithPopUpTo(nav: NavHostController, route: String) = nav.navigate(route) {
        popUpTo(route) {
            inclusive = true
        }
        launchSingleTop = true
        restoreState = true
    }

    fun isNotificationServiceEnabled(context: Context): Boolean {
        /*val pkgName = context.packageName
        val enabledListeners = android.provider.Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false

        return enabledListeners.contains(pkgName)*/
        val packageName = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        if (!isEmpty(flat)) {
            val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null && equals(packageName, cn.packageName)) {
                    return true
                }
            }
        }
        return false
    }

    fun getAppNameFromPackage(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName // fallback nếu không tìm được
        }
    }

    fun isMediaActuallyMuted(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.isStreamMute(AudioManager.STREAM_MUSIC)
        } else {
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun openNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        context.startActivity(intent)
    }

    fun MutableList<MediaItem>.updateOrAddItem(item: MediaItem) {
        val index = this.indexOfFirst { it.pkgName == item.pkgName }
        // TODO checking stopped late
        if (index != -1 /*&& item.state != PlaybackState.STATE_STOPPED*/) this[index] = item
        else this.add(item)
    }
}