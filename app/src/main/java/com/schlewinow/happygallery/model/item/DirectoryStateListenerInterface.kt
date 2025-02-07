package com.schlewinow.happygallery.model.item

/**
 * Simple observer to be used by the UI to properly reflect directory load state changes.
 */
interface DirectoryStateListenerInterface {
    /**
     * Callback on directory load state change.
     * @param galleryDirectory The directory containing the updated state.
     */
    fun onLoadStateChanged(galleryDirectory: GalleryDirectoryContainer)
}