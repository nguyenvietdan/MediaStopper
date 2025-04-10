package com.monkey.mediastopper.presentations.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.monkey.mediastopper.R
import com.monkey.mediastopper.presentations.navigation.Screen
import com.monkey.mediastopper.presentations.theme.GradientColors
import com.monkey.mediastopper.presentations.theme.OuterBackgroundColor
import com.monkey.mediastopper.presentations.theme.TimerBackgroundColor
import com.monkey.mediastopper.presentations.viewmodel.StopperViewModel
import com.monkey.mediastopper.presentations.widgets.CircularSeekbarView
import com.monkey.mediastopper.utils.Utils.formatTime
import com.monkey.mediastopper.utils.Utils.formatTimeFromMinute
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun StopTimerCircularSeekbarScreen(viewModel: StopperViewModel, onAddedStopTimer: () -> Unit = {}) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val size = min(screenWidth, screenHeight) / 2f
    val context = LocalContext.current

    var value by rememberSaveable {
        mutableStateOf(
            ((viewModel.sharePrefs.stopTimer.value - System.currentTimeMillis()) / (60 * 1000)).coerceAtLeast(
                0
            ).toFloat()
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                /*.background(color = TimerBackgroundColor)*/
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(size)
                    .padding(16.dp), contentAlignment = Alignment.Center
            ) {
                CircularSeekbarView(
                    value = value,
                    onChange = { newValue ->
                        value = newValue
                    },
                    maxValue = viewModel.sharePrefs.maxStopTimer.value
                )

                Text(
                    text = formatTimeFromMinute(value.roundToInt()),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    Toast.makeText(
                        context,
                        "Timer added: ${
                            formatTime(
                                TimeUnit.MINUTES.toMillis(
                                    value.roundToInt().toLong()
                                )
                            )
                        }",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.updateTimerStop(value.roundToInt().toLong())
                    onAddedStopTimer()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .background(
                        color = OuterBackgroundColor, // Outer background color
                        shape = RoundedCornerShape(8.dp) // Optional rounded corners for the outer shape
                    )
                    .padding(12.dp), // Inner padding to reveal the outer background
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent, // Make button background transparent
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp) // Rounded corners for the inner gradient
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = GradientColors // Gradient colors
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.add_timer),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

    }
}