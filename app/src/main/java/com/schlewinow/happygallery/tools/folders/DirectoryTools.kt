package com.schlewinow.happygallery.tools.folders

import android.os.Handler
import android.os.HandlerThread
import com.schlewinow.happygallery.model.GalleryDirectoryLoadState
import com.schlewinow.happygallery.model.item.GalleryBaseContainer
import com.schlewinow.happygallery.model.item.GalleryDirectoryContainer
import com.schlewinow.happygallery.model.item.GalleryFileContainer
import com.schlewinow.happygallery.settings.GallerySettings
import java.util.Comparator

/**
 * Support functions to load directory contents.
 */
object DirectoryTools {
    /**
     * Maximum amount of active threads that are pre-loading directory contents.
     */
    private const val MAX_THREAD_COUNT: Int = 8

    /**
     * Active amount of threads that are pre-loading directory contents.
     */
    @get:Synchronized @set:Synchronized
    private var activeThreadCount: Int = 0

    /**
     * Queue for the directories that should be pre-loaded, but can't because too many threads are already active.
     */
    private val preloadDirectoryQueue: MutableList<LoadQueueEntry> = mutableListOf()

    /**
     * Collect the files and directories contained within a specific directory as GalleryFileContainers.
     * This will only include files which are supported by the gallery. Non-recursive.
     * @param parentDirectory The directory for which to collect the contained files.
     * @return A list of each collected directories and files as a pair.
     */
    fun loadChildren(parentDirectory: GalleryDirectoryContainer) : Pair<MutableList<GalleryDirectoryContainer>, MutableList<GalleryFileContainer>> {
        val childFileContainers: MutableList<GalleryFileContainer> = mutableListOf()
        val childDirectoryContainers: MutableList<GalleryDirectoryContainer> = mutableListOf()

        for (childDocFile in parentDirectory.docFile.listFiles()) {
            // Only add files supported by the gallery, ignore everything else.
            if (childDocFile.isFile() &&
                (ImageFileTools.checkIfImage(childDocFile) || VideoFileTools.checkIfVideo(childDocFile))) {
                childFileContainers.add(GalleryFileContainer(childDocFile))
            }
            // Double-check is required, as some files will wrongly show up as directories.
            else if (childDocFile.isDirectory() && !childDocFile.isFile()) {
                childDirectoryContainers.add(GalleryDirectoryContainer(childDocFile, parentDirectory))
            }
        }

        return Pair(childDirectoryContainers, childFileContainers)
    }

    /**
     * Accessing directories is rather slow, so the access is pre-computed in parallel threads.
     * To avoid massive resource drain during the process, the maximum directories to be loaded at a time are limited.
     * If the limit is reached, new entries will be queued up instead of immediately processed.
     * @param directory The directory to be preloaded. Will add child directories recursively.
     */
    fun preloadChildDirectories(directory: GalleryDirectoryContainer, priority: Int = 0) {
        directory.loadingState = GalleryDirectoryLoadState.QUEUED
        if (activeThreadCount < MAX_THREAD_COUNT) {
            startPreloadChildDirectoryThread(directory, priority)
        }
        else {
            synchronized(preloadDirectoryQueue) {
                preloadDirectoryQueue.add(LoadQueueEntry(directory, priority))
            }
        }
    }

    /**
     * Accessing directories is rather slow, so the access is pre-computed and stored.
     * To collect directory structures, multiple threads are used to accelerate the process.
     * @param directory The directory to be preloaded. Will add child directories recursively.
     */
    private fun startPreloadChildDirectoryThread(directory: GalleryDirectoryContainer, priority: Int) {
        ++activeThreadCount
        val directoryLoaderThread = HandlerThread(directory.docFile.uri.toString())
        directoryLoaderThread.start()
        val directoryLoadHandler = Handler(directoryLoaderThread.looper)

        directoryLoadHandler.post {
            // Avoid concurrent modification and racing conditions with the UI.
            synchronized(directory) {
                // Simple access will trigger the children to be loaded, if necessary.
                directory.loadChildren()
                for (childDirectory in directory.getChildDirectories()) {
                    preloadChildDirectories(childDirectory, priority + 1)
                }
            }
            directoryLoaderThread.quitSafely()
            --activeThreadCount
            directory.loadingState = GalleryDirectoryLoadState.FINISHED

            // Avoid racing condition removing an element from an already empty queue.
            synchronized(preloadDirectoryQueue) {
                // One thread finished, a new one may start now.
                if (preloadDirectoryQueue.isNotEmpty()) {
                    preloadDirectoryQueue.sortWith(LoadQueueComparator())
                    val nextEntry = preloadDirectoryQueue.removeAt(0)
                    startPreloadChildDirectoryThread(nextEntry.directory, nextEntry.priority)
                }
            }
        }
    }

    /**
     * Whenever the user navigates the gallery, the priorities should be updated
     * to focus on the current directory and its children.
     */
    fun updateLoadQueuePriority(topPriorityDirectory: GalleryDirectoryContainer) {
        // Priority change is expensive and synced, so avoid if not necessary.
        if (topPriorityDirectory.loadingState == GalleryDirectoryLoadState.FINISHED) {
            var allDone = true
            for (childDirectory in topPriorityDirectory.getChildDirectories()) {
                allDone = childDirectory.loadingState == GalleryDirectoryLoadState.FINISHED

                // Cancel early if possible.
                if(!allDone) {
                    break
                }
            }

            if (allDone) {
                return
            }
        }

        // Update priorities to focus on target folder and subfolders.
        synchronized(preloadDirectoryQueue) {
            for (queueElement: LoadQueueEntry in preloadDirectoryQueue) {
                if (queueElement.directory == topPriorityDirectory) {
                    queueElement.priority = 0
                }
                if (queueElement.directory.parentDirectory == topPriorityDirectory) {
                    queueElement.priority = 1
                }
            }
        }
    }

    /**
     * Apply sorting and filters as defined by the current settings to a list of gallery items.
     * @param galleryItems The directories and/or files to filter and sort.
     * @return The filtered and sorted list of items. Output type will be same as input type.
     */
    fun <T: GalleryBaseContainer>applySortingAndFilters(galleryItems: List<T>) : List<T> {
        // No need to filter or sort empty lists.
        if (galleryItems.isEmpty()) {
            return galleryItems
        }

        var filteredItems = galleryItems
        if (!GallerySettings.showHiddenFiles) {
            filteredItems = filteredItems.filter { file -> !file.name.startsWith(".") }
        }
        return filteredItems.sortedWith(GallerySettings.sortingComparator)
    }

    /**
     * Minimalistic container used to prioritize the directories to load.
     */
    private class LoadQueueEntry (val directory: GalleryDirectoryContainer, var priority: Int)

    /**
     * Comparator used to oder the directories by priority.
     */
    private class LoadQueueComparator: Comparator<LoadQueueEntry> {
        override fun compare(entry1: LoadQueueEntry?, entry2: LoadQueueEntry?): Int {
            return entry1?.priority!! - entry2?.priority!!
        }
    }
}