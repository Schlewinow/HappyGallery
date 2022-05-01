package com.schlewinow.happygallery.tools.sorting

import com.schlewinow.happygallery.model.GalleryFileContainer

/**
 * Base comparable for file sorting actions.
 * Will keep all folders at the start, regardless of sorting.
 */
abstract class BaseFileComparator(val ascending: Boolean) : Comparator<GalleryFileContainer> {
    protected abstract val valueComparator: Comparator<GalleryFileContainer>
    private val minor = if(ascending) -1 else 1
    private val major = if(ascending) 1 else -1

    override fun compare(file1: GalleryFileContainer?, file2: GalleryFileContainer?): Int {
        when {
            file1 == null && file2 == null -> return 0
            file1 == null -> return minor
            file2 == null -> return major
        }

        val file1Directory = file1!!.isDirectory
        val file2Directory = file2!!.isDirectory

        // Check if both files are folders or both files are non-folders.
        if((file1Directory && file2Directory) || (!file1Directory && !file2Directory)) {
            // Inheriting classes will use a certain property at this point, like file name or size.
            return valueComparator.compare(file1, file2)
        }

        // If one is a folder and the other is not, folder comes first.
        // This is independent of ascending or descending order.
        if(file1Directory) {
            return -1
        } else {
            return 1
        }
    }
}