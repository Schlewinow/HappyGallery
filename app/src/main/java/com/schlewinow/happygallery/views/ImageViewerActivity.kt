package com.schlewinow.happygallery.views

import android.app.WallpaperManager
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.Guideline
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.model.GalleryNavigationData
import com.schlewinow.happygallery.model.item.GalleryBaseContainer
import com.schlewinow.happygallery.model.item.GalleryFileContainer
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Activity showing a single image.
 * May be targeted externally if the app is used to open a single image file.
 */
class ImageViewerActivity : AppCompatActivity() {
    private var currentGalleryImage: GalleryBaseContainer? = null

    private var fallbackRotation: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)
        setSupportActionBar(findViewById(R.id.imageViewerToolbar))

        // Transparent system bars at top and bottom.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        window.statusBarColor = Color.TRANSPARENT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

        // Load image data from intent.
        if (intent != null && intent.data != null) {
            setup(intent.data!!)
        }
        else {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_image_viewer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_image_wallpaper -> {
                setImageAsWallpaper()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Open an intent to set the currently shown image as wallpaper in the OS.
     */
    private fun setImageAsWallpaper() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        if (wallpaperManager.isSetWallpaperAllowed) {
            val wallpaperIntent = wallpaperManager.getCropAndSetWallpaperIntent(currentGalleryImage?.uri)
            startActivity(wallpaperIntent)
        }
    }

    private fun setup(imageUri: Uri) {
        val currentDirFiles = GalleryNavigationData.currentDirectoryFiles
        currentGalleryImage = currentDirFiles.find { file -> file.contentFile.uri == imageUri }

        supportActionBar?.title = currentGalleryImage?.name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // The special scaling image view does not support GIF, so use a regular image view as fallback.
        val useFallback: Boolean = imageUri.toString().endsWith("gif", true)
        if (useFallback) {
            setupFallbackImageMode(imageUri)
        }
        else {
            setupScaleImageMode(imageUri)
        }

        val previousButton: ImageButton = findViewById(R.id.imageViewerPreviousButton)
        val previousImage: GalleryBaseContainer? = findPreviousImage()
        if (previousImage != null) {
            previousButton.setOnClickListener {
                navigateToActivity(ImageViewerActivity::class.java, previousImage.uri)
                finish()
            }
        } else {
            previousButton.visibility = View.INVISIBLE
        }

        val nextButton: ImageButton = findViewById(R.id.imageViewerNextButton)
        val nextImage: GalleryBaseContainer? = findNextImage()
        if (nextImage != null) {
            nextButton.setOnClickListener {
                navigateToActivity(ImageViewerActivity::class.java, nextImage.uri)
                finish()
            }
        } else {
            nextButton.visibility = View.INVISIBLE
        }

        setupGuidelines(GalleryNavigationData.statusBarHeight, GalleryNavigationData.navigationBarHeight)
    }

    private fun setupScaleImageMode(imageUri: Uri) {
        val imageView: SubsamplingScaleImageView = findViewById(R.id.imageViewerImageTouchView)
        imageView.setMinimumDpi(40)
        imageView.orientation = SubsamplingScaleImageView.ORIENTATION_0
        imageView.setImage(ImageSource.uri(imageUri))

        val rotateLeftButton: ImageButton = findViewById(R.id.imageViewerRotateLeftButton)
        rotateLeftButton.setOnClickListener {
            var targetOrientation: Int = SubsamplingScaleImageView.ORIENTATION_0
            when (imageView.orientation) {
                SubsamplingScaleImageView.ORIENTATION_0 -> targetOrientation = SubsamplingScaleImageView.ORIENTATION_270
                SubsamplingScaleImageView.ORIENTATION_270 -> targetOrientation = SubsamplingScaleImageView.ORIENTATION_180
                SubsamplingScaleImageView.ORIENTATION_180 -> targetOrientation = SubsamplingScaleImageView.ORIENTATION_90
            }

            imageView.orientation = targetOrientation
            imageView.setImage(ImageSource.uri(imageUri))
        }

        val rotateRightButton: ImageButton = findViewById(R.id.imageViewerRotateRightButton)
        rotateRightButton.setOnClickListener {
            var targetOrientation: Int = SubsamplingScaleImageView.ORIENTATION_0
            when (imageView.orientation) {
                SubsamplingScaleImageView.ORIENTATION_0 -> targetOrientation = SubsamplingScaleImageView.ORIENTATION_90
                SubsamplingScaleImageView.ORIENTATION_90 -> targetOrientation = SubsamplingScaleImageView.ORIENTATION_180
                SubsamplingScaleImageView.ORIENTATION_180 -> targetOrientation = SubsamplingScaleImageView.ORIENTATION_270
            }

            imageView.orientation = targetOrientation
            imageView.setImage(ImageSource.uri(imageUri))
        }
    }

    private fun setupFallbackImageMode(imageUri: Uri) {
        val imageView: SubsamplingScaleImageView = findViewById(R.id.imageViewerImageTouchView)
        imageView.visibility = View.GONE

        val imageFallbackView: ImageView = findViewById(R.id.imageViewerFallbackImageView)
        imageFallbackView.visibility = View.VISIBLE
        Glide.with(this)
            .load(imageUri)
            .into(imageFallbackView)

        val rotateLeftButton: ImageButton = findViewById(R.id.imageViewerRotateLeftButton)
        rotateLeftButton.setOnClickListener {
            rotateImageViewContentBy90(imageFallbackView, -1f)
        }

        val rotateRightButton: ImageButton = findViewById(R.id.imageViewerRotateRightButton)
        rotateRightButton.setOnClickListener {
            rotateImageViewContentBy90(imageFallbackView, 1f)
        }
    }

    /**
     * Image view content must be rotated manually via transformation matrix operations.
     * This function allows to rotate the ImageView content in 90 degree steps.
     */
    private fun rotateImageViewContentBy90(imageView: ImageView, direction: Float) {
        imageView.scaleType = ImageView.ScaleType.MATRIX
        val transformMatrix = Matrix()

        // Apply rotation
        val imageWidth: Int = imageView.drawable.bounds.width()
        val imageHeight: Int = imageView.drawable.bounds.height()
        fallbackRotation += 90f * direction.sign

        transformMatrix.postRotate(fallbackRotation, (imageWidth / 2).toFloat(), (imageHeight / 2).toFloat())

        // Scale to fit screen size.
        val screenWidth: Int = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight: Int = Resources.getSystem().displayMetrics.heightPixels
        val rotatedImageWidth =  if (fallbackRotation.toInt() % 180 == 0) imageWidth else imageHeight
        val rotatedImageHeight = if (fallbackRotation.toInt() % 180 == 0) imageHeight else imageWidth
        val xOffset = screenWidth - rotatedImageWidth
        val yOffset = screenHeight - rotatedImageHeight
        var scaleFactor = 1f

        if (xOffset.absoluteValue < yOffset.absoluteValue) {
            scaleFactor = screenWidth.toFloat() / rotatedImageWidth.toFloat()
        }
        else {
            scaleFactor = screenHeight.toFloat() / rotatedImageHeight.toFloat()
        }
        transformMatrix.postScale(scaleFactor, scaleFactor)

        // Center image on screen.
        if (screenHeight > screenWidth) {
            transformMatrix.postTranslate(-xOffset.toFloat() * scaleFactor / 2f, yOffset.toFloat() / 2f)
        }
        else {
            transformMatrix.postTranslate(xOffset.toFloat() / 2f, -yOffset.toFloat() * scaleFactor / 2f)
        }

        imageView.imageMatrix = transformMatrix
    }

    private fun setupGuidelines(statusBarHeight: Int, navigationBarHeight: Int) {
        val topGuideline: Guideline = findViewById(R.id.imageViewerTopGuideline)
        topGuideline.setGuidelineBegin(statusBarHeight)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val bottomGuideline: Guideline = findViewById(R.id.imageViewerBottomGuideline)
            bottomGuideline.setGuidelineEnd(navigationBarHeight)
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val leftGuideline: Guideline = findViewById(R.id.imageViewerLeftGuideline)
            val rightGuideline: Guideline = findViewById(R.id.imageViewerRightGuideline)

            val rotation: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                rotation = display?.rotation ?: Surface.ROTATION_90
            } else {
                rotation = getWindowManager().getDefaultDisplay().getRotation()
            }

            if (rotation == Surface.ROTATION_90) {
                leftGuideline.setGuidelineBegin(0)
                rightGuideline.setGuidelineEnd(navigationBarHeight)
            } else if (rotation == Surface.ROTATION_270) {
                leftGuideline.setGuidelineBegin(navigationBarHeight)
                rightGuideline.setGuidelineEnd(0)
            }
        }
    }

    private fun findNextImage() : GalleryBaseContainer? {
        val currentDirFiles = GalleryNavigationData.currentDirectoryFiles
        val currentIndex = currentDirFiles.indexOf(currentGalleryImage)

        // Find the next image in the current folder.
        var nextIndex = currentIndex
        while (nextIndex < currentDirFiles.size - 1) {
            ++nextIndex
            if (currentDirFiles[nextIndex].isGalleryFile) {
                if ((currentDirFiles[nextIndex] as GalleryFileContainer).isImage) {
                    return currentDirFiles[nextIndex]
                }
            }
        }

        return null
    }

    private fun findPreviousImage() : GalleryBaseContainer? {
        val currentDirFiles = GalleryNavigationData.currentDirectoryFiles
        val currentIndex = currentDirFiles.indexOf(currentGalleryImage)

        // Find the previous image in the current folder.
        var previousIndex = currentIndex
        while (previousIndex > 0) {
            --previousIndex
            if (currentDirFiles[previousIndex].isGalleryFile) {
                if ((currentDirFiles[previousIndex] as GalleryFileContainer).isImage) {
                    return currentDirFiles[previousIndex]
                }
            }
        }

        return null
    }

    private fun navigateToActivity(destination: Class<*>?, data: Uri? = null) {
        val navigationIntent = Intent(this, destination)
        if (data != null) {
            navigationIntent.data = data
        }
        startActivity(navigationIntent)
    }
}