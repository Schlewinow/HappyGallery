package com.schlewinow.happygallery.settings

import android.content.Context
import android.net.Uri

/**
 * Settings used to control the live wallpaper functionality.
 * These settings are modified in the main app and applied by the live wallpaper service implementation.
 */
object LiveWallpaperSettings {
    /**
     * Access ID to the live wallpaper shared preferences.
     */
    const val SHARED_PREFERENCES_ACCESS = "LiveWallpaperSettings"

    private const val PREF_LIVE_WALLPAPER_URI = "LiveWallpaperUri"

    /**
     * Uri of the video file to be played as live wallpaper.
     */
    var videoLiveWallpaperUri: Uri? = null

    /**
     * Visualization settings for the live wallpaper while in portrait mode.
     */
    var portraitSettings: LiveWallpaperOrientationSettings? = null
        private set

    /**
     * Visualization settings for the live wallpaper while in landscape mode.
     */
    var landscapeSettings: LiveWallpaperOrientationSettings? = null
        private set

    /**
     * Restore the last saved state of the settings.
     * Uses private shared preferences.
     * @param context Local app context.
     */
    fun restoreSettings(context: Context?) {
        if (context == null) {
            return
        }

        val prefs = context.getSharedPreferences(SHARED_PREFERENCES_ACCESS, Context.MODE_PRIVATE)
        val liveVideoWallpaperUriString = prefs.getString(SHARED_PREFERENCES_ACCESS + PREF_LIVE_WALLPAPER_URI, "")
        if (liveVideoWallpaperUriString?.isNotEmpty() == true) {
            videoLiveWallpaperUri = Uri.parse(liveVideoWallpaperUriString)
        }

        portraitSettings = LiveWallpaperOrientationSettings()

        landscapeSettings = LiveWallpaperOrientationSettings()
    }

    /**
     * Permanently store the current runtime settings.
     * Uses private shared preferences.
     * @param context Local app context.
     */
    fun storeSettings(context: Context?) {
        if(context == null) {
            return
        }

        val prefs = context.getSharedPreferences(SHARED_PREFERENCES_ACCESS, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString(SHARED_PREFERENCES_ACCESS + PREF_LIVE_WALLPAPER_URI, videoLiveWallpaperUri.toString())
        editor.apply()
    }
}