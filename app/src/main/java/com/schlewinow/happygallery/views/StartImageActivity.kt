package com.schlewinow.happygallery.views

import android.content.Intent

/**
 * Entry point activity when opening an image file.
 * Starts into the [ImageViewerActivity].
 */
class StartImageActivity : StartBaseActivity() {
    override fun onNavigateFromStart() {
        val navigationIntent = Intent(intent)
        navigationIntent.setClass(this, ImageViewerActivity::class.java)
        startActivity(navigationIntent)
    }
}