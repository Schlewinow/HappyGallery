package com.schlewinow.happygallery.settings

import android.content.SharedPreferences

/**
 * Specific settings for the live wallpaper functionality used in a single orientation mode.
 * Two of these should be available, one for portrait and one for landscape mode.
 * ToDo: should at some point allow to choose target video area
 */
class LiveWallpaperOrientationSettings {
    /**
     * Available video scale modes.
     * Abstraction to the video scale modes used by different video players (e.g. Android MediaPlayer, ExoPlayer)
     */
    enum class VideoScaleMode {
        SCALE_TO_FIT, SCALE_TO_FIT_WITH_CROPPING
    }

    private val PREF_LIVE_WALLPAPER_ORIENTATION_SCALE_MODE = "LiveWallpaperOrientationScaleMode"

    /**
     * Video scaling mode for this setting's orientation.
     */
    var videoScalingMode: VideoScaleMode = VideoScaleMode.SCALE_TO_FIT_WITH_CROPPING

    /**
     * Restore the last saved state of the live wallpaper orientation settings.
     * Uses private shared preferences.
     * @param prefs Shared preferences to restore data from.
     * @param storageSuffix Used to specify which orientation data is restored.
     */
    fun restoreSettings(prefs: SharedPreferences, storageSuffix: String) {
        val scaleModeString = prefs.getString(
            LiveWallpaperSettings.SHARED_PREFERENCES_ACCESS + PREF_LIVE_WALLPAPER_ORIENTATION_SCALE_MODE + storageSuffix,
            VideoScaleMode.SCALE_TO_FIT_WITH_CROPPING.name)
        videoScalingMode = VideoScaleMode.valueOf(scaleModeString ?: VideoScaleMode.SCALE_TO_FIT_WITH_CROPPING.name)
        android.util.Log.d("VIDEO SCALE MODE restored", videoScalingMode.name)
    }

    /**
     * Permanently store the the live wallpaper orientation settings.
     * Uses private shared preferences.
     * @param prefs Shared preferences to store data to.
     * @param storageSuffix Used to specify which orientation data is stored.
     */
    fun storeSettings(prefs: SharedPreferences, storageSuffix: String) {
        val editor = prefs.edit()

        editor.putString(
            LiveWallpaperSettings.SHARED_PREFERENCES_ACCESS + PREF_LIVE_WALLPAPER_ORIENTATION_SCALE_MODE + storageSuffix,
            videoScalingMode.name)
        editor.apply()
        android.util.Log.d("VIDEO SCALE MODE stored", videoScalingMode.name)
    }
}