package com.schlewinow.happygallery.tools

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.schlewinow.happygallery.tools.folders.DirectoryTools
import com.lazygeniouz.filecompat.file.DocumentFileCompat
import com.schlewinow.happygallery.model.item.GalleryDirectoryContainer
import com.schlewinow.happygallery.model.item.GalleryRootDirectoryContainer

/**
 * Runtime navigation manager used with the gallery view.
 * Stores current directory and works as cache for some costly operations.
 */
object GalleryNavigationManager {
    /**
     * Stores the directories through which the user has navigated so far.
     * When navigating up, directories are removed accordingly.
     */
    private val folderNavigationStack: ArrayDeque<GalleryDirectoryContainer> = ArrayDeque()

    /**
     * The user must specify the root directories to navigate in the app.
     * These are the highest level directories available for navigation and shown at app start.
     */
    private val rootDirectoryContainer : GalleryRootDirectoryContainer = GalleryRootDirectoryContainer()

    /**
     * The directory which the user is currently browsing in the gallery UI.
     * Updated by using the proper navigation calls in this class, like navigateTo().
     */
    var currentDirectory: GalleryDirectoryContainer = rootDirectoryContainer
        private set

    /**
     * Size of the status bar at the top of the app, showing system info.
     * Set once at the start of the app.
     * Used in full screen views to properly place the UI elements.
     */
    var statusBarHeight: Int = 0

    /**
     * Size of the navigation bar at the top of the app, showing system info.
     * Set once at the start of the app.
     * Used in full screen views to properly place the UI elements.
     */
    var navigationBarHeight: Int = 0

    /**
     * Add an additional physical directory to the currently available roots.
     * Triggers loading the directory contents as well.
     * Has no effect if selected document uri is already  among current root directories.
     * @param rootDirectory Directory to be added to the root directories.
     * @param context Local app context. Usually current activity.
     */
    fun loadRootDirectoryGalleryContainers(rootDirectory: DocumentFile, context: Context) {
        // Don't add same directories twice.
        if (rootDirectoryContainer.getChildDirectories().any { dir -> dir.uri == rootDirectory.uri }) {
            return
        }

        val newRootDirectoryContainer = GalleryDirectoryContainer(DocumentFileCompat.fromTreeUri(context, rootDirectory.uri)!!, null)
        rootDirectoryContainer.addChildDirectory(newRootDirectoryContainer)

        // Start loading the sub-directories in background threads.
        DirectoryTools.preloadChildDirectories(newRootDirectoryContainer)
    }

    /**
     * Remove a directory from the available root directories.
     * User may do so in the settings UI.
     * @param rootDirectoryUri Uri of the directory to remove. Has no effect if selected uri is not among current root directories.
     */
    fun removeRootDirectoryGalleryContainers(rootDirectoryUri: Uri) {
        try {
            val removeRootDirectoryContainer = rootDirectoryContainer.getChildDirectories().first { dir -> dir.uri == rootDirectoryUri }
            rootDirectoryContainer.removeChildDirectory(removeRootDirectoryContainer)
        }
        catch (nseEx: NoSuchElementException) {
            nseEx.printStackTrace()
        }
    }

    /**
     * Check if the user is currently at the root gallery folder.
     * @return True if the user is currently at the gallery root directory, false otherwise.
     */
    fun isAtRoot(): Boolean {
        return folderNavigationStack.isEmpty()
    }

    /**
     * Update the current directory in the navigation manager and place it on the navigation stack.
     * @param target The directory to navigate to.
     * @param galleryRecyclerState Optional recycler state of the current gallery view. Used to restore scroll state on back navigation.
     */
    fun navigateTo(target: GalleryDirectoryContainer, galleryRecyclerState: Parcelable?) {
        // RecyclerView will re-use visual elements, so to avoid pseudo-random UI updates,
        // clear all state update listeners when leaving a directory.
        for(childDirectory: GalleryDirectoryContainer in currentDirectory.getChildDirectories()) {
            childDirectory.clearLoadStateListener()
        }

        currentDirectory.galleryRecyclerState = galleryRecyclerState
        folderNavigationStack.addLast(target)
        currentDirectory = target

        DirectoryTools.updateLoadQueuePriority(target)
    }

    /**
     * Navigate back and remove the current directory from the navigation stack.
     * Can't move past the root directory.
     */
    fun navigateBack() {
        folderNavigationStack.removeLast()
        if (folderNavigationStack.isNotEmpty()) {
            currentDirectory = folderNavigationStack.last()
        }
        else {
            currentDirectory = rootDirectoryContainer
        }
    }
}