package com.monkey.mediastopper.di

import com.monkey.mediastopper.framework.MediaControllerMgr
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MediaControllerManagerEntryPoint {
    fun mediaControllerManager(): MediaControllerMgr
}