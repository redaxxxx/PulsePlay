package com.android.dev.prof.musicapp

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

class SongAudio {
    fun listOfSongs(context: Context): ArrayList<Song>{
        val listOfAllSongs: ArrayList<Song> = ArrayList()
        var id: Long
        var name: String
        var duration: Int
        var size: Int
        var albumId: Long
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val orderBy = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        val uri: Uri

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }else{
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            orderBy
        )

        val idColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val nameColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val sizeColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
        val albumColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

        while (cursor!!.moveToNext()){
            id = cursor.getLong(idColumn!!)
            name = cursor.getString(nameColumn!!)
            duration = cursor.getInt(durationColumn!!)
            size = cursor.getInt(sizeColumn!!)
            albumId = cursor.getLong(albumColumn!!)

            // song uri
            val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                id
            )

            // album uri
            val albumUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),
                albumId
            )

            // remove .mp3 extension from the song's name
            name = name.substring(0, name.lastIndexOf("."))

            // song item
            val song = Song(name, contentUri, albumUri, size, duration)
            listOfAllSongs.add(song)

        }

        return listOfAllSongs
    }
}