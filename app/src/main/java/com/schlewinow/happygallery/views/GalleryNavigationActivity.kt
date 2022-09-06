package com.schlewinow.happygallery.views

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.model.GalleryFileContainer
import com.schlewinow.happygallery.model.GalleryNavigationData
import com.schlewinow.happygallery.model.VideoData
import com.schlewinow.happygallery.settings.GallerySettings
import com.schlewinow.happygallery.tools.folders.DirectoryTools
import com.schlewinow.happygallery.tools.folders.ImageFileTools
import com.schlewinow.happygallery.tools.folders.VideoFileTools

class GalleryNavigationActivity : AppCompatActivity() {
    private var fileRecycler: RecyclerView? = null

    private var isPortraitOrientation: Boolean = true

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

        fileRecycler?.layoutManager?.onRestoreInstanceState(GalleryNavigationData.fileRecyclerViewState)
    }

    override fun onPause() {
        super.onPause()

        GalleryNavigationData.fileRecyclerViewState = fileRecycler?.layoutManager?.onSaveInstanceState()
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
                if(isPortraitOrientation) {
                    GallerySettings.fileColumnsPortrait++
                } else {
                    GallerySettings.fileColumnsLandscape++
                }
                GallerySettings.storeSettings(this)

                // Complete redraw required because of updated thumbnails and layout.
                updateGalleryThumbnails()
                updateGalleryLayout()
                return true
            }
            R.id.menu_item_decrease_columns -> {
                if(isPortraitOrientation) {
                    GallerySettings.fileColumnsPortrait--
                } else {
                    GallerySettings.fileColumnsLandscape--
                }
                GallerySettings.storeSettings(this)

                // Complete redraw required because of updated thumbnails and layout.
                updateGalleryThumbnails()
                updateGalleryLayout()
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
        if(!navigateBack()) {
            super.onBackPressed()
        }
    }

    private fun setupGallery() {
        updateGalleryLayout()
        fileRecycler?.adapter = NavigationRecyclerAdapter(GalleryNavigationData.currentDirectoryFiles)
        updateGalleryElements()
    }

    private fun updateGalleryLayout() {
        var columns = GallerySettings.fileColumnsPortrait
        if(!isPortraitOrientation) {
           columns = GallerySettings.fileColumnsLandscape
        }

        val gridLayoutManager = GridLayoutManager(this, columns)
        fileRecycler?.layoutManager = gridLayoutManager
    }

    private fun updateGalleryElements() {
        GalleryNavigationData.updateCurrentDirectoryFiles()
        fileRecycler?.adapter?.notifyDataSetChanged()
    }

    private fun updateGalleryThumbnails() {
        // If the layout grid changed, a different thumbnail size must be requested.
        fileRecycler?.adapter?.notifyDataSetChanged()
    }

    private fun setupActionBar() {
        if(GalleryNavigationData.folderNavigationStack.isEmpty()) {
            supportActionBar?.title = resources.getString(R.string.gallery_title)
        } else {
            supportActionBar?.title = GalleryNavigationData.folderNavigationStack.last().name
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(!GalleryNavigationData.folderNavigationStack.isEmpty())
    }

    private fun navigateUp(target: GalleryFileContainer) {
        if(!GalleryNavigationData.folderNavigationStack.isEmpty()) {
            GalleryNavigationData.folderNavigationStack.last().galleryRecyclerState = fileRecycler?.layoutManager?.onSaveInstanceState()
        }

        GalleryNavigationData.folderNavigationStack.addLast(target)

        setupActionBar()
        updateGalleryElements()
        fileRecycler?.scrollToPosition(0)
    }

    private fun navigateBack(): Boolean {
        if(!GalleryNavigationData.folderNavigationStack.isEmpty()) {
            GalleryNavigationData.folderNavigationStack.removeLast()

            setupActionBar()
            updateGalleryElements()

            // Restoring recycler scroll state won't work in root folder overview.
            restoreFileRecyclerStateFromStack()
            return true
        }

        return false
    }

    private fun navigateToActivity(destination: Class<*>?, data: Uri? = null) {
        val navigationIntent = Intent(this, destination)
        if(data != null) {
            navigationIntent.data = data
        }
        startActivity(navigationIntent)
    }

    private fun restoreFileRecyclerStateFromStack() {
        if(!GalleryNavigationData.folderNavigationStack.isEmpty()) {
            val fileRecyclerState: Parcelable? = GalleryNavigationData.folderNavigationStack.last().galleryRecyclerState
            if(fileRecyclerState != null) {
                fileRecycler?.layoutManager?.onRestoreInstanceState(fileRecyclerState)
            }
        }
    }

    inner class NavigationRecyclerAdapter(private val files: MutableList<GalleryFileContainer>) : RecyclerView.Adapter<FileEntryHolder>() {
        override fun getItemViewType(position: Int): Int {
            if(files[position].isImage || files[position].isVideo) {
                return 1
            }
            return 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileEntryHolder {
            val view: View = when(viewType) {
                1 -> LayoutInflater.from(this@GalleryNavigationActivity).inflate(R.layout.element_gallery_recycler_image, parent, false)
                else -> LayoutInflater.from(this@GalleryNavigationActivity).inflate(R.layout.element_gallery_recycler_folder, parent, false)
            }
            return FileEntryHolder(view)
        }

        override fun onBindViewHolder(holder: FileEntryHolder, position: Int) {
            when(holder.itemViewType) {
               0 -> holder.setupDirectory(files[position])
               1 -> holder.setupImage(files[position])
            }
        }

        override fun getItemCount(): Int {
            return files.size
        }
    }

    inner class FileEntryHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun setupDirectory(galleryFile: GalleryFileContainer) {
            val nameText: TextView = view.findViewById(R.id.galleryFolderElementName)
            nameText.text = galleryFile.name

            val childrenCountText: TextView = view.findViewById(R.id.galleryFolderChildrenCount)
            childrenCountText.text ="${galleryFile.subFolders}-${galleryFile.subFiles}"

            val directoryPreviewImage: ImageView = view.findViewById(R.id.galleryFolderPreviewImage)
            directoryPreviewImage.setImageDrawable(null)
            val previewFile = DirectoryTools.getDirectoryPreviewImage(galleryFile)
            if(previewFile != null) {
                if(previewFile.isImage) {
                    ImageFileTools.loadThumbnail(this@GalleryNavigationActivity, previewFile, directoryPreviewImage, isPortraitOrientation)
                } else if(previewFile.isVideo) {
                    VideoFileTools.loadThumbnail(this@GalleryNavigationActivity, previewFile, directoryPreviewImage, isPortraitOrientation)
                }
            }

            view.setOnClickListener { navigateUp(galleryFile) }
        }

        fun setupImage(galleryFile: GalleryFileContainer) {
            val nameText: TextView = view.findViewById(R.id.galleryImageElementName)
            nameText.text = galleryFile.name

            val previewImage: ImageView = view.findViewById(R.id.galleryImagePreviewImage)
            previewImage.setImageDrawable(null)

            val movieBorder: ImageView = view.findViewById(R.id.galleryImageMovieBorder)

            if(galleryFile.isImage) {
                ImageFileTools.loadThumbnail(this@GalleryNavigationActivity, galleryFile, previewImage, isPortraitOrientation)
                view.setOnClickListener { navigateToActivity(ImageViewerActivity::class.java, galleryFile.file.uri) }
                movieBorder.visibility = View.GONE
            } else if(galleryFile.isVideo) {
                VideoFileTools.loadThumbnail(this@GalleryNavigationActivity, galleryFile, previewImage, isPortraitOrientation)
                view.setOnClickListener {
                    VideoData.reset()
                    navigateToActivity(VideoViewerVlcActivity::class.java, galleryFile.file.uri) }
                movieBorder.visibility = View.VISIBLE
            }
        }
    }
}