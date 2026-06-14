package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AwakeRepository
import com.example.data.AwakeSettings
import com.example.service.AwakeService
import com.example.service.AwakeServiceStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AwakeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AwakeRepository
    val settingsState: StateFlow<AwakeSettings>
    val remainingSeconds: StateFlow<Int?> = AwakeServiceStatus.remainingSeconds
    val isRunning: StateFlow<Boolean> = AwakeServiceStatus.isServiceRunning

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AwakeRepository(database.awakeSettingsDao())

        settingsState = repository.settingsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AwakeSettings()
        )
    }

    fun toggleAwake() {
        val currentlyRunning = isRunning.value
        val intent = Intent(getApplication(), AwakeService::class.java)

        if (currentlyRunning) {
            intent.action = AwakeService.ACTION_STOP
            getApplication<Application>().startService(intent)
        } else {
            intent.action = AwakeService.ACTION_START
            getApplication<Application>().startService(intent)
        }
    }

    fun updateDuration(minutes: Int) {
        viewModelScope.launch {
            repository.updateDuration(minutes)

            // If the service is running, restart to immediately live-apply the new timer constraints
            if (isRunning.value) {
                val intent = Intent(getApplication(), AwakeService::class.java).apply {
                    action = AwakeService.ACTION_START
                }
                getApplication<Application>().startService(intent)
            }
        }
    }

    fun updateBatteryGuard(enabled: Boolean, percentage: Int) {
        viewModelScope.launch {
            repository.updateBatteryThreshold(enabled, percentage)
        }
    }
}
