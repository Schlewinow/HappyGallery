package com.schlewinow.happygallery.model

import android.net.Uri
import android.os.Parcelable
import com.schlewinow.happygallery.tools.folders.ImageFileTools
import com.lazygeniouz.filecompat.file.DocumentFileCompat
import com.schlewinow.happygallery.tools.folders.VideoFileTools

class GalleryFileContainer(val file: DocumentFileCompat) {
    val uri: Uri = file.uri
    val isDirectory: Boolean = !file.isFile()
    val isFile: Boolean = file.isFile()
    val name: String = file.name
    val size: Long = file.length
    val lastModified: Long = file.lastModified
    val type: String = file.extension

    val isImage: Boolean = ImageFileTools.checkIfImage(this)
    val isVideo: Boolean = VideoFileTools.checkIfVideo(this)

    /**
     * Used to save and restore the scroll state for folders in the gallery recycler view.
     */
    var galleryRecyclerState: Parcelable? = null

    private val children: MutableList<GalleryFileContainer> = mutableListOf()
    val subFolders: Int
        get() {
            return children.filter { f -> f.isDirectory }.size
        }
    val subFiles: Int
        get() {
            return children.filter { f -> f.isFile }.size
        }

    fun getChildren() : List<GalleryFileContainer> {
        if(isDirectory && children.isEmpty()) {
            loadChildFiles()
        }
        return children
    }

    fun loadChildFiles() {
        synchronized(this) {
            children.clear()
            children.addAll(
                file.listFiles()
                    .map { docFile -> GalleryFileContainer(docFile) }
                    .filter{galCon -> galCon.isDirectory || galCon.isImage || galCon.isVideo})
        }
    }
}