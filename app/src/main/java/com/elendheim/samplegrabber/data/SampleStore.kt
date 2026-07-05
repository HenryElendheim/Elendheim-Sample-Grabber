package com.elendheim.samplegrabber.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

data class Sample(
    val uri: Uri,
    val name: String,
    val durationMs: Long,
    val sizeBytes: Long
)

/** Saves, lists and deletes grabs in Music/Elendheim Samples via MediaStore. */
object SampleStore {

    private const val RELATIVE_PATH = "Music/Elendheim Samples/"

    fun save(context: Context, displayName: String, wavBytes: ByteArray): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/x-wav")
            put(MediaStore.Audio.Media.RELATIVE_PATH, RELATIVE_PATH)
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values)
            ?: throw IllegalStateException("Could not create $displayName")
        resolver.openOutputStream(uri)?.use { it.write(wavBytes) }
            ?: throw IllegalStateException("Could not write $displayName")
        values.clear()
        values.put(MediaStore.Audio.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri
    }

    fun list(context: Context): List<Sample> {
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE
        )
        val samples = mutableListOf<Sample>()
        context.contentResolver.query(
            collection,
            projection,
            "${MediaStore.Audio.Media.RELATIVE_PATH} = ?",
            arrayOf(RELATIVE_PATH),
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            while (cursor.moveToNext()) {
                val size = cursor.getLong(sizeCol)
                var duration = cursor.getLong(durationCol)
                if (duration <= 0 && size > 44) {
                    // Our files are always mono 16-bit 44.1 kHz WAVs, so the
                    // size tells us the duration even before the media
                    // scanner has extracted it.
                    duration = (size - 44) * 1000 / (44100 * 2)
                }
                samples.add(
                    Sample(
                        uri = ContentUris.withAppendedId(collection, cursor.getLong(idCol)),
                        name = cursor.getString(nameCol),
                        durationMs = duration,
                        sizeBytes = size
                    )
                )
            }
        }
        return samples
    }

    fun delete(context: Context, uri: Uri): Boolean =
        runCatching { context.contentResolver.delete(uri, null, null) > 0 }.getOrDefault(false)
}
