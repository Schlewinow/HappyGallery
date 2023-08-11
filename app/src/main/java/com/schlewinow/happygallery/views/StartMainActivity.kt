package com.schlewinow.happygallery.views

import android.content.Intent
import com.schlewinow.happygallery.settings.GallerySettings
import com.schlewinow.happygallery.settings.LiveWallpaperSettings
import com.schlewinow.happygallery.settings.RootDirectorySettings

/**
 * Main entry point activity.
 * Starts into the [GalleryNavigationActivity].
 */
class StartMainActivity : StartBaseActivity() {
    override fun onNavigateFromStart() {
        RootDirectorySettings.restoreSettings(this)
        GallerySettings.restoreSettings(this)
        LiveWallpaperSettings.restoreSettings(this)

        val navigationIntent = Intent(this, GalleryNavigationActivity::class.java)
        startActivity(navigationIntent)
    }
}