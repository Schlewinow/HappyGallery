package com.schlewinow.happygallery.tools.sorting

import com.schlewinow.happygallery.model.item.GalleryBaseContainer

class FileDateComparator(ascending: Boolean) : BaseFileComparator(ascending) {
    override val valueComparator: Comparator<GalleryBaseContainer>
        get() = object : Comparator<GalleryBaseContainer> {
            override fun compare(file1: GalleryBaseContainer?, file2: GalleryBaseContainer?): Int {
                if (ascending) {
                    return (file1!!.lastModified).compareTo(file2!!.lastModified)
                } else {
                    return (file2!!.lastModified).compareTo(file1!!.lastModified)
                }
            }
        }
}