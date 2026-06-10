package com.example.shakeflashlight

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun processorResetAllowsFreshGestureCycle() {
        val processor = ShakeSignalProcessor(
            thresholdG = 2.0f,
            minPeakCount = 2,
            peakWindowMs = 500L,
            cooldownMs = 5_000L
        )

        assertFalse(processor.onSample(timestampMs = 100L, x = 0f, y = 0f, z = 30f))
        assertTrue(processor.onSample(timestampMs = 300L, x = 0f, y = 0f, z = 30f))

        processor.reset()

        assertFalse(processor.onSample(timestampMs = 320L, x = 0f, y = 0f, z = 30f))
        assertTrue(processor.onSample(timestampMs = 500L, x = 0f, y = 0f, z = 30f))
    }
}