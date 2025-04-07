package com.monkey.mediastopper.presentations.navigation

sealed class Screen(val route: String) {
    data object HomeScreen: Screen("home_screen")
    data object SettingsScreen: Screen("settings_screen")

}