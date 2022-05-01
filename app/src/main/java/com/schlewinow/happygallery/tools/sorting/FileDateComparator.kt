package com.schlewinow.happygallery.tools.sorting

import com.schlewinow.happygallery.model.GalleryFileContainer

class FileDateComparator(ascending: Boolean) : BaseFileComparator(ascending) {
    override val valueComparator: Comparator<GalleryFileContainer>
        get() = object : Comparator<GalleryFileContainer> {
            override fun compare(file1: GalleryFileContainer?, file2: GalleryFileContainer?): Int {
                if (ascending) {
                    return (file1!!.lastModified).compareTo(file2!!.lastModified)
                } else {
                    return (file2!!.lastModified).compareTo(file1!!.lastModified)
                }
            }
        }
}