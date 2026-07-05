package com.elendheim.samplegrabber.audio

import kotlin.math.sqrt

/**
 * Cuts the dead air off both ends of a recording so the sample starts
 * where the sound does. Works on 20 ms RMS windows with a threshold
 * relative to the loudest moment, keeping a little padding so nothing
 * gets clipped mid-transient.
 */
object SilenceTrimmer {

    private const val WINDOW_MS = 20
    private const val PAD_WINDOWS = 3
    private const val RELATIVE_THRESHOLD = 0.05
    private const val ABSOLUTE_FLOOR = 180.0

    fun trim(samples: ShortArray, sampleRate: Int): ShortArray {
        val window = sampleRate * WINDOW_MS / 1000
        val windowCount = samples.size / window
        if (windowCount < PAD_WINDOWS * 2 + 1) return samples

        val rms = DoubleArray(windowCount)
        var peak = 0.0
        for (w in 0 until windowCount) {
            var sum = 0.0
            val base = w * window
            for (i in 0 until window) {
                val v = samples[base + i].toDouble()
                sum += v * v
            }
            rms[w] = sqrt(sum / window)
            if (rms[w] > peak) peak = rms[w]
        }

        val threshold = maxOf(ABSOLUTE_FLOOR, peak * RELATIVE_THRESHOLD)
        val first = rms.indexOfFirst { it >= threshold }
        val last = rms.indexOfLast { it >= threshold }
        if (first == -1 || last < first) return samples

        val start = (first - PAD_WINDOWS).coerceAtLeast(0) * window
        val end = ((last + 1 + PAD_WINDOWS) * window).coerceAtMost(samples.size)
        if (end - start < sampleRate / 4) return samples
        return samples.copyOfRange(start, end)
    }
}
