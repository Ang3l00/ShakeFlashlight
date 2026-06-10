package com.example.shakeflashlight

import kotlin.math.sqrt

/*
Design comment:
- Problem solved: a single acceleration spike could toggle the torch by mistake, while some valid shakes were ignored.
- Main idea: detect a shake only after multiple strong peaks appear within a short time window, then apply a cooldown.
- Alternatives rejected:
  1) Complex digital filters: better precision, but unnecessary complexity for this app.
  2) ML-based gesture classification: overkill and harder to debug/maintain.
*/
class ShakeSignalProcessor(
    private var thresholdG: Float = 2.2f,
    private val minPeakCount: Int = 2,
    private val peakWindowMs: Long = 650L,
    private val cooldownMs: Long = 1_200L
) {
    private val peakTimestampsMs = ArrayDeque<Long>()
    private var lastTriggerAtMs = Long.MIN_VALUE

    fun onSample(timestampMs: Long, x: Float, y: Float, z: Float): Boolean {
        if (timestampMs < 0L) return false

        pruneOldPeaks(timestampMs)
        if (!isStrongPeak(x, y, z)) return false

        peakTimestampsMs.addLast(timestampMs)
        if (!hasEnoughPeaks()) return false
        if (isInsideCooldown(timestampMs)) return false

        // Why: consumed peaks should not spill into the next gesture detection cycle.
        peakTimestampsMs.clear()
        lastTriggerAtMs = timestampMs
        return true
    }

    fun reset() {
        peakTimestampsMs.clear()
        lastTriggerAtMs = Long.MIN_VALUE
    }

    fun updateThresholdG(newThresholdG: Float) {
        if (newThresholdG <= 0f) return
        thresholdG = newThresholdG
        reset()
    }

    private fun pruneOldPeaks(currentTimeMs: Long) {
        while (peakTimestampsMs.isNotEmpty()) {
            val oldestPeakMs = peakTimestampsMs.first()
            if (currentTimeMs - oldestPeakMs <= peakWindowMs) break
            peakTimestampsMs.removeFirst()
        }
    }

    private fun hasEnoughPeaks(): Boolean {
        return peakTimestampsMs.size >= minPeakCount
    }

    private fun isInsideCooldown(currentTimeMs: Long): Boolean {
        if (lastTriggerAtMs == Long.MIN_VALUE) return false
        return currentTimeMs - lastTriggerAtMs < cooldownMs
    }

    private fun isStrongPeak(x: Float, y: Float, z: Float): Boolean {
        return gForce(x, y, z) >= thresholdG
    }

    private fun gForce(x: Float, y: Float, z: Float): Float {
        val gravity = 9.80665f
        val normalizedX = x / gravity
        val normalizedY = y / gravity
        val normalizedZ = z / gravity
        return sqrt(
            normalizedX * normalizedX +
                normalizedY * normalizedY +
                normalizedZ * normalizedZ
        )
    }
}
