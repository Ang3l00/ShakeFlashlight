package com.example.shakeflashlight

import android.content.Context
import java.util.Calendar

/*
Design comment:
- Problem solved: users need deterministic automatic activation/deactivation by weekday and time range.
- Main idea: keep one persisted weekly config (days + start/end + enabled) and provide pure helpers
  for schedule validation and "is active now" evaluation.
- Alternatives rejected:
  1) Hardcoding weekdays/hours in AlarmReceiver: no reusable source of truth and hard to test.
  2) Persisting each day as separate key-only flags without a config model: more fragile read/write logic.
*/
object WeeklyScheduleSettings {
    private const val PREFS_NAME = "shake_flashlight_prefs"
    private const val KEY_WEEKLY_ENABLED = "weekly_schedule_enabled"
    private const val KEY_WEEKLY_DAYS_MASK = "weekly_schedule_days_mask"
    private const val KEY_WEEKLY_START_MINUTES = "weekly_schedule_start_minutes"
    private const val KEY_WEEKLY_END_MINUTES = "weekly_schedule_end_minutes"

    const val MINUTES_PER_DAY = 24 * 60
    const val DEFAULT_START_MINUTES = 8 * 60
    const val DEFAULT_END_MINUTES = 22 * 60

    private val defaultSelectedDays = setOf(
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY
    )

    val orderedDaysMondayFirst = listOf(
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY,
        Calendar.SUNDAY
    )

    data class WeeklyScheduleConfig(
        val enabled: Boolean,
        val selectedDays: Set<Int>,
        val startMinutes: Int,
        val endMinutes: Int
    ) {
        fun isCrossMidnight(): Boolean {
            return endMinutes <= startMinutes
        }
    }

    fun readConfig(context: Context): WeeklyScheduleConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedMask = prefs.getInt(KEY_WEEKLY_DAYS_MASK, daySetToMask(defaultSelectedDays))
        return WeeklyScheduleConfig(
            enabled = prefs.getBoolean(KEY_WEEKLY_ENABLED, false),
            selectedDays = maskToDaySet(storedMask),
            startMinutes = clampMinutes(prefs.getInt(KEY_WEEKLY_START_MINUTES, DEFAULT_START_MINUTES)),
            endMinutes = clampMinutes(prefs.getInt(KEY_WEEKLY_END_MINUTES, DEFAULT_END_MINUTES))
        )
    }

    fun saveConfig(context: Context, config: WeeklyScheduleConfig) {
        val safeDays = config.selectedDays.filter { it in Calendar.SUNDAY..Calendar.SATURDAY }.toSet()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_WEEKLY_ENABLED, config.enabled)
            .putInt(KEY_WEEKLY_DAYS_MASK, daySetToMask(safeDays))
            .putInt(KEY_WEEKLY_START_MINUTES, clampMinutes(config.startMinutes))
            .putInt(KEY_WEEKLY_END_MINUTES, clampMinutes(config.endMinutes))
            .apply()
    }

    fun canBeEnabled(config: WeeklyScheduleConfig): Boolean {
        if (config.selectedDays.isEmpty()) return false
        return config.startMinutes != config.endMinutes
    }

    fun clampMinutes(minutes: Int): Int {
        return minutes.coerceIn(0, MINUTES_PER_DAY - 1)
    }

    fun isActiveNow(config: WeeklyScheduleConfig, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!config.enabled || !canBeEnabled(config)) return false
        val calendar = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val minuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        return isActiveAt(config, dayOfWeek, minuteOfDay)
    }

    fun isActiveAt(config: WeeklyScheduleConfig, dayOfWeek: Int, minuteOfDay: Int): Boolean {
        if (!canBeEnabled(config)) return false
        val safeMinute = clampMinutes(minuteOfDay)
        val crossesMidnight = config.isCrossMidnight()
        val isCurrentDaySelected = config.selectedDays.contains(dayOfWeek)

        if (!crossesMidnight) {
            return isCurrentDaySelected &&
                safeMinute >= config.startMinutes &&
                safeMinute < config.endMinutes
        }

        if (isCurrentDaySelected && safeMinute >= config.startMinutes) return true
        val previousDay = previousDay(dayOfWeek)
        return config.selectedDays.contains(previousDay) && safeMinute < config.endMinutes
    }

    fun nextDay(dayOfWeek: Int): Int {
        return if (dayOfWeek == Calendar.SATURDAY) Calendar.SUNDAY else dayOfWeek + 1
    }

    fun previousDay(dayOfWeek: Int): Int {
        return if (dayOfWeek == Calendar.SUNDAY) Calendar.SATURDAY else dayOfWeek - 1
    }

    private fun daySetToMask(days: Set<Int>): Int {
        var mask = 0
        for (day in days) {
            if (day in Calendar.SUNDAY..Calendar.SATURDAY) {
                mask = mask or (1 shl day)
            }
        }
        return mask
    }

    private fun maskToDaySet(mask: Int): Set<Int> {
        val decoded = mutableSetOf<Int>()
        for (day in Calendar.SUNDAY..Calendar.SATURDAY) {
            if ((mask and (1 shl day)) != 0) decoded += day
        }
        return decoded
    }
}
