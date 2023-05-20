package com.schlewinow.happygallery.views

import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.Guideline
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.model.GalleryNavigationData
import com.schlewinow.happygallery.model.VideoData
import com.schlewinow.happygallery.tools.folders.VideoFileTools
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.IOException
import kotlin.math.roundToLong

/**
 * Activity showing a video.
 * Uses VLC to play the video.
 * May be targeted externally if the app is used to open a video file.
 */
class VideoViewerVlcActivity : AppCompatActivity() {
    private var libVLC: LibVLC? = null
    private var videoPlayer: MediaPlayer? = null
    private var videoUri: Uri? = null

    private var videoProgressBar: SeekBar? = null
    private var videoProgressTimeText: TextView? = null
    private var videoTotalTimeText: TextView? = null

    private var frameRate: Long = 24
    private var isPrepared: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_viewer_vlc)
        setSupportActionBar(findViewById(R.id.videoViewerToolbar))

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

        // Load video data from intent.
        if(intent != null && intent.data != null) {
            videoUri = intent.data
            setupUI(videoUri!!)
        }
        else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        setupVideo(videoUri!!)
        resetVideoUI()
    }

    override fun onPause() {
        super.onPause()

        storeVideoProgress()
        shutdownVideo()
    }

    override fun onDestroy() {
        super.onDestroy()

        videoPlayer?.release()
        libVLC?.release()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupUI(videoUri: Uri) {
        val currentDirFiles = GalleryNavigationData.currentDirectoryFiles
        val currentGalleryImage = currentDirFiles.find { file -> file.file.uri == videoUri }
        frameRate = VideoFileTools.getVideoFramerate(this, videoUri, frameRate.toInt()).toLong()

        supportActionBar?.title = currentGalleryImage?.name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val videoView: VLCVideoLayout = findViewById(R.id.videoViewerVlcLayout)
        val controlsLayout: View = findViewById(R.id.videoViewerControlsLayout)
        val toolbar: Toolbar = findViewById(R.id.videoViewerToolbar)

        videoView.setOnClickListener {
            // Allow to hide and show video controls.
            if(controlsLayout.visibility == View.VISIBLE) {
                controlsLayout.visibility = View.GONE
                toolbar.visibility = View.GONE
            } else {
                controlsLayout.visibility = View.VISIBLE
                toolbar.visibility = View.VISIBLE
            }
        }

        val playButton: ImageButton = findViewById(R.id.videoViewerPlayButton)
        val pauseButton: ImageButton = findViewById(R.id.videoViewerPauseButton)
        pauseButton.visibility = View.GONE

        playButton.setOnClickListener {
            if (videoPlayer != null && !videoPlayer!!.isPlaying) {
                videoPlayer?.play()
                playButton.visibility = View.GONE
                pauseButton.visibility = View.VISIBLE
            }
        }

        pauseButton.setOnClickListener {
            if (videoPlayer != null && videoPlayer!!.isPlaying) {
                videoPlayer?.pause()
                playButton.visibility = View.VISIBLE
                pauseButton.visibility = View.GONE
            }
        }

        val rewindSingleButton: ImageButton = findViewById(R.id.videoViewerRewindSingleButton)
        rewindSingleButton.setOnClickListener { rewindVideo(1000 / frameRate) }

        val rewindButton: ImageButton = findViewById(R.id.videoViewerRewindButton)
        rewindButton.setOnClickListener { rewindVideo(1000) }

        val forwardButton: ImageButton = findViewById(R.id.videoViewerForwardButton)
        forwardButton.setOnClickListener { forwardVideo(1000) }

        val forwardSingleButton: ImageButton = findViewById(R.id.videoViewerForwardSingleButton)
        forwardSingleButton.setOnClickListener { forwardVideo(1000 / frameRate) }

        videoProgressBar = findViewById(R.id.videoViewerProgressBar)
        videoProgressBar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                val videoBarProgress : Long = videoProgressBar?.progress?.toLong()?: 0
                setVideoPlayerProgress(videoBarProgress)
            }
        })

        setupGuidelines(GalleryNavigationData.statusBarHeight, GalleryNavigationData.navigationBarHeight)
    }

    private fun resetVideoUI() {
        // Show controls.
        val controlsLayout: View = findViewById(R.id.videoViewerControlsLayout)
        controlsLayout.visibility = View.VISIBLE
        val toolbar: Toolbar = findViewById(R.id.videoViewerToolbar)
        toolbar.visibility = View.VISIBLE

        // Reset Play/pause buttons.
        val playButton: ImageButton = findViewById(R.id.videoViewerPlayButton)
        playButton.visibility = View.VISIBLE
        val pauseButton: ImageButton = findViewById(R.id.videoViewerPauseButton)
        pauseButton.visibility = View.GONE
    }

    private fun setupVideo(videoUri: Uri) {
        val videoView: VLCVideoLayout = findViewById(R.id.videoViewerVlcLayout)

        videoProgressTimeText = findViewById(R.id.videoViewerCurrentTimeText)
        videoTotalTimeText = findViewById(R.id.videoViewerTotalTimeText)

        val args: ArrayList<String> = ArrayList()
        args.add("-vvv")
        libVLC = LibVLC(this, args)
        videoPlayer = MediaPlayer(libVLC)
        videoPlayer?.attachViews(videoView, null, false, false)

        try {
            val fd = contentResolver.openFileDescriptor(videoUri, "r")
            val media = Media(libVLC, fd!!.fileDescriptor)
            videoPlayer?.media = media
            media.release()
        }
        catch (iox: IOException) {
            iox.printStackTrace()
            finish()
        }

        isPrepared = false

        videoPlayer?.setEventListener { event ->
            if (event?.type == MediaPlayer.Event.Playing && !isPrepared) {
                onPrepared(videoPlayer!!, videoView)
                videoPlayer?.position = restoreVideoProgress()

                videoProgressBar?.max = videoPlayer?.length?.toInt() ?: 1
                videoProgressTimeText?.text = makeTimeString(videoPlayer?.position?.toLong() ?: 0)
                videoTotalTimeText?.text = makeTimeString(videoPlayer?.length ?: 0)
            }
            else if(event?.type == MediaPlayer.Event.PositionChanged) {
                if(!isPrepared) {
                    // To show the first frame of the video and allow proper preview after jumping in the video,
                    // a frame or two must be played.
                    videoPlayer?.pause()
                    isPrepared = true
                }

                updateVideoUIProgress(percentToMillis(videoPlayer?.position))
            }
            else if (event?.type == MediaPlayer.Event.EndReached) {
                shutdownVideo()
                setVideoPlayerProgress(0)
                storeVideoProgress()

                setupVideo(videoUri)
                resetVideoUI()
            }
        }

        // Render first frame of video.
        videoPlayer?.play()
    }

    private fun shutdownVideo() {
        videoPlayer?.stop()
        videoPlayer?.detachViews()
    }

    /**
     * Set the progress of the video in milliseconds from the start.
     * Will take some time to buffer afterwards.
     * @param progress New video position in milliseconds (from the start of the video).
     */
    private fun setVideoPlayerProgress(progress: Long) {
        videoPlayer?.position = millisToPercent(progress)
        updateVideoUIProgress(progress)
    }

    /**
     * Update the video UI with current progress of the video.
     * Will also update the stored video progress state.
     * @param progress New video position in milliseconds (from the start of the video).
     */
    private fun updateVideoUIProgress(progress: Long) {
        videoProgressTimeText?.text = makeTimeString(progress)
        if(videoProgressBar?.progress != progress.toInt()) {
            videoProgressBar?.progress = progress.toInt()
        }
    }

    /**
     * Store the current video progress.
     * Notice that the progress returned by VLC is inaccurate.
     */
    private fun storeVideoProgress() {
        VideoData.currentVideoPercent = videoPlayer?.position?: 0f
    }

    /**
     * Get the stored video progress.
     * Notice that the progress returned by VLC is inaccurate and slight frame jumps are possible.
     */
    private fun restoreVideoProgress(): Float {
        return VideoData.currentVideoPercent
    }

    private fun rewindVideo(milliseconds: Long) {
        val currentPosition = percentToMillis(videoPlayer?.position)
        val targetPosition: Long

        if(currentPosition > milliseconds) {
            targetPosition = currentPosition - milliseconds
        } else {
            targetPosition = 0
        }

        setVideoPlayerProgress(targetPosition)
    }

    private fun forwardVideo(milliseconds: Long) {
        val currentPosition = percentToMillis(videoPlayer?.position)
        val duration = videoPlayer?.length ?: milliseconds
        val targetPosition: Long

        if(currentPosition < duration - milliseconds) {
            targetPosition = currentPosition + milliseconds
        } else {
            targetPosition = duration
        }

        setVideoPlayerProgress(targetPosition)
    }

    /**
     * Adjust the size of the video so it fits on the screen.
     */
    private fun onPrepared(mediaPlayer: MediaPlayer, videoLayout: VLCVideoLayout) {
        val videoWidth: Int = mediaPlayer.currentVideoTrack.width
        val videoHeight: Int = mediaPlayer.currentVideoTrack.height
        val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()
        val layoutParams: ViewGroup.LayoutParams = videoLayout.layoutParams

        if (videoProportion > screenProportion) {
            layoutParams.width = screenWidth
            layoutParams.height = (screenWidth.toFloat() / videoProportion).toInt()
        } else {
            layoutParams.width = (videoProportion * screenHeight.toFloat()).toInt()
            layoutParams.height = screenHeight
        }

        videoLayout.layoutParams = layoutParams
        videoLayout.isClickable = true
        mediaPlayer.vlcVout.setWindowSize(layoutParams.width, layoutParams.height)
    }

    private fun percentToMillis(percent: Float?): Long {
        return ((videoPlayer?.length?: 0) * (percent?: 0f)).roundToLong()
    }

    private fun millisToPercent(milliSeconds: Long?): Float {
        return (milliSeconds?.toFloat()?: 0f) / (videoPlayer?.length?.toFloat()?: 1f)
    }

    private fun makeTimeString(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 60000) % 60
        val hours = milliseconds / 3600000

        var timeString = "$hours:"
        if(minutes < 10) {
            timeString += "0"
        }
        timeString += "$minutes:"
        if(seconds < 10) {
            timeString += "0"
        }
        timeString += seconds.toString()

        return timeString
    }

    private fun setupGuidelines(statusBarHeight: Int, navigationBarHeight: Int) {
        val topGuideline: Guideline = findViewById(R.id.videoViewerTopGuideline)
        topGuideline.setGuidelineBegin(statusBarHeight)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val bottomGuideline: Guideline = findViewById(R.id.videoViewerBottomGuideline)
            bottomGuideline.setGuidelineEnd(navigationBarHeight)
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val leftGuideline: Guideline = findViewById(R.id.videoViewerLeftGuideline)
            val rightGuideline: Guideline = findViewById(R.id.videoViewerRightGuideline)

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
}