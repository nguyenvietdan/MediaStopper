package com.monkey.mediastopper.presentations.screens

import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.hilt.navigation.compose.hiltViewModel
import com.monkey.mediastopper.presentations.navigation.Screen
import com.monkey.mediastopper.presentations.theme.ColorPrimary
import com.monkey.mediastopper.presentations.theme.LightGreen
import com.monkey.mediastopper.presentations.theme.ProgressBarBg
import com.monkey.mediastopper.presentations.theme.ProgressBarProgress
import com.monkey.mediastopper.presentations.theme.ProgressBarTint
import com.monkey.mediastopper.presentations.viewmodel.StopperViewModel
import com.monkey.mediastopper.utils.AngleUtils.calculateAngle
import com.monkey.mediastopper.utils.Constants.TAG
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun StopTimerScreen(viewModel: StopperViewModel) {
    viewModel.updateCurrentScreen(Screen.TimerScreen.route)
    var currentCicler by rememberSaveable { mutableStateOf(0.0) }
    val configuration = LocalConfiguration.current


    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val size = min(screenWidth, screenHeight) / 1.5f

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                MuneerCircularProgressBar(modifier = Modifier.size(size)) { progress: Double ->
                    currentCicler = progress
                    Log.i(TAG, "CustomViewScreen: $progress")
                }
                Text(text = currentCicler.roundToInt().toString())
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MuneerCircularProgressBar(
    modifier: Modifier = Modifier,
    padding: Float = 50f,
    stroke: Float = 35f,
    cap: StrokeCap = StrokeCap.Round,
    initialAngle: Double = 0.0,
    startAngle: Float = -90f,// 0 độ tại 3h, tính theo chều kim đồng hồ
    swipeAngle: Float = 360f,// tổng số độ hển thị circle
    onProgressChanged: (progress: Double) -> Unit,
) {
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    var radius by remember { mutableStateOf(0f) }
    var center by remember { mutableStateOf(Offset.Zero) }

    var appliedAngle by remember {
        mutableStateOf(initialAngle)
    }
    var lastAngle by remember {
        mutableStateOf(0.0)
    }

    Canvas(
        modifier = modifier
            .size(270.dp)
            .onGloballyPositioned {
                width = it.size.width
                height = it.size.height
                center = Offset(width / 2f, height / 2f)
                radius = Math.min(width.toFloat(), height.toFloat()) / 2f - padding - stroke / 2f
            }
            .pointerInteropFilter {

                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        Log.i(TAG, "MuneerCircularProgressBar down: ${it.x},${it.y}")
                        calculateAngle(
                            center,
                            it.x,
                            it.y,
                            lastAngle,
                            radius,
                            swipeAngle,
                            stroke
                        ) { angle ->
                            appliedAngle = angle
                            onProgressChanged(angle /*/ 360.0*/)
                            lastAngle = angle
                        }
                        return@pointerInteropFilter true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        Log.i(TAG, "MuneerCircularProgressBar move:  ${it.x},${it.y}")
                        calculateAngle(
                            center,
                            it.x,
                            it.y,
                            lastAngle,
                            radius,
                            radius,
                            swipeAngle
                        ) { angle ->
                            appliedAngle = angle
                            onProgressChanged(angle /*/ 360.0*/)
                            lastAngle = angle
                        }
                        return@pointerInteropFilter true
                    }

                    MotionEvent.ACTION_UP -> {
                        Log.e(TAG, "MuneerCircularProgressBar up:  ${it.x},${it.y}")
                    }

                    else -> return@pointerInteropFilter false
                }
                return@pointerInteropFilter false

            }
    ) {
        drawArc(
            color = ProgressBarBg,
            startAngle = startAngle,
            sweepAngle = swipeAngle,
            useCenter = false,
            topLeft = center - Offset(radius, radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(
                width = stroke,
                cap = cap
            )
        )


        drawArc(
            color = ProgressBarProgress,
            startAngle = startAngle,
            sweepAngle = abs(appliedAngle.toFloat()),
            topLeft = center - Offset(radius, radius),
            size = Size(radius * 2, radius * 2),
            useCenter = false,
            style = Stroke(
                width = stroke,
                cap = cap
            )
        )
        drawCircle(
            color = ProgressBarTint,
            radius = stroke,
            center = center + Offset(
                radius * cos((startAngle + abs(appliedAngle)) * PI / 180f).toFloat(),
                radius * sin((startAngle + abs(appliedAngle)) * PI / 180f).toFloat()
            )
        )

        drawCircle(
            color = ColorPrimary,
            radius = ((stroke * 2.0) / 3.0).toFloat(),
            center = center + Offset(
                radius * cos((startAngle + abs(appliedAngle)) * PI / 180f).toFloat(),
                radius * sin((startAngle + abs(appliedAngle)) * PI / 180f).toFloat()
            )
        )

        drawLine(
            color = LightGreen,
            start = center + Offset(
                (radius - 10) * cos((startAngle + abs(appliedAngle)) * PI / 180f).toFloat(),
                (radius - 10) * sin((startAngle + abs(appliedAngle)) * PI / 180f).toFloat()
            ),
            end = center + Offset(
                (radius + 10) * cos((startAngle + abs(appliedAngle)) * PI / 180f).toFloat(),
                (radius + 10) * sin((startAngle + abs(appliedAngle)) * PI / 180f).toFloat()
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CustomViewScreenPreview() {
    StopTimerScreen(viewModel = hiltViewModel())
}