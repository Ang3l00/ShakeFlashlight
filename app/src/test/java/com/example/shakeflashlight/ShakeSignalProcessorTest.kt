package com.example.shakeflashlight

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeSignalProcessorTest {

    private fun newProcessor(): ShakeSignalProcessor {
        return ShakeSignalProcessor(
            thresholdG = 2.0f,
            minPeakCount = 2,
            peakWindowMs = 500L,
            cooldownMs = 1_000L
        )
    }

    @Test
    fun singlePeakDoesNotTriggerShake() {
        val processor = newProcessor()

        val triggered = processor.onSample(timestampMs = 100L, x = 0f, y = 0f, z = 30f)

        assertFalse(triggered)
    }

    @Test
    fun twoStrongPeaksInsideWindowTriggerShake() {
        val processor = newProcessor()

        assertFalse(processor.onSample(timestampMs = 100L, x = 0f, y = 0f, z = 30f))
        val triggered = processor.onSample(timestampMs = 350L, x = 0f, y = 0f, z = 30f)

        assertTrue(triggered)
    }

    @Test
    fun peaksOutsideWindowDoNotTriggerShake() {
        val processor = newProcessor()

        assertFalse(processor.onSample(timestampMs = 100L, x = 0f, y = 0f, z = 30f))
        val triggered = processor.onSample(timestampMs = 800L, x = 0f, y = 0f, z = 30f)

        assertFalse(triggered)
    }

    @Test
    fun cooldownBlocksImmediateSecondTrigger() {
        val processor = newProcessor()

        assertFalse(processor.onSample(timestampMs = 100L, x = 0f, y = 0f, z = 30f))
        assertTrue(processor.onSample(timestampMs = 300L, x = 0f, y = 0f, z = 30f))

        assertFalse(processor.onSample(timestampMs = 500L, x = 0f, y = 0f, z = 30f))
        val secondTrigger = processor.onSample(timestampMs = 700L, x = 0f, y = 0f, z = 30f)

        assertFalse(secondTrigger)
    }

    @Test
    fun triggerWorksAgainAfterCooldown() {
        val processor = newProcessor()

        assertFalse(processor.onSample(timestampMs = 100L, x = 0f, y = 0f, z = 30f))
        assertTrue(processor.onSample(timestampMs = 300L, x = 0f, y = 0f, z = 30f))

        assertFalse(processor.onSample(timestampMs = 1_450L, x = 0f, y = 0f, z = 30f))
        val triggeredAgain = processor.onSample(timestampMs = 1_650L, x = 0f, y = 0f, z = 30f)

        assertTrue(triggeredAgain)
    }

    @Test
    fun loweringThresholdMakesDetectionMoreSensitive() {
        val processor = ShakeSignalProcessor(
            thresholdG = 2.6f,
            minPeakCount = 2,
            peakWindowMs = 500L,
            cooldownMs = 1_000L
        )

        assertFalse(processor.onSample(timestampMs = 100L, x = 0f, y = 0f, z = 22f))
        assertFalse(processor.onSample(timestampMs = 300L, x = 0f, y = 0f, z = 22f))

        processor.updateThresholdG(2.1f)
        assertFalse(processor.onSample(timestampMs = 1_200L, x = 0f, y = 0f, z = 22f))
        assertTrue(processor.onSample(timestampMs = 1_400L, x = 0f, y = 0f, z = 22f))
    }
}
