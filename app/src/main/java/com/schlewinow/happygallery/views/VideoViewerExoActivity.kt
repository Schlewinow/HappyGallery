package com.schlewinow.happygallery.views

import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.Guideline
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.model.GalleryNavigationData
import com.schlewinow.happygallery.model.VideoData
import com.schlewinow.happygallery.tools.folders.VideoFileTools
import java.lang.Thread.sleep

class VideoViewerExoActivity : AppCompatActivity() {
    private var videoPlayer: ExoPlayer? = null
    private var videoProgressBar: SeekBar? = null
    private var videoProgressTimeText: TextView? = null
    private var videoTotalTimeText: TextView? = null
    private var progressUpdateThread: HandlerThread? = null
    private var progressUpdateHandler: Handler? = null
    private var progressUpdateRunning: Boolean = true
    private var mainHandler: Handler? = null
    private var frameRate: Long = 24

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_viewer_exo)
        setSupportActionBar(findViewById(R.id.videoViewerToolbar))

        mainHandler = Handler(mainLooper)

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

    override fun onResume() {
        super.onResume()

        // Restore UI.
        resetVideoUI()
        val progressToRestore = VideoData.currentVideoMillis
        val restoreProgressHandler = Handler(mainLooper)
        restoreProgressHandler.postDelayed( {
            videoPlayer?.seekTo(progressToRestore)
        }, 100)

        // Restore thread to update progress bar according to video progress.
        progressUpdateThread= HandlerThread("VideoProgressUpdate")
        progressUpdateThread?.start()
        progressUpdateRunning = true

        progressUpdateHandler = Handler(progressUpdateThread!!.looper)
        progressUpdateHandler?.post {
            while(progressUpdateRunning) {
                mainHandler?.post {
                    videoProgressBar?.progress = videoPlayer?.currentPosition?.toInt() ?: 0
                    videoProgressTimeText?.text = makeTimeString(videoPlayer?.currentPosition ?: 0)
                    VideoData.currentVideoMillis = videoPlayer?.currentPosition ?: 0
                }
                sleep(100)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        videoPlayer?.pause()
        progressUpdateRunning = false
        progressUpdateThread?.quitSafely()
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

    override fun onDestroy() {
        super.onDestroy()
        videoPlayer?.release()
    }

    private fun setup(videoUri: Uri) {
        val currentDirFiles = GalleryNavigationData.currentDirectoryFiles
        val currentGalleryImage = currentDirFiles.find { file -> file.file.uri == videoUri }
        frameRate = VideoFileTools.getVideoFramerate(this, videoUri, frameRate.toInt()).toLong()

        supportActionBar?.title = currentGalleryImage?.name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val videoView: SurfaceView = findViewById(R.id.videoViewerSurface)
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

        videoProgressTimeText = findViewById(R.id.videoViewerCurrentTimeText)
        videoTotalTimeText = findViewById(R.id.videoViewerTotalTimeText)

        videoView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
                videoPlayer = ExoPlayer.Builder(this@VideoViewerExoActivity).build()
                videoPlayer?.setMediaItem(MediaItem.fromUri(videoUri))
                videoPlayer?.prepare()
                videoPlayer?.setVideoSurfaceHolder(surfaceHolder)

                videoPlayer?.addListener(object: Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        // Set video surface size to keep aspect ratio yet fill screen.
                        if(playbackState == Player.STATE_READY) {
                            if(videoPlayer != null) {
                                val mediaPlayer = videoPlayer
                                onPrepared(mediaPlayer!!, videoView)

                                videoProgressBar?.max = videoPlayer?.duration?.toInt() ?: 1
                                videoProgressTimeText?.text = makeTimeString(videoPlayer?.currentPosition ?: 0)
                                videoTotalTimeText?.text = makeTimeString(videoPlayer?.duration ?: 0)
                            }
                        }

                        // Reset UI once video finishes playing
                        if (playbackState == Player.STATE_ENDED) {
                            resetVideoUI()
                        }
                    }
                })

                // Render first frame of video.
                videoPlayer?.play()
                videoPlayer?.pause()

                videoProgressBar = findViewById(R.id.videoViewerProgressBar)
                videoProgressBar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                        videoPlayer?.seekTo(videoProgressBar?.progress?.toLong() ?: 0)
                    }
                })
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
            }
        })

        setupGuidelines(GalleryNavigationData.statusBarHeight, GalleryNavigationData.navigationBarHeight)
    }

    private fun rewindVideo(milliseconds: Long) {
        val currentPosition = videoPlayer?.currentPosition?: 0
        val targetPosition: Long

        if(currentPosition > milliseconds) {
            targetPosition = currentPosition - milliseconds
        } else {
            targetPosition = 0
        }

        videoPlayer?.seekTo(targetPosition)
    }

    private fun forwardVideo(milliseconds: Long) {
        val currentPosition = videoPlayer?.currentPosition?: 0
        val duration = videoPlayer?.duration ?: milliseconds
        val targetPosition: Long

        if(currentPosition < duration - milliseconds) {
            targetPosition = currentPosition + milliseconds
        } else {
            targetPosition = duration
        }

        videoPlayer?.seekTo(targetPosition)
    }

    private fun resetVideoUI() {
        // Render first frame of video.
        videoPlayer?.seekTo(0)
        videoPlayer?.play()
        videoPlayer?.pause()

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

    /**
     * Adjust the size of the video so it fits on the screen.
     */
    fun onPrepared(mediaPlayer: ExoPlayer, surfaceView: SurfaceView) {
        val videoWidth: Int = mediaPlayer.videoSize.width
        val videoHeight: Int = mediaPlayer.videoSize.height
        val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()
        val layoutParams: ViewGroup.LayoutParams = surfaceView.layoutParams

        if (videoProportion > screenProportion) {
            layoutParams.width = screenWidth
            layoutParams.height = (screenWidth.toFloat() / videoProportion).toInt()
        } else {
            layoutParams.width = (videoProportion * screenHeight.toFloat()).toInt()
            layoutParams.height = screenHeight
        }

        surfaceView.layoutParams = layoutParams
        surfaceView.isClickable = true
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
                rotation = windowManager.getDefaultDisplay().getRotation()
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