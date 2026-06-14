package com.example.service

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.data.AppDatabase
import com.example.data.AwakeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AwakeTileService : TileService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var repository: AwakeRepository

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(applicationContext)
        repository = AwakeRepository(database.awakeSettingsDao())
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isRunning = AwakeServiceStatus.isServiceRunning.value

        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Screen Awake"

        serviceScope.launch {
            val settings = repository.getSettings()
            tile.subtitle = if (isRunning) {
                if (settings.durationMinutes > 0) "${settings.durationMinutes}m duration" else "Infinite"
            } else {
                "Tapped to Wake"
            }
            tile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val isRunning = AwakeServiceStatus.isServiceRunning.value
        if (isRunning) {
            val intent = Intent(this, AwakeService::class.java).apply {
                action = AwakeService.ACTION_STOP
            }
            startService(intent)
        } else {
            val intent = Intent(this, AwakeService::class.java).apply {
                action = AwakeService.ACTION_START
            }
            startService(intent)
        }

        serviceScope.launch {
            kotlinx.coroutines.delay(200)
            updateTileState()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
