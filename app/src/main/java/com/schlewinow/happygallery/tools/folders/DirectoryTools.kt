package com.schlewinow.happygallery.tools.folders

import android.os.Handler
import android.os.HandlerThread
import com.schlewinow.happygallery.model.GalleryFileContainer
import com.schlewinow.happygallery.settings.GallerySettings

/**
 * Support function to deal with directories.
 */
object DirectoryTools {
    /**
     * Maximum amount of active threads that are pre-loading directory contents.
     */
    private val maxThreadCount: Int = 10

    /**
     * Active amount of threads that are pre-loading directory contents.
     */
    @get:Synchronized @set:Synchronized
    private var activeThreadCount: Int = 0

    /**
     * Queue for the directories that should be pre-loaded, but can't because too many threads are already active.
     */
    private val preloadDirectoryQueue: MutableList<GalleryFileContainer> = mutableListOf()

    /**
     * Accessing directories is rather slow, so the access is pre-computed in parallel threads.
     * To avoid massive resource drain during the process, the maximum directories to be loaded at a time are limited.
     * If the limit is reached, new entries will be queued up instead of immediately processed.
     * @param directory The directory to be preloaded. Will add child directories recursively.
     */
    fun preloadChildDirectories(directory: GalleryFileContainer) {
        if (activeThreadCount < maxThreadCount) {
            startPreloadChildDirectoryThread(directory)
        } else {
            preloadDirectoryQueue.add(directory)
        }
    }

    /**
     * Accessing directories is rather slow, so the access is pre-computed and stored.
     * To collect directory structures, multiple threads are used to accelerate the process.
     * @param directory The directory to be preloaded. Will add child directories recursively.
     */
    private fun startPreloadChildDirectoryThread(directory: GalleryFileContainer) {
        ++activeThreadCount
        val directoryLoaderThread = HandlerThread(directory.file.uri.toString())
        directoryLoaderThread.start()
        val directoryLoadHandler = Handler(directoryLoaderThread.looper)

        directoryLoadHandler.post {
            // Avoid concurrent modification and racing conditions with the UI.
            synchronized(directory) {
                // Simple access will trigger the children to be loaded, if necessary.
                for(childDirectory in directory.getChildren().filter { dir -> dir.isDirectory }) {
                    preloadChildDirectories(childDirectory)
                }
            }
            directoryLoaderThread.quitSafely()
            --activeThreadCount

            // One thread finished, a new one may start now.
            if (preloadDirectoryQueue.isNotEmpty()) {
                startPreloadChildDirectoryThread(preloadDirectoryQueue.removeFirst())
            }
        }
    }

    fun getDirectoryPreviewImage(directory: GalleryFileContainer): GalleryFileContainer? {
        val childImages = directory.getChildren().filter { file -> file.isImage || file.isVideo }
        if(childImages.isNotEmpty()) {
            val sortedImages = childImages.sortedWith(GallerySettings.sortingComparator)
            return sortedImages.first()
        }

        return null
    }
}