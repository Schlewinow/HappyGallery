package com.schlewinow.happygallery.views

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.Surface
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Guideline
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.settings.LiveWallpaperSettings
import com.schlewinow.happygallery.tools.VideoLiveWallpaperExoService

/**
 * Common base methods used in any video player view implementation.
 */
open class VideoViewerBaseActivity : AppCompatActivity() {

    protected var videoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load video data from intent.
        if(intent != null && intent.data != null) {
            videoUri = intent.data
        }
        else {
            finish()
        }
    }

    /**
     * Configure transparent system bars at top and bottom of the screen.
     * Use full screen to render contents.
     */
    protected fun setupTransparentSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        window.statusBarColor = Color.TRANSPARENT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_video_viewer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_video_wallpaper -> {
                setVideoAsWallpaper(videoUri)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Open an intent to set the currently shown video as wallpaper in the OS.
     * @param videoUri Uri of the video to be used as live wallpaper.
     */
    private fun setVideoAsWallpaper(videoUri: Uri?) {
        val wallpaperManager = WallpaperManager.getInstance(this)
        if (wallpaperManager.isSetWallpaperAllowed) {
            LiveWallpaperSettings.videoLiveWallpaperUri = videoUri
            LiveWallpaperSettings.storeSettings(this)

            val wallpaperIntent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            val wallpaperComponentName = ComponentName(this, VideoLiveWallpaperExoService::class.java)
            wallpaperIntent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, wallpaperComponentName)
            startActivity(wallpaperIntent)
        }
    }

    /**
     * Create a string showing the current video progress in the format hours:minutes:seconds.
     * @param milliseconds Time in milliseconds to parse into a readable time string.
     */
    protected fun makeTimeString(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 60000) % 60
        val hours = milliseconds / 3600000

        var timeString = "$hours:"
        if(minutes < 10) {
            timeString += "0"
        }
        timeString += "$minutes:"
        if(seconds < 10) {
            timeString += "0"
        }
        timeString += seconds.toString()

        return timeString
    }

    /**
     * Setup the guidelines used in the UI to position buttons and menu properly.
     * @param statusBarHeight Height of the system status bar in pixels.
     * @param navigationBarHeight Height of the system navigation bar in pixels.
     */
    protected fun setupGuidelines(statusBarHeight: Int, navigationBarHeight: Int) {
        val topGuideline: Guideline = findViewById(R.id.videoViewerTopGuideline)
        topGuideline.setGuidelineBegin(statusBarHeight)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val bottomGuideline: Guideline = findViewById(R.id.videoViewerBottomGuideline)
            bottomGuideline.setGuidelineEnd(navigationBarHeight)
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val leftGuideline: Guideline = findViewById(R.id.videoViewerLeftGuideline)
            val rightGuideline: Guideline = findViewById(R.id.videoViewerRightGuideline)

            val rotation: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                rotation = display?.rotation ?: Surface.ROTATION_90
            } else {
                rotation = getWindowManager().getDefaultDisplay().getRotation()
            }

            if (rotation == Surface.ROTATION_90) {
                leftGuideline.setGuidelineBegin(0)
                rightGuideline.setGuidelineEnd(navigationBarHeight)
            } else if (rotation == Surface.ROTATION_270) {
                leftGuideline.setGuidelineBegin(navigationBarHeight)
                rightGuideline.setGuidelineEnd(0)
            }
        }
    }
}