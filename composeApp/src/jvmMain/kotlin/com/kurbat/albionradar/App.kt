package com.kurbat.albionradar

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kurbat.albionradar.navigation.Screen
import com.kurbat.albionradar.overlay.presentation.composables.MainScreen
import com.kurbat.albionradar.settings.presentation.composables.SettingsScreen

@Composable
fun App(isDebugBuild: Boolean, onToggleOverlay: () -> Unit) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Radar) }

    MaterialTheme {
        when (currentScreen) {
            Screen.Radar -> MainScreen(
                onNavigateToSettings = { currentScreen = Screen.Settings },
                onToggleOverlay = onToggleOverlay
            )

            Screen.Settings -> {
                SettingsScreen(
                    isDebugBuild = isDebugBuild,
                    onBack = { currentScreen = Screen.Radar }
                )
            }
        }
    }
}
