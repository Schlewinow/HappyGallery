package com.schlewinow.happygallery.tools.folders

import android.os.Handler
import android.os.HandlerThread
import com.schlewinow.happygallery.model.GalleryFileContainer
import com.schlewinow.happygallery.settings.GallerySettings

/**
 *
 */
object DirectoryTools {
    /**
     * Accessing directories is rather slow, so the access is pre-computed and stored.
     * To collect directory structures, multiple threads are used to accelerate the process.
     */
    fun preloadChildDirectories(directory: GalleryFileContainer) {
        val directoryLoaderThread = HandlerThread(directory.file.uri.toString())
        directoryLoaderThread.start()
        val directoryLoadHandler = Handler(directoryLoaderThread.looper)

        directoryLoadHandler.post{
            // Avoid concurrent modification and racing conditions with the UI.
            synchronized(directory) {
                // Simple access will trigger the children to be loaded, if necessary.
                for(childDirectory in directory.getChildren().filter { dir -> dir.isDirectory }) {
                    preloadChildDirectories(childDirectory)
                }
            }
            directoryLoaderThread.quitSafely()
        }
    }

    fun getDirectoryPreviewImage(directory: GalleryFileContainer): GalleryFileContainer? {
        val childImages = directory.getChildren().filter { file -> file.isImage || file.isVideo }
        if(!childImages.isEmpty()) {
            val sortedImages = childImages.sortedWith(GallerySettings.sortingComparator)
            return sortedImages.first()
        }

        return null
    }
}