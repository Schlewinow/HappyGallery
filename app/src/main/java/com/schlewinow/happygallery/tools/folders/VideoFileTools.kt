package com.schlewinow.happygallery.tools.folders

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Size
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.schlewinow.happygallery.model.GalleryFileContainer
import com.schlewinow.happygallery.settings.GallerySettings
import java.io.IOException

object VideoFileTools {
    /**
     * Check if a file is an image file.
     * (Or rather, a currently supported image file)
     */
    fun checkIfVideo(galleryFile: GalleryFileContainer): Boolean {
        val stringFileType = galleryFile.type
        if (stringFileType == "mp4"
            || stringFileType == "wmv") {
            return true
        }
        return false
    }

    /**
     * Generate a thumbnail of an image file an apply it to the target view.
     * Size of the thumbnail is automatically chosen depending on display resolution, orientation and layout settings.
     */
    fun loadThumbnail(context: Context, galleryFile: GalleryFileContainer, targetView: ImageView, isPortraitOrientation: Boolean) {
        if (!galleryFile.isVideo) {
            return
        }
        val videoSize: Size = getVideoResolution(context, galleryFile.uri)
        val aspectRatio: Float = videoSize.width.toFloat() / videoSize.height.toFloat()

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

    private fun getVideoResolution(context: Context, videoUri: Uri) : Size {
        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(context, videoUri)
        val videoHeight = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
        val videoWidth = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0

        return Size(videoWidth, videoHeight)
    }

    fun getVideoFramerate(context: Context, videoUri: Uri, defaultFrameRate: Int): Int {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, videoUri, null)
            val numTracks: Int = extractor.getTrackCount()
            for (index: Int in 0..numTracks) {
                val format: MediaFormat = extractor.getTrackFormat(index)
                val mime: String = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("video/")) {
                    if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        return format.getInteger(MediaFormat.KEY_FRAME_RATE)
                    }
                }
            }
        } catch (iox: IOException) {
            iox.printStackTrace()
        }finally {
            extractor.release();
        }

        return defaultFrameRate
    }
}