package com.schlewinow.happygallery.model

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.schlewinow.happygallery.settings.GallerySettings
import com.schlewinow.happygallery.tools.folders.DirectoryTools
import com.lazygeniouz.filecompat.file.DocumentFileCompat

/**
 * Runtime navigation data.
 * Stores current directory and works as cache for some costly operations.
 */
object GalleryNavigationData {
    val folderNavigationStack: ArrayDeque<GalleryFileContainer> = ArrayDeque()

    val currentDirectoryFiles: MutableList<GalleryFileContainer> = mutableListOf()

    private val rootDirectoryGalleryContainers: HashMap<Uri, GalleryFileContainer> = HashMap()

    var fileRecyclerViewState: Parcelable? = null

    var statusBarHeight: Int = 0

    var navigationBarHeight: Int = 0

    fun loadRootDirectoryGalleryContainers(rootDirectory: DocumentFile, context: Context) {
        val rootGalleryContainer = GalleryFileContainer(DocumentFileCompat.fromTreeUri(context, rootDirectory.uri)!!)
        rootDirectoryGalleryContainers[rootDirectory.uri] = rootGalleryContainer
        // Start loading the sub-directories in background threads.
        DirectoryTools.preloadChildDirectories(rootGalleryContainer)
    }

    fun removeRootDirectoryGalleryContainers(rootDirectoryUri: Uri) {
        rootDirectoryGalleryContainers.remove(rootDirectoryUri)
    }

    fun updateCurrentDirectoryFiles() {
        currentDirectoryFiles.clear()

        if(folderNavigationStack.isEmpty()) {
            currentDirectoryFiles.addAll(rootDirectoryGalleryContainers.values)
        } else {
            currentDirectoryFiles.addAll(folderNavigationStack.last().getChildren())
        }

        currentDirectoryFiles.sortWith(GallerySettings.sortingComparator)
    }
}