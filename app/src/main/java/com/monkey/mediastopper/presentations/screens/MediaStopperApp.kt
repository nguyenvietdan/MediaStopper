package com.monkey.mediastopper.presentations.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.monkey.mediastopper.R
import com.monkey.mediastopper.model.DrawerItem
import com.monkey.mediastopper.presentations.navigation.StopperGraph
import com.monkey.mediastopper.presentations.theme.MediaStopperTheme
import com.monkey.mediastopper.presentations.viewmodel.StopperViewModel
import com.monkey.mediastopper.utils.Constants.TAG
import com.monkey.mediastopper.utils.Utils.navigateWithPopUpTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MediaStopperApp(
    nav: NavHostController,
    stopperViewModel: StopperViewModel /*= hiltViewModel()*/
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentScreen by stopperViewModel.currentScreen.collectAsState()
    val topBarTitle by stopperViewModel.topBarTitle.collectAsState()
    val gesturesEnabled by stopperViewModel.gesturesEnabled.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerSheetContent(drawerState, stopperViewModel, currentScreen) {
                navigateWithPopUpTo(nav, it)
            }
        },
        gesturesEnabled = gesturesEnabled
        //gesturesEnabled = false // Disable swipe gesture to open the drawer`
    ) {

        Scaffold(
            topBar = {
                TopAppBarScreen(topBarTitle) {
                    scope.launch {
                        drawerState.apply {
                            if (isOpen) close() else open()
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                StopperGraph(nav, stopperViewModel)
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun DrawerSheetContent(
    drawerState: DrawerState,
    stopperViewModel: StopperViewModel,
    currentScreen: String = "",
    naviGate: (String) -> Unit = {}
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.nav_header_height)),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "MediaStopper",
                style = TextStyle(fontSize = 28.sp, color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(16.dp)
            )
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        stopperViewModel.apply {
            drawerItems.value.forEach { drawerItem ->
                DrawerSheetItem(
                    drawerState,
                    rememberCoroutineScope(),
                    drawerItem,
                    currentScreen
                ) {
                    naviGate(it)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun DrawerSheetItem(
    drawerState: DrawerState,
    scope: CoroutineScope,
    drawerItem: DrawerItem,
    screen: String,
    naviGate: (String) -> Unit = {}
) {
    Log.i(TAG, "DrawerSheetItem: ${drawerItem.screenRoute} screen $screen title ${drawerItem.title}")
    NavigationDrawerItem(
        icon = { Icon(drawerItem.icon, contentDescription = drawerItem.title) },
        label = { Text(text = drawerItem.title, style = MaterialTheme.typography.titleLarge) },
        selected = drawerItem.screenRoute == screen,
        onClick = {
            scope.launch {
                drawerState.close()
            }
            naviGate(drawerItem.screenRoute)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarScreen(title: String, openDrawer: () -> Unit) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        navigationIcon = {
            IconButton(onClick = {
                openDrawer()
            }) {
                Icon(Icons.Default.Menu, contentDescription = "Drawer Menu")
            }
        })
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MediaStopperAppPreview() {
    MediaStopperTheme {
        MediaStopperApp(
            rememberNavController(), StopperViewModel(
                context = TODO(),
                controllerManager = TODO(),
                sharePrefs = TODO(),
                Dispatchers.IO,
                scheduledMediaStopper = TODO()
            )
        )
    }
}