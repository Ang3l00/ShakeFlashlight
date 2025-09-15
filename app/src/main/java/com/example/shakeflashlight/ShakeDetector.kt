package com.example.shakeflashlight

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

class ShakeDetector(private val listener: OnShakeListener) : SensorEventListener {

    companion object {
        private const val TAG = "ShakeDetector"
        private const val SHAKE_THRESHOLD = 60.0f // Soglia di accelerazione per rilevare l'agitazione
        private const val SHAKE_WAIT_TIME_MS = 1500 // Tempo di attesa tra rilevamenti (debounce)
    }

    interface OnShakeListener {
        fun onShake()
    }

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeTime = 0L

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
        Log.d(TAG, "ShakeDetector fermato")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Calcola l'accelerazione totale
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH

                // Verifica se supera la soglia e rispetta il debounce
                if (acceleration > SHAKE_THRESHOLD) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastShakeTime > SHAKE_WAIT_TIME_MS) {
                        lastShakeTime = currentTime
                        listener.onShake()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Non necessario per questo caso d'uso
    }
}