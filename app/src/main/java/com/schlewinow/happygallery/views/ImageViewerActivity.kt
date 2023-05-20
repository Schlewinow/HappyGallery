package com.schlewinow.happygallery.views

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.constraintlayout.widget.Guideline
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.model.GalleryFileContainer
import com.schlewinow.happygallery.model.GalleryNavigationData

/**
 * Activity showing a single image.
 * May be targeted externally if the app is used to open a single image file.
 */
class ImageViewerActivity : AppCompatActivity() {
    private var currentGalleryImage: GalleryFileContainer? = null

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
        if(intent != null && intent.data != null) {
            setup(intent.data!!)
        }
        else {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when {
            item.itemId == android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setup(imageUri: Uri) {
        val currentDirFiles = GalleryNavigationData.currentDirectoryFiles
        currentGalleryImage = currentDirFiles.find { file -> file.file.uri == imageUri }

        supportActionBar?.title = currentGalleryImage?.name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val imageView: SubsamplingScaleImageView = findViewById(R.id.ImageTouchView)
        imageView.setMinimumDpi(40)
        imageView.setImage(ImageSource.uri(imageUri))

        val previousButton: ImageButton = findViewById(R.id.imageViewerPreviousButton)
        val previousImage: GalleryFileContainer? = findPreviousImage()
        if(previousImage != null) {
            previousButton.setOnClickListener {
                navigateToActivity(ImageViewerActivity::class.java, previousImage.uri)
                finish()
            }
        } else {
            previousButton.visibility = View.GONE
        }

        val nextButton: ImageButton = findViewById(R.id.imageViewerNextButton)
        val nextImage: GalleryFileContainer? = findNextImage()
        if(nextImage != null) {
            nextButton.setOnClickListener {
                navigateToActivity(ImageViewerActivity::class.java, nextImage.uri)
                finish()
            }
        } else {
            nextButton.visibility = View.GONE
        }

        setupGuidelines(GalleryNavigationData.statusBarHeight, GalleryNavigationData.navigationBarHeight)
    }

    private fun setupGuidelines(statusBarHeight: Int, navigationBarHeight: Int) {
        val topGuideline: Guideline = findViewById(R.id.imageViewerTopGuideline)
        topGuideline.setGuidelineBegin(statusBarHeight)

        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val bottomGuideline: Guideline = findViewById(R.id.imageViewerBottomGuideline)
            bottomGuideline.setGuidelineEnd(navigationBarHeight)
        } else if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val leftGuideline: Guideline = findViewById(R.id.imageViewerLeftGuideline)
            val rightGuideline: Guideline = findViewById(R.id.imageViewerRightGuideline)

            val rotation: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                rotation = display?.rotation ?: Surface.ROTATION_90
            } else {
                rotation = getWindowManager().getDefaultDisplay().getRotation()
            }

            if(rotation == Surface.ROTATION_90) {
                leftGuideline.setGuidelineBegin(0)
                rightGuideline.setGuidelineEnd(navigationBarHeight)
            } else if (rotation == Surface.ROTATION_270) {
                leftGuideline.setGuidelineBegin(navigationBarHeight)
                rightGuideline.setGuidelineEnd(0)
            }
        }
    }

    private fun findNextImage() : GalleryFileContainer? {
        val currentDirFiles = GalleryNavigationData.currentDirectoryFiles
        val currentIndex = currentDirFiles.indexOf(currentGalleryImage)

        // Find the next image in the current folder.
        var nextIndex = currentIndex
        while(nextIndex < currentDirFiles.size - 1) {
            ++nextIndex
            if(currentDirFiles[nextIndex].isImage) {
                return currentDirFiles[nextIndex]
            }
        }

        return null
    }

    private fun findPreviousImage() : GalleryFileContainer? {
        val currentDirFiles = GalleryNavigationData.currentDirectoryFiles
        val currentIndex = currentDirFiles.indexOf(currentGalleryImage)

        // Find the previous image in the current folder.
        var previousIndex = currentIndex
        while(previousIndex > 0) {
            --previousIndex
            if(currentDirFiles[previousIndex].isImage) {
                return currentDirFiles[previousIndex]
            }
        }

        return null
    }

    private fun navigateToActivity(destination: Class<*>?, data: Uri? = null) {
        val navigationIntent = Intent(this, destination)
        if(data != null) {
            navigationIntent.data = data
        }
        startActivity(navigationIntent)
    }
}