package com.monkey.mediastopper.presentations.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.monkey.mediastopper.presentations.navigation.Screen
import com.monkey.mediastopper.presentations.theme.MediaStopperTheme
import com.monkey.mediastopper.presentations.viewmodel.StopperViewModel

@Composable
fun SettingsScreen(stopperViewModel: StopperViewModel) {
    stopperViewModel.updateCurrentScreen(Screen.SettingsScreen.route)
    Text("SettingsScreen")
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    MediaStopperTheme {
        SettingsScreen(hiltViewModel())
    }
}
