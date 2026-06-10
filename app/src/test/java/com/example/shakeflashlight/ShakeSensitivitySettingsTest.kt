package com.example.shakeflashlight

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeSensitivitySettingsTest {

    @Test
    fun thresholdGetsLowerWhenSensitivityGetsHigher() {
        val lowSensitivityThreshold = ShakeSensitivitySettings.thresholdForSensitivityPercent(0f)
        val mediumSensitivityThreshold = ShakeSensitivitySettings.thresholdForSensitivityPercent(50f)
        val highSensitivityThreshold = ShakeSensitivitySettings.thresholdForSensitivityPercent(100f)

        assertTrue(lowSensitivityThreshold > mediumSensitivityThreshold)
        assertTrue(mediumSensitivityThreshold > highSensitivityThreshold)
    }

    @Test
    fun clampKeepsSensitivityInsideExpectedRange() {
        assertEquals(0f, ShakeSensitivitySettings.clampSensitivityPercent(-20f), 0f)
        assertEquals(100f, ShakeSensitivitySettings.clampSensitivityPercent(120f), 0f)
        assertEquals(35f, ShakeSensitivitySettings.clampSensitivityPercent(35f), 0f)
    }
}
