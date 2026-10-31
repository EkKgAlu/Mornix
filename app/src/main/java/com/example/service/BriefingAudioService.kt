package com.example.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.util.Locale

class BriefingAudioService : Service(), TextToSpeech.OnInitListener {

    companion object {
        const val ACTION_PLAY = "com.example.mornix.ACTION_PLAY_BRIEFING"
        const val ACTION_STOP = "com.example.mornix.ACTION_STOP_BRIEFING"
        const val EXTRA_SCRIPT = "extra_spoken_script"
        const val EXTRA_TITLE = "extra_briefing_title"
        private const val UTTERANCE_ID = "mornix_notification_speech"
    }

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var pendingScript: String? = null
    private var briefingTitle: String = "Mornix Daily Briefing"

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(applicationContext, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_PLAY -> {
                val script = intent.getStringExtra(EXTRA_SCRIPT)
                briefingTitle = intent.getStringExtra(EXTRA_TITLE) ?: "Mornix Daily Briefing"

                if (!script.isNullOrBlank()) {
                    pendingScript = script
                    startForeground(MornixNotificationHelper.NOTIFICATION_AUDIO_ID, buildPlayingNotification())
                    if (isTtsInitialized) {
                        speakScript(script)
                    }
                }
            }
            ACTION_STOP -> {
                stopPlayback()
            }
        }

        return START_NOT_STICKY
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.ENGLISH
            isTtsInitialized = true
            setupProgressListener()

            pendingScript?.let {
                speakScript(it)
                pendingScript = null
            }
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                stopPlayback()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                stopPlayback()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                stopPlayback()
            }
        })
    }

    private fun speakScript(text: String) {
        tts?.stop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
        } else {
            @Suppress("DEPRECATION")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, hashMapOf(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to UTTERANCE_ID))
        }
    }

    private fun stopPlayback() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.cancel(MornixNotificationHelper.NOTIFICATION_AUDIO_ID)
        stopSelf()
    }

    private fun buildPlayingNotification(): Notification {
        MornixNotificationHelper.createNotificationChannels(this)

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            10,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, BriefingAudioService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                this,
                11,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                this,
                11,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        return NotificationCompat.Builder(this, MornixNotificationHelper.CHANNEL_AUDIO_ID)
            .setSmallIcon(R.drawable.mornix_logo)
            .setContentTitle("🔊 Playing Morning Briefing")
            .setContentText(briefingTitle)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "⏹ Stop Audio",
                stopPendingIntent
            )
            .build()
    }

    override fun onDestroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
