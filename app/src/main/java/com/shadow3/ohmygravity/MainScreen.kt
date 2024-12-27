package com.shadow3.ohmygravity

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shadow3.ohmygravity.components.SensorDataVisualizer

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val sensorData by viewModel.sensorData.collectAsState()
    val matchedGesture by viewModel.matchedGesture.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(75.dp))
        Text(
            text = "Sensor Data Visualization",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(25.dp))
        Box(
            modifier = Modifier
                .size(300.dp)
                .border(2.dp, Color.Black)
        ) {
            SensorDataVisualizer(sensorData)
        }

        Spacer(modifier = Modifier.height(25.dp))

        Text(text = "x: ${sensorData.xData.lastOrNull()}")
        Text(text = "y: ${sensorData.yData.lastOrNull()}")
        Text(text = "z: ${sensorData.zData.lastOrNull()}")

        Spacer(modifier = Modifier.height(25.dp))

        Button(onClick = { viewModel.saveGestureAsRecord() }) {
            Text("Save and end gesture")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 结束手势按钮
        Button(onClick = { viewModel.endGesture() }) {
            Text("End gesture")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Result: $matchedGesture")
    }

    GyroscopeSensor { x, y, z ->
        viewModel.updateSensorData(x, y, z)
    }
}


@Composable
fun GyroscopeSensor(
    onSensorDataChanged: (x: Float, y: Float, z: Float) -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(SensorManager::class.java) }
    val gyroscope = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE) }
    val listener = rememberUpdatedState(onSensorDataChanged)

    DisposableEffect(sensorManager, gyroscope) {
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let {
                    listener.value(it[0], it[1], it[2])
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        gyroscope?.let {
            sensorManager?.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        onDispose {
            sensorManager?.unregisterListener(sensorEventListener)
        }
    }
}
