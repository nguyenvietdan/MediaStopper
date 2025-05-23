package com.monkey.mediastopper.presentations.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.WorkManager
import com.monkey.mediastopper.R
import com.monkey.mediastopper.model.MediaItem
import com.monkey.mediastopper.presentations.navigation.Screen
import com.monkey.mediastopper.presentations.theme.MediaStopperTheme
import com.monkey.mediastopper.presentations.viewmodel.StopperViewModel
import com.monkey.mediastopper.utils.Utils.formatTime
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(stopperViewModel: StopperViewModel) {
    val mediaItems by stopperViewModel.mediaItems.collectAsState()
    val volume by stopperViewModel.sharePrefs.volume.collectAsState()
    val mediaState by stopperViewModel.isPlaying.collectAsState()
    val remainTimer by stopperViewModel.remainTimer.collectAsState()

    LaunchedEffect(remainTimer) {
        while (remainTimer > 0) {
            delay(1000)
            stopperViewModel.updateRemainTimer()
        }
    }

    LaunchedEffect(mediaState) {
        while (mediaState) {
            delay(1000)
            stopperViewModel.tick()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        if (remainTimer > 0)
            Text(
                text = stringResource(R.string.remain_timer, formatTime(remainTimer)),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
        LazyColumn {
            items(mediaItems) { media ->
                MediaControlCard(media = media, volume == 0, stopperViewModel)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }


}

@Composable
fun MediaControlCard(
    media: MediaItem,
    isMuted: Boolean = false,
    stopperViewModel: StopperViewModel
) {
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
            Text(media.appName, style = MaterialTheme.typography.titleMedium)
            Text("Playing: ${media.title}", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                Text(
                    text = formatTime(media.position)
                )
                Text(
                    text = formatTime(media.duration)
                )
            }
            if (media.duration > 0) {
                LinearProgressIndicator(
                    progress = { media.position.toFloat() / media.duration },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                TextButton(onClick = {
                    stopperViewModel.muteOrUnMuteMedia(media.pkgName)
                }) {
                    Icon(
                        painter = painterResource(
                            if (isMuted || media.isMuted()) {
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
                    stopperViewModel.pauseOrPlay(media.pkgName, media.isPlaying())
                }) {
                    Icon(
                        painter = painterResource(
                            if (media.isPlaying()) {
                                R.drawable.baseline_pause_24
                            } else {
                                R.drawable.baseline_play_arrow_24
                            }
                        ),
                        contentDescription = "Pause"
                    )
                }
                TextButton(onClick = {
                    stopperViewModel.stopMedia(media.pkgName)
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