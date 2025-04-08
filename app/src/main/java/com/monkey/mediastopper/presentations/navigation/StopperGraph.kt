package com.monkey.mediastopper.presentations.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.monkey.mediastopper.presentations.screens.HomeScreen
import com.monkey.mediastopper.presentations.screens.SettingsScreen
import com.monkey.mediastopper.presentations.screens.StopTimerScreen
import com.monkey.mediastopper.presentations.viewmodel.StopperViewModel

@Composable
fun StopperGraph(nav: NavHostController, stopperViewModel: StopperViewModel) {
    NavHost(navController = nav, startDestination = Screen.HomeScreen.route) {
        composable(Screen.HomeScreen.route) {
            HomeScreen(stopperViewModel)
        }
        composable(Screen.SettingsScreen.route) {
            SettingsScreen(stopperViewModel)
        }
        composable(Screen.TimerScreen.route) {
            StopTimerScreen(stopperViewModel)
        }
    }
}
