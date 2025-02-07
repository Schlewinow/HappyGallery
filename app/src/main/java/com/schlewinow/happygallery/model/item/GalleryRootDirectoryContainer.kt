package com.schlewinow.happygallery.model.item

/**
 * Technically, there is no actual root directory, but an overview containing the root physical directories.
 * This container allows to create an artificial directory containing the physical roots,
 * allowing for consistent data access throughout the code.
 */
class GalleryRootDirectoryContainer() : GalleryDirectoryContainer(null, null) {

    /**
     * The root directory manages the physical root directories managed by the user.
     * Add a physical root directory to the currently available root directories.
     */
    fun addChildDirectory(directory : GalleryDirectoryContainer) {
        childDirs.add(directory)
    }

    /**
     * The root directory manages the physical root directories managed by the user.
     * Remove a physical root directory from the currently available root directories.
     */
    fun removeChildDirectory(directory: GalleryDirectoryContainer) {
        childDirs.remove(directory)
    }
}