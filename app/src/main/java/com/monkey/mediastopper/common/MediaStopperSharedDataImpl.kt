package com.monkey.mediastopper.common

import com.monkey.mediastopper.di.MediaStopperScoped
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStopperSharedDataImpl @Inject constructor(
    mediaSharedEvents: Map<String, @JvmSuppressWildcards MutableSharedFlow<*>>,
    mediaStateEvents: Map<String, @JvmSuppressWildcards MutableStateFlow<*>>
) : MediaStopperSharedData {

    override val sharedEvents: Map<String, MutableSharedFlow<*>> = mediaSharedEvents
    override val sharedStates: Map<String, MutableStateFlow<*>> = mediaStateEvents
}

@InstallIn(SingletonComponent::class)
@Module
abstract class MediaStopperSharedDataModule {

    @Binds
    abstract fun binds(impl: MediaStopperSharedDataImpl): MediaStopperSharedData
}