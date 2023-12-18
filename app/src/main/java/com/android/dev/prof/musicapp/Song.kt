package com.android.dev.prof.musicapp

import android.net.Uri

data class Song(
    val name: String,
    val uri: Uri,
    val artNetwork: Uri,
    val size: Int,
    val duration: Int
)