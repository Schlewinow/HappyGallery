package com.schlewinow.happygallery.tools.sorting

import com.schlewinow.happygallery.model.GalleryFileContainer

class FileSizeComparator(ascending: Boolean) : BaseFileComparator(ascending) {
    override val valueComparator: Comparator<GalleryFileContainer>
        get() = object : Comparator<GalleryFileContainer> {
            override fun compare(file1: GalleryFileContainer?, file2: GalleryFileContainer?): Int {
                if (ascending) {
                    return (file1!!.size).compareTo(file2!!.size)
                } else {
                    return (file2!!.size).compareTo(file1!!.size)
                }
            }
        }
}