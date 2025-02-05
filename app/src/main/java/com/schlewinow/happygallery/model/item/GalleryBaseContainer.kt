package com.schlewinow.happygallery.model.item

import android.net.Uri
import com.lazygeniouz.filecompat.file.DocumentFileCompat

open class GalleryBaseContainer(val contentFile: DocumentFileCompat) {
    val uri: Uri = contentFile.uri
    val name: String = contentFile.name
    val size: Long = contentFile.length
    val lastModified: Long = contentFile.lastModified
    val isDirectory: Boolean = !contentFile.isFile()
    val isGalleryFile: Boolean = contentFile.isFile()
}