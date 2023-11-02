package com.audiorecorder.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.audiorecorder.AudioFile
import com.audiorecorder.R
import com.audiorecorder.activity.AudioPlayerActivity

class AudioListAdapter(private val audioFiles: ArrayList<AudioFile>) :
    RecyclerView.Adapter<AudioListAdapter.ViewHolder>() {
    private val selectedItems = arrayListOf<AudioFile>()
    private var titleBar: LinearLayout? = null
    private var deleteButton: Button? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileNameTextView: TextView = itemView.findViewById(R.id.tv)
        private val dateTimeTextView: TextView = itemView.findViewById(R.id.date)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)

        fun bind(audioFile: AudioFile) {
            fileNameTextView.text = audioFile.fileName
            dateTimeTextView.text = audioFile.dateTime
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audio, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val audioFile = audioFiles[position]
        holder.bind(audioFile)
            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, AudioPlayerActivity::class.java)
                intent.putExtra("audioFiles", audioFiles)
                intent.putExtra("position", position)
                intent.putExtra("audioFilePath", audioFile.filePath)
                intent.putExtra("audioName", audioFile.fileName)
                context.startActivity(intent)
            }

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(audioFile)
            } else {
                selectedItems.remove(audioFile)
            }
            holder.checkbox.isChecked = isChecked
            updateTitleBarVisibility()
        }
    }
    fun setTitleBarAndDeleteButton(titleBar: LinearLayout, deleteButton: Button) {
        this.titleBar = titleBar
        this.deleteButton = deleteButton
    }

    private fun updateTitleBarVisibility() {
        val showTitleBar = selectedItems.isNotEmpty()

        titleBar!!.visibility = if (showTitleBar) View.VISIBLE else View.GONE
        deleteButton!!.visibility = if (showTitleBar) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return audioFiles.size
    }
    fun removeSelectedItems() {
        for (audioFile in selectedItems) {
            val position = audioFiles.indexOf(audioFile)
            if (position != -1) {
                audioFiles.removeAt(position)
                notifyItemRemoved(position)
            }
        }
        selectedItems.clear()
    }
}
