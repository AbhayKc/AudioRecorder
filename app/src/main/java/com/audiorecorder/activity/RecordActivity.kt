package com.audiorecorder.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.audiorecorder.R
import com.audiorecorder.Timer
import com.audiorecorder.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class RecordActivity : AppCompatActivity(), Timer.OnTimeTickListener {
    private var newFileName: String? = null
    private var currentAudioFileName: String? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var recorder: MediaRecorder
    private lateinit var timer: Timer
    private var permissions =
        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var permissionGranted = false
    private var isRecording = false
    private var isPaused = false
    private val audioDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            .toString() + File.separator + "AudioRecorder/"

    @SuppressLint("DiscouragedApi")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recorder = MediaRecorder()
        window.statusBarColor = ContextCompat.getColor(this, R.color.action)

        permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED
        permissionGranted = permissionGranted && ActivityCompat.checkSelfPermission(
            this,
            permissions[1]
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted)
            ActivityCompat.requestPermissions(this, permissions, 200)

        timer = Timer(this)

        binding.recordBtn.setOnClickListener {
            when {
                isRecording -> pauseRecorder()
                isPaused -> resumeRecorder()
                else -> {
                    startRecording()
                }
            }
        }
        binding.listBtn.setOnClickListener {
            val intent = Intent(this@RecordActivity, AudioListActivity::class.java)
            startActivity(intent)
        }
        binding.doneBtn.setOnClickListener {
            showSaveAudioDialog()
            stopRecorder()
        }
        binding.deleteBtn.setOnClickListener {
            stopRecorder()
        }
        binding.deleteBtn.isClickable = false
    }

    private fun pauseRecorder() {
        recorder.pause()
        isRecording = false
        isPaused = true
        binding.recordBtn.setImageResource(R.drawable.svg_start)
        timer.pause()
    }

    private fun resumeRecorder() {
        recorder.resume()
        isPaused = false
        isRecording = true
        binding.recordBtn.setImageResource(R.drawable.svg_pause)
        timer.start()
    }

    @SuppressLint("SimpleDateFormat")
    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val audioFilePath = generateNewAudioFilePath()
        recorder = MediaRecorder()
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)
            try {
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            start()
        }
        currentAudioFileName = audioFilePath
        binding.recordBtn.setImageResource(R.drawable.svg_pause)
        isRecording = true
        isPaused = false
        timer.start()

        binding.deleteBtn.isClickable = true
        binding.deleteBtn.setImageResource(R.drawable.svg_clear)

        binding.listBtn.visibility = View.GONE
        binding.doneBtn.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun stopRecorder() {
        timer.stop()
        if (isRecording) {
            recorder.stop()
        }
    }
    @SuppressLint("SimpleDateFormat")
    private fun generateNewAudioFilePath(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return "$audioDir$timeStamp.mp3"
    }
    private fun showSaveAudioDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_save_name, null)
        val fileNameEditText = dialogView.findViewById<EditText>(R.id.fileNameEditText)

        builder.setView(dialogView)
        builder.setTitle("Set Audio Name")
        builder.setMessage("Enter the name for the audio file")

        builder.setPositiveButton("Save") { _, _ ->
            newFileName = fileNameEditText.text.toString().trim()
            saveAudio()
            resetUI()
        }
        builder.setNegativeButton("Cancel") { _, _ ->
            deleteAudio(currentAudioFileName)
            resetUI()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun saveAudio() {
        if (newFileName != null) {
            val destinationFile = File("$audioDir$newFileName.mp3")
            currentAudioFileName?.let { File(it) }?.renameTo(destinationFile)
        }
    }
    private fun resetUI() {
        if (isRecording) {
            recorder.release()
        }
        isPaused = false
        isRecording = false

        binding.listBtn.visibility = View.VISIBLE
        binding.doneBtn.visibility = View.GONE

        binding.deleteBtn.isClickable = false
        binding.deleteBtn.setImageResource(R.drawable.svg_clear_dis)
        binding.recordBtn.setImageResource(R.drawable.ic_record)

        binding.textView.text = "00:00:00"
    }
    override fun onTimeTick(duration: String) {
        binding.textView.text = duration
    }

    private fun deleteAudio(fileName: String?) {
        fileName?.let {
            val audioFilePath = "$audioDir$fileName"
            val file = File(audioFilePath)
            if (file.exists()) {
                file.delete()
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }
}


