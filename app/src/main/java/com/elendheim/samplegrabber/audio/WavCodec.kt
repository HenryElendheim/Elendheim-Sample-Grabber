package com.elendheim.samplegrabber.audio

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Encodes mono 16-bit PCM as a standard WAV file. */
object WavCodec {

    fun encode(samples: ShortArray, sampleRate: Int): ByteArray {
        val dataSize = samples.size * 2
        val out = ByteArrayOutputStream(44 + dataSize)
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray(Charsets.US_ASCII))
        header.putInt(36 + dataSize)
        header.put("WAVE".toByteArray(Charsets.US_ASCII))
        header.put("fmt ".toByteArray(Charsets.US_ASCII))
        header.putInt(16)                    // fmt chunk size
        header.putShort(1)                   // PCM
        header.putShort(1)                   // mono
        header.putInt(sampleRate)
        header.putInt(sampleRate * 2)        // byte rate
        header.putShort(2)                   // block align
        header.putShort(16)                  // bits per sample
        header.put("data".toByteArray(Charsets.US_ASCII))
        header.putInt(dataSize)
        out.write(header.array())

        val data = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
        for (s in samples) data.putShort(s)
        out.write(data.array())
        return out.toByteArray()
    }
}
