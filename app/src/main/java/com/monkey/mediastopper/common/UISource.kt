package com.monkey.mediastopper.common

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.monkey.mediastopper.R
import com.monkey.mediastopper.model.DrawerItem
import com.monkey.mediastopper.presentations.navigation.Screen

object UISource {
    fun getDrawerItems(context: Context) = listOf(
        DrawerItem(
            context.getString(R.string.menu_home),
            Icons.Default.Home,
            Screen.HomeScreen.route
        ),
        DrawerItem(
            context.getString(R.string.menu_settings),
            Icons.Default.Settings,
            Screen.SettingsScreen.route
        ),
        DrawerItem(
            context.getString(R.string.menu_timer),
            ImageVector.vectorResource(null, context.resources, R.drawable.baseline_access_time_24),
            Screen.TimerScreen.route
        )
    )

}