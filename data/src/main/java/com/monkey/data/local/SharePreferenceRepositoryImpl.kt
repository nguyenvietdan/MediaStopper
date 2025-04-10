package com.monkey.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monkey.domain.repository.SharePreferenceRepository
import com.monkey.domain.repository.SharePreferenceRepository.Constants.BASE_SHARE_PREFS
import com.monkey.domain.repository.SharePreferenceRepository.Constants.KEY_MAX_STOP_TIMER
import com.monkey.domain.repository.SharePreferenceRepository.Constants.KEY_STOP_TIMER
import com.monkey.domain.repository.SharePreferenceRepository.Constants.KEY_VOLUME
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharePreferenceRepositoryImpl @Inject constructor(private val context: Context): SharePreferenceRepository {

    private val TAG = "SharePreferenceRepositoryImpl"

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = BASE_SHARE_PREFS)
    private val sharePreferenceName get() = BASE_SHARE_PREFS

    private val _volume = VOLUME.createFlow(1)
    override val volume: StateFlow<Int> = _volume.asStateFlow()

    private val _stopTimer = STOP_TIMER.createFlow(-1L)
    override val stopTimer: StateFlow<Long> = _stopTimer.asStateFlow()

    private val _maxStopTimer = MAX_STOP_TIMER.createFlow(100F)
    override val maxStopTimer: StateFlow<Float> = _maxStopTimer.asStateFlow()

    override fun setCurrentVolume(volume: Int) {
        _volume.value = volume
    }

    override suspend fun save(key: String, value: Any) {
        Log.i(TAG, "save: $key = $value")
        context.dataStore.edit { preferences ->
            when(key) {
                KEY_VOLUME -> {
                    preferences[VOLUME] = value as Int
                    _volume.value = value
                }
                KEY_STOP_TIMER -> {
                    preferences[STOP_TIMER] = value as Long
                    _stopTimer.value = value
                }
                KEY_MAX_STOP_TIMER -> {
                    preferences[MAX_STOP_TIMER] = value as Float
                    _maxStopTimer.value = value
                }
            }
        }
    }

    private inline fun <T, reified R> Preferences.Key<T>.default(default: R): R = runBlocking {
        val value = context.dataStore.data
            .catch { exception ->
                if (exception is CorruptionException || exception is java.io.IOException) {
                    Log.w(TAG, "Data Store Exception $exception")
                    context.filesDir.listFiles()
                        ?.find { it.name == "datastore" }?.let { dataStoreFolder ->
                            dataStoreFolder.listFiles()
                                ?.find { it.name.contains(sharePreferenceName) }?.let {
                                    Log.w(TAG, "Data Store File delete ${it.name}")
                                    it.delete()
                                }
                        }
                    emit(emptyPreferences())
                } else throw exception
            }.map {
                Log.i(
                    TAG,
                    "[default] preference load : " +
                            "${this@default} = ${it[this@default]}, $default"
                )
                if (it[this@default] == null) {
                    context.dataStore.edit { preferences ->
                        preferences[this@default] = default as T
                    }
                }
                return@map it[this@default] ?: default
            }.first()
        if (value is R) value else default
    }

    private inline fun <reified T> Preferences.Key<T>.createFlow(default: T): MutableStateFlow<T> {
        return MutableStateFlow(this.default(default))
    }

    private fun Preferences.Key<Int>.createVolumeFlow(default: Int) =
        MutableStateFlow(this.default(default))

    companion object {
        private val VOLUME = intPreferencesKey(KEY_VOLUME)
        private val STOP_TIMER = longPreferencesKey(KEY_STOP_TIMER)
        private val MAX_STOP_TIMER = floatPreferencesKey(KEY_MAX_STOP_TIMER)
    }
}