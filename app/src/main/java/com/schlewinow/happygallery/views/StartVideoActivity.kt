package com.schlewinow.happygallery.views

import android.content.Intent
import com.schlewinow.happygallery.model.VideoData

/**
 * Entry point activity when opening a video file.
 * Starts into the [VideoViewerVlcActivity].
 */
class StartVideoActivity : StartBaseActivity() {
    override fun onNavigateFromStart() {
        VideoData.reset()

        val navigationIntent = Intent(intent)
        navigationIntent.setClass(this, VideoViewerVlcActivity::class.java)
        startActivity(navigationIntent)
    }
}