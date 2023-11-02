package com.audiorecorder

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioFile(
    val fileName: String,
    val filePath: String,
    val dateTime: String,
    var isSelected: Boolean = false
) : Parcelable