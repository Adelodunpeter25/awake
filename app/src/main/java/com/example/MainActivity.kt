package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AwakeViewModel
import com.example.ui.ConfigurationScreen
import com.example.ui.DashboardScreen
import com.example.ui.theme.MyApplicationTheme

enum class ActiveScreen {
    DASHBOARD,
    CONFIGURATION
}

class MainActivity : ComponentActivity() {

    private val viewModel: AwakeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Safe permission checking for API 33+ to enable FGS notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 101)
            }
        }

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(ActiveScreen.DASHBOARD) }
                val settings by viewModel.settingsState.collectAsStateWithLifecycle()

                when (currentScreen) {
                    ActiveScreen.DASHBOARD -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { currentScreen = ActiveScreen.CONFIGURATION },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ActiveScreen.CONFIGURATION -> {
                        ConfigurationScreen(
                            settings = settings,
                            onBack = { currentScreen = ActiveScreen.DASHBOARD },
                            onSaveSettings = { updatedDuration, batteryEnabled, batteryPct ->
                                viewModel.updateDuration(updatedDuration)
                                viewModel.updateBatteryGuard(batteryEnabled, batteryPct)
                                currentScreen = ActiveScreen.DASHBOARD
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
