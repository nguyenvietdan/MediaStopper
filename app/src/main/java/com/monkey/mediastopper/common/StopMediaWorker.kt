package com.monkey.mediastopper.common

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.monkey.mediastopper.framework.MediaControllerHolder
import com.monkey.mediastopper.framework.MediaControllerMgr
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StopMediaWorker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val TAG = "StopMediaWork"

    private var mediaControllerMgr: MediaControllerMgr? = null
    init {
        mediaControllerMgr = MediaControllerHolder.controller
        Log.i(TAG, "checking media $mediaControllerMgr : ")
    }

    override suspend fun doWork(): Result {
        return try {
            Log.i(TAG, "doWork:")
            stopAllMedia()
            Result.success()
        } catch (ex: Throwable) {
            Log.i(TAG, "doWork: fail due to ${ex.printStackTrace()}")
            Result.failure()
        }
    }

    private fun stopAllMedia() {
        mediaControllerMgr?.stopAllMedia()
    }
}