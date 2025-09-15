package com.example.shakeflashlight

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.shakeflashlight.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val CAMERA_PERMISSION_REQUEST = 100

    // BroadcastReceiver DENTRO la classe
    private val flashStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isFlashOn = intent?.getBooleanExtra("isFlashOn", false) ?: false
            updateFlashStatus(isFlashOn)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()

        // Registra il receiver per stato flash
        val filter = IntentFilter("com.example.shakeflashlight.FLASH_STATUS")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(flashStatusReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(flashStatusReceiver, filter)
        }

        updateServiceStatus()
    }

    override fun onPause() {
        super.onPause()

        // Deregistra il receiver
        try {
            unregisterReceiver(flashStatusReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver non registrato
        }
    }

    private fun setupUI() {
        // Configurazione del switch principale
        binding.switchService.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (hasAllPermissions()) {
                    startShakeService()
                } else {
                    binding.switchService.isChecked = false
                    requestPermissions()
                }
            } else {
                stopShakeService()
            }
        }

        // Stato iniziale del servizio
        updateServiceStatus()
        updateFlashStatus(false)
    }

    private fun hasAllPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        return cameraPermission && notificationPermission
    }

    private fun checkPermissions() {
        if (!hasAllPermissions()) {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), CAMERA_PERMISSION_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permessi concessi!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permessi necessari per il funzionamento", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startShakeService() {
        val serviceIntent = Intent(this, ShakeDetectorService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        updateServiceStatus()
    }

    private fun stopShakeService() {
        val serviceIntent = Intent(this, ShakeDetectorService::class.java)
        stopService(serviceIntent)
        updateServiceStatus()
        updateFlashStatus(false)
    }

    private fun updateServiceStatus() {
        val isRunning = ShakeDetectorService.isServiceRunning
        binding.switchService.isChecked = isRunning
        binding.textServiceStatus.text = if (isRunning) {
            "Servizio attivo - Agita il dispositivo!"
        } else {
            "Servizio non attivo"
        }
        binding.cardServiceStatus.setCardBackgroundColor(
            ContextCompat.getColor(
                this,
                if (isRunning) android.R.color.holo_green_light
                else android.R.color.holo_red_light
            )
        )
    }

    private fun updateFlashStatus(isOn: Boolean) {
        binding.textFlashStatus.text = if (isOn) {
            "Flash LED: ACCESO"
        } else {
            "Flash LED: SPENTO"
        }
        binding.cardFlashStatus.setCardBackgroundColor(
            ContextCompat.getColor(
                this,
                if (isOn) android.R.color.holo_orange_light
                else android.R.color.darker_gray
            )
        )
    }
}