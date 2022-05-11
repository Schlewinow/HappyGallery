package com.schlewinow.happygallery.views

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.model.GalleryNavigationData
import com.schlewinow.happygallery.settings.GallerySettings
import com.schlewinow.happygallery.settings.RootDirectorySettings

class MainActivity : AppCompatActivity() {
    var navigationInitiated: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize()
    }

    private fun initialize() {
        RootDirectorySettings.restoreSettings(this)
        GallerySettings.restoreSettings(this)

        // Adding this listener leads to the mysterious misbehaviour of deleting the status bar theme,
        // but only on the current activity.
        // Use the start activity to gather the required values and finish it.
        val rootLayout: View = findViewById(R.id.mainRootLayout)
        rootLayout.rootView.setOnApplyWindowInsetsListener { view, windowInsets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                GalleryNavigationData.statusBarHeight = windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars()).top
                GalleryNavigationData.navigationBarHeight = windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars()).bottom
            }
            else {
                GalleryNavigationData.statusBarHeight = windowInsets.systemWindowInsetTop
                GalleryNavigationData.navigationBarHeight = windowInsets.systemWindowInsetBottom
            }

            // It seems this callback is called twice, so avoid opening the gallery twice.
            if(!navigationInitiated) {
                val navigationIntent = Intent(this, GalleryNavigationActivity::class.java)
                startActivity(navigationIntent)
                finish()
                navigationInitiated = true
            }

            windowInsets
        }
    }
}