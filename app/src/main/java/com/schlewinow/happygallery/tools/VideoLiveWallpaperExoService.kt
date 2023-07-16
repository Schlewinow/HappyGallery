package com.schlewinow.happygallery.tools

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Configuration
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.schlewinow.happygallery.settings.LiveWallpaperOrientationSettings
import com.schlewinow.happygallery.settings.LiveWallpaperSettings

/**
 * Live wallpaper playback service using ExoPlayer.
 * Accesses [LiveWallpaperSettings] to get the required Uri and visual calibration info.
 */
// ToDo: implement VLC service. Not possible with libVLC 3 as no fluent looped playback is available. Requires libVLC 4.
class VideoLiveWallpaperExoService : WallpaperService() {

    override fun onCreateEngine(): WallpaperService.Engine {
        return VideoEngine()
    }

    internal inner class VideoEngine : WallpaperService.Engine(), OnSharedPreferenceChangeListener {

        private var videoPlayer: ExoPlayer? = null

        private var sharedPreferences: SharedPreferences? = null

        init {
            LiveWallpaperSettings.restoreSettings(baseContext)
            videoPlayer = ExoPlayer.Builder(baseContext).build()
            setupVideo()
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)

            videoPlayer?.setVideoSurfaceHolder(surfaceHolder)
            videoPlayer?.play()

            sharedPreferences = baseContext.getSharedPreferences(LiveWallpaperSettings.SHARED_PREFERENCES_ACCESS, Context.MODE_PRIVATE)
            sharedPreferences?.registerOnSharedPreferenceChangeListener(this@VideoEngine)
        }

        override fun onDestroy() {
            super.onDestroy()
            videoPlayer?.release()
            sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this@VideoEngine)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            // Optimize performance. Only play video if wallpaper is actually visible.
            if (visible) {
                videoPlayer?.play()
            }
            else {
                videoPlayer?.pause()
            }
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            // This is the entry point to react to changes in the settings, like picking a different video file.
            setupVideo()
        }

        /**
         * Load the given video Uri and apply additional settings.
         */
        private fun setupVideo() {
            val videoUri = LiveWallpaperSettings.videoLiveWallpaperUri ?: return

            videoPlayer?.setMediaItem(MediaItem.fromUri(videoUri))
            videoPlayer?.prepare()
            videoPlayer?.repeatMode = ExoPlayer.REPEAT_MODE_ALL

            var orientationSettings: LiveWallpaperOrientationSettings? =
                LiveWallpaperSettings.portraitSettings
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                orientationSettings = LiveWallpaperSettings.landscapeSettings
            }
            when (orientationSettings?.videoScalingMode) {
                LiveWallpaperOrientationSettings.VideoScaleMode.SCALE_TO_FIT -> {
                    videoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                }

                LiveWallpaperOrientationSettings.VideoScaleMode.SCALE_TO_FIT_WITH_CROPPING -> {
                    videoPlayer?.videoScalingMode =
                        C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                }

                else -> {
                    videoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
                }
            }
        }
    }
}