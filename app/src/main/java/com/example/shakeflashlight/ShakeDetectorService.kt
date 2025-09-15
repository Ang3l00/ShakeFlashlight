package com.example.shakeflashlight

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat

class ShakeDetectorService : Service(), ShakeDetector.OnShakeListener {

    companion object {
        private const val TAG = "ShakeDetectorService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "shake_detector_channel"
        var isServiceRunning = false
            private set
    }

    private lateinit var shakeDetector: ShakeDetector
    private lateinit var cameraManager: CameraManager
    private lateinit var vibrator: Vibrator
    private var cameraId: String? = null
    private var isFlashOn = false
    private var wakeLock: PowerManager.WakeLock? = null

    // NUOVO - TorchCallback per monitorare stato flash
    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            super.onTorchModeChanged(cameraId, enabled)

            // Solo se riguarda la nostra camera con flash
            if (cameraId == this@ShakeDetectorService.cameraId) {
                Log.d(TAG, "Stato torcia cambiato esternamente: $enabled")

                // Aggiorna solo se diverso dal nostro stato interno
                if (enabled != isFlashOn) {
                    isFlashOn = enabled
                    updateNotificationAndApp()
                }
            }
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            super.onTorchModeUnavailable(cameraId)
            Log.d(TAG, "Torcia non disponibile: $cameraId")

            if (cameraId == this@ShakeDetectorService.cameraId && isFlashOn) {
                isFlashOn = false
                updateNotificationAndApp()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        shakeDetector = ShakeDetector(this)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // Solo Android 12+ - Codice semplificato
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibrator = vibratorManager.defaultVibrator

        // Ottieni l'ID della fotocamera posteriore
        try {
            val cameraIds = cameraManager.cameraIdList
            for (id in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE)
                if (hasFlash == true) {
                    cameraId = id
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Errore nell'ottenere l'ID della fotocamera", e)
        }

        // Crea il canale di notifica
        createNotificationChannel()

        // NUOVO - Registra TorchCallback per monitoraggio
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager.registerTorchCallback(torchCallback, null)
            Log.d(TAG, "TorchCallback registrato")
        }

        // Acquisisce wake lock per mantenere attivi i sensori
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "$TAG::WakeLock"
        )
        wakeLock?.acquire()

        Log.d(TAG, "Servizio creato")
    }

    // NUOVO - Metodo per aggiornamento sincronizzato
    private fun updateNotificationAndApp() {
        // Aggiorna notifica
        val notification = createNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        // Informa l'Activity
        val intent = Intent("com.example.shakeflashlight.FLASH_STATUS")
        intent.putExtra("isFlashOn", isFlashOn)
        sendBroadcast(intent)

        Log.d(TAG, "Stato sincronizzato: Flash ${if (isFlashOn) "ACCESO" else "SPENTO"}")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Avvia la notifica foreground
        startForeground(NOTIFICATION_ID, createNotification())

        // Avvia il rilevamento dell'agitazione
        shakeDetector.start(this)
        isServiceRunning = true

        Log.d(TAG, "Servizio avviato")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        // Ferma il rilevamento
        shakeDetector.stop()
        isServiceRunning = false

        // NUOVO - Deregistra TorchCallback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager.unregisterTorchCallback(torchCallback)
            Log.d(TAG, "TorchCallback deregistrato")
        }

        // Spegne il flash se acceso
        if (isFlashOn) {
            toggleFlash()
        }

        // Rilascia il wake lock
        wakeLock?.release()

        Log.d(TAG, "Servizio distrutto")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onShake() {
        // Callback chiamata quando viene rilevata un'agitazione
        toggleFlash()

        // Vibrazione semplificata - solo Android 12+
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))

        Log.d(TAG, "Agitazione rilevata - Flash ${if (isFlashOn) "acceso" else "spento"}")
    }

    private fun toggleFlash() {
        cameraId?.let { id ->
            try {
                cameraManager.setTorchMode(id, !isFlashOn)
                isFlashOn = !isFlashOn

                val intent = Intent("com.example.shakeflashlight.FLASH_STATUS")
                intent.putExtra("isFlashOn", isFlashOn)
                sendBroadcast(intent)

                // Aggiorna la notifica
                val notification = createNotification()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)

                Log.d(TAG, "Flash ${if (isFlashOn) "acceso" else "spento"} dalla nostra app")

            } catch (e: CameraAccessException) {
                Log.w(TAG, "Flash occupato da altra app", e)
            } catch (e: Exception) {
                Log.e(TAG, "Errore nel controllo del flash", e)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Shake Detector Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Servizio per rilevare l'agitazione del dispositivo"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Shake Flashlight")
            .setContentText("Flash: ${if (isFlashOn) "ACCESO" else "SPENTO"} - Agita per cambiare")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}