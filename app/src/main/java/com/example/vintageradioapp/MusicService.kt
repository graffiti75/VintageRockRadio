package com.example.vintageradioapp

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.vintageradioapp.data.Song
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class MusicService : Service() {

    private var youTubePlayer: YouTubePlayer? = null
    private val binder = MusicBinder()
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "MusicServiceChannel"
    private var currentSong: Song? = null
    private var isPlaying = false

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY -> currentSong?.let { play(it) }
                ACTION_PAUSE -> pause()
            }
        }
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(notificationReceiver, filter)
        }

        startForeground(NOTIFICATION_ID, createNotification())

        val playerView = YouTubePlayerView(this)
        val options = IFramePlayerOptions.Builder().controls(0).build()
        playerView.enableAutomaticInitialization = false
        playerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                this@MusicService.youTubePlayer = youTubePlayer
                youTubePlayer.addListener(youtubePlayerListener)
            }
        }, true, options)
    }

    private val youtubePlayerListener = object : AbstractYouTubePlayerListener() {
        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
            // Not needed for now
        }

        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            // Not needed for now
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
    }

    fun play(song: Song) {
        this.currentSong = song
        this.isPlaying = true
        youTubePlayer?.loadVideo(song.youtubeId, 0f)
        updateNotification()
    }

    fun pause() {
        this.isPlaying = false
        youTubePlayer?.pause()
        updateNotification()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Music Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(): Notification {
        val playIntent = Intent(this, MusicService::class.java).apply { action = ACTION_PLAY }
        val playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val pauseIntent = Intent(this, MusicService::class.java).apply { action = ACTION_PAUSE }
        val pausePendingIntent = PendingIntent.getBroadcast(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentSong?.songTitle ?: "Vintage Radio")
            .setContentText(currentSong?.band ?: "Now Playing")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)

        if (isPlaying) {
            notificationBuilder.addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)
        } else {
            notificationBuilder.addAction(R.drawable.ic_play, "Play", playPendingIntent)
        }

        return notificationBuilder.build()
    }

    companion object {
        const val ACTION_PLAY = "com.example.vintageradioapp.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.vintageradioapp.ACTION_PAUSE"
    }
}
