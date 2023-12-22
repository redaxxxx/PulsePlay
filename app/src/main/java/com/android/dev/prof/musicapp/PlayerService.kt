package com.android.dev.prof.musicapp

import android.app.Notification
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.google.android.exoplayer2.audio.AudioAttributes
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import java.util.Objects
import com.android.dev.prof.musicapp.PlayerService

class PlayerService() : Service() {

    //member
    private val serviceBinder: IBinder = ServiceBinder()

    //exoplayer
    lateinit var exoPlayer: ExoPlayer
    lateinit var playerNotificationManager: PlayerNotificationManager


    //class binder for clients
    inner class ServiceBinder : Binder() {
        fun getPlayerService(): PlayerService = this@PlayerService
    }

    fun getMediaPlayer(): ExoPlayer{
        return exoPlayer
    }

    override fun onBind(intent: Intent): IBinder {
        return serviceBinder
    }

    override fun onCreate() {
        super.onCreate()

        exoPlayer = ExoPlayer.Builder(applicationContext).build()

        Log.d("MusicApp", "exoplayer: $exoPlayer")

        //audio focus attribute
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        exoPlayer.setAudioAttributes(audioAttributes, true)

        //Notification manager
        val channelId = resources.getString(R.string.app_name) + "Music Channel"
        val notificationId = 11111

        playerNotificationManager = PlayerNotificationManager.Builder(this, notificationId, channelId)
            .setChannelImportance(IMPORTANCE_HIGH)
            .setNotificationListener(notificationListener)
            .setMediaDescriptionAdapter(descriptionAdapter)
            .setChannelDescriptionResourceId(R.string.app_name)
            .setSmallIconResourceId(R.drawable.baseline_notifications_24)
            .setNextActionIconResourceId(R.drawable.baseline_skip_next_24)
            .setPreviousActionIconResourceId(R.drawable.baseline_skip_previous_24)
            .setPauseActionIconResourceId(R.drawable.baseline_pause_24)
            .setPlayActionIconResourceId(R.drawable.baseline_play_arrow_24)
            .setChannelNameResourceId(R.string.app_name)
            .build()

        //set player to notification manager
        playerNotificationManager.setPlayer(exoPlayer)
        playerNotificationManager.setPriority(NotificationCompat.PRIORITY_MAX)
        playerNotificationManager.setUseRewindAction(false)
        playerNotificationManager.setUseFastForwardAction(false)
    }

    override fun onDestroy() {
        //release the player
        if (exoPlayer.isPlaying){
            exoPlayer.stop()
        }
        playerNotificationManager.setPlayer(null)
        exoPlayer.release()
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    //notification listener
    val notificationListener = object: PlayerNotificationManager.NotificationListener{
        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            super.onNotificationCancelled(notificationId, dismissedByUser)
            stopForeground(true)
            if (exoPlayer.isPlaying){
                exoPlayer.pause()
            }
        }

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            super.onNotificationPosted(notificationId, notification, ongoing)

            startForeground(notificationId, notification)
        }
    }

    //notification description adapter
    val descriptionAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter{
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return player.currentMediaItem?.mediaMetadata?.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val openAppIntent = Intent(applicationContext, MainActivity::class.java)
            return PendingIntent.getActivity(applicationContext, 0, openAppIntent,
                PendingIntent.FLAG_IMMUTABLE)
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return null
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            //try creating an Image view on the fly then get its drawable
            val imageView = ImageView(applicationContext)
            imageView.setImageURI(player.currentMediaItem?.mediaMetadata?.artworkUri)

            //get view drawable
            var bitmapDrawable = imageView.drawable as? BitmapDrawable
            if (bitmapDrawable == null){
                bitmapDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.art_mc) as BitmapDrawable
            }

            assert(bitmapDrawable != null)

            return bitmapDrawable.bitmap
        }

    }


}