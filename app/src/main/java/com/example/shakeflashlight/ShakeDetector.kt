package com.example.shakeflashlight

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class ShakeDetector(private val listener: OnShakeListener) : SensorEventListener {

    companion object {
        private const val TAG = "ShakeDetector"
    }

    interface OnShakeListener {
        fun onShake()
    }
    private val signalProcessor = ShakeSignalProcessor()

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    fun start(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer != null) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            Log.d(TAG, "ShakeDetector avviato")
        } else {
            Log.e(TAG, "Accelerometro non disponibile")
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
        accelerometer = null
        signalProcessor.reset()
        Log.d(TAG, "ShakeDetector fermato")
    }

    fun setSensitivityPercent(sensitivityPercent: Float) {
        val thresholdG = ShakeSensitivitySettings.thresholdForSensitivityPercent(sensitivityPercent)
        signalProcessor.updateThresholdG(thresholdG)
        Log.d(TAG, "Sensibilità aggiornata: $sensitivityPercent% (thresholdG=$thresholdG)")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorEvent = event ?: return
        if (sensorEvent.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val shouldTrigger = signalProcessor.onSample(
            timestampMs = sensorTimestampToMillis(sensorEvent.timestamp),
            x = sensorEvent.values[0],
            y = sensorEvent.values[1],
            z = sensorEvent.values[2]
        )

        if (shouldTrigger) {
            listener.onShake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Non necessario per questo caso d'uso
    }

    private fun sensorTimestampToMillis(sensorTimestampNs: Long): Long {
        // Why: SensorEvent timestamp is monotonic (not wall-clock), perfect for gesture timing.
        return sensorTimestampNs / 1_000_000L
    }
}