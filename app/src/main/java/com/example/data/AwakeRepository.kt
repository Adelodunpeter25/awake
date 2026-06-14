package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AwakeRepository(private val dao: AwakeSettingsDao) {

    val settingsFlow: Flow<AwakeSettings> = dao.getSettingsFlow().map {
        it ?: AwakeSettings() // Return default if nothing is stored yet
    }

    suspend fun getSettings(): AwakeSettings {
        return dao.getSettings() ?: AwakeSettings()
    }

    suspend fun updateSettings(settings: AwakeSettings) {
        dao.insertSettings(settings)
    }

    suspend fun setAwakeActive(isActive: Boolean) {
        val current = getSettings()
        dao.insertSettings(current.copy(isAwakeActive = isActive))
    }

    suspend fun updateDuration(minutes: Int) {
        val current = getSettings()
        dao.insertSettings(current.copy(durationMinutes = minutes))
    }

    suspend fun updateBatteryThreshold(enabled: Boolean, threshold: Int) {
        val current = getSettings()
        dao.insertSettings(
            current.copy(
                batteryThresholdEnabled = enabled,
                batteryThreshold = threshold
            )
        )
    }
}
