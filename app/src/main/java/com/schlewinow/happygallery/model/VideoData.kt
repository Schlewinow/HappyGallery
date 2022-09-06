package com.schlewinow.happygallery.model;

object VideoData {
    var currentVideoMillis: Long = 0
    var currentVideoPercent: Float = 0f

    fun reset() {
        currentVideoMillis = 0
        currentVideoPercent = 0f
    }
}
