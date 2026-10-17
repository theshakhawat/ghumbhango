package com.shakhawat.ghhumbhango.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.shakhawat.ghhumbhango.domain.model.AlarmIntensity
import com.shakhawat.ghhumbhango.domain.model.AlarmSound

/**
 * Robust alarm audio and vibration controller.
 * Plays looping alert tones configured with AudioAttributes.USAGE_ALARM
 * and pulsed vibration patterns to ensure waking up sleeping passengers.
 * Also provides audio previewing for settings and alarm configuration.
 */
object AlarmPlayer {

    private const val TAG = "AlarmPlayer"
    private var mediaPlayer: MediaPlayer? = null
    private var previewPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var isPlaying = false
    private var currentIntensity: AlarmIntensity = AlarmIntensity.FULL

    @Synchronized
    fun start(
        context: Context,
        isVibrationEnabled: Boolean = true,
        alarmSound: AlarmSound = AlarmSound.DEFAULT,
        customToneUri: Uri? = null,
        intensity: AlarmIntensity = AlarmIntensity.FULL
    ) {
        if (isPlaying) {
            Log.d(TAG, "Alarm is already playing, ignoring duplicate start request.")
            return
        }

        stopPreview()
        currentIntensity = intensity

        try {
            startAudio(context, alarmSound, customToneUri, intensity)
            if (isVibrationEnabled) {
                startVibrationWithIntensity(context, intensity)
            }
            isPlaying = true
            Log.d(TAG, "GhumBhango alarm audio and vibration initiated with intensity: $intensity")
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating alarm player: ${e.message}", e)
        }
    }

    private fun startAudio(
        context: Context,
        alarmSound: AlarmSound,
        customToneUri: Uri?,
        intensity: AlarmIntensity
    ) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            // Set alarm stream volume to max audible level, scaling actual output via MediaPlayer volume
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            mediaPlayer?.release()

            if (customToneUri != null) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(audioAttributes)
                    if (customToneUri.scheme == "file" && customToneUri.path != null) {
                        setDataSource(customToneUri.path)
                    } else {
                        setDataSource(context, customToneUri)
                    }
                    isLooping = true
                    prepare()
                    setVolume(intensity.volumeRatio, intensity.volumeRatio)
                    start()
                }
            } else {
                val player = MediaPlayer.create(context, alarmSound.rawResId)
                if (player != null) {
                    player.setAudioAttributes(audioAttributes)
                    player.isLooping = true
                    player.setVolume(intensity.volumeRatio, intensity.volumeRatio)
                    player.start()
                    mediaPlayer = player
                } else {
                    val toneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(audioAttributes)
                        setDataSource(context, toneUri)
                        isLooping = true
                        prepare()
                        setVolume(intensity.volumeRatio, intensity.volumeRatio)
                        start()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MediaPlayer for alarm: ${e.message}", e)
            try {
                val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, fallbackUri)
                    isLooping = true
                    prepare()
                    setVolume(intensity.volumeRatio, intensity.volumeRatio)
                    start()
                }
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Fallback audio playback failed: ${fallbackEx.message}", fallbackEx)
            }
        }
    }

    /**
     * Plays a non-looping audio preview of the selected alarm sound.
     */
    @Synchronized
    fun playPreview(context: Context, sound: AlarmSound, customToneUri: Uri? = null) {
        stopPreview()

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val player = if (sound == AlarmSound.CUSTOM && customToneUri != null) {
                MediaPlayer().apply {
                    setAudioAttributes(audioAttributes)
                    if (customToneUri.scheme == "file" && customToneUri.path != null) {
                        setDataSource(customToneUri.path)
                    } else {
                        setDataSource(context, customToneUri)
                    }
                    prepare()
                }
            } else if (sound != AlarmSound.CUSTOM && sound.rawResId != 0) {
                MediaPlayer.create(context, sound.rawResId) ?: run {
                    val toneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    MediaPlayer().apply {
                        setAudioAttributes(audioAttributes)
                        setDataSource(context, toneUri)
                        prepare()
                    }
                }
            } else {
                val toneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                MediaPlayer().apply {
                    setAudioAttributes(audioAttributes)
                    setDataSource(context, toneUri)
                    prepare()
                }
            }

            previewPlayer = player.apply {
                isLooping = false
                setOnCompletionListener {
                    try {
                        it.release()
                    } catch (_: Exception) {}
                    if (previewPlayer == it) {
                        previewPlayer = null
                    }
                }
                start()
            }
            Log.d(TAG, "Playing preview for sound: ${sound.displayLabel}, customUri: $customToneUri")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound preview: ${e.message}", e)
        }
    }

    /**
     * Immediately stops any active audio preview.
     */
    @Synchronized
    fun stopPreview() {
        try {
            previewPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping preview player: ${e.message}", e)
        } finally {
            previewPlayer = null
        }
    }

    private fun startVibrationWithIntensity(context: Context, intensity: AlarmIntensity) {
        try {
            if (vibrator == null) {
                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =
                        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (intensity.vibrationAmplitudes != null && vibrator?.hasAmplitudeControl() == true) {
                    VibrationEffect.createWaveform(intensity.vibrationTimings, intensity.vibrationAmplitudes, 0)
                } else {
                    VibrationEffect.createWaveform(intensity.vibrationTimings, 0)
                }
                val vibrationAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                @Suppress("DEPRECATION")
                vibrator?.vibrate(effect, vibrationAttributes)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(intensity.vibrationTimings, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to trigger vibration: ${e.message}", e)
        }
    }

    /**
     * Dynamically updates the volume ratio and vibration pattern based on remaining distance.
     */
    @Synchronized
    fun updateIntensity(
        context: Context,
        intensity: AlarmIntensity,
        isVibrationEnabled: Boolean = true
    ) {
        if (!isPlaying || currentIntensity == intensity) return

        Log.d(TAG, "Updating alarm intensity from $currentIntensity to $intensity")
        currentIntensity = intensity

        try {
            mediaPlayer?.setVolume(intensity.volumeRatio, intensity.volumeRatio)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update MediaPlayer volume: ${e.message}")
        }

        if (isVibrationEnabled) {
            try {
                vibrator?.cancel()
                startVibrationWithIntensity(context, intensity)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update vibration pattern: ${e.message}")
            }
        }
    }

    @Synchronized
    fun stop() {
        stopPreview()

        if (!isPlaying) return

        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player: ${e.message}", e)
        } finally {
            mediaPlayer = null
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling vibrator: ${e.message}", e)
        } finally {
            vibrator = null
        }

        isPlaying = false
        currentIntensity = AlarmIntensity.FULL
        Log.d(TAG, "Alarm player completely stopped.")
    }

    fun isAlarmActive(): Boolean = isPlaying
}
