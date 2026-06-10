package com.example.shakeflashlight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }
        val config = WeeklyScheduleSettings.readConfig(context)
        WeeklyScheduleScheduler.scheduleAll(context, config)
        if (config.enabled && WeeklyScheduleSettings.isActiveNow(config)) {
            val startIntent = Intent(context, ShakeDetectorService::class.java).apply {
                this.action = AppContract.ACTION_START_SERVICE
            }
            ContextCompat.startForegroundService(context, startIntent)
        }
    }
}
