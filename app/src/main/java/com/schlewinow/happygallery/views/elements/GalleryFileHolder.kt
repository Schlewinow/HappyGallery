package com.schlewinow.happygallery.views.elements

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.model.GalleryDirectoryLoadState
import com.schlewinow.happygallery.model.VideoProgressData
import com.schlewinow.happygallery.model.DirectoryStateListenerInterface
import com.schlewinow.happygallery.model.item.GalleryDirectoryContainer
import com.schlewinow.happygallery.model.item.GalleryFileContainer
import com.schlewinow.happygallery.tools.folders.DirectoryTools.applySortingAndFilters
import com.schlewinow.happygallery.tools.folders.ImageFileTools
import com.schlewinow.happygallery.tools.folders.VideoFileTools
import com.schlewinow.happygallery.views.GalleryNavigationActivity
import com.schlewinow.happygallery.views.ImageViewerActivity
import com.schlewinow.happygallery.views.VideoViewerVlcActivity

class GalleryFileEntryHolder(
    private val view: View,
    private val galleryNavigationActivity: GalleryNavigationActivity
    ) : RecyclerView.ViewHolder(view), DirectoryStateListenerInterface {

    /**
     * Index inside the recycler view adapter. Used to optimize redraw requests.
     */
    private var position: Int = 0

    /**
     * Used to trigger redraw requests once the loading status changes.
     */
    private var adapter: RecyclerView.Adapter<GalleryFileEntryHolder>? = null

    fun setupDirectory(galleryDirectory: GalleryDirectoryContainer, position: Int, adapter: RecyclerView.Adapter<GalleryFileEntryHolder>) {
        this.position = position
        this.adapter = adapter

        val nameText: TextView = view.findViewById(R.id.galleryFolderElementName)
        nameText.text = galleryDirectory.name

        val childrenCountText: TextView = view.findViewById(R.id.galleryFolderChildrenCount)
        if (galleryDirectory.loadingState == GalleryDirectoryLoadState.NOT_LOADED) {
            childrenCountText.text = ""
        }
        else if (galleryDirectory.loadingState == GalleryDirectoryLoadState.QUEUED) {
            childrenCountText.text = "loading"
        }

        // Only add listener if status isn't finished already to save performance.
        if (galleryDirectory.loadingState != GalleryDirectoryLoadState.FINISHED) {
            galleryDirectory.setLoadStateListener(this)
        }

        val directoryPreviewImage: ImageView = view.findViewById(R.id.galleryFolderPreviewImage)
        // Required in case there is an unfinished preview image loading process.
        Glide.with(galleryNavigationActivity).clear(directoryPreviewImage)
        directoryPreviewImage.setImageDrawable(null)

        // Only start loading preview image once files are actually available.
        if (galleryDirectory.loadingState == GalleryDirectoryLoadState.FINISHED) {
            // Find preview image by sorting the files inside the child folder.
            val filteredAndSortedFiles = applySortingAndFilters(galleryDirectory.getChildFiles())
            var previewFile: GalleryFileContainer? = null
            if (filteredAndSortedFiles.isNotEmpty()) {
                previewFile = filteredAndSortedFiles.first()
            }

            // Apply selected preview image.
            if (previewFile != null) {
                if (previewFile.isImage) {
                    ImageFileTools.loadThumbnail(galleryNavigationActivity, previewFile, directoryPreviewImage, galleryNavigationActivity.isPortraitOrientation)
                }
                else if (previewFile.isVideo) {
                    VideoFileTools.loadThumbnail(galleryNavigationActivity, previewFile, directoryPreviewImage, galleryNavigationActivity.isPortraitOrientation)
                }
            }

            // Since files are already filtered and sorted,
            // do the same with directories now to get the correct child directory count.
            val filteredAndSortedDirs = applySortingAndFilters(galleryDirectory.getChildDirectories())
            childrenCountText.text = "${filteredAndSortedDirs.count()}-${filteredAndSortedFiles.count()}"
        }

        view.setOnClickListener {
            galleryNavigationActivity.navigateToDirectory(galleryDirectory)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onLoadStateChanged(galleryDirectory: GalleryDirectoryContainer) {
        // Make sure to call this on the UI thread,
        // as the state update will most likely be called from a separate thread.
        view.post {
            // Triggers update to redraw the complete cell.
            adapter?.notifyItemChanged(position)
        }
    }

    fun setupImage(galleryFile: GalleryFileContainer) {
        val nameText: TextView = view.findViewById(R.id.galleryImageElementName)
        nameText.text = galleryFile.name

        val previewImage: ImageView = view.findViewById(R.id.galleryImagePreviewImage)
        // Required in case there is an unfinished preview image loading process.
        Glide.with(galleryNavigationActivity).clear(previewImage)
        previewImage.setImageDrawable(null)

        val previewMovieBorder: ImageView = view.findViewById(R.id.galleryImageMovieBorder)

        if (galleryFile.isImage) {
            ImageFileTools.loadThumbnail(galleryNavigationActivity, galleryFile, previewImage, galleryNavigationActivity.isPortraitOrientation)
            view.setOnClickListener {
                galleryNavigationActivity.navigateToActivity(ImageViewerActivity::class.java, galleryFile.uri)
            }
            previewMovieBorder.visibility = View.GONE
        }
        else if (galleryFile.isVideo) {
            VideoFileTools.loadThumbnail(galleryNavigationActivity, galleryFile, previewImage, galleryNavigationActivity.isPortraitOrientation)
            view.setOnClickListener {
                VideoProgressData.reset()
                galleryNavigationActivity.navigateToActivity(VideoViewerVlcActivity::class.java, galleryFile.uri)
            }
            previewMovieBorder.visibility = View.VISIBLE
        }
    }
}