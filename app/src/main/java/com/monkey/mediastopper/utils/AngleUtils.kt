package com.monkey.mediastopper.utils

import android.util.Log
import androidx.compose.ui.geometry.Offset
import com.monkey.mediastopper.utils.Constants.TAG
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

object AngleUtils {

    fun calculateAngle(
        center: Offset,
        x: Float,
        y: Float,
        lastAngle: Double,
        radius: Float,
        stroke: Float,
        swipeAngle: Float,
        onSuccess: (Double) -> Unit = {}
    ) {
        val distance = calculateDistance(center.x, center.y, x, y)
        Log.e(TAG, "calculateAngle: distance $distance radius $radius $stroke")
        if (distance < radius - stroke || distance > radius + stroke) return

        var appliedAngle = if (center.x > x && center.y > y) {
            270 + deltaAngle(center.x - x, center.y - y)
        } else {
            90 - deltaAngle(x - center.x, center.y - y)
        }.coerceAtMost(swipeAngle.toDouble())

        val diff = abs(lastAngle - appliedAngle)
        if (diff > 180) {
            appliedAngle = if (appliedAngle < 180) {
                swipeAngle.toDouble()
            } else {
                0.0
            }
        }
        onSuccess(appliedAngle)
    }

    fun deltaAngle(x: Float, y: Float): Double {
        return Math.toDegrees(atan2(y.toDouble(), x.toDouble()))
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float) =
        sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))

    fun getRotationAngle(currentPosition: Offset, center: Offset): Double {
        val (dx, dy) = currentPosition - center
        val theta = atan2(dy, dx).toDouble()

        var angle = Math.toDegrees(theta)

        if (angle < 0) {
            angle += 360.0
        }
        return angle
    }
}