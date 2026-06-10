package com.example.shakeflashlight

import android.content.Context

/*
Design comment:
- Problem solved: sensitivity value was implicit in code and impossible to tune per user/device.
- Main idea: persist a user-facing percentage and map it to detector threshold in one place.
- Alternatives rejected:
  1) Persist raw thresholdG directly: less intuitive and harder to expose safely in UI.
  2) Keep only in-memory setting: user loses calibration after app restart.
*/
object ShakeSensitivitySettings {
    private const val PREFS_NAME = "shake_flashlight_prefs"
    private const val KEY_SENSITIVITY_PERCENT = "sensitivity_percent"

    const val DEFAULT_SENSITIVITY_PERCENT = 60f
    const val MIN_SENSITIVITY_PERCENT = 0f
    const val MAX_SENSITIVITY_PERCENT = 100f

    private const val MIN_THRESHOLD_G = 1.6f
    private const val MAX_THRESHOLD_G = 2.8f

    fun readSensitivityPercent(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedValue = prefs.getFloat(KEY_SENSITIVITY_PERCENT, DEFAULT_SENSITIVITY_PERCENT)
        return clampSensitivityPercent(storedValue)
    }

    fun saveSensitivityPercent(context: Context, sensitivityPercent: Float) {
        val safeValue = clampSensitivityPercent(sensitivityPercent)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_SENSITIVITY_PERCENT, safeValue).apply()
    }

    fun clampSensitivityPercent(value: Float): Float {
        return value.coerceIn(MIN_SENSITIVITY_PERCENT, MAX_SENSITIVITY_PERCENT)
    }

    fun thresholdForSensitivityPercent(sensitivityPercent: Float): Float {
        val normalized = clampSensitivityPercent(sensitivityPercent) / MAX_SENSITIVITY_PERCENT
        return MAX_THRESHOLD_G - ((MAX_THRESHOLD_G - MIN_THRESHOLD_G) * normalized)
    }
}
