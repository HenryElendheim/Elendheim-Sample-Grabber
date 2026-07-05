package com.elendheim.samplegrabber.data

import android.content.Context

enum class ExportFormat(val extension: String, val mimeType: String) {
    WAV("wav", "audio/x-wav"),
    MP3("mp3", "audio/mpeg")
}

/** User preferences: what format grabs are saved as and how long they run. */
class Settings(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var format: ExportFormat
        get() = runCatching {
            ExportFormat.valueOf(prefs.getString(KEY_FORMAT, ExportFormat.WAV.name)!!)
        }.getOrDefault(ExportFormat.WAV)
        set(value) = prefs.edit().putString(KEY_FORMAT, value.name).apply()

    var recordSeconds: Int
        get() = prefs.getInt(KEY_SECONDS, DEFAULT_SECONDS).coerceIn(LENGTH_OPTIONS.first(), LENGTH_OPTIONS.last())
        set(value) = prefs.edit().putInt(KEY_SECONDS, value).apply()

    companion object {
        val LENGTH_OPTIONS = listOf(5, 10, 15, 20, 30)
        const val DEFAULT_SECONDS = 10
        private const val KEY_FORMAT = "export_format"
        private const val KEY_SECONDS = "record_seconds"
    }
}
