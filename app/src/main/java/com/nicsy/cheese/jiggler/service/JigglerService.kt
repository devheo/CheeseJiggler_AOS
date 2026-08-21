package com.nicsy.cheese.jiggler.service

import android.app.ActivityOptions
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nicsy.cheese.jiggler.MainActivity
import com.nicsy.cheese.jiggler.R
import com.nicsy.cheese.jiggler.layout.AppPreferences
import com.nicsy.cheese.jiggler.remote.FirebaseRemoteManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class JigglerService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private val CHANNEL_ID = "jiggler_service_channel_v2"
    private val NOTIFICATION_ID = 1

    private lateinit var remoteManager: FirebaseRemoteManager
    private lateinit var prefs: AppPreferences
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var isJiggling = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        remoteManager = FirebaseRemoteManager(applicationContext)
        prefs = AppPreferences(applicationContext)

        startRemoteListener()
    }

    private fun startRemoteListener() {
        serviceScope.launch {
            remoteManager.initializeDevice { }
            remoteManager.startListening(object : FirebaseRemoteManager.RemoteCommandCallback {
                override fun onCommandReceived(command: String) {
                    Log.d("JigglerService", "Remote command received: $command")
                    when (command) {
                        "START" -> startJiggling()
                        "STOP" -> stopJiggling()
                        "STATUS_CHECK" -> updateStatus()
                    }
                }
            })
        }
    }

    private fun startJiggling() {
        isJiggling = true
        updateStatus()
        
        // Use high-priority notification with Full Screen Intent to bring app to foreground
        updateNotification("Active - Jiggling...", isFullScreen = true)
        
        // Launch MainActivity to show the animation and keep screen on
        val activityIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_REMOTE_START
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        
        startActivity(activityIntent)

        Log.d("JigglerService", "Sending START broadcast")
        // Broadcast to Activity if it's already open (Target specific package for reliability)
        sendBroadcast(Intent(ACTION_STATE_CHANGED).apply {
            setPackage(packageName)
            putExtra(EXTRA_IS_RUNNING, true)
        })
    }

    private fun stopJiggling() {
        Log.d("JigglerService", "stopJiggling() called")
        isJiggling = false
        releaseWakeLock()
        updateStatus()
        updateNotification("Idle - Waiting for command")

        Log.d("JigglerService", "Sending STOP broadcast")
        sendBroadcast(Intent(ACTION_STATE_CHANGED).apply {
            setPackage(packageName)
            putExtra(EXTRA_IS_RUNNING, false)
        })

        // Ensure service actually stops if requested via remote
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateStatus() {
        remoteManager.updateStatus(
            isRunning = isJiggling,
            mode = prefs.jiggleMode.name,
            speed = prefs.speedMultiplier,
            tileType = prefs.tileType.name,
            isForeground = false // Service always reports background unless Activity updates it
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification("Idle - Waiting for command", false), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, createNotification("Idle - Waiting for command", false))
        }
        acquireWakeLock()
        
        return START_STICKY
    }

    private fun createNotification(statusText: String, isFullScreen: Boolean): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            if (isFullScreen) {
                action = ACTION_REMOTE_START
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)

        if (isFullScreen) {
            builder.setFullScreenIntent(pendingIntent, true)
        }

        return builder.build()
    }

    private fun updateNotification(statusText: String, isFullScreen: Boolean = false) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(statusText, isFullScreen))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Jiggler Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority channel for remote control"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "CheeseJiggler::WakeLock"
        ).apply {
            acquire(10 * 60 * 1000L /*10 minutes*/)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        releaseWakeLock()
        remoteManager.stopListening()
        // Cancel the scope to prevent leaks
        serviceScope.coroutineContext[Job]?.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.nicsy.cheese.jiggler.ACTION_STOP"
        const val ACTION_REMOTE_START = "com.nicsy.cheese.jiggler.ACTION_REMOTE_START"
        const val ACTION_STATE_CHANGED = "com.nicsy.cheese.jiggler.ACTION_STATE_CHANGED"
        const val EXTRA_IS_RUNNING = "extra_is_running"
    }
}
