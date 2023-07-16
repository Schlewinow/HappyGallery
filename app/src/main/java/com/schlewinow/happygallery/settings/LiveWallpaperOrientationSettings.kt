package com.schlewinow.happygallery.settings

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

    /**
     * Video scaling mode for this setting's orientation.
     */
    var videoScalingMode: VideoScaleMode = VideoScaleMode.SCALE_TO_FIT_WITH_CROPPING
}