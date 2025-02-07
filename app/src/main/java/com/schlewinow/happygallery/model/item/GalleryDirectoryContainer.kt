package com.schlewinow.happygallery.model.item

import android.os.Parcelable
import com.lazygeniouz.filecompat.file.DocumentFileCompat
import com.schlewinow.happygallery.model.GalleryDirectoryLoadState
import com.schlewinow.happygallery.tools.folders.DirectoryTools

/**
 * Gallery data container used by directories.
 */
open class GalleryDirectoryContainer(
    private val contentFile: DocumentFileCompat?, val parentDirectory: GalleryDirectoryContainer?)
    : GalleryBaseContainer(contentFile) {

    /**
     * Required for loading the contents inside this directory.
     * Usage should be kept minimal. Uri and other stats are available per base class.
     */
    val docFile: DocumentFileCompat
        get() = contentFile!!

    /**
     * Used to save and restore the scroll state for folders in the gallery recycler view.
     */
    var galleryRecyclerState: Parcelable? = null

    /**
     * The loading state of this directories content. Non-recursive.
     * Changing the state triggers a notification to its listeners.
     */
    var loadingState: GalleryDirectoryLoadState = GalleryDirectoryLoadState.NOT_LOADED
        set(value) {
            synchronized(this) {
                field = value
                if (loadingStateListener != null) {
                    loadingStateListener?.onLoadStateChanged(this)

                    // Final state reached. Clean up listener to avoid dead UI references.
                    if (field == GalleryDirectoryLoadState.FINISHED) {
                        loadingStateListener = null
                    }
                }
            }
        }

    /**
     * To keep the UI up to date on the current directory loading state,
     * the gallery UI may add a listener to a directory.
     * Only a single listener is required, as only a single UI cell may be connected at a time.
     */
    private var loadingStateListener: DirectoryStateListenerInterface? = null

    /**
     * Child file containers within this folder.
     * Only files supported by the gallery will be listed.
     */
    private val childFiles: MutableList<GalleryFileContainer> = mutableListOf()

    /**
     * Child directory containers within this folder.
     */
    protected val childDirs : MutableList<GalleryDirectoryContainer> = mutableListOf()

    fun setLoadStateListener(listener: DirectoryStateListenerInterface) {
        synchronized(this) {
            loadingStateListener = listener
        }
    }

    /**
     * When navigation changes the current gallery directory, it should clear any listeners.
     * Listeners are only required on UI cells currently shown to the user.
     */
    fun clearLoadStateListener() {
        synchronized(this) {
            loadingStateListener = null
        }
    }

    /**
     * Return the files inside the directory.
     * @return List containing child files
     */
    fun getChildFiles() : List<GalleryFileContainer> {
        return childFiles
    }

    /**
     * Return the directories inside the directory.
     * @return List containing child directories.
     */
    fun getChildDirectories() : List<GalleryDirectoryContainer> {
        return childDirs
    }

    /**
     * Load the child files and directories within this directory.
     * Used as part of the process running in background threads managed by DirectoryTools.
     */
    fun loadChildren() {
        // Should only happen at root folders.
        // If the content file is null, the directory tools will crash when accessing the child files.
        if (contentFile == null) {
            return
        }

        childDirs.clear()
        val (dirs, files) = DirectoryTools.loadChildren(this)
        childDirs.addAll(dirs)
        childFiles.addAll(files)
    }
}