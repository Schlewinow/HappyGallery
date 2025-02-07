package com.schlewinow.happygallery.model.item

import android.net.Uri
import com.lazygeniouz.filecompat.file.DocumentFileCompat

open class GalleryBaseContainer(contentFile: DocumentFileCompat?) {
    val uri: Uri = contentFile?.uri ?: Uri.EMPTY
    val name: String = contentFile?.name ?: ""
    val size: Long = contentFile?.length ?: 0L
    val lastModified: Long = contentFile?.lastModified ?: 0L
    val isDirectory: Boolean = contentFile?.isDirectory() ?: false
    val isGalleryFile: Boolean = contentFile?.isFile() ?: false
}