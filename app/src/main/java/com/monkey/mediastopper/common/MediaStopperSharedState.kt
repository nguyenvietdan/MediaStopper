package com.monkey.mediastopper.common

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class MediaStopperSharedState<T>(initialValue: T): MutableStateFlow<T> by MutableStateFlow(initialValue) {

    @Singleton
    class MediaStopper @Inject constructor(): MediaStopperSharedState<Unit>(Unit)
}

@InstallIn(SingletonComponent::class)
@Module
abstract class MediaStopperSharedStateModule {
    @Binds
    @IntoMap
    @StringKey("media_stopper")
    abstract fun binds(sharedState: MediaStopperSharedState.MediaStopper): MutableStateFlow<*>
}