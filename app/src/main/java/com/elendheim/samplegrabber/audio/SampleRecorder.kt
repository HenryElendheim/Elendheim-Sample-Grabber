package com.elendheim.samplegrabber.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.abs

/**
 * Records mono 16-bit PCM from the mic. A grab is always between
 * MIN_SECONDS and MAX_SECONDS long: stopping early waits for the
 * minimum, and the recorder cuts itself off at the maximum.
 */
class SampleRecorder {

    companion object {
        const val SAMPLE_RATE = 44100
        const val MIN_SECONDS = 5
        const val MAX_SECONDS = 10
    }

    @Volatile
    var stopRequested = false
        private set

    fun requestStop() {
        stopRequested = true
    }

    /**
     * Blocking; call from a background dispatcher. Reports progress as
     * (elapsed milliseconds, peak level 0..1) roughly every 46 ms.
     */
    @SuppressLint("MissingPermission")
    fun record(onProgress: (Int, Float) -> Unit): ShortArray {
        stopRequested = false
        val minSamples = SAMPLE_RATE * MIN_SECONDS
        val maxSamples = SAMPLE_RATE * MAX_SECONDS
        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(minBuffer, 8192)
        )
        check(recorder.state == AudioRecord.STATE_INITIALIZED) { "Microphone unavailable" }

        val result = ShortArray(maxSamples)
        val chunk = ShortArray(2048)
        var written = 0
        try {
            recorder.startRecording()
            while (written < maxSamples && !(stopRequested && written >= minSamples)) {
                val toRead = minOf(chunk.size, maxSamples - written)
                val read = recorder.read(chunk, 0, toRead)
                if (read <= 0) break
                chunk.copyInto(result, written, 0, read)
                written += read
                var peak = 0
                for (i in 0 until read) {
                    val v = abs(chunk[i].toInt())
                    if (v > peak) peak = v
                }
                onProgress(written * 1000 / SAMPLE_RATE, peak / 32767f)
            }
        } finally {
            runCatching { recorder.stop() }
            recorder.release()
        }
        return result.copyOf(written)
    }
}
