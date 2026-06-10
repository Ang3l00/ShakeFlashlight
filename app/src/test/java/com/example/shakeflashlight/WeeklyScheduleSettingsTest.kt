package com.example.shakeflashlight

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class WeeklyScheduleSettingsTest {

    @Test
    fun scheduleCannotBeEnabledWithoutDaysOrWithSameTimes() {
        val noDays = WeeklyScheduleSettings.WeeklyScheduleConfig(
            enabled = true,
            selectedDays = emptySet(),
            startMinutes = 8 * 60,
            endMinutes = 22 * 60
        )
        val sameTimes = WeeklyScheduleSettings.WeeklyScheduleConfig(
            enabled = true,
            selectedDays = setOf(Calendar.MONDAY),
            startMinutes = 8 * 60,
            endMinutes = 8 * 60
        )

        assertFalse(WeeklyScheduleSettings.canBeEnabled(noDays))
        assertFalse(WeeklyScheduleSettings.canBeEnabled(sameTimes))
    }

    @Test
    fun activeWindowWorksForSameDayRange() {
        val config = WeeklyScheduleSettings.WeeklyScheduleConfig(
            enabled = true,
            selectedDays = setOf(Calendar.MONDAY),
            startMinutes = 8 * 60,
            endMinutes = 22 * 60
        )

        assertTrue(WeeklyScheduleSettings.isActiveAt(config, Calendar.MONDAY, 9 * 60))
        assertFalse(WeeklyScheduleSettings.isActiveAt(config, Calendar.MONDAY, 23 * 60))
        assertFalse(WeeklyScheduleSettings.isActiveAt(config, Calendar.TUESDAY, 9 * 60))
    }

    @Test
    fun activeWindowWorksForOvernightRange() {
        val config = WeeklyScheduleSettings.WeeklyScheduleConfig(
            enabled = true,
            selectedDays = setOf(Calendar.MONDAY),
            startMinutes = 22 * 60,
            endMinutes = 6 * 60
        )

        assertTrue(WeeklyScheduleSettings.isActiveAt(config, Calendar.MONDAY, 23 * 60))
        assertTrue(WeeklyScheduleSettings.isActiveAt(config, Calendar.TUESDAY, 5 * 60))
        assertFalse(WeeklyScheduleSettings.isActiveAt(config, Calendar.TUESDAY, 8 * 60))
    }
}
