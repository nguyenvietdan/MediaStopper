package com.monkey.mediastopper.presentations.screens

import android.media.session.PlaybackState
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.monkey.mediastopper.R
import com.monkey.mediastopper.model.MediaItem
import com.monkey.mediastopper.presentations.navigation.Screen
import com.monkey.mediastopper.presentations.theme.MediaStopperTheme
import com.monkey.mediastopper.presentations.viewmodel.StopperViewModel
import com.monkey.mediastopper.utils.Utils.formatTime
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(stopperViewModel: StopperViewModel) {
    stopperViewModel.updateCurrentScreen(Screen.HomeScreen.route)
    val mediaItems by stopperViewModel.mediaItems.collectAsState()
    val isMuted by stopperViewModel.isMuted.collectAsState()
    val mediaState by stopperViewModel.isPlaying.collectAsState()
    LaunchedEffect(mediaState) {
        while (mediaState) {
            delay(1000)
            stopperViewModel.tick()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        mediaItems.forEach { item ->
            MediaControlCard(item = item, title = item.title, isMuted, stopperViewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MediaControlCard(item: MediaItem, title: String, isMuted: Boolean = false, stopperViewModel: StopperViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(item.appName, style = MaterialTheme.typography.titleMedium)
            Text("Playing: $title", style = MaterialTheme.typography.bodySmall)
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                Text(
                    text = formatTime(item.position)
                )
                Text(
                    text = formatTime(item.duration)
                )
            }
            if (item.duration > 0) {
                LinearProgressIndicator(
                    progress = { item.position.toFloat() / item.duration },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                TextButton(onClick = { /* Mute */ }) {
                    Icon(
                        painter = painterResource(
                            if (isMuted) {
                                R.drawable.baseline_volume_off_24
                            } else {
                                R.drawable.baseline_volume_up_24
                            }
                        ),
                        contentDescription = "Mute"
                    )
                }
                //baseline_play_arrow_24
                TextButton(onClick = {
                    stopperViewModel.pauseOrPlay(item.pkgName, item.state)
                }) {
                    Icon(
                        painter = painterResource(
                            if (item.state == PlaybackState.STATE_PLAYING) {
                                R.drawable.baseline_pause_24
                            } else {
                                R.drawable.baseline_play_arrow_24
                            }
                        ),
                        contentDescription = "Pause"
                    )
                }
                TextButton(onClick = {
                    stopperViewModel.stopMedia(item.pkgName)
                }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_stop_24),
                        contentDescription = "Stop"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    MediaStopperTheme {
        HomeScreen(hiltViewModel())
    }
}