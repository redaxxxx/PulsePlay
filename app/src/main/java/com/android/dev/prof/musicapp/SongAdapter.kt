package com.android.dev.prof.musicapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.android.dev.prof.musicapp.databinding.SongRowItemBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import java.text.DecimalFormat

class SongAdapter(val context: Context, var songList: List<Song>, val exoPlayer: ExoPlayer,
                  val playerView: View):
    Adapter<SongAdapter.ViewHolder>() {

    class ViewHolder(val binding: SongRowItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(SongRowItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songList[position]

        holder.binding.titleView.text = song.name
        holder.binding.durationView.text = formatDuration(song.duration)
        holder.binding.sizeView.text = formatSize(song.size.toLong())

        val uriArtWork = song.artNetwork
        holder.binding.artWorkView.setImageURI(uriArtWork)

        if (holder.binding.artWorkView.drawable == null){
            holder.binding.artWorkView.setImageResource(R.drawable.art_mc)
        }

        holder.itemView.setOnClickListener{
            if (!exoPlayer.isPlaying){
                exoPlayer.setMediaItems(getMediaItems(), position, 0)
            }else{
                exoPlayer.pause()
                exoPlayer.seekTo(position, 0)
            }

            //prepare and play
            exoPlayer.prepare()
            exoPlayer.play()

            Toast.makeText(context, song.name, Toast.LENGTH_SHORT).show()

            //show player View
            playerView.visibility = View.VISIBLE
        }



        //exoplayer Listener
    }

    private fun getMediaItems(): MutableList<MediaItem> {
        val mediaItems: ArrayList<MediaItem> = ArrayList()
        songList.forEach {
            val mediaItem = MediaItem.Builder()
                .setUri(it.uri)
                .setMediaMetadata(getMetadata(it))
                .build()

            mediaItems.add(mediaItem)
        }

        return mediaItems
    }

    private fun getMetadata(song: Song): MediaMetadata{
        return MediaMetadata.Builder()
            .setTitle(song.name)
            .setArtworkUri(song.artNetwork)
            .build()
    }

    private fun formatDuration(duration: Int): String{
        val durationText: String

        val hr = duration / (1000*60*60)
        val min = (duration % (1000*60*60)) / (1000 * 60)
        val sec = (((duration % (1000*60*60)) % (1000*60*60)) % (1000*60)) / 1000

        if (hr < 1){
            durationText = String.format("%02d:%02d", min, sec)
        }else {
            durationText = String.format("%1d:%02d:%02d", hr, min, sec)
        }

        return durationText
    }

    private fun formatSize(size: Long): String{
        val sizeText : String
        val kpb = size / 1024.0
        val mpb = (size / 1024.0) / 1024.0
        val gpb = ((size / 1024.0) / 1024.0) / 1024.0

        val decimalFromat = DecimalFormat("0.00")

        if (gpb > 1){
            sizeText = decimalFromat.format(gpb) + "GB"
        } else if (mpb > 1){
            sizeText = decimalFromat.format(mpb) + "MB"
        }else if (kpb > 1){
            sizeText = decimalFromat.format(kpb) + "KB"
        }else{
            sizeText = decimalFromat.format(gpb) + "Bytes"
        }
        return sizeText
    }

    fun filterSongs(filterList: List<Song>){
        songList = filterList
        notifyDataSetChanged()
    }

    class OnclickListener(val clickListener: (song: Song) -> Unit){
        fun onClick(song: Song) = clickListener(song)
    }
}