package com.schlewinow.happygallery.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.settings.GallerySettings
import com.schlewinow.happygallery.settings.RootDirectorySettings

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize()
    }

    private fun initialize() {
        RootDirectorySettings.restoreSettings(this)
        GallerySettings.restoreSettings(this)

        val navigationIntent = Intent(this, GalleryNavigationActivity::class.java)
        startActivity(navigationIntent)
        finish()
    }
}