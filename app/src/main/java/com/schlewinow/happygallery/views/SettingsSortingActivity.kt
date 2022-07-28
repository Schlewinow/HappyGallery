package com.schlewinow.happygallery.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.RadioGroup
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.settings.GallerySettings
import com.schlewinow.happygallery.tools.sorting.*

class SettingsSortingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_sorting)
        setSupportActionBar(findViewById(R.id.settingsSortingToolbar))

        supportActionBar?.title = resources.getString(R.string.settings_sorting_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sortingRadioGroup: RadioGroup = findViewById(R.id.settingsSortingRadioGroup)
        val ascending = GallerySettings.sortingComparator.ascending
        when {
            GallerySettings.sortingComparator is NumberAwareFileNameComparator && ascending -> sortingRadioGroup.check(R.id.settingsSortingNameAscendingRadioButton)
            GallerySettings.sortingComparator is NumberAwareFileNameComparator && !ascending -> sortingRadioGroup.check(R.id.settingsSortingNameDescendingRadioButton)
            GallerySettings.sortingComparator is FileSizeComparator && ascending -> sortingRadioGroup.check(R.id.settingsSortingSizeAscendingRadioButton)
            GallerySettings.sortingComparator is FileSizeComparator && !ascending -> sortingRadioGroup.check(R.id.settingsSortingSizeDescendingRadioButton)
            GallerySettings.sortingComparator is FileDateComparator && ascending -> sortingRadioGroup.check(R.id.settingsSortingLastModifiedAscendingRadioButton)
            GallerySettings.sortingComparator is FileDateComparator && !ascending -> sortingRadioGroup.check(R.id.settingsSortingLastModifiedDescendingRadioButton)
        }

        sortingRadioGroup.setOnCheckedChangeListener { radioGroup, index ->
            run {
                val selectedButtonId = radioGroup.checkedRadioButtonId
                var selectedComparator: BaseFileComparator = GallerySettings.sortingComparator

                when (selectedButtonId) {
                    R.id.settingsSortingNameAscendingRadioButton ->
                        selectedComparator = NumberAwareFileNameComparator(true)
                    R.id.settingsSortingNameDescendingRadioButton ->
                        selectedComparator = NumberAwareFileNameComparator(false)
                    R.id.settingsSortingSizeAscendingRadioButton ->
                        selectedComparator = FileSizeComparator(true)
                    R.id.settingsSortingSizeDescendingRadioButton ->
                        selectedComparator = FileSizeComparator(false)
                    R.id.settingsSortingLastModifiedAscendingRadioButton ->
                        selectedComparator = FileDateComparator(true)
                    R.id.settingsSortingLastModifiedDescendingRadioButton ->
                        selectedComparator = FileDateComparator(false)
                }

                GallerySettings.sortingComparator = selectedComparator
                GallerySettings.storeSettings(this)
            }
        }
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
}