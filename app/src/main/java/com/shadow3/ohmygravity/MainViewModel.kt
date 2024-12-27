package com.shadow3.ohmygravity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class MainViewModel : ViewModel() {
    private val _sensorData = MutableStateFlow(SensorData(emptyList(), emptyList(), emptyList()))
    val sensorData: StateFlow<SensorData> = _sensorData

    private val _recordedGesture = MutableStateFlow<List<TimedSensorData>>(emptyList())
    val recordedGesture: StateFlow<List<TimedSensorData>> = _recordedGesture

    private val _matchedGesture = MutableStateFlow<String?>(null)
    val matchedGesture: StateFlow<String?> = _matchedGesture

    private val _currentGesture = MutableStateFlow<List<TimedSensorData>>(emptyList())
    val currentGesture: StateFlow<List<TimedSensorData>> = _currentGesture

    private var isGestureInProgress = false
    private val threshold = 0.1 // 静止阈值

    fun updateSensorData(x: Float, y: Float, z: Float) {
        val timestamp = System.currentTimeMillis()

        // 只记录传感器值大于阈值的数据
        if (abs(x) < threshold && abs(y) < threshold && abs(z) < threshold) {
            // 如果传感器数据很小，认为是静止状态，直接忽略
            return
        }

        // 开始新的手势录制，如果当前没有进行手势录制
        if (!isGestureInProgress) {
            isGestureInProgress = true
            _currentGesture.value = emptyList() // 清空当前手势数据
        }

        // 更新手势数据
        val newTimedData = TimedSensorData(timestamp, x, y, z)
        _currentGesture.value = currentGesture.value + newTimedData
        _sensorData.value = SensorData(
            xData = currentGesture.value.map { it.x },
            yData = currentGesture.value.map { it.y },
            zData = currentGesture.value.map { it.z }
        )
    }

    fun saveGestureAsRecord() {
        // 保存当前手势为记录，并结束手势录制
        if (isGestureInProgress) {
            _recordedGesture.value = currentGesture.value
            println("Gesture saved as record: $currentGesture")
            isGestureInProgress = false // 结束手势录制
            // 清空当前手势数据
            _currentGesture.value = emptyList()
            _sensorData.value = SensorData(emptyList(), emptyList(), emptyList())
        }
    }

    fun endGesture() {
        // 结束手势录制，不保存当前手势
        if (isGestureInProgress) {
            isGestureInProgress = false // 结束手势录制
            // 进行手势匹配
            val isMatched = isGestureMatching(currentGesture.value)
            _matchedGesture.value = if (isMatched) "Matched" else "Not Matched"
            // 清空当前手势数据
            _currentGesture.value = emptyList()
            _sensorData.value = SensorData(emptyList(), emptyList(), emptyList())
        }
    }

    private fun isGestureMatching(currentGesture: List<TimedSensorData>): Boolean {
        val recordedData = recordedGesture.value
        if (recordedData.isEmpty() || currentGesture.isEmpty()) return false

        val normalizedSize = 100
        val normalizedRecorded = normalizeGesture(recordedData, normalizedSize) ?: return false
        val normalizedCurrent = normalizeGesture(currentGesture, normalizedSize) ?: return false

        val distance = calculateDTWDistance(normalizedRecorded, normalizedCurrent)
        Log.d("GestureMatching", "DTW Distance: $distance")

        return distance < 70.0 // 调整阈值以适应具体场景
    }

    private fun normalizeGesture(
        gesture: List<TimedSensorData>,
        targetSize: Int
    ): List<TimedSensorData>? {
        if (gesture.size <= 1) return null // 如果手势数据点太少，无需缩放

        val result = mutableListOf<TimedSensorData>()
        val totalDuration = gesture.last().timestamp - gesture.first().timestamp
        val stepDuration = totalDuration / (targetSize - 1)

        var currentIndex = 0
        for (i in 0 until targetSize) {
            val targetTime = gesture.first().timestamp + i * stepDuration

            // 确保 currentIndex 不越界
            while (currentIndex < gesture.size - 1 &&
                gesture[currentIndex + 1].timestamp <= targetTime
            ) {
                currentIndex++
            }

            // 线性插值（需要确保索引合法）
            val start = gesture[currentIndex]
            val end = if (currentIndex + 1 < gesture.size) gesture[currentIndex + 1] else start
            val factor = if (end.timestamp != start.timestamp) {
                (targetTime - start.timestamp).toFloat() / (end.timestamp - start.timestamp).toFloat()
            } else {
                0f
            }

            val interpolatedX = start.x + factor * (end.x - start.x)
            val interpolatedY = start.y + factor * (end.y - start.y)
            val interpolatedZ = start.z + factor * (end.z - start.z)

            result.add(TimedSensorData(targetTime, interpolatedX, interpolatedY, interpolatedZ))
        }

        return result
    }

    private fun calculateDTWDistance(
        recordedGesture: List<TimedSensorData>,
        currentGesture: List<TimedSensorData>
    ): Double {
        val n = recordedGesture.size
        val m = currentGesture.size

        // 初始化 DTW 矩阵
        val dtw = Array(n + 1) { DoubleArray(m + 1) { Double.POSITIVE_INFINITY } }
        dtw[0][0] = 0.0

        // 填充 DTW 矩阵
        for (i in 1..n) {
            for (j in 1..m) {
                val cost = calculatePointDistance(recordedGesture[i - 1], currentGesture[j - 1])
                dtw[i][j] = cost + minOf(dtw[i - 1][j], dtw[i][j - 1], dtw[i - 1][j - 1])
            }
        }

        return dtw[n][m]
    }

    private fun calculatePointDistance(p1: TimedSensorData, p2: TimedSensorData): Float {
        val diffX = p1.x - p2.x
        val diffY = p1.y - p2.y
        val diffZ = p1.z - p2.z
        return sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ)
    }
}

data class SensorData(
    val xData: List<Float>,
    val yData: List<Float>,
    val zData: List<Float>
)

data class TimedSensorData(
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val z: Float
)
