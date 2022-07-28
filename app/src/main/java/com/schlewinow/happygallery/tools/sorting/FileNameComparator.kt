package com.schlewinow.happygallery.tools.sorting

import com.schlewinow.happygallery.model.GalleryFileContainer

/**
 * Sort gallery entries by file name.
 * Deprecated by [NumberAwareFileNameComparator].
 */
class FileNameComparator(ascending: Boolean) : BaseFileComparator(ascending) {
    override val valueComparator: Comparator<GalleryFileContainer>
        get() = object : Comparator<GalleryFileContainer> {
            override fun compare(file1: GalleryFileContainer?, file2: GalleryFileContainer?): Int {
                if (ascending) {
                    return (file1!!.name.lowercase()).compareTo(file2!!.name.lowercase())
                } else {
                    return (file2!!.name.lowercase()).compareTo(file1!!.name.lowercase())
                }
            }
        }
}