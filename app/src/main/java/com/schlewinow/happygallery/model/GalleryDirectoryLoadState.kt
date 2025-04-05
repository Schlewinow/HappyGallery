package com.schlewinow.happygallery.model

/**
 * Used to identify the loading state of directories.
 * Accessing files via OS calls is generally slow on Android,
 * so every file access is pre-loaded and stored in GalleryDirectoryContainers.
 */
enum class GalleryDirectoryLoadState {
    /**
     * Item is not yet loaded and did not start loading process.
     */
    NOT_LOADED,

    /**
     * Item was queued up for load, but is not yet being processed.
     */
    QUEUED,

    /**
     * Item was completely processed.
     */
    FINISHED
}