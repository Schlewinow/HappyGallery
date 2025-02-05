package com.schlewinow.happygallery.model.item

import com.schlewinow.happygallery.tools.folders.ImageFileTools
import com.lazygeniouz.filecompat.file.DocumentFileCompat
import com.schlewinow.happygallery.tools.folders.VideoFileTools

/**
 * Gallery data container used by files.
 * Only supported files will be listed by any requests, which are limited to certain image and video formats.
 */
class GalleryFileContainer(contentFile: DocumentFileCompat) : GalleryBaseContainer(contentFile) {
    /**
     * File type defined as file extension.
     */
    val type: String = contentFile.extension

    /**
     * True if the file is a supported image type, false otherwise.
     */
    val isImage: Boolean = ImageFileTools.checkIfImage(contentFile)

    /**
     * True if the file is a supported video type, false otherwise.
     */
    val isVideo: Boolean = VideoFileTools.checkIfVideo(contentFile)
}