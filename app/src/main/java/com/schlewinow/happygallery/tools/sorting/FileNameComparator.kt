package com.schlewinow.happygallery.tools.sorting

import com.schlewinow.happygallery.model.GalleryFileContainer

class FileNameComparator(ascending: Boolean) : BaseFileComparator(ascending) {
    override val valueComparator: Comparator<GalleryFileContainer>
        get() = object : Comparator<GalleryFileContainer> {
            override fun compare(file1: GalleryFileContainer?, file2: GalleryFileContainer?): Int {
                if (ascending) {
                    return (file1!!.name).compareTo(file2!!.name)
                } else {
                    return (file2!!.name).compareTo(file1!!.name)
                }
            }
        }
}