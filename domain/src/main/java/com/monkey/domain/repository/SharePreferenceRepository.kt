package com.monkey.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SharePreferenceRepository {

    val volume: StateFlow<Int>
    val stopTimer: StateFlow<Long>
    val maxStopTimer: StateFlow<Float>

    fun setCurrentVolume(volume: Int)
    suspend fun save(key: String, value: Any)

    object Constants {
        const val BASE_SHARE_PREFS = "base_share_prefs"
        const val KEY_VOLUME = "key_volume"
        const val KEY_STOP_TIMER = "key_stop_timer"
        const val KEY_MAX_STOP_TIMER = "key_max_stop_timer"
    }
}