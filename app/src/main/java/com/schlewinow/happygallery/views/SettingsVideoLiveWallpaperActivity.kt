package com.schlewinow.happygallery.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.RadioGroup
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.settings.LiveWallpaperOrientationSettings
import com.schlewinow.happygallery.settings.LiveWallpaperSettings

/**
 * Activity to allow changing settings to configure video live wallpapers.
 *
 * Setting for Android live wallpapers are currently in limbo.
 * SettingsActivity is deprecated and newer replacements seem to be incompatible with live wallpapers.
 */
class SettingsVideoLiveWallpaperActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_live_wallpaper_settings)
        setSupportActionBar(findViewById(R.id.settingsVideoLiveWallpaperToolbar))

        supportActionBar?.title = resources.getString(R.string.settings_video_live_wallpaper_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupUI()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when {
            item.itemId == android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()

        // This activity is a dead end navigation wise.
        // So save the settings once the activity is being finished.
        LiveWallpaperSettings.storeSettings(this)
    }

    /**
     * Setup UI.
     * Initialize UI with current setting state and add functionality to change settings.
     */
    private fun setupUI() {
        val portraitSettings: LiveWallpaperOrientationSettings = LiveWallpaperSettings.portraitSettings

        val portraitScaleModeGroup: RadioGroup = findViewById(R.id.settingsVideoLiveWallpaperPortraitScaleModeGroup)
        when (portraitSettings.videoScalingMode) {
            LiveWallpaperOrientationSettings.VideoScaleMode.SCALE_TO_FIT -> {
                portraitScaleModeGroup.check(R.id.settingsVideoLiveWallpaperPortraitScaleModeButton1)
            }
            LiveWallpaperOrientationSettings.VideoScaleMode.SCALE_TO_FIT_WITH_CROPPING -> {
                portraitScaleModeGroup.check(R.id.settingsVideoLiveWallpaperPortraitScaleModeButton2)
            }
        }

        val portraitScaleButton1: RadioButton = findViewById(R.id.settingsVideoLiveWallpaperPortraitScaleModeButton1)
        portraitScaleButton1.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                portraitSettings.videoScalingMode = LiveWallpaperOrientationSettings.VideoScaleMode.SCALE_TO_FIT
            }
        }

        val portraitScaleButton2: RadioButton = findViewById(R.id.settingsVideoLiveWallpaperPortraitScaleModeButton2)
        portraitScaleButton2.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                portraitSettings.videoScalingMode = LiveWallpaperOrientationSettings.VideoScaleMode.SCALE_TO_FIT_WITH_CROPPING
            }
        }

        val landscapeSettings: LiveWallpaperOrientationSettings = LiveWallpaperSettings.landscapeSettings

        val landscapeScaleModeGroup: RadioGroup = findViewById(R.id.settingsVideoLiveWallpaperLandscapeScaleModeGroup)
        when (landscapeSettings.videoScalingMode) {
            LiveWallpaperOrientationSettings.VideoScaleMode.SCALE_TO_FIT -> {
                landscapeScaleModeGroup.check(R.id.settingsVideoLiveWallpaperLandscapeScaleModeButton1)
            }
            LiveWallpaperOrientationSettings.VideoScaleMode.SCALE_TO_FIT_WITH_CROPPING -> {
                landscapeScaleModeGroup.check(R.id.settingsVideoLiveWallpaperLandscapeScaleModeButton2)
            }
        }

        val landscapeScaleButton1: RadioButton = findViewById(R.id.settingsVideoLiveWallpaperLandscapeScaleModeButton1)
        landscapeScaleButton1.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                landscapeSettings.videoScalingMode = LiveWallpaperOrientationSettings.VideoScaleMode.SCALE_TO_FIT
            }
        }

        val landscapeScaleButton2: RadioButton = findViewById(R.id.settingsVideoLiveWallpaperLandscapeScaleModeButton2)
        landscapeScaleButton2.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                landscapeSettings.videoScalingMode = LiveWallpaperOrientationSettings.VideoScaleMode.SCALE_TO_FIT_WITH_CROPPING
            }
        }
    }
}