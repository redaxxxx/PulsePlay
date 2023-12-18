package com.android.dev.prof.musicapp

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.SearchView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.android.dev.prof.musicapp.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class MainActivity : AppCompatActivity() {

    private lateinit var songList: ArrayList<Song>
    private lateinit var songAdapter: SongAdapter
    private lateinit var binding: ActivityMainBinding

    //Exoplayer
    private lateinit var exoplayer: ExoPlayer

    //repeat all = 1, repeat one = 2, shuffle all = 3
    private var repeatMode = 1

    private var defaultStatusColor: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        exoplayer = ExoPlayer.Builder(this).build()

        //save the status bar
        defaultStatusColor = window.statusBarColor

        //set the navigation color
        window.navigationBarColor = ColorUtils.setAlphaComponent(defaultStatusColor, 199)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = resources.getString(R.string.app_name)
        if (allPermissionGranted()){
            loadSongs()
        }else{
            requestPermission()
        }

        playerControls()
    }

    private fun playerControls(){
        // song name marquee
        binding.homeSongName.isSelected = true
        binding.playerView.songNameText.isSelected = true

        binding.playerView.arrowBackBtn.setOnClickListener {
            exitPlayerView()
        }

        binding.homeControlWrapper.setOnClickListener{
            showPlayerView()
        }

        exoplayer.addListener(object : Player.Listener{
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                //show the playing song title
                if (mediaItem != null){
                    binding.homeSongName.text = mediaItem.mediaMetadata.title
                    binding.playerView.songNameText.text = mediaItem.mediaMetadata.title

                    binding.playerView.startProgress.text = getReadableTime(exoplayer.currentPosition.toInt())

                    binding.playerView.seekbar.progress = exoplayer.currentPosition.toInt()
                    binding.playerView.seekbar.max = exoplayer.duration.toInt()

                    binding.playerView.endProgress.text = getReadableTime(exoplayer.duration.toInt())

                    binding.playerView.playPauseBtn.setImageResource(R.drawable.baseline_pause_circle_outline_24)
                    binding.playBtn.setImageResource(R.drawable.baseline_pause_24)

                    //show the current artwork
                    showCurrentArtwork()

                    //update progress position
                    updatePlayerPositionProgress()

                    //load the artwork animation
                    binding.playerView.artworkView.animation = loadRotation()

                    if (!exoplayer.isPlaying){
                        exoplayer.play()
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                if (playbackState == ExoPlayer.STATE_READY){
                    binding.homeSongName.text = exoplayer.mediaMetadata.title
                    binding.playerView.songNameText.text = exoplayer.mediaMetadata.title

                    binding.playerView.startProgress.text = getReadableTime(exoplayer.currentPosition.toInt())
                    binding.playerView.endProgress.text = getReadableTime(exoplayer.duration.toInt())
                    binding.playerView.seekbar.max = exoplayer.duration.toInt()
                    binding.playerView.seekbar.progress = exoplayer.currentPosition.toInt()

                    binding.playerView.playPauseBtn.setImageResource(R.drawable.baseline_pause_circle_outline_24)
                    binding.playBtn.setImageResource(R.drawable.baseline_pause_24)

                    //show the current artwork
                    showCurrentArtwork()

                    //update progress position
                    updatePlayerPositionProgress()

                    //load the artwork animation
                    binding.playerView.artworkView.animation = loadRotation()
                }else {
                    binding.playerView.playPauseBtn.setImageResource(R.drawable.baseline_play_circle_outline_24)
                    binding.playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
                }
            }
        })

        //skip to next track
        binding.skipNextBtn.setOnClickListener { skipToNext() }
        binding.playerView.skipNextBtn.setOnClickListener { skipToNext() }

        //skip to previous track
        binding.skipPreviousBtn.setOnClickListener { skipToPrevious() }
        binding.playerView.skipPreviousBtn.setOnClickListener { skipToPrevious() }

        //play or pause the player
        binding.playerView.playPauseBtn.setOnClickListener { playOrPausePlayer() }
        binding.playBtn.setOnClickListener { playOrPausePlayer() }

        //seekbar listener
        binding.playerView.seekbar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            var progressValue = 0
            override fun onProgressChanged(seekbar: SeekBar?, p1: Int, p2: Boolean) {
                if (seekbar != null) {
                    progressValue = seekbar.progress
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekbar: SeekBar?) {
                if (exoplayer.playbackState == ExoPlayer.STATE_READY){
                    if (seekbar != null) {
                        seekbar.progress = progressValue
                        binding.playerView.startProgress.text = getReadableTime(progressValue)
                        exoplayer.seekTo(progressValue.toLong())
                    }
                }
            }

        })

        //repeat mode
        binding.playerView.repeatBtn.setOnClickListener {
            if (repeatMode == 1){
                //repeat one
                exoplayer.repeatMode = ExoPlayer.REPEAT_MODE_ONE
                repeatMode = 2

                binding.playerView.repeatBtn.setImageResource(R.drawable.baseline_repeat_one_24)
            }
            else if (repeatMode == 2){
                //repeat all
                exoplayer.shuffleModeEnabled = true
                exoplayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
                binding.playerView.repeatBtn.setImageResource(R.drawable.baseline_shuffle_24)
            }
            else if (repeatMode == 3){
                //repeat all
                exoplayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
                exoplayer.shuffleModeEnabled = false
                repeatMode = 1
                binding.playerView.repeatBtn.setImageResource(R.drawable.baseline_repeat_24)
            }
        }
    }

    private fun playOrPausePlayer(){
        if (exoplayer.isPlaying){
            exoplayer.pause()
            binding.playerView.playPauseBtn.setImageResource(R.drawable.baseline_play_circle_outline_24)
            binding.playBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            binding.playerView.artworkView.clearAnimation()
        }else {
            exoplayer.play()
            binding.playerView.playPauseBtn.setImageResource(R.drawable.baseline_pause_circle_outline_24)
            binding.playBtn.setImageResource(R.drawable.baseline_pause_24)
            binding.playerView.artworkView.startAnimation(loadRotation())
        }
    }

    private fun skipToNext(){
        if (exoplayer.hasNextMediaItem()){
            exoplayer.seekToNext()
        }
    }
    private fun skipToPrevious(){
        if (exoplayer.hasPreviousMediaItem()){
            exoplayer.seekToPrevious()
        }
    }

    private fun loadRotation(): Animation? {
        val rotateAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f)

        rotateAnimation.interpolator = LinearInterpolator()
        rotateAnimation.duration = 10000
        rotateAnimation.repeatCount = Animation.INFINITE
        return rotateAnimation
    }

    private fun updatePlayerPositionProgress() {

        if (exoplayer.isPlaying){
                binding.playerView.startProgress.text = getReadableTime(exoplayer.currentPosition.toInt())
                binding.playerView.seekbar.progress = exoplayer.currentPosition.toInt()
            }
        Handler().postDelayed(runnable, 50)
    }

    private val runnable = Runnable {
        updatePlayerPositionProgress()
    }

    private fun showCurrentArtwork() {
        binding.playerView.artworkView.setImageURI(exoplayer.currentMediaItem!!.mediaMetadata.artworkUri)
        if (binding.playerView.artworkView.drawable == null){
            binding.playerView.artworkView.setImageResource(R.drawable.art_mc)
        }

    }

    private fun getReadableTime(duration: Int): String {
        var time: String
        val hr = duration / (1000*60*60)
        val min = (duration % (1000*60*60)) / (1000*60)
        val sec = (((duration % (1000*60*60)) % (1000*60*60)) % (1000*60)) / 1000

        if (hr < 1){
            time = "$min:$sec"
        }else {
            time = "$hr:$min:$sec"
        }

        return time
    }

    private fun showPlayerView() {
        binding.playerView.root.visibility = View.VISIBLE
    }

    private fun exitPlayerView(){
        binding.playerView.root.visibility = View.GONE
        window.statusBarColor = defaultStatusColor
        window.navigationBarColor = ColorUtils.setAlphaComponent(defaultStatusColor, 199)
    }

    private fun loadSongs() {
        songList = SongAudio().listOfSongs(this)

        binding.songsRecyclerView.setHasFixedSize(true)
        binding.songsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
            false)

        songAdapter = SongAdapter(this, songList, exoplayer, binding.playerView.root)

        binding.songsRecyclerView.adapter = songAdapter
        title = resources.getString(R.string.app_name) + " - ${songList.size}"
        supportActionBar!!.title = title
    }

    private fun allPermissionGranted() = Constants.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext,it) == PackageManager.PERMISSION_GRANTED
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions())
    {permissions->
        var permissionGranted = true
        permissions.entries.forEach{
            if (it.key in Constants.REQUIRED_PERMISSIONS && !it.value){
                permissionGranted = false
            }
        }
        if (!permissionGranted){
            Toast.makeText(baseContext, "Permission request denied", Toast.LENGTH_SHORT).show()
        }else {
            loadSongs()
        }

    }

    private fun requestPermission(){
        permissionLauncher.launch(Constants.REQUIRED_PERMISSIONS)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_btn, menu)

        val menuItem = menu!!.findItem(R.id.search_btn)
        val searchView: SearchView = menuItem.actionView as SearchView

        searchSong(searchView)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoplayer.isPlaying){
            exoplayer.stop()
        }
        exoplayer.release()
    }

    private fun searchSong(searchView: SearchView) {
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                filterSongs(p0!!.lowercase())
                return true
            }

        })
    }

    private fun filterSongs(toLowerCase: String) {
        val filteredSongs: ArrayList<Song> = ArrayList()

        if (songList.size > 0){
            songList.forEach {
                if (it.name.lowercase().contains(toLowerCase)){
                    filteredSongs.add(it)
                }
            }
        }
        songAdapter.filterSongs(filteredSongs)
    }
}