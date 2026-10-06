package com.kurbat.albionradar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState

@Composable
fun ApplicationScope.AppWindow(
    showOverlay: MutableState<Boolean> // todo del
) {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Albion Radar",
        state = rememberWindowState(
            size = DpSize(1400.dp, 900.dp)
        )
    ) {
        App(
            isDebugBuild = JvmMainBuildConfig.IS_DEBUG_BUILD,
            onToggleOverlay = { showOverlay.value = !showOverlay.value }
        )
    }
}
