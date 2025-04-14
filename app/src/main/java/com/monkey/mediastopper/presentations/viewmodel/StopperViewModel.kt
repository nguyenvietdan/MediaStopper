package com.monkey.mediastopper.presentations.viewmodel

import android.content.Context
import android.media.AudioManager
import android.media.session.PlaybackState
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.monkey.domain.repository.SharePreferenceRepository
import com.monkey.domain.repository.SharePreferenceRepository.Constants.KEY_STOP_TIMER
import com.monkey.domain.repository.SharePreferenceRepository.Constants.KEY_VOLUME
import com.monkey.mediastopper.R
import com.monkey.mediastopper.common.MediaStopperSharedData
import com.monkey.mediastopper.common.ScheduledMediaStopper
import com.monkey.mediastopper.common.StopMediaWorker
import com.monkey.mediastopper.common.UISource.getDrawerItems
import com.monkey.mediastopper.common.getEvent
import com.monkey.mediastopper.di.IoDispatcher
import com.monkey.mediastopper.framework.AudioFocusManager
import com.monkey.mediastopper.framework.MediaControllerHolder
import com.monkey.mediastopper.framework.MediaControllerMgr
import com.monkey.mediastopper.framework.MediaStopperSharedDataHolder
import com.monkey.mediastopper.framework.VolumeChangeListener
import com.monkey.mediastopper.framework.VolumeObserver
import com.monkey.mediastopper.model.DrawerItem
import com.monkey.mediastopper.model.MediaItem
import com.monkey.mediastopper.presentations.navigation.Screen
import com.monkey.mediastopper.utils.Utils.updateOrAddItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class StopperViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val controllerManager: MediaControllerMgr,
    val sharePrefs: SharePreferenceRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val scheduledMediaStopper: ScheduledMediaStopper,
    private val mediaSharedData: MediaStopperSharedData,
    private val audioFocusManager: AudioFocusManager
) : ViewModel() {

    private val TAG = "StopperViewModel"

    private val _drawerItems = MutableStateFlow<List<DrawerItem>>(emptyList())
    val drawerItems: StateFlow<List<DrawerItem>> = _drawerItems.asStateFlow()
    private val _currentScreen = MutableStateFlow<String>(Screen.HomeScreen.route)
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()
    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>>
        get() = _mediaItems.asStateFlow()
    private val _topBarTitle = MutableStateFlow("")
    val topBarTitle: StateFlow<String> = _topBarTitle.asStateFlow()
    private val _gesturesEnabled = MutableStateFlow(true)
    val gesturesEnabled: StateFlow<Boolean> = _gesturesEnabled.asStateFlow()

    private val _remainTimer = MutableStateFlow(0L)
    val remainTimer: StateFlow<Long> = _remainTimer.asStateFlow()

    private var _currentVolume = 1

    private val _isPlaying = MutableStateFlow<Boolean>(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()


    private val volumeChangeListener = object : VolumeChangeListener {
        override fun onVolumeChanged(selfChanged: Boolean) {

            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            updateVolume(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
        }
    }

    private val volumeObserver = VolumeObserver(context, volumeChangeListener)

    init {
        startObserving()
        _currentVolume = getCurrentVolume()
        _drawerItems.value = getDrawerItems(context)
        MediaControllerHolder.controller = controllerManager
        MediaStopperSharedDataHolder.mediaStopperSharedData = mediaSharedData
        updateTopBarTitle(_currentScreen.value)
        viewModelScope.launch(ioDispatcher) {
            sharePrefs.stopTimer.collectLatest {
                updateRemainTimer()
            }
        }
        loadMediaItems()
        mediaSharedData.getEvent<Unit>("StopMedias")?.onEach {
            loadMediaItems()
            updatePlaying()
            stopOtherMedia()
        }?.launchIn(viewModelScope)
    }

    private fun loadMediaItems() {
        viewModelScope.launch {
            _mediaItems.value = controllerManager.getMediaItems()
        }
    }

    fun updateCurrentScreen(screen: String) {
        updateTopBarTitle(screen)
        Log.e(TAG, "updateCurrentScreen: $screen" )
        _currentScreen.value = screen
    }

    private fun updateTopBarTitle(screen: String) {
        _topBarTitle.value =
            _drawerItems.value.find { it.screenRoute == screen }?.title ?: context.getString(
                R.string.menu_home
            )
    }

    private fun updateVolume(volume: Int) = viewModelScope.launch(ioDispatcher) {
        sharePrefs.save(KEY_VOLUME, volume)
    }

    private fun updateMediaInfo(item: MediaItem, isPosted: Boolean = true) {

        val currentMediaItems = _mediaItems.value.toMutableList()
        if (isPosted) currentMediaItems.updateOrAddItem(item)
        else currentMediaItems.removeIf { it.pkgName == item.pkgName }
        _mediaItems.value = currentMediaItems

    }

    fun updateMediaInfo(pkg: String, isPosted: Boolean) {
        Log.i(TAG, "updateMediaInfo: ${pkg} isPosted $isPosted") // Posted means that added
        updateMediaInfo(controllerManager.getMediaItem(pkg) ?: MediaItem(pkgName = pkg), isPosted)
        updatePlaying()
    }

    private fun updatePlaying() {
        Log.i(TAG, "updatePlaying: isPlaying ${_isPlaying.value} ")
        _isPlaying.value = controllerManager.isPlaying()
    }

    fun tick() = _mediaItems.value.find { it.state == PlaybackState.STATE_PLAYING }?.let {
        if (it.position + 999 < it.duration)
            updateMediaInfo(it.copy(position = it.position + 1000))
        else _isPlaying.value = false
    } ?: { _isPlaying.value = false }

    private fun startObserving() = volumeObserver.register()

    override fun onCleared() {
        volumeObserver.unregister()
        super.onCleared()
    }

    fun pauseOrPlay(pkg: String, isPlaying: Boolean) =
        if (isPlaying) controllerManager.pauseMedia(pkg) else controllerManager.play(pkg)

    fun stopMedia(pkg: String) = try {
        controllerManager.stopMedia(pkg)
    } catch (e: Exception) {
        Log.e(TAG, "stopMedia: $e")
    }

    fun muteOrUnMuteMedia(pkg: String) {
        val currentVolume = getCurrentVolume()
        Log.i(TAG, "muteOrUnMuteMedia: $currentVolume")
        if (currentVolume > 0) {
            _currentVolume = currentVolume
            controllerManager.muteApp(pkg)
        } else {
            controllerManager.muteApp(pkg, _currentVolume)
        }
    }

    fun unMuteMedia(pkg: String) {
        controllerManager.muteApp(pkg, _currentVolume)
    }

    fun updateGesturesEnabled(enabled: Boolean) {
        _gesturesEnabled.value = enabled
    }

    fun updateTimerStop(minute: Long) {
        viewModelScope.launch(ioDispatcher) {
            val milliseconds = TimeUnit.MINUTES.toMillis(minute)
            Log.i(TAG, "updateTimerStop: $minute $milliseconds")
            sharePrefs.save(
                KEY_STOP_TIMER,
                System.currentTimeMillis() + milliseconds
            )
            requestStopMedia(milliseconds)
        }
    }

    private fun getCurrentVolume() =
        (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).getStreamVolume(
            AudioManager.STREAM_MUSIC
        )

    fun updateRemainTimer() {
        viewModelScope.launch {
            _remainTimer.value =
                (sharePrefs.stopTimer.value - System.currentTimeMillis()).coerceAtLeast(0L)
        }
    }

    private fun requestStopMedia(time: Long) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag("stop_media_task")
        val stopMediaRequest = OneTimeWorkRequestBuilder<StopMediaWorker>().setInitialDelay(time,
            TimeUnit.MILLISECONDS
        ).addTag("stop_media_task").build()
        workManager.enqueue(stopMediaRequest)
    }

    private fun stopOtherMedia() {
        val granted = audioFocusManager.requestFocus()
        if (granted) {
            Log.d(TAG, "Audio focus granted – media apps may pause")
        } else {
            Log.w(TAG, "Audio focus request denied")
        }
    }
}