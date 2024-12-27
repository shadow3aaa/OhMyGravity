package com.shadow3.ohmygravity.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.shadow3.ohmygravity.SensorData

@Composable
fun SensorDataVisualizer(sensorData: SensorData) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val xStep = canvasWidth / (sensorData.xData.size.coerceAtLeast(1))
        val yScale = canvasHeight / 20f

        fun mapToCanvas(index: Int, value: Float): androidx.compose.ui.geometry.Offset {
            val x = index * xStep
            val y = canvasHeight / 2 - value * yScale
            return androidx.compose.ui.geometry.Offset(x, y)
        }

        val xPath = Path().apply {
            if (sensorData.xData.isNotEmpty()) moveTo(0f, canvasHeight / 2 - sensorData.xData.first() * yScale)
            sensorData.xData.forEachIndexed { index, value ->
                lineTo(mapToCanvas(index, value).x, mapToCanvas(index, value).y)
            }
        }
        drawPath(path = xPath, color = Color.Red, style = Stroke(width = 4f))

        val yPath = Path().apply {
            if (sensorData.yData.isNotEmpty()) moveTo(0f, canvasHeight / 2 - sensorData.yData.first() * yScale)
            sensorData.yData.forEachIndexed { index, value ->
                lineTo(mapToCanvas(index, value).x, mapToCanvas(index, value).y)
            }
        }
        drawPath(path = yPath, color = Color.Green, style = Stroke(width = 4f))

        val zPath = Path().apply {
            if (sensorData.zData.isNotEmpty()) moveTo(0f, canvasHeight / 2 - sensorData.zData.first() * yScale)
            sensorData.zData.forEachIndexed { index, value ->
                lineTo(mapToCanvas(index, value).x, mapToCanvas(index, value).y)
            }
        }
        drawPath(path = zPath, color = Color.Blue, style = Stroke(width = 4f))

        drawLine(
            color = Color.Gray,
            start = androidx.compose.ui.geometry.Offset(0f, canvasHeight / 2),
            end = androidx.compose.ui.geometry.Offset(canvasWidth, canvasHeight / 2),
            strokeWidth = 2f
        )
    }
}
