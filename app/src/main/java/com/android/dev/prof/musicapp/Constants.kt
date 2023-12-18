package com.android.dev.prof.musicapp

import android.Manifest
import android.os.Build

object Constants {
    const val TAG = "MusicApp"
    val REQUIRED_PERMISSIONS =
        mutableListOf (
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO

        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()}