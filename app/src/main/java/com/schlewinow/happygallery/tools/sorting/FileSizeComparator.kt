package com.schlewinow.happygallery.tools.sorting

import com.schlewinow.happygallery.model.item.GalleryBaseContainer

class FileSizeComparator(ascending: Boolean) : BaseFileComparator(ascending) {
    override val valueComparator: Comparator<GalleryBaseContainer>
        get() = object : Comparator<GalleryBaseContainer> {
            override fun compare(file1: GalleryBaseContainer?, file2: GalleryBaseContainer?): Int {
                if (ascending) {
                    return (file1!!.size).compareTo(file2!!.size)
                } else {
                    return (file2!!.size).compareTo(file1!!.size)
                }
            }
        }
}