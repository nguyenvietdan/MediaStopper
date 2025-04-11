package com.monkey.mediastopper.presentations.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import kotlin.math.min

class FillMinDimensionModifier(val maxSize: Int? = null): LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val minDimension = min(constraints.maxWidth, constraints.maxHeight).let {
            if (maxSize != null) min(it, maxSize) else it
        }
        val placeable = measurable.measure(
            Constraints(minDimension, minDimension, minDimension, minDimension)
        )


        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }
}

fun Modifier.fillMinDimension(maxSize: Int? = null) = this.then(FillMinDimensionModifier(maxSize))