package com.monkey.mediastopper.presentations.viewmodel

import android.content.Context
import android.media.AudioManager
import android.media.session.PlaybackState
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.lifecycle.ViewModel
import com.monkey.mediastopper.framework.MediaControllerMgr
import com.monkey.mediastopper.framework.VolumeChangeListener
import com.monkey.mediastopper.framework.VolumeObserver
import com.monkey.mediastopper.model.DrawerItem
import com.monkey.mediastopper.model.MediaItem
import com.monkey.mediastopper.presentations.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class StopperViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val controllerManager: MediaControllerMgr
) : ViewModel() {

    val TAG = "StopperViewModel"

    private val _drawerItems = MutableStateFlow<List<DrawerItem>>(emptyList())
    val drawerItems: StateFlow<List<DrawerItem>> = _drawerItems
    private val _currentScreen = MutableStateFlow<String>(Screen.HomeScreen.route)
    val currentScreen: StateFlow<String> = _currentScreen
    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>>
        get() = _mediaItems
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _isPlaying = MutableStateFlow<Boolean>(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying


    private val volumeChangeListener = object : VolumeChangeListener {
        override fun onVolumeChanged(selfChanged: Boolean) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            Log.i("dan.nv", "onVolumeChanged: $selfChanged volume $volume")
            _isMuted.value = volume == 0
        }
    }

    private val volumeObserver = VolumeObserver(context, volumeChangeListener)

    init {
        _drawerItems.value = createDrawerItems()
        startObserving()
        Log.e(TAG, "controllerManager: $controllerManager")
    }

    private fun createDrawerItems(): List<DrawerItem> = listOf(
        DrawerItem("Home", Icons.Default.Home, Screen.HomeScreen.route),
        DrawerItem("Settings", Icons.Default.Settings, Screen.SettingsScreen.route)
    )

    fun updateCurrentScreen(screen: String) {
        _currentScreen.value = screen
    }

    private fun updateMediaInfo(item: MediaItem, shouldUpdatePlaying: Boolean = true) {
        val currentMediaItems = _mediaItems.value.toMutableList()
        currentMediaItems.removeAll { it.pkgName == item.pkgName }
        currentMediaItems.add(item)
        _mediaItems.value = currentMediaItems
        if (shouldUpdatePlaying)
            updatePlaying()
    }

    fun updateMediaInfo(pkg: String) {
        controllerManager.getMediaItem(pkg)?.let {
            updateMediaInfo(it, true)
        }
    }

    private fun updatePlaying() {
        Log.i(TAG, "updatePlaying: isPlaying ${_isPlaying.value}")
        _mediaItems.value.find { it.state == PlaybackState.STATE_PLAYING }?.let {
            _isPlaying.value = true
        } ?: { _isPlaying.value = false }
    }


    fun tick() = _mediaItems.value.find { it.state == PlaybackState.STATE_PLAYING }?.let {
        if (it.position + 999 < it.duration)
            updateMediaInfo(it.copy(position = it.position + 1000), false)
        else _isPlaying.value = false

    } ?: { _isPlaying.value = false }


    private fun startObserving() = volumeObserver.register()

    override fun onCleared() {
        volumeObserver.unregister()
        super.onCleared()
    }

    fun pauseOrPlay(pkg: String, state: Int) =
        if (state == PlaybackState.STATE_PLAYING) controllerManager.pauseMedia(pkg) else controllerManager.play(pkg)

    fun stopMedia(pkg: String) = try {
        controllerManager.stopMedia(pkg)
    } catch (e: Exception) {
        Log.e(TAG, "stopMedia: $e")
    }

    fun muteMedia(pkg: String) = controllerManager.muteApp(pkg)

}