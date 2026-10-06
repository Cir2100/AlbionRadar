package com.kurbat.albionradar.settings.presentation.composables

import androidx.compose.runtime.Composable
import com.kurbat.albionradar.settings.presentation.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    isDebugBuild: Boolean,
    onBack: () -> Unit
) {
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    SettingsScreenContent(
        viewModel = settingsViewModel,
        isDebugBuild = isDebugBuild,
        onBack = onBack
    )
}
