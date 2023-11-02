package com.audiorecorder.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.audiorecorder.AudioFile
import com.audiorecorder.R
import com.audiorecorder.adapter.AudioListAdapter
import java.text.SimpleDateFormat
import java.util.Date

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("NotifyDataSetChanged", "Range")
class AudioListActivity : AppCompatActivity() {

    private lateinit var audioListAdapter: AudioListAdapter
    private val audioFiles: ArrayList<AudioFile> = arrayListOf()
    private var permissions =
        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    private var permissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_list)
        window.statusBarColor = ContextCompat.getColor(this, R.color.action)

        permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            permissions[0]
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted)
            ActivityCompat.requestPermissions(this, permissions, 200)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        audioListAdapter = AudioListAdapter(audioFiles)
        val titleBar = findViewById<LinearLayout>(R.id.titleBar)
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            audioListAdapter.removeSelectedItems()
            titleBar.visibility = View.GONE
        }

        audioListAdapter.setTitleBarAndDeleteButton(titleBar, deleteButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = audioListAdapter
        loadAudioFiles()
    }
    @SuppressLint("SimpleDateFormat")
    private fun loadAudioFiles() {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val audioCollection = MediaStore.Audio.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )
        val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%AudioRecorder%")

        val cursor = contentResolver.query(
            audioCollection,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val fileName =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                val filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED))
                val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(dateAdded * 1000))
                audioFiles.add(AudioFile(fileName, filePath,dateTime))
            }
            cursor.close()
        }
        audioListAdapter.notifyDataSetChanged()
    }
}
