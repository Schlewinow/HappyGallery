package com.schlewinow.happygallery.model.item

import android.os.Parcelable
import com.lazygeniouz.filecompat.file.DocumentFileCompat
import com.schlewinow.happygallery.settings.GallerySettings
import com.schlewinow.happygallery.tools.folders.DirectoryTools

/**
 * Gallery data container used by directories.
 */
class GalleryDirectoryContainer(fileContent: DocumentFileCompat) : GalleryBaseContainer(fileContent) {
    /**
     * Used to save and restore the scroll state for folders in the gallery recycler view.
     */
    var galleryRecyclerState: Parcelable? = null

    /**
     * Child file containers within this folder.
     * Only files supported by teh gallery will be listed.
     */
    private val childFiles: MutableList<GalleryFileContainer> = mutableListOf()

    /**
     * Child directory containers within this folder.
     */
    private val childDirectories : MutableList<GalleryDirectoryContainer> = mutableListOf()

    /**
     * Return the files inside the directory.
     * Triggers the load process if the files are not yet available.
     */
    fun getChildFiles() : List<GalleryFileContainer> {
        if (childFiles.isEmpty() && childDirectories.isEmpty()) {
            loadChildren()
        }

        if (GallerySettings.showHiddenFiles) {
            return childFiles
        }
        else {
            return childFiles.filter { child -> !child.name.startsWith(".") }
        }
    }

    /**
     * Return the directories inside the directory.
     * Triggers the load process if the directories are not yet available.
     */
    fun getChildDirectories() : List<GalleryDirectoryContainer> {
        if (childFiles.isEmpty() && childDirectories.isEmpty()) {
            loadChildren()
        }

        if (GallerySettings.showHiddenFiles) {
            return childDirectories
        }
        else {
            return childDirectories.filter { child -> !child.name.startsWith(".") }
        }
    }

    /**
     * Return the files and directories inside the directory.
     * Triggers the load process if the contents are not yet available.
     */
    fun getChildren() : List<GalleryBaseContainer> {
        return getChildDirectories() + getChildFiles()
    }

    /**
     * Load the child files and directories within this directory.
     */
    private fun loadChildren() {
        childDirectories.clear()
        val (dirs, files) = DirectoryTools.loadChildren(this)
        childDirectories.addAll(dirs)
        childFiles.addAll(files)
    }
}