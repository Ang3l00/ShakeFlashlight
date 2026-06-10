package com.example.shakeflashlight

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WeeklyScheduleAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WeeklyScheduleScheduler.handleAlarm(context, intent)
    }
}
