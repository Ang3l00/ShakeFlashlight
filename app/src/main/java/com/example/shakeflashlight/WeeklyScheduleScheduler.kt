package com.example.shakeflashlight

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import java.util.Calendar

/*
Design comment:
- Problem solved: weekly automation needs durable start/stop triggers that survive process death.
- Main idea: schedule one alarm per selected day per event type (start/stop), then reschedule that
  event for the next week when it fires.
- Alternatives rejected:
  1) In-app timers only while Activity is open: unreliable in background and after app kill.
  2) Polling every minute with WorkManager: unnecessary battery overhead for fixed clock events.
*/
object WeeklyScheduleScheduler {
    const val ACTION_WEEKLY_START_ALARM =
        "com.example.shakeflashlight.action.WEEKLY_START_ALARM"
    const val ACTION_WEEKLY_STOP_ALARM =
        "com.example.shakeflashlight.action.WEEKLY_STOP_ALARM"

    private const val EXTRA_SOURCE_DAY = "extra_source_day"
    private const val EXTRA_START_MINUTES = "extra_start_minutes"
    private const val EXTRA_END_MINUTES = "extra_end_minutes"

    private const val EVENT_START = 1
    private const val EVENT_STOP = 2

    fun scheduleAll(context: Context, config: WeeklyScheduleSettings.WeeklyScheduleConfig) {
        cancelAll(context)
        if (!config.enabled || !WeeklyScheduleSettings.canBeEnabled(config)) return

        for (sourceDay in config.selectedDays) {
            scheduleEvent(context, sourceDay, EVENT_START, config)
            scheduleEvent(context, sourceDay, EVENT_STOP, config)
        }
    }

    fun cancelAll(context: Context) {
        for (sourceDay in Calendar.SUNDAY..Calendar.SATURDAY) {
            cancelEvent(context, sourceDay, EVENT_START)
            cancelEvent(context, sourceDay, EVENT_STOP)
        }
    }

    fun handleAlarm(context: Context, intent: Intent) {
        val eventType = eventTypeFromAction(intent.action) ?: return
        val sourceDay = intent.getIntExtra(EXTRA_SOURCE_DAY, -1)
        val startMinutes = intent.getIntExtra(EXTRA_START_MINUTES, -1)
        val endMinutes = intent.getIntExtra(EXTRA_END_MINUTES, -1)
        if (sourceDay !in Calendar.SUNDAY..Calendar.SATURDAY) return
        if (startMinutes !in 0 until WeeklyScheduleSettings.MINUTES_PER_DAY) return
        if (endMinutes !in 0 until WeeklyScheduleSettings.MINUTES_PER_DAY) return

        val latestConfig = WeeklyScheduleSettings.readConfig(context)
        val alarmStillValid = latestConfig.enabled &&
            latestConfig.selectedDays.contains(sourceDay) &&
            latestConfig.startMinutes == startMinutes &&
            latestConfig.endMinutes == endMinutes &&
            WeeklyScheduleSettings.canBeEnabled(latestConfig)
        if (!alarmStillValid) return

        when (eventType) {
            EVENT_START -> {
                val startIntent = Intent(context, ShakeDetectorService::class.java).apply {
                    action = AppContract.ACTION_START_SERVICE
                }
                ContextCompat.startForegroundService(context, startIntent)
            }

            EVENT_STOP -> {
                context.stopService(Intent(context, ShakeDetectorService::class.java))
            }
        }

        scheduleEvent(context, sourceDay, eventType, latestConfig)
    }

    private fun scheduleEvent(
        context: Context,
        sourceDay: Int,
        eventType: Int,
        config: WeeklyScheduleSettings.WeeklyScheduleConfig
    ) {
        val triggerAtMillis = nextTriggerTimeMillis(sourceDay, eventType, config)
        val pendingIntent = buildPendingIntent(
            context = context,
            sourceDay = sourceDay,
            eventType = eventType,
            startMinutes = config.startMinutes,
            endMinutes = config.endMinutes,
            noCreate = false
        ) ?: return

        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    private fun cancelEvent(context: Context, sourceDay: Int, eventType: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = buildPendingIntent(
            context = context,
            sourceDay = sourceDay,
            eventType = eventType,
            startMinutes = WeeklyScheduleSettings.DEFAULT_START_MINUTES,
            endMinutes = WeeklyScheduleSettings.DEFAULT_END_MINUTES,
            noCreate = true
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun nextTriggerTimeMillis(
        sourceDay: Int,
        eventType: Int,
        config: WeeklyScheduleSettings.WeeklyScheduleConfig,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val triggerDay = when (eventType) {
            EVENT_START -> sourceDay
            EVENT_STOP -> {
                if (config.isCrossMidnight()) {
                    WeeklyScheduleSettings.nextDay(sourceDay)
                } else {
                    sourceDay
                }
            }

            else -> sourceDay
        }
        val triggerMinutes = if (eventType == EVENT_START) {
            config.startMinutes
        } else {
            config.endMinutes
        }
        return nextOccurrenceOfDayAndMinutes(triggerDay, triggerMinutes, nowMillis)
    }

    private fun nextOccurrenceOfDayAndMinutes(
        dayOfWeek: Int,
        minuteOfDay: Int,
        nowMillis: Long
    ): Long {
        val nowCalendar = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val next = Calendar.getInstance().apply { timeInMillis = nowMillis }
        next.set(Calendar.SECOND, 0)
        next.set(Calendar.MILLISECOND, 0)

        val currentDay = nowCalendar.get(Calendar.DAY_OF_WEEK)
        val daysUntil = (dayOfWeek - currentDay + 7) % 7
        next.add(Calendar.DAY_OF_YEAR, daysUntil)
        next.set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
        next.set(Calendar.MINUTE, minuteOfDay % 60)

        if (next.timeInMillis <= nowMillis) {
            next.add(Calendar.DAY_OF_YEAR, 7)
        }
        return next.timeInMillis
    }

    private fun eventTypeFromAction(action: String?): Int? {
        return when (action) {
            ACTION_WEEKLY_START_ALARM -> EVENT_START
            ACTION_WEEKLY_STOP_ALARM -> EVENT_STOP
            else -> null
        }
    }

    private fun buildPendingIntent(
        context: Context,
        sourceDay: Int,
        eventType: Int,
        startMinutes: Int,
        endMinutes: Int,
        noCreate: Boolean
    ): PendingIntent? {
        val action = when (eventType) {
            EVENT_START -> ACTION_WEEKLY_START_ALARM
            EVENT_STOP -> ACTION_WEEKLY_STOP_ALARM
            else -> return null
        }
        val requestCode = sourceDay * 10 + eventType
        val alarmIntent = Intent(context, WeeklyScheduleAlarmReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_SOURCE_DAY, sourceDay)
            putExtra(EXTRA_START_MINUTES, startMinutes)
            putExtra(EXTRA_END_MINUTES, endMinutes)
        }
        val flags = if (noCreate) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(context, requestCode, alarmIntent, flags)
    }
}
