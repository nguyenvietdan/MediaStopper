package com.monkey.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SharePreferenceRepository {

    val volume: StateFlow<Int>

    fun setCurrentVolume(volume: Int)
    suspend fun save(key: String, value: Any)

    object Constants {
        const val BASE_SHARE_PREFS = "base_share_prefs"
        const val KEY_VOLUME = "KEY_VOLUME"
    }
}