package com.audiorecorder.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import com.audiorecorder.AudioFile
import com.audiorecorder.R
import com.audiorecorder.databinding.ActivityAudioPlayerBinding

class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAudioPlayerBinding
    private var mediaPlayer: MediaPlayer? = null
    private var filePath: String? = null
    private var seekBar: SeekBar? = null
    private var fileName: TextView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var playButton: AppCompatImageButton? = null
    private var nextButton: AppCompatImageButton? = null
    private var previousButton: AppCompatImageButton? = null
    private var isRecording = false
    private var isPaused = false
    private var audioFiles: ArrayList<AudioFile>? = null
    private var currentPosition: Int = 0
    private var startTimeTextView: TextView? = null
    private var endTimeTextView: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.action)
        startTimeTextView = findViewById(R.id.startTimeTextView)
        endTimeTextView = findViewById(R.id.endTimeTextView)


        audioFiles = intent.getParcelableArrayListExtra("audioFiles")
        currentPosition = intent.getIntExtra("position", 0)
        filePath = intent.getStringExtra("audioFilePath")
        val audioFileName = intent.getStringExtra("audioName")
        setupMediaPlayerForCurrentAudio()

        seekBar = findViewById(R.id.audioSeekBar)
        fileName = findViewById(R.id.audioFileName)

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(filePath)
        mediaPlayer?.prepare()

        fileName?.text = audioFileName
        seekBar?.max = mediaPlayer?.duration ?: 0
        playButton = findViewById(R.id.recordBtn)
        nextButton = findViewById(R.id.nextBtn)
        previousButton = findViewById(R.id.previousBtn)

        playButton?.setOnClickListener {
            if (mediaPlayer != null) {
                if (isRecording) {
                    playAudio()
                } else if (isPaused) {
                    resumeAudio()
                } else {
                    pauseAudio()
                }
            }
        }

        nextButton?.setOnClickListener {
            playNextAudio()
        }

        previousButton?.setOnClickListener {
            playPreviousAudio()
        }
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
                val startTime = formatTime(progress)
                val endTime = formatTime(mediaPlayer?.duration ?: 0)

                startTimeTextView?.text = startTime
                endTimeTextView?.text = endTime
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        mediaPlayer?.start()

        mediaPlayer?.setOnCompletionListener {
        }
        handler.postDelayed(updateSeekBar, 100)
    }
    private fun formatTime(ms: Int): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }


    private fun setupMediaPlayerForCurrentAudio() {
        if (audioFiles != null && audioFiles!!.isNotEmpty()) {
            val currentAudioFile = audioFiles!![currentPosition]
            filePath = currentAudioFile.filePath
        }
    }

    private fun playNextAudio() {
        if (audioFiles != null && currentPosition < audioFiles!!.size - 1) {
            currentPosition++
            setupMediaPlayerForCurrentAudio()
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(filePath)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            fileName?.text = audioFiles!![currentPosition].fileName
            seekBar?.max = mediaPlayer?.duration ?: 0
            seekBar?.progress = 0
        }
    }

    private fun playPreviousAudio() {
        if (audioFiles != null && currentPosition > 0) {
            currentPosition--
            setupMediaPlayerForCurrentAudio()
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(filePath)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            fileName?.text = audioFiles!![currentPosition].fileName
            seekBar?.max = mediaPlayer?.duration ?: 0
            seekBar?.progress = 0
        }
    }

    private val updateSeekBar = object : Runnable {
        override fun run() {
            seekBar?.progress = mediaPlayer?.currentPosition ?: 0
            handler.postDelayed(this, 100)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        handler.removeCallbacks(updateSeekBar)
    }

    private fun playAudio() {
        if (!mediaPlayer?.isPlaying!!) {
            mediaPlayer?.start()
            isRecording = false
            isPaused = false
            binding.recordBtn.setImageResource(R.drawable.svg_pause)
        }
    }

    private fun pauseAudio() {
        if (mediaPlayer?.isPlaying!!) {
            mediaPlayer?.pause()
            isPaused = true
            binding.recordBtn.setImageResource(R.drawable.svg_start)
        }
    }

    private fun resumeAudio() {
        if (!mediaPlayer?.isPlaying!!) {
            mediaPlayer?.start()
            isPaused = false
            binding.recordBtn.setImageResource(R.drawable.svg_pause)
        }
    }
}

