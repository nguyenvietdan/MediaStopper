package com.monkey.mediastopper.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.monkey.mediastopper.presentations.navigation.Screen

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val screenRoute: String
)
