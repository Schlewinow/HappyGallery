package com.schlewinow.happygallery.views

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.tools.GalleryNavigationManager
import com.schlewinow.happygallery.model.item.GalleryDirectoryContainer
import com.schlewinow.happygallery.settings.GallerySettings
import com.schlewinow.happygallery.tools.folders.DirectoryTools
import com.schlewinow.happygallery.views.elements.GalleryNavigationRecyclerAdapter

/**
 * Main view of the application.
 * Shows a gallery overview of files and directories and allows navigation between those.
 * Entering a directory does not open a new activity, but instead update the current view.
 * Hence, the [GalleryNavigationManager] is used to provide additional storage and functionality in navigation.
 * Directories are loaded on the run, so not all info may be available while entering a directory.
 * It will load and update though with the help of the [DirectoryTools].
 */
class GalleryNavigationActivity : AppCompatActivity() {
    private var fileRecycler: RecyclerView? = null

    var isPortraitOrientation: Boolean = true
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_navigation)
        setSupportActionBar(findViewById(R.id.navigationToolbar))

        fileRecycler = findViewById(R.id.navigationFileRecycler)
        isPortraitOrientation = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    override fun onResume() {
        super.onResume()

        setupActionBar()
        setupGallery()

        fileRecycler?.layoutManager?.onRestoreInstanceState(GalleryNavigationManager.currentDirectory.galleryRecyclerState)
    }

    override fun onPause() {
        super.onPause()

        GalleryNavigationManager.currentDirectory.galleryRecyclerState = fileRecycler?.layoutManager?.onSaveInstanceState()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_gallery_navigation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.menu_item_sorting -> {
                navigateToActivity(SettingsSortingActivity::class.java)
                return true
            }
            R.id.menu_item_increase_columns -> {
                if (isPortraitOrientation) {
                    GallerySettings.fileColumnsPortrait++
                } else {
                    GallerySettings.fileColumnsLandscape++
                }
                GallerySettings.storeSettings(this)

                // Complete redraw required because of updated layout.
                updateGalleryLayout()
                return true
            }
            R.id.menu_item_decrease_columns -> {
                if (isPortraitOrientation) {
                    GallerySettings.fileColumnsPortrait--
                } else {
                    GallerySettings.fileColumnsLandscape--
                }
                GallerySettings.storeSettings(this)

                // Complete redraw required because of updated layout.
                updateGalleryLayout()
                return true
            }
            R.id.menu_item_show_hidden_files -> {
                GallerySettings.showHiddenFiles = true
                GallerySettings.storeSettings(this)
                updateGalleryElements()
                return true
            }
            R.id.menu_item_hide_hidden_files -> {
                GallerySettings.showHiddenFiles = false
                GallerySettings.storeSettings(this)
                updateGalleryElements()
                return true
            }
            R.id.menu_item_root_folders -> {
                navigateToActivity(SettingsRootFolderActivity::class.java)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!navigateBack()) {
            super.onBackPressed()
        }
    }

    private fun setupGallery() {
        updateGalleryLayout()
        fileRecycler?.adapter = GalleryNavigationRecyclerAdapter(this)
        updateGalleryElements()
    }

    /**
     * Change the gallery layout depending on the current device orientation.
     * Forces a redraw of all element cells.
     */
    private fun updateGalleryLayout() {
        var columns = GallerySettings.fileColumnsPortrait
        if (!isPortraitOrientation) {
           columns = GallerySettings.fileColumnsLandscape
        }

        val gridLayoutManager = GridLayoutManager(this, columns)
        fileRecycler?.layoutManager = gridLayoutManager
    }

    /**
     * Update the contents and force redraw of all element cells.
     */
    private fun updateGalleryElements() {
        (fileRecycler?.adapter as GalleryNavigationRecyclerAdapter).reloadItems()
    }

    private fun setupActionBar() {
        if (GalleryNavigationManager.isAtRoot()) {
            supportActionBar?.title = resources.getString(R.string.gallery_title)
        }
        else {
            supportActionBar?.title = GalleryNavigationManager.currentDirectory.name
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(!GalleryNavigationManager.isAtRoot())
    }

    fun navigateToDirectory(target: GalleryDirectoryContainer) {
        GalleryNavigationManager.navigateTo(target, fileRecycler?.layoutManager?.onSaveInstanceState())

        setupActionBar()
        updateGalleryElements()
        fileRecycler?.scrollToPosition(0)
    }

    private fun navigateBack(): Boolean {
        if (!GalleryNavigationManager.isAtRoot()) {
            GalleryNavigationManager.navigateBack()

            setupActionBar()
            updateGalleryElements()

            restoreFileRecyclerStateFromStack()
            return true
        }

        return false
    }

    /**
     * Most navigation stays inside the gallery view.
     * This call is used to navigate into a new activity outside of the gallery view.
     * @param destination Type of activity to navigate to.
     * @param data A uri as parameter towards the target view. Since the gallery will open media,
     * like images or videos, pass the file uri here.
     */
    fun navigateToActivity(destination: Class<*>?, data: Uri? = null) {
        val navigationIntent = Intent(this, destination)
        if (data != null) {
            navigationIntent.data = data
        }
        startActivity(navigationIntent)
    }

    /**
     * When navigating back, the last folder on the stack should have scroll info stored.
     * Use these to restore the previous position of the view.
     */
    private fun restoreFileRecyclerStateFromStack() {
        val fileRecyclerState: Parcelable? = GalleryNavigationManager.currentDirectory.galleryRecyclerState
        if (fileRecyclerState != null) {
            fileRecycler?.layoutManager?.onRestoreInstanceState(fileRecyclerState)
        }
    }
}