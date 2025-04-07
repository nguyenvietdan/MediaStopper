package com.monkey.mediastopper.framework

import android.content.Context
import android.media.VolumeProvider
import android.media.session.MediaController
import android.os.SystemClock
import android.util.Log
import android.media.MediaMetadata as mm
import com.monkey.mediastopper.model.MediaItem
import com.monkey.mediastopper.utils.Utils.getAppNameFromPackage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaControllerMgr @Inject constructor(@ApplicationContext private val context: Context) {
    private val TAG = "MediaControllerMgr"

    private val _controllerMap = mutableMapOf<String, MediaController>()
    val controllerMap: Map<String, MediaController> = _controllerMap

    fun addMedia(pkg: String, mediaController: MediaController) {
        _controllerMap[pkg] = mediaController
    }

    fun getController(packageName: String): MediaController? {
        return _controllerMap[packageName]
    }

    fun pauseAll() {
        Log.i(TAG, "pauseAll: ")
        _controllerMap.values.forEach {
            it.transportControls.pause()
        }
    }

    fun play(packageName: String) {
        Log.i(TAG, "play: $packageName")
        _controllerMap[packageName]?.transportControls?.play()
    }

    fun clearAll() {
        _controllerMap.clear()
    }

    fun getAllPackages(): Set<String> = controllerMap.keys

    fun addController(pkg: String, controller: MediaController) {
        try {
            _controllerMap[pkg] = controller
            Log.e(TAG, "addController: add success for $pkg")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create MediaController: ${e.message}")
        }
    }

    fun removeMedia(pkg: String) {
        Log.i(TAG, "removeMedia: $pkg")
        _controllerMap.remove(pkg)
    }

    fun pauseMedia(pkg: String) {
        Log.i(TAG, "pauseMedia: $pkg")
        _controllerMap[pkg]?.transportControls?.pause()
    }

    fun stopMedia(pkg: String) {
        Log.i(TAG, "stopMedia: $pkg")
        _controllerMap[pkg]?.transportControls?.stop()
    }

    fun muteApp(packageName: String) {
        _controllerMap[packageName]?.let { controller ->
            if (controller.playbackInfo.volumeControl == VolumeProvider.VOLUME_CONTROL_ABSOLUTE) {
                controller.setVolumeTo(0, 0)
                Log.d(TAG, "Muted $packageName")
            } else {
                Log.w(TAG, "App $packageName không hỗ trợ điều chỉnh âm lượng")
            }
        }
    }

    fun getMediaItem(packageName: String): MediaItem? {
        val mediaController = _controllerMap[packageName] ?: return null
        val metadata = mediaController.metadata
        val title = metadata?.description?.title?.toString() ?: "Unknown"
        val durationMs = metadata?.getLong(mm.METADATA_KEY_DURATION) ?: 0L
        val currentPosition = mediaController.playbackState?.let {
            val timeDelta = SystemClock.elapsedRealtime() - it.lastPositionUpdateTime // Calculate time passed since last update
            it.position + (timeDelta * it.playbackSpeed).toLong() // Calculate current position
        }?.coerceAtMost(durationMs) ?: 0L

        val appName = getAppNameFromPackage(context, packageName)
        Log.i(TAG, "getMediaItem: currentPosition $currentPosition")

        val currentVolume = if (mediaController.playbackInfo.volumeControl == VolumeProvider.VOLUME_CONTROL_ABSOLUTE) mediaController.playbackInfo.currentVolume else Int.MAX_VALUE

        return MediaItem(
            appName = appName,
            title,
            packageName,
            mediaController.playbackState?.state ?: 0,
            durationMs,
            currentPosition,
            currentVolume
        )
    }

}