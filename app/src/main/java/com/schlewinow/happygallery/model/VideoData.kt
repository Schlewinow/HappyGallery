package com.schlewinow.happygallery.model;

object VideoData {
    var currentVideoMillis: Long = 0

    var currentVideoPercent: Float = 0f

    /**
     * Reset the current video progress data.
     * The next time a video starts playing, it will be from the start of the video.
     */
    fun reset() {
        currentVideoMillis = 0
        currentVideoPercent = 0f
    }
}
