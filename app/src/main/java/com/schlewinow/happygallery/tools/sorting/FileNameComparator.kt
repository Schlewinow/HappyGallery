package com.schlewinow.happygallery.tools.sorting

import com.schlewinow.happygallery.model.item.GalleryBaseContainer

/**
 * Sort gallery entries by file name.
 * Deprecated by [NumberAwareFileNameComparator].
 */
class FileNameComparator(ascending: Boolean) : BaseFileComparator(ascending) {
    override val valueComparator: Comparator<GalleryBaseContainer>
        get() = object : Comparator<GalleryBaseContainer> {
            override fun compare(file1: GalleryBaseContainer?, file2: GalleryBaseContainer?): Int {
                if (ascending) {
                    return (file1!!.name.lowercase()).compareTo(file2!!.name.lowercase())
                } else {
                    return (file2!!.name.lowercase()).compareTo(file1!!.name.lowercase())
                }
            }
        }
}