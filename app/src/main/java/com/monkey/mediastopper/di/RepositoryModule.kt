package com.monkey.mediastopper.di

import android.content.Context
import com.monkey.data.local.SharePreferenceRepositoryImpl
import com.monkey.domain.repository.SharePreferenceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideSharePreferenceRepository(@ApplicationContext context: Context): SharePreferenceRepository =
        SharePreferenceRepositoryImpl(context)
}