package com.schlewinow.happygallery.tools.folders

import android.os.Handler
import android.os.HandlerThread
import com.schlewinow.happygallery.model.item.GalleryDirectoryContainer
import com.schlewinow.happygallery.model.item.GalleryFileContainer
import com.schlewinow.happygallery.settings.GallerySettings

/**
 * Support functions to load directory contents.
 */
object DirectoryTools {
    /**
     * Maximum amount of active threads that are pre-loading directory contents.
     */
    private const val MAX_THREAD_COUNT: Int = 10

    /**
     * Active amount of threads that are pre-loading directory contents.
     */
    @get:Synchronized @set:Synchronized
    private var activeThreadCount: Int = 0

    /**
     * Queue for the directories that should be pre-loaded, but can't because too many threads are already active.
     */
    private val preloadDirectoryQueue: MutableList<GalleryDirectoryContainer> = mutableListOf()

    /**
     * Collect the files and directories contained within a specific directory as GalleryFileContainers.
     * This will only include files which are supported by the gallery. Non-recursive.
     * @param parentDirectory The directory for which to collect the contained files.
     * @return A list of each collected directories and files as a pair.
     */
    fun loadChildren(parentDirectory: GalleryDirectoryContainer) : Pair<MutableList<GalleryDirectoryContainer>, MutableList<GalleryFileContainer>> {
        synchronized(this) {
            val childFileContainers: MutableList<GalleryFileContainer> = mutableListOf()
            val childDirectoryContainers: MutableList<GalleryDirectoryContainer> = mutableListOf()

            for (childFile in parentDirectory.contentFile.listFiles()) {
                if (childFile.isFile() &&
                    (ImageFileTools.checkIfImage(childFile) || VideoFileTools.checkIfVideo(childFile))) {
                    childFileContainers.add(GalleryFileContainer(childFile))
                }
                else if (childFile.isDirectory()) {
                    childDirectoryContainers.add(GalleryDirectoryContainer(childFile))
                }
            }

            return Pair(childDirectoryContainers, childFileContainers)
        }
    }

    /**
     * Accessing directories is rather slow, so the access is pre-computed in parallel threads.
     * To avoid massive resource drain during the process, the maximum directories to be loaded at a time are limited.
     * If the limit is reached, new entries will be queued up instead of immediately processed.
     * @param directory The directory to be preloaded. Will add child directories recursively.
     */
    fun preloadChildDirectories(directory: GalleryDirectoryContainer) {
        if (activeThreadCount < MAX_THREAD_COUNT) {
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
    private fun startPreloadChildDirectoryThread(directory: GalleryDirectoryContainer) {
        ++activeThreadCount
        val directoryLoaderThread = HandlerThread(directory.contentFile.uri.toString())
        directoryLoaderThread.start()
        val directoryLoadHandler = Handler(directoryLoaderThread.looper)

        directoryLoadHandler.post {
            // Avoid concurrent modification and racing conditions with the UI.
            synchronized(directory) {
                // Simple access will trigger the children to be loaded, if necessary.
                for (childDirectory in directory.getChildren().filter { dir -> dir.isDirectory }) {
                    preloadChildDirectories(childDirectory as GalleryDirectoryContainer)
                }
            }
            directoryLoaderThread.quitSafely()
            --activeThreadCount

            // Avoid racing condition removing an element from an already empty queue.
            synchronized(preloadDirectoryQueue) {
                // One thread finished, a new one may start now.
                if (preloadDirectoryQueue.isNotEmpty()) {
                    startPreloadChildDirectoryThread(preloadDirectoryQueue.removeFirst())
                }
            }
        }
    }

    fun getDirectoryPreviewImage(directory: GalleryDirectoryContainer): GalleryFileContainer? {
        val childImages = directory.getChildFiles()
        if (childImages.isNotEmpty()) {
            val sortedImages = childImages.sortedWith(GallerySettings.sortingComparator)
            return sortedImages.first()
        }
        return null
    }
}