package com.monkey.mediastopper.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

interface MediaStopperSharedData {
    val sharedEvents: Map<String, @JvmSuppressWildcards MutableSharedFlow<*>>
    val sharedStates: Map<String, @JvmSuppressWildcards MutableStateFlow<*>>
}

@Suppress("UNCHECKED_CAST")
fun <T> MediaStopperSharedData.getEvent(name: String): MutableSharedFlow<T>? {
    return sharedEvents[name]?.let {
        it as MutableSharedFlow<T>
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> MediaStopperSharedData.getState(name: String): MutableStateFlow<T>? {
    return sharedStates[name]?.let {
        it as MutableStateFlow<T>
    }
}