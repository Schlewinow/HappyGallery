package com.schlewinow.happygallery.tools.folders

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Size
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.schlewinow.happygallery.model.GalleryFileContainer
import com.schlewinow.happygallery.settings.GallerySettings

/**
 * Toolset for any operation including images.
 */
object ImageFileTools {
    /**
     * Check if a file is an image file.
     * (Or rather, a currently supported image file)
     */
    fun checkIfImage(galleryFile: GalleryFileContainer): Boolean {
        val stringFileType = galleryFile.type
        if (stringFileType == "jpg" || stringFileType == "jpeg"
            || stringFileType == "png"
            || stringFileType == "webp") {
            return true
        }
        return false
    }

    /**
     * Generate a thumbnail of an image file an apply it to the target view.
     * Size of the thumbnail is automatically chosen depending on display resolution, orientation and layout settings.
     */
    fun loadThumbnail(context: Context, galleryFile: GalleryFileContainer, targetView: ImageView, isPortraitOrientation: Boolean) {
        val contentResolver = context.contentResolver

        if (!galleryFile.isImage) {
            return
        }

        val imageSize: Size = getImageResolution(galleryFile.uri, contentResolver)
        val aspectRatio: Float = imageSize.width.toFloat() / imageSize.height.toFloat()

        var thumbnailSize = context.resources.displayMetrics.widthPixels / GallerySettings.fileColumnsPortrait
        if(!isPortraitOrientation) {
            thumbnailSize = context.resources.displayMetrics.widthPixels / GallerySettings.fileColumnsLandscape
        }

        var thumbnailWidth = thumbnailSize
        var thumbnailHeight = thumbnailSize
        if (aspectRatio > 1) {
            thumbnailHeight = (thumbnailHeight * aspectRatio).toInt()
        } else {
            thumbnailWidth = (thumbnailWidth / aspectRatio).toInt()
        }

        Glide.with(context)
            .load(galleryFile.uri)
            .override(thumbnailWidth, thumbnailHeight)
            .into(targetView)
    }

    private fun getImageResolution(imageUri: Uri, contentResolver: ContentResolver) : Size {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(
            contentResolver.openInputStream(imageUri),
            null,
            options
        )
        val imageHeight: Int = options.outHeight
        val imageWidth: Int = options.outWidth

        return Size(imageWidth, imageHeight)
    }
}