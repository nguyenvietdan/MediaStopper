package com.monkey.mediastopper.di

import android.content.Context
import com.monkey.mediastopper.framework.MediaControllerMgr
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FrameworkModule {
    @Provides
    @Singleton
    fun provideMediaControllerManager(
        @ApplicationContext context: Context
    ): MediaControllerMgr {
        return MediaControllerMgr(context)
    }
}