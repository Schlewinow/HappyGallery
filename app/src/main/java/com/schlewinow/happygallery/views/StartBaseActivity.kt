package com.schlewinow.happygallery.views;

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.model.GalleryNavigationData

/**
 * Base activity for any entry point to the app.
 * Because of some Google UI code shenanigans, some steps must be done on an activity that is immediately left afterwards.
 * So any entrypoint (main app, opening image or video, etc.) needs to start from an activity inheriting this base class.
 */
abstract class StartBaseActivity : AppCompatActivity() {
    /**
     * Used to avoid navigation from this activity to be invoked twice.
     */
    var navigationInitiated: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        initialize()
    }

    private fun initialize() {
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

            // It seems this callback is called twice, so avoid navigation from here multiple times.
            if(!navigationInitiated) {
                onNavigateFromStart()
                finish()
                navigationInitiated = true
            }

            windowInsets
        }
    }

    /**
     * Must be overridden by implementing classes to allow navigating somewhere after the setup is done.
     * Should simply navigate to another activity, may pass intent data alongside.
     */
    protected abstract fun onNavigateFromStart()
}
