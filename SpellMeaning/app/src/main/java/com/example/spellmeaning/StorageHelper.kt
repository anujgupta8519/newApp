package com.example.spellmeaning

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Handles all local persistence:
 *  - internal history.json  -> source of truth the app reads to build the "Saved Words" list
 *  - a plain-text export to the device's Downloads folder -> the file you can open, share,
 *    or review outside the app (e.g. with any text/file viewer)
 */
object StorageHelper {

    private const val HISTORY_FILE = "history.json"
    private const val EXPORT_FILE_NAME = "spell_meanings.txt"
    private val gson = Gson()

    /** Reads all saved entries, newest first. */
    fun loadAll(context: Context): MutableList<WordEntry> {
        val file = File(context.filesDir, HISTORY_FILE)
        if (!file.exists()) return mutableListOf()
        return try {
            val json = file.readText()
            val type = object : TypeToken<MutableList<WordEntry>>() {}.type
            val list: MutableList<WordEntry> = gson.fromJson(json, type) ?: mutableListOf()
            list.sortedByDescending { it.timestamp }.toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    /**
     * Saves a new entry automatically. If the same word was searched before,
     * it replaces the old meaning instead of creating a duplicate.
     */
    @Synchronized
    fun saveEntry(context: Context, entry: WordEntry) {
        val current = loadAll(context)
        current.removeAll { it.word.equals(entry.word, ignoreCase = true) }
        current.add(entry)

        val file = File(context.filesDir, HISTORY_FILE)
        file.writeText(gson.toJson(current))

        // Keep a human-readable copy in Downloads in sync too, so the user always
        // has an up-to-date file they can open outside the app.
        exportToDownloads(context, current)
    }

    fun deleteEntry(context: Context, word: String) {
        val current = loadAll(context)
        current.removeAll { it.word.equals(word, ignoreCase = true) }
        val file = File(context.filesDir, HISTORY_FILE)
        file.writeText(gson.toJson(current))
        exportToDownloads(context, current)
    }

    /**
     * Writes a plain-text, human-readable version of the whole history to the public
     * Downloads folder (visible in any Files app), sorted newest-first.
     */
    private fun exportToDownloads(context: Context, entries: List<WordEntry>) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sorted = entries.sortedByDescending { it.timestamp }
        val builder = StringBuilder()
        builder.append("Saved Spellings & Meanings\n")
        builder.append("==========================\n\n")
        for (e in sorted) {
            builder.append(e.word.uppercase(Locale.getDefault())).append("\n")
            builder.append("Saved: ").append(sdf.format(Date(e.timestamp))).append("\n")
            builder.append(e.meaning).append("\n")
            builder.append("--------------------------\n\n")
        }
        val content = builder.toString()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val existingUri = findExistingDownload(context)
                if (existingUri != null) {
                    resolver.openOutputStream(existingUri, "wt")?.use { it.write(content.toByteArray()) }
                } else {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, EXPORT_FILE_NAME)
                        put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    uri?.let {
                        resolver.openOutputStream(it)?.use { out -> out.write(content.toByteArray()) }
                        values.clear()
                        values.put(MediaStore.Downloads.IS_PENDING, 0)
                        resolver.update(it, values, null, null)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
                val outFile = File(downloadsDir, EXPORT_FILE_NAME)
                outFile.writeText(content)
            }
        } catch (e: Exception) {
            // Exporting is best-effort; the in-app history (history.json) remains the source of truth
            // even if the public export fails (e.g. storage permission denied on old Android versions).
        }
    }

    private fun findExistingDownload(context: Context): android.net.Uri? {
        val resolver = context.contentResolver
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
        resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            arrayOf(EXPORT_FILE_NAME),
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                return android.content.ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
            }
        }
        return null
    }
}
