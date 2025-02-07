package com.schlewinow.happygallery.views.elements

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.model.item.GalleryDirectoryContainer
import com.schlewinow.happygallery.model.item.GalleryFileContainer
import com.schlewinow.happygallery.settings.GallerySettings
import com.schlewinow.happygallery.tools.GalleryNavigationManager
import com.schlewinow.happygallery.views.GalleryNavigationActivity

/**
 * Holds the files and folders to be shown in the gallery navigation UI.
 * Applies sorting and other visual modifiers.
 */
class GalleryNavigationRecyclerAdapter(private val galleryNavigationActivity: GalleryNavigationActivity)
    : RecyclerView.Adapter<GalleryFileEntryHolder>() {

    private val currentDirs: MutableList<GalleryDirectoryContainer> = mutableListOf()
    private val currentFiles: MutableList<GalleryFileContainer> = mutableListOf()

    /**
     * View types are defined as:
     * 0 is a directory,
     * 1 is a file
     */
    override fun getItemViewType(position: Int): Int {
        if (position < currentDirs.size) {
            return 0
        }
        return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryFileEntryHolder {
        val view: View = when(viewType) {
            1 -> LayoutInflater.from(galleryNavigationActivity).inflate(R.layout.element_gallery_recycler_image, parent, false)
            else -> LayoutInflater.from(galleryNavigationActivity).inflate(R.layout.element_gallery_recycler_folder, parent, false)
        }
        return GalleryFileEntryHolder(view, galleryNavigationActivity)
    }

    override fun onBindViewHolder(holder: GalleryFileEntryHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> holder.setupDirectory(currentDirs[position], position, this)
            1 -> holder.setupImage(currentFiles[position - currentDirs.size])
        }
    }

    override fun getItemCount(): Int {
        return currentDirs.size + currentFiles.size
    }

    /**
     * Whenever the user navigates to a new directory or changes settings like sorting or filters,
     * the complete range of items in the recycler must be redrawn.
     * Update the item lists and force the UI to update.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun reloadItems() {
        var dirs = GalleryNavigationManager.currentDirectory.getChildDirectories().sortedWith(
            GallerySettings.sortingComparator)
        var files = GalleryNavigationManager.currentDirectory.getChildFiles().sortedWith(
            GallerySettings.sortingComparator)
        if (!GallerySettings.showHiddenFiles) {
            dirs = dirs.filter { file -> !file.name.startsWith(".") }
            files = files.filter { file -> !file.name.startsWith(".") }
        }

        currentDirs.clear()
        currentDirs.addAll(dirs)
        currentFiles.clear()
        currentFiles.addAll(files)

        // In fact, the whole item list has changed at this point.
        // Hence, we can suppress the warning.
        notifyDataSetChanged()
    }
}