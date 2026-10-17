package com.shakhawat.ghhumbhango.alarm

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import java.io.File

/**
 * Helper to pick and persistently cache custom alarm ringtones and audio files.
 * Caches files directly into internal storage so playback never fails across reboots or lockscreens.
 */
object CustomAlarmAudioHelper {
    private const val TAG = "CustomAlarmAudio"

    fun createRingtonePickerIntent(existingUriString: String?): Intent {
        return Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_RINGTONE)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Tone")
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            if (!existingUriString.isNullOrBlank()) {
                try {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingUriString))
                } catch (_: Exception) {}
            }
        }
    }

    fun handleRingtonePickerResult(context: Context, data: Intent?): Pair<String, String>? {
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        } ?: return null

        val title = try {
            RingtoneManager.getRingtone(context, uri)?.getTitle(context) ?: "কাস্টম রিংটোন"
        } catch (_: Exception) {
            "কাস্টম রিংটোন"
        }

        return cacheAudioUri(context, uri, title) ?: Pair(uri.toString(), title)
    }

    fun handleAudioFilePickerResult(context: Context, uri: Uri?): Pair<String, String>? {
        if (uri == null) return null

        var displayName = "Custom Audio"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    displayName = cursor.getString(nameIndex) ?: displayName
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting display name: ${e.message}")
        }

        return cacheAudioUri(context, uri, displayName)
    }

    private fun cacheAudioUri(context: Context, sourceUri: Uri, displayName: String): Pair<String, String>? {
        return try {
            val targetFile = File(context.filesDir, "custom_alarm_sound.audio")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            val cachedUri = Uri.fromFile(targetFile).toString()
            Log.d(TAG, "Successfully cached custom audio file: $cachedUri ($displayName)")
            Pair(cachedUri, displayName)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache custom audio file: ${e.message}", e)
            Pair(sourceUri.toString(), displayName)
        }
    }
}

