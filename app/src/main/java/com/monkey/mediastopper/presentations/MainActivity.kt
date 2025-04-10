package com.monkey.mediastopper.presentations

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.monkey.mediastopper.common.ScheduledMediaStopper
import com.monkey.mediastopper.presentations.screens.MediaStopperApp
import com.monkey.mediastopper.presentations.theme.MediaStopperTheme
import com.monkey.mediastopper.presentations.viewmodel.StopperViewModel
import com.monkey.mediastopper.utils.Constants.EXTRA_IS_POSTED
import com.monkey.mediastopper.utils.Constants.EXTRA_PACKAGE
import com.monkey.mediastopper.utils.Constants.MEDIA_UPDATER
import com.monkey.mediastopper.utils.Utils.isNotificationServiceEnabled
import com.monkey.mediastopper.utils.Utils.openNotificationSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG = "MainActivityComponent"

    private lateinit var mediaReceiver: BroadcastReceiver

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val stopperViewModel: StopperViewModel by viewModels()
        mediaListener(stopperViewModel)
        if (!isNotificationServiceEnabled(this)) {
            Log.d(TAG, "Notification listener service is NOT enabled. Prompting user.")
            // Show a dialog or message explaining why you need notification access
            // and provide a button to call openNotificationSettings(this)
            // e.g. using an AlertDialog:

            AlertDialog.Builder(this)
                .setTitle("Notification Access Required")
                .setMessage("This app needs access to notifications to function correctly. Please enable the service in settings.")
                .setPositiveButton("Go to Settings") { dialog, which ->
                    openNotificationSettings(this)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        setContent {
            val nav = rememberNavController()
            MediaStopperTheme {
                MediaStopperApp(nav, stopperViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mediaReceiver)
    }

    private fun startNotification() {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        startActivity(intent)
    }

    private fun registerBroadcast() {
        val intentFilter = IntentFilter(MEDIA_UPDATER)
        ContextCompat.registerReceiver(
            this,
            mediaReceiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun mediaListener(stopperViewModel: StopperViewModel) {
        mediaReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val pkgName = intent?.getStringExtra(EXTRA_PACKAGE) ?: return
                val isPosted = intent.getBooleanExtra(EXTRA_IS_POSTED, true)
                Log.i(TAG, "onReceive: receive ${pkgName} ${isPosted}")
                stopperViewModel.updateMediaInfo(pkgName, isPosted)
            }
        }
        registerBroadcast()
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    MediaStopperTheme {
        Greeting("Android")
    }
}