package com.example.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.AwakeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AwakeServiceStatus {
    private val _remainingSeconds = MutableStateFlow<Int?>(null)
    val remainingSeconds: StateFlow<Int?> = _remainingSeconds.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    fun updateRemaining(seconds: Int?) {
        _remainingSeconds.value = seconds
    }

    fun setRunning(running: Boolean) {
        _isServiceRunning.value = running
        if (!running) {
            _remainingSeconds.value = null
        }
    }
}

class AwakeService : Service() {

    companion object {
        const val ACTION_START = "com.example.service.action.START"
        const val ACTION_STOP = "com.example.service.action.STOP"
        private const val CHANNEL_ID = "screen_awake_channel"
        private const val NOTIFICATION_ID = 4829
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    private lateinit var repository: AwakeRepository

    private val serviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    // Manual screen lock by the user releases the screen-awake lock
                    stopAwakeService()
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        val percentage = (level * 100 / scale.toFloat()).toInt()
                        checkBatteryLimit(percentage)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(applicationContext)
        repository = AwakeRepository(database.awakeSettingsDao())

        // Register receivers
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(serviceReceiver, filter)

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAwakeService()
            return START_NOT_STICKY
        }

        // Default or ACTION_START
        startAwakeService()
        return START_STICKY
    }

    private fun startAwakeService() {
        // Safe lock release if already active
        releaseWakeLock()

        AwakeServiceStatus.setRunning(true)

        serviceScope.launch {
            // Write active state to DB
            repository.setAwakeActive(true)

            val settings = repository.getSettings()
            acquireWakeLock()

            // Setup timer based on minutes (-1 is infinite)
            val durationMinutes = settings.durationMinutes
            if (durationMinutes > 0) {
                val totalSeconds = durationMinutes * 60
                startCountdown(totalSeconds)
            } else {
                // Infinite screen awake mode
                AwakeServiceStatus.updateRemaining(null)
                updateNotification("Screen Keep Awake Active", "Set to Infinite stay-awake")
            }
        }
    }

    private fun startCountdown(totalSeconds: Int) {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                AwakeServiceStatus.updateRemaining(remaining)
                val hours = remaining / 3600
                val minutes = (remaining % 3600) / 60
                val seconds = remaining % 60
                val timeString = if (hours > 0) {
                    String.format("%02d:%02d:%02d remaining", hours, minutes, seconds)
                } else if (minutes > 0) {
                    String.format("%02d:%02d remaining", minutes, seconds)
                } else {
                    String.format("%d seconds remaining", seconds)
                }

                updateNotification("Screen Keep Awake Clock active", timeString)
                delay(1000)
                remaining--
            }
            // Once countdown finishes, turn off screen keeper
            stopAwakeService()
        }
    }

    private fun checkBatteryLimit(currentPercentage: Int) {
        serviceScope.launch {
            val settings = repository.getSettings()
            if (settings.batteryThresholdEnabled && currentPercentage <= settings.batteryThreshold) {
                // Instantly shut down to safeguard device battery health
                stopAwakeService()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            // Use SCREEN_BRIGHT_WAKE_LOCK to hold the screen active and bright.
            // ON_AFTER_RELEASE keeps the screen on for a normal delay if released organically.
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                "ScreenAwake::WakeLock"
            ).apply {
                acquire()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAwakeService() {
        timerJob?.cancel()
        releaseWakeLock()
        AwakeServiceStatus.setRunning(false)

        serviceScope.launch {
            repository.setAwakeActive(false)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun updateNotification(title: String, content: String) {
        // Action to Stop Service via system notification tray
        val stopIntent = Intent(this, AwakeService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Open app when notification clicked
        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this,
            1,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery) // Minimalist dynamic system system icon
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Turn Off",
                stopPendingIntent
            )
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Keep Screen Awake Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows screen awake notification status and duration controls"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        timerJob?.cancel()
        releaseWakeLock()
        try {
            unregisterReceiver(serviceReceiver)
        } catch (e: Exception) {
            // Already unregistered or failed
        }
        serviceScope.cancel()
        AwakeServiceStatus.setRunning(false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
