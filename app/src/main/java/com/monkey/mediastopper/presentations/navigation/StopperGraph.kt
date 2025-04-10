package com.monkey.mediastopper.presentations.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.monkey.mediastopper.presentations.screens.HomeScreen
import com.monkey.mediastopper.presentations.screens.SettingsScreen
import com.monkey.mediastopper.presentations.screens.StopTimerCircularSeekbarScreen
import com.monkey.mediastopper.presentations.screens.StopTimerScreen
import com.monkey.mediastopper.presentations.viewmodel.StopperViewModel
import com.monkey.mediastopper.utils.Utils.navigateWithPopUpTo

@Composable
fun StopperGraph(nav: NavHostController, stopperViewModel: StopperViewModel) {
    NavHost(navController = nav, startDestination = Screen.HomeScreen.route) {
        composable(Screen.HomeScreen.route) {
            stopperViewModel.updateCurrentScreen(Screen.HomeScreen.route)
            HomeScreen(stopperViewModel)
        }
        composable(Screen.SettingsScreen.route) {
            stopperViewModel.updateCurrentScreen(Screen.SettingsScreen.route)
            SettingsScreen(stopperViewModel)
        }
        composable(Screen.TimerScreen.route) {
            stopperViewModel.updateCurrentScreen(Screen.TimerScreen.route)
            StopTimerCircularSeekbarScreen(stopperViewModel) {
                navigateWithPopUpTo(nav, Screen.HomeScreen.route)
            }
        }
    }
}
