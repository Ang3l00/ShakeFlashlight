package com.example.shakeflashlight

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
/*
Design comment:
- Problem solved: UI state, notification text and real torch status could diverge.
- Main idea: this foreground service is the single source of truth and publishes every state transition.
- Alternatives rejected:
  1) Binding Activity <-> Service with callbacks: more plumbing for a small app.
  2) Polling camera state from UI: wasted cycles and worse battery profile.
*/

class ShakeDetectorService : Service(), ShakeDetector.OnShakeListener {

    companion object {
        private const val TAG = "ShakeDetectorService"
        private const val WAKE_LOCK_TAG = "$TAG::WakeLock"

        @Volatile
        var isServiceRunning = false
            private set
    }

    private lateinit var shakeDetector: ShakeDetector
    private lateinit var cameraManager: CameraManager
    private lateinit var vibrator: Vibrator
    private var flashCameraId: String? = null
    private var isFlashOn = false
    private var wakeLock: PowerManager.WakeLock? = null
    private var isTorchCallbackRegistered = false

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId != flashCameraId) return
            if (enabled == isFlashOn) return

            isFlashOn = enabled
            publishFlashState()
            updateForegroundNotification()
            Log.d(TAG, "Flash aggiornato da app esterna: $enabled")
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId != flashCameraId || !isFlashOn) return
            isFlashOn = false
            publishFlashState()
            updateForegroundNotification()
            Log.d(TAG, "Flash non disponibile: $cameraId")
        }
    }

    override fun onCreate() {
        super.onCreate()

        shakeDetector = ShakeDetector(this)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        vibrator = getDefaultVibrator()
        flashCameraId = findFlashCameraId()
        applySensitivity(
            sensitivityPercent = ShakeSensitivitySettings.readSensitivityPercent(this),
            persist = false
        )

        createNotificationChannel()
        registerTorchCallback()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            AppContract.ACTION_STOP_SERVICE -> {
                // Why: explicit stop action guarantees graceful cleanup before process teardown.
                stopSelf()
                START_NOT_STICKY
            }

            AppContract.ACTION_TOGGLE_FLASH -> {
                ensureServiceStarted()
                toggleFlash(trigger = "notification")
                START_STICKY
            }

            AppContract.ACTION_UPDATE_SENSITIVITY -> {
                val updatedSensitivity = intent.getFloatExtra(
                    AppContract.EXTRA_SENSITIVITY_PERCENT,
                    ShakeSensitivitySettings.DEFAULT_SENSITIVITY_PERCENT
                )
                applySensitivity(updatedSensitivity, persist = true)
                if (isServiceRunning) {
                    START_STICKY
                } else {
                    stopSelf()
                    START_NOT_STICKY
                }
            }

            else -> {
                ensureServiceStarted()
                START_STICKY
            }
        }
    }

    override fun onDestroy() {
        stopShakeDetection()
        turnOffFlashIfNeeded()
        unregisterTorchCallback()
        releaseWakeLock()

        isServiceRunning = false
        publishServiceState()
        publishFlashState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onShake() {
        toggleFlash(trigger = "shake")
        vibrateFeedback()
    }

    private fun ensureServiceStarted() {
        if (isServiceRunning) {
            updateForegroundNotification()
            return
        }

        if (flashCameraId == null) {
            Log.e(TAG, "Nessuna fotocamera con flash disponibile.")
            stopSelf()
            return
        }

        startForeground(AppContract.NOTIFICATION_ID, buildNotification())
        shakeDetector.start(this)
        acquireWakeLock()

        isServiceRunning = true
        publishServiceState()
        publishFlashState()
        Log.d(TAG, "Servizio avviato")
    }

    private fun applySensitivity(sensitivityPercent: Float, persist: Boolean) {
        val safeValue = ShakeSensitivitySettings.clampSensitivityPercent(sensitivityPercent)
        shakeDetector.setSensitivityPercent(safeValue)
        if (persist) {
            ShakeSensitivitySettings.saveSensitivityPercent(this, safeValue)
        }
        Log.d(TAG, "Sensibilità applicata: $safeValue%")
    }

    private fun stopShakeDetection() {
        shakeDetector.stop()
    }

    private fun toggleFlash(trigger: String) {
        val cameraId = flashCameraId ?: return
        val nextState = !isFlashOn

        if (!setFlashState(cameraId, nextState)) return

        isFlashOn = nextState
        publishFlashState()
        updateForegroundNotification()
        Log.d(TAG, "Flash ${if (isFlashOn) "acceso" else "spento"} da $trigger")
    }

    private fun turnOffFlashIfNeeded() {
        val cameraId = flashCameraId ?: return
        if (!isFlashOn) return
        if (!setFlashState(cameraId, enabled = false)) return
        isFlashOn = false
    }

    private fun setFlashState(cameraId: String, enabled: Boolean): Boolean {
        return try {
            cameraManager.setTorchMode(cameraId, enabled)
            true
        } catch (securityException: SecurityException) {
            Log.e(TAG, "Permesso camera mancante.", securityException)
            false
        } catch (cameraException: CameraAccessException) {
            Log.w(TAG, "Flash non disponibile (occupato da altra app).", cameraException)
            false
        } catch (error: Exception) {
            Log.e(TAG, "Errore imprevisto nel controllo flash.", error)
            false
        }
    }

    private fun findFlashCameraId(): String? {
        return try {
            cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (error: Exception) {
            Log.e(TAG, "Errore nel rilevare la fotocamera con flash.", error)
            null
        }
    }

    private fun registerTorchCallback() {
        if (isTorchCallbackRegistered) return
        try {
            cameraManager.registerTorchCallback(torchCallback, null)
            isTorchCallbackRegistered = true
        } catch (error: Exception) {
            Log.e(TAG, "Impossibile registrare TorchCallback.", error)
        }
    }

    private fun unregisterTorchCallback() {
        if (!isTorchCallbackRegistered) return
        try {
            cameraManager.unregisterTorchCallback(torchCallback)
            isTorchCallbackRegistered = false
        } catch (error: Exception) {
            Log.e(TAG, "Impossibile deregistrare TorchCallback.", error)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            AppContract.NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val flashStateText = getString(
            if (isFlashOn) R.string.notification_flash_on else R.string.notification_flash_off
        )

        return NotificationCompat.Builder(this, AppContract.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_body, flashStateText))
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(buildOpenAppPendingIntent())
            .addAction(
                0,
                getString(R.string.notification_action_toggle_flash),
                buildServicePendingIntent(AppContract.ACTION_TOGGLE_FLASH, requestCode = 11)
            )
            .addAction(
                0,
                getString(R.string.notification_action_stop_service),
                buildServicePendingIntent(AppContract.ACTION_STOP_SERVICE, requestCode = 12)
            )
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun buildOpenAppPendingIntent(): PendingIntent {
        val openAppIntent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            10,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildServicePendingIntent(action: String, requestCode: Int): PendingIntent {
        val serviceIntent = Intent(this, ShakeDetectorService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            requestCode,
            serviceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateForegroundNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(AppContract.NOTIFICATION_ID, buildNotification())
    }

    private fun publishServiceState() {
        val stateIntent = Intent(AppContract.ACTION_SERVICE_STATE_CHANGED).apply {
            setPackage(packageName)
            putExtra(AppContract.EXTRA_IS_SERVICE_RUNNING, isServiceRunning)
        }
        sendBroadcast(stateIntent)
    }

    private fun publishFlashState() {
        val flashIntent = Intent(AppContract.ACTION_FLASH_STATE_CHANGED).apply {
            setPackage(packageName)
            putExtra(AppContract.EXTRA_IS_FLASH_ON, isFlashOn)
        }
        sendBroadcast(flashIntent)
    }

    private fun getDefaultVibrator(): Vibrator {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        return vibratorManager.defaultVibrator
    }

    private fun vibrateFeedback() {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG).apply {
            setReferenceCounted(false)
            acquire()
        }
    }

    private fun releaseWakeLock() {
        val currentWakeLock = wakeLock ?: return
        if (currentWakeLock.isHeld) {
            currentWakeLock.release()
        }
        wakeLock = null
    }
}